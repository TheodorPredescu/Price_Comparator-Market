package org.example.model;

import com.opencsv.bean.CsvBindByName;

public class Product {

  @CsvBindByName(column = "product_id")
  private String productId;

  @CsvBindByName(column = "product_name")
  private String productName;

  @CsvBindByName(column = "product_category")
  private String productCategory;

  @CsvBindByName(column = "brand")
  private String brand;

  @CsvBindByName(column = "package_quantity")
  private Float packageQuantity;

  @CsvBindByName(column = "package_unit")
  private String packageUnit;

  @CsvBindByName(column = "price")
  private Float price;

  @CsvBindByName(column = "currency")
  private String currency;

  // ------------------------------------------------------------------------------------

  public Product() {
  }

  public Product(String id, String name, String category, String brand, Float quantity, String pac_unit, Float price,
      String currency) {

    this.productId = id;
    this.productName = name;
    this.productCategory = category;
    this.brand = brand;
    this.packageQuantity = quantity;
    this.packageUnit = pac_unit;
    this.price = price;
    this.currency = currency;
  }

  // ------------------------------------------------------------------------------------

  public String getCurrency() {
    return currency;
  }

  public Float getPrice() {
    return price;
  }

  public String getPacakgeUnit() {
    return packageUnit;
  }

  public Float getPackageQuantity() {
    return packageQuantity;
  }

  public String getBrand() {
    return brand;
  }

  public String getProductCategory() {
    return productCategory;
  }

  public String getProductName() {
    return productName;
  }

  public String getProductId() {
    return productId;
  }

  public void setPrice(Float price) {
    this.price = price;
  }

}
