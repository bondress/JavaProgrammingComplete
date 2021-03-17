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
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author pc
 */
public class ProductManager {

    private Product product;
    private Review[] reviews = new Review[5];
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

        /*
        If the reviews array is full, increase the size of the array by 5
        elements.
         */
        if (reviews[reviews.length - 1] != null) {
            reviews = Arrays.copyOf(reviews, reviews.length + 5);
        }
        int sum = 0, i = 0;
        /*
        The purpose of these variables is to compute the total number of stars
        in all ratings and to count a number of ratings so that the average
        rating value can be determined.
         */
        boolean reviewed = false;
        /*
        The purpose of this variable is to indicate if the review was
        successfully added to the array of reviews and use it as a condition
        to terminate iteration through this array.
         */

 /*
        This loop will continue to iterate until it reaches the end of the
        array and check that the review has not yet been added to the array
         */
        while (i < reviews.length && !reviewed) {
            /*
            This if statement checks if an element in the array is null. If this
            is the case, create a new Review object passing @rating and 
            @comments parameters to the constructor, assign this review to the
            current element in the reviews array, and set the reviewed variable
            to be true, to indicate that no more iterations are required.
             */
            if (reviews[i] == null) {
                reviews[i] = new Review(rating, comments);
                reviewed = true;
            }
            /*
            This statement adds the int stars value of Rating to the sum
            variable.
            It invokes the getRating method on a current reviews array object
            and uses the ordinal method that is available for any enumeration
            to achieve this.
             */
            sum += reviews[i].getRating().ordinal();
            i++; //progress to the next iteration
        }
        this.product
        = product.applyRating(Rateable.convert(Math.round((float) sum / i)));
        /*
        This new code caluculates the rating based on the average number of
        ratings (sum/i)
        */
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
        for (Review review : reviews) {
            if (review == null) {
                break;
            }
            txt.append(MessageFormat.format(resources.getString("review"),
                    review.getRating().getStars(),
                    review.getComments()));
            txt.append('\n');
        }
        if(reviews[0] == null){
            txt.append(resources.getString("no.reviews"));
            txt.append('\n');
        }
            System.out.println(txt);
    }
}
