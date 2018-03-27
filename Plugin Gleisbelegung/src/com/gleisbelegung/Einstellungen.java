package com.gleisbelegung;

import com.gleisbelegung.lib.Stellwerk;
import com.gleisbelegung.lib.data.Bahnhof;
import com.gleisbelegung.lib.data.Bahnsteig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

public class Einstellungen {
    public static int update = 15;
    public static int vorschau = 60;
    public static int spaltenbreite = 100;
    public static int schriftgroesse = 18;
    public static boolean informationenAnzeigen = true;
    public static int informationenBreite = 300;
    public static String appOrdner;
    public static boolean maximiert = true;
    public static Fenster fenster;
    private File einstellungen;

    public Einstellungen(){
        appOrdner = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "Plugin Gleisbelegung";
        einstellungen = new File(appOrdner + File.separator + "Einstellungen.xml");

        File pluginOrdner = new File(appOrdner);

        if (pluginOrdner.exists()) {
            if (einstellungen.exists()) {
                leseEinstellungen();
            }
        } else if(pluginOrdner.mkdirs()){
            System.out.println("Ordner wurde angelegt, und für Speicherungen vorbereitet");
        } else {
            System.out.println("Fehler beim Erzeugen des Ordners für die Einstellungen!");
        }
    }

    public void schreibeEinstellungen(){
        try {
            if(einstellungen.exists() || einstellungen.createNewFile()){
                BufferedWriter bw = new BufferedWriter(new FileWriter(einstellungen));
                bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                bw.write("<einstellungen version=\"1\">\n");
                bw.write("\t<update>" + update + "</update>\n");
                bw.write("\t<vorschau>" + vorschau + "</vorschau>\n");
                bw.write("\t<spaltenbreite>" + spaltenbreite + "</spaltenbreite>\n");
                bw.write("\t<schriftgroesse>" + schriftgroesse + "</schriftgroesse>\n");
                bw.write("\t<informationenAnzeigen>" + informationenAnzeigen + "</informationenAnzeigen>\n");
                bw.write("\t<informationenBreite>" + informationenBreite + "</informationenBreite>\n");
                bw.write("\t<maximiert>" + maximiert + "</maximiert>\n");
                bw.write("</einstellungen>\n");

                bw.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void leseEinstellungen(){
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(einstellungen);

            Element e = (Element) doc.getElementsByTagName("einstellungen").item(0);
            int xmlVersion = Integer.parseInt(e.getAttribute("version"));
            if(xmlVersion == 1){
                update = Integer.parseInt(e.getElementsByTagName("update").item(0).getTextContent());
                vorschau = Integer.parseInt(e.getElementsByTagName("vorschau").item(0).getTextContent());
                spaltenbreite = Integer.parseInt(e.getElementsByTagName("spaltenbreite").item(0).getTextContent());
                schriftgroesse = Integer.parseInt(e.getElementsByTagName("schriftgroesse").item(0).getTextContent());
                informationenAnzeigen = Boolean.parseBoolean(e.getElementsByTagName("informationenAnzeigen").item(0).getTextContent());
                informationenBreite = Integer.parseInt(e.getElementsByTagName("informationenBreite").item(0).getTextContent());
                maximiert = Boolean.parseBoolean(e.getElementsByTagName("maximiert").item(0).getTextContent());
            } else{
                System.out.println("Einstellungsdatei niicht mehr auf dem neusten Stand. Einstellungen können erst nach einm Klick auf Speichern erneut gelesen werden.");
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            //Tritt auf, wenn neue Attribute gelesen werden, diese aber noch nicht in der Einstellungsdatei existieren
            //e.printStackTrace();
        }
    }
}
