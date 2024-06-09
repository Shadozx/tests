package org.example.test.readers.elements;

import java.util.ArrayList;

public class TElements extends ArrayList<TElement> {

    @Override
    public String toString() {
        return String.join("\n", super.stream().map(TElement::toString).toList());
    }
}

