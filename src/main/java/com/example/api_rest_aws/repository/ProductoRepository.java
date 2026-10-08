package com.example.api_rest_aws.repository;

import com.example.api_rest_aws.entity.Producto;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    boolean existsByMarca_IdMarca(Long idMarca);

    /**
     * Bloqueo pesimista para evitar condiciones de carrera (race conditions)
     * al consultar y descontar el stock en ventas concurrentes.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.idProducto = :id")
    Optional<Producto> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.idProducto IN :ids ORDER BY p.idProducto ASC")
    List<Producto> findAllByIdInForUpdate(@Param("ids") Collection<Long> ids);
}
