package org.example.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Map;

import org.example.model.Product;
import org.example.model.ProductDiscount;
import org.example.repository.ProductRepository;
import org.springframework.core.io.Resource;

public class CsvUtils {

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
      return "unknown";
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

  public void printResources(ProductRepository productRepository) {

    for (Map.Entry<String, List<Resource>> entry : productRepository.getFullPriceResources().entrySet()) {
      System.out.println(entry.getKey() + ":");
      for (Resource res : entry.getValue()) {
        System.out.println(res.getFilename());
      }
    }

    System.out.println();
    System.out.println();

    for (Map.Entry<String, List<Resource>> entry : productRepository.getDiscountedPriceResources().entrySet()) {
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
