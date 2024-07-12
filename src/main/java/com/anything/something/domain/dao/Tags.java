package com.anything.something.domain.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table
public class Tags {
    @Id
    private UUID id;
    private String name;
}
