package com.anything.something.repository;

import com.anything.something.domain.dao.ProductData;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DemoRepository extends R2dbcRepository<ProductData, UUID> {
}
