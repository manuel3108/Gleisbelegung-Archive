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
            leseEinstellungen();
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

    public void schreibeStellwerksEinstellungen(Stellwerk stellwerk){
        File stellwerksOrdner = new File(appOrdner + File.separator + "Stellwerke");
        if(!stellwerksOrdner.exists()){
            stellwerksOrdner.mkdir();
        }

        File datei = new File(stellwerksOrdner + File.separator + stellwerk.getAnlagenid() + ".xml");

        try {
            if(datei.exists() || datei.createNewFile()){
                BufferedWriter bw = new BufferedWriter(new FileWriter(datei));
                bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                bw.write("<gleisbelegung version=\"1\">\n");

                for(Bahnhof b : stellwerk.getBahnhoefe()){
                    bw.write("\t<bahnhof>\n");
                    bw.write("\t\t<id>" + b.getId() + "</id>\n");
                    bw.write("\t\t<x>0</x>\n"); //notwendig für 1.7
                    bw.write("\t\t<y>0</y>\n"); //notwendig für 1.7
                    for(Bahnsteig ba : b.getBahnsteige()){
                        bw.write("\t\t<bahnsteig>\n");
                        bw.write("\t\t\t<id>" + ba.getId() + "</id>\n");
                        bw.write("\t\t\t<orderId>" + ba.getOrderId() + "</orderId>\n");
                        bw.write("\t\t\t<sichtbar>" + ba.isSichtbar() + "</sichtbar>\n");
                        bw.write("\t\t</bahnsteig>\n");
                    }
                    bw.write("\t</bahnhof>\n");
                }

                bw.write("</gleisbelegung>");

                bw.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean leseStellwerksEinstellungen(Stellwerk stellwerk){
        File stellwerksOrdner = new File(appOrdner + File.separator + "Stellwerke");
        if(!stellwerksOrdner.exists())return false;

        File datei = new File(stellwerksOrdner + File.separator + stellwerk.getAnlagenid() + ".xml");
        if(!datei.exists()) return false;

        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(datei);

            Element e = (Element) doc.getElementsByTagName("gleisbelegung").item(0);
            int xmlVersion = Integer.parseInt(e.getAttribute("version"));
            if(xmlVersion == 1){
                NodeList bahnhoefe = doc.getElementsByTagName("bahnhof");

                int bahnhofCounter = 0;
                for(Bahnhof b : stellwerk.getBahnhoefe()){
                    Node bahnhofNode = bahnhoefe.item(bahnhofCounter);
                    if(bahnhofNode.getNodeType() == Node.ELEMENT_NODE){
                        Element bahnhof = (Element) bahnhofNode;
                        int x = Integer.parseInt(bahnhof.getElementsByTagName("x").item(0).getTextContent());
                        int y = Integer.parseInt(bahnhof.getElementsByTagName("y").item(0).getTextContent());

                        NodeList bahnsteige = bahnhof.getElementsByTagName("bahnsteig");

                        int bahnsteigCounter = 0;
                        for(Bahnsteig ba : b.getBahnsteige()){
                            Node bahnsteigNode = bahnsteige.item(bahnsteigCounter);

                            if (bahnsteigNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element bahnsteig = (Element) bahnsteigNode;

                                ba.setOrderId(Integer.parseInt(bahnsteig.getElementsByTagName("orderId").item(0).getTextContent()));
                                ba.setSichtbar(Boolean.parseBoolean(bahnsteig.getElementsByTagName("sichtbar").item(0).getTextContent()));
                            }
                            bahnsteigCounter++;
                        }
                    }
                    bahnhofCounter++;
                }

                /*int counter = 0;
                for(Bahnhof b : stellwerk.getBahnhoefe()){
                    for(Bahnsteig ba : b.getBahnsteige()){
                        Node nNode = nList.item(counter);

                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element eElement = (Element) nNode;

                            ba.setOrderId(Integer.parseInt(eElement.getElementsByTagName("orderId").item(0).getTextContent()));
                            ba.setSichtbar(Boolean.parseBoolean(eElement.getElementsByTagName("sichtbar").item(0).getTextContent()));
                        }

                        counter++;
                    }
                }*/
            } else{
                System.out.println("Einstellungsdatei niicht mehr auf dem neusten Stand. Einstellungen können erst nach einm Klick auf Speichern erneut gelesen werden.");
            }

            return true;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            return false;
        } catch (NullPointerException e){
            //Tritt auf, wenn neue Attribute gelesen werden, diese aber noch nicht in der Einstellungsdatei existieren
            //e.printStackTrace();
            return false;
        }
    }
}
