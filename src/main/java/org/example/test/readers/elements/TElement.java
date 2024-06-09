package org.example.test.readers.elements;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TElement {
    private TType type;

    public TElement(TType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "type=" + type;
    }
}
