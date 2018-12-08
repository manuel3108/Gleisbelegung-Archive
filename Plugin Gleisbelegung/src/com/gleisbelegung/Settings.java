package com.gleisbelegung;

import com.gleisbelegung.lib.SignalBox;
import com.gleisbelegung.lib.data.Platform;
import com.gleisbelegung.lib.data.Station;
import com.sun.javafx.geom.Vec2d;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class Settings {

    public static int updateIntervall = 15;
    public static int columnWidth = 100;
    public static int fontSize = 18;
    public static boolean showTrainInformations = true;
    public static int trainInformationWidth = 300;
    public static int view;
    public static String applicationFolder;
    public static boolean fullscreen = true;
    public static Fenster window;
    private File settingsFile;

    public Settings() {
        applicationFolder =
                System.getProperty("user.home") + File.separator + "Documents"
                        + File.separator + "Plugin Gleisbelegung";
        settingsFile =
                new File(applicationFolder + File.separator + "Settings.xml");

        File pluginOrdner = new File(applicationFolder);

        if (pluginOrdner.exists()) {
            if (settingsFile.exists()) {
                leseEinstellungen();
            }
        } else if (pluginOrdner.mkdirs()) {
            System.out.println(
                    "Ordner wurde angelegt, und für Speicherungen vorbereitet");
        } else {
            System.out.println(
                    "Fehler beim Erzeugen des Ordners für die Settings!");
        }
    }

    public void schreibeEinstellungen() {
        try {
            if (settingsFile.exists() || settingsFile.createNewFile()) {
                BufferedWriter bw =
                        new BufferedWriter(new FileWriter(settingsFile));
                bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                bw.write("<settings version=\"1\">\n");
                bw.write("\t<updateIntervall>" + updateIntervall + "</updateIntervall>\n");
                bw.write("\t<columnWidth>" + columnWidth
                        + "</columnWidth>\n");
                bw.write("\t<fontSize>" + fontSize
                        + "</fontSize>\n");
                bw.write("\t<showTrainInformations>" + showTrainInformations
                        + "</showTrainInformations>\n");
                bw.write("\t<trainInformationWidth>" + trainInformationWidth
                        + "</trainInformationWidth>\n");
                bw.write("\t<fullscreen>" + fullscreen + "</fullscreen>\n");
                bw.write("</settings>\n");

                bw.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void leseEinstellungen() {
        try {
            DocumentBuilder dBuilder =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(settingsFile);

            Element e =
                    (Element) doc.getElementsByTagName("settings").item(0);
            int xmlVersion = Integer.parseInt(e.getAttribute("version"));
            if (xmlVersion == 1) {
                updateIntervall = Integer.parseInt(
                        e.getElementsByTagName("updateIntervall").item(0)
                                .getTextContent());
                columnWidth = Integer.parseInt(
                        e.getElementsByTagName("columnWidth").item(0)
                                .getTextContent());
                fontSize = Integer.parseInt(
                        e.getElementsByTagName("fontSize").item(0)
                                .getTextContent());
                showTrainInformations = Boolean.parseBoolean(
                        e.getElementsByTagName("showTrainInformations").item(0)
                                .getTextContent());
                trainInformationWidth = Integer.parseInt(
                        e.getElementsByTagName("trainInformationWidth").item(0)
                                .getTextContent());
                fullscreen = Boolean.parseBoolean(
                        e.getElementsByTagName("fullscreen").item(0)
                                .getTextContent());
            } else {
                System.out.println(
                        "Einstellungsdatei niicht mehr auf dem neusten Stand. Settings können erst nach einm Klick auf Speichern erneut gelesen werden.");
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            //Tritt auf, wenn neue Attribute gelesen werden, diese aber noch nicht in der Einstellungsdatei existieren
            //e.printStackTrace();
        }
    }

    public void schreibeStellwerksEinstellungen(SignalBox signalBox) {
        File stellwerksOrdner =
                new File(applicationFolder + File.separator + "Stellwerke");
        if (!stellwerksOrdner.exists()) {
            stellwerksOrdner.mkdir();
        }

        File datei = new File(
                stellwerksOrdner + File.separator + signalBox.getSignalBoxId()
                        + ".xml");

        try {
            if (datei.exists() || datei.createNewFile()) {
                BufferedWriter bw = new BufferedWriter(new FileWriter(datei));
                bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                bw.write("<gleisbelegung version=\"2\">\n");

                for (Station b : signalBox.getStations()) {
                    bw.write("\t<bahnhof>\n");
                    bw.write("\t\t<id>" + b.getId() + "</id>\n");
                    bw.write("\t\t<x>" + b.getPosition().x
                            + "</x>\n"); //notwendig für 1.6
                    bw.write("\t\t<y>" + b.getPosition().y
                            + "</y>\n"); //notwendig für 1.6
                    bw.write("\t\t<alternativName>" + b.getNameByUser()
                            + "</alternativName>\n");
                    for (Platform ba : b.getPlatforms()) {
                        bw.write("\t\t<bahnsteig>\n");
                        bw.write("\t\t\t<id>" + ba.getId() + "</id>\n");
                        bw.write("\t\t\t<orderId>" + ba.getOrderId()
                                + "</orderId>\n");
                        bw.write("\t\t\t<sichtbar>" + ba.getVisible()
                                + "</sichtbar>\n");
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

    public boolean leseStellwerksEinstellungen(SignalBox signalBox) {
        File stellwerksOrdner =
                new File(applicationFolder + File.separator + "Stellwerke");
        if (!stellwerksOrdner.exists())
            return false;

        File datei = new File(
                stellwerksOrdner + File.separator + signalBox.getSignalBoxId()
                        + ".xml");
        if (!datei.exists())
            return false;

        try {
            DocumentBuilder dBuilder =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(datei);

            Element e =
                    (Element) doc.getElementsByTagName("gleisbelegung").item(0);
            int xmlVersion = Integer.parseInt(e.getAttribute("version"));
            NodeList bahnhoefe = doc.getElementsByTagName("bahnhof");

            if (xmlVersion == 1) {
                leseStellwerksEinstellungenVersionEins(bahnhoefe, signalBox);
            } else if (xmlVersion == 2) {
                leseStellwerksEinstellungenVersionEins(bahnhoefe, signalBox);
                leseStellwerksEinstellungenVersionZwei(bahnhoefe, signalBox);
            } else {
                System.out.println(
                        "Einstellungsdatei niicht mehr auf dem neusten Stand. Settings können erst nach einm Klick auf Speichern erneut gelesen werden.");
            }

            return true;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            return false;
        } catch (NullPointerException e) {
            //Tritt auf, wenn neue Attribute gelesen werden, diese aber noch nicht in der Einstellungsdatei existieren
            //e.printStackTrace();
            return false;
        }
    }

    private void leseStellwerksEinstellungenVersionEins(NodeList bahnhoefe,
            SignalBox signalBox) {
        int bahnhofCounter = 0;
        for (Station b : signalBox.getStations()) {
            Node bahnhofNode = bahnhoefe.item(bahnhofCounter);
            if (bahnhofNode.getNodeType() == Node.ELEMENT_NODE) {
                Element bahnhof = (Element) bahnhofNode;
                double x = Double.parseDouble(
                        bahnhof.getElementsByTagName("x").item(0)
                                .getTextContent());
                double y = Double.parseDouble(
                        bahnhof.getElementsByTagName("y").item(0)
                                .getTextContent());
                b.setPosition(new Vec2d(x, y));

                NodeList bahnsteige = bahnhof.getElementsByTagName("bahnsteig");

                int bahnsteigCounter = 0;
                for (Platform ba : b.getPlatforms()) {
                    Node bahnsteigNode = bahnsteige.item(bahnsteigCounter);

                    if (bahnsteigNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element bahnsteig = (Element) bahnsteigNode;

                        ba.setOrderId(Integer.parseInt(
                                bahnsteig.getElementsByTagName("orderId")
                                        .item(0).getTextContent()));
                        ba.setVisible(Boolean.parseBoolean(
                                bahnsteig.getElementsByTagName("sichtbar")
                                        .item(0).getTextContent()));
                    }
                    bahnsteigCounter++;
                }
            }
            bahnhofCounter++;
        }
    }

    private void leseStellwerksEinstellungenVersionZwei(NodeList bahnhoefe,
            SignalBox signalBox) {
        int bahnhofCounter = 0;
        for (Station b : signalBox.getStations()) {
            Node bahnhofNode = bahnhoefe.item(bahnhofCounter);
            if (bahnhofNode.getNodeType() == Node.ELEMENT_NODE) {
                Element bahnhof = (Element) bahnhofNode;

                b.setNameByUser(
                        bahnhof.getElementsByTagName("alternativName").item(0)
                                .getTextContent());
            }
            bahnhofCounter++;
        }
    }
}
