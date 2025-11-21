package shoestore.helper;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hỗ trợ luồng OAuth 2.0 của Google để lấy email Gmail phục vụ đăng nhập nhanh.
 */
public final class GoogleOAuthHelper {

    private static final Logger LOGGER = Logger.getLogger(GoogleOAuthHelper.class.getName());
    private static final String CLIENT_ID = System.getenv("GOOGLE_CLIENT_ID"); // Giải thích: đọc Client ID từ biến môi trường để tránh hard-code khóa bí mật.
    private static final String CLIENT_SECRET = System.getenv("GOOGLE_CLIENT_SECRET"); // Giải thích: Client Secret cũng được cấp phát trong Google Cloud Console.
    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
    private static final Pattern JSON_VALUE_PATTERN = Pattern.compile("\"(\\w+)\"\\s*:\\s*\"([^\"]*)\""); // Giải thích: regex nhỏ gọn để rút giá trị từ JSON trả về mà không cần thêm thư viện.

    private GoogleOAuthHelper() {
    }

    public static String authenticateAndGetEmail() throws IOException {
        ensureConfigured(); // Giải thích: nếu chưa cấu hình Client ID/Secret thì dừng ngay để người dùng biết cách xử lý.
        int port = findFreePort(); // Giải thích: chọn port ngẫu nhiên để dựng web server mini nhận callback OAuth.
        String redirectUri = "http://localhost:" + port + "/oauth2callback";
        String state = UUID.randomUUID().toString(); // Giải thích: state ngẫu nhiên giúp chống tấn công CSRF.
        CompletableFuture<String> codeFuture = new CompletableFuture<>();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0); // Giải thích: HttpServer nhẹ để tiếp nhận phản hồi Google.
        server.createContext("/oauth2callback", exchange -> handleCallback(exchange, state, codeFuture));
        server.start();
        try {
            String authUrl = buildAuthorizationUrl(redirectUri, state); // Giải thích: chuẩn bị URL Google yêu cầu quyền email.
            openBrowser(authUrl); // Giải thích: mở trình duyệt mặc định để người dùng đăng nhập Gmail.
            String code = waitForCode(codeFuture); // Giải thích: chờ tối đa 3 phút để người dùng hoàn tất bước xác thực.
            return fetchEmailFromCode(code, redirectUri); // Giải thích: đổi mã code lấy access token rồi truy vấn email.
        } finally {
            server.stop(0); // Giải thích: luôn dừng server mini để giải phóng port dù thành công hay thất bại.
        }
    }

    private static void ensureConfigured() {
        if (CLIENT_ID == null || CLIENT_ID.isBlank() || CLIENT_SECRET == null || CLIENT_SECRET.isBlank()) {
            throw new IllegalStateException("Chưa cấu hình GOOGLE_CLIENT_ID/GOOGLE_CLIENT_SECRET cho đăng nhập Gmail");
        }
    }

    private static int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort(); // Giải thích: truyền 0 để hệ điều hành tự cấp port còn trống.
        }
    }

    private static void handleCallback(HttpExchange exchange, String expectedState, CompletableFuture<String> codeFuture) throws IOException {
        Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
        String responseMessage;
        if (params.containsKey("error")) {
            responseMessage = "Google báo lỗi: " + params.get("error");
            completeExceptionally(codeFuture, new IOException(responseMessage));
        } else if (!Objects.equals(expectedState, params.get("state"))) {
            responseMessage = "State không khớp, từ chối yêu cầu.";
            completeExceptionally(codeFuture, new IOException(responseMessage));
        } else {
            String code = params.get("code");
            if (code == null || code.isBlank()) {
                responseMessage = "Không nhận được mã xác thực";
                completeExceptionally(codeFuture, new IOException(responseMessage));
            } else {
                responseMessage = "Đăng nhập Gmail thành công, bạn có thể quay lại ứng dụng.";
                codeFuture.complete(code); // Giải thích: gửi code về thread chính để tiếp tục đổi token.
            }
        }
        writeHtmlResponse(exchange, responseMessage);
    }

    private static void completeExceptionally(CompletableFuture<String> codeFuture, IOException exception) {
        if (!codeFuture.isDone()) {
            codeFuture.completeExceptionally(exception); // Giải thích: đảm bảo thread chính nhận được nguyên nhân thất bại.
        }
    }

    private static Map<String, String> parseQuery(String query) throws IOException {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isBlank()) {
            return params;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            params.put(key, value);
        }
        return params;
    }

    private static void writeHtmlResponse(HttpExchange exchange, String message) throws IOException {
        String html = "<html><body><h3>" + message + "</h3><p>Bạn có thể đóng tab này.</p></body></html>";
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String buildAuthorizationUrl(String redirectUri, String state) throws IOException {
        StringBuilder builder = new StringBuilder(AUTH_URL);
        builder.append("?response_type=code");
        builder.append("&client_id=").append(URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8));
        builder.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));
        builder.append("&scope=").append(URLEncoder.encode("openid email profile", StandardCharsets.UTF_8));
        builder.append("&access_type=offline&prompt=select_account");
        builder.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8));
        return builder.toString();
    }

    private static void openBrowser(String url) throws IOException {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(url)); // Giải thích: mở URL trong trình duyệt mặc định của máy.
            } catch (URISyntaxException e) {
                throw new IOException("URL đăng nhập không hợp lệ", e);
            }
        } else {
            LOGGER.log(Level.WARNING, "Máy không hỗ trợ Desktop.BROWSE, vui lòng mở URL thủ công: {0}", url);
        }
    }

    private static String waitForCode(CompletableFuture<String> codeFuture) throws IOException {
        try {
            return codeFuture.get(180, TimeUnit.SECONDS); // Giải thích: timeout 3 phút giúp ứng dụng không treo vô hạn.
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Luồng đợi phản hồi bị gián đoạn", ex);
        } catch (ExecutionException | TimeoutException ex) {
            throw new IOException("Không thể nhận mã đăng nhập từ Google", ex);
        }
    }

    private static String fetchEmailFromCode(String code, String redirectUri) throws IOException {
        String accessToken = exchangeCodeForAccessToken(code, redirectUri); // Giải thích: bước 2 của OAuth là đổi code sang token.
        return requestEmail(accessToken); // Giải thích: dùng token để đọc thông tin tài khoản (bao gồm email).
    }

    private static String exchangeCodeForAccessToken(String code, String redirectUri) throws IOException {
        String body = "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                + "&client_id=" + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&grant_type=authorization_code";
        HttpURLConnection connection = (HttpURLConnection) new URL(TOKEN_URL).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        try (OutputStream os = connection.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        String response = readResponse(connection);
        String token = extractJsonValue(response, "access_token");
        if (token == null || token.isBlank()) {
            throw new IOException("Không lấy được access token từ Google");
        }
        return token;
    }

    private static String requestEmail(String accessToken) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(USERINFO_URL).openConnection();
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        String response = readResponse(connection);
        String email = extractJsonValue(response, "email");
        if (email == null || email.isBlank()) {
            throw new IOException("Google không trả về email Gmail");
        }
        return email;
    }

    private static String readResponse(HttpURLConnection connection) throws IOException {
        int status = connection.getResponseCode();
        InputStream stream = status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream();
        if (stream == null) {
            throw new IOException("Google trả về mã " + status + " nhưng không có nội dung chi tiết");
        }
        byte[] bytes = stream.readAllBytes();
        String response = new String(bytes, StandardCharsets.UTF_8);
        if (status < 200 || status >= 300) {
            throw new IOException("Google trả về lỗi: " + response);
        }
        return response;
    }

    private static String extractJsonValue(String json, String key) {
        Matcher matcher = JSON_VALUE_PATTERN.matcher(json);
        while (matcher.find()) {
            if (key.equals(matcher.group(1))) {
                return matcher.group(2); // Giải thích: trả ngay khi gặp field đúng tên.
            }
        }
        return null;
    }
}
