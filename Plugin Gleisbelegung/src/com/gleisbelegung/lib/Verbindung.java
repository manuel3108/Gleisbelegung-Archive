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
    private boolean aktualisiere;

    private Stellwerk stellwerk;

    //Führt einige notwendige Kommunikationsschritte mit der Verbindung durch und Verlangt u.a. Uhrzeit und Bahnsteige
    Verbindung(Socket socket, Stellwerk stellwerk, String pluginName, String pluginBeschreibung, String autor, int version) {
        this.stellwerk = stellwerk;

        try {
            this.socket = socket;
            xml = new XMLHandler(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("Es liegt ein Fehler bei der Anmeldung vor.\n\nPrüfe ob die Plugin-Schnittstelle aktiv ist und ob überhaupt ein Simulator läuft.");
            System.exit(-1);
        }

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
        stellwerk.simbuild = Integer.parseInt(temp.get("simbuild"));
        stellwerk.stellwerksname = temp.get("name");
        stellwerk.anlagenid = Integer.parseInt(temp.get("aid"));
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
            bahnsteige[i] = bahnsteigIterator.next().get("name");
        }
        stellwerk.erstelleBahnhoefe(bahnsteige);

        /*if (setSocketCode("<wege />") != 1) { //auf der SIM-Seite noch nicht implementiert
            System.out.println(-10, "Beim Senden der Daten an die Plugin-Schitstelle ist ein Fehler aufgetreten.");
            System.exit(-10);
        }*/
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

    public void aktualisiereSimZeit() {
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
        stellwerk.spielzeit = ((System.currentTimeMillis() - timeBeforeSending) / 1000) / 2 + Long.parseLong(temp.get("zeit")) - 1000 * 60 * 60;
    }

    //Aktualisiert die Daten aller Züge
    public void update() {
        stellwerk.letzteAktualisierung = System.currentTimeMillis();
        aktualisiere = true;

        try {
            setSocketCode("<zugliste />");
        } catch (Exception e) {
            e.printStackTrace();
        }


        XML zugliste = xml.read();

        for (XML xmlZug : zugliste.getInternXML()) {
            try {
                boolean exists = false;
                for (Zug z : stellwerk.zuege) {
                    try {
                        if (!xmlZug.get("zid").equals("") && Integer.parseInt(xmlZug.get("zid")) == z.getZugId()) {
                            exists = true;
                            break;
                        }
                    } catch (Exception e) {
                        System.out.println("GITHUB #20: " + xmlZug.get("zid") + xmlZug.get("name"));
                        e.printStackTrace();
                    }
                }

                if (!exists && !xmlZug.get("zid").equals("") && !xmlZug.get("name").equals("")) {
                    //System.out.println("INFORMATION: " + zugliste.get(i).get(0).get(1)[1] + " wurde hinzugefügt!");
                    stellwerk.zuege.add(new Zug(Integer.parseInt(xmlZug.get("zid")), xmlZug.get("name")));
                }
            } catch (Exception e) {
                System.out.println("FEHLER: Zuglistenaktualisierungsfehler!");
                e.printStackTrace();
            }
        }

        ArrayList<Zug> removing = new ArrayList<>();

        for (int j = 0; j < stellwerk.zuege.size(); j++) {
            Zug z = stellwerk.zuege.get(j);

            try {
                boolean updateNeeded = false;
                setSocketCode("<zugdetails zid='" + z.getZugId() + "'/>");
                XML zugdetails = xml.read();
                int counter = 0;

                if (zugdetails == null || !zugdetails.getKey().equals("zugdetails")) {
                    // Fehler
                } else {
                    String verspaetungString = zugdetails.get("verspaetung");
                    if (verspaetungString != null) {
                        int verspaetung = Integer.parseInt(verspaetungString);
                        if (verspaetung != z.getVerspaetung()) {
                            z.setVerspaetung(verspaetung);
                            updateNeeded = true;
                        }
                        counter++;
                    }
                    String gleis = zugdetails.get("gleis");
                    if (gleis != null) {
                        if (z.getBahnsteig() == null || !gleis.equals(z.getBahnsteig().getName())) {
                            z.setBahnsteig(sucheBahnsteig(gleis));
                            updateNeeded = true;
                        }
                        counter++;
                    }
                    String amgleisString = zugdetails.get("amgleis");
                    if (amgleisString != null) {
                        boolean amgleis = Boolean.parseBoolean(amgleisString);
                        if (amgleis != z.getAmGleis()) {
                            z.setAmGleis(amgleis);
                            updateNeeded = true;
                        }
                        counter++;
                    }
                    if (zugdetails.get("von") != null) {
                        if (!zugdetails.get("von").equals(z.getVon())) {
                            z.setVon(zugdetails.get("von"));
                            updateNeeded = true;
                        }
                        counter++;
                    }
                    if (zugdetails.get("nach") != null) {
                        if (!zugdetails.get("nach").equals(z.getNach())) {
                            z.setNach(zugdetails.get("nach"));
                            updateNeeded = true;
                        }
                        counter++;
                    }
                    if(zugdetails.get("plangleis") != null){
                        if(z.getPlangleis() == null || !zugdetails.get("plangleis").equals(z.getPlangleis().getName())){
                            z.setPlangleis(sucheBahnsteig(zugdetails.get("plangleis")));
                            updateNeeded = true;
                        }
                        counter++;
                    }
                    if(zugdetails.get("sichtbar") != null){
                        if(Boolean.parseBoolean(zugdetails.get("sichtbar")) != z.getSichtbar()){
                            z.setSichtbar(Boolean.parseBoolean(zugdetails.get("sichtbar")));
                            updateNeeded = true;
                        }
                        counter++;
                    }
                }

                if (counter != 7 && counter != 5) {
                    System.out.println("INFORMATION: " + z.getZugName() + " es wurden nicht alle Daten gesetzt " + z.getVerspaetung() + " " + z.getBahnsteig().getName() + " " + z.getAmGleis() + " " + z.getVon() + " " + z.getNach() + " " + z.getPlangleis().getName() + " " + z.getSichtbar());
                } else if (counter == 5) {
                    removing.add(z);
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
                            try{
                                long time = dateFormat.parse(zugfahrplanEintrag.get("an")).getTime();
                                zugfahrplanEintrag.set("an", String.valueOf(time));
                            } catch(Exception e){
                                zugfahrplanEintrag.set("an", "0");
                            }

                            try{
                                long time = dateFormat.parse(zugfahrplanEintrag.get("ab")).getTime();
                                zugfahrplanEintrag.set("ab", String.valueOf(time));
                            } catch(Exception e){
                                zugfahrplanEintrag.set("ab", "0");
                            }
                            fahrplan.add(new FahrplanHalt(Long.parseLong(zugfahrplanEintrag.get("ab")), sucheBahnsteig(zugfahrplanEintrag.get("name")), zugfahrplanEintrag.get("flags"), sucheBahnsteig(zugfahrplanEintrag.get("plan")), Long.parseLong(zugfahrplanEintrag.get("an")), z));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (!updateNeeded) {
                        if (z.getFahrplan() != null && z.getFahrplan().size() != fahrplan.size()) {
                            updateNeeded = true;
                        } else if (z.getFahrplan() != null) {
                            for (int i = 0; i < z.getFahrplan().size(); i++) {
                            if (z.getFahrplan(i) != null && fahrplan.get(i) != null) {
                                    if (z.getFahrplan(i).getAnkuft() != fahrplan.get(i).getAnkuft()) {
                                        updateNeeded = true;
                                    } else if (z.getFahrplan(i).getAbfahrt() != fahrplan.get(i).getAbfahrt()) {
                                        updateNeeded = true;
                                    } else if (!z.getFahrplan(i).getBahnsteig().getName().equals(fahrplan.get(i).getBahnsteig().getName())) {
                                        updateNeeded = true;
                                    } else if (!z.getFahrplan(i).getPlanBahnsteig().getName().equals(fahrplan.get(i).getPlanBahnsteig().getName())) {
                                        updateNeeded = true;
                                    } else if (!z.getFahrplan(i).getFlags().equals(fahrplan.get(i).getFlags())) {
                                        updateNeeded = true;
                                    }
                                } else {
                                    updateNeeded = true;
                                }
                            }
                        }
                    }

                    if (updateNeeded) {
                        if (!z.isNewTrain()) {
                            z.removeFromGrid();
                        }
                        z.setFahrplan(fahrplan);
                        z.setNeedUpdate(true);
                    }
                }
            } catch (Exception e) {
                System.out.println("ZUG: " + z.getZugName() + ": Verbindungsfehler!");
                e.printStackTrace();
            }
        }

        for (Zug z : removing) {
            //System.out.println("INFORMATION: " + z.getZugName() + " wurde entfernt.");
            stellwerk.zuege.remove(z);
        }

        for (Zug z : stellwerk.zuege) {
            try {
                if (z != null && z.getFahrplan() != null) {
                    for (FahrplanHalt fh : z.getFahrplan()) {
                        try {
                            if (fh != null && fh.getFlags() != null && !fh.getFlags().equals("")) {
                                Zug flagged = getFlaggedTrain(fh.getFlags());
                                if(flagged != null && flagged.getFahrplan() != null && flagged.getFahrplan(0) != null){
                                    fh.setFlaggedTrain(flagged);
                                    flagged.getFahrplan(0).setVorgaenger(fh);
                                } else{
                                    fh.setFlaggedTrain(null);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("ZUG: " + z.getZugName() + ": Flag-Problem!");
                e.printStackTrace();
            }
        }

        aktualisiere = false;
    }

    //Checkt ob ein Zug einen Nachfolger hat.
    private Zug getFlaggedTrain(String content) {
        if (content.contains("(") && content.contains(")")) {
            char[] in = content.toCharArray();
            String out = "";
            boolean inKlammer = false;

            for (int i = 0; i < in.length; i++) {
                if (in[i] == '(') {
                    inKlammer = true;
                    i++;
                }
                if (in[i] == ')') {
                    inKlammer = false;
                }

                if (inKlammer) {
                    out += in[i];
                }
            }

            for (Zug z : stellwerk.zuege) {
                if (z.getZugId() == Integer.parseInt(out)) {
                    return z;
                }
            }
        }
        return null;
    }

    public boolean isAktualisiere() {
        return aktualisiere;
    }

    private Bahnsteig sucheBahnsteig(String name) {
        for (Bahnhof bahnhof : stellwerk.getBahnhoefe()) {
            for (Bahnsteig bahnsteig : bahnhof.getBahnsteige()) {
                if (bahnsteig.getName().equals(name)) return bahnsteig;
            }
        }
        return null;
    }

    public Socket getSocket(){
        return socket;
    }

    @Override
    public String toString() {
        return "Verbindung{" +
                "socket=" + socket +
                ", xml=" + xml +
                ", aktualisiere=" + aktualisiere +
                ", stellwerk=" + stellwerk +
                '}';
    }
}
