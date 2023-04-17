package com.example.demo.dao;

import com.example.demo.entity.Employee;

import java.util.List;

public interface EmployeeDao
{
    List<Employee> findAll();

        void insertEmployee(Employee emp);

        void updateEmployee(Employee emp);

        void executeUpdateEmployee(Employee emp);

        public void deleteEmployee(Employee emp);


}
