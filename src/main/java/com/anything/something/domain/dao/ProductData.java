package com.anything.something.domain.dao;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@Table(name = "product")
public class ProductData {
    @Id
    private UUID id;

    private String name;
    private String description;
    private String image;
    private Double rate;
    private String currency;
    private List<Tags> tags;
}
