# Employee Detail System
---

This is a RESTFUL Spring boot backend application for creating an employee, get an employee, get all employees, delete an employe permanently or making the employee inactive, updating an employee fully or partially. It can also import employee details from an excel file, download employee details in the database into excel and pdf files.

## TECH STACK
* Maven
* Java verion 21
* Spring boot 3.5
* Spring Validaition IO
* Spring Web
* Spring Data JPA
* Spring H2 in-memory Database
* Apache POI
* Openpdf

## RESTFUL ENDPONT DETAILS
| METHOD | URL | PURPOSE |
| ------ | --- | ------- |
| GET    | localhost:8080/api/v1/employees/all | Get all Employees in the database |
| GET    | localhost:8080/api/v1/employees | Get all Employees in with pagination support |
| GET    | localhost:8080/api/v1/employees/{id} | Get an Employee using id|
| GET    | localhost:8080/api/v1/employees/salary-range | Get all Employees based minimum and maximum salary specified |
| GET    | localhost:8080/api/v1/employees/export/excel | Get all Employees and populate them in a downloable excel file |
| GET    | localhost:8080/api/v1/employees/export/pdf | Get all Employees and populate them in a downloable pdf file |
| POST    | localhost:8080/api/v1/employees | Create an employee |
| POST    | localhost:8080/api/v1/employees/import | Import an excel file containing employee details and persisting them into the database |
| PUT    | localhost:8080/api/v1/employees/{id} | Fully update an employee entity |
| PATCH    | localhost:8080/api/v1/employees/{id} | Partially update an employee entity |
| DELETE    | localhost:8080/api/v1/employees/{id}/hard | Delete an employee entity permantly|
| DELETE    | localhost:8080/api/v1/employees/{id} | Mark an employee entity inactive |


## HOW TO RUN THE PROJECT
1. Make sure at least Java 21 is installed on your system
2. Clone the repository into your local system
3. Open the cloned project in your IDE
4. Build the program
5. Run the program
