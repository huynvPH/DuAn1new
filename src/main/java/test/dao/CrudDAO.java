/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package test.dao;

import java.util.List;

/**
 *
 * @author DINH SANG
 */
public interface CrudDAO<T, ID> {

    T create(T entity);

    void update(T entity);

    void deletebyId(ID id);

    List<T> findAll();

    T findById(ID id);
}
