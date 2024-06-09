package org.example;

import nl.siegmann.epublib.domain.*;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.service.MediatypeService;
import org.example.test.readers.elements.TSection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.ls.LSOutput;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main {

//    public static void main(String[] args) throws IOException {
//
//        //        FB2Reader reader = new FB2Reader();
//
////
////        try (InputStream in =
////                     new FileInputStream(new File("books/stoicizm.fb2"))) {
//
//
//        String fileUrl = "http://flibusta.site/b/712050/fb2";
//
//
//        InputStream in = downloadFile(fileUrl);
//
//
//        if (in != null) {
//            String file = getFB2File(in);
//
//
//            if (file != null) {
//
//
//                FB2Reader reader = new FB2Reader();
//                var book = reader.parse(file);
//
//                System.out.println(book);
//                var sections = book.getSections();
//
//
//                displaySections(sections.stream().limit(5).toList());
//
////                try(FileOutputStream outputStream = new FileOutputStream("books/marshal.fb2")) {
////                    byte[] buffer = new byte[4096]; // Буфер для читання/запису
////                    int bytesRead;
////
////                    while ((bytesRead = file.read(buffer)) != -1) {
////                        outputStream.write(buffer, 0, bytesRead);
////                    }
////                }
//            }else {
//                System.out.println("Щось сталося з отриманням файлу fb2");
//            }
//        }else {
//            System.out.println("Щось сталося з скачуванням файла");
//        }
//
//    }

    public static void main(String[] args) {
        try {

            FileInputStream epubFile = new FileInputStream("books/augustus.epub");
//
//            EPUBReader reader = new EPUBReader();
//
//            var book = reader.parse(epubFile);
//

//            var sections = book.getSections();

            Book book = (new EpubReader()).readEpub(epubFile);

            int img = 1;
            // Отримайте список розділів з таблиці змісту
            for (TOCReference tocReference : book.getTableOfContents().getTocReferences()) {
                // Отримайте ресурс розділу
                Resource chapterResource = tocReference.getResource();

                // Перевірте, чи ресурс не є порожнім і має медіатип XHTML
                if (chapterResource != null && isXHTMLMediaType(chapterResource.getMediaType().getName())) {
                    // Отримайте вміст розділу як рядок
                    String xhtmlContent = new String(chapterResource.getData(), StandardCharsets.UTF_8);

                    // Розберіть вміст XHTML за допомогою Jsoup
                    Document doc = Jsoup.parse(xhtmlContent);

                    // Знайдіть всі теги <img> у XHTML
                    Elements imgElements = doc.select("img");

                    for (Element imgElement : imgElements) {
                        // Отримайте атрибут 'src' тегу <img>, який містить посилання на фотографію
                        String imageUrl = imgElement.attr("src");

                        // Завантажте фотографію з URL
                        byte[] imageBytes = downloadImageBytes(imageUrl);

                        try(FileWriter writer = new FileWriter("images/" + img + ".png")) {
                            writer.write(new String(imageBytes));
                            img++;
                        }
                        // Тепер imageBytes містить фотографію у вигляді байтів
                        // Ви можете опрацьовувати або зберігати ці байти за потребою
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Функція для перевірки медіатипу на належність XHTML
    private static boolean isXHTMLMediaType(String mediaType) {
        return mediaType.equals("application/xhtml+xml");
    }

    // Функція для завантаження фотографії з URL та повернення її у вигляді байтів
    private static byte[] downloadImageBytes(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);

        try (InputStream in = url.openStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }


    private static void printTableOfContents(TableOfContents tableOfContents, String indent) {
        for (TOCReference reference : tableOfContents.getTocReferences()) {
            System.out.println(indent + reference.getTitle());
            try {
                var r = reference.getResource().getInputStream();
                System.out.println(Jsoup.parse(r, "UTF-8", ""));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Рекурсивно виводьте текст для підсторінок
            if (reference.getChildren() != null && !reference.getChildren().isEmpty()) {
                printTableOfContents((TableOfContents) reference.getChildren(), indent + "  ");
            }
        }
    }

    private static String extractChapterTitle(Document document) {
        // Власний код для видобування заголовка глави
        // Наприклад, спробуйте знайти заголовок в якості першого рядка тексту
        Element firstParagraph = document.select("p").first();
        return firstParagraph != null ? firstParagraph.text() : "Без заголовку";
    }

    private static InputStream downloadFile(String fileUrl) {

        try {

            URL url = new URL(fileUrl);

            URLConnection connection;
            connection = url.openConnection();

            return connection.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    private static String getFB2File(InputStream in) {

        try {
//            if (detectDocTypeUsingDetector(in).equals("application/zip")) {

            ZipInputStream zipInputStream = new ZipInputStream(in);
            ZipEntry entry;

            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().endsWith(".fb2")) {
                    System.out.println("here!");
                    System.out.println(entry.getName());
                    // Розпаковка та передача InputStream в метод

                    String fb2Content = readInputStreamAsString(zipInputStream, "UTF-8");

                    return fb2Content;

                }

                zipInputStream.closeEntry();
            }
//            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

//    public static String detectDocTypeUsingDetector(InputStream stream)
//            throws IOException {
//        Tika tika = new Tika();
//        return tika.detect(stream);
//    }

    private static String readInputStreamAsString(InputStream inputStream, String charset) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            return sb.toString();
        }
    }

    private static void displaySections(List<TSection> sections) {
        for (var section : sections) {
            System.out.println(section.getTitle() == null ? "* * *" : section.getTitle());
            System.out.println("-----------------");
            System.out.println(section.getElements());
            System.out.println("=".repeat(10));
        }
    }
}
