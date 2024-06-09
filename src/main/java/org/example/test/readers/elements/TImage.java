package org.example.test.readers.elements;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TImage extends TElement {
    private byte[] data;

    public TImage(byte[] data) {
        super(TType.IMAGE);
        this.data = data;
    }

    @Override
    public String toString() {
        return "TImage{" +
                "data=" + data.length +
                ", " + super.toString() +
                '}';
    }
}
