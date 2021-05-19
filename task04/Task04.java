package task04;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Пользователь с клавиатуры вводит путь к существую-
 * щей директории и слово для поиска. После чего запускаются
 * два потока. Первый должен найти файлы, содержащие
 * искомое слово и слить их содержимое в один файл. Второй
 * поток ожидает завершения работы первого потока. После
 * чего проводит вырезание всех запрещенных слов(список
 * этих слов нужно считать из файла с запрещенными сло-
 * вами) из полученного файла. В методе main необходимо
 *   отобразить статистику выполненных операций.
 */
public class Task04 {
    public static void main(String[] args) {
        System.out.println("Введите рвбочую папку ");
        String source = "./src/testsFileTask04"; //todo изменить на пользовательский ввод после отладки
        System.out.println("Введите слово для поиска");
        String word = "саша"; //todo изменить на пользовательский ввод после отладки
        File fileForbiddenWords = new File("./src/task04/forbiddenWords.txt");
        FirstThread firstThread = new FirstThread(source, "resultTask04.txt", word);
        SecondThread secondThread = new SecondThread(firstThread, fileForbiddenWords);
        System.out.println();
    }
}

class FirstThread extends Thread {
    private String sourceDirUrl;
    private String resultFileName;
    private File resultFile;
    private String word;

    public FirstThread(String sourceDirName, String resultFileName, String word) {
        this.sourceDirUrl = sourceDirName;
        this.resultFileName = resultFileName;
        this.resultFile = new File(resultFileName);
        if (!resultFile.exists()) {
            try {
                resultFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

        }
        this.word = word;
        start();
    }

    @Override
    public void run() {
        System.out.println("Старт работы потока поиска и копирования текста");
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Path.of(sourceDirUrl))) {
            directoryStream.forEach(this::copyIfHaveWord);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Окончание работы потока поиска и копирования текста");
    }

    private void copyIfHaveWord(Path resultPath) {
        try (BufferedReader br = new BufferedReader(new FileReader(resultPath.toFile()))) {
            String str = "";
            while ((str = br.readLine()) != null) {
                for (String s : str.split("\s+")) {
                    if (s.equals(word)) {
                        copyContent(resultPath.toFile(), resultFile);
                        return;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void copyContent(File inputFile, File outputFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile, true));
             PrintWriter pw = new PrintWriter(bw)) {
            br.lines().forEach(s -> {
                pw.println(s);
                //bw.write(s + "\n");
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getWord() {
        return word;
    }

    public String getSourceDirUrl() {
        return sourceDirUrl;
    }

    public String getResultFileName() {
        return resultFileName;
    }
}

class SecondThread implements Runnable {

    private Thread secondThread;
    private FirstThread firstThread;
    private File fileForbiddenWords;

    public SecondThread(FirstThread firstThread, File fileForbiddenWords) {
        this.firstThread = firstThread;
        this.fileForbiddenWords = fileForbiddenWords;
        this.secondThread = new Thread(this);
        secondThread.start();
    }

    @Override
    public void run() {
        try (BufferedReader brForbiddenWords = new BufferedReader(new FileReader(fileForbiddenWords));
             BufferedReader brSource = new BufferedReader(new FileReader(firstThread.getResultFileName()));
             BufferedWriter bw = new BufferedWriter(new FileWriter(firstThread.getResultFileName()));
             PrintWriter pw = new PrintWriter(bw)) {
            firstThread.join();
            bw.flush();
            System.out.println("Второй поток начал работу");
            String[] forbiddenWords = brForbiddenWords.readLine().split("\s+");
            brSource.lines()
                    .map(s -> s = replaceWord(s, forbiddenWords))
                    .forEach(line -> {
                        pw.println(line.trim());
                        System.out.println("записываем строку : " + line); //todo убрать после отладки
                    });
            System.out.println("Второй поток закончил работу");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String replaceWord(String str, String... words) {
        for (String word : words) {
            str = str.replace(word, "");
        }
        return str;

    }
}

