# 🍊 OrangeHRM – Candidate API Automation
> REST Assured + Java + TestNG automation for adding and deleting candidates via REST API

---

## 📌 Overview

This project automates the **Recruitment → Candidates** module of [OrangeHRM](https://opensource-demo.orangehrmlive.com) using REST Assured (Java). It covers:

- ✅ Navigating to the Candidates page
- ✅ Adding a new candidate via REST API
- ✅ Verifying the candidate exists
- ✅ Deleting the candidate via REST API
- ✅ Confirming deletion returns 404

---

## 🛠️ Tech Stack

| Tool | Version | Purpose |
|---|---|---|
| Java | 17+ | Programming language |
| REST Assured | 5.4.0 | API testing library |
| TestNG | 7.9.0 | Test runner & assertions |
| Maven | 3.8+ | Build & dependency management |
| Jackson | 2.17.1 | JSON serialization |
| Allure | 2.27.0 | Test reporting |

---

## 📁 Project Structure

```
orangehrm-restassured/
├── src/
│   └── test/
│       └── java/
│           └── tests/
│               └── CandidateApiTest.java   # Main test class
├── pom.xml                                  # Maven dependencies
├── testng.xml                               # TestNG suite config
├── .gitignore
└── README.md
```

---

## ⚙️ Prerequisites

Make sure you have the following installed:

```bash
java -version    # Java 17+
mvn -version     # Maven 3.8+
git --version    # Git
```

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/shereensaad/orangehrm-restassured.git
cd orangehrm-restassured
```

### 2. Install Dependencies

```bash
mvn clean install -DskipTests
```

### 3. Run Tests

```bash
mvn clean test
```

### 4. View Allure Report

```bash
mvn allure:serve
```

---

## 🔄 Test Flow

```
@BeforeClass  →  Login (scrape CSRF token → POST credentials → store session cookie)
     │
     ▼
Test 1  →  GET  /recruitment/viewCandidates          (assert 200)
     │
     ▼
Test 2  →  POST /api/v2/recruitment/candidates       (add candidate → save ID)
     │
     ▼
Test 3  →  GET  /api/v2/recruitment/candidates/{id}  (verify candidate exists)
     │
     ▼
Test 4  →  DELETE /api/v2/recruitment/candidates     (body: {"ids": [id]})
     │
     ▼
Test 5  →  GET  /api/v2/recruitment/candidates/{id}  (assert 404 — deleted)
```

---

## 🌐 API Reference

### Add Candidate
```http
POST /web/index.php/api/v2/recruitment/candidates
Content-Type: application/json
Cookie: orangehrm=<session>

{
  "firstName": "John",
  "middleName": "REST",
  "lastName": "Assured",
  "email": "john@example.com",
  "contactNumber": "0501234567",
  "keywords": "java, automation",
  "dateOfApplication": "2026-03-29",
  "comment": "Added via REST Assured",
  "consentToKeepData": true
}
```

### Delete Candidate
```http
DELETE /web/index.php/api/v2/recruitment/candidates
Content-Type: application/json
Cookie: orangehrm=<session>

{
  "ids": [42]
}
```

> 💡 OrangeHRM uses a **bulk-delete** design — IDs are always passed as an array in the request body, not as path/query parameters.

---

## 🧪 Test Configuration

| Property | Value |
|---|---|
| Base URL | `https://opensource-demo.orangehrmlive.com` |
| Username | `Admin` |
| Password | `admin123` |

To change credentials, update `CandidateApiTest.java`:

```java
private static final String USERNAME = "Admin";
private static final String PASSWORD = "admin123";
```

---

## 📊 Sample Test Output

```
── STEP 1 : Navigate to View Candidates ──
✅ /recruitment/viewCandidates returned 200

── STEP 2a : Add Candidate via REST API ──
📤 POST /api/v2/recruitment/candidates
✅ Candidate created – ID: 42

── STEP 2b : Verify Candidate in System ──
✅ Candidate ID 42 confirmed in OrangeHRM

── STEP 2c : Delete Candidate via REST API ──
🗑️  DELETE /api/v2/recruitment/candidates  body={ids=[42]}
✅ Candidate ID 42 deleted successfully

── STEP 2d : Confirm Deletion (expect 404) ──
✅ Candidate ID 42 confirmed deleted (404)
```

---

## 🤝 Contributing

1. Fork the repository
2. Create your branch → `git checkout -b feature/your-feature`
3. Commit your changes → `git commit -m "feat: your message"`
4. Push to the branch → `git push origin feature/your-feature`
5. Open a Pull Request

---

## 📄 License

This project is for educational and demo purposes using the public OrangeHRM demo environment.
