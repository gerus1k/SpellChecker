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
        frame = new JFrame("");
        frame.setBounds(100, 100, 600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        frame.getContentPane().add(mainPanel);

        JPanel topPanel = new JPanel(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);

        JLabel lblCheckIt = new JLabel("Check It");
        lblCheckIt.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(lblCheckIt, BorderLayout.NORTH);

        JLabel lblDescription = new JLabel("<html><div style='text-align: center;'>Просто предоставьте URL-ссылку на нужный сайт, и Spell Checker автоматически проанализирует текст на странице, выявляя и исправляя возможные ошибки. Улучшите качество контента, повысьте профессионализм и уверенность в своих текстах с помощью нашего надежного инструмента Check It.</div></html>");
        topPanel.add(lblDescription, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.add(inputPanel, BorderLayout.SOUTH);

        JLabel lblEnterTheUrl = new JLabel("Enter the URL to check:");
        inputPanel.add(lblEnterTheUrl);

        urlTextField = new JTextField();
        inputPanel.add(urlTextField);
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
        inputPanel.add(btnCheck);

        JSeparator separator = new JSeparator();
        separator.setPreferredSize(new Dimension(600, 5)); // Регулируйте размер по вашему желанию
        mainPanel.add(separator, BorderLayout.CENTER);

        JPanel settingsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        mainPanel.add(settingsPanel, BorderLayout.CENTER);

        JPanel languageSettingsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        settingsPanel.add(languageSettingsPanel);

        JLabel lblLanguageSettings = new JLabel("Выбор языка для проверки:");
        languageSettingsPanel.add(lblLanguageSettings);

        JCheckBox chkRussian = new JCheckBox("Russian");
        chkRussian.setSelected(true); // Выбран по умолчанию
        languageSettingsPanel.add(chkRussian);

        JCheckBox chkEnglish = new JCheckBox("English");
        languageSettingsPanel.add(chkEnglish);

        JButton btnAddLanguage = new JButton("Add"); // Заглушка под кнопку "Добавить"
        languageSettingsPanel.add(btnAddLanguage);

        JPanel checkSettingsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        settingsPanel.add(checkSettingsPanel);

        JLabel lblCheckSettings = new JLabel("Выбор проверок:");
        checkSettingsPanel.add(lblCheckSettings);

        JCheckBox chkGrammar = new JCheckBox("Проверка грамматики на ошибки");
        chkGrammar.setSelected(true); // Выбран по умолчанию
        checkSettingsPanel.add(chkGrammar);

        JCheckBox chkSyntax = new JCheckBox("Проверка синтаксиса на ошибки");
        chkSyntax.setSelected(true); // Выбран по умолчанию
        checkSettingsPanel.add(chkSyntax);

        JCheckBox chkSentenceStructure = new JCheckBox("Проверка на правила построения предложений");
        chkSentenceStructure.setSelected(true); // Выбран по умолчанию
        checkSettingsPanel.add(chkSentenceStructure);

        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);

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
