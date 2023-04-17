Set up a Spring Boot Application with PostgreSQL
In this article, we will see the steps to set up a Spring boot application with PostgreSQL.
Joydip Kumar user avatar by Joydip Kumar  · Jan. 11, 19 · Tutorial
Like (20)
Comment (4)
Save
Tweet
Share
452.82K Views
Join the DZone community and get the full member experience. JOIN FOR FREE
In this article, we will see the steps to set up a Spring Boot application with PostgreSQL. We will have a simple CRUD operation in Postgres Database by exposing the application via Rest API. We will use POSTMAN to test the application.

Setting up Postgres Server
Download the Postgres server from the link: https://www.postgresql.org/download/
Run the installer. It will also ask the password for the superuser: postgres
Click on pgAdmin4.exe located inside the PostgreSQL folder inside Program Files.
Setting up Spring Boot Application
Prerequisite:
Have JDK 1.8 installed

Download a sample Spring Boot project from https://start.spring.io/
Update the pom.xml as below:
1
<?xml version="1.0" encoding="UTF-8"?>
2
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
3
xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
4
<modelVersion>4.0.0</modelVersion>
5
<parent>
6
<groupId>org.springframework.boot</groupId>
7
<artifactId>spring-boot-starter-parent</artifactId>
8
<version>2.1.1.RELEASE</version>
9
<relativePath /> <!-- lookup parent from repository -->
10
</parent>
11
<groupId>com.sample</groupId>
12
<artifactId>postgress</artifactId>
13
<version>0.0.1-SNAPSHOT</version>
14
<name>postgress</name>
15
<description>Demo project for Spring Boot</description>
16
​
17
<properties>
18
<java.version>1.8</java.version>
19
</properties>
20
​
21
<dependencies>
22
<dependency>
23
<groupId>org.springframework.boot</groupId>
24
<artifactId>spring-boot-starter</artifactId>
25
</dependency>
26
<dependency>
27
<groupId>org.springframework.boot</groupId>
28
<artifactId>spring-boot-starter-web</artifactId>
29
</dependency>
30
<dependency>
31
<groupId>org.springframework.boot</groupId>
32
<artifactId>spring-boot-starter-jdbc</artifactId>
33
</dependency>
34
<dependency>
35
<groupId>org.postgresql</groupId>
36
<artifactId>postgresql</artifactId>
37
<scope>runtime</scope>
38
</dependency>
39
<dependency>
40
<groupId>org.springframework.boot</groupId>
41
<artifactId>spring-boot-starter-test</artifactId>
42
<scope>test</scope>
43
</dependency>
44
</dependencies>
45
​
46
<build>
47
<plugins>
48
<plugin>
49
<groupId>org.springframework.boot</groupId>
50
<artifactId>spring-boot-maven-plugin</artifactId>
51
</plugin>
52
</plugins>
53
</build>
54
​
55
</project>
spring-boot-starter-jdbc artifact will give all the spring jdbc related jars

org.postgresql.postgresql will have the dependency of postgres jdbc driver in runtime.

Create a schema.sql in resource folder. An employee table will be created in server startup. This can be ignored if you don't want the initial database to be configured during server start. Generally, for building a production-ready application, this step can be ignored as tables will be created with scrip directly in the DB.
1
CREATE TABLE employee
2
(
3
employeeName varchar(100) NOT NULL,
4
employeeId varchar(11) NOT NULL ,
5
employeeAddress varchar(100) DEFAULT NULL,
6
employeeEmail varchar(100) DEFAULT NULL,
7
PRIMARY KEY (employeeId)
8
);
Create data.sql in resource folder for loading the first set of employee during startup. Can be skipped otherwise:
insert into employee(employeeId, employeeName , employeeAddress,employeeEmail) values('1','Jack','USA','jack@gmail.com');

Changes in application.properties to configure the data source with URL, username, and password of the Postgres DB. 5432 is the default port of Postgres. Hibernate will automatically pick up the postgressSQLDialect.
1
spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.PostgreSQLDialect
2
spring.jpa.hibernate.ddl-auto=none
3
spring.jpa.hibernate.show-sql=true
4
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
5
spring.datasource.username=postgres
6
spring.datasource.password=admin
7
​
8
​
9
spring.datasource.initialization-mode=always
10
spring.datasource.initialize=true
11
spring.datasource.schema=classpath:/schema.sql
12
spring.datasource.continue-on-error=true
spring.jpa.hibernate.ddl-auto will turn off the hibernate auto-creation of the tables from the entity objects. Generally, Hibernate runs it if there is an Entity defined. But we will be using a native SQL query with JdbcTemplate, hence, we can turn this off as we will not be creating an Entity.

spring.datasource.initialization-mode is marked as always as we want initialization of the database to happen on every startup. This is optional and made for this sample purpose.

spring.datasource.initialize=true will mark the initialization to be true.

spring.datasource.continue-on-error=true will continue application startup in spite of any errors in data initialization.

spring.datasource.schema is the schema path that needs to be initialized.

spring.datasource.url URL of the Postgres DB. It can be a remote DB as well.

spring.datasource.username username for the database.

spring.datasource.password password for the database.

Create a dao interface and dao implementation.
1
package com.sample.postgress.dao;
2
​
3
import java.util.List;
4
​
5
import com.sample.postgress.entity.Employee;
6
​
7
public interface EmployeeDao {
8
​
9
List<Employee> findAll();
10
​
11
void insertEmployee(Employee emp);
12
​
13
void updateEmployee(Employee emp);
14
​
15
void executeUpdateEmployee(Employee emp);
16
​
17
public void deleteEmployee(Employee emp);
18
}
1
package com.sample.postgress.dao;
2
​
3
import java.sql.PreparedStatement;
4
import java.sql.SQLException;
5
import java.util.HashMap;
6
import java.util.List;
7
import java.util.Map;
8
​
9
import org.springframework.dao.DataAccessException;
10
import org.springframework.jdbc.core.PreparedStatementCallback;
11
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
12
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
13
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
14
import org.springframework.jdbc.support.GeneratedKeyHolder;
15
import org.springframework.jdbc.support.KeyHolder;
16
import org.springframework.stereotype.Repository;
17
​
18
import com.sample.postgress.entity.Employee;
19
import com.sample.postgress.mapper.EmployeeRowMapper;
20
@Repository
21
public class EmployeeDaoImpl implements EmployeeDao{
22
​
23
public EmployeeDaoImpl(NamedParameterJdbcTemplate template) {  
24
this.template = template;  
25
}  
26
NamedParameterJdbcTemplate template;  
27
​
28
@Override
29
public List<Employee> findAll() {
30
return template.query("select * from employee", new EmployeeRowMapper());
31
}
32
@Override
33
public void insertEmployee(Employee emp) {
34
final String sql = "insert into employee(employeeId, employeeName , employeeAddress,employeeEmail) values(:employeeId,:employeeName,:employeeEmail,:employeeAddress)";
35
​
36
KeyHolder holder = new GeneratedKeyHolder();
37
SqlParameterSource param = new MapSqlParameterSource()
38
.addValue("employeeId", emp.getEmployeeId())
39
.addValue("employeeName", emp.getEmployeeName())
40
.addValue("employeeEmail", emp.getEmployeeEmail())
41
.addValue("employeeAddress", emp.getEmployeeAddress());
42
template.update(sql,param, holder);
43
​
44
}
45
​
46
@Override
47
public void updateEmployee(Employee emp) {
48
final String sql = "update employee set employeeName=:employeeName, employeeAddress=:employeeAddress, employeeEmail=:employeeEmail where employeeId=:employeeId";
49
​
50
KeyHolder holder = new GeneratedKeyHolder();
51
SqlParameterSource param = new MapSqlParameterSource()
52
.addValue("employeeId", emp.getEmployeeId())
53
.addValue("employeeName", emp.getEmployeeName())
54
.addValue("employeeEmail", emp.getEmployeeEmail())
55
.addValue("employeeAddress", emp.getEmployeeAddress());
56
template.update(sql,param, holder);
57
​
58
}
59
​
60
@Override
61
public void executeUpdateEmployee(Employee emp) {
62
final String sql = "update employee set employeeName=:employeeName, employeeAddress=:employeeAddress, employeeEmail=:employeeEmail where employeeId=:employeeId";
63
​
64
​
65
Map<String,Object> map=new HashMap<String,Object>();  
66
map.put("employeeId", emp.getEmployeeId());
67
map.put("employeeName", emp.getEmployeeName());
68
map.put("employeeEmail", emp.getEmployeeEmail());
69
map.put("employeeAddress", emp.getEmployeeAddress());
70
​
71
template.execute(sql,map,new PreparedStatementCallback<Object>() {  
72
@Override  
73
public Object doInPreparedStatement(PreparedStatement ps)  
74
throws SQLException, DataAccessException {  
75
return ps.executeUpdate();  
76
}  
77
});  
78
​
79
​
80
}
81
​
82
@Override
83
public void deleteEmployee(Employee emp) {
84
final String sql = "delete from employee where employeeId=:employeeId";
85
​
86
​
87
Map<String,Object> map=new HashMap<String,Object>();  
88
map.put("employeeId", emp.getEmployeeId());
89
​
90
template.execute(sql,map,new PreparedStatementCallback<Object>() {  
91
@Override  
92
public Object doInPreparedStatement(PreparedStatement ps)  
93
throws SQLException, DataAccessException {  
94
return ps.executeUpdate();  
95
}  
96
});  
97
​
98
​
99
}
100
​
101
}
findAll() retrieves all the employee and then map the resultset to a Employee Object using RowMapper described below .

insertEmployee() will insert an employee using template.update(sql,param, holder) where param is the SqlParameterSource, which will map the values dynamically in the query marked with a colon. GeneratedKeyHolder will return an auto-generated value when  data is inserted.

executeUpdateEmployee() will update the employee using template.execute

1
template.execute(sql,map,new PreparedStatementCallback<Object>() {  
2
@Override  
3
public Object doInPreparedStatement(PreparedStatement ps)  
4
throws SQLException, DataAccessException {  
5
return ps.executeUpdate();  
6
}  
7
});
EmployeeRowMapper to map the result set retrieved from the select query with the POJO.
1
package com.sample.postgress.mapper;
2
​
3
import java.sql.ResultSet;
4
import java.sql.SQLException;
5
​
6
import org.springframework.jdbc.core.RowMapper;
7
​
8
import com.sample.postgress.entity.Employee;
9
​
10
public class EmployeeRowMapper implements RowMapper<Employee> {
11
​
12
@Override
13
public Employee mapRow(ResultSet rs, int arg1) throws SQLException {
14
Employee emp = new Employee();
15
emp.setEmployeeId(rs.getString("employeeId"));
16
emp.setEmployeeName(rs.getString("employeeName"));
17
emp.setEmployeeEmail(rs.getString("employeeEmail"));
18
​
19
return emp;
20
}
21
​
22
​
23
}
You can create a controller and a service class as follows:
1
package com.sample.postgress.controller;
2
​
3
import java.util.List;
4
​
5
import javax.annotation.Resource;
6
​
7
import org.springframework.web.bind.annotation.DeleteMapping;
8
import org.springframework.web.bind.annotation.GetMapping;
9
import org.springframework.web.bind.annotation.PostMapping;
10
import org.springframework.web.bind.annotation.PutMapping;
11
import org.springframework.web.bind.annotation.RequestBody;
12
import org.springframework.web.bind.annotation.RequestMapping;
13
import org.springframework.web.bind.annotation.RestController;
14
​
15
import com.sample.postgress.entity.Employee;
16
import com.sample.postgress.service.EmployeeService;
17
​
18
@RestController
19
@RequestMapping("/postgressApp")
20
public class ApplicationController {
21
​
22
@Resource
23
EmployeeService employeeService;
24
​
25
@GetMapping(value = "/employeeList")
26
public List<Employee> getEmployees() {
27
return employeeService.findAll();
28
​
29
}
30
​
31
@PostMapping(value = "/createEmp")
32
public void createEmployee(@RequestBody Employee emp) {
33
employeeService.insertEmployee(emp);
34
​
35
}
36
@PutMapping(value = "/updateEmp")
37
public void updateEmployee(@RequestBody Employee emp) {
38
employeeService.updateEmployee(emp);
39
​
40
}
41
@PutMapping(value = "/executeUpdateEmp")
42
public void executeUpdateEmployee(@RequestBody Employee emp) {
43
employeeService.executeUpdateEmployee(emp);
44
​
45
}
46
​
47
@DeleteMapping(value = "/deleteEmpById")
48
public void deleteEmployee(@RequestBody Employee emp) {
49
employeeService.deleteEmployee(emp);
50
​
51
}
52
​
53
​
54
}
Now, let's use POSTMAN to validate the changes:

Test 1: Get the list of employees

http://localhost:8080/postgressApp/employeeList

Image title

Test 2: Create An employee

http://localhost:8080/postgressApp/createEmp

Image title

We see an entry got inserted with JONES.

Image title

Test 3: Update an Employee

http://localhost:8080/postgressApp/executeUpdateEmp

Image title

Image title

Test 4: Delete Employee

http://localhost:8080/postgressApp/deleteEmpById

Image title

Image title

Conclusion
We have learned how to set up a Spring Boot application with Postgres and how to do a CRUD operation. You will find the complete code here.

Happy coding!