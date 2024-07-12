package com.anything.something.domain.dto;

import java.util.List;

public record Discount(Factor factor, List<Rate> rates) {
}
