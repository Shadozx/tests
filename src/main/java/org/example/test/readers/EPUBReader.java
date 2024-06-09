package org.example.test.readers;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;
import org.example.test.readers.elements.TBook;
import org.example.test.readers.elements.TImage;
import org.example.test.readers.elements.TSection;
import org.example.test.readers.elements.TText;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Stack;

public class EPUBReader {


    public TBook parse(InputStream inputStream) throws IOException {


        Book book = getEpubReader()
                .readEpub(inputStream);
        return parseBook(book);
    }

    private EpubReader getEpubReader() {
        return new EpubReader();
    }

    public TBook parse(String strB) {


//        return parseBook(Jsoup.parse(strB, "", Parser.xmlParser()));
        return null;
    }

    private TBook parseBook(Book book) {

        var epubBook = getBook(book);


        if (epubBook == null) return null;

        try {
            epubBook.setSections(getSections(book));
        } catch (IOException e) {
            System.out.println("Error with getting sections");
        }

//        Book book = (new EpubReader()).readEpub(epubFile);
//
////            System.out.println("Authors:" + book.getMetadata().getAuthors());
//
////            System.out.println(book.getMetadata().getTitles());
//        for (var s : book.getSpine().getSpineReferences().stream().limit(5).toList()) {
//            var r = s.getResource();
//
//            Document doc = Jsoup.parse(new String(r.getData(), "UTF-8"), "", Parser.xmlParser());
//
//
//            unwrapElement(doc, "span");
//
//            deleteElements(doc, "p.empty-line");
//            for (var e : doc.body().children()) {
//
//
//                if (e.className().equals("title1")) {
//                    System.out.println("Title - " + e.text());
//                } else {
//                    System.out.println(e.text());
//                }
//            }
//            System.out.println("=".repeat(10));
//        }
//        // Закрийте EPUB-файл
//        epubFile.close();

//        return book;
        return epubBook;
    }

    private TBook getBook(Book book) {

        String bookTitle = book.getTitle();
        String annotation = String.join(" ", book.getMetadata().getDescriptions());

        return bookTitle != null ? new TBook(bookTitle, annotation) : null;
    }

    private List<TSection> getSections(Book book) throws IOException {

        Stack<TSection> sections = new Stack<>();
        for (var s : book.getSpine().getSpineReferences()) {
            var r = s.getResource();

            if (r.getMediaType().getName().startsWith("image")){
                System.out.println("here");
            }

//            if (r.getMediaType().getName().equals("img")) {
//
//                System.out.println(r.getHref());
//            } else {

            Document doc = Jsoup.parse(new String(r.getData(), "UTF-8"), "", Parser.xmlParser());


            unwrapElement(doc, "span");

            deleteElements(doc, "p.empty-line");
            for (var e : doc.body().children()) {

                addSection(sections, doc, e);

            }
//            }
        }

        return sections;

    }


    private void addSection(Stack<TSection> sections, Document doc, Element element) {
        if (element.className().equals("title1")) {

            var prev = !sections.isEmpty() ? sections.pop() : new TSection();


            // якщо існує глава але має назва глави но немає тексту
            if (!prev.isTitleEmpty() && prev.isTextEmpty()) {
                prev.addTitle(element.text().trim());
                sections.push(prev);
            } else if (element.text().matches("^\\d+$")) {
                prev.addElement(new TText(element.text()));
                sections.push(prev);
            } else {
                sections.push(prev);

                var current = new TSection();

                current.addTitle(element.text().trim());

                sections.push(current);
            }
        } else {

            if (element.tagName().equals("div") && element.className().equals("image")) {

                System.out.println("Book image");
                System.out.println("HERE!");

            } else {
                var prev = !sections.isEmpty() ? sections.pop() : new TSection();

                prev.addElement(new TText(element.text().trim()));

                sections.push(prev);
            }
        }
    }

    private void unwrapElement(Document doc, String element) {
        for (var e : doc.select(element)) {
            e.unwrap();
        }
    }

    private void deleteElements(Document doc, String element) {
        doc.select(element).remove();
    }

}
