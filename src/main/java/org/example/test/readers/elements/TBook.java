package org.example.test.readers.elements;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TBook {
    private String title;
    private String description;

    @Setter
    private List<TSection> sections;

    public TBook(String title, String description) {
        this.title = title;
        this.description = description;
    }


    public void addSection(TSection section) {
        if (sections == null) sections = new ArrayList<>();

        sections.add(section);
    }

    @Override
    public String toString() {
        return "TBook{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
