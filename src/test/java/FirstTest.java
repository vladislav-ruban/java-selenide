import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import java.util.List;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.CollectionCondition.*;

public class FirstTest {
    @Test
    public void searchTest() {
        String dell = "Dell";
        open("https://price.ua/");
        $(By.id("SearchForm_searchPhrase")).val("Dell").pressEnter();
        List<SelenideElement> list = $$(By.xpath(".//a[@class='model-name ga_card_mdl_title']"));
        list.stream().map(s -> s.shouldHave(text("Dell")));
        closeWebDriver();
    }
}
