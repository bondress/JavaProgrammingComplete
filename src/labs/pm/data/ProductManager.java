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

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author pc
 */
public class ProductManager {

    private Product product;
    private Review review;
    private Locale locale;
    private ResourceBundle resources;
    private DateTimeFormatter dateFormat;
    private NumberFormat moneyFormat;

    public ProductManager(Locale locale) {
        this.locale = locale;
        resources = ResourceBundle.getBundle("labs.pm.data.resources", locale);
        dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                .localizedBy(locale);
        moneyFormat = NumberFormat.getCurrencyInstance(locale);
    }

    public Product createProduct(int id, String name, BigDecimal price,
            Rating rating, LocalDate bestBefore) {
        product = new Food(id, name, price, rating, bestBefore);
        return product;
    }

    public Product createProduct(int id, String name, BigDecimal price,
            Rating rating) {
        product = new Drink(id, name, price, rating);
        return product;
    }

    /*
    A more generic(alternative) design of this application could have used a
    type Rateable instead of Product for both instance variable and method
    argument, to enable application to create reviews of any other objects
    that implement the Rateable interface.
     */
    public Product reviewProduct(Product product, Rating rating,
            String comments) {
        review = new Review(rating, comments);
        this.product = product.applyRating(rating);
        return this.product;
    }

    //Creates, prepares and prints a report on a product and its review    
    public void printProductReport() {
        StringBuilder txt = new StringBuilder();
        txt.append(MessageFormat.format(resources.getString("product"),
                product.getName(),
                moneyFormat.format(product.getPrice()),
                product.getRating().getStars(),
                dateFormat.format(product.getBestBefore())));
        txt.append('\n');
        if (review != null) {
            txt.append(MessageFormat.format(resources.getString("review"),
                    review.getRating().getStars(),
                    review.getComments()));
        } else {
            txt.append(resources.getString("no.reviews"));
        }
        txt.append('\n');
        System.out.println(txt);
    }
}
