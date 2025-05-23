package org.example.controller;

import org.example.model.PriceHistory;
import org.example.model.Product;
import org.example.model.ProductDiscount;
import org.example.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    // ! The products differenciate by ID, not by name! If a product has the same
    // name, but a different ID, they will be considered different
    @GetMapping("/dailyShoppingBasketMonitoring")
    public List<Map.Entry<String, Product>> dailyShoppingBasketMonitoring() {
        List<String> productId = new ArrayList<>();
        productId.add("P001");
        productId.add("P002");
        productId.add("P005");
        productId.add("P012");
        productId.add("P038");
        productId.add("P138");

        List<Map.Entry<String, Product>> products = productService.getBestPriceForProducts(productId);

        for (Map.Entry<String, Product> entry : products) {
            System.out.println(entry.getKey() + " -> " + entry.getValue().getProductName() + " ("
                    + entry.getValue().getProductId() + ") -> " + entry.getValue().getPrice() + " per "
                    + entry.getValue().getPackageQuantity() + " " + entry.getValue().getPacakgeUnit());

            // Get a standardized view of the product (in l, kg and in 1 unit)
            Product productTransformed = productService.standardize(entry.getValue());
            System.out.println("standardized product: \n\t" +
                    productTransformed.getProductName() + " ("
                    + productTransformed.getProductId() + ") -> " + productTransformed.getPrice()
                    + " per "
                    + productTransformed.getPackageQuantity() + " " +
                    productTransformed.getPacakgeUnit());

        }

        return products;
    }

    @GetMapping("/customPriceAlert")
    public Map<String, Float> customPriceAlert(
            @RequestParam String productId,
            @RequestParam Float priceLimit) {
        // We give the product id that we want to check and the maximum price of that
        // product;
        // It will be returned pairs of the store name and the price if that store has a
        // lower price then the maximum value provided
        try {
            // The RequestParam does not accept null, this is an extra verification
            if (productId == null || priceLimit == null)
                throw new Exception();
            return productService.searchForRecommendation(productId, priceLimit);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
