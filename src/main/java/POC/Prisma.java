package POC;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Double.parseDouble;

public class Prisma implements Store {
    // change this variable manually to see info during runtime
    // true - program outputs status info into the console
    // false - no system output (preferred when not testing)
    private static final boolean debug = true;

    public static List<Product> searchProducts(String keyword) {
        //Returns a list of products from prismamarket.ee with given keyword
        List<Product> products = new ArrayList<>();

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);

        String url = "https://www.prismamarket.ee/products/search/" + keyword;
        driver.get(url);

        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(1));

        List<WebElement> shelves = driver.findElements(By.className("js-products-shelf"));//Products are split into different categories in prismamarket.ee
        int shelfCount = shelves.size();
        int scrapedShelfCount = 1;

        for (WebElement shelf : shelves) {
            List<WebElement> items = shelf.findElements(By.className("js-shelf-item"));

            if (debug) System.out.println("[Prisma] Shelf (" + scrapedShelfCount + "/" + shelfCount + ")");
            scrapedShelfCount++;

            for (WebElement item : items) {

                WebElement img = item.findElement(By.className("js-image-wrapper"));
                String imgURL = img.findElement(By.tagName("img")).getAttribute("src");
                String name = item.findElement(By.className("name")).getText();
                String link = item.findElement(By.tagName("a")).getAttribute("href");

                String integer = item.findElement(By.className("whole-number")).getText();
                String cents = item.findElement(By.className("decimal")).getText();
                double price = parseDouble(integer + "." + cents);

                if (item.findElements(By.className("discount-price")).size() > 0) {
                    String preSalePriceString = item.findElement(By.className("discount-price")).getText();
                    double preSalePrice = parseDouble(preSalePriceString.replace(",", ".").replace(" €", ""));
                    products.add(new Product("Prisma", name, price, DiscountType.campaign, imgURL, preSalePrice, link));
                } else products.add(new Product("Prisma", name, price, DiscountType.noDiscount, imgURL, price, link));

            }
        }

        driver.quit();

        return products;
    }

}
