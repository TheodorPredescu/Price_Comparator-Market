package org.example.util;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.example.repository.ProductRepository;
import org.springframework.core.io.Resource;

public class CsvUtils {

  public static LocalDate extractDate(Resource res) {
    String filename = res.getFilename();
    if (filename == null || !filename.contains("_"))
      return null;

    int lastUnderscore = filename.lastIndexOf("_");
    int firstDot = filename.indexOf(".");

    if (lastUnderscore < 0 || firstDot < 0)
      return null;
    return LocalDate.parse(filename.substring(lastUnderscore + 1, firstDot));
  }

  public static String extractStoreName(Resource res) {
    String filename = res.getFilename();
    if (filename == null || !filename.contains("_"))
      return "unknown";
    return filename.substring(0, filename.indexOf('_')).toLowerCase();
  }

  public void printResources(ProductRepository productRepository) {

    for (Map.Entry<String, List<Resource>> entry : productRepository.getFullPriceResources().entrySet()) {
      System.out.println(entry.getKey() + ":");
      for (Resource res : entry.getValue()) {
        System.out.println(res.getFilename());
      }
    }

    System.out.println();
    System.out.println();

    for (Map.Entry<String, List<Resource>> entry : productRepository.getDiscountedPriceResources().entrySet()) {
      System.out.println(entry.getKey() + ":");
      for (Resource res : entry.getValue()) {
        System.out.println(res.getFilename());
      }
    }
  }
}
