package org.example;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import org.fusesource.jansi.Ansi;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.Russian;

import org.languagetool.rules.RuleMatch;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws InterruptedException, IOException, LangDetectException {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);

        System.out.println("Enter the URL to check : ");
        Scanner sc = new Scanner(System.in);
        String url = sc.next();

        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        driver.get(url);

        List<WebElement> webElements = driver.findElements(By.xpath("//*[normalize-space(text()) != '']"));

        DetectorFactory.loadProfile("src/main/resources/profiles");
        Detector detector = DetectorFactory.create();

        JLanguageTool languageToolRu = new JLanguageTool(new Russian());
        JLanguageTool languageToolEn = new JLanguageTool(new AmericanEnglish());
        languageToolRu.disableRule("UPPERCASE_SENTENCE_START");
        languageToolRu.disableRule("COMMA_PARENTHESIS_WHITESPACE");
        languageToolRu.disableRule("Many_PNN");

        ArrayList<String> textList = new ArrayList<>();

        for (WebElement element : webElements) {
            String tmp = element.getText().trim();

            if (!tmp.isEmpty()) {
                textList.add(element.getText());
            }
        }

        List<RuleMatch> matches = null;

        System.out.println("---------------------------------------------------------------------------------");
        System.out.println("| Error Location                   | Word with Error   | Suggested Correction   |");
        System.out.println("---------------------------------------------------------------------------------");

        for (String text : textList) {
            detector.append(text);

            if (detector.detect().equals("ru")) {
                matches = languageToolRu.check(text);
            } else if (detector.detect().equals("en")) {
                matches = languageToolEn.check(text);
            }

            assert matches != null;
            for (RuleMatch match : matches) {


                System.out.printf("| %-40s | %-25s | %-30s |\n",
                        Ansi.ansi().fg(Ansi.Color.YELLOW).a(text.substring(0, Math.min(text.length(), 20)) + "...").reset(),
                        Ansi.ansi().fg(Ansi.Color.RED).a(text.substring(match.getFromPos(), match.getToPos())).reset(),
                        Ansi.ansi().fg(Ansi.Color.GREEN).a(match.getSuggestedReplacements().isEmpty() ? "-" : match.getSuggestedReplacements().get(0)).reset());
                System.out.println("---------------------------------------------------------------------------------");
            }
        }
        Thread.sleep(2000);
        driver.close();
        driver.quit();
    }

}
