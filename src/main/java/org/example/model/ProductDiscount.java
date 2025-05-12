package org.example.model;

import java.time.LocalDate;

import org.springframework.cglib.core.Local;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import org.example.util.CsvUtils;

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
  private String fromDate_string;

  @CsvBindByName(column = "to_date")
  private String toDate_string;

  @CsvBindByName(column = "percentage_of_discount")
  private Float percentage_of_discount;

  private LocalDate fromDate_date = null;

  private LocalDate toDate_date = null;

  public ProductDiscount() {
  }

  public ProductDiscount(String id, String name, String brand, Float quantity, String unit, String category,
      String from,
      String to, Float discount) {

    this.productId = id;
    this.productName = name;
    this.brand = brand;
    this.packageQuantity = quantity;
    this.packageUnit = unit;
    this.productCategory = category;
    this.fromDate_string = from;
    this.toDate_string = to;
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

  public String getFromDateString() {
    return fromDate_string;
  }

  public String getToDateString() {
    return toDate_string;
  }

  public Float getPercentageOfDiscount() {
    return percentage_of_discount;
  }

  public LocalDate getFromDate() {
    if (fromDate_date == null)
      fromDate_date = CsvUtils.transformFromStringToLocalDate(this.fromDate_string);
    return fromDate_date;
  }

  public LocalDate getToDate() {
    if (toDate_date == null)
      toDate_date = CsvUtils.transformFromStringToLocalDate(this.toDate_string);
    return toDate_date;
  }
}
