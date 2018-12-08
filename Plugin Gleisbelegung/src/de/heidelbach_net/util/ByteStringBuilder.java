package de.heidelbach_net.util;

import java.nio.charset.Charset;


/**
 * @author interpret
 * @license LGPLv2
 */
class ByteStringBuilder {

    private final byte[] array = new byte[200];
    private final Charset charset;
    private int length = 0;

    /**
     * @param charset Charset to use for encoding in {@link #toString()}
     */
    public ByteStringBuilder(final Charset charset) {
        this.charset = charset;
    }

    /**
     * @param b byte to append
     */
    public void append(final int b) {
        this.array[this.length++] = (byte) b;
    }

    /**
     * Clears the input
     */
    public void clear() {
        this.length = 0;
    }

    /**
     * Returns the number of bytes appended so far. {@link #toString()} may
     * return shorter Strings if multiple bytes encode one char.
     *
     * @return number of bytes contained
     */
    public int length() {
        return this.length;
    }

    /**
     * Converts any bytes read since last call of {@link #clear()} to a
     * {@link String} using the {@link Charset} used upon construction.
     */
    @Override public String toString() {
        return new String(this.array, 0, this.length, this.charset);
    }

}
