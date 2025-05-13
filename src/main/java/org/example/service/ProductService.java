package org.example.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.example.model.Product;
import org.example.model.ProductDiscount;
import org.example.repository.ProductRepository;
import org.example.util.CsvUtils;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

  ProductRepository productRepository = new ProductRepository();

  // --------------------------------------------------------------------------------------
  public List<Product> getDiscountListBasedOnStore(String store, LocalDate date) throws Exception {

    List<Product> productsList = new ArrayList<>();

    store = store.toLowerCase();
    productRepository.readResources();

    Map<String, List<Resource>> fullPriceResources = productRepository.getFullPriceResources();

    if (!fullPriceResources.containsKey(store)) {
      System.out.println("Store not found");
    }

    for (Resource res : fullPriceResources.get(store)) {

      // Might throw exception
      productsList = productRepository.returnProductFromSpecificCSV(res.getFilename());
      List<ProductDiscount> prodDisc = getDiscountsBasedOnStoreCsvFormat(res);
      System.err.println();
      System.err.println("Now getting info from " + res.getFilename());
      System.err.println();

      List<Product> productsListWithDiscounts = combinePricesWithDiscounts(productsList, prodDisc, date);

      System.err.println();
      System.err.println();
      for (Product product : productsListWithDiscounts) {
        System.out.println(product.getProductId() + ":" + product.getProductName() + " -> " + product.getPrice());
      }
    }

    return null;
  }

  // --------------------------------------------------------------------------------------
  private List<ProductDiscount> getDiscountsBasedOnStoreCsvFormat(Resource res) {

    List<ProductDiscount> discountsList = new ArrayList<>();

    String name = res.getFilename();
    String store_name = CsvUtils.extractStoreName(res);
    LocalDate dateCsvWasTaken = CsvUtils.extractDate(res);
    Map<String, List<Resource>> discountedPriceResources = productRepository.getDiscountedPriceResources();

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

  private ProductDiscount searchProductInDiscountsList(Product product, List<ProductDiscount> productDiscounts) {

    String prodId = product.getProductId();
    for (ProductDiscount prodDisc : productDiscounts) {
      if (prodId.equals(prodDisc.getProductId()))
        return prodDisc;
    }

    return null;
  }

  // --------------------------------------------------------------------------------------
  // --------------------------------------------------------------------------------------
}
