package org.deidentifier.arx.framework.check.distribution;

import java.io.Serializable;
import java.util.Arrays;

public class IntArrayWrapper implements Comparable<IntArrayWrapper>, Serializable {

    private static final long serialVersionUID = 7711032544501833155L;
    private final int[]       array;
    private final int         hashCode;

    public IntArrayWrapper(final int[] array) {
        this.array = array;
        this.hashCode = Arrays.hashCode(array);
    }

    @Override
    public final boolean equals(final Object obj) {
        return Arrays.equals(array, ((IntArrayWrapper) obj).array);
    }

    public final int[] getArray() {
        return array;
    }

    @Override
    public final int hashCode() {
        return hashCode;
    }

    @Override
    public final String toString() {
        return Arrays.toString(array);
    }

    @Override
    public int compareTo(IntArrayWrapper o) {

        // TODO really???
        if (array.length > o.array.length) { return +1; }
        if (array.length < o.array.length) { return -1; }

        // negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
        for (int i = 0; i < array.length; i++) {
            if (array[i] > o.array[i]) { return +1; }
            if (array[i] < o.array[i]) { return -1; }
        }

        return 0;
    }
}
