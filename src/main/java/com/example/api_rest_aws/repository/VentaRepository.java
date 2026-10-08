package com.example.api_rest_aws.repository;

import com.example.api_rest_aws.entity.Venta;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    boolean existsByCliente_IdCliente(Long idCliente);

    @Query("SELECT DISTINCT v FROM Venta v LEFT JOIN FETCH v.cliente LEFT JOIN FETCH v.detalles d LEFT JOIN FETCH d.producto WHERE v.idVenta = :id")
    Optional<Venta> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT v FROM Venta v LEFT JOIN FETCH v.cliente LEFT JOIN FETCH v.detalles d LEFT JOIN FETCH d.producto ORDER BY v.idVenta DESC")
    List<Venta> findAllWithDetails();
}
