package com.concesionaria.app.repository;

import com.concesionaria.app.dto.ClienteResumenDto;
import com.concesionaria.app.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    @Query("""
        select new com.concesionaria.app.dto.ClienteResumenDto(
            c.id,
            p.nombre,
            p.domicilio,
            c.estaActivo,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )
        from Cliente c
        join c.persona p
        where c.estaActivo = true
        order by c.id
        """)
    List<ClienteResumenDto> findResumenes();
}
