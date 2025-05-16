package org.example.controller;

import org.example.model.Product;
import org.example.model.ProductDiscount;
import org.example.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
public class ProductController {

  ProductService productService = new ProductService();

  @GetMapping("/getBestDiscounts")
  public void bestDiscounts() {

    try {
      // The string is the store name
      // Those are based only by the latest data (data that is in the older csv-s is
      // considered expired)
      List<Map.Entry<String, ProductDiscount>> topDiscounts = productService.getBestDiscounts(20);

      for (Map.Entry<String, ProductDiscount> elem : topDiscounts) {
        System.out.println("Store: " + elem.getKey() + "\n\tProduct: " + elem.getValue().getProductName() + " -> "
            + elem.getValue().getPercentageOfDiscount());
      }
    } catch (Exception ex) {
      System.err.println("Error: " + ex.getMessage());
    }
  }

  @GetMapping("/")
  public void newDiscounts() {

    LocalDate date = LocalDate.of(2020, 5, 13);

    List<Product> ceva = productService.getDiscountedPricesListBasedOnStore("kauflant", date);
    try {

    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
    }
  }
}
