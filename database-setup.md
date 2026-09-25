# Local clinic database setup

The five JPA tables are created by Spring Boot; MongoDB creates `prescriptions` on the first insert. The seed scripts assume a **fresh, empty** `cms` database so the generated appointment IDs match the MongoDB references. Do not run them against real patient data or a previously seeded database.

Run `mvn clean install` from `app/` without starting databases or setting credentials. Spring integration tests use the `test` profile with an in-memory H2 database, a test-only JWT signing key, and a prescription repository stub; they do not verify a live MongoDB connection. Running the application still requires the MySQL, MongoDB, and `JWT_SECRET` settings below.

For containers already initialized on this machine, reuse their original passwords. If they were created by this lab's local run, load them into the shell without printing them:

```bash
export MYSQL_ROOT_PASSWORD="$(docker inspect -f '{{range .Config.Env}}{{println .}}{{end}}' java-database-capstone-mysql-1 | sed -n 's/^MYSQL_ROOT_PASSWORD=//p')"
export MONGO_ROOT_PASSWORD="$(docker inspect -f '{{range .Config.Env}}{{println .}}{{end}}' java-database-capstone-mongo-1 | sed -n 's/^MONGO_INITDB_ROOT_PASSWORD=//p')"
```

Use Docker Compose from the repository root with your own passwords, kept out of version control:

```bash
export MYSQL_ROOT_PASSWORD='your-local-mysql-password'
export MONGO_ROOT_PASSWORD='your-local-mongo-password'
docker compose up -d --wait
export MYSQL_URL='jdbc:mysql://localhost:3307/cms'
export MYSQL_USER=root
export SPRING_DATASOURCE_PASSWORD="$MYSQL_ROOT_PASSWORD"
export MONGODB_URI="mongodb://root:${MONGO_ROOT_PASSWORD}@localhost:27018/prescriptions?authSource=admin"
export JWT_SECRET="$(openssl rand -hex 32)"
cd app
mvn spring-boot:run
```

When the application reports it has started, stop it; the `admin`, `appointment`, `doctor`, `doctor_available_times`, and `patient` tables should exist in `cms`. Run the following from the repository root:

```bash
docker compose exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -u root cms' < app/db/seed.sql
docker compose exec -T mongo sh -c 'mongosh --quiet -u root -p "$MONGO_INITDB_ROOT_PASSWORD" --authenticationDatabase admin prescriptions' < app/db/seed-prescriptions.js
```

The sample includes 25 doctors, 100 availability slots, 25 patients, 74 appointments (50 future scheduled, 24 past completed), one admin, and 24 prescriptions tied to completed appointments. Seed passwords are deliberately unusable placeholders. **After importing the seed**, restart the application with a private admin password:

```bash
export CLINIC_ADMIN_USERNAME=sample-admin
printf 'Admin password (12+ characters): '
read -s CLINIC_ADMIN_PASSWORD
printf '\n'
export CLINIC_ADMIN_PASSWORD
cd app
mvn spring-boot:run
```

This bootstrap replaces only a disabled account (or creates a new one); it never resets an active admin's password. Create doctors through the admin portal and patients through signup to obtain usable accounts. Sample phone numbers are ten digits as required by model validation.

Check the data from the repository root:

```bash
docker compose exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -u root cms -e "SHOW TABLES; SELECT * FROM doctor LIMIT 5; SELECT * FROM doctor_available_times LIMIT 5; SELECT * FROM patient LIMIT 5; SELECT * FROM appointment ORDER BY appointment_time LIMIT 5; SELECT * FROM admin;"'
docker compose exec -T mongo sh -c 'mongosh --quiet -u root -p "$MONGO_INITDB_ROOT_PASSWORD" --authenticationDatabase admin prescriptions --eval "db.prescriptions.find().limit(5).pretty()"'
```

The full output of these verification queries for the local synthetic dataset is saved in `database-verification.md`. Avoid publishing any real account or patient data. Passwords are required only for first-time container initialization; on subsequent runs of the same volumes, use the same values.

Dashboard views at `/adminDashboard/{token}` and `/doctorDashboard/{token}` require a signed, unexpired role-specific token for an existing account. Set `JWT_SECRET` to a private value of at least 32 bytes before starting the application and reuse the same value across restarts so existing sessions remain valid. The synthetic doctor and patient accounts remain disabled; they cannot log in until separately provisioned. Login and signup now issue role-bound tokens for active accounts. Avoid committing passwords, JWTs, or patient data; `config.js` uses the browser's current origin without needing to hard-code a public URL.

REST endpoints return JSON with `message` on errors and `token` on successful login. For example, after creating a patient account via `POST /patient`, log in via `POST /patient/login` with `{"email":"...","password":"..."}`, then use `GET /patient/{patientId}/patient/{token}` to retrieve that patient's appointments. Public doctor discovery is available at `GET /doctor` and `GET /doctor/filter/{name}/{time}/{speciality}`; use `null` for omitted filters. `time` accepts `AM`, `PM`, or a slot such as `09:00-10:00`. JWTs in path segments may appear in access logs or browser history; use only local/test accounts in this lab. Existing doctor appointment history blocks doctor deletion, and cancelling an appointment retains it with status `2` instead of erasing its record.

## Stored procedure reports

After the tables exist, install (or replace) the three reports using the MySQL CLI. This can be done before or after seeding:

```bash
docker compose exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -u root cms' < app/db/report-procedures.sql
```

To report on the synthetic seed data, run the following on the same day it was seeded:

```bash
docker compose exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -u root cms -e "CALL GetDailyAppointmentReportByDoctor(CURDATE() - INTERVAL 1 DAY); CALL GetDoctorWithMostPatientsByMonth(MONTH(CURDATE()), YEAR(CURDATE())); CALL GetDoctorWithMostPatientsByYear(YEAR(CURDATE()));"'
```

The daily report includes all appointment statuses for a date; the monthly and yearly reports count **distinct patients with completed visits** (`status = 1`), not future scheduled visits. If doctors are tied, the lowest doctor ID wins. The lab's literal 2025 dates do not match the relative dates generated by this seed; see `stored-procedure-verification.md` for the local run's output. In the IDE, run the file in a MySQL CLI (where `DELIMITER` is supported), then invoke the `CALL` statements in the database console.
