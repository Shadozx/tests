package org.example.test.readers.elements;

import lombok.Getter;
import lombok.Setter;
import org.example.test.readers.EPUBReader;
import org.example.test.readers.elements.TElement;
import org.example.test.readers.elements.TElements;

@Getter
@Setter
public class TSection {
    private String title;

    private TElements elements;

    public void addElement(TElement element) {
        if (elements == null) elements = new TElements();

        elements.add(element);
    }

    public void addTitle(String title) {
        this.title = isTitleEmpty() ? title : "\n" + title;
    }

    public void addElements(TSection section) {
        if (this.elements == null) this.elements = new TElements();

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
        return "EPUBSection{" +
                "title='" + title + '\'' +
                ", elements=" + elements +
                '}';
    }
}
