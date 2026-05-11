DROP DATABASE IF EXISTS StudentAssessment;
CREATE DATABASE IF NOT EXISTS StudentAssessment;
USE StudentAssessment;


---------------------------------------------------
-- STUDENT
---------------------------------------------------
CREATE TABLE IF NOT EXISTS STUDENT (
    studentID INT,
    Fname VARCHAR(50) NOT NULL,
    Lname VARCHAR(50) NOT NULL,
    email VARCHAR(50) NOT NULL,
    phoneNB VARCHAR(15),
    address VARCHAR(50),
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    PRIMARY KEY (studentID)
);

---------------------------------------------------
-- PROGRAM
---------------------------------------------------
CREATE TABLE IF NOT EXISTS PROGRAM (
    programID INT,
    programName VARCHAR(50) NOT NULL,
    PRIMARY KEY (programID)
);
---------------------------------------------------
-- SCHOOL
---------------------------------------------------
CREATE TABLE IF NOT EXISTS SCHOOL (
    schoolID INT,
    schoolName VARCHAR(50) NOT NULL,
    PRIMARY KEY (schoolID)
);

---------------------------------------------------
-- DEPARTMENT
---------------------------------------------------
CREATE TABLE IF NOT EXISTS DEPARTMENT (
    departmentID INT,
    schoolID INT NOT NULL,
    departmentName VARCHAR(50) NOT NULL,
    PRIMARY KEY (departmentID),
    FOREIGN KEY (schoolID) REFERENCES SCHOOL(schoolID) ON DELETE RESTRICT
);

---------------------------------------------------
-- INSTRUCTOR
---------------------------------------------------
CREATE TABLE IF NOT EXISTS INSTRUCTOR (
    instructorID INT,
    Fname VARCHAR(50) NOT NULL,
    Lname VARCHAR(50) NOT NULL,
    email VARCHAR(50) NOT NULL,
    phoneNB VARCHAR(15),
    address VARCHAR(50),
    departmentID INT NOT NULL,
    PRIMARY KEY (instructorID),
    FOREIGN KEY (departmentID) REFERENCES DEPARTMENT(departmentID) ON DELETE RESTRICT
);


---------------------------------------------------
-- COORDINATOR/APPROVER
---------------------------------------------------
CREATE TABLE IF NOT EXISTS COORDINATOR (
    coordinatorID INT,
    PRIMARY KEY (coordinatorID),
    FOREIGN KEY (coordinatorID) REFERENCES INSTRUCTOR(instructorID) ON DELETE RESTRICT
);


---------------------------------------------------
-- COURSE
---------------------------------------------------
CREATE TABLE IF NOT EXISTS COURSE (
    CCRN DEC(5),
    CHECK (CCRN > 0),
    programID INT NOT NULL,
    coordinatorID INT NOT NULL,
    courseName VARCHAR(50) NOT NULL,
    credits INT NOT NULL,
    CHECK (credits BETWEEN 0 AND 6),
    courseDesc VARCHAR(500) NOT NULL,
    PRIMARY KEY (CCRN),
    FOREIGN KEY (programID) REFERENCES PROGRAM(programID) ON DELETE RESTRICT,
    FOREIGN KEY (coordinatorID) REFERENCES COORDINATOR(coordinatorID) ON DELETE RESTRICT
);

---------------------------------------------------
-- PROGRAM_REGISTRATION
---------------------------------------------------
CREATE TABLE IF NOT EXISTS PROGRAM_REGISTRATION (
    programID INT,
    studentID INT,
    startDate DATE NOT NULL,
    endDate DATE NOT NULL,
    CHECK (startDate < endDate),
    PRIMARY KEY (programID, studentID, startDate),
    FOREIGN KEY (programID) REFERENCES PROGRAM(programID) ON DELETE RESTRICT,
    FOREIGN KEY (studentID) REFERENCES STUDENT(studentID) ON DELETE CASCADE
);


---------------------------------------------------
-- MEMBER_OF_DEPARTMENT
---------------------------------------------------
CREATE TABLE IF NOT EXISTS MEMBER_OF (
    departmentID INT,
    instructorID INT,
    startDate DATE NOT NULL,
	endDate DATE NOT NULL,
	CHECK (startDate < endDate),
	PRIMARY KEY (departmentID, instructorID, startDate, endDate),
    FOREIGN KEY (departmentID) REFERENCES DEPARTMENT(departmentID) ON DELETE RESTRICT,
    FOREIGN KEY (instructorID) REFERENCES INSTRUCTOR(instructorID) ON DELETE CASCADE
);

---------------------------------------------------
-- CHAIR_OF
---------------------------------------------------
CREATE TABLE IF NOT EXISTS CHAIR_OF (
    coordinatorID INT,
    departmentID INT,
    startDate DATE,
    endDate DATE,
    CHECK (startDate < endDate),
    PRIMARY KEY (coordinatorID, departmentID, startDate, endDate),
    FOREIGN KEY (coordinatorID) REFERENCES COORDINATOR(coordinatorID) ON DELETE CASCADE,
    FOREIGN KEY (departmentID) REFERENCES DEPARTMENT(departmentID) ON DELETE RESTRICT
);

---------------------------------------------------
-- INSTRUCTOR_ASSESSMENT
---------------------------------------------------
CREATE TABLE IF NOT EXISTS INSTRUCTOR_ASSESSMENT (
    studentID INT,
    instructorID INT,
    instructorAssessmentDate DATE,
    rating1 INT,
    CHECK (rating1 BETWEEN 1 AND 5),
    rating2 INT,
    CHECK (rating2 BETWEEN 1 AND 5),
    rating3 INT,
    CHECK (rating3 BETWEEN 1 AND 5),
    rating4 INT,
    CHECK (rating4 BETWEEN 1 AND 5),
    recommendations VARCHAR(500),
    PRIMARY KEY (studentID, instructorID, instructorAssessmentDate),
    FOREIGN KEY (studentID) REFERENCES STUDENT(studentID) ON DELETE CASCADE,
    FOREIGN KEY (instructorID) REFERENCES INSTRUCTOR(instructorID) ON DELETE CASCADE
);

---------------------------------------------------
-- COURSE_ASSESSMENT
---------------------------------------------------
CREATE TABLE IF NOT EXISTS COURSE_ASSESSMENT (
    studentID INT,
    CCRN DEC(5),
    courseAssessmentDate DATE,
    rating1 INT,
    CHECK (rating1 BETWEEN 1 AND 5),
    rating2 INT,
    CHECK (rating2 BETWEEN 1 AND 5),
    rating3 INT,
    CHECK (rating3 BETWEEN 1 AND 5),
    rating4 INT,
    CHECK (rating4 BETWEEN 1 AND 5),
    recommendations VARCHAR(500),
    PRIMARY KEY (studentID, CCRN, courseAssessmentDate),
    FOREIGN KEY (studentID) REFERENCES STUDENT(studentID) ON DELETE CASCADE,
    FOREIGN KEY (CCRN) REFERENCES COURSE(CCRN) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS REGISTRATION (
	registrationID INT NOT NULL AUTO_INCREMENT,
    CCRN DEC(5) NOT NULL,
    instructorID INT NOT NULL,
    PRIMARY KEY(registrationID),
    FOREIGN KEY (CCRN) REFERENCES COURSE(CCRN) ON DELETE CASCADE,
    FOREIGN KEY (instructorID) REFERENCES INSTRUCTOR(instructorID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS STUDENT_REGISTRATION(
	studentID INT NOT NULL,
    registrationID INT NOT NULL,
    PRIMARY KEY(studentID, registrationID),
    FOREIGN KEY (studentID) REFERENCES STUDENT(studentID) ON DELETE CASCADE,
	FOREIGN KEY (registrationID) REFERENCES REGISTRATION(registrationID) ON DELETE CASCADE
);

---------------------------------------------------
-- OFFER
---------------------------------------------------
CREATE TABLE IF NOT EXISTS OFFER (
    CCRN DEC(5),
    programID INT,
    PRIMARY KEY (CCRN, programID),
    FOREIGN KEY (CCRN) REFERENCES COURSE(CCRN) ON DELETE CASCADE,
    FOREIGN KEY (programID) REFERENCES PROGRAM(programID) ON DELETE CASCADE
);

USE StudentAssessment;

---------------------------------------------------
-- SCHOOL
---------------------------------------------------
INSERT INTO SCHOOL (schoolID, schoolName) VALUES
(1, 'School of Engineering'),
(2, 'School of Business');

---------------------------------------------------
-- DEPARTMENT
---------------------------------------------------
INSERT INTO DEPARTMENT (departmentID, schoolID, departmentName) VALUES
(10, 1, 'Computer Science'),
(11, 1, 'Electrical Engineering'),
(20, 2, 'Management');

---------------------------------------------------
-- PROGRAM
---------------------------------------------------
INSERT INTO PROGRAM (programID, programName) VALUES
(100, 'BS Computer Science'),
(101, 'BS Electrical Engineering'),
(200, 'BBA Management');

---------------------------------------------------
-- STUDENT
---------------------------------------------------
INSERT INTO STUDENT
(studentID, Fname, Lname, email, phoneNB, address, username, password_hash)
VALUES
(1, 'Maya', 'Haddad', 'maya.haddad@example.com', '+96170111222', 'Beirut', 'maya.h', 'maya123'),
(2, 'Karim', 'Nasser', 'karim.nasser@example.com', '+96176123456', 'Tripoli', 'karim.n', 'uni123pass'),
(3, 'Lina', 'Salameh', 'lina.salameh@example.com', '+96103123456', 'Sidon',  'lina.s', 'ls@passuni'),
(4, 'Omar', 'Khalil', 'omar.khalil@example.com', '+96178199887', 'Byblos', 'omar.k', 'gingerbread');

---------------------------------------------------
-- INSTRUCTOR
---------------------------------------------------
INSERT INTO INSTRUCTOR
(instructorID, Fname, Lname, email, phoneNB, address, departmentID)
VALUES
(1000, 'Nadine', 'Farah', 'nadine.farah@uni.edu', '+96170101010', 'Beirut', 10),
(1001, 'Jad',    'Saab',  'jad.saab@uni.edu',    '+96170101011', 'Beirut', 10),
(1002, 'Rami',   'Zein',  'rami.zein@uni.edu',   '+96170101012', 'Jounieh', 11),
(1003, 'Hala',   'Mansour','hala.mansour@uni.edu','+96170101013', 'Beirut', 20);

---------------------------------------------------
-- COORDINATOR (must exist in INSTRUCTOR)
---------------------------------------------------
INSERT INTO COORDINATOR (coordinatorID) VALUES
(1000),
(1003);

---------------------------------------------------
-- COURSE (coordinatorID references COORDINATOR)
---------------------------------------------------
INSERT INTO COURSE
(CCRN, programID, coordinatorID, courseName, credits, courseDesc)
VALUES
(12345, 100, 1000, 'Database Systems', 3, 'Relational design, SQL, constraints, and transactions.'),
(23456, 100, 1000, 'Data Structures', 3, 'Lists, trees, graphs, hashing, and complexity.'),
(34567, 101, 1000, 'Digital Logic',   3, 'Boolean algebra, combinational and sequential circuits.'),
(45678, 200, 1003, 'Principles of Management', 3, 'Organizations, leadership, and planning.');

---------------------------------------------------
-- PROGRAM_REGISTRATION
---------------------------------------------------
INSERT INTO PROGRAM_REGISTRATION
(programID, studentID, startDate, endDate)
VALUES
(100, 1, '2024-09-01', '2026-06-30'),
(100, 2, '2024-09-01', '2026-06-30'),
(101, 3, '2024-09-01', '2026-06-30'),
(200, 4, '2024-09-01', '2026-06-30');

---------------------------------------------------
-- MEMBER_OF (instructor belongs to department across dates)
---------------------------------------------------
INSERT INTO MEMBER_OF
(departmentID, instructorID, startDate, endDate)
VALUES
(10, 1000, '2022-09-01', '2026-06-30'),
(10, 1001, '2023-09-01', '2026-06-30'),
(11, 1002, '2021-09-01', '2026-06-30'),
(20, 1003, '2020-09-01', '2026-06-30');

---------------------------------------------------
-- CHAIR_OF (coordinator is chair of a department)
---------------------------------------------------
INSERT INTO CHAIR_OF
(coordinatorID, departmentID, startDate, endDate)
VALUES
(1000, 10, '2024-01-01', '2025-12-31'),
(1003, 20, '2023-01-01', '2025-12-31');

---------------------------------------------------
--  (student enrollments with instructor)
---------------------------------------------------
INSERT INTO REGISTRATION (CCRN, instructorID) VALUES
(12345, 1000),
(23456, 1001),
(34567, 1002),
(45678, 1003);

---------------------------------------------------
-- STUDENT_REGISTRATION (student enrollments with instructor)
---------------------------------------------------
INSERT INTO STUDENT_REGISTRATION (studentID, registrationID) VALUES
(1, 1),
(1, 2),
(2, 1),
(2, 2),
(3, 3),
(4, 4);

---------------------------------------------------
-- COURSE_ASSESSMENT
---------------------------------------------------
INSERT INTO COURSE_ASSESSMENT
(studentID, CCRN, courseAssessmentDate, rating1, rating2, rating3, rating4, recommendations)
VALUES
(1, 12345, '2025-01-10', 5, 4, 5, 4, 'Great course, add more practice queries.'),
(2, 12345, '2025-01-10', 4, 4, 4, 3, 'More examples in lectures would help.'),
(1, 23456, '2025-01-12', 5, 5, 4, 5, 'Excellent content and pace.'),
(3, 34567, '2025-01-15', 4, 3, 4, 4, 'Labs are helpful; add more quizzes.'),
(4, 45678, '2025-01-20', 5, 4, 4, 5, 'Nice discussions; include case studies.');

---------------------------------------------------
-- INSTRUCTOR_ASSESSMENT
---------------------------------------------------
INSERT INTO INSTRUCTOR_ASSESSMENT
(studentID, instructorID, instructorAssessmentDate, rating1, rating2, rating3, rating4, recommendations)
VALUES
(1, 1000, '2025-01-10', 5, 4, 5, 5, 'Very clear explanations.'),
(2, 1000, '2025-01-10', 4, 4, 4, 4, 'Good overall, a bit fast sometimes.'),
(1, 1001, '2025-01-12', 5, 5, 4, 5, 'Great engagement with students.'),
(3, 1002, '2025-01-15', 4, 3, 4, 4, 'More office hours would help.'),
(4, 1003, '2025-01-20', 5, 4, 4, 5, 'Supportive and practical examples.');

---------------------------------------------------
-- OFFER (many-to-many course/program mapping)
---------------------------------------------------
INSERT INTO OFFER (CCRN, programID) VALUES
(12345, 100),
(23456, 100),
(34567, 101),
(45678, 200);

USE StudentAssessment;
SELECT * FROM STUDENT;

