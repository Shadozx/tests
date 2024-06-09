package org.example.test.readers.elements;

public class TText extends TElement {
    private String text;

    public TText(String text) {
        super(TType.TEXT);
        this.text = text;
    }


    @Override
    public String toString() {
        return "EPUBText{" +
                "text='" + text + "', " + super.toString() +
                '}';
    }
}
