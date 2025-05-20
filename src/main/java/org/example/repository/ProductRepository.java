package org.example.repository;

import org.example.model.Product;
import org.example.model.ProductDiscount;
import org.example.util.CsvUtils;
import org.springframework.core.io.Resource;

import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.HashMap;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
    private Map<String, List<Resource>> discountsPriceResources;

    // ------------------------------------------------------------------------------------------
    public Map<String, List<Resource>> getFullPriceResources() {
        try {
            readResources();
        } catch (Exception e) {
            System.err.println("Error at reading resources: " + e.getMessage());
        }
        return fullPriceResources;
    }

    public Map<String, List<Resource>> getDiscountsPriceResources() {
        try {
            readResources();
        } catch (Exception e) {
            System.err.println("Error at reading resources: " + e.getMessage());
        }
        return discountsPriceResources;
    }

    // ------------------------------------------------------------------------------------------

    // I need to run this before I call anything else, because here I set
    // fullPriceResources and discountedPriceResources
    private void readResources() throws IOException {

        // Getting the data from resources data and selecting it based on the
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:data/*.csv");

        fullPriceResources = Arrays.stream(resources)
                .filter(resource -> {
                    String filename = resource.getFilename();
                    return filename != null && !filename.toLowerCase().contains("discounts");
                })
                .collect(Collectors.groupingBy(CsvUtils::extractStoreName));

        discountsPriceResources = Arrays.stream(resources)
                .filter(resource -> {
                    String filename = resource.getFilename();
                    return filename != null && filename.toLowerCase().contains("discounts");
                })
                .collect(Collectors.groupingBy(CsvUtils::extractStoreName));

    }

    // The current discounts are just in the latest discounts list (discounts that
    // are in older csv-s, even though the period has not expired, will not be
    // valid; only the latest discount csv-s are aplicable)
    public Map<String, Resource> returnLatestResourcesDiscounts() throws IOException {

        Map<String, Resource> latestResources = new HashMap<>();

        readResources();

        if (discountsPriceResources.isEmpty())
            throw new IOException("No files found and read.");

        for (Map.Entry<String, List<Resource>> entry : discountsPriceResources.entrySet()) {
            Resource latest = entry.getValue().get(0);

            for (Resource instance : entry.getValue()) {
                if (CsvUtils.extractDate(latest).isBefore(CsvUtils.extractDate(instance))) {
                    latest = instance;
                }
            }

            latestResources.put(entry.getKey(), latest);
        }

        return latestResources;
    }

    // ------------------------------------------------------------------------------------------
    public Map<String, Resource> returnLatestResources() throws IOException {

        Map<String, Resource> latestResources = new HashMap<>();

        readResources();

        if (fullPriceResources.isEmpty())
            throw new IOException("No files found and read.");

        for (Map.Entry<String, List<Resource>> entry : fullPriceResources.entrySet()) {
            Resource latest = entry.getValue().get(0);

            for (Resource instance : entry.getValue()) {
                if (CsvUtils.extractDate(latest).isBefore(CsvUtils.extractDate(instance))) {
                    latest = instance;
                }
            }

            latestResources.put(entry.getKey(), latest);
        }

        return latestResources;
    }

    // ------------------------------------------------------------------------------------------
    public List<Product> returnProductsFromSpecificCSV(String csv_name) throws Exception {

        readResources();

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

    // ------------------------------------------------------------------------------------------
    public List<ProductDiscount> returnProductDiscountFromSpecificCSV(String csv_name) throws Exception {

        readResources();

        String pathToCSV = "data/" + csv_name;
        InputStream rawInput = getClass().getClassLoader().getResourceAsStream(pathToCSV);
        if (rawInput == null)
            throw new RuntimeException("CSV file not found");

        BOMInputStream input = new BOMInputStream(rawInput);
        // Wrap in BufferedReader to peek at the first line
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String headerLine = reader.readLine();
        if (headerLine == null) {
            reader.close();
            throw new RuntimeException("CSV is empty");
        }

        // For UTF-8: it starts with <feff>
        if (headerLine != null && headerLine.startsWith("\uFEFF"))
            headerLine = headerLine.substring(1);

        // See if its a csv with ; or ,
        char separator = headerLine.contains(";") ? ';' : ',';
        String sep = headerLine.contains(";") ? ";" : ",";

        // for (String column : headerLine.split(sep)) {
        // System.out.println("Column: [" + column + "]");
        // }

        // Re-open input stream to re-read from the beginning
        reader.close();
        rawInput = getClass().getClassLoader().getResourceAsStream(pathToCSV);

        // if (input == null)
        // throw new RuntimeException("CSV file not found");

        input = new BOMInputStream(rawInput);
        CsvToBean<ProductDiscount> csvToBean = new CsvToBeanBuilder<ProductDiscount>(new InputStreamReader(input))
                .withType(ProductDiscount.class)
                .withSeparator(separator)
                .withIgnoreLeadingWhiteSpace(true)
                .withIgnoreEmptyLine(true)
                .build();

        return csvToBean.parse();
    }

    // ------------------------------------------------------------------------------------------
    // public Map<String, List<ProductDiscount>> getBe
}
