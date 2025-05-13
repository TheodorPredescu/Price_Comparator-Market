package org.example.repository;

import org.example.model.Product;
import org.example.model.ProductDiscount;
import org.example.service.ProductService;
import org.example.util.CsvUtils;
import org.springframework.core.io.Resource;

import java.util.stream.Collectors;
import java.util.Arrays;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Repository;

import org.apache.commons.io.input.BOMInputStream;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

@Repository
public class ProductRepository {

  // Store and the files associated with that store
  private Map<String, List<Resource>> fullPriceResources;
  private Map<String, List<Resource>> discountedPriceResources;

  // ------------------------------------------------------------------------------------------

  public Map<String, List<Resource>> getFullPriceResources() {
    return fullPriceResources;
  }

  public Map<String, List<Resource>> getDiscountedPriceResources() {
    return discountedPriceResources;
  }

  // ------------------------------------------------------------------------------------------

  // ------------------------------------------------------------------------------------------
  public void readResources() throws Exception {

    // Getting the data from resources data and selecting it based on the
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    Resource[] resources = resolver.getResources("classpath:data/*.csv");

    fullPriceResources = Arrays.stream(resources)
        .filter(resource -> {
          String filename = resource.getFilename();
          return filename != null && !filename.toLowerCase().contains("discounts");
        })
        .collect(Collectors.groupingBy(CsvUtils::extractStoreName));

    discountedPriceResources = Arrays.stream(resources)
        .filter(resource -> {
          String filename = resource.getFilename();
          return filename != null && filename.toLowerCase().contains("discounts");
        })
        .collect(Collectors.groupingBy(CsvUtils::extractStoreName));

  }

  // public List<String> getFilenamesByDiscount(boolean isDiscount) throws
  // Exception {
  // PathMatchingResourcePatternResolver resolver = new
  // PathMatchingResourcePatternResolver();
  // Resource[] resources = resolver.getResources("classpath:data/*.csv");
  //
  // return Arrays.stream(resources)
  // .map(Resource::getFilename)
  // .filter(filename -> filename != null &&
  // (isDiscount == filename.toLowerCase().contains("discount")))
  // .collect(Collectors.toList());
  // }

  public List<Product> returnProductFromSpecificCSV(String csv_name) throws Exception {

    String pathToCSV = "data/" + csv_name;
    InputStream input = getClass().getClassLoader().getResourceAsStream(pathToCSV);
    if (input == null)
      throw new RuntimeException("CSV file not found");

    // Wrap in BufferedReader to peek at the first line
    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
    String headerLine = reader.readLine();
    if (headerLine == null)
      throw new RuntimeException("CSV is empty");

    // For UTF-8: it starts with <feff>
    if (headerLine != null && headerLine.startsWith("\uFEFF"))
      headerLine = headerLine.substring(1);

    char separator = headerLine.contains(";") ? ';' : ',';

    // Re-open input stream to re-read from the beginning
    reader.close();
    input = getClass().getClassLoader().getResourceAsStream(pathToCSV);
    if (input == null)
      throw new RuntimeException("CSV file not found");

    CsvToBean<Product> csvToBean = new CsvToBeanBuilder<Product>(new InputStreamReader(input))
        .withType(Product.class)
        .withSeparator(separator)
        .withIgnoreLeadingWhiteSpace(true)
        .withIgnoreEmptyLine(true)
        .build();

    return csvToBean.parse();
  }

  public List<ProductDiscount> returnProductDiscountFromSpecificCSV(String csv_name) throws Exception {
    String pathToCSV = "data/" + csv_name;
    InputStream rawInput = getClass().getClassLoader().getResourceAsStream(pathToCSV);
    if (rawInput == null)
      throw new RuntimeException("CSV file not found");

    BOMInputStream input = new BOMInputStream(rawInput);
    // Wrap in BufferedReader to peek at the first line
    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
    String headerLine = reader.readLine();
    if (headerLine == null)
      throw new RuntimeException("CSV is empty");

    // For UTF-8: it starts with <feff>
    if (headerLine != null && headerLine.startsWith("\uFEFF"))
      headerLine = headerLine.substring(1);

    char separator = headerLine.contains(";") ? ';' : ',';
    String sep = headerLine.contains(";") ? ";" : ",";

    for (String column : headerLine.split(sep)) {

      System.out.println("Column: [" + column + "]");
    }

    // Re-open input stream to re-read from the beginning
    reader.close();
    rawInput = getClass().getClassLoader().getResourceAsStream(pathToCSV);
    if (input == null)
      throw new RuntimeException("CSV file not found");

    input = new BOMInputStream(rawInput);
    CsvToBean<ProductDiscount> csvToBean = new CsvToBeanBuilder<ProductDiscount>(new InputStreamReader(input))
        .withType(ProductDiscount.class)
        .withSeparator(separator)
        .withIgnoreLeadingWhiteSpace(true)
        .withIgnoreEmptyLine(true)
        .build();

    return csvToBean.parse();
  }
}
