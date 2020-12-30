package com.han.startup.model;

import lombok.Data;

import java.util.concurrent.TimeUnit;

@Data
public class PropertyBase {
    String address;
    double sellingPrice;

    TimeUnit rentalPeriodUnit;
    int rentalPeriod;
    double priceForTheRentalPeriod;

    String contact;
}
