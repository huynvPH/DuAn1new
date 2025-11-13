/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author DINH SANG
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DotGiamGia {
    private int idDotGiamGia;
    private String tenDotGiamGia;
    private Date ngayBatDau;
    private Date ngayKetThuc;
    private int phanTramGiam;
    private int trangThai;
}
