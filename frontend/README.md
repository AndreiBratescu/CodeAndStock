# Frontend (CodeAndStock)

Interfață statică: Manager produse și Raport redistribuire stocuri.

## Rulare

1. Pornește mai întâi **backend**-ul (port 8080):
   ```bash
   cd ../backend && mvn spring-boot:run
   ```

2. Pornește frontend-ul (port 3000):
   ```bash
   npm start
   ```

3. Deschide în browser: **http://localhost:3000**
   - [Manager produse](http://localhost:3000/manager.html)
   - [Raport redistribuire](http://localhost:3000/redistribution.html)

Frontend-ul apelează API-ul de la `http://localhost:8080` când e servit pe portul 3000.
