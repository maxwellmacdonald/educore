# EduCore – School Management System

EduCore is a compact full-stack school management app: a **Spring Boot 3 / MongoDB** REST API and a
**React + Vite + Tailwind** dashboard. It manages students, classes, subjects and daily attendance, and shows live
analytics computed from the database.

## Features

- **Students** – create, view, edit, delete; unique admission numbers; paginated list; search by name / admission number; filter by class; validation with clear field errors.
- **Classes** – create, list, update, delete; unique class names (case-insensitive); capacity 1–200.
- **Subjects** – create, list (optionally per class), update, delete; unique subject codes (stored upper-case).
- **Attendance** – mark Present / Absent / Late per student per day; one record per student per date (enforced in the service **and** by a unique MongoDB index); roster-style bulk save; attendance percentages from real records.
- **Dashboard** – totals, present/absent/late for a chosen date, attendance %, recent registrations, enrollment-by-class chart and 7-day attendance trend, all computed server-side.
- **UI** – navy collapsible sidebar, responsive layout, Recharts charts, toasts, confirmation dialogs, loading skeletons, empty states and honest connection-error panels (nothing is simulated when the API is down).

**Attendance percentage** = (present + late) ÷ total records × 100, rounded to one decimal; 0 when there are no records.

## Technology stack

| Layer | Tech |
|---|---|
| Backend | Java 17, Spring Boot 3.3.5, Maven, Spring Web, Spring Data MongoDB, Bean Validation |
| Tests | JUnit 5, Mockito, MockMvc (`@WebMvcTest`), AssertJ – no running MongoDB needed |
| Frontend | React 18, Vite 5, JavaScript, Tailwind CSS 3, React Router 6, Axios, Recharts, Lucide React |

## Prerequisites

- JDK 17 and Maven 3.9+
- Node.js 18+ (20/22 recommended) and npm
- MongoDB 6/7 (local, Docker, or Atlas)

## MongoDB configuration

The API reads `MONGODB_URI` (default `mongodb://localhost:27017/educore`).

```bash
# Option A – Docker (compose file included at the project root)
docker compose up -d mongo

# Option B – local install
mongod --dbpath /path/to/data

# Option C – Atlas / custom
export MONGODB_URI='mongodb+srv://<user>:<password>@<cluster>.mongodb.net/educore?retryWrites=true&w=majority'
```

All settings (see `backend/.env.example`, `backend/src/main/resources/application.properties`):

| Variable | Default | Purpose |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/educore` | MongoDB connection string |
| `SERVER_PORT` | `8080` | API port |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173` | Allowed frontend origins |
| `SEED_SAMPLE_DATA` | `true` | Insert labelled sample data into an **empty** database |

**Sample data:** when enabled, the seeder only runs if the students, classes and subjects collections are all empty, so it never inserts duplicates on restart. Sample records are easy to spot: admission numbers and subject codes start with `DEMO-`, teachers end in `(Sample)`, and the UI shows an amber **Sample** badge. Set `SEED_SAMPLE_DATA=false` to start with an empty database.

## Run the backend

```bash
cd backend
mvn spring-boot:run          # API on http://localhost:8080
```

## Run the frontend

```bash
cd frontend
npm install
npm run dev                  # http://localhost:5173
npm run build                # production build into frontend/dist
```

The API base URL defaults to `http://localhost:8080/api`; override with `VITE_API_URL` (see `frontend/.env.example`).

## Build and test

```bash
cd backend
mvn clean install
```

Surefire reports are written to `backend/target/surefire-reports/` (`*.txt` and `TEST-*.xml`).
The test suite mocks repositories / `MongoTemplate` and uses MockMvc slices, so it does not need MongoDB.

## API summary

| Method & path | Description |
|---|---|
| `GET /api/students?search=&className=&page=0&size=10` | Paged list (search name/admission no.; filter class) |
| `GET /api/students/{id}` · `POST /api/students` · `PUT /api/students/{id}` · `DELETE /api/students/{id}` | Student CRUD (201 / 204 / 404 / 409 / 400) |
| `GET /api/classes` · `POST` · `PUT /{id}` · `DELETE /{id}` | Class CRUD |
| `GET /api/subjects?className=` · `POST` · `PUT /{id}` · `DELETE /{id}` | Subject CRUD |
| `GET /api/attendance?date=&className=` | Attendance records for a date (and class) |
| `POST /api/attendance` | Mark one student (409 if already recorded for that date) |
| `POST /api/attendance/bulk` | Save a roster `{date, entries:[{studentId,status}]}` (create-or-update) |
| `PUT /api/attendance/{id}` | Change a record's status |
| `GET /api/attendance/stats?date=&className=&studentId=` | Present / absent / late / total / percentage |
| `GET /api/dashboard/summary?date=` | Dashboard totals, attendance, recent students, chart data |

Errors use one JSON shape: `{ timestamp, status, error, message, fieldErrors }`.

## Known limitations

- No authentication or roles (intentionally out of scope for this version).
- Class names are free text on students/subjects; renaming or deleting a class does not update or block related students/subjects, and capacity is not enforced on enrollment.
- Attendance "class" is copied from the student when the record is created and is not rewritten if the student later changes class.
- Dashboard enrollment uses one count query per class and the trend uses one query per day – fine for a school-sized dataset, not for very large ones.
- Search is a case-insensitive substring match (no text index).
- Automated tests mock the database; live MongoDB integration (unique indexes, queries, seeding) is verified by running the app against a real MongoDB.
