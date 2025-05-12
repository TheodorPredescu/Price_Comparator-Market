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

      System.out.println();
      System.out.println(prodDisc.size());
      System.out.println();

      List<Product> productsListWithDiscounts = combinePricesWithDiscounts(productsList, prodDisc, date);

      for (Product product : productsListWithDiscounts) {
        System.out.println(product.getProductName() + " -> " + product.getPrice());
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

    System.out.println("Doing file:" + name);

    try {

      // If there are any discounts, we are adding them now
      if (discountedPriceResources.containsKey(store_name)) {
        List<Resource> discountsForSpecificStore = discountedPriceResources.get(store_name);

        System.out.println("date mother: " + dateCsvWasTaken);

        for (Resource resDisc : discountsForSpecificStore) {

          System.out.println("List element: " + resDisc.getFilename());
          System.out.println("date son: " + CsvUtils.extractDate(resDisc));
          if (dateCsvWasTaken.equals(CsvUtils.extractDate(resDisc))) {

            System.out.println("???????????");
            discountsList = productRepository.returnProductDiscountFromSpecificCSV(resDisc.getFilename());
            System.out.println("Fount discount list for " + store_name + " of size: " + discountsList.size());
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

    for (Product prod : productsList) {
      ProductDiscount prodDisc = searchProductInDiscountsList(prod, productDiscountsList);

      // System.out.println();
      // System.out.println("Works??");
      // System.out.println();

      if (prodDisc == null) {
        // System.out.println();
        // System.out.println("No");
        // System.out.println();
        continue;
      }
      System.out
          .println("Product: " + prodDisc.getProductName() + ", percentage: " + prodDisc.getPercentageOfDiscount());

      if (prodDisc.getFromDate() == null)
        System.out.println("!!!!!!!!!!");
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
      String fromDate = prodDisc.getFromDate().format(formatter);
      String toDate = prodDisc.getToDate().format(formatter);
      System.out.println("From : " + fromDate + " to : " + toDate);
      if (!(prodDisc.getFromDate().isBefore(date) && prodDisc.getToDate().isAfter(date)))
        continue;

      prod.setPrice(prod.getPrice() * prodDisc.getPercentageOfDiscount() / 100);
      System.out.println("Discount applied to product " + prod.getProductName() + "in value of "
          + prodDisc.getPercentageOfDiscount() + "%.");
    }

    return productsList;
  }

  private ProductDiscount searchProductInDiscountsList(Product product, List<ProductDiscount> productDiscounts) {

    String prodId = product.getProductId();
    for (ProductDiscount prodDisc : productDiscounts) {
      if (prodId.equals(prodDisc.getproductId()))
        return prodDisc;
    }

    return null;
  }

  // --------------------------------------------------------------------------------------
  // --------------------------------------------------------------------------------------
}
