package com.example.api_rest_aws.repository;

import com.example.api_rest_aws.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    boolean existsByProducto_IdProducto(Long idProducto);
}
