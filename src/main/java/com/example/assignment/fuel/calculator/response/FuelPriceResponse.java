package com.example.assignment.fuel.calculator.response;

import lombok.Data;

@Data
public class FuelPriceResponse {
    String townname;
    String petrol;
    String diesel;
    String ismetro;
}
