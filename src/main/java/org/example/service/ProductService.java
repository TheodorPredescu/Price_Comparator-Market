package org.example.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.example.model.PriceHistory;
import org.example.model.Product;
import org.example.model.ProductDiscount;
import org.example.model.UnitOfMeasure;
import org.example.repository.ProductRepository;
import org.example.util.CsvUtils;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    ProductRepository productRepository = new ProductRepository();

    // --------------------------------------------------------------------------------------
    public List<Product> getDiscountedPricesListBasedOnStore(String store, LocalDate date) {

        if (store == null) {
            System.err.println("Store name is null; cannot continue");
            return new ArrayList<>();
        }

        if (date == null) {
            System.err.println("Date null, getting current date as variable");
            date = LocalDate.now();
        }

        List<Product> productsListWithDiscounts = new ArrayList<>();
        store = store.toLowerCase();
        Map<String, List<Resource>> fullPriceResources = productRepository.getFullPriceResources();

        if (!fullPriceResources.containsKey(store)) {
            System.out.println("Store not found");
            return null;
        }

        for (Resource res : fullPriceResources.get(store)) {
            List<Product> productsList;

            try {
                productsList = productRepository.returnProductsFromSpecificCSV(res.getFilename());
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

        Map<String, Resource> allDiscounts = productRepository.returnLatestResourcesDiscounts();
        List<Map.Entry<String, ProductDiscount>> bestDiscounts = new ArrayList<>();

        for (Map.Entry<String, Resource> entry : allDiscounts.entrySet()) {

            System.out.println(entry.getKey() + " ==> " + entry.getValue().getFilename());

            List<ProductDiscount> allOfInstanceDiscounts = productRepository
                    .returnProductDiscountFromSpecificCSV(entry.getValue().getFilename());

            for (ProductDiscount discount : allOfInstanceDiscounts) {
                bestDiscounts.add(new AbstractMap.SimpleEntry<>(entry.getKey(), discount));
            }
        }

        System.out.println("Size of all: " + bestDiscounts.size());

        return sortByTopProcentage(bestDiscounts, nr_top);
    }

    // Dynamic Price History Graphs:
    // o Provide data points that would allow a frontend to calculate and display
    // price
    // trends over time for individual products.
    // o This data should be filterable by store, product category, or brand.
    // It will not be affected by discounts, it will be only with base prices
    public List<PriceHistory> priceHistoryForGraphs() throws Exception {
        List<PriceHistory> allPricesWithInfo = new ArrayList<>();
        Map<String, List<Resource>> allCsvFiles = productRepository.getFullPriceResources();

        for (Map.Entry<String, List<Resource>> entry : allCsvFiles.entrySet()) {
            for (Resource elem : entry.getValue()) {
                List<Product> prodList = productRepository.returnProductsFromSpecificCSV(elem.getFilename());
                if (prodList.isEmpty())
                    continue;
                for (Product prod : prodList) {
                    allPricesWithInfo.add(new PriceHistory(prod.getPrice(), entry.getKey(), prod.getProductCategory(),
                            prod.getBrand(), CsvUtils.extractDate(elem)));
                }
            }
        }

        return allPricesWithInfo;

    }

    // --------------------------------------------------------------------------------------
    // Daily Shopping Basket Monitoring
    // ! The products differenciate by ID, not by name! If a product has the same
    // name, but a different ID, they will be considered different
    public List<Map.Entry<String, Product>> getBestPriceForProducts(List<String> productIdList) {

        List<Map.Entry<String, Product>> productsFoundList = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();

        for (String productId : productIdList) {

            // Product product;
            Map.Entry<String, Product> storeNameAndProduct = null;

            // We check if we found every product
            try {
                storeNameAndProduct = getBestProductBasedOnIp(productId, currentDate);

                // getBestProductBasedOnIp() will throw null if the product was not found;
                // probably I could have made it cleaner with throw
                if (storeNameAndProduct.getKey() == null)
                    throw new Exception("Product " + productId + " not found.");

            } catch (Exception e) {
                System.out.println("Error at searching for product with id " + productId);
                System.out.println(e.getMessage());
                continue;
            }

            productsFoundList.add(storeNameAndProduct);

        }

        return productsFoundList;
    }

    // --------------------------------------------------------------------------------------
    public List<Map.Entry<String, ProductDiscount>> getNewDiscounts(Period daysAgo) throws Exception {

        Map<String, Resource> allDiscountsNames = productRepository.returnLatestResourcesDiscounts();
        List<Map.Entry<String, ProductDiscount>> bestDiscounts = new ArrayList<>();

        for (Map.Entry<String, Resource> entry : allDiscountsNames.entrySet()) {

            System.out.println(entry.getKey() + " ==> " + entry.getValue().getFilename());

            List<ProductDiscount> allOfInstanceDiscounts = productRepository
                    .returnProductDiscountFromSpecificCSV(entry.getValue().getFilename());

            for (ProductDiscount discount : allOfInstanceDiscounts) {
                bestDiscounts.add(new AbstractMap.SimpleEntry<>(entry.getKey(), discount));
            }
        }

        System.out.println("Size of all: " + bestDiscounts.size());

        return filterByRecentAppearence(bestDiscounts, daysAgo);
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

    private List<Map.Entry<String, ProductDiscount>> filterByRecentAppearence(
            List<Map.Entry<String, ProductDiscount>> discounts, Period daysAgo) {

        LocalDate cutoff = LocalDate.now().minus(daysAgo);
        System.err.println(String.valueOf(cutoff));
        return discounts.stream()
                .filter(entry -> !entry.getValue().getFromDate().isBefore(cutoff))
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
                        System.out.println(
                                "Found discount list for " + name + " of size: " + discountsList.size() + "\n\t"
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
                    prodDisc.getProductName() + " : " + prodDisc.getFromDateString() + " -> "
                            + prodDisc.getToDateString()
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
    // If the product is not found, it will return a null pointer
    private Map.Entry<String, Product> getBestProductBasedOnIp(String id, LocalDate dateBasketWasMade)
            throws Exception {

        Map<String, Resource> productCsvFilesList = productRepository.returnLatestResources();

        String store = null;
        Product bestProductFound = null;

        for (Map.Entry<String, Resource> entry : productCsvFilesList.entrySet()) {
            List<Product> productList = productRepository.returnProductsFromSpecificCSV(entry.getValue().getFilename());

            for (Product product : productList) {

                if (product.getProductId().equals(id)) {

                    // Need to aply discount if it has
                    product = applyDiscountIfPresent(entry.getValue(), product, dateBasketWasMade);

                    if (bestProductFound == null) {
                        bestProductFound = product;
                        store = CsvUtils.extractStoreName(entry.getValue());
                    } else if (product.getPrice() < bestProductFound.getPrice()) {
                        bestProductFound = product;
                        store = CsvUtils.extractStoreName(entry.getValue());
                    }
                    break;
                }
            }
        }

        return new AbstractMap.SimpleEntry<>(store, bestProductFound);
    }

    // --------------------------------------------------------------------------------------
    private Product applyDiscountIfPresent(Resource productCsv, Product product, LocalDate currentDate)
            throws Exception {

        Resource discountRes = searchForDiscountInCsvTitless(productCsv);
        if (discountRes == null)
            return product;

        List<ProductDiscount> productDiscounts = productRepository
                .returnProductDiscountFromSpecificCSV(discountRes.getFilename());

        for (ProductDiscount prodDisc : productDiscounts) {
            if (prodDisc.getProductId().equals(product.getProductId()) &&
                    !prodDisc.getFromDate().isAfter(currentDate) &&
                    !prodDisc.getToDate().isBefore(currentDate)) {

                product.setPrice(product.getPrice() * (100 - prodDisc.getPercentageOfDiscount()) / 100);
            }
        }

        return product;
    }

    private Resource searchForDiscountInCsvTitless(Resource res) throws Exception {

        Map<String, Resource> productDiscountsCsvFilesList = productRepository.returnLatestResourcesDiscounts();

        for (Map.Entry<String, Resource> productDiscountCsv : productDiscountsCsvFilesList.entrySet()) {

            if (CsvUtils.extractDate(productDiscountCsv.getValue()).equals(CsvUtils.extractDate(res)) &&
                    CsvUtils.extractStoreName(productDiscountCsv.getValue()).equals(CsvUtils.extractStoreName(res))) {
                return productDiscountCsv.getValue();
            }
        }
        return null;
    }

    // --------------------------------------------------------------------------------------
    public Product standardize(Product product) {

        Product productCopy = new Product(product);

        try {
            if (UnitOfMeasure.isLiquid(productCopy.getPacakgeUnit())) {

                productCopy
                        .setPrice((productCopy.getPrice() * UnitOfMeasure.getLiquidFactor(productCopy.getPacakgeUnit()))
                                / productCopy.getPackageQuantity());
                productCopy.setPackageUnit(UnitOfMeasure.getLiquidBaseUnit());
            } else if (UnitOfMeasure.isWeight(productCopy.getPacakgeUnit())) {
                productCopy
                        .setPrice((productCopy.getPrice() * UnitOfMeasure.getWeightFactor(productCopy.getPacakgeUnit()))
                                / productCopy.getPackageQuantity());
                productCopy.setPackageUnit(UnitOfMeasure.getWeightBaseUnit());
            } else {
                productCopy.setPrice(productCopy.getPrice() / productCopy.getPackageQuantity());
            }
            productCopy.setPackageQuantityToOne();

            return productCopy;
        } catch (Exception e) {
            System.out.println("Error at getting standard price: " + e.getMessage());
            return null;
        }
    }

    // --------------------------------------------------------------------------------------
    public Map<String, Float> searchForRecommendation(String productId, Float productPriceLimit)
            throws Exception {

        LocalDate date = LocalDate.now();
        // The string is the store name and the float is the price;
        // We can use map because we dont expect for a store to have the same object 2
        // times
        Map<String, Float> productsBelowPriceLimit = new HashMap<>();
        Map<String, Resource> storesCsvData = productRepository.returnLatestResources();

        for (Map.Entry<String, Resource> entry : storesCsvData.entrySet()) {
            List<Product> productsOnStore = getDiscountedPricesListBasedOnStore(entry.getKey(),
                    date);

            for (Product product : productsOnStore) {
                // If we want to check for price per unit or price per l/kg
                // Product productStandard = standardize(product);
                if (product.getProductId().equals(productId) && product.getPrice() <= productPriceLimit)
                    productsBelowPriceLimit.put(entry.getKey(), product.getPrice());
            }
        }

        return productsBelowPriceLimit;
    }
}
