package de.heidelbach_net.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;


/**
 * @author interpret
 * @license LGPLv2
 */
public class XML {

    private final String key;
    private final String data;
    private final Map<String, String> keyValuePairs;
    private final List<XML> subXML;

    private XML(final String key, final String data,
            final Map<String, String> keyValuePairs, final List<XML> subXML) {
        this.key = key;
        this.keyValuePairs = new HashMap<>(
                keyValuePairs == null ? Collections.emptyMap() : keyValuePairs);
        this.subXML = subXML == null ?
                null :
                (subXML.isEmpty() ? null : new ArrayList<>(subXML));
        if (data == null)
            this.data = null;
        else if (data.isEmpty())
            this.data = null;
        else
            this.data = data;
    }

    /**
     * @param string a valid textual representation of xML
     * @return parsed XML
     * @throws MalformedXMLException if given string violates XML syntax
     */
    public static XML parse(final String string) throws MalformedXMLException {
        try {
            return XML.read(new ByteArrayInputStream(string.getBytes()),
                    Charset.defaultCharset());
        } catch (final IOException e) {
            throw new MalformedXMLException();
        }
    }

    /**
     * @param key           Key
     * @param keyValuePairs CDATA part of XML expression to construct
     * @param subXML        contained XML
     * @return parsed XML
     * @see #parse(String, String, Map, List)
     */
    public static XML parse(final String key,
            final Map<String, String> keyValuePairs, final List<XML> subXML) {
        return XML.parse(key, null, keyValuePairs, subXML);
    }

    /**
     * @param key           Key
     * @param data          DATA part of XML expression to construct
     * @param keyValuePairs CDATA part of XML expression to construct
     * @param subXML        contained XML
     * @return parsed XML
     * @see #parse(String, String, Map, List)
     */
    public static XML parse(final String key, final String data,
            final Map<String, String> keyValuePairs, final List<XML> subXML) {
        return new XML(key, data, keyValuePairs, subXML);
    }

    /**
     * Reads from given {@link InputStream} bytes encoded in given Charset until a
     * full XML expression can be retrieved or the so far read bytes violate the
     * XML syntax.
     *
     * @param in      stream to read from
     * @param charset Charset to use for decoding
     * @return parsed XML
     * @throws IOException           if an I/O-error occurs while reading from
     * @throws MalformedXMLException if the read bytes violate XML syntax
     */
    public static XML read(final InputStream in, final Charset charset)
            throws IOException, MalformedXMLException {
        return read_(in, charset);
    }

    private static XML read_(final InputStream in, final Charset charset,
            final char... stack) throws IOException, MalformedXMLException {
        boolean complete = false;
        final ByteStringBuilder sbName = new ByteStringBuilder(charset);
        final ByteStringBuilder sbKey = new ByteStringBuilder(charset);
        final ByteStringBuilder sbValue = new ByteStringBuilder(charset);
        final ByteStringBuilder sbIntern = new ByteStringBuilder(charset);
        final ByteStringBuilder sbData = new ByteStringBuilder(charset);
        final ByteStringBuilder sbEnd = new ByteStringBuilder(charset);
        final Map<String, String> keyValues = new HashMap<>();
        final List<XML> subXMLs = new ArrayList<>();

        boolean potentialXML = false;
        XMLParseState phase = XMLParseState.START;
        int stack_offset = 0;
        while (!complete) {
            final int read;
            if (stack_offset < stack.length) {
                read = stack[stack_offset++];
            } else {
                read = in.read();
                if (read < 0) {
                    // EOF
                    return null;
                }
                if (read == '\n' || read == '\r') {
                    continue;
                }
            }

            switch (phase) {
            case START:
                if (read == '\n') {
                    continue;
                }
                if (read != '<') {
                    throw new MalformedXMLException();
                }
                phase = XMLParseState.INIT;
                continue;
            case INIT:
                if (read == ' ') {
                    if (sbName.length() == 0) {
                        continue;
                    }
                    phase = XMLParseState.KEY_VALUES;
                } else if (read == '>') {
                    phase = XMLParseState.INTERN_START;
                } else if (read == '/') {
                    phase = XMLParseState.END;
                } else {
                    sbName.append(read);
                }
                continue;
            case KEY:
                if (read == '=') {
                    phase = XMLParseState.VALUE_START;
                } else {
                    sbKey.append(read);
                }
                continue;
            case VALUE_START:
                if (read == '\"') {
                    phase = XMLParseState.VALUE_2;
                } else if (read == '\'') {
                    phase = XMLParseState.VALUE_1;
                } else if (read != ' ') {
                    throw new MalformedXMLException();
                }
                continue;
            case VALUE_1:
            case VALUE_2:
                if (((read == '\"') && (phase == XMLParseState.VALUE_2)) || (
                        (read == '\'') && (phase == XMLParseState.VALUE_1))) {
                    phase = XMLParseState.KEY_VALUES;
                    keyValues.put(sbKey.toString(), sbValue.toString());
                    sbKey.clear();
                    sbValue.clear();
                    continue;
                }
                sbValue.append(read);
                continue;
            case KEY_VALUES:
                if (read == ' ') {
                    continue;
                }
                if (read == '>') {
                    phase = XMLParseState.INTERN_START;
                    continue;
                }
                if (read == '/') {
                    phase = XMLParseState.END;
                    continue;
                }
                sbKey.append(read);
                phase = XMLParseState.KEY;
                continue;
            case INTERN_START:
                if (read == ' ') {
                    continue;
                }
                if (read == '<') {
                    phase = XMLParseState.INTERN_DATA;
                    potentialXML = true;
                } else {
                    sbIntern.append(read);
                    phase = XMLParseState.INTERN_NAME;
                }
                continue;

            case INTERN_DATA:
                if (read == '<') {
                    phase = XMLParseState.INTERN_DATA_END;
                } else if (read == '/') {
                    phase = XMLParseState.END_NAME;
                } else if (potentialXML) {
                    try {
                        final XML subXML =
                                XML.read_(in, charset, '<', (char) read);
                        if (subXML == null) {
                            // EOF
                            return null;
                        }
                        subXMLs.add(subXML);
                        phase = XMLParseState.INTERN_START;
                    } catch (final MalformedXMLException e) {
                        throw new MalformedXMLException();
                    }

                } else {
                    sbData.append(read);
                }
                potentialXML = false;
                continue;
            case INTERN_DATA_END:
                if (read == ' ') {
                    continue;
                } else if (read != '/') {
                    throw new MalformedXMLException();
                }
                phase = XMLParseState.END_NAME;
                continue;

            case INTERN_NAME:
                if (read == '<') {
                    phase = XMLParseState.INTERN_NAME_END;
                    continue;
                }
                if (read == '/') {
                    throw new MalformedXMLException();
                }
                sbIntern.append(read);
                continue;

            case INTERN_NAME_END:
                if (read != '/') {
                    throw new MalformedXMLException();
                }
                phase = XMLParseState.END_NAME;
                continue;

            case INTERN:
                throw new RuntimeException("unimplemented case");
                // continue;

            case END_NAME:
                if ((read == ' ') || (read == '>')) {
                    if ((read == ' ') && (sbEnd.length() == 0)) {
                        continue;
                    }
                    if (!sbEnd.toString().equals(sbName.toString())) {
                        throw new MalformedXMLException();
                    }
                    complete = true;
                    continue;
                }
                sbEnd.append(read);
                continue;

            case END:
                if (read == ' ') {
                    continue;
                }
                if (read != '>') {
                    throw new MalformedXMLException();
                }
                complete = true;
                continue;
            default:
                throw new RuntimeException();
            }
        }

        final String name = sbName.toString();
        if (name == null)
            throw new MalformedXMLException();
        final String intern = sbIntern.toString();
        return XML.parse(name, intern, keyValues, subXMLs);
    }

    public static XML generateEmptyXML(final String key) {
        return new XML(key, null, null, null);
    }

    @Override public boolean equals(final Object o) {
        if (o == null)
            return false;
        if (XML.class.isAssignableFrom(o.getClass())) {
            final XML oXML = (XML) o;
            if (!this.key.equals(oXML.key))
                return false;
            if (this.data == null ^ oXML.data == null)
                return false;
            if (this.subXML == null ^ oXML.subXML == null)
                return false;
            if (this.data != null && !this.data.equals(oXML.data))
                return false;
            if (!this.keyValuePairs.equals(oXML.keyValuePairs))
                return false;
            if (this.subXML != null && !this.subXML.equals(oXML.subXML))
                return false;
            return true;
        }
        return false;
    }

    @Override public int hashCode() {
        return toString().hashCode();

    }

    /**
     * @param key the key within CDATA
     * @return the corresponding value to given key
     */
    public String get(final String key) {
        return this.keyValuePairs.get(key);
    }

    /**
     * Encodes this XML expression to corresponding bytes for given Charset
     *
     * @param charset Charset to use for encoding
     * @return encoded bytes representing this XML
     */
    public byte[] getBytes(final Charset charset) {
        return toString().getBytes(charset);
    }

    /**
     * @return DATA of this XML
     */
    public String getData() {
        return this.data;
    }

    /**
     * @return a list of XML contained within this XML
     */
    public List<XML> getInternXML() {
        if (this.subXML == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(this.subXML);
    }

    /**
     * @return the tag of this XML
     */
    public String getKey() {
        return this.key;
    }

    /**
     * @param newKey new tag name
     * @return XML with tag renamed to newKey
     */
    public XML setKey(final String newKey) {
        return new XML(newKey, this.data, this.keyValuePairs, this.subXML);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @SuppressWarnings("javadoc") @Override public final String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("<");
        sb.append(this.key);
        for (final Map.Entry<String, String> entry : this.keyValuePairs
                .entrySet()) {
            sb.append(" ");
            sb.append(entry.getKey());
            sb.append("=\"");
            sb.append(entry.getValue());
            sb.append("\"");
        }
        if ((this.subXML == null) && (this.data == null)) {
            sb.append(" />");
        } else {
            sb.append(" >");
            if (this.data != null) {
                sb.append(this.data);
            }
            if (this.subXML != null) {
                for (final XML xml : this.subXML) {
                    sb.append(xml.toString());
                }
            }
            sb.append("</");
            sb.append(this.key);
            sb.append(">");
        }
        return sb.toString();
    }

    public void set(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        this.keyValuePairs.put(key, value);
    }

    enum XMLParseState {
        START, INIT, KEY_VALUES, KEY, VALUE_START, VALUE_1, VALUE_2, INTERN_START, INTERN_NAME, INTERN_NAME_END, INTERN_DATA, INTERN_DATA_END, INTERN, END_NAME, END;
    }
}
