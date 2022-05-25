# Account Service
A REST API simulating sending payrolls to the employee's account on the corporate website. Service has a defined role model, business logic is implemented, and security is ensured.

## Table of contents
* [Overview](#overview)
* [Screenshots](#screenshots)
* [Technologies](#technologies)
* [How to start](#how-to-start)

## Overview
A project aimed at familiarizing oneself with the Spring Security module, the basics of user authentication and authorization, logging security-related events and learning about various requirements of modern information security standards for web applications.

## Screenshots
- Some screenshots of various test cases:

  ``loading...``

###### user registration
![user registration - negative](src/main/resources/screenshots/user_registration-negative.png) 
![user registration - positive](src/main/resources/screenshots/user_registration-positive.png)

###### user authentication
![authentication - negative](src/main/resources/screenshots/authentication-negative.png)
![authentication - negative 2](src/main/resources/screenshots/authentication-negative_2.png)

###### authorization
![authorization - negative](src/main/resources/screenshots/role_model_authorization-negative.png)

###### changing password
![changing password](src/main/resources/screenshots/changing_password.png)
![changing password 2](src/main/resources/screenshots/changing_password_2.png)

###### business logic - adding payrolls
![adding payrolls](src/main/resources/screenshots/business_logic-adding_payrolls.png)
![adding payrolls 2](src/main/resources/screenshots/business_logic-adding_payrolls_2.png)
![adding payrolls 3](src/main/resources/screenshots/business_logic-adding_payrolls_3.png)
![adding payrolls 4](src/main/resources/screenshots/business_logic-adding_payrolls_3.png)

###### changing roles
![changing roles - negative](src/main/resources/screenshots/changing_roles-negative.png)
![changing roles - negative 2](src/main/resources/screenshots/changing_roles-negative_2.png)
![changing roles - negative 3](src/main/resources/screenshots/changing_roles-negative_3.png)
![changing roles - negative 4](src/main/resources/screenshots/changing_roles-negative_4.png)
![changing roles - positive](src/main/resources/screenshots/changing_roles-positive.png)
![changing roles - positive 2](src/main/resources/screenshots/changing_roles-positive_2.png)

###### deleting user
![deleting user](src/main/resources/screenshots/deleting_user.png)
![deleting user 2](src/main/resources/screenshots/deleting_user.png)

###### locking user
![locking user](src/main/resources/screenshots/locking_user.png)
![locking user 2](src/main/resources/screenshots/locking_user_2.png)

###### logging security events
![logging security events](src/main/resources/screenshots/logging_security_events.png)
![logging security events 2](src/main/resources/screenshots/logging_security_events_2.png)
![logging security events 3](src/main/resources/screenshots/logging_security_events_3.png)
![logging security events 4](src/main/resources/screenshots/logging_security_events_4.png)
![logging security events 5](src/main/resources/screenshots/logging_security_events_5.png)

## Technologies
- Java 17
- Spring Boot 2.5.3
- Gson 2.9.0
- Gradle 7.2
- H2 Database 1.4
- JUnit 5.7.2
- Hyperskill Testing Library 8.2
- Lombok 1.20

## How to start
You can simply download an archive, unzip it inside the directory you want to, and open it in your IDE.

If you want clone the repo:

- run command line in the directory you want to store the app and type the following command:

``git clone https://github.com/codeofcarbon/account_service``

- or start with Project from Version Control in your IDE by providing the url of this repository.
