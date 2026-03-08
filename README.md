# CodeAndStock

Proiect separat în **backend** (API Spring Boot) și **frontend** (interfață statică).

## Structură

```
CodeAndStock/
├── pom.xml          # Parent Maven (module: backend)
├── backend/         # API REST, Timefold, JPA, Security
│   ├── pom.xml
│   ├── src/
│   └── mvnw, .mvn
└── frontend/        # Pagini statice (manager, planificare)
    ├── package.json
    ├── public/
    │   ├── index.html
    │   ├── manager.html
    │   └── redistribution.html
    └── README.md
```

## Backend

- **Tehnologii:** Spring Boot 3, JPA, PostgreSQL, Timefold Solver, Spring Security, JWT
- **Rulare:** `cd backend && mvn spring-boot:run` (port 8080)
- **Baza de date:** Creează în PostgreSQL baza `phone_accessories_stock` (vezi `backend/src/main/resources/application.properties`)

## Frontend

- **Conținut:** Manager produse, Raport redistribuire stocuri (HTML/JS/CSS)
- **Rulare:** `cd frontend && npm start` (port 3000); necesită backend pornit pe 8080

## Build

Din rădăcina proiectului:

```bash
mvn clean install
```

Construiește doar modulul `backend`. Frontend-ul se rulează cu `npm start` din `frontend/`.
