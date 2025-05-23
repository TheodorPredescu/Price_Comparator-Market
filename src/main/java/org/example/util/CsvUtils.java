package org.example.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.model.Product;
import org.example.model.ProductDiscount;
import org.example.repository.ProductRepository;
import org.springframework.core.io.Resource;

public class CsvUtils {

    private static final Map<String, Float> liquidUnitsToLiter = new HashMap<>();
    private static final Map<String, Float> weightUnitsToKg = new HashMap<>();

    static {
        weightUnitsToKg.put("kg", 1.0f); // kilogram
        weightUnitsToKg.put("g", 0.001f); // gram
        weightUnitsToKg.put("mg", 0.000001f); // milligram
        weightUnitsToKg.put("lb", 0.453592f); // pound (avoirdupois)
        weightUnitsToKg.put("oz", 0.0283495f); // ounce
    }
    static {
        liquidUnitsToLiter.put("l", 1.0f); // liter
        liquidUnitsToLiter.put("ml", 0.001f); // milliliter
        liquidUnitsToLiter.put("cl", 0.01f); // centiliter
        liquidUnitsToLiter.put("dl", 0.1f); // deciliter
        liquidUnitsToLiter.put("fl oz", 0.0295735f);// US fluid ounce
        liquidUnitsToLiter.put("pt", 0.473176f); // US pint
        liquidUnitsToLiter.put("qt", 0.946353f); // US quart
        liquidUnitsToLiter.put("gal", 3.78541f); // US gallon
    }

    // ---------------------------------------------------------------------------------------
    public static LocalDate extractDate(Resource res) {
        String filename = res.getFilename();
        if (filename == null || !filename.contains("_"))
            return null;

        int lastUnderscore = filename.lastIndexOf("_");
        int firstDot = filename.indexOf(".");

        if (lastUnderscore < 0 || firstDot < 0)
            return null;
        return LocalDate.parse(filename.substring(lastUnderscore + 1, firstDot));
    }

    public static String extractStoreName(Resource res) {
        String filename = res.getFilename();
        if (filename == null || !filename.contains("_"))
            return null;
        return filename.substring(0, filename.indexOf('_')).toLowerCase();
    }

    public static LocalDate transformFromStringToLocalDate(String dateString) {

        if (dateString == null)
            return null;
        dateString = dateString.trim();

        DateTimeFormatter[] formatters = new DateTimeFormatter[] {
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                // DateTimeFormatter.ofPattern("MM-dd-yy"),
                new DateTimeFormatterBuilder()
                        .appendPattern("MM-dd-")
                        .appendValueReduced(ChronoField.YEAR, 2, 2, 2000)
                        .toFormatter()
                        .withResolverStyle(ResolverStyle.STRICT),
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateString, formatter);
            } catch (Exception e) {
            }
        }
        System.err.println("Error in transformStringToLocalDate() : " + dateString);
        System.err.println();
        return null;
    }

    // ---------------------------------------------------------------------------------------
    public void printResources(ProductRepository productRepository) {

        for (Map.Entry<String, List<Resource>> entry : productRepository.getFullPriceResources().entrySet()) {
            System.out.println(entry.getKey() + ":");
            for (Resource res : entry.getValue()) {
                System.out.println(res.getFilename());
            }
        }

        System.out.println();
        System.out.println();

        for (Map.Entry<String, List<Resource>> entry : productRepository.getDiscountsPriceResources().entrySet()) {
            System.out.println(entry.getKey() + ":");
            for (Resource res : entry.getValue()) {
                System.out.println(res.getFilename());
            }
        }
    }

    public static void printOneProduct(Product prod) {
        System.err.println();
        System.out.println("id " + prod.getProductId());
        System.out.println("Brand " + prod.getBrand());
        System.out.println("ProductCategory " + prod.getProductCategory());
        System.out.println("Name " + prod.getProductName());
        System.out.println("PacakgeUnit " + prod.getPacakgeUnit());
        System.out.println("PackageQuantity " + prod.getPackageQuantity());
        System.out.println("Price " + prod.getPrice());
        System.out.println("Currency " + prod.getCurrency());
        System.err.println();
    }

    public static void printOneProductDiscount(ProductDiscount prod) {
        System.err.println();
        System.out.println("ID: " + prod.getProductId());
        System.out.println("Name: " + prod.getProductName());
        System.out.println("Brand: " + prod.getBrand());
        System.out.println("Product Category: " + prod.getCategory());
        System.out.println("Package Quantity: " + prod.getpackageQuantity());
        System.out.println("Package Unit: " + prod.getpackageUnit());
        System.out.println("From Date (string): " + prod.getFromDateString());
        System.out.println("To Date (string): " + prod.getToDateString());
        System.out.println("From Date (parsed): " + prod.getFromDate());
        System.out.println("To Date (parsed): " + prod.getToDate());
        System.out.println("Discount (%): " + prod.getPercentageOfDiscount());
        System.err.println();
    }
}
