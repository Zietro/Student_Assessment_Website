-- Enforce "one assessment per student per course/instructor"
-- Schema: studentassessment
--
-- IMPORTANT:
-- If you already have duplicates, the UNIQUE constraint will fail.
-- Run the "find duplicates" queries first and clean them up.

-- 1) Find duplicates (course assessments)
SELECT studentID, CCRN, COUNT(*) AS cnt
FROM COURSE_ASSESSMENT
GROUP BY studentID, CCRN
HAVING COUNT(*) > 1;

-- 2) Find duplicates (instructor assessments)
SELECT studentID, instructorID, COUNT(*) AS cnt
FROM INSTRUCTOR_ASSESSMENT
GROUP BY studentID, instructorID
HAVING COUNT(*) > 1;

-- 3) Add UNIQUE constraints (this is the real guarantee)
ALTER TABLE COURSE_ASSESSMENT
  ADD UNIQUE KEY uq_course_assessment_student_course (studentID, CCRN);

ALTER TABLE INSTRUCTOR_ASSESSMENT
  ADD UNIQUE KEY uq_instructor_assessment_student_instructor (studentID, instructorID);



