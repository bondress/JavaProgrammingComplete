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
package labs.pm.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import labs.pm.data.Product;
import labs.pm.data.ProductManager;
import labs.pm.data.Rating;

/**
 * {@code Shop} class represents an application that manages Products
 *
 * @version 4.0
 * @author pc
 */
public class Shop {

    public static void main(String[] args) {
        ProductManager pm = new ProductManager("en-GB");
        Product p1 = pm.createProduct(101, "Tea", BigDecimal.valueOf(1.99),
                Rating.NOT_RATED);
//        pm.printProductReport(p1);
        p1 = pm.reviewProduct(p1, Rating.FOUR_STAR, "Nice hot cup of tea!");
        //code to test multiple review capability
        p1 = pm.reviewProduct(101, Rating.TWO_STAR, "Rather weak tea");
        p1 = pm.reviewProduct(101, Rating.FOUR_STAR, "Fine tea");
        p1 = pm.reviewProduct(101, Rating.FOUR_STAR, "Good tea");
        p1 = pm.reviewProduct(101, Rating.FIVE_STAR, "Perfect tea");
        p1 = pm.reviewProduct(101, Rating.THREE_STAR, "Just add some lemon");
//        pm.printProductReport(101);
        /*
        Code to create more products to test multi-product capabilities of the
        ProductManager class.
         */
        // Switching locale
//        pm.changeLocale("ru-RU");
        Product p2 = pm.createProduct(102, "Coffee", BigDecimal.valueOf(1.99),
                Rating.NOT_RATED);
        p2 = pm.reviewProduct(102, Rating.THREE_STAR, "Coffee was ok");
        p2 = pm.reviewProduct(102, Rating.ONE_STAR, "Where is the milk?!");
        p2 = pm.reviewProduct(102, Rating.FIVE_STAR,
                "It's perfect with ten spoons of sugar!");
//        pm.printProductReport(102);
        // Switching locale
//        pm.changeLocale("en-US");
        Product p3 = pm.createProduct(103, "Cake", BigDecimal.valueOf(3.99),
                Rating.NOT_RATED, LocalDate.now().plusDays(2));
        p3 = pm.reviewProduct(103, Rating.FIVE_STAR, "Very nice cake");
        p3 = pm.reviewProduct(103, Rating.FOUR_STAR,
                "It's good but I've expected more chocolate");
        p3 = pm.reviewProduct(103, Rating.FIVE_STAR, "This cake is perfect!");
//        pm.printProductReport(103);
        Product p4 = pm.createProduct(104, "Cookie", BigDecimal.valueOf(2.99),
                Rating.NOT_RATED, LocalDate.now());
        p4 = pm.reviewProduct(104, Rating.THREE_STAR, "Just another cookie");
        p4 = pm.reviewProduct(104, Rating.THREE_STAR, "Ok");
//        pm.printProductReport(104);
        //Switching locale
//        pm.changeLocale("fr-FR");
        Product p5 = pm.createProduct(105, "Hot Chocolate",
                BigDecimal.valueOf(2.50), Rating.NOT_RATED);
        p5 = pm.reviewProduct(105, Rating.FOUR_STAR, "Tasty!");
        p5 = pm.reviewProduct(105, Rating.FOUR_STAR, "Not bad at all");
//        pm.printProductReport(105);

        // Switching locale
//        pm.changeLocale("zh-CN");
        Product p6 = pm.createProduct(106, "Chocolate",
                BigDecimal.valueOf(2.50), Rating.NOT_RATED,
                LocalDate.now().plusDays(3));
        p6 = pm.reviewProduct(106, Rating.TWO_STAR, "Too sweet");
        p6 = pm.reviewProduct(106, Rating.THREE_STAR, "Better than cookie");
        p6 = pm.reviewProduct(106, Rating.TWO_STAR, "Too bitter");
        p6 = pm.reviewProduct(106, Rating.ONE_STAR, "I don't get it!");
        pm.printProductReport(106);
        
        /*
        Testing new logic
        Use forEach method to iterate through all Map entries returned by the 
        getDiscounts method. Write a Lambda expression that implements
        BiConsumer interface to handle the key-value pairs for each of the 
        map entries, where teh key is the rating and value is the discount.
        Print each rating value concatenated with tab "\t" character and then 
        concatenated with the value of discount
        */
        pm.getDiscounts().forEach(
        (rating, discount)->System.out.println(rating + "\t" + discount));

//        //Lambda expression that orders products based on their ratings
//        //Descending order
//        //This expression ran successful although compiled with errors
        pm.printProducts(p -> p.getPrice().floatValue() < 2,
                (p1, p2) -> p2.getRating().ordinal()
                - p1.getRating().ordinal());
//        //Lambda expression that orders products based on their prices
//        //Descending order
//        pm.printProducts((p1, p2) -> p2.getPrice().compareTo(p1.getPrice()));

//        //Combining multiple Comparator objects
//        //This expression ran successful although compiled with errors
//        Comparator<Product> ratingSorter = (p1,p2) -> 
//                p2.getRating().ordinal() - p1.getRating().ordinal();
//        Comparator<Product> priceSorter = (p1,p2) -> 
//                p2.getPrice().compareTo(p1.getPrice());
//        pm.printProducts(ratingSorter.thenComparing(priceSorter));
//        pm.printProducts(ratingSorter.thenComparing(priceSorter).reversed());
    }

}
