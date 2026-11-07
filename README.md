# 📚 BookNest – Online Library Management System

BookNest is a full-stack Online Library Management System
that simplifies library operations through a web-based
application.

Users can explore books, search for books, check availability,
borrow books, and return borrowed books. Administrators can
manage the library collection.

## ✨ Features

- User registration and email OTP verification
- Secure login using JWT authentication
- Role-based authorization for Admin and Member
- Add, view, update, and delete books
- Search books by title and author
- Borrow and return books
- Book availability management
- Import book information using Open Library API
- Input validation and exception handling
- REST API documentation using Swagger

## 🛠️ Technologies Used

### Frontend
- React.js
- JavaScript
- HTML5
- CSS3
- Vite

### Backend
- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- REST APIs
- JWT Authentication
- JavaMailSender

### Database
- MySQL

### Development Tools
- Eclipse IDE
- Visual Studio Code
- Postman
- Swagger UI
- Git and GitHub

## 📁 Project Structure

BookNest-Portfolio/
│
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── ...
│
├── booknest-frontend/
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── ...
│
├── .gitignore
└── README.md

## ⚙️ Prerequisites

- Java 21
- MySQL
- Node.js compatible with Vite 8
- npm
- Git

## 🚀 Backend Setup

1. Clone the repository.
2. Open the backend folder.
3. Create a MySQL database named:

   booknest_db

4. Configure the following environment variables:

   - DB_USERNAME
   - DB_PASSWORD
   - MAIL_USERNAME
   - MAIL_PASSWORD

5. Run the Spring Boot application.

The backend runs on:

http://localhost:8080

## 💻 Frontend Setup

Open a terminal inside the booknest-frontend folder.

Install dependencies:

npm install

Start the development server:

npm run dev

The frontend runs on:

http://localhost:5173

## 🔗 API Documentation

Swagger UI is available locally at:

http://localhost:8080/swagger-ui/index.html

## 👩‍💻 Developer

Bhagya Biradar

Computer Science and Engineering Graduate – 2026

## 📌 Project Status

Developed as a full-stack learning and portfolio project.