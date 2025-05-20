package org.example.controller;

import org.example.model.PriceHistory;
import org.example.model.Product;
import org.example.model.ProductDiscount;
import org.example.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@RestController
public class ProductController {

    ProductService productService = new ProductService();

    @GetMapping("/getBestDiscounts")
    public List<Map.Entry<String, ProductDiscount>> bestDiscounts() {

        List<Map.Entry<String, ProductDiscount>> topDiscounts = null;
        try {
            // The string is the store name
            // Those are based only by the latest data (data that is in the older csv-s is
            // considered expired)
            topDiscounts = productService.getBestDiscounts(20);

            for (Map.Entry<String, ProductDiscount> elem : topDiscounts) {
                System.out
                        .println("Store: " + elem.getKey() + "\n\tProduct: " + elem.getValue().getProductName() + " -> "
                                + elem.getValue().getPercentageOfDiscount());
            }
        } catch (Exception ex) {
            System.err.println("Error: " + ex.getMessage());
        }

        return topDiscounts;
    }

    @GetMapping("/getNewDiscounts")
    public List<Map.Entry<String, ProductDiscount>> newDiscounts() {

        try (Scanner scanner = new Scanner(System.in)) {

            System.out.println("How many days ago?");
            Integer var = scanner.nextInt();
            System.out.println(var);
            Period now = Period.ofDays(var);
            List<Map.Entry<String, ProductDiscount>> topDiscounts = productService.getNewDiscounts(now);

            for (Map.Entry<String, ProductDiscount> elem : topDiscounts) {
                System.out
                        .println("Store: " + elem.getKey() + "\n\tProduct: " + elem.getValue().getProductName() + " -> "
                                + elem.getValue().getPercentageOfDiscount() + "\n\tfrom: "
                                + elem.getValue().getFromDateString() + "; to: "
                                + elem.getValue().getToDateString());
            }
            return topDiscounts;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        return null;
    }

    @GetMapping("/priceHistory")
    public List<PriceHistory> priceHistory() {

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            return productService.priceHistoryForGraphs();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }
    }

    // We give a list of product IDs and we return the best product and the store
    // (the value of the product is calculated with discount if it appears in the
    // latest discount list of that store)
    @GetMapping("/dailyShoppingBasketMonitoring")
    public List<Map.Entry<String, Product>> dailyShoppingBasketMonitoring() {
        List<String> productId = new ArrayList<>();
        productId.add("r001");
        productId.add("P002");
        productId.add("P003");
        productId.add("P004");
        productId.add("P005");

        List<Map.Entry<String, Product>> products = productService.getBestPriceForProducts(productId);

        for (Map.Entry<String, Product> entry : products) {
            System.out.println(entry.getKey() + " -> " + entry.getValue().getProductName() + " ("
                    + entry.getValue().getProductId() + ") -> " + entry.getValue().getPrice());
        }

        return products;
    }
}
