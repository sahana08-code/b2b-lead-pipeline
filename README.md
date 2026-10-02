# B2B Lead Pipeline

A web application for tracking B2B sales leads through a pipeline (New, Contacted, Qualified, Won, Lost), assigning them to sales reps, and spotting which leads need attention first. Java Spring Boot REST API with a MySQL database and a single-page HTML frontend.

## Features
- **Lead management:** create, view and delete leads with company, contact, phone, deal value and hot/not-hot flag
- **Pipeline stages:** move a lead through NEW, CONTACTED, QUALIFIED, WON or LOST from a dropdown in the table
- **Priority score:** ranks open leads using deal value, hot flag, stage and days since last contact
- **Analytics:** total leads, open pipeline value, won value, win rate and a count per stage
- **Overdue follow-ups:** lists open leads not contacted for more than N days (default 3)
- **Sales reps:** add reps, assign or unassign leads, and view each rep's assigned leads and total pipeline value

## Tech stack
Java 17, Spring Boot 4.0.7 (Spring Web, Spring Data JPA / Hibernate), MySQL (H2 in-memory database for demo mode), Maven, plain HTML/JavaScript frontend

## How the priority score works
For every lead that is not WON or LOST:

```
score = dealValue / 1000
      + 50   (if the lead is hot)
      + 30 / 15 / 5   (QUALIFIED / CONTACTED / NEW)
      + 2 x days since last contact
```

## REST API
| Method | Endpoint | What it does |
|---|---|---|
| GET | `/leads` | List all leads |
| GET | `/leads/{id}` | Get one lead |
| POST | `/leads` | Create a lead |
| DELETE | `/leads/{id}` | Delete a lead |
| PUT | `/leads/{id}/stage` | Change stage, body `{"stage":"QUALIFIED"}` |
| PUT | `/leads/{id}/value` | Change deal value, body `{"dealValue":50000}` |
| PUT | `/leads/{id}/assign/{repId}` | Assign a lead to a rep |
| PUT | `/leads/{id}/unassign` | Remove the rep from a lead |
| PUT | `/leads/{id}/contact` | Mark as contacted today |
| GET | `/leads/priority` | Leads sorted by priority score |
| GET | `/leads/analytics` | Pipeline summary |
| GET | `/leads/overdue?days=3` | Overdue follow-ups |
| GET / POST | `/reps` | List or create reps |
| DELETE | `/reps/{id}` | Delete a rep |
| GET | `/reps/{id}/performance` | A rep's leads and total pipeline value |

## How to run
You need **Java 17 or newer**. Maven and Spring Boot download themselves the first time.

### Option A: quick demo, no database needed
Uses a temporary in-memory database, so data disappears when you stop the app.

1. Open a terminal in the project folder (the one that contains `pom.xml`).
2. Run (PowerShell):
   ```
   .\mvnw spring-boot:run "-Dspring-boot.run.profiles=h2"
   ```
   On Mac or Linux: `./mvnw spring-boot:run -Dspring-boot.run.profiles=h2`
3. Wait for `Started LeadPipeline2Application`, then open **http://localhost:8080**.

### Option B: with MySQL (data is saved)
1. Install MySQL and create the database:
   ```sql
   CREATE DATABASE b2b_lead_pipeline;
   ```
2. The app expects MySQL on **port 3307** with user `root` (see `src/main/resources/application.properties`; change them if yours differ).
3. Set your MySQL password as an environment variable (PowerShell):
   ```
   $env:DB_PASSWORD="your_mysql_password"
   ```
4. Start the app:
   ```
   .\mvnw spring-boot:run
   ```
5. Open **http://localhost:8080**. Tables `leads2` and `sales_reps2` are created automatically.

If port 8080 is busy, add `server.port=8081` to `application.properties`, and also change the `API` address near the bottom of `src/main/resources/static/index.html` to match.

## Project structure
```
src/main/java/com/b2b/leadpipeline2/
  Lead.java, SalesRep.java                  database entities
  LeadRepository.java, SalesRepRepository   Spring Data JPA repositories
  LeadController.java, SalesRepController   REST endpoints and business logic
  WebConfig.java                            CORS settings
src/main/resources/static/index.html        frontend
```

## Known limitations
- No login or authentication, so anyone who can reach the app can read and change data.
- Missing leads or reps return an empty response instead of a proper 404 error.
- No input validation and no automated tests.
- A lead stores its rep as a plain `repId` number, not a database relationship, so deleting a rep leaves leads pointing to a rep that no longer exists.
- Stages and dates are stored as text. CORS allows every origin. Both are fine for a local project but not for production.

## Team
Built with Magdalene Sharon S.
