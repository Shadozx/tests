package org.example.test.reader;

import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FB2Reader {

    public FB2Book parse(InputStreamReader inputStreamReader) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        InputSource inputSource = new InputSource(inputStreamReader);

        Document doc = dBuilder.parse(inputSource);

        doc.getDocumentElement().normalize();


        Element titleInfoElement = (Element) doc.getElementsByTagName("title-info").item(0);

        String bookTitle = getTitle(titleInfoElement);

        String description = getDescription(titleInfoElement);

        FB2Book book = new FB2Book(bookTitle, description);

        NodeList sectionList = doc.getElementsByTagName("section");


        for (int i = 0; i < sectionList.getLength(); i++) {
            Element sectionElement = getSectionElement(doc, i);
            if (sectionElement != null) {

                FB2Section section = getSection(doc, sectionElement);
                if (section != null) book.addSection(section);
            }

        }

        return book;
    }

    private String getTitle(Element element) {
        Element bookTitleElement = (Element) element.getElementsByTagName("book-title").item(0);

        return bookTitleElement.getTextContent();
    }

    private String getDescription(Element element) {
        Element annotationElement = (Element) element.getElementsByTagName("annotation").item(0);

        StringBuilder descriptionText = new StringBuilder();
        NodeList annotationParagraphs = annotationElement.getElementsByTagName("p");
        for (int i = 0; i < annotationParagraphs.getLength(); i++) {
            Node paragraphNode = annotationParagraphs.item(i);
            if (paragraphNode.getNodeType() == Node.ELEMENT_NODE) {
                Element paragraphElement = (Element) paragraphNode;
                descriptionText.append(paragraphElement.getTextContent()).append("\n");
            }
        }

        return descriptionText.toString();
    }

    public static Element getSectionElement(Document doc, int sectionNumber) {
        NodeList sectionList = doc.getElementsByTagName("section");
        if (sectionNumber >= 0 && sectionNumber < sectionList.getLength()) {
            Node sectionNode = sectionList.item(sectionNumber);
            if (sectionNode.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) sectionNode;
            }
        }
        return null;
    }

    public FB2Section getSection(Document doc, Element sectionElement) {
        FB2Section section = new FB2Section();

        NodeList elements = sectionElement.getChildNodes();

        for (int i = 0; i < elements.getLength(); i++) {
            Node node = elements.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) node;
                if ("title".equals(childElement.getNodeName())) {
//                    System.out.println("Found a <title> element: " + childElement.getTextContent().trim());
                    section.setTitle(childElement.getTextContent().trim());
                    if (childElement.getTextContent().contains("1. По своей природе мы – социальные животные, призванные помогать друг другу")) {
                        System.out.println("here!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    }

                } else if (childElement.getNodeName().equals("image")) {

                    String href = childElement.getAttribute("l:href");

                    System.out.println(href.replace("#", ""));
                    Element binaryElement = doc.getElementById(href.replace("#", ""));
                    if (binaryElement != null) {

                        System.out.println("Image is not null");
                        String contentType = binaryElement.getAttribute("content-type");
                        String imageData = binaryElement.getTextContent();

                        System.out.println(imageData);
//                        if (contentType.startsWith("image")) {

//                        saveImage(imageBytes, "image" + i + ".jpg"); // Зберегти зображення у файл

                        section.addElement(new FB2Image(FB2Type.IMAGE, decodeBase64(imageData)));
//                        }

                    } else System.out.println("Image is null");
                } else {

                    section.addElement(new FB2Text(FB2Type.TEXT, childElement.getTextContent().trim()));
                }
            }
        }
//            for (int i = 0; i < elements.getLength(); i++) {
//                Node node = elements.item(i);
//
//
//                Element element = (Element) node;
//
//            }

        return section;
    }


    public static byte[] decodeBase64(String base64Data) {
        return java.util.Base64.getDecoder().decode(base64Data);
    }


    @Getter
    public static class FB2Book {
        private String title;
        private String description;

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

        public FB2Section() {
        }

        public FB2Section(String title) {
            this.title = title;
        }

        public void addElement(FB2Element element) {
            if (elements == null) elements = new FB2Elements();

            elements.add(element);
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
    }
}
