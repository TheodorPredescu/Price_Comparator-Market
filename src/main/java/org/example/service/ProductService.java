package org.example.service;

import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.example.model.Product;
import org.example.model.ProductDiscount;
import org.example.repository.ProductRepository;
import org.example.util.CsvUtils;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import ch.qos.logback.core.joran.sanity.Pair;

@Service
public class ProductService {

  ProductRepository productRepository = new ProductRepository();

  // --------------------------------------------------------------------------------------
  public List<Product> getDiscountedPricesListBasedOnStore(String store, LocalDate date) {

    List<Product> productsListWithDiscounts = new ArrayList<>();

    if (store == null) {
      System.err.println("Store name is null");
      return productsListWithDiscounts;
    }

    store = store.toLowerCase();

    try {
      productRepository.readResources();
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      return new ArrayList<>();
    }

    Map<String, List<Resource>> fullPriceResources = productRepository.getFullPriceResources();

    if (!fullPriceResources.containsKey(store)) {
      System.out.println("Store not found");
      return null;
    }

    for (Resource res : fullPriceResources.get(store)) {
      List<Product> productsList;

      try {
        productsList = productRepository.returnProductFromSpecificCSV(res.getFilename());
      } catch (Exception e) {
        System.err.println("Error at reading file: " + res.getFilename() + " ->" + e.getMessage());
        continue;
      }

      List<ProductDiscount> prodDisc = getDiscountsBasedOnStoreCsvFormat(res);

      if (prodDisc.isEmpty())
        continue;

      productsListWithDiscounts.addAll(combinePricesWithDiscounts(productsList, prodDisc, date));
    }

    for (Product product : productsListWithDiscounts) {
      System.out.println(product.getProductId() + ":" + product.getProductName() + " -> " + product.getPrice());
    }

    return productsListWithDiscounts;
  }

  // --------------------------------------------------------------------------------------
  public List<Map.Entry<String, ProductDiscount>> getBestDiscounts(int nr_top) throws Exception {

    Map<String, Resource> allDiscounts = productRepository.returnLatestResources();
    // Map<String, ProductDiscount> bestDiscounts = new LinkedHashMap<>();
    List<Map.Entry<String, ProductDiscount>> bestDiscounts = new ArrayList<>();

    for (Map.Entry<String, Resource> entry : allDiscounts.entrySet()) {

      System.out.println(entry.getKey() + " ==> " + entry.getValue().getFilename());

      List<ProductDiscount> allOfInstanceDiscounts = productRepository
          .returnProductDiscountFromSpecificCSV(entry.getValue().getFilename());

      for (ProductDiscount discount : allOfInstanceDiscounts) {
        // bestDiscounts.put(entry.getKey(), discount);
        bestDiscounts.add(new AbstractMap.SimpleEntry<>(entry.getKey(), discount));
      }
    }

    System.out.println("Size of all: " + bestDiscounts.size());

    return sortByTopProcentage(bestDiscounts, nr_top);
  }

  // --------------------------------------------------------------------------------------
  // TODO: Get the best price for a specific productID
  public Pair<String, Product> getBestPriceForProduct(String productID) {

    return null;
  }

  // --------------------------------------------------------------------------------------
  public void getNewDiscounts() {

  }

  // --------------------------------------------------------------------------------------
  private List<Map.Entry<String, ProductDiscount>> sortByTopProcentage(
      List<Map.Entry<String, ProductDiscount>> discounts,
      int nr_top) {

    return discounts.stream()
        .sorted((e1, e2) -> Float.compare(
            e2.getValue().getPercentageOfDiscount(),
            e1.getValue().getPercentageOfDiscount()))
        .limit(nr_top)
        .collect(Collectors.toList());
  }

  // --------------------------------------------------------------------------------------
  private List<ProductDiscount> getDiscountsBasedOnStoreCsvFormat(Resource res) {

    List<ProductDiscount> discountsList = new ArrayList<>();

    String name = res.getFilename();
    String store_name = CsvUtils.extractStoreName(res);
    LocalDate dateCsvWasTaken = CsvUtils.extractDate(res);
    Map<String, List<Resource>> discountedPriceResources = productRepository.getDiscountsPriceResources();

    System.err.println();
    System.err.println();
    System.out.println("Doing file:" + name);

    try {

      // If there are any discounts, we are adding them now
      if (discountedPriceResources.containsKey(store_name)) {
        List<Resource> discountsForSpecificStore = discountedPriceResources.get(store_name);

        for (Resource resDisc : discountsForSpecificStore) {

          if (dateCsvWasTaken.equals(CsvUtils.extractDate(resDisc))) {

            discountsList = productRepository.returnProductDiscountFromSpecificCSV(resDisc.getFilename());
            System.out.println("Found discount list for " + name + " of size: " + discountsList.size() + "\n\t"
                + resDisc.getFilename());
            break;
          }
        }
      } else {
        System.out.println("No discount list found for the name " + store_name);
        return new ArrayList<>();
      }

    } catch (Exception e) {
      System.err.println("Error at reading csv:" + name + e.getMessage());
    }

    return discountsList;
  }

  // --------------------------------------------------------------------------------------
  private List<Product> combinePricesWithDiscounts(List<Product> productsListOriginal,
      List<ProductDiscount> productDiscountsList, LocalDate date) {

    List<Product> productsList = new ArrayList<>(productsListOriginal);

    System.out.println("Size of the copy: " + productsList.size());
    System.out.println("Size of discounts list: " + productDiscountsList.size());

    for (Product prod : productsList) {
      // CsvUtils.printOneProduct(prod);
      ProductDiscount prodDisc = searchProductInDiscountsList(prod, productDiscountsList);

      if (prodDisc == null)
        continue;

      System.out.println(
          prodDisc.getProductName() + " : " + prodDisc.getFromDateString() + " -> " + prodDisc.getToDateString()
              + " percentage: " + prodDisc.getPercentageOfDiscount());

      if (date.isBefore(prodDisc.getFromDate()) || date.isAfter(prodDisc.getToDate()))
        continue;

      prod.setPrice(prod.getPrice() * (100 - prodDisc.getPercentageOfDiscount()) / 100);
      System.out.println("Discount applied to product " + prod.getProductName() +
          " in value of "
          + prodDisc.getPercentageOfDiscount() + "%.");
    }

    return productsList;
  }

  // --------------------------------------------------------------------------------------
  private ProductDiscount searchProductInDiscountsList(Product product, List<ProductDiscount> productDiscounts) {

    String prodId = product.getProductId();
    for (ProductDiscount prodDisc : productDiscounts) {
      if (prodId.equals(prodDisc.getProductId()))
        return prodDisc;
    }

    return null;
  }

  // --------------------------------------------------------------------------------------
}
