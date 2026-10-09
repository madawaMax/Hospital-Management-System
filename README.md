# Hospital Management System (HMS)

A comprehensive Desktop-based **Hospital Management System** built using **Java Swing** and **MySQL**. This application provides an intuitive graphical user interface (GUI) to manage core hospital operations, catering to three primary roles: **Admin**, **Doctor**, and **Staff / Receptionist**.

---

## 🚀 Features

### 🔑 Authentication & Role-Based Access
- Secure **Login System** for Admin, Doctors, and Receptionists/Staff.
- Role-specific dashboards and feature navigation.

### 👨‍⚕️ Doctor Portal
- **My Appointments:** View patient appointments scheduled for the doctor.
- **Patient History:** Access and manage past medical histories, diagnoses, and treatments.
- **Issue Prescriptions:** Create and issue digital prescriptions with integrated **Print/Receipt Generation**.
- **My Schedule:** Manage working days, consultation time slots, and maximum patient capacity.

### 👨‍💼 Staff / Receptionist Portal
- **Register Patient:** Full CRUD operations (Add, Update, Delete, Search) for patient records.
- **Book Appointment:** Schedule, update, or cancel patient appointments with specific doctors.
- **Patient Billing:** Calculate medical bills (Doctor Fee, Hospital Fee, Medicine Fee) and generate print-ready payment receipts.

---

## 🛠️ Tech Stack & Tools

- **Programming Language:** Java (JDK 8 / 17+)
- **UI Framework:** Java Swing & AWT
- **Database:** MySQL
- **Database Connectivity:** JDBC (Java Database Connectivity)
- **Document Printing:** Java Printing API (`java.awt.print`)
- **IDE:** IntelliJ IDEA / Eclipse / NetBeans
- **Version Control:** Git & GitHub

---

## 🗄️ Database Architecture

The system uses a relational MySQL database (`hospital_db`). The primary entities and relationships include:

1. `users` - Login credentials and role assignments (Admin, Doctor, Staff).
2. `patients` - Patient registration details (Name, Age, Gender, Contact, Address).
3. `doctors` - Doctor details and specializations.
4. `appointments` - Booked appointments connecting patients and doctors with dates/times.
5. `patient_history` - Historical medical records, diagnosis notes, and past prescriptions.
6. `prescriptions` - Issued prescriptions for patients.
7. `doctor_schedule` - Working availability and time slots for doctors.
8. `billing` - Payment records, broken-down fees, and payment balances.

---

## 🔧 Prerequisites & Setup Instructions

### 1. Prerequisites
- [Java Development Kit (JDK 8 or higher)](https://www.oracle.com/java/technologies/downloads/)
- [MySQL Server](https://dev.mysql.com/downloads/mysql/) & [MySQL Workbench](https://dev.mysql.com/downloads/workbench/)
- [MySQL Connector/J (JDBC Driver)](https://dev.mysql.com/downloads/connector/j/)

### 2. Database Configuration
1. Open MySQL Workbench and create the database:
   ```sql
   CREATE DATABASE hospital_db;
   USE hospital_db;


📌 Usage Flow
Launch App: Run LoginForm.java.

Login: Enter valid credentials based on your assigned role (Admin, Doctor, or Staff).

Staff Workflow:

Register a new patient.

Schedule an appointment for an available doctor.

Generate and print the invoice at the billing window.

Doctor Workflow:

View scheduled appointments.

Check patient medical history.

Write and print prescriptions.

Update working availability under "My Schedule".

🤝 Contributing
Contributions, issues, and feature requests are welcome! Feel free to check the issues page.