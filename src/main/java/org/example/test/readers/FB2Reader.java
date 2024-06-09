package org.example.test.readers;

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

public class FB2Reader {


    public TBook parse(InputStream inputStream) throws IOException {

        return parseBook(Jsoup.parse(inputStream, "UTF-8", "", Parser.xmlParser()));
    }

    public TBook parse(String strB) {
        return parseBook(Jsoup.parse(strB, "", Parser.xmlParser()));
    }

    private TBook parseBook(Document doc) {

        Element titleInfo = doc.getElementsByTag("title-info").get(0);

        var book = getBook(titleInfo);

        if (book == null) return null;


        var sections = getSections(doc);
        book.setSections(sections);


        return book;
    }

    private TBook getBook(Element element) {

        Element bookTitle = element.selectFirst("book-title");


        Element annotation = element.selectFirst("annotation");


        return bookTitle != null && annotation != null ? new TBook(bookTitle.text(), annotation.text()) : null;
    }

    private Stack<TSection> getSections(Document doc) {
        Stack<TSection> sections = new Stack<>();

        var sectionsElements = doc.getElementsByTag("section");

        for (var sectionElement : sectionsElements) {
            for (var element : sectionElement.children().not("section")) {

                addSection(sections, doc, element);
            }
        }

        return sections;
    }

    private void addSection(Stack<TSection> sections, Document doc, Element element) {
        if (element.tagName().equals("title")) {

            var prev = !sections.isEmpty() ? sections.pop() : new TSection();


            // якщо існує глава але має назва глави но немає тексту
            if (!prev.isTitleEmpty() && prev.isTextEmpty()) {
                prev.addTitle(element.text().trim());
                sections.push(prev);
            } else {
                sections.push(prev);

                var current = new TSection();

                current.addTitle(element.text().trim());

                sections.push(current);
            }
        } else {

            if (element.tagName().equals("image")) {

                String href = element.attr("l:href");

                Element binaryElement = doc.getElementById(href.replace("#", ""));
                if (binaryElement != null) {

                    var prev = !sections.isEmpty() ? sections.pop() : new TSection();

                    String imageData = binaryElement.text();

                    byte[] data = decodeBase64(imageData);

//                            File file = new File("images/" + UUID.randomUUID() + ".png");
//
//                            if (!file.exists()) {
//                                try {
//                                    file.createNewFile();
//                                } catch (IOException e) {
//                                    System.out.println("Errro!");
//                                }
//                            }

//                            try (FileOutputStream writer = new FileOutputStream(file)) {
//                                writer.write(data);
//                            } catch (IOException e) {
//                                throw new RuntimeException(e);
//                            }

                    prev.addElement(new TImage(data));

                    sections.push(prev);
                }
            } else if (List.of("epigraph", "cite", "emphasis").contains(element.tagName())) {
                var prev = !sections.isEmpty() ? sections.pop() : new TSection();

                prev.addElement(new TText(element.tagName("i").text(element.text()).toString()));

                sections.push(prev);
            } else if (List.of("strong").contains(element.tagName())) {
                var prev = !sections.isEmpty() ? sections.pop() : new TSection();

                prev.addElement(new TText(element.tagName("b").text(element.text()).toString()));

                sections.push(prev);
            } else {
                var prev = !sections.isEmpty() ? sections.pop() : new TSection();

                prev.addElement(new TText(element.text().trim()));

                sections.push(prev);
            }
        }
    }

    private List<TSection> formatSections(List<TSection> sections) {
        Stack<TSection> stack = new Stack<>();


        for (int i = 0; i < sections.size(); i++) {
            var current = sections.get(i);

            if (current != null) {
                if (i == 0 && current.isTitleEmpty()) {

                    current.addTitle("* * *");
                    stack.push(current);
                } else if (current.isTitleEmpty()) {
                    var prev = !stack.isEmpty() ? stack.pop() : new TSection();


                    prev.addElements(current);
                    stack.push(prev);

                } else stack.push(current);
            }
        }

        return stack;
    }

    public static byte[] decodeBase64(String base64Data) {
        return java.util.Base64.getDecoder().decode(base64Data);
    }


    /*@Getter
    public static class FB2Book {
        private String title;
        private String description;

        @Setter
        private List<FB2Section> sections;

        public FB2Book(String title, String description) {
            this.title = title;
            this.description = description;
        }


        public void addSection(FB2Section section) {
            if (sections == null) sections = new ArrayList<>();

            sections.add(section);
        }

        @Override
        public String toString() {
            return "FB2Book{" +
                    "title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }

    @Getter
    @Setter
    public static class FB2Section {
        private String title;

        private FB2Elements elements;

        public void addElement(FB2Element element) {
            if (elements == null) elements = new FB2Elements();

            elements.add(element);
        }

        public void addTitle(String title) {
            this.title = isTitleEmpty() ? title : "\n" + title;
        }

        public void addElements(FB2Section section) {
            if (this.elements == null) this.elements = new FB2Elements();

            if (!section.isTextEmpty()) {
                this.elements.addAll(section.getElements());
            }
        }

        public boolean isTitleEmpty() {
            return title == null || title.equals("");
        }

        public boolean isTextEmpty() {
            return elements == null || elements.isEmpty();
        }


        public boolean isEmpty() {
            return isTitleEmpty() && isTextEmpty();
        }

        @Override
        public String toString() {
            return "FB2Section{" +
                    "title='" + title + '\'' +
                    ", elements=" + elements +
                    '}';
        }
    }

    public static class FB2Elements extends ArrayList<FB2Element> {

        @Override
        public String toString() {
            return String.join("\n", super.stream().map(FB2Element::toString).toList());
        }
    }

    @Getter
    @Setter
    private static class FB2Element {
        private FB2Type type;

        public FB2Element(FB2Type type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "type=" + type;
        }
    }

    @Getter
    @Setter
    private static class FB2Text extends FB2Element {
        private String text;

        public FB2Text(FB2Type type, String text) {
            super(type);
            this.text = text;
        }


        @Override
        public String toString() {
            return "FB2Text{" +
                    "text='" + text + "', " + super.toString() +
                    '}';
        }
    }

    @Getter
    @Setter
    private static class FB2Image extends FB2Element {
        private byte[] data;

        public FB2Image(FB2Type type, byte[] data) {
            super(type);
            this.data = data;
        }

        @Override
        public String toString() {
            return "FB2Image{" +
                    "data=" + data.length +
                    ", " + super.toString() +
                    '}';
        }
    }

    private enum FB2Type {
        IMAGE, TEXT;
    }*/
}

