Payments Management System

A Java-based console application designed to simulate a real-world payment management workflow with role-based access control, audit tracking, and file-based persistence.

This system follows a clean Layered Architecture (View → Service → Repository → Persistence) and demonstrates strong software engineering principles including separation of concerns, logging, synchronization, and maintainable design.

Features
Role-Based Access Control

Admin

View all payments

Update payment status (PENDING → PROCESSING → COMPLETED/REJECTED)

Create Finance Managers

View complete audit logs

Finance Manager

Add payments (Credit/Debit)

View their own payments

Secure login/logout

Payment Management

Credit & Debit transactions

Categories:

Salary

Vendor Payment

Client Payment

Auto-generated unique payment IDs

Timestamp tracking

Status lifecycle management

Audit Logging

Every critical action is logged:

User login/logout

Default admin creation

Finance manager creation

Payment addition

Payment status updates

Payment viewing

Each audit record contains:

Timestamp

Username

Action

Detailed description

💾 Persistence

File-based storage using JSON

users.json

payments.json

audit_logs.json

Automatic file creation on first run

Data consistency maintained across sessions

Project Architecture
<img width="331" height="367" alt="image" src="https://github.com/user-attachments/assets/22f5ca16-76ba-458d-b1f7-8da8012906b0" />



Layer Responsibilities
Layer	Responsibility
View	User interaction & console menus
Service	Business logic & validations
Repository	Data access & file operations
Persistence	JSON file storage
Config	Dependency wiring
Design Principles Used

Layered Architecture

Interface-based design

Dependency Injection (via ApplicationConfig)

Single Responsibility Principle

Open/Closed Principle

Audit-driven traceability

Synchronized operations for safe updates

Clean logging using java.util.logging

Concurrency Handling

The system ensures safe access to shared resources using:

Synchronized methods

Controlled update operations

Safe repository modifications

Proper audit logging for concurrent-sensitive actions

This prevents:

Race conditions

Lost updates

Inconsistent data writes

How to Run
1. Compile
mvn clean install

2. Run
java -jar target/your-jar-name.jar


Or run directly from IDE via Main.java.

Default Credentials
Username: admin
Password: admin123


The default admin is automatically created during first startup.

Sample Workflow

Admin logs in

Creates Finance Managers

Finance Manager logs in

Adds payments

Admin updates payment status

Admin reviews audit logs

All actions are logged and persisted.

Technologies Used

Java

Maven

Jackson (JSON handling)

java.util.logging

JUnit (for testing)
