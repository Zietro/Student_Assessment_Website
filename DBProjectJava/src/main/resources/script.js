/*
  ====== SIMPLE DATA ======
  In a real system this data would come from a server.
  Here we hard-code a few example courses.
*/

// Course evaluation questions (for all courses, just as an example)
const courseQuestions = [
    "The course materials were well-organized.",
    "The learning objectives were clear.",
    "The assessments (exams, assignments) matched the course content.",
    "The pace of the course was appropriate."
];

// Instructor evaluation questions
const instructorQuestions = [
    "The instructor explained concepts clearly.",
    "The instructor was well-prepared for class.",
    "The instructor was available for questions/consultation.",
    "The instructor treated students with respect."
];

// Simple state variables
let currentUser = null;
let currentCourseId = null;
let currentRole = null; // "student" | "admin"
let adminToken = null;

let currentCourseQuestionIndex = 0;
let currentInstrQuestionIndex = 0;

// Special index for recommendations step (after all rating questions)
const COURSE_RECOMMENDATIONS_INDEX = courseQuestions.length;
const INSTRUCTOR_RECOMMENDATIONS_INDEX = instructorQuestions.length;

// To store answers (in memory only)
let courseAnswers = {};
let instructorAnswers = {};

// Track completed evaluations
let completedCourseEvaluations = {}; // {courseId: true}
let completedInstructorEvaluations = {}; // {courseId: true}

/* ====== HELPER: SHOW ONE SCREEN AT A TIME ====== */
function showScreen(screenId) {
    const screens = document.querySelectorAll(".screen");
    screens.forEach(s => s.classList.remove("active"));
    const screen = document.getElementById(screenId);
    if (screen) screen.classList.add("active");
}

/* ====== HELPER: UPDATE PROGRESS BARS ====== */
function updateCourseProgress() {
    // Total steps = rating questions + recommendations step
    const totalSteps = courseQuestions.length + 1;
    let currentStep, progressText;

    if (currentCourseQuestionIndex === COURSE_RECOMMENDATIONS_INDEX) {
        // On recommendations step
        currentStep = totalSteps;
        progressText = "Recommendations (Final Step)";
    } else {
        // On a rating question
        currentStep = currentCourseQuestionIndex + 1;
        progressText = `Question ${currentStep} of ${courseQuestions.length}`;
    }

    const percent = (currentStep / totalSteps) * 100;

    const textEl = document.getElementById("courseProgressText");
    const barEl = document.getElementById("courseProgressBar");
    if (textEl) {
        textEl.textContent = progressText;
    }
    if (barEl) {
        barEl.style.width = `${percent}%`;
    }
}

function updateInstructorProgress() {
    // Total steps = rating questions + recommendations step
    const totalSteps = instructorQuestions.length + 1;
    let currentStep, progressText;

    if (currentInstrQuestionIndex === INSTRUCTOR_RECOMMENDATIONS_INDEX) {
        // On recommendations step
        currentStep = totalSteps;
        progressText = "Recommendations (Final Step)";
    } else {
        // On a rating question
        currentStep = currentInstrQuestionIndex + 1;
        progressText = `Question ${currentStep} of ${instructorQuestions.length}`;
    }

    const percent = (currentStep / totalSteps) * 100;

    const textEl = document.getElementById("instrProgressText");
    const barEl = document.getElementById("instrProgressBar");
    if (textEl) {
        textEl.textContent = progressText;
    }
    if (barEl) {
        barEl.style.width = `${percent}%`;
    }
}

/* ====== LOGIN LOGIC ====== */
async function handleLogin() {
    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value.trim();
    const errorDiv = document.getElementById("loginError");

    if (!username || !password) {
        errorDiv.style.display = "block";
        errorDiv.textContent = "Please enter both username and password.";
        return;
    }

    try {
        const url = `http://localhost:8000/login?username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`;

        const response = await fetch(url);

        if (!response.ok) {
            const text = await response.text();
            throw new Error(text);
        }

        const result = await response.json();

        if (result.success) {
            currentUser = username;
            currentRole = result.role || "student";
            document.getElementById("topBar").style.display = "flex";
            document.getElementById("topBarUsername").textContent = currentUser;

            if (currentRole === "admin") {
                adminToken = result.token || null;
                showScreen("adminScreen");
                await loadAdminStudents();
                await loadAdminInstructors();
                await loadAdminCourses();
                await loadAdminRegistrations();
                await loadAdminStudentRegistrations();
            } else {
                showScreen("mainMenuScreen");
            }
        } else {
            errorDiv.style.display = "block";
            errorDiv.textContent = result.error || "Invalid credentials";
        }
    } catch (err) {
        console.error(err);
        errorDiv.style.display = "block";
        errorDiv.textContent = "Cannot connect to server";
    }
}


function logout() {
    currentUser = null;
    currentCourseId = null;
    currentRole = null;
    adminToken = null;
    document.getElementById("topBar").style.display = "none";
    // Clear username and password fields
    document.getElementById("username").value = "";
    document.getElementById("password").value = "";
    // Clear login error if any
    const errorDiv = document.getElementById("loginError");
    if (errorDiv) {
        errorDiv.style.display = "none";
        errorDiv.textContent = "";
    }
    showScreen("loginScreen");
}

/* ====== ADMIN DASHBOARD ====== */
function setAdminError(msg) {
    const el = document.getElementById("adminError");
    if (!el) return;
    if (!msg) {
        el.style.display = "none";
        el.textContent = "";
    } else {
        el.style.display = "block";
        el.textContent = msg;
    }
}

function showAdminTab(tab) {
    const students = document.getElementById("adminStudentsTab");
    const instructors = document.getElementById("adminInstructorsTab");
    const courses = document.getElementById("adminCoursesTab");
    const registrations = document.getElementById("adminRegistrationsTab");
    const studentRegistrations = document.getElementById("adminStudentRegistrationsTab");
    if (!students || !instructors || !courses || !registrations || !studentRegistrations) return;

    students.style.display = tab === "students" ? "block" : "none";
    instructors.style.display = tab === "instructors" ? "block" : "none";
    courses.style.display = tab === "courses" ? "block" : "none";
    registrations.style.display = tab === "registrations" ? "block" : "none";
    studentRegistrations.style.display = tab === "student-registrations" ? "block" : "none";
    setAdminError("");
    
    // Auto-load data when tab is shown
    if (tab === "courses") {
        loadAllPrograms();
        loadAllCoordinators();
        loadAdminCourses();
    } else if (tab === "instructors") {
        loadInstructorsDepartments();
        loadAdminInstructors();
    } else if (tab === "registrations") {
        loadRegistrationsInstructors();
        loadRegistrationsCourses();
        loadAdminRegistrations();
    } else if (tab === "student-registrations") {
        loadStudentRegistrationsList();
        loadStudentRegistrationsStudents();
        loadAdminStudentRegistrations();
    }
}

async function adminFetch(url, options = {}) {
    const headers = options.headers || {};
    headers["X-Admin-Token"] = adminToken || "";
    options.headers = headers;
    const res = await fetch(url, options);
    if (res.status === 401) throw new Error("Admin unauthorized. Please log in again.");
    return res;
}

// ---- Students ----
function adminClearStudentForm() {
    document.getElementById("admStudentID").value = "";
    document.getElementById("admStudentFname").value = "";
    document.getElementById("admStudentLname").value = "";
    document.getElementById("admStudentUsername").value = "";
    document.getElementById("admStudentPassword").value = "";
    document.getElementById("admStudentEmail").value = "";
    document.getElementById("admStudentPhone").value = "";
    document.getElementById("admStudentAddress").value = "";
}

async function loadAdminStudents() {
    setAdminError("");
    const tbody = document.getElementById("adminStudentsBody");
    tbody.innerHTML = "<tr><td colspan='8'>Loading...</td></tr>";
    try {
        const res = await adminFetch("http://localhost:8000/admin/students");
        const rows = await res.json();
        tbody.innerHTML = "";
        rows.forEach(s => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${s.studentID}</td>
                <td>${s.Fname || ""}</td>
                <td>${s.Lname || ""}</td>
                <td>${s.username || ""}</td>
                <td>${s.email || ""}</td>
                <td>${s.phoneNB || ""}</td>
                <td>${s.address || ""}</td>
                <td>
                    <button class="btn-secondary">Edit</button>
                    <button class="btn-secondary">Delete</button>
                </td>`;
            const [editBtn, delBtn] = tr.querySelectorAll("button");
            editBtn.onclick = () => {
                document.getElementById("admStudentID").value = s.studentID;
                document.getElementById("admStudentFname").value = s.Fname || "";
                document.getElementById("admStudentLname").value = s.Lname || "";
                document.getElementById("admStudentUsername").value = s.username || "";
                document.getElementById("admStudentPassword").value = ""; // don’t show stored password_hash
                document.getElementById("admStudentEmail").value = s.email || "";
                document.getElementById("admStudentPhone").value = s.phoneNB || "";
                document.getElementById("admStudentAddress").value = s.address || "";
            };
            delBtn.onclick = async () => {
                if (!confirm(`Delete student ${s.studentID}?`)) return;
                await adminDeleteStudent(s.studentID);
            };
            tbody.appendChild(tr);
        });
    } catch (e) {
        console.error(e);
        setAdminError(e.message || "Failed to load students");
        tbody.innerHTML = "";
    }
}

async function adminAddStudent() {
    try {
        const payload = {
            studentID: parseInt(document.getElementById("admStudentID").value, 10),
            Fname: document.getElementById("admStudentFname").value,
            Lname: document.getElementById("admStudentLname").value,
            username: document.getElementById("admStudentUsername").value,
            password: document.getElementById("admStudentPassword").value,
            email: document.getElementById("admStudentEmail").value,
            phoneNB: document.getElementById("admStudentPhone").value,
            address: document.getElementById("admStudentAddress").value
        };
        const res = await adminFetch("http://localhost:8000/admin/students", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        if (!res.ok) throw new Error((await res.json().catch(() => ({}))).error || "Add failed");
        adminClearStudentForm();
        await loadAdminStudents();
    } catch (e) {
        setAdminError(e.message || "Add failed");
    }
}

async function adminUpdateStudent() {
    try {
        const payload = {
            studentID: parseInt(document.getElementById("admStudentID").value, 10),
            Fname: document.getElementById("admStudentFname").value,
            Lname: document.getElementById("admStudentLname").value,
            username: document.getElementById("admStudentUsername").value,
            password: document.getElementById("admStudentPassword").value,
            email: document.getElementById("admStudentEmail").value,
            phoneNB: document.getElementById("admStudentPhone").value,
            address: document.getElementById("admStudentAddress").value
        };
        const res = await adminFetch("http://localhost:8000/admin/students", {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        const body = await res.json().catch(() => ({}));
        if (!res.ok) throw new Error(body.error || "Update failed");
        await loadAdminStudents();
    } catch (e) {
        setAdminError(e.message || "Update failed");
    }
}

async function adminDeleteStudent(studentID) {
    try {
        const res = await adminFetch("http://localhost:8000/admin/students", {
            method: "DELETE",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ studentID })
        });
        const body = await res.json().catch(() => ({}));
        if (!res.ok) throw new Error(body.error || "Delete failed");
        await loadAdminStudents();
    } catch (e) {
        setAdminError(e.message || "Delete failed");
    }
}

// ---- Instructors ----
function adminClearInstructorForm() {
    document.getElementById("admInstructorID").value = "";
    document.getElementById("admInstructorFname").value = "";
    document.getElementById("admInstructorLname").value = "";
    document.getElementById("admInstructorEmail").value = "";
    document.getElementById("admInstructorPhone").value = "";
    document.getElementById("admInstructorAddress").value = "";
    document.getElementById("admInstructorDept").value = "";
}

async function loadAdminInstructors() {
    setAdminError("");
    const tbody = document.getElementById("adminInstructorsBody");
    tbody.innerHTML = "<tr><td colspan='8'>Loading...</td></tr>";
    try {
        const res = await adminFetch("http://localhost:8000/admin/instructors");
        const rows = await res.json();
        tbody.innerHTML = "";
        rows.forEach(i => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${i.instructorID}</td>
                <td>${i.Fname || ""}</td>
                <td>${i.Lname || ""}</td>
                <td>${i.email || ""}</td>
                <td>${i.phoneNB || ""}</td>
                <td>${i.address || ""}</td>
                <td>${i.departmentID ?? ""}</td>
                <td>
                    <button class="btn-secondary">Edit</button>
                    <button class="btn-secondary">Delete</button>
                </td>`;
            const [editBtn, delBtn] = tr.querySelectorAll("button");
            editBtn.onclick = () => {
                document.getElementById("admInstructorID").value = i.instructorID;
                document.getElementById("admInstructorFname").value = i.Fname || "";
                document.getElementById("admInstructorLname").value = i.Lname || "";
                document.getElementById("admInstructorEmail").value = i.email || "";
                document.getElementById("admInstructorPhone").value = i.phoneNB || "";
                document.getElementById("admInstructorAddress").value = i.address || "";
                document.getElementById("admInstructorDept").value = i.departmentID ?? "";
            };
            delBtn.onclick = async () => {
                if (!confirm(`Delete instructor ${i.instructorID}?`)) return;
                await adminDeleteInstructor(i.instructorID);
            };
            tbody.appendChild(tr);
        });
    } catch (e) {
        console.error(e);
        setAdminError(e.message || "Failed to load instructors");
        tbody.innerHTML = "";
    }
}

async function adminAddInstructor() {
    try {
        const payload = {
            instructorID: parseInt(document.getElementById("admInstructorID").value, 10),
            Fname: document.getElementById("admInstructorFname").value,
            Lname: document.getElementById("admInstructorLname").value,
            email: document.getElementById("admInstructorEmail").value,
            phoneNB: document.getElementById("admInstructorPhone").value,
            address: document.getElementById("admInstructorAddress").value,
            departmentID: parseInt(document.getElementById("admInstructorDept").value || "0", 10)
        };
        const res = await adminFetch("http://localhost:8000/admin/instructors", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        if (!res.ok) throw new Error((await res.json().catch(() => ({}))).error || "Add failed");
        adminClearInstructorForm();
        await loadAdminInstructors();
    } catch (e) {
        setAdminError(e.message || "Add failed");
    }
}

async function adminUpdateInstructor() {
    try {
        const payload = {
            instructorID: parseInt(document.getElementById("admInstructorID").value, 10),
            Fname: document.getElementById("admInstructorFname").value,
            Lname: document.getElementById("admInstructorLname").value,
            email: document.getElementById("admInstructorEmail").value,
            phoneNB: document.getElementById("admInstructorPhone").value,
            address: document.getElementById("admInstructorAddress").value,
            departmentID: parseInt(document.getElementById("admInstructorDept").value || "0", 10)
        };
        const res = await adminFetch("http://localhost:8000/admin/instructors", {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        const body = await res.json().catch(() => ({}));
        if (!res.ok) throw new Error(body.error || "Update failed");
        await loadAdminInstructors();
    } catch (e) {
        setAdminError(e.message || "Update failed");
    }
}

async function adminDeleteInstructor(instructorID) {
    try {
        const res = await adminFetch("http://localhost:8000/admin/instructors", {
            method: "DELETE",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ instructorID })
        });
        const body = await res.json().catch(() => ({}));
        if (!res.ok) throw new Error(body.error || "Delete failed");
        await loadAdminInstructors();
    } catch (e) {
        setAdminError(e.message || "Delete failed");
    }
}

// ---- Courses ----
function adminClearCourseForm() {
    document.getElementById("admCourseCCRN").value = "";
    document.getElementById("admCourseProgram").value = "";
    document.getElementById("admCourseCoordinatorID").value = "";
    document.getElementById("admCourseName").value = "";
    document.getElementById("admCourseCredits").value = "";
    document.getElementById("admCourseDesc").value = "";
}

async function loadAllPrograms() {
    const tbody = document.getElementById("programsListBody");
    tbody.innerHTML = "<tr><td colspan='2'>Loading programs...</td></tr>";
    try {
        const res = await adminFetch("http://localhost:8000/admin/programs");
        const programs = await res.json();
        tbody.innerHTML = "";
        
        if (programs.length === 0) {
            tbody.innerHTML = "<tr><td colspan='2'>No programs found in the system.</td></tr>";
            return;
        }
        
        programs.forEach(program => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${program.programID}</td>
                <td>${program.programName || "N/A"}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch (e) {
        console.error("Error loading programs:", e);
        tbody.innerHTML = "<tr><td colspan='2'>Error loading programs. Please try again.</td></tr>";
    }
}

async function loadInstructorsDepartments() {
    const tbody = document.getElementById("instructorsDepartmentsListBody");
    tbody.innerHTML = "<tr><td colspan='2'>Loading departments...</td></tr>";
    try {
        const res = await adminFetch("http://localhost:8000/admin/departments");
        const departments = await res.json();
        tbody.innerHTML = "";
        
        if (departments.length === 0) {
            tbody.innerHTML = "<tr><td colspan='2'>No departments found in the system.</td></tr>";
            return;
        }
        
        departments.forEach(dept => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${dept.departmentID}</td>
                <td>${dept.departmentName || "N/A"}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch (e) {
        console.error("Error loading departments:", e);
        tbody.innerHTML = "<tr><td colspan='2'>Error loading departments. Please try again.</td></tr>";
    }
}

async function loadRegistrationsInstructors() {
    const tbody = document.getElementById("registrationsInstructorsListBody");
    tbody.innerHTML = "<tr><td colspan='6'>Loading instructors...</td></tr>";
    try {
        const res = await adminFetch("http://localhost:8000/admin/instructors");
        const instructors = await res.json();
        tbody.innerHTML = "";
        
        if (instructors.length === 0) {
            tbody.innerHTML = "<tr><td colspan='6'>No instructors found in the system.</td></tr>";
            return;
        }
        
        instructors.forEach(inst => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${inst.instructorID}</td>
                <td>${inst.Fname || ""} ${inst.Lname || ""}</td>
                <td>${inst.email || "N/A"}</td>
                <td>${inst.phoneNB || "N/A"}</td>
                <td>${inst.address || "N/A"}</td>
                <td>${inst.departmentID ?? "N/A"}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch (e) {
        console.error("Error loading instructors:", e);
        tbody.innerHTML = "<tr><td colspan='6'>Error loading instructors. Please try again.</td></tr>";
    }
}

async function loadRegistrationsCourses() {
    const tbody = document.getElementById("registrationsCoursesListBody");
    tbody.innerHTML = "<tr><td colspan='6'>Loading courses...</td></tr>";
    try {
        const res = await adminFetch("http://localhost:8000/admin/courses");
        const courses = await res.json();
        tbody.innerHTML = "";
        
        if (courses.length === 0) {
            tbody.innerHTML = "<tr><td colspan='6'>No courses found in the system.</td></tr>";
            return;
        }
        
        courses.forEach(course => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${course.CCRN}</td>
                <td>${course.courseName || "N/A"}</td>
                <td>${course.programID ?? "N/A"}</td>
                <td>${course.coordinatorID ?? "N/A"}</td>
                <td>${course.credits ?? "N/A"}</td>
                <td>${course.courseDesc || "N/A"}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch (e) {
        console.error("Error loading courses:", e);
        tbody.innerHTML = "<tr><td colspan='6'>Error loading courses. Please try again.</td></tr>";
    }
}

async function loadStudentRegistrationsList() {
    const tbody = document.getElementById("studentRegistrationsListBody");
    tbody.innerHTML = "<tr><td colspan='4'>Loading registrations...</td></tr>";
    try {
        const res = await adminFetch("http://localhost:8000/admin/registrations");
        const registrations = await res.json();
        tbody.innerHTML = "";
        
        if (registrations.length === 0) {
            tbody.innerHTML = "<tr><td colspan='4'>No registrations found in the system.</td></tr>";
            return;
        }
        
        registrations.forEach(reg => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${reg.registrationID}</td>
                <td>${reg.CCRN}</td>
                <td>${reg.courseName || "N/A"}</td>
                <td>${reg.instructorName || "N/A"}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch (e) {
        console.error("Error loading registrations:", e);
        tbody.innerHTML = "<tr><td colspan='4'>Error loading registrations. Please try again.</td></tr>";
    }
}

async function loadStudentRegistrationsStudents() {
    const tbody = document.getElementById("studentRegistrationsStudentsListBody");
    tbody.innerHTML = "<tr><td colspan='7'>Loading students...</td></tr>";
    try {
        const res = await adminFetch("http://localhost:8000/admin/students");
        const students = await res.json();
        tbody.innerHTML = "";
        
        if (students.length === 0) {
            tbody.innerHTML = "<tr><td colspan='7'>No students found in the system.</td></tr>";
            return;
        }
        
        students.forEach(student => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${student.studentID}</td>
                <td>${student.Fname || "N/A"}</td>
                <td>${student.Lname || "N/A"}</td>
                <td>${student.username || "N/A"}</td>
                <td>${student.email || "N/A"}</td>
                <td>${student.phoneNB || "N/A"}</td>
                <td>${student.address || "N/A"}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch (e) {
        console.error("Error loading students:", e);
        tbody.innerHTML = "<tr><td colspan='7'>Error loading students. Please try again.</td></tr>";
    }
}

async function loadAllCoordinators() {
    const tbody = document.getElementById("coordinatorsListBody");
    tbody.innerHTML = "<tr><td colspan='6'>Loading coordinators...</td></tr>";
    try {
        const res = await adminFetch("http://localhost:8000/admin/coordinators");
        const coordinators = await res.json();
        tbody.innerHTML = "";
        
        if (coordinators.length === 0) {
            tbody.innerHTML = "<tr><td colspan='6'>No coordinators found in the system.</td></tr>";
            return;
        }
        
        coordinators.forEach(coord => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${coord.coordinatorID}</td>
                <td>${coord.Fname || ""} ${coord.Lname || ""}</td>
                <td>${coord.email || "N/A"}</td>
                <td>${coord.phoneNB || "N/A"}</td>
                <td>${coord.address || "N/A"}</td>
                <td>${coord.departmentID ?? "N/A"}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch (e) {
        console.error("Error loading coordinators:", e);
        tbody.innerHTML = "<tr><td colspan='6'>Error loading coordinators. Please try again.</td></tr>";
    }
}

async function loadAdminCourses() {
    setAdminError("");
    const tbody = document.getElementById("adminCoursesBody");
    tbody.innerHTML = "<tr><td colspan='7'>Loading...</td></tr>";
    try {
        const res = await adminFetch("http://localhost:8000/admin/courses");
        const rows = await res.json();
        tbody.innerHTML = "";
        rows.forEach(c => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${c.CCRN}</td>
                <td>${c.programID ?? ""}</td>
                <td>${c.coordinatorID ?? ""}</td>
                <td>${c.courseName || ""}</td>
                <td>${c.credits ?? ""}</td>
                <td>${c.courseDesc || ""}</td>
                <td>
                    <button class="btn-secondary">Edit</button>
                    <button class="btn-secondary">Delete</button>
                </td>`;
            const [editBtn, delBtn] = tr.querySelectorAll("button");
            editBtn.onclick = () => {
                document.getElementById("admCourseCCRN").value = c.CCRN;
                document.getElementById("admCourseProgram").value = c.programID ?? "";
                document.getElementById("admCourseCoordinatorID").value = c.coordinatorID ?? "";
                document.getElementById("admCourseName").value = c.courseName || "";
                document.getElementById("admCourseCredits").value = c.credits ?? "";
                document.getElementById("admCourseDesc").value = c.courseDesc || "";
            };
            delBtn.onclick = async () => {
                if (!confirm(`Delete course ${c.CCRN}?`)) return;
                await adminDeleteCourse(c.CCRN);
            };
            tbody.appendChild(tr);
        });
    } catch (e) {
        console.error(e);
        setAdminError(e.message || "Failed to load courses");
        tbody.innerHTML = "";
    }
}

async function adminAddCourse() {
    try {
        const payload = {
            CCRN: parseInt(document.getElementById("admCourseCCRN").value, 10),
            programID: parseInt(document.getElementById("admCourseProgram").value || "0", 10),
            coordinatorID: parseInt(document.getElementById("admCourseCoordinatorID").value || "0", 10),
            courseName: document.getElementById("admCourseName").value,
            credits: parseInt(document.getElementById("admCourseCredits").value || "0", 10),
            courseDesc: document.getElementById("admCourseDesc").value
        };
        const res = await adminFetch("http://localhost:8000/admin/courses", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        if (!res.ok) throw new Error((await res.json().catch(() => ({}))).error || "Add failed");
        adminClearCourseForm();
        await loadAdminCourses();
    } catch (e) {
        setAdminError(e.message || "Add failed");
    }
}

async function adminUpdateCourse() {
    try {
        const payload = {
            CCRN: parseInt(document.getElementById("admCourseCCRN").value, 10),
            programID: parseInt(document.getElementById("admCourseProgram").value || "0", 10),
            coordinatorID: parseInt(document.getElementById("admCourseCoordinatorID").value || "0", 10),
            courseName: document.getElementById("admCourseName").value,
            credits: parseInt(document.getElementById("admCourseCredits").value || "0", 10),
            courseDesc: document.getElementById("admCourseDesc").value
        };
        const res = await adminFetch("http://localhost:8000/admin/courses", {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        const body = await res.json().catch(() => ({}));
        if (!res.ok) throw new Error(body.error || "Update failed");
        await loadAdminCourses();
    } catch (e) {
        setAdminError(e.message || "Update failed");
    }
}

async function adminDeleteCourse(CCRN) {
    try {
        const res = await adminFetch("http://localhost:8000/admin/courses", {
            method: "DELETE",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ CCRN })
        });
        const body = await res.json().catch(() => ({}));
        if (!res.ok) throw new Error(body.error || "Delete failed");
        await loadAdminCourses();
    } catch (e) {
        setAdminError(e.message || "Delete failed");
    }
}

// ---- Registrations ----
function adminClearRegistrationForm() {
    document.getElementById("admRegistrationCCRN").value = "";
    document.getElementById("admRegistrationInstructorID").value = "";
}

async function loadAdminRegistrations() {
    setAdminError("");
    const tbody = document.getElementById("adminRegistrationsBody");
    tbody.innerHTML = "<tr><td colspan='6'>Loading...</td></tr>";
    try {
        const res = await adminFetch("http://localhost:8000/admin/registrations");
        const rows = await res.json();
        tbody.innerHTML = "";
        rows.forEach(r => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${r.registrationID}</td>
                <td>${r.CCRN}</td>
                <td>${r.courseName || ""}</td>
                <td>${r.instructorID}</td>
                <td>${r.instructorName || ""}</td>
                <td>
                    <button class="btn-secondary">Delete</button>
                </td>`;
            const [delBtn] = tr.querySelectorAll("button");
            delBtn.onclick = async () => {
                if (!confirm(`Delete registration ${r.registrationID}? This will also delete all related student registrations.`)) return;
                await adminDeleteRegistration(r.registrationID);
            };
            tbody.appendChild(tr);
        });
    } catch (e) {
        console.error(e);
        setAdminError(e.message || "Failed to load registrations");
        tbody.innerHTML = "";
    }
}

async function adminAddRegistration() {
    try {
        const payload = {
            CCRN: parseInt(document.getElementById("admRegistrationCCRN").value, 10),
            instructorID: parseInt(document.getElementById("admRegistrationInstructorID").value, 10)
        };
        const res = await adminFetch("http://localhost:8000/admin/registrations", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        if (!res.ok) throw new Error((await res.json().catch(() => ({}))).error || "Add failed");
        adminClearRegistrationForm();
        await loadAdminRegistrations();
    } catch (e) {
        setAdminError(e.message || "Add failed");
    }
}

async function adminDeleteRegistration(registrationID) {
    try {
        const res = await adminFetch("http://localhost:8000/admin/registrations", {
            method: "DELETE",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ registrationID })
        });
        const body = await res.json().catch(() => ({}));
        if (!res.ok) throw new Error(body.error || "Delete failed");
        await loadAdminRegistrations();
    } catch (e) {
        setAdminError(e.message || "Delete failed");
    }
}

// ---- Student Registrations ----
function adminClearStudentRegistrationForm() {
    document.getElementById("admStudentRegistrationStudentID").value = "";
    document.getElementById("admStudentRegistrationRegistrationID").value = "";
}

async function loadAdminStudentRegistrations() {
    setAdminError("");
    const tbody = document.getElementById("adminStudentRegistrationsBody");
    tbody.innerHTML = "<tr><td colspan='9'>Loading...</td></tr>";
    try {
        const res = await adminFetch("http://localhost:8000/admin/student-registrations");
        const rows = await res.json();
        tbody.innerHTML = "";
        rows.forEach(sr => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${sr.studentID}</td>
                <td>${sr.studentName || ""}</td>
                <td>${sr.username || ""}</td>
                <td>${sr.registrationID}</td>
                <td>${sr.CCRN}</td>
                <td>${sr.courseName || ""}</td>
                <td>${sr.instructorID}</td>
                <td>${sr.instructorName || ""}</td>
                <td>
                    <button class="btn-secondary">Delete</button>
                </td>`;
            const [delBtn] = tr.querySelectorAll("button");
            delBtn.onclick = async () => {
                if (!confirm(`Delete student registration (Student ${sr.studentID}, Registration ${sr.registrationID})?`)) return;
                await adminDeleteStudentRegistration(sr.studentID, sr.registrationID);
            };
            tbody.appendChild(tr);
        });
    } catch (e) {
        console.error(e);
        setAdminError(e.message || "Failed to load student registrations");
        tbody.innerHTML = "";
    }
}

async function adminAddStudentRegistration() {
    try {
        const payload = {
            studentID: parseInt(document.getElementById("admStudentRegistrationStudentID").value, 10),
            registrationID: parseInt(document.getElementById("admStudentRegistrationRegistrationID").value, 10)
        };
        const res = await adminFetch("http://localhost:8000/admin/student-registrations", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        if (!res.ok) throw new Error((await res.json().catch(() => ({}))).error || "Add failed");
        adminClearStudentRegistrationForm();
        await loadAdminStudentRegistrations();
    } catch (e) {
        setAdminError(e.message || "Add failed");
    }
}

async function adminDeleteStudentRegistration(studentID, registrationID) {
    try {
        const res = await adminFetch("http://localhost:8000/admin/student-registrations", {
            method: "DELETE",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ studentID, registrationID })
        });
        const body = await res.json().catch(() => ({}));
        if (!res.ok) throw new Error(body.error || "Delete failed");
        await loadAdminStudentRegistrations();
    } catch (e) {
        setAdminError(e.message || "Delete failed");
    }
}

/* ====== MAIN MENU NAVIGATION ====== */
function showCourseSearch() {
    document.getElementById("courseSearchInput").value = "";
    document.getElementById("courseSearchResults").innerHTML = "";
    document.getElementById("courseSearchError").style.display = "none";
    showScreen("courseSearchScreen");
}

function showInstructorSearch() {
    document.getElementById("instructorSearchInput").value = "";
    document.getElementById("instructorSearchResults").innerHTML = "";
    document.getElementById("instructorSearchError").style.display = "none";
    showScreen("instructorSearchScreen");
}

function showCoursesForEvaluation() {
    populateCoursesTable();
    showScreen("coursesScreen");
}

function backToMainMenu() {
    showScreen("mainMenuScreen");
}

/* ====== COURSES LIST ====== */
async function populateCoursesTable() {
    const tbody = document.getElementById("coursesTableBody");
    tbody.innerHTML = "<tr><td colspan='4'>Loading courses...</td></tr>";

    try {
        // Fetch courses from servlet
        const response = await fetch(`http://localhost:8000/student-course?username=${encodeURIComponent(currentUser)}`);
        if (!response.ok) {
            throw new Error("Failed to fetch courses");
        }
        const courses = await response.json();

        tbody.innerHTML = ""; // clear loading message

        // Map servlet course data to frontend format
        courses.forEach(course => {
            const tr = document.createElement("tr");

            const tdCode = document.createElement("td");
            tdCode.textContent = course.CCRN || "N/A";

            const tdTitle = document.createElement("td");
            tdTitle.textContent = course.courseName || "N/A";

            const tdInstr = document.createElement("td");
            tdInstr.textContent = course.instructorName; // Instructor info would need to come from another endpoint

            const tdActions = document.createElement("td");

            // Check if course evaluation is completed (DB-backed flag from /student-course)
            const courseCompleted = !!course.courseAssessmentDone || !!completedCourseEvaluations[course.CCRN];
            const btnCourseEval = document.createElement("button");
            if (courseCompleted) {
                btnCourseEval.textContent = "Done";
                btnCourseEval.className = "btn-done";
                btnCourseEval.disabled = true;
            } else {
                btnCourseEval.textContent = "Course Evaluation";
                btnCourseEval.className = "btn-primary";
                btnCourseEval.onclick = () => startCourseEvaluation(course.CCRN);
            }

            // Check if instructor evaluation is completed (DB-backed flag from /student-course)
            const instructorCompleted = !!course.instructorAssessmentDone || !!completedInstructorEvaluations[course.CCRN];
            const btnInstrEval = document.createElement("button");
            if (instructorCompleted) {
                btnInstrEval.textContent = "Done";
                btnInstrEval.className = "btn-done";
                btnInstrEval.disabled = true;
            } else {
                btnInstrEval.textContent = "Instructor Evaluation";
                btnInstrEval.className = "btn-secondary";
                btnInstrEval.onclick = () => startInstructorEvaluation(course.CCRN);
            }

            tdActions.appendChild(btnCourseEval);
            tdActions.appendChild(btnInstrEval);

            tr.appendChild(tdCode);
            tr.appendChild(tdTitle);
            tr.appendChild(tdInstr);
            tr.appendChild(tdActions);

            tbody.appendChild(tr);
        });
    } catch (error) {
        console.error("Error loading courses:", error);
        tbody.innerHTML = "<tr><td colspan='4'>Error loading courses. Please try again.</td></tr>";
    }
}

function backToCourses() {
    showScreen("coursesScreen");
}

/* ====== COURSE EVALUATION FLOW ====== */
async function startCourseEvaluation(courseId) {
    currentCourseId = courseId;
    currentCourseQuestionIndex = 0;

    // Fetch course details from servlet
    try {
        const response = await fetch("http://localhost:8000/courses");
        const courses = await response.json();
        const course = courses.find(c => c.CCRN === courseId);

        if (course) {
            document.getElementById("courseEvalHeader").textContent =
                `Evaluating course: ${course.CCRN} - ${course.courseName}`;
        } else {
            document.getElementById("courseEvalHeader").textContent =
                `Evaluating course: ${courseId}`;
        }
    } catch (error) {
        document.getElementById("courseEvalHeader").textContent =
            `Evaluating course: ${courseId}`;
    }

    // Make sure answers object exists for this course
    if (!courseAnswers[courseId]) {
        courseAnswers[courseId] = {};
    }

    renderCourseQuestion();
    updateCourseButtons();
    updateCourseProgress();
    showScreen("courseEvalScreen");
}

function renderCourseQuestion() {
    const container = document.getElementById("courseQuestionContainer");
    const courseId = currentCourseId;

    container.innerHTML = ""; // clear

    // Check if we're on the recommendations step
    if (currentCourseQuestionIndex === COURSE_RECOMMENDATIONS_INDEX) {
        renderCourseRecommendations();
    } else {
        // Render a rating question
        const questionText = courseQuestions[currentCourseQuestionIndex];
        const savedAnswer = courseAnswers[courseId][currentCourseQuestionIndex] || "";

        const div = document.createElement("div");
        div.className = "question";

        const q = document.createElement("div");
        q.className = "question-text";
        q.textContent = `Q${currentCourseQuestionIndex + 1}. ${questionText}`;

        const ratingDiv = document.createElement("div");
        ratingDiv.className = "rating-options";
        ratingDiv.textContent = "Your rating: ";

        // Create 1–5 radio buttons
        for (let i = 1; i <= 5; i++) {
            const label = document.createElement("label");
            const radio = document.createElement("input");
            radio.type = "radio";
            radio.name = "courseRating";
            radio.value = i;
            if (savedAnswer == i) radio.checked = true;

            label.appendChild(radio);
            label.appendChild(document.createTextNode(" " + i));
            ratingDiv.appendChild(label);
        }

        div.appendChild(q);
        div.appendChild(ratingDiv);
        container.appendChild(div);
    }

    updateCourseProgress();
}

function renderCourseRecommendations() {
    const container = document.getElementById("courseQuestionContainer");
    const courseId = currentCourseId;
    const savedRecommendation = courseAnswers[courseId].recommendation || "";

    const div = document.createElement("div");
    div.className = "recommendations-section";

    const label = document.createElement("label");
    label.textContent = "Your recommendations for this course (optional):";
    label.setAttribute("for", "courseRecommendation");

    const textarea = document.createElement("textarea");
    textarea.id = "courseRecommendation";
    textarea.name = "courseRecommendation";
    textarea.placeholder = "Please share any recommendations or suggestions for improving this course...";
    textarea.value = savedRecommendation;

    div.appendChild(label);
    div.appendChild(textarea);
    container.appendChild(div);
}

function saveCurrentCourseAnswer() {
    // Check if we're on the recommendations step
    if (currentCourseQuestionIndex === COURSE_RECOMMENDATIONS_INDEX) {
        const textarea = document.getElementById("courseRecommendation");
        if (textarea) {
            courseAnswers[currentCourseId].recommendation = textarea.value.trim();
        }
    } else {
        // Save rating answer
        const radios = document.getElementsByName("courseRating");
        let selected = null;
        radios.forEach(r => {
            if (r.checked) selected = r.value;
        });
        courseAnswers[currentCourseId][currentCourseQuestionIndex] = selected;
    }
}

function updateCourseButtons() {
    const prevBtn = document.getElementById("coursePrevBtn");
    const nextBtn = document.getElementById("courseNextBtn");
    const submitBtn = document.getElementById("courseSubmitBtn");

    // Show Previous button unless we're on the first question
    prevBtn.style.display = currentCourseQuestionIndex === 0 ? "none" : "inline-block";

    // If on recommendations step, show Submit button
    if (currentCourseQuestionIndex === COURSE_RECOMMENDATIONS_INDEX) {
        nextBtn.style.display = "none";
        submitBtn.style.display = "inline-block";
    }
    // If on last rating question, show Next button (to go to recommendations)
    else if (currentCourseQuestionIndex === courseQuestions.length - 1) {
        nextBtn.style.display = "inline-block";
        submitBtn.style.display = "none";
    }
    // Otherwise, show Next button
    else {
        nextBtn.style.display = "inline-block";
        submitBtn.style.display = "none";
    }
}

function nextCourseQuestion() {
    saveCurrentCourseAnswer();
    // Move to next step (either next rating question or recommendations)
    if (currentCourseQuestionIndex < COURSE_RECOMMENDATIONS_INDEX) {
        currentCourseQuestionIndex++;
        renderCourseQuestion();
        updateCourseButtons();
        updateCourseProgress();
    }
}

function prevCourseQuestion() {
    saveCurrentCourseAnswer();
    if (currentCourseQuestionIndex > 0) {
        currentCourseQuestionIndex--;
        renderCourseQuestion();
        updateCourseButtons();
        updateCourseProgress();
    }
}

function submitCourseEvaluation() {
    saveCurrentCourseAnswer();

    // Send to backend (MySQL) - one submission per (student, course)
    const payload = {
        username: currentUser,
        ccrn: currentCourseId,
        rating1: parseInt(courseAnswers[currentCourseId][0] || "0", 10),
        rating2: parseInt(courseAnswers[currentCourseId][1] || "0", 10),
        rating3: parseInt(courseAnswers[currentCourseId][2] || "0", 10),
        rating4: parseInt(courseAnswers[currentCourseId][3] || "0", 10),
        recommendations: courseAnswers[currentCourseId].recommendation || ""
    };

    fetch("http://localhost:8000/course-assessment", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    })
        .then(async (res) => {
            if (res.status === 409) {
                const err = await res.json().catch(() => ({}));
                throw new Error(err.error || "You already submitted this course evaluation.");
            }
            if (!res.ok) {
                const err = await res.json().catch(() => ({}));
                throw new Error(err.error || "Failed to submit course evaluation.");
            }
            return res.json().catch(() => ({}));
        })
        .then(() => {
            completedCourseEvaluations[currentCourseId] = true;
            showScreen("thankYouScreen");
        })
        .catch((err) => {
            alert(err.message || "Error submitting evaluation");
        });
}

/* ====== INSTRUCTOR EVALUATION FLOW ====== */
async function startInstructorEvaluation(courseId) {
    currentCourseId = courseId;
    currentInstrQuestionIndex = 0;

    // Fetch course details from servlet
    try {
        const response = await fetch("http://localhost:8000/courses");
        const courses = await response.json();
        const course = courses.find(c => c.CCRN === courseId);

        if (course) {
            document.getElementById("instructorEvalHeader").textContent =
                `Evaluating instructor for: ${course.CCRN} - ${course.courseName}`;
        } else {
            document.getElementById("instructorEvalHeader").textContent =
                `Evaluating instructor for course: ${courseId}`;
        }
    } catch (error) {
        document.getElementById("instructorEvalHeader").textContent =
            `Evaluating instructor for course: ${courseId}`;
    }

    if (!instructorAnswers[courseId]) {
        instructorAnswers[courseId] = {};
    }

    renderInstructorQuestion();
    updateInstructorButtons();
    updateInstructorProgress();
    showScreen("instructorEvalScreen");
}

function renderInstructorQuestion() {
    const container = document.getElementById("instructorQuestionContainer");
    const courseId = currentCourseId;

    container.innerHTML = "";

    // Check if we're on the recommendations step
    if (currentInstrQuestionIndex === INSTRUCTOR_RECOMMENDATIONS_INDEX) {
        renderInstructorRecommendations();
    } else {
        // Render a rating question
        const questionText = instructorQuestions[currentInstrQuestionIndex];
        const savedAnswer = instructorAnswers[courseId][currentInstrQuestionIndex] || "";

        const div = document.createElement("div");
        div.className = "question";

        const q = document.createElement("div");
        q.className = "question-text";
        q.textContent = `Q${currentInstrQuestionIndex + 1}. ${questionText}`;

        const ratingDiv = document.createElement("div");
        ratingDiv.className = "rating-options";
        ratingDiv.textContent = "Your rating: ";

        for (let i = 1; i <= 5; i++) {
            const label = document.createElement("label");
            const radio = document.createElement("input");
            radio.type = "radio";
            radio.name = "instrRating";
            radio.value = i;
            if (savedAnswer == i) radio.checked = true;

            label.appendChild(radio);
            label.appendChild(document.createTextNode(" " + i));
            ratingDiv.appendChild(label);
        }

        div.appendChild(q);
        div.appendChild(ratingDiv);
        container.appendChild(div);
    }

    updateInstructorProgress();
}

function renderInstructorRecommendations() {
    const container = document.getElementById("instructorQuestionContainer");
    const courseId = currentCourseId;
    const savedRecommendation = instructorAnswers[courseId].recommendation || "";

    const div = document.createElement("div");
    div.className = "recommendations-section";

    const label = document.createElement("label");
    label.textContent = "Your recommendations for this instructor (optional):";
    label.setAttribute("for", "instructorRecommendation");

    const textarea = document.createElement("textarea");
    textarea.id = "instructorRecommendation";
    textarea.name = "instructorRecommendation";
    textarea.placeholder = "Please share any recommendations or suggestions for this instructor...";
    textarea.value = savedRecommendation;

    div.appendChild(label);
    div.appendChild(textarea);
    container.appendChild(div);
}

function saveCurrentInstructorAnswer() {
    // Check if we're on the recommendations step
    if (currentInstrQuestionIndex === INSTRUCTOR_RECOMMENDATIONS_INDEX) {
        const textarea = document.getElementById("instructorRecommendation");
        if (textarea) {
            instructorAnswers[currentCourseId].recommendation = textarea.value.trim();
        }
    } else {
        // Save rating answer
        const radios = document.getElementsByName("instrRating");
        let selected = null;
        radios.forEach(r => {
            if (r.checked) selected = r.value;
        });
        instructorAnswers[currentCourseId][currentInstrQuestionIndex] = selected;
    }
}

function updateInstructorButtons() {
    const prevBtn = document.getElementById("instrPrevBtn");
    const nextBtn = document.getElementById("instrNextBtn");
    const submitBtn = document.getElementById("instrSubmitBtn");

    // Show Previous button unless we're on the first question
    prevBtn.style.display = currentInstrQuestionIndex === 0 ? "none" : "inline-block";

    // If on recommendations step, show Submit button
    if (currentInstrQuestionIndex === INSTRUCTOR_RECOMMENDATIONS_INDEX) {
        nextBtn.style.display = "none";
        submitBtn.style.display = "inline-block";
    }
    // If on last rating question, show Next button (to go to recommendations)
    else if (currentInstrQuestionIndex === instructorQuestions.length - 1) {
        nextBtn.style.display = "inline-block";
        submitBtn.style.display = "none";
    }
    // Otherwise, show Next button
    else {
        nextBtn.style.display = "inline-block";
        submitBtn.style.display = "none";
    }
}

function nextInstructorQuestion() {
    saveCurrentInstructorAnswer();
    // Move to next step (either next rating question or recommendations)
    if (currentInstrQuestionIndex < INSTRUCTOR_RECOMMENDATIONS_INDEX) {
        currentInstrQuestionIndex++;
        renderInstructorQuestion();
        updateInstructorButtons();
        updateInstructorProgress();
    }
}

function prevInstructorQuestion() {
    saveCurrentInstructorAnswer();
    if (currentInstrQuestionIndex > 0) {
        currentInstrQuestionIndex--;
        renderInstructorQuestion();
        updateInstructorButtons();
        updateInstructorProgress();
    }
}

function submitInstructorEvaluation() {
    saveCurrentInstructorAnswer();

    // Send to backend (MySQL) - one submission per (student, instructor)
    // Backend determines instructorID using (username, ccrn) from Registration/studentRegistration.
    const payload = {
        username: currentUser,
        ccrn: currentCourseId,
        rating1: parseInt(instructorAnswers[currentCourseId][0] || "0", 10),
        rating2: parseInt(instructorAnswers[currentCourseId][1] || "0", 10),
        rating3: parseInt(instructorAnswers[currentCourseId][2] || "0", 10),
        rating4: parseInt(instructorAnswers[currentCourseId][3] || "0", 10),
        recommendations: instructorAnswers[currentCourseId].recommendation || ""
    };

    fetch("http://localhost:8000/instructor-assessment", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    })
        .then(async (res) => {
            if (res.status === 409) {
                const err = await res.json().catch(() => ({}));
                throw new Error(err.error || "You already submitted this instructor evaluation.");
            }
            if (!res.ok) {
                const err = await res.json().catch(() => ({}));
                throw new Error(err.error || "Failed to submit instructor evaluation.");
            }
            return res.json().catch(() => ({}));
        })
        .then(() => {
            completedInstructorEvaluations[currentCourseId] = true;
            showScreen("thankYouScreen");
        })
        .catch((err) => {
            alert(err.message || "Error submitting evaluation");
        });
}

/* ====== THANK YOU NAVIGATION ====== */
function goBackToCoursesFromThankYou() {
    populateCoursesTable(); // Refresh to show "Done" buttons
    showScreen("coursesScreen");
}

function calculateAvgRating(evaluation) {
    const ratings = [evaluation.rating1, evaluation.rating2, evaluation.rating3, evaluation.rating4]
        .map(Number)
        .filter(Number.isFinite);

    if (ratings.length === 0) return 0;

    const sum = ratings.reduce((a, b) => a + b, 0);
    return sum / ratings.length; // 0..5
}


function equalsIgnoreCaseInclude(string1, string2) {
    s1 = string1;
    s2 = string2;
    return (s1.toLowerCase().includes(s2.toLowerCase()));
}

/* ====== COURSE SEARCH FUNCTIONALITY ====== */
async function searchCourse() {
    const searchInput = document.getElementById("courseSearchInput").value.trim();
    const errorDiv = document.getElementById("courseSearchError");
    const resultsDiv = document.getElementById("courseSearchResults");

    if (!searchInput) {
        errorDiv.style.display = "block";
        errorDiv.textContent = "Please enter a course code (CCRN).";
        resultsDiv.innerHTML = "";
        return;
    }

    errorDiv.style.display = "none";
    resultsDiv.innerHTML = "<div class='search-result-card'><p>Loading...</p></div>";

    try {
        // Parse CCRN (should be a number)
        const ccrn = parseInt(searchInput);
        if (isNaN(ccrn)) {
            throw new Error("Course code must be a number (CCRN)");
        }

        // Fetch course assessments from servlet
        const response = await fetch(`http://localhost:8000/course-assessment?ccrn=${ccrn}`);
        if (!response.ok) {
            if (response.status === 400) {
                const error = await response.json();
                throw new Error(error.error || "Invalid course code");
            }
            throw new Error("Failed to fetch course assessments");
        }
        const assessments = await response.json();

        // Also fetch course details
        const coursesResponse = await fetch("http://localhost:8000/courses");
        const courses = await coursesResponse.json();
        const course = courses.find(c => c.CCRN === ccrn);

        if (!course) {
            resultsDiv.innerHTML = `<div class="search-result-card"><p>Course "${searchInput}" not found.</p></div>`;
            return;
        }

        // Get all evaluations for this course (from assessments)
        const evaluations = assessments || [];

        // Calculate average rating
        // Calculate average rating
        let averageRating = 0;
        if (evaluations.length > 0) {
            let totalRating = 0;
            let ratingCount = 0;

            evaluations.forEach(ev => {
                const r = calculateAvgRating(ev);
                if (Number.isFinite(r) && r > 0) {
                    totalRating += r;
                    ratingCount++;
                }
            });

            averageRating = ratingCount > 0 ? (totalRating / ratingCount) : 0;
        }


        // Build results HTML
        let html = `<div class="search-result-card">
            <h3>${course.CCRN} - ${course.courseName}</h3>
            <p><strong>Credits:</strong> ${course.credits}</p>
            <p><strong>Description:</strong> ${course.courseDesc || "N/A"}</p>`;

        if (evaluations.length > 0) {
            html += `<div class="rating-display">Average Rating: ${averageRating.toFixed(2)} / 5.00</div>`;
            html += `<div class="rating-stars">${'★'.repeat(Math.round(averageRating))}${'☆'.repeat(5 - Math.round(averageRating))}</div>`;
            html += `<p><strong>Total Reviews:</strong> ${evaluations.length}</p>`;

            // Show reviews
            html += `<div class="reviews-section"><h4>Reviews:</h4>`;
            evaluations.forEach((eval, index) => {
                html += `<div class="review-item">
                    <p><strong>Review ${index + 1}</strong> (Student ID: ${eval.studentID})</p>
                    <p><strong>Rating:</strong> ${calculateAvgRating(eval)}/5</p>
                    <p><strong>Recommendations:</strong> ${eval.recommendations || "N/A"}</p>
                    <p><strong>Date:</strong> ${eval.courseAssessmentDate}</p>
                </div>`;
            });
            html += `</div>`;
        } else {
            html += `<p class="no-reviews">No evaluations submitted yet for this course.</p>`;
        }

        html += `</div>`;
        resultsDiv.innerHTML = html;
    } catch (error) {
        console.error("Search error:", error);
        errorDiv.style.display = "block";
        errorDiv.textContent = error.message || "Error searching for course.";
        resultsDiv.innerHTML = "";
    }
}



/* ====== INSTRUCTOR SEARCH FUNCTIONALITY ====== */
async function searchInstructor() {
    const searchInput = document.getElementById("instructorSearchInput").value.trim();
    const errorDiv = document.getElementById("instructorSearchError");
    const resultsDiv = document.getElementById("instructorSearchResults");

    resultsDiv.innerHTML = "";

    if (!searchInput) {
        errorDiv.style.display = "block";
        errorDiv.textContent = "Please enter Name of an Instructor.";
        resultsDiv.innerHTML = "";
        return;
    }

    errorDiv.style.display = "none";
    resultsDiv.innerHTML = "<div class='search-result-card'><p>Loading...</p></div>";

    try {

        const instructorName = searchInput;
        if (instructorName == null) {
            throw new Error("Instructor Name not Valid");
        }

        // Fetch instructor assessments from servlet
        const response = await fetch(`http://localhost:8000/instructor-assessment?instructorName=${instructorName}`);
        if (!response.ok) {
            if (response.status === 400) {
                const error = await response.json();
                throw new Error(error.error || "Invalid instructor Name");
            }
            throw new Error("Failed to fetch instructor assessments");
        }
        const assessments = await response.json();

        // Also fetch instructor details
        const instructorsResponse = await fetch("http://localhost:8000/instructors");
        const instructors = await instructorsResponse.json();
        const matchingInstructors = instructors.filter(i => equalsIgnoreCaseInclude(i.instructorName, instructorName));

        if (matchingInstructors.length === 0) {
            resultsDiv.innerHTML = `<div class="search-result-card"><p>Instructor "${searchInput}" not found.</p></div>`;
            return;
        }

        // Get all evaluations for all instructors that has instructorName in their name
        let html = "";

        matchingInstructors.forEach(instructor => {
            const allEvals = (assessments || []).filter(a => a.instructorID === instructor.instructorID);

            // Calculate average rating
            // Calculate average rating
            let averageRating = 0;
            if (allEvals.length > 0) {
                let totalRating = 0;
                let ratingCount = 0;

                allEvals.forEach(ev => {
                    const r = calculateAvgRating(ev);
                    if (Number.isFinite(r) && r > 0) {
                        totalRating += r;
                        ratingCount++;
                    }
                });

                averageRating = ratingCount > 0 ? (totalRating / ratingCount) : 0;
            }


            // Build results HTML
            html += `<div class="search-result-card">
            <h3>${instructor.Fname} ${instructor.Lname}</h3>
            <p><strong>Email:</strong> ${instructor.email}</p>
            <p><strong>Phone:</strong> ${instructor.phoneNB || "N/A"}</p>`;

            if (allEvals.length > 0) {
                html += `<div class="rating-display">Average Rating: ${averageRating.toFixed(2)} / 5.00</div>`;
                html += `<div class="rating-stars">${'★'.repeat(Math.round(averageRating))}${'☆'.repeat(5 - Math.round(averageRating))}</div>`;
                html += `<p><strong>Total Reviews:</strong> ${allEvals.length}</p>`;

                // Show reviews
                html += `<div class="reviews-section"><h4>Reviews:</h4>`;
                allEvals.forEach((eval, index) => {
                    html += `<div class="review-item">
                    <p><strong>Review ${index + 1}</strong> (Student ID: ${eval.studentID})</p>
                    <p><strong>Rating:</strong> ${calculateAvgRating(eval)}/5</p>
                    <p><strong>Recommendations:</strong> ${eval.recommendations || "N/A"}</p>
                    <p><strong>Date:</strong> ${eval.instructorAssessmentDate}</p>
                </div>`;
                });
                html += `</div>`;
            } else {
                html += `<p class="no-reviews">No evaluations submitted yet for this instructor.</p>`;
            }

            html += `</div>`;
        });

        resultsDiv.innerHTML = html;
    } catch (error) {
        console.error("Search error:", error);
        errorDiv.style.display = "block";
        errorDiv.textContent = error.message || "Error searching for instructor.";
        resultsDiv.innerHTML = "";
    }
}