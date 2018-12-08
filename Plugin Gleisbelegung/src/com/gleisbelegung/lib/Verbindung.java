package com.gleisbelegung.lib;
/*
@author: Manuel Serret
@email: manuel-serret@t-online.de
@contact: Email, Github, STS-Forum

Hinweis: In jeder Klasse werden alle Klassenvariablen erklärt, sowie jede Methode. Solltest du neue Variablen oder Methoden hinzufügen, vergiss bitte nicht sie zu implementieren.

Erstellt und hält die Verbindung mit der Schnitstelle aufrecht.
 */

import com.gleisbelegung.lib.data.Bahnhof;
import com.gleisbelegung.lib.data.Bahnsteig;
import com.gleisbelegung.lib.data.FahrplanHalt;
import com.gleisbelegung.lib.data.ScheduleFlags;
import com.gleisbelegung.lib.data.Trainlist;
import com.gleisbelegung.lib.data.Zug;
import de.heidelbach_net.util.XML;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Verbindung {
    private Socket socket;       //Java-Socket zur Kommunikation über TCP/IP
    private XMLHandler xml;     //Verarbeitet die Empfangenen Daten in einer Eigenen Klasse
    
    public Verbindung(Socket socket) throws IOException {
    	  this.socket = socket;
          xml = new XMLHandler(socket.getInputStream());
    }
    
    //Führt einige notwendige Kommunikationsschritte mit der Verbindung durch und Verlangt u.a. Uhrzeit und Bahnsteige
    public Stellwerk initialisiere(String pluginName, String autor, int version, String pluginBeschreibung, int protokoll) {
        XML temp = xml.read();
        if (temp != null && Integer.parseInt(temp.get("code")) != 300) {
            System.out.println("Es liegt ein Fehler bei der Anmeldung vor.\n\nPrüfe ob die Plugin-Schnittstelle aktiv ist und ob überhaupt ein Simulator läuft.");
            System.exit(-2);
        }

        if (setSocketCode("<register name=\"" + pluginName + "\" autor=\"" + autor + "\" version=\"" + version + "\" protokoll=\"1\" text=\"" + pluginBeschreibung + "\" />") != 1) {
            System.out.println("Es liegt ein Fehler bei der Anmeldung vor.\n\nPrüfe ob die Plugin-Schnittstelle aktiv ist und ob überhaupt ein Simulator läuft.");
            System.exit(-3);
        }

        temp = xml.read();
        if (temp != null && Integer.parseInt(temp.get("code")) != 220) {
            System.out.println("Anmeldung erfolgreich!\n\nSollte diese Meldung kommen, habe ich etwas falsch Programmiert");
            System.exit(-4);
        }

        if (setSocketCode("<anlageninfo />") != 1) {
            System.out.println("Beim Senden der Daten an die Plugin-Schitstelle ist ein Fehler aufgetreten.");
            System.exit(-5);
        }

        temp = xml.read();
        if (temp == null) {
            System.out.println("Beim Empfangen der Daten von der Plugin-Schitstelle ist ein Fehler aufgetreten.");
            System.exit(-6);
        }
        Stellwerk stellwerk = new Stellwerk(this, temp.get("name"), Integer.parseInt(temp.get("aid")), Integer.parseInt(temp.get("simbuild")));
        System.out.println("Die Verbindung mit dem Stellwerk " + temp.get("name") + " und der Anlagen-Id " + temp.get("aid") + " wurde erfolgreich hergestellt. Aktuelle Simulator-Build: " + temp.get("simbuild"));
        
        long timeBeforeSending = System.currentTimeMillis();
        if (setSocketCode("<simzeit sender='" + timeBeforeSending + "' />") != 1) {
            System.out.println("Beim Senden der Daten an die Plugin-Schitstelle ist ein Fehler aufgetreten.");
            System.exit(-7);
        }

        temp = xml.read();
        if (temp == null) {
            System.out.println("Beim Empfangen der Daten von der Plugin-Schitstelle ist ein Fehler aufgetreten.");
            System.exit(-8);
        }
        stellwerk.spielzeit = ((System.currentTimeMillis() - timeBeforeSending) / 1000) / 2 + Long.parseLong(temp.get("zeit")) - 1000 * 60 * 60;

        if (setSocketCode("<bahnsteigliste />") != 1) {
            System.out.println("Beim Senden der Daten an die Plugin-Schitstelle ist ein Fehler aufgetreten.");
            System.exit(-9);
        }

        temp = xml.read();
        List<XML> bahnsteigeXML = temp.getInternXML();
        String[] bahnsteige = new String[bahnsteigeXML.size()];
        Iterator<XML> bahnsteigIterator = bahnsteigeXML.iterator();
        for (int i = 0; i < bahnsteige.length; i++) {
            XML b = bahnsteigIterator.next();
            bahnsteige[i] = b.get("name");
        }
        stellwerk.erstelleBahnhoefe(bahnsteige);

        bahnsteigIterator = bahnsteigeXML.iterator();
        for (Bahnsteig bahnsteig : stellwerk.getBahnsteige()) {
            XML b = bahnsteigIterator.next();

            List<Bahnsteig> nachbarn = new ArrayList<Bahnsteig>();
            for(XML nachbarXml : b.getInternXML()){
                Bahnsteig nachbar = stellwerk.getBahnsteigByName(nachbarXml.get("name"));
                if(nachbar != null){
                    nachbarn.add(nachbar);
                }
            }
            bahnsteig.setNachbarn(nachbarn);
        }
        /*if (setSocketCode("<wege />") != 1) { //auf der SIM-Seite noch nicht implementiert
            System.out.println(-10, "Beim Senden der Daten an die Plugin-Schitstelle ist ein Fehler aufgetreten.");
            System.exit(-10);
        }*/
        update(stellwerk);
        
        return stellwerk;
    }

    //Sendet Daten-Anfragen an die Plugin-Schnitstelle
    private int setSocketCode(String s) {
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            out.write(s + "\n");
            out.flush();
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    long aktualisiereSimZeit() {
        long timeBeforeSending = System.currentTimeMillis();
        if (setSocketCode("<simzeit sender='" + timeBeforeSending + "' />") != 1) {
            System.out.println("Beim Senden der Daten an die Plugin-Schitstelle ist ein Fehler aufgetreten.");
            System.exit(-7);
        }
        XML temp = xml.read();
        if (temp == null) {
            System.out.println("Beim Empfangen der Daten von der Plugin-Schitstelle ist ein Fehler aufgetreten.");
            System.exit(-8);
        }
        return ((System.currentTimeMillis() - timeBeforeSending) / 1000) / 2 + Long.parseLong(temp.get("zeit")) - 1000 * 60 * 60;
    }

    //Aktualisiert die Daten aller Züge
    public boolean update(Stellwerk stellwerk) {
        try {
            setSocketCode("<zugliste />");
        } catch (Exception e) {
            e.printStackTrace();
        }
        stellwerk.updateZugliste(xml.read());
        
        for (Zug z : stellwerk.getZuege()) {
            try {
                setSocketCode("<zugdetails zid='" + z.getZugId() + "'/>");
                XML zugdetails = xml.read();
                int counter = 0;

                if (zugdetails == null || !zugdetails.getKey().equals("zugdetails")) {
                    // Fehler
                    if (zugdetails.getKey().equals("status")) {
                        System.out.println("INFORMATION: " + z.getZugName() + " hat status 402 \"zid unbekannt\" " + z.getVerspaetungInMinuten() + " " + z.getBahnsteig().getName() + " " + z.getAmGleis() + " " + z.getVon() + " " + z.getNach() + " " + z.getPlangleis().getName() + " " + z.getSichtbar());
                        stellwerk.removeZug(z);
                    }
                    continue;
                } else {
                    String verspaetungString = zugdetails.get("verspaetung");
                    if (verspaetungString != null) {
                        int verspaetung = Integer.parseInt(verspaetungString);
                        if (verspaetung != z.getVerspaetungInMinuten()) {
                            z.setVerspaetung(verspaetung);
                            z.setNeedUpdate(true);
                        }
                        counter++;
                    }
                    String gleis = zugdetails.get("gleis");
                    if (gleis != null) {
                        if (z.getBahnsteig() == null || !gleis.equals(z.getBahnsteig().getName())) {
                            z.setBahnsteig(stellwerk.getBahnsteigByName(gleis));
                            z.setNeedUpdate(true);
                        }
                        counter++;
                    }
                    String amgleisString = zugdetails.get("amgleis");
                    if (amgleisString != null) {
                        boolean amgleis = Boolean.parseBoolean(amgleisString);
                        if (amgleis != z.getAmGleis()) {
                            z.setAmGleis(amgleis);
                            z.setNeedUpdate(true);
                        }
                        counter++;
                    }
                    if (zugdetails.get("von") != null) {
                        if (!zugdetails.get("von").equals(z.getVon())) {
                            z.setVon(zugdetails.get("von"));
                            z.setNeedUpdate(true);
                        }
                        counter++;
                    }
                    if (zugdetails.get("nach") != null) {
                        if (!zugdetails.get("nach").equals(z.getNach())) {
                            z.setNach(zugdetails.get("nach"));
                            z.setNeedUpdate(true);
                        }
                        counter++;
                    }
                    if (zugdetails.get("plangleis") != null) {
                        if (z.getPlangleis() == null || !zugdetails.get("plangleis").equals(z.getPlangleis().getName())) {
                            z.setPlangleis(stellwerk.getBahnsteigByName(zugdetails.get("plangleis")));
                            z.setNeedUpdate(true);
                        }
                        counter++;
                    }
                    if (zugdetails.get("sichtbar") != null) {
                        if (Boolean.parseBoolean(zugdetails.get("sichtbar")) != z.getSichtbar()) {
                            z.setSichtbar(Boolean.parseBoolean(zugdetails.get("sichtbar")));
                            z.setNeedUpdate(true);
                        }
                        counter++;
                    }
                }

                if (counter != 7 && counter != 5) {
                    System.out.println("INFORMATION: " + z.getZugName() + " es wurden nicht alle Daten gesetzt " + z.getVerspaetungInMinuten() + " " + z.getBahnsteig().getName() + " " + z.getAmGleis() + " " + z.getVon() + " " + z.getNach() + " " + z.getPlangleis().getName() + " " + z.getSichtbar());
                } else if (counter == 5) {
                    stellwerk.removeZug(z);
                }

                setSocketCode("<zugfahrplan zid='" + z.getZugId() + "'/>");
                XML zugfahrplan = xml.read();
                if (zugfahrplan == null || !zugfahrplan.getKey().equals("zugfahrplan")) {
                    // Fehler
                } else {
                    List<XML> zugfahrplanEintraege = zugfahrplan.getInternXML();
                    ArrayList<FahrplanHalt> fahrplan = new ArrayList<>();
                    Iterator<XML> fahrplanIterator = zugfahrplanEintraege.iterator();
                    for (int i = 0; i < zugfahrplanEintraege.size(); i++) {
                        try {
                            XML zugfahrplanEintrag = fahrplanIterator.next();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                            try {
                                long time = dateFormat.parse(zugfahrplanEintrag.get("an")).getTime();
                                zugfahrplanEintrag.set("an", String.valueOf(time));
                            } catch (Exception e) {
                                zugfahrplanEintrag.set("an", "0");
                            }

                            try {
                                long time = dateFormat.parse(zugfahrplanEintrag.get("ab")).getTime();
                                zugfahrplanEintrag.set("ab", String.valueOf(time));
                            } catch (Exception e) {
                                zugfahrplanEintrag.set("ab", "0");
                            }
                            fahrplan.add(new FahrplanHalt(Long.parseLong(zugfahrplanEintrag.get("ab")), stellwerk.getBahnsteigByName(zugfahrplanEintrag.get("name")), ScheduleFlags.parse(zugfahrplanEintrag, z, stellwerk.getZugMap()), stellwerk.getBahnsteigByName(zugfahrplanEintrag.get("plan")), Long.parseLong(zugfahrplanEintrag.get("an")), z));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if(z.isNewTrain()){
                        z.setFahrplan(fahrplan);
                    } else if (z.getFahrplan() != null && z.getFahrplan().size() != fahrplan.size()) {
                        z.setNeedUpdate(true);
                    } else if (z.getFahrplan() != null) {
                        for (int i = 0; i < z.getFahrplan().size(); i++) {
                            if (z.getFahrplan(i) != null && fahrplan.get(i) != null) {
                                FahrplanHalt fh = z.getFahrplan(i);
                                if (fh.getAnkuft() != fahrplan.get(i).getAnkuft()) {
                                    fh.setNeedUpdate(true);
                                    fh.setAnkuft(fahrplan.get(i).getAnkuft());
                                } else if (fh.getAbfahrt() != fahrplan.get(i).getAbfahrt()) {
                                    fh.setNeedUpdate(true);
                                    fh.setAbfahrt(fahrplan.get(i).getAbfahrt());
                                } else if (!fh.getBahnsteig().getName().equals(fahrplan.get(i).getBahnsteig().getName())) {
                                    fh.setNeedUpdate(true);
                                    fh.setBahnsteig(fahrplan.get(i).getBahnsteig());
                                } else if (!fh.getPlanBahnsteig().getName().equals(fahrplan.get(i).getPlanBahnsteig().getName())) {
                                    fh.setNeedUpdate(true);
                                    fh.setPlanBahnsteig(fahrplan.get(i).getPlanBahnsteig());
                                } else if (!fh.getFlags().equals(fahrplan.get(i).getFlags())) {
                                    fh.setNeedUpdate(true);
                                }
                            } else {
                                z.getFahrplan(i).setNeedUpdate(true);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("ZUG: " + z.getZugName() + ": Verbindungsfehler!");
                e.printStackTrace();
            }
        }
        
        return true;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public String toString() {
        return "Verbindung{" +
                "socket=" + socket +
                ", xml=" + xml +
                '}';
    }
}
