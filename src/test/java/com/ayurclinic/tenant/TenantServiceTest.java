package com.ayurclinic.tenant;

import com.ayurclinic.tenant.dto.CreateTenantRequest;
import com.ayurclinic.tenant.repository.TenantRepository;
import com.ayurclinic.tenant.service.TenantService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TenantServiceTest {

    @Test
    void createsTenant() {
        TenantRepository repository = Mockito.mock(TenantRepository.class);
        TenantService service = new TenantService(repository);

        when(repository.existsByNameIgnoreCase("Pilot Clinic")).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.create(new CreateTenantRequest("Pilot Clinic"));

        assertThat(result.name()).isEqualTo("Pilot Clinic");
        assertThat(result.status()).isEqualTo("ACTIVE");
    }
}
