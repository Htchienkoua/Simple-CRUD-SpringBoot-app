package com.example.demo.service;

import com.example.demo.dao.EmployeeDao;
import com.example.demo.entity.Employee;
import jakarta.annotation.Resource;

import java.util.List;

public class EmployeeServiceImpl implements EmployeeService{
    @Resource
    EmployeeDao employeeDao;


    @Override
    public List<Employee> findAll(){
        return employeeDao.findAll();
    }

    @Override
    public void insertEmployee(Employee emp){
        employeeDao.insertEmployee(emp);
    }

    @Override
    public void updateEmployee(Employee emp){
        employeeDao.updateEmployee(emp);
    }

    @Override
    public void executeUpdateEmployee(Employee emp){
        employeeDao.executeUpdateEmployee(emp);
    }

    @Override
    public void deleteEmployee(Employee emp){
        employeeDao.deleteEmployee(emp);
    }

}
