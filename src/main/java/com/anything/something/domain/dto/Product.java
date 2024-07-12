package com.anything.something.domain.dto;

import com.anything.something.domain.dao.Tags;

import java.util.List;

public record Product(String name, String description, Double rate,
                      String image, List<Tags> tags, Discount discount) {}

