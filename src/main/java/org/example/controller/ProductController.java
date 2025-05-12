package org.example.controller;

import org.example.model.Product;
import org.example.service.ProductService;
import org.example.repository.ProductRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
public class ProductController {

  @GetMapping("/products")
  public void loadProducts() {
    ProductService productService = new ProductService();

    String name = "kaufland_2025-05-08.csv";
    LocalDate date = LocalDate.of(2025, 5, 10);

    try {
      productService.getDiscountListBasedOnStore("kaufland", date);

      // productService.returnProductFromSpecificCSV(name);
      // for (Product product : products) {
      // System.out.println(product.getProductName() + "->" + product.getPrice() +
      // " "
      // + product.getCurrency());
      // }
    } catch (Exception ex) {
      System.out.println("Not working");
      throw new RuntimeException(ex);
    }
  }
}
