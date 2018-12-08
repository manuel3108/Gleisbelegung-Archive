package com.gleisbelegung.lib;
/*
@author: Manuel Serret
@email: manuel-serret@t-online.de
@contact: Email, Github, STS-Forum

Achtung: Dies ist eine höchst sensible Klasse. Vor dem ändern unbedingt ein Backup der Klasse machen, aus diesem Grund gibt es hier auch keine Anmerkungen
 */

import de.heidelbach_net.util.XML;

import java.io.InputStream;
import java.nio.charset.Charset;


public class XMLHandler {

    static private final Charset XML_CHARSET = Charset.forName("UTF-8");
    InputStream in;

    public XMLHandler(InputStream in) {
        this.in = in;
    }

    public XML read() {
        try {
            return XML.read(in, XML_CHARSET);
        } catch (Exception e) { //Ausgabe entfernt, um Fehler beim Neustart nicht auszugeben, da der Neustart auch mit diesem Fehler funktioniert
            return null;
        }
    }
}
