package org.example;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.Russian;
import org.languagetool.rules.RuleMatch;
import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

public class Main {

    private JFrame frame;
    private JTextField urlTextField;
    private JTextArea resultTextArea;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new Main().initialize();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void initialize() {
        frame = new JFrame("Language Checker");
        frame.setBounds(100, 100, 600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        frame.getContentPane().add(panel, BorderLayout.NORTH);

        JLabel lblEnterTheUrl = new JLabel("Enter the URL to check:");
        panel.add(lblEnterTheUrl);

        urlTextField = new JTextField();
        panel.add(urlTextField);
        urlTextField.setColumns(30);

        JButton btnCheck = new JButton("Check");
        btnCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    onCheckButtonClick();
                } catch (InterruptedException | LangDetectException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        panel.add(btnCheck);

        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    private void onCheckButtonClick() throws InterruptedException, LangDetectException, IOException {
        String url = urlTextField.getText();
        WebDriver driver = new ChromeDriver();
        driver.get(url);

        List<WebElement> webElements = driver.findElements(By.xpath("//*[normalize-space(text()) != '']"));

        DetectorFactory.loadProfile("src/main/resources/profiles");
        Detector detector = DetectorFactory.create();

        JLanguageTool languageToolRu = new JLanguageTool(new Russian());
        JLanguageTool languageToolEn = new JLanguageTool(new AmericanEnglish());
        languageToolRu.disableRule("UPPERCASE_SENTENCE_START");
        languageToolRu.disableRule("COMMA_PARENTHESIS_WHITESPACE");
        languageToolRu.disableRule("Many_PNN");

        StringBuilder resultText = new StringBuilder("---------------------------------------------------------------------------------\n");
        resultText.append("| Error Location                   | Word with Error   | Suggested Correction   |\n");
        resultText.append("---------------------------------------------------------------------------------\n");

        for (WebElement element : webElements) {
            String text = element.getText().trim();

            // Добавим проверку наличия текста и достаточной длины
            if (!text.isEmpty() && text.length() > 10) {
                detector.append(text);

                List<RuleMatch> matches;
                if (detector.detect().equals("ru")) {
                    matches = languageToolRu.check(text);
                } else if (detector.detect().equals("en")) {
                    matches = languageToolEn.check(text);
                } else {
                    matches = null;
                }

                if (matches != null) {
                    for (RuleMatch match : matches) {
                        resultText.append(String.format("| %-40s | %-25s | %-30s |\n",
                                text.substring(0, Math.min(text.length(), 20)) + "...",
                                text.substring(match.getFromPos(), match.getToPos()),
                                match.getSuggestedReplacements().isEmpty() ? "-" : match.getSuggestedReplacements().get(0)));
                        resultText.append("---------------------------------------------------------------------------------\n");
                    }
                }
            }
        }

        resultTextArea.setText(resultText.toString());
        Thread.sleep(2000);
        driver.close();
        driver.quit();
    }

}
