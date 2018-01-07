package com.gleisbelegung.plugin;
/*
@author: Manuel Serret
@email: manuel-serret@t-online.de
@contact: Email, Github, STS-Forum

Achtung: Dies ist eine höchst sensible Klasse. Vor dem ändern unbedingt ein Backup der Klasse machen, aus diesem Grund gibt es hier auch keine Anmerkungen
 */

import java.io.InputStream;
import java.nio.charset.Charset;
import de.heidelbach_net.util.XML;

public class XMLHandler {
	
    InputStream in;
    
    static private final Charset XML_CHARSET = Charset.forName("UTF-8");

    public XMLHandler(InputStream in){
        this.in = in;
    }

    public XML read(){
        try {
           return XML.read(in, XML_CHARSET);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
