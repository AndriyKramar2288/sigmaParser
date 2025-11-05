package org.banew.sigmaParser;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {

    @Data
    static class Result {
        double leftPrice;
        double rightPrice;
        double priceDifference;
        String url;

        public Result(double leftPrice, double rightPrice, String url) {
            this.leftPrice = leftPrice;
            this.rightPrice = rightPrice;
            this.url = "https://white.market/" + url;
            priceDifference = (double) Math.round((rightPrice - leftPrice) * 100) / 100;
        }
    }

    private static void gortannaPokiNeVsretsa(WebDriver driver) throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        Actions actions = new Actions(driver);

        int killerMOtherFocker = 0;

        var oldDocumentHeight = (long) js.executeScript("return document.body.scrollHeight");

        while (true) {
            actions.sendKeys(Keys.PAGE_DOWN).perform();
            Thread.sleep(100); // щоб дати сторінці довантажитися

            var ourY = (long) js.executeScript("return Math.round(window.scrollY)");
            var documentHeight = (long) js.executeScript("return document.body.scrollHeight");

            System.out.println("наше " + ourY + " сторінка " + documentHeight + "\n===");

            if (documentHeight - ourY < 1000) {
                if (++killerMOtherFocker >= 10) {
                    break;
                }
                if (oldDocumentHeight != documentHeight) {
                    killerMOtherFocker = 0;
                }
            }

            oldDocumentHeight = documentHeight;
        }
    }

    public static void main(String[] args) {

        String requestUrl = args[0];

        try {
            while (true) {
                sex(requestUrl, "aboba.json");
                Thread.sleep(Long.parseLong(args[1]) * 1000 * 60);
            }
        }
        catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }

    private static void sex(String requestUrl, String resultFile) throws Exception {
        WebDriverManager
                .chromedriver()
                .cachePath("/tmp/selenium-cache")
                .setup();

        ChromeOptions options = new ChromeOptions();
        //options.addArguments("--headless");
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));

        List<Result> results = new ArrayList<>();

        try {
            driver.get(requestUrl);

            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy((By.className("styles_item__u5Pqr"))));

            gortannaPokiNeVsretsa(driver);

            Document document = Jsoup.parse(driver.getPageSource());

            Elements elements = document.select(".styles_item__u5Pqr");
            for (Element element : elements) {
                try {
                    String src = element.selectFirst("a").attr("href");
                    String left = element.selectFirst(".price").text();
                    String right = element.selectFirst(".styles_steam-price__QBsK8")
                            .selectFirst(".styles_paragraph__SdnEx").text();

                    results.add(new Result(formatString(left), formatString(right), src));
                }
                catch (NullPointerException ex) {
                    System.out.println("ЇБАТЬ");
                }
            }
        } catch (Exception e) {
            throw e;
        }
        finally {
            driver.quit();

            results.sort(Comparator.comparing(r -> r.priceDifference));

            ObjectMapper objectMapper = new ObjectMapper();

            Files.writeString(
                    Paths.get(resultFile),
                    objectMapper.writeValueAsString(results),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            results.forEach(System.out::println);
            System.out.println("Size: " + results.size());
        }
    }

    private static double formatString(String string) {
        return Double.parseDouble(string.substring(1));
    }
}