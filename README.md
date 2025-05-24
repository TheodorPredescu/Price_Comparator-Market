#  Backend for a Price Comparator for Stores

Written in Java using Spring Boot and Maven, this project processes CSV data for product pricing and discounts to help users find the best deals across stores.

---

##  Project Structure Overview

The project uses a typical Spring Boot structure:

- `model/` – Contains the data models representing products, discounts, and other entities used throughout the application.
- `repository/` – Includes Spring Data interfaces for accessing and managing CSV-parsed data and historical records.
- `service/` – Implements the core business logic, such as applying discounts, computing value per unit, and generating price trends.
- `controller/` – Exposes REST API endpoints to interact with the application.
- `util/` – Utility classes, such as CSV parsers, date formatters, or unit converters.
- `resources/` – Configuration files like `application.yml`, and other resource files (e.g., sample CSVs).

This structure follows standard Spring Boot best practices for clean, maintainable, and scalable code.

###  **Product CSV:**
- `product_id`
- `product_name`
- `product_category`
- `brand`
- `package_quantity`
- `package_unit`
- `price`
- `currency`

---

###  **Discount CSV:**
- `product_id`
- `product_name`
- `brand`
- `package_quantity`
- `package_unit`
- `product_category`
- `from_date`
- `to_date`
- `percentage_of_discount`

---

##  Features

###  Daily Shopping Basket Monitoring
- Helps users split their basket into shopping lists that optimize for cost savings.
- It gets a list of product IDs and it returns the product and the store with the best price for it.

###  Best Discounts
- Lists products with the highest current percentage discounts across all tracked stores.
- Based on a list of product IDs we return the store with the best offer and the offer itself.
- The discount is automatically applied if it is present (if we have a CSV from that store with the same date as the product CSV).

###  New Discounts
- Displays discounts that were introduced n days ago (where n is a given value).
- Only the latest CSV with discounts will be considered; Older CSV are assumed to be expired.

###  Dynamic Price History Data
- It lists the price history of products; a list of the products price, store, category, brand and date.
- Provides data points to allow a frontend to display price trends over time for individual products.
- Filterable by store, product category, or brand.

###  Product Substitutes & Recommendations
- Highlights "value per unit" (e.g., price per kg, price per liter) to help identify the best buys, even if pack sizes differ.
- Returns a clarified form of the product price, making it easier to compare it with products from other stores.

###  Custom Price Alerts
- Allows users to set a target price for a product.
- It returns information about the stores where the price is lower or equal with the one provided.

---

## How to build and run it

###  Prerequisites

Ensure the following tools are installed:

- **Java 17** or later
- **Maven 3.6+**
- **Git** (optional, for cloning the repository)

---

###  Clone the Repository

If you're using Git:

```bash
git clone https://github.com/TheodorPredescu/Price_Comparator-Market
cd Price_Comparator-Market
```

### Build the project and run it

```bash
mvn clean install
mvn spring-boot:run
```

##  Assumptions

- Only the latest CSV (based on the date in the filename) is considered in stock.
- Only discounts with the same date as the latest product CSV are considered valid.
- The CSV files are delimited by either a semicolon (;) or a comma (,).


---
---





## How to Use the Implemented Features (API Endpoints)

The backend exposes several REST API endpoints to query product pricing, discounts, and price alerts. All endpoints use HTTP GET requests.

---

### 1. Get Best Discounts

**Endpoint:** `/getBestDiscounts`

**Example Request:**  

```plaintext
GET http://localhost:8080/getBestDiscounts?topN=20
```



**Response:**  
A JSON array of entries where each entry contains:
- Store name (String)
- Discount details (product name, percentage of discount, etc.)

---

### 2. Get New Discounts Introduced N Days Ago

**Endpoint:** `/getNewDiscounts`

**Query Parameter:**  
- `daysAgo` (integer): Number of days ago when discounts were introduced

**Example Request:**  

```plaintext
GET http://localhost:8080/getNewDiscounts?daysAgo=3
```

**Response:**  
A JSON array of entries with store name and discount details (product, percentage, from/to dates).

---

### 3. Get Price History for Products

**Endpoint:** `/priceHistory`

**Example Request:**  
```plaintext
http://localhost:8080/priceHistory
```

**Response:**  
A list of price history records including price, date, store, etc.

---

### 4. Daily Shopping Basket Monitoring

**Endpoint:** `/dailyShoppingBasketMonitoring`

**Query Parameter:**  
- `productId` (required) — List of product IDs to check best prices for. Can be specified multiple times or as a comma-separated list.
**Example Request:**  

```plaintext
GET http://localhost:8080/dailyShoppingBasketMonitoring?productId=P001,P002,P005
```

**Response:**  
A list of entries with store name and product details (name, ID, price, package quantity/unit, and standardized unit price).

---

### 5. Custom Price Alert

**Endpoint:** `/customPriceAlert`

**Query Parameters:**  
- `productId` (String): ID of the product to monitor  
- `priceLimit` (Float): Target price threshold

**Example Request:**  

```plaintext
GET http://localhost:8080/customPriceAlert?productId=P001&priceLimit=10.5
```

**Response:**  
A JSON map with store names as keys and the product prices as values where the price is below the threshold.

---

### Notes

- All endpoints return JSON responses.
- The product IDs are case-sensitive.
- Discounts are only considered valid if they come from the latest discount CSV matching the latest product CSV date.

---

If you want, you can test these endpoints with tools like `curl`, Postman, or any REST client.
