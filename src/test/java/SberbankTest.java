import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class SberbankTest {

    public static WebDriver driver;
    public static WebDriverWait wait;

    @Before
    public void getStarted() {
        System.setProperty("webdriver.chrome.driver", "driver/chromedriver.exe");
        driver = new ChromeDriver();

        wait = new WebDriverWait(driver, 1);
        driver.manage().window().maximize();
        driver.get("https://www.sberbank.ru");
    }

    @Test
    public void test() {

        goToCalculationPage();

        input("Стоимость недвижимости", "5180000");
        input("Первоначальный взнос", "3058000");
        input("Срок кредита", "30");

        clickToggle("Есть зарплатная карта Сбербанка");
        clickToggle("Есть возможность подтвердить доход справкой");
        clickToggle("Молодая семья");

        check("Сумма кредита", "2 122 000 ₽");
        check("Ежемесячный платеж", "17 998 ₽");
        check("Необходимый доход", "29 997 ₽");
        check("Процентная ставка", "9,6 %");

        clickToggle("Есть возможность подтвердить доход справкой");

        check("Сумма кредита", "2 122 000 ₽");
        check("Ежемесячный платеж", "17 535 ₽");
        check("Необходимый доход", "29 224 ₽");
        check("Процентная ставка", "9,4 %");
    }

    @After
    public void shutDown() {
        driver.quit();
    }



    public static void goToCalculationPage() {
        closeAlert();
        WebElement element = waitToBeClicable(By.xpath("//span[@class='lg-menu__text' and text()='Ипотека']"));
        new Actions(driver).moveToElement(waitToBeClicable(element)).perform();;
        waitToBeClicable(By.linkText("Ипотека на готовое жильё")).click();
        driver.switchTo().frame(driver.findElement(By.xpath("//iframe[@title='Основной контент']")));
    }

    private static void closeAlert() {
        By locator = By.xpath("//a[@title='Закрыть предупреждение']");
        while (true) {
            try {
                waitToBeClicable(locator).click();
                break;
            }
            catch (Exception ignored) {}

            try {
                waitPresence(locator);
            }
            catch (TimeoutException ignored) {
                break;
            }
        }
    }


    private static void waitPresence(By locator) {
        wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    private static WebElement waitToBeVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private static List<WebElement> waitElementsToBeVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    private static WebElement waitToBeClicable(WebElement element) {
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    private static WebElement waitToBeClicable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static void input(String keywords, String value) {
        WebElement element = waitToBeClicable(getInput(keywords));
        while (true) {
            element.clear();
            element.sendKeys(value);
            if (value.equals(element.getAttribute("value").replaceAll("\\D", ""))) {
                try {
                    wait.until(driver -> !value.equals(element.getAttribute("value").replaceAll("\\D", "")));
                }
                catch (TimeoutException e) {
                    String oldValue = getValue("Ежемесячный платеж");
                    try {
                        wait.until((driver) -> !oldValue.equals(getValue("Ежемесячный платеж")));
                    }
                    catch (TimeoutException ignored) {
                    }
                    break;
                }
            }
        }
    }

    private static void check(String keywords, String value) {
        System.out.println("Ожидаемое значение поля <" + keywords + "> " + value
                + ", в действительности это значение " + getValue(keywords) + ".");
        Assert.assertEquals(value, getValue(keywords));
    }

    private static WebElement getToggle(String keywords) {
        List<WebElement> elements = waitElementsToBeVisible(By.xpath("//div[@class='dcCalc_switch-tablet__title']"));
        return elements
                .stream()
                .filter(element -> element.getText().contains(keywords))
                .map(element -> element.findElement(By.xpath("./..//label")))
                .findFirst()
                .orElse(null);
    }

    private static WebElement getInput(String keywords) {
        List<WebElement> elements = waitElementsToBeVisible(By.xpath("//div[@class='dcCalc_input-row-tablet__label']"));
        return elements
                .stream()
                .filter(element -> element.getText().contains(keywords))
                .map(element -> element.findElement(By.xpath("./..//input")))
                .findFirst()
                .orElse(null);
    }

    private static void scrollDown() {
        ((Locatable) waitToBeVisible(By.xpath("//div[contains(text(), 'не является публичной офертой')]")))
                .getCoordinates().inViewPort();
    }

    public static void clickToggle(String keywords) {
        scrollDown();
        WebElement toggle = getToggle(keywords);
        if (toggle == null) {
            System.out.println("Переключатель <" + keywords +"> не найден.");
            return;
        }
        String value = getValue("Ежемесячный платеж");
           while (true) {
            String toggleClass = toggle.getAttribute("class");
            toggle.click();
            try {
                wait.until(driver -> !toggleClass.equals(toggle.getAttribute("class")));
                break;
            }
            catch (TimeoutException ignored) {
            }
        }
        System.out.println("Клик по переключателю <" + keywords + ">.");
        wait.until((driver) -> !value.equals(getValue("Ежемесячный платеж")));
        while (true) {
            String loopValue = getValue("Ежемесячный платеж");
            try {
                wait.until((driver) -> !loopValue.equals(getValue("Ежемесячный платеж")));
            }
            catch (TimeoutException e) {
                break;
            }
        }
    }

    private static String getValue(String keywords) {
        List<WebElement> elements = waitElementsToBeVisible(By.xpath("//div[@class='dcCalc_result-calculation__title']"));
        return elements
                .stream()
                .filter(element -> element.getText().contains(keywords))
                .map(element -> element.findElement(By.xpath("./..//span")).getText())
                .findFirst()
                .orElse("не найдено");
    }
}
