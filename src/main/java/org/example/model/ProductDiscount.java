package org.example.model;

import java.time.LocalDate;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;

public class ProductDiscount {

  @CsvBindByName(column = "product_id")
  private String productId;

  @CsvBindByName(column = "product_name")
  private String productName;

  @CsvBindByName(column = "brand")
  private String brand;

  @CsvBindByName(column = "package_quantity")
  private Float packageQuantity;

  @CsvBindByName(column = "package_unit")
  private String packageUnit;

  @CsvBindByName(column = "product_category")
  private String productCategory;

  @CsvBindByName(column = "from_date")
  @CsvDate("MM/dd/yyyy")
  private LocalDate fromDate;

  @CsvBindByName(column = "to_date")
  @CsvDate("MM/dd/yyyy")
  private LocalDate toDate;

  @CsvBindByName(column = "percentage_of_discount")
  private Float percentage_of_discount;

  public ProductDiscount() {
  }

  public ProductDiscount(String id, String name, String brand, Float quantity, String unit, String category,
      LocalDate from,
      LocalDate to, Float discount) {

    this.productId = id;
    this.productName = name;
    this.brand = brand;
    this.packageQuantity = quantity;
    this.packageUnit = unit;
    this.productCategory = category;
    this.fromDate = from;
    this.toDate = to;
    this.percentage_of_discount = discount;
  }

  public String getproductId() {
    return productId;
  }

  public String getProductName() {
    return productName;
  }

  public String getBrand() {
    return brand;
  }

  public Float getpackageQuantity() {
    return packageQuantity;
  }

  public String getpackageUnit() {
    return packageUnit;
  }

  public String getCategory() {
    return productCategory;
  }

  public LocalDate getFromDate() {
    return fromDate;
  }

  public LocalDate getToDate() {
    return toDate;
  }

  public Float getPercentageOfDiscount() {
    return percentage_of_discount;
  }
}
