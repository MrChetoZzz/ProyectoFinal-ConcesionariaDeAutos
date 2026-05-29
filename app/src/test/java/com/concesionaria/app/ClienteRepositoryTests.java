package com.concesionaria.app;

import com.concesionaria.app.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
class ClienteRepositoryTests {

    @Autowired
    private ClienteRepository clienteRepository;

    @Test
    void findResumenesReturnsClientesFromDatabase() {
        var clientes = findResumenesIfRbacIsConfigured();

        assertThat(clientes).isNotEmpty();
        assertThat(clientes.get(0).nombreCompleto()).isNotBlank();
    }

    private java.util.List<com.concesionaria.app.dto.ClienteResumenDto> findResumenesIfRbacIsConfigured() {
        try {
            return clienteRepository.findResumenes();
        } catch (InvalidDataAccessResourceUsageException ex) {
            var message = ex.getMostSpecificCause().getMessage();
            assumeTrue(
                message == null || !message.contains("SELECT permission was denied"),
                "RBAC pendiente: aplicar los GRANT de RBAC.md para Login_AppConcesionaria."
            );
            throw ex;
        }
    }
}
