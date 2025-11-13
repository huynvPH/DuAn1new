/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.dao.impl;

import java.util.List;
import test.dao.DotGiamGiaDAO;
import test.entity.DotGiamGia;
import test.until.XJdbc;
import test.until.XQuery;

/**
 *
 * @author DINH SANG
 */
public class DotGiamGiaDAOImpl implements DotGiamGiaDAO{

    @Override
    public DotGiamGia create(DotGiamGia entity) {
    //    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    String sql = "INSERT INTO DotGiamGia (TenDotGiamGia, NgayBatDau, NgayKetThuc, PhanTramGiam, TrangThai) " +
             "VALUES (?, ?, ?, ?, ?)";
    XJdbc.executeUpdate(sql, 
            entity.getTenDotGiamGia(),
            entity.getNgayBatDau(),
            entity.getNgayKetThuc(),
            entity.getPhanTramGiam(),
            entity.getTrangThai());
    return entity;
    }

    @Override
    public void update(DotGiamGia entity) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void deletebyId(Integer id) {
    //    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    String sql = "DELETE FROM DotGiamGia WHERE IdDotGiamGia = ?";
    XJdbc.executeUpdate(sql, id);
    }

    @Override
    public List<DotGiamGia> findAll() {
        String sql = "SELECT * FROM DotGiamGia";
        return XQuery.getBeanList(DotGiamGia.class, sql);
    }

    @Override
    public DotGiamGia findById(Integer id) {
    //    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        String sql = "SELECT [IdDotGiamGia]\n" +
"  FROM [GIAYTHETHAO].[dbo].[DotGiamGia]";
        return XQuery.getSingleBean(DotGiamGia.class, sql, id);
    }
    
}
