import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import org.hamcrest.Matchers;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.ArrayList;
import java.util.List;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

public class PriceUaTests {

    String email = "yoxanov792@mail7d.com";
    String password = "12345678";
    int minPrice = 1000;
    int maxPrice = 2000;

    @DataProvider
    private static Object[][] searchQueries() {
        return new Object[][] {
                {"Dell"},
                {"Samsung"},
                {"Sony"}
        };
    }

    @DataProvider
    private static Object[][] producerAliases() {
        return new Object[][] {
                {"apple"},
                {"samsung"},
                {"xiaomi"}
        };
    }

    @BeforeTest
    public void setUp() {
        Configuration.timeout = 15000;
        Configuration.browserSize = "1920x1080";
    }

    @BeforeMethod
    public void getPage() {
        open("https://price.ua/");
        $(By.xpath(".//a[@class='announcement-goal announcer-link']")).shouldBe(visible);
    }

    @AfterTest
    public void tearDown() {
        closeWebDriver();
    }

    private void selectCategoryMobilePhones() {
        $(By.xpath(".//a[@class='announcement-goal announcer-link']")).shouldBe(visible);
        $(By.xpath(".//a[@class='ga_cats_lateral'][@data-tracker-cid='6']")).shouldBe(visible).hover();
        $(By.xpath(".//a[@class='ga_cats_lateral'][@data-tracker-cid='52']")).shouldBe(visible).click();
    }

    @Test
    public void categorySelectionTest() {
        selectCategoryMobilePhones();
        Assert.assertEquals($(By.xpath("//span[@class='breadcrumbs-last']")).getText(), "Мобильные телефоны, смартфоны");
    }

    @Test
    public void addProductFromCategoryToWishlistTest() {
        selectCategoryMobilePhones();
        String productTitle = $(By.xpath(".//div[contains(@class, 'product-block')]/div[@class='white-wrap']/a"))
                .shouldHave(attribute("title")).getText();
        $(By.xpath(".//a[@class='announcement-goal announcer-link']")).shouldBe(visible);
        $(By.xpath(".//span[contains(@class, 'add-to-wishlist-link')]")).shouldBe(visible).click();
        $(By.xpath(".//div[contains(@class, 'wishlist-popunder')]")).shouldBe(visible);
        $(By.xpath(".//a[@class='wishlist-panel-link']/span")).shouldBe(visible);
        $(By.xpath(".//a[@class='wishlist-panel-link']")).click();
        Assert.assertEquals($(By.xpath(".//div[@class='local-wish-item']/a")).getText(), productTitle);
        $(By.xpath(".//div[@class='local-wish-item']/span[@class='remove_local_wish']")).click();
    }

    @Test(dataProvider = "producerAliases")
    public void filterByManufacturerTest(String producerAlias) {
        selectCategoryMobilePhones();
        $(By.xpath(".//a[@data-producer-alias='" + producerAlias + "']")).shouldBe(visible).click();
        List<SelenideElement> productList = $$(By.xpath(".//div[@class='white-wrap']/a[contains(@class, 'model-name')]")).shouldBe(sizeGreaterThan(0));
        List<SelenideElement> productListContainsModel = $$(By.xpath(".//div[@class='white-wrap']/a[contains(@class, 'model-name')]")).shouldBe(sizeGreaterThan(0))
                .filterBy(text(producerAlias));
        Assert.assertEquals(productList, productListContainsModel);
    }

    @Test
    public void filterByPriceFixed() {
        selectCategoryMobilePhones();
        $(By.xpath(".//a[contains(@data-filter-value, '\"price[max]\":\"1300\"')]")).click();
        $(By.xpath(".//div[@class='applied-filters']")).shouldBe(visible);

        List<SelenideElement> priceLabelList = $$(By.xpath(".//div[@class='price-wrap']//span[@class='price']"));
        ArrayList<Integer> priceIntegerList = new ArrayList();
        for (SelenideElement price : priceLabelList)
            priceIntegerList.add(Integer.parseInt(price.getText().replaceAll("[^0-9]", "")));
        assertThat(priceIntegerList, everyItem(Matchers.lessThanOrEqualTo(1300)));
    }

    @Test
    public void filterByPriceInput() {
        selectCategoryMobilePhones();
        $(By.xpath(".//input[@id='price_min_']")).click();
        $(By.xpath(".//input[@id='price_min_']")).sendKeys(String.valueOf(minPrice));
        $(By.xpath(".//input[@id='price_max_']")).click();
        $(By.xpath(".//input[@id='price_max_']")).sendKeys(String.valueOf(maxPrice), Keys.ENTER);
        Assert.assertTrue(WebDriverRunner.url().contains(String.valueOf(minPrice)));
        Assert.assertTrue(WebDriverRunner.url().contains(String.valueOf(maxPrice)));
        List<SelenideElement> priceLabelList = $$(By.xpath(".//div[@class='price-wrap']//span[@class='price']"));
        ArrayList<Integer> priceIntegerList = new ArrayList();
        for (SelenideElement price : priceLabelList) priceIntegerList.add(Integer.parseInt(price.getText().replaceAll("[^0-9]", "")));
        assertThat(priceIntegerList, everyItem(Matchers.greaterThanOrEqualTo(minPrice)));
        assertThat(priceIntegerList, everyItem(Matchers.lessThan(maxPrice)));
    }

    @Test(dataProvider = "searchQueries")
    public void searchTest(String searchQuery) {
        $(By.id("SearchForm_searchPhrase")).val(searchQuery).pressEnter();
        List<SelenideElement> modelNameList = $$(By.xpath(".//a[contains(@class, 'model-name')]"));
        List<String> modelNameStringList = new ArrayList<>();
        modelNameList.stream().map(s -> modelNameStringList.add(s.getText()));
        assertThat(modelNameStringList, everyItem(containsString(searchQuery)));
    }

    @Test
    public void linkToTopButtonTest() {
        $(By.xpath(".//button[@class='close announcement-acb']")).click();
        $(By.xpath(".//h1[contains(@class, 'title-before-seotext')]")).scrollTo();
        $(By.xpath(".//div[contains(@class, 'header-fixed')]")).shouldBe(visible);
        $(By.xpath(".//a[@id='link-to-top']")).shouldBe(visible).click();
        Assert.assertTrue($(By.xpath("//div[@class='image']")).isDisplayed());
    }

    @Test
    public void loginTest() {
        $(By.xpath(".//a[@form-name='login']")).shouldBe(visible).click();
        $(By.xpath(".//input[@id='LoginForm_username']")).shouldBe(visible).click();
        $(By.xpath(".//input[@id='LoginForm_username']")).sendKeys(email);
        $(By.xpath(".//input[@id='login_user_password']")).click();
        $(By.xpath(".//input[@id='login_user_password']")).sendKeys(password, Keys.ENTER);
        Assert.assertTrue($(By.xpath(".//a[@id='header-user-link']")).shouldBe(visible).isDisplayed());
        $(By.xpath(".//a[@id='header-user-link']")).click();
        $(By.xpath(".//a[@class='i-logout']")).shouldBe(visible).click();
        $(By.xpath(".//a[@form-name='login']")).shouldBe(visible);
    }
}
