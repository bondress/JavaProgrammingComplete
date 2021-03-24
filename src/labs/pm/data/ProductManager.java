/*
 * Copyright (C) 2021 pc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package labs.pm.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author pc
 */
public class ProductManager {

    private Map<Product, List<Review>> products = new HashMap<>();

    private ResourceBundle config
            = ResourceBundle.getBundle("labs.pm.data.config");
    private MessageFormat reviewFormat
            = new MessageFormat(config.getString("review.data.format"));
    private MessageFormat productFormat
            = new MessageFormat(config.getString("product.data.format"));
    private Path reportsFolder
            = Path.of(config.getString("reports.folder"));
    private Path dataFolder
            = Path.of(config.getString("data.folder"));
    private Path tempFolder = Path.of(config.getString("temp.folder"));
    private ResourceFormatter formatter;
    private static Map<String, ResourceFormatter> formatters
            = Map.of("en-GB", new ResourceFormatter(Locale.UK),
                    "en-US", new ResourceFormatter(Locale.US),
                    "fr-FR", new ResourceFormatter(Locale.FRANCE),
                    "ru-RU", new ResourceFormatter(new Locale("ru", "RU")),
                    "zh-CN", new ResourceFormatter(Locale.CHINA));
    private static final Logger logger
            = Logger.getLogger(ProductManager.class.getName());

    public ProductManager(Locale locale) {
        this(locale.toLanguageTag());
    }

    public ProductManager(String languageTag) {
        changeLocale(languageTag);
        loadAllData();
    }

    public void changeLocale(String languageTag) {
        formatter = formatters.getOrDefault(languageTag,
                formatters.get("en-GB"));
    }

    public static Set<String> getSupportedLocales() {
        return formatters.keySet();
    }

    public Product createProduct(int id, String name, BigDecimal price,
            Rating rating, LocalDate bestBefore) {
        Product product = new Food(id, name, price, rating, bestBefore);
        products.putIfAbsent(product, new ArrayList<>());
        return product;
    }

    public Product createProduct(int id, String name, BigDecimal price,
            Rating rating) {
        Product product = new Drink(id, name, price, rating);
        products.putIfAbsent(product, new ArrayList<>());
        return product;
    }

    /**
     * This method performs Product search based on a product id value
     *
     * @param id
     * @return a Product object
     */
    public Product findProduct(int id) throws ProductManagerException {
        /*
        Use keySet method to obtain a Set of Product objects from the products
        Map.
        Use stream method to obtain Stream from this Set.
        Use filter method to look for product objects with the same id as the 
        method parameter.
        Use Lambda expression that implements the Predicate interface to provide
        the filter condition.
        Use findFirst method to find the first element that matches the 
        Predicate condition
        findFirst method returns an Optional object
        Use orElseGet method to get this product from the Optional object or 
        return null if the product is not found
        Use Lambda expression that implements Supplier interface to provide the 
        orElseGet logic
        This stream returns either a Product with the matching id or null, if
        such a Product is not found. Return this value from the findProduct
        method.
         */

        return products.keySet()
                .stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElseThrow(()
                        -> new ProductManagerException("Product with id "
                        + id + " not found")
                );
        //orElseGet method was replaced with orElseThrow method
    }

    /*
    A more generic(alternative) design of this application could have used a
    type Rateable instead of Product for both instance variable and method
    argument, to enable application to create reviews of any other objects
    that implement the Rateable interface.
     */
    public Product reviewProduct(Product product, Rating rating,
            String comments) {
        /*
        Locate the entry in the HashMap that corresponds to the product and 
        get from it, the list of reviews.
         */
        List<Review> reviews = products.get(product);
        /*
        Once the entry is located, remove it.
         */
        products.remove(product, reviews);
        /*
        Create a new review object and append it to the reviews list, using
        rating and coments as parameters for the Review constructor
         */
        reviews.add(new Review(rating, comments));
        /*
        Iterate through the list of reviews and calculate the sum of all 
        ratings.
        Use the stream method to obtain a Stream from the list of reviews for
        the given product.
        Use mapToInt method to convert each Review object to an int value of 
        Rating.
        Use Lambda expression that implements the ToIntFunction interface to 
        provide conversion of each review to int value for its Rating.
        Use average method to calculate the aerage rating for reviews in the 
        stream.
        Average method returns an OptionalDouble object.
        Use orElse method to get the double value from the OptionalDouble object
        or return 0 if no reviews were present in the stream.
        This stream returns a double number that represents an average rating
        value.
        Convert this double number to int using Math.round method and cast 
        returned result into an int value.
        Invoke convert method provided by the Rateable interface to convert the 
        average value of stars into a Rating enum value.
        Pass this Rating to the applyRating method and reassign the product 
        object reference.
         */

        product = product.applyRating(
                Rateable.convert(
                        (int) Math.round(
                                reviews.stream()
                                        .mapToInt(r -> r.getRating().ordinal())
                                        .average()
                                        .orElse(0))));

        /* 
        Create a new product that is essentially a replica of the old one
        but with a different rating
         */
        products.put(product, reviews);
        // Return the updated product
        return product;
    }

    /*
    Overloaded version of reviewProduct method that uses int id parameter and
    locates the required product using findProduct method
     */
    public Product reviewProduct(int id, Rating rating, String comments) {
        try {
            return reviewProduct(findProduct(id), rating, comments);
        } catch (ProductManagerException ex) {
            logger.log(Level.INFO, ex.getMessage());
        }
        return null;
    }

    /*
    Overloaded version of printProduct method that uses int id parameter and 
    locates the required product using findProduct method
     */
    public void printProductReport(int id) {
        try {
            printProductReport(findProduct(id));
        } catch (ProductManagerException ex) {
            logger.log(Level.INFO, ex.getMessage());
        } catch (IOException ex) {
            logger.log(Level.SEVERE,
                    "Error printing product report " + ex.getMessage(), ex);
        }
    }

    //Creates, prepares and prints a report on a product and its review    
    public void printProductReport(Product product) throws IOException {
        List<Review> reviews = products.get(product);
        Path productFile
                = reportsFolder.resolve(MessageFormat.format(
                        config.getString("report.file"), product.getId()));
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(
                Files.newOutputStream(productFile,
                        StandardOpenOption.CREATE), "UTF-8"))) {
            out.append(formatter.formatProduct(product)
                    + System.lineSeparator());
            Collections.sort(reviews);
            if (reviews.isEmpty()) {
                out.append(formatter.getText("no.reviews")
                        + System.lineSeparator());
            } else {

                out.append(reviews.stream()
                        .map(r -> formatter.formatReview(r)
                        + System.lineSeparator())
                        .collect(Collectors.joining()));
            }
        }

    }

    /*
    Added a Predicate parameter called filter to this method.
    It will be used to filter the stream content
     */
    public void printProducts(Predicate<Product> filter,
            Comparator<Product> sorter) {
        StringBuilder txt = new StringBuilder();
        /*
        Use keySet method to obtain a Set of Product objects from the products 
        Map.
        Use stream method to obtain a Stream from this Set
        Use sorted method, passing sorter object as parameter to order the 
        stream.
        Use forEach method to append each formatted Product object to the 
        StringBuilder and a new line '\n' character.
         */
        products.keySet()
                .stream()
                .sorted(sorter)
                .filter(filter)
                .forEach(p -> txt.append(formatter.formatProduct(p) + '\n'));
        System.out.println(txt);
    }

    private Product loadProduct(Path file) {
        Product product = null;
        try {
            product = parseProduct(
                    Files.lines(dataFolder.resolve(file),
                            Charset.forName("UTF-8")).findFirst().orElseThrow());
        } catch (Exception ex) {
            logger.log(Level.WARNING,
                    "Error loading products " + ex.getMessage());
        }
        return product;
    }

    private List<Review> loadReviews(Product product) {
        List<Review> reviews = null;
        Path file = dataFolder.resolve(
                MessageFormat.format(
                        config.getString("reviews.data.file"), product.getId())
        );
        if (Files.notExists(file)) {
            reviews = new ArrayList<>();
        } else {
            try {
                reviews = Files.lines(file, Charset.forName("UTF-8"))
                        .map(text -> parseReview(text))
                        .filter(review -> review != null)
                        .collect(Collectors.toList());
            } catch (IOException ex) {
                logger.log(Level.WARNING,
                        "Error loading reviews " + ex.getMessage());
            }
        }

        return reviews;
    }

    private void dumpData() {
        try {
            if (Files.notExists(tempFolder)) {
                Files.createDirectory(tempFolder);
            }
            Path tempFile = tempFolder.resolve(
                    /*
                    The Instant.now() method causes an InvalidPathException on 
                    Windows because it tried to add ":" character in the file
                    name and this is not allowed on Windows
                    Changed it to LocalDate.now() and it works perfectly
                    */
                    MessageFormat.format(config.getString("temp.file"), LocalDate.now()));
            try (ObjectOutputStream out = new ObjectOutputStream(
                    Files.newOutputStream(tempFile, StandardOpenOption.CREATE))) {
                out.writeObject(products);
                products = new HashMap<>();
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE,
                    "Error dumping data " + ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    private void restoreData() {
        try {
            Path tempFile = Files.list(tempFolder)
                    .filter(path -> path.getFileName().toString().endsWith("tmp"))
                    .findFirst().orElseThrow();
            try (ObjectInputStream in = new ObjectInputStream(
                    Files.newInputStream(tempFile, StandardOpenOption.DELETE_ON_CLOSE))) {
                products = (HashMap) in.readObject();
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE,
                    "Error restoring data " + ex.getMessage(), ex);
        }
    }

    private void loadAllData() {
        try {
            products = Files.list(dataFolder)
                    .filter(file -> file.getFileName().toString().startsWith("product"))
                    .map(file -> loadProduct(file))
                    .filter(product -> product != null)
                    .collect(Collectors.toMap(product -> product,
                            product -> loadReviews(product)));
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error loading data " + ex.getMessage(), ex);
        }
    }

    private Review parseReview(String text) {
        Review review = null;
        try {
            Object[] values = reviewFormat.parse(text);
            review = new Review(Rateable.convert(
                    Integer.parseInt((String) values[0])), (String) values[1]);
        } catch (ParseException | NumberFormatException ex) {
            logger.log(Level.WARNING, "Error parsing review " + text + " "
                    + ex.getMessage());
        }
        return review;
    }

    private Product parseProduct(String text) {
        Product product = null;
        try {
            Object[] values = productFormat.parse(text);
            int id = Integer.parseInt((String) values[1]);
            String name = (String) values[2];
            BigDecimal price = BigDecimal.valueOf(Double
                    .parseDouble((String) values[3]));
            Rating rating
                    = Rateable.convert(Integer.parseInt((String) values[4]));
            switch ((String) values[0]) {
                case "D":
                    product = new Drink(id, name, price, rating);
                    break;
                case "F":
                    LocalDate bestBefore = LocalDate.parse((String) values[5]);
                    product = new Food(id, name, price, rating, bestBefore);
                    break;// Not neccesary to put break as this is the last case
            }
        } catch (ParseException | NumberFormatException
                | DateTimeParseException ex) {
            logger.log(Level.WARNING, "Error parsing product " + text + " "
                    + ex.getMessage());
        }
        return product;
    }

    public Map<String, String> getDiscounts() {
        /*
        Use keySet method to obtain a Set of Product objects from the 
        products Map
        Use stream method to create a stream of Product objects
        Use collect method to asemble your calculation results into a Map
        (You will need to pass two parameters to this collect operation - the 
        first one will be a grouping collector to create a map entry per each 
        rating and the second one will be the calculation, followed by 
        formatting of the total discount value for every rating.)
        Use Collectors.groupingBy method to group discount values by ratings.
        Extract the stars property from the rating of every product to create 
        key value for the results Map
        Use Collectors.collectingAndThen method to produce the formatted value 
        of the total discount per rating. (You will need to pass two parameters 
        to this operation - the first one performing the sum discount 
        calculation and the second one to format this discount value)
        Use Collectors.summingDouble method to perform discount calculation, 
        extracting each product discount as a Double value.
         */
        return products.keySet()
                .stream()
                .collect(
                        Collectors.groupingBy(
                                product -> product.getRating().getStars(),
                                Collectors.collectingAndThen(
                                        Collectors.summingDouble(
                                                product -> product.getDiscount()
                                                        .doubleValue()),
                                        discount -> formatter.moneyFormat
                                                .format(discount))));
        /*
        Using streams to implement such a calculation, formatting and data
        regrouping logic may improve performance by merging a number of data
        manipulations into a single pass on data and potentially benefitting 
        from the parallel stream processing capabilities in case you may have
        to handle a very large collection of products.
         */
    }

    private static class ResourceFormatter {

        private Locale locale;
        private ResourceBundle resources;
        private DateTimeFormatter dateFormat;
        private NumberFormat moneyFormat;

        private ResourceFormatter(Locale locale) {
            this.locale = locale;
            resources = ResourceBundle.getBundle("labs.pm.data.resources",
                    locale);
            dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                    .localizedBy(locale);
            moneyFormat = NumberFormat.getCurrencyInstance(locale);
        }

        private String formatProduct(Product product) {
            return MessageFormat.format(resources.getString("product"),
                    product.getName(), moneyFormat.format(product.getPrice()),
                    product.getRating().getStars(),
                    dateFormat.format(product.getBestBefore()));
        }

        private String formatReview(Review review) {
            return MessageFormat.format(resources.getString("review"),
                    review.getRating().getStars(), review.getComments());
        }

        private String getText(String key) {
            return resources.getString(key);
        }
    }
}
