# Sprint 1 Checklist

## Database
- [ ] V1 tenants
- [ ] V2 clinics
- [ ] V3 users
- [ ] V4 doctors
- [ ] V5 patients
- [ ] V6 doctor availability
- [ ] Verify Flyway clean migration
- [ ] Verify `ddl-auto=validate`
- [ ] Review indexes
- [ ] Review tenant foreign keys

## Backend
- [ ] Common API response
- [ ] Exception handling
- [ ] Tenant entity
- [ ] Tenant repository
- [ ] Tenant service
- [ ] Tenant controller
- [ ] Validation
- [ ] Health endpoint

## Security foundation
- [ ] Stateless API
- [ ] Public health endpoint
- [ ] Temporary tenant bootstrap endpoint
- [ ] Plan JWT authentication for Sprint 2
- [ ] Remove/lock down bootstrap endpoint before production

## Testing
- [ ] Unit test tenant service
- [ ] Migration test
- [ ] Integration test with PostgreSQL/Testcontainers
- [ ] API validation test
- [ ] Tenant isolation test

## Exit criteria
- [ ] Fresh database starts successfully
- [ ] All Flyway migrations pass
- [ ] Application starts
- [ ] Tenant can be created
- [ ] Tenant can be retrieved
- [ ] CI passes
