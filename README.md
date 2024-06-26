# Accommodation Booking Service

---
![My Image](assets/images/accommodation.webp)
## Project Description
In this project, I aim to revolutionize the housing rental experience. My mission was to develop an advanced online 
management system for housing rentals. This system will not only simplify the tasks of service administrators but also 
provide renters with a seamless and efficient platform for securing accommodations, transforming the way people 
experience housing rentals.
---
## Navigation
1. [Technologies](#-technologies)
2. [Domain Models (Entities)](#domain-models-entities)
3. [Database structure](#database-structure)
4. [Application API](#application-api)
5. [Notification Service](#notification-service)
6. [How You Can Set Up And Use This Project](#-how-you-can-set-up-and-use-this-project)
---
## ⚙️ Technologies
- JDK 17;
- Git;
- Apache Maven;
- Spring Boot;
- Spring Security;
- Spring Data JPA;
- Swagger;
- My SQL;
- Junit 5;
- Liquibase;
- Docker;
- JWT tokens;
- Lombok;
- Mockito, JUnit tests
- Stripe API
-  Telegram API
---
## 📂 Domain Models (Entities):
- **User:** Contains information about the registered user including their authentication details and personal information.
- **Role:** Represents the role of a user in the system, for example, admin or user.
- **Accommodation:** Represents an accommodation available for booking.
- **Address:** Represents an address of accommodation.
- **Booking:** Represents a user's booking.
- **Payment:** Represents a payment process.
---
## 📈 Database Structure
![Diagram](assets/images/diagram.png)
---
## Application API
[Authentication Controller](#authentication-controller) | [User Controller](#user-controller) 
 
[Accommodation Controller](#accommodation-controller) | [Booking Controller](#booking-controller) |
[Payment Controller](#payment-controller)
### Authentication Controller
**Authentication Endpoints:**
- POST: /register - Allows users to register a new account.
- POST: /login - Grants JWT tokens to authenticated users.
### User Controller 
#### Managing authentication and user registration
**User Endpoints:**
- PUT: /users/{id}/role - Enables users to update their roles, providing role-based access. (Role - ADMIN)
-  GET: /users/me - Retrieves the profile information for the currently logged-in user. (Role - USER)
- PUT/PATCH: /users/me - Allows users to update their profile information. (Role - USER)
### Accommodation Controller
#### Managing accommodation inventory (CRUD for Accommodations)
**Accommodation Endpoints:**
- POST: /accommodations - Permits the addition of new accommodations. (Role - ADMIN)
- GET: /accommodations - Provides a list of available accommodations. (Role - USER)
- GET: /accommodations/{id} - Retrieves detailed information about a specific accommodation. (Role - USER)
- PUT: /accommodations/{id} - Allows updates to accommodation details, including inventory management. (Role - ADMIN)
- DELETE: /accommodations/{id} - Enables the removal of accommodations. (Role - ADMIN)
### Booking Controller
#### Managing users' bookings
**Booking Endpoints:**
- POST: /bookings - Permits the creation of new accommodation bookings. (Role - USER)
- GET: /bookings/?user_id=...&status=... - Retrieves bookings based on user ID and their status. (Role - ADMIN)
- GET: /bookings/my - Retrieves user bookings (Role - USER)
- GET: /bookings/{id} - Provides information about a specific booking. (Role - ADMIN)
- PUT/PATCH: /bookings/{id} - Allows users to update their booking details. (Role - USER)
- DELETE: /bookings/{id} - Enables the cancellation of bookings. (Role - ADMIN)
### Payment Controller
#### Facilitates payments for bookings through the platform. Interacts with Stripe API
**Payment Endpoints:**
- GET: /payments/?user_id=... - Retrieves payment information for users. (Role - ADMIN)
- POST: /payments/ - Initiates payment sessions for booking transactions. (Role - USER)
- GET: /payments/success/ - Handles successful payment processing through Stripe redirection.
- GET: /payments/cancel/ - Manages payment cancellation and returns payment paused messages during Stripe redirection.
### Payment page:
![Payment Page](assets/images/payment_page.png)
---
## Notification Service

Notifications about 
- new bookings created/canceled 

  ![Booking Telegram](assets/images/booking_telegram.png)

- new created/released accommodations

  ![Accommodation Telegram](assets/images/accommodation_telegram.png)

- successful payments

  ![Payment Telegram](assets/images/payment_telegram.png)

## 🦶 How You Can Set Up And Use This Project
1. [x] Fork this repository;
2. [x] Clone forked repository;
3. [x] [Install](https://www.mysql.com/downloads/) MySQL;
4. [x] Create a database for your application and configure it in the application.properties file;
5. [x] [Install](https://www.docker.com/products/docker-desktop/) Docker;
6. [x] Create .env file. Example this file you can find in file .env.sample;
7. [x] Run the following command to build and start the Docker containers:

```text
docker-compose up --build
```
8. [x] Enjoy using this application. Good luck! 😊
