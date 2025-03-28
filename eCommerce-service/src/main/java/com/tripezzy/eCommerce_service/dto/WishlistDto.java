package com.tripezzy.eCommerce_service.dto;


import java.util.List;

public class WishlistDto {

    private Long userId;
    private List<ProductDto> productDtos;

    public WishlistDto() {
    }

    public WishlistDto(Long userId, List<ProductDto> productDtos) {
        this.userId = userId;
        this.productDtos = productDtos;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<ProductDto> getProductDtos() {
        return productDtos;
    }

    public void setProductDtos(List<ProductDto> productDtos) {
        this.productDtos = productDtos;
    }
}
