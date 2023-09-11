# Spell Checker

Spell Checker is a tool for checking spelling and grammar in web pages. It uses various tools like LanguageTool and Cybozu Labs Language Detector to detect and correct errors in the text.

## Installation and Usage

1. Ensure you have the following components installed:
   - [Java Development Kit (JDK)](https://www.oracle.com/java/technologies/downloads/)
   - [Maven](https://maven.apache.org/download.cgi)
   - [WebDriver (browser driver, e.g., ChromeDriver)](https://chromedriver.chromium.org/downloads)

2. Clone the repository to your computer:

   ```shell
   git clone https://github.com/gerus1k/SpellChecker.git
   ```

3. Navigate to the project directory:

   ```shell
   cd SpellChecker
   ```

4. Compile and run the program:

   ```shell
   mvn compile
   mvn exec:java -Dexec.mainClass="org.example.Main"
   ```


## Usage

1. Start the program by following the instructions above.

2. Enter the URL of the web page you want to check.

3. The program will load the web page and begin checking spelling and grammar.

4. The results of the check will be displayed in the console, including detected errors and suggested corrections.

## Configuration

- You can configure language settings and checking rules in the `languagetool-config.xml` file.

## License

This project is licensed under the Apache License - see the [LICENSE](LICENSE) file for details.

## Contact

If you have questions, suggestions, or bug reports, please contact at sheide.german@gmail.com or via [Telegram](https://t.me/sheide_gs).

## Authors

- Sheide German

## Acknowledgments

Would like to thank the following projects and their creators for their open-source and free software that made this project possible:

- [LanguageTool](https://languagetool.org/)
- [Cybozu Labs Language Detector](https://github.com/shuyo/language-detection)

Thank you for using Spell Checker!
