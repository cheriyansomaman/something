package com.anything.something.domain.mapper;

import com.anything.something.domain.dao.ProductData;
import com.anything.something.domain.dto.Product;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {
    Product map(ProductData productData);
    ProductData map(Product product);
}
