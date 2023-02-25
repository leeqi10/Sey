package com.southwind.vo;

import lombok.Data;

@Data
public class OrderDetailVO {
    private Integer id;
    private Integer quantity;
    private String name;
    private Float price;
    private String fileName;
    private Float cost;
}
