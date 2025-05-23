package org.example.model;

import java.util.Map;

public final class UnitOfMeasure {

    public static final Map<String, Float> liquidUnitsToLiter = Map.of(
            "ml", 0.001f,
            "cl", 0.01f,
            "dl", 0.1f,
            "l", 1.0f,
            "fl oz", 0.0295735f,
            "pt", 0.473176f,
            "qt", 0.946353f,
            "gal", 3.78541f);

    public static final Map<String, Float> weightUnitsToKg = Map.of(
            "mg", 0.000001f,
            "g", 0.001f,
            "kg", 1.0f,
            "oz", 0.0283495f,
            "lb", 0.453592f);

    public static Float getLiquidFactor(String symbol) {
        return liquidUnitsToLiter.get(symbol.toLowerCase());
    }

    public static Float getWeightFactor(String symbol) {
        return weightUnitsToKg.get(symbol.toLowerCase());
    }

    public static Boolean isLiquid(String symbol) {
        return liquidUnitsToLiter.containsKey(symbol);
    }

    public static Boolean isWeight(String symbol) {
        return weightUnitsToKg.containsKey(symbol);
    }

    public static String getLiquidBaseUnit() {
        return "l";
    }

    public static String getWeightBaseUnit() {
        return "kg";
    }
}
