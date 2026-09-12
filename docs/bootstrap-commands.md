# Bootstrap Commands

From the project root:

```bash
mvn -N wrapper:wrapper
./mvnw test
docker compose up -d postgres
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
mvn -N wrapper:wrapper
.\mvnw.cmd test
docker compose up -d postgres
.\mvnw.cmd spring-boot:run
```

For the first production iteration, replace local database credentials with environment/Secrets Manager values.
