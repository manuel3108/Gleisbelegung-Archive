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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Verbindung{
    private Socket socket;       //Java-Socket zur Kommunikation über TCP/IP
    private XMLHandler xml;     //Verarbeitet die Empfangenen Daten in einer Eigenen Klasse
    private boolean aktualisiere;

    private Stellwerk stellwerk;

    //Führt einige notwendige Kommunikationsschritte mit der Verbindung durch und Verlangt u.a. Uhrzeit und Bahnsteige
    Verbindung(Socket socket, Stellwerk stellwerk, String pluginName, String pluginBeschreibung, String autor, int version){
        this.stellwerk = stellwerk;

        try {
            this.socket = socket;
            xml = new XMLHandler(socket.getInputStream());
        } catch (IOException e) {
            stellwerk.errorWindow(-1, "Es liegt ein Fehler bei der Anmeldung vor.\n\nPrüfe ob die Plugin-Schnittstelle aktiv ist und ob überhaupt ein Simulator läuft.");
            System.exit(-1);
        }

        ArrayList<String[]> temp = xml.readLine();
        if(temp != null && Integer.parseInt(temp.get(0)[1]) != 300){
            stellwerk.errorWindow(-2, "Es liegt ein Fehler bei der Anmeldung vor.\n\nPrüfe ob die Plugin-Schnittstelle aktiv ist und ob überhaupt ein Simulator läuft.");
            System.exit(-2);
        }

        if(setSocketCode("<register name=\""+pluginName+"\" autor=\""+autor+"\" version=\""+version+"\" protokoll=\"1\" text=\""+pluginBeschreibung+"\" />") != 1){
            stellwerk.errorWindow(-3, "Es liegt ein Fehler bei der Anmeldung vor.\n\nPrüfe ob die Plugin-Schnittstelle aktiv ist und ob überhaupt ein Simulator läuft.");
            System.exit(-3);
        }

        temp = xml.readLine();
        if(temp != null && Integer.parseInt(temp.get(0)[1]) != 220){
            stellwerk.errorWindow(-4, "Anmeldung erfolgreich!\n\nSollte diese Meldung kommen, habe ich etwas falsch Programmiert");
            System.exit(-4);
        }

        if(setSocketCode("<anlageninfo />") != 1){
            stellwerk.errorWindow(-5, "Beim Senden der Daten an die Plugin-Schitstelle ist ein Fehler aufgetreten.");
            System.exit(-5);
        }

        temp = xml.readLine();
        if(temp == null){
            stellwerk.errorWindow(-6, "Beim Empfangen der Daten von der Plugin-Schitstelle ist ein Fehler aufgetreten.");
            System.exit(-6);
        }

        stellwerk.simbuild = Integer.parseInt(temp.get(0)[1]);
        stellwerk.stellwerksname = temp.get(1)[1];
        stellwerk.anlagenid = Integer.parseInt(temp.get(2)[1]);
        System.out.println("Die Verbindung mit dem Stellwerk " + temp.get(1)[1] + " und der Anlagen-Id " + temp.get(2)[1] + " wurde erfolgreich hergestellt. Aktuelle Simulator-Build: " + temp.get(0)[1]);

        long timeBeforeSending = System.currentTimeMillis();
        if(setSocketCode("<simzeit sender='" + timeBeforeSending + "' />") != 1){
            stellwerk.errorWindow(-7, "Beim Senden der Daten an die Plugin-Schitstelle ist ein Fehler aufgetreten.");
            System.exit(-7);
        }

        temp = xml.readLine();
        if(temp == null){
            stellwerk.errorWindow(-8, "Beim Empfangen der Daten von der Plugin-Schitstelle ist ein Fehler aufgetreten.");
            System.exit(-8);
        }
        stellwerk.spielzeit = ((System.currentTimeMillis() - timeBeforeSending)/1000)/2 + Long.parseLong(temp.get(1)[1]) - 1000*60*60;

        if(setSocketCode("<bahnsteigliste />") != 1){
            stellwerk.errorWindow(-9, "Beim Senden der Daten an die Plugin-Schitstelle ist ein Fehler aufgetreten.");
            System.exit(-9);
        }

        ArrayList<ArrayList<ArrayList<String[]>>> temp1 = xml.readLines();
        String[] bahnsteige = new String[temp1.size()];
        for(int i = 0; i < temp1.size(); i++){
            bahnsteige[i] = temp1.get(i).get(0).get(0)[1];
        }
        stellwerk.erstelleBahnhoefe(bahnsteige);

        if(setSocketCode("<wege />") != 1){
            stellwerk.errorWindow(-10, "Beim Senden der Daten an die Plugin-Schitstelle ist ein Fehler aufgetreten.");
            System.exit(-10);
        }
    }

    //Sendet Daten-Anfragen an die Plugin-Schnitstelle
    private int setSocketCode(String s){
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            out.write(s+"\n");
            out.flush();
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void aktualisiereSimZeit(){
        long timeBeforeSending = System.currentTimeMillis();
        if(setSocketCode("<simzeit sender='" + timeBeforeSending + "' />") != 1){
            stellwerk.errorWindow(-7, "Beim Senden der Daten an die Plugin-Schitstelle ist ein Fehler aufgetreten.");
            System.exit(-7);
        }
        ArrayList<String[]> temp = xml.readLine();
        if(temp == null){
            stellwerk.errorWindow(-8, "Beim Empfangen der Daten von der Plugin-Schitstelle ist ein Fehler aufgetreten.");
            System.exit(-8);
        }
        stellwerk.spielzeit = ((System.currentTimeMillis() - timeBeforeSending)/1000)/2 + Long.parseLong(temp.get(1)[1]) - 1000*60*60;
    }

    //Aktualisiert die Daten aller Züge
    public void update(){
        stellwerk.letzteAktualisierung = System.currentTimeMillis();
        aktualisiere = true;

        try{
            setSocketCode("<zugliste />");
        } catch (Exception e){
            e.printStackTrace();
        }


        ArrayList<ArrayList<ArrayList<String[]>>> zugliste = xml.readLines();

        for(int i = 0; i < zugliste.size(); i++){
            try{
                boolean exists = false;
                for(Zug z : stellwerk.zuege){
                    try{
                        if(!zugliste.get(i).get(0).get(0)[1].equals("") && Integer.parseInt(zugliste.get(i).get(0).get(0)[1]) == z.getZugId()){
                            exists = true;
                            break;
                        }
                    } catch (Exception e){
                        System.out.println("GITHUB #20: " + zugliste.get(i).get(0).get(0)[1] + zugliste.get(i).get(0).get(1)[1]);
                        e.printStackTrace();
                    }
                }

                if(!exists && !zugliste.get(i).get(0).get(0)[1].equals("") && !zugliste.get(i).get(0).get(1)[1].equals("")){
                    //System.out.println("INFORMATION: " + zugliste.get(i).get(0).get(1)[1] + " wurde hinzugefügt!");
                    stellwerk.zuege.add(new Zug(Integer.parseInt(zugliste.get(i).get(0).get(0)[1]), zugliste.get(i).get(0).get(1)[1]));
                }
            } catch (Exception e){
                System.out.println("FEHLER: Zuglistenaktualisierungsfehler!");
                e.printStackTrace();
            }
        }

        ArrayList<Zug> removing = new ArrayList<>();

        for(int j = 0; j < stellwerk.zuege.size(); j++){
            Zug z = stellwerk.zuege.get(j);

            try{
                boolean updateNeeded = false;
                setSocketCode("<zugdetails zid='" + z.getZugId() + "'/>");
                ArrayList<String[]> zugdetails = xml.readLine();
                int counter = 0;

                if(zugdetails != null){
                    for(String[] s : zugdetails){
                        s[0] = s[0].replace(" ", "");
                        if(s[0].equals("verspaetung")){
                            if(Integer.parseInt(s[1]) != z.getVerspaetung()){
                                z.setVerspaetung(Integer.parseInt(s[1]));
                                updateNeeded = true;
                            }
                            counter = counter + 1;
                        } else if(s[0].equals("gleis")){
                            if(z.getBahnsteig() == null || !s[1].equals(z.getBahnsteig().getName())){
                                z.setBahnsteig(sucheBahnsteig(s[1]));
                                updateNeeded = true;
                            }
                            counter = counter + 1;
                        } else if(s[0].equals("amgleis")){
                            if(Boolean.parseBoolean(s[1]) != z.getAmGleis()){
                                z.setAmGleis(Boolean.parseBoolean(s[1]));
                                updateNeeded = true;
                            }
                            counter = counter + 1;
                        } else if(s[0].equals("von")){
                            if(! s[1].equals(z.getVon())){
                                z.setVon(s[1]);
                                updateNeeded = true;
                            }
                            counter = counter + 1;
                        } else if(s[0].equals("nach")){
                            if(! s[1].equals(z.getNach())){
                                z.setNach(s[1]);
                                updateNeeded = true;
                            }
                            counter = counter + 1;
                        } else if(s[0].equals("plangleis")){
                            if(z.getPlangleis() == null || ! s[1].equals(z.getPlangleis().getName())){
                                z.setPlangleis(sucheBahnsteig(s[1]));
                                updateNeeded = true;
                            }
                            counter = counter + 1;
                        } else if(s[0].equals("sichtbar")){
                            if(Boolean.parseBoolean(s[1]) != z.getSichtbar()){
                                z.setSichtbar(Boolean.parseBoolean(s[1]));
                                updateNeeded = true;
                            }
                            counter = counter + 1;
                        }
                    }
                }

                if(counter!=7 && counter!=5){
                    System.out.println("INFORMATION: " + z.getZugName() + " es wurden nicht alle Daten gesetzt " + z.getVerspaetung() + " " + z.getBahnsteig() + " " + z.getAmGleis() + " " + z.getVon() + " " + z.getNach() + " " + z.getPlangleis() + " " + z.getSichtbar());
                } else if(counter == 5){
                    removing.add(z);
                }

                setSocketCode("<zugfahrplan zid='" + z.getZugId() + "'/>");
                ArrayList<ArrayList<ArrayList<String[]>>> zugfahrplan = xml.readLines();

                ArrayList<FahrplanHalt> fahrplan = new ArrayList<>();
                for (int i = 0; i < zugfahrplan.size(); i++) {
                    try{
                        if(zugfahrplan.get(i).get(0).size() >= 4){
                            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

                            try{
                                long time = dateFormat.parse(zugfahrplan.get(i).get(0).get(0)[1]).getTime();
                                zugfahrplan.get(i).get(0).get(0)[1] = String.valueOf(time);
                            } catch(Exception e){
                                zugfahrplan.get(i).get(0).get(0)[1] = String.valueOf(0);
                            }

                            try{
                                long time = dateFormat.parse(zugfahrplan.get(i).get(0).get(4)[1]).getTime();
                                zugfahrplan.get(i).get(0).get(4)[1] = String.valueOf(time);
                            } catch (Exception e){
                                zugfahrplan.get(i).get(0).get(4)[1] = String.valueOf(0);
                            }

                            ArrayList<String[]> fahrplanhalt = zugfahrplan.get(i).get(0);
                            fahrplan.add(i, new FahrplanHalt(Long.parseLong(fahrplanhalt.get(0)[1]), sucheBahnsteig(fahrplanhalt.get(1)[1]), fahrplanhalt.get(2)[1], sucheBahnsteig(fahrplanhalt.get(3)[1]), Long.parseLong(fahrplanhalt.get(4)[1]), z));
                        } else{
                            System.out.println("ARRAY out of Bounds: Zug: " + z.getZugName());
                        }
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }

                if(! updateNeeded){
                    if(z.getFahrplan() != null && z.getFahrplan().size() != fahrplan.size()){
                        updateNeeded = true;
                    } else if(z.getFahrplan() != null){
                        for (int i = 0; i < z.getFahrplan().size(); i++) {
                            if(z.getFahrplan(i) != null && fahrplan.get(i) != null && z.getFahrplan(i).getAnkuft() != 0){
                                if(z.getFahrplan(i).getAnkuft() != fahrplan.get(i).getAnkuft()){
                                    updateNeeded = true;
                                } else if(z.getFahrplan(i).getAbfahrt() != fahrplan.get(i).getAbfahrt()){
                                    updateNeeded = true;
                                } else if(! z.getFahrplan(i).getBahnsteig().getName().equals(fahrplan.get(i).getBahnsteig().getName())){
                                    updateNeeded = true;
                                } else if(! z.getFahrplan(i).getPlanBahnsteig().getName().equals(fahrplan.get(i).getPlanBahnsteig().getName())){
                                    updateNeeded = true;
                                } else if(! z.getFahrplan(i).getFlags().equals(fahrplan.get(i).getFlags())){
                                    updateNeeded = true;
                                }
                            } else{
                                updateNeeded = true;
                            }
                        }
                    }
                }

                if(updateNeeded){
                    if(! z.isNewTrain()){
                        z.removeFromGrid();
                    }
                    z.setFahrplan(fahrplan);
                    z.setNeedUpdate(true);
                }
            } catch(Exception e){
                System.out.println("ZUG: " + z.getZugName() + ": Verbindungsfehler!");
                e.printStackTrace();
            }
        }

        for(Zug z : removing){
            //System.out.println("INFORMATION: " + z.getZugName() + " wurde entfernt.");
            stellwerk.zuege.remove(z);
        }

        for (Zug z : stellwerk.zuege) {
            try{
                if(z != null && z.getFahrplan() != null){
                    for(FahrplanHalt fh : z.getFahrplan()){
                        try{
                            if(fh != null && fh.getFlags() != null && !fh.getFlags().equals("")){
                                Zug flagged = getFlaggedTrain(fh.getFlags());
                                if(flagged != null && flagged.getFahrplan() != null && flagged.getFahrplan(0) != null){
                                    flagged.getFahrplan(0).setDrawable(false);
                                    fh.setFlaggedTrain(flagged);
                                } else{
                                    fh.setFlaggedTrain(null);
                                }
                            }
                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }catch(Exception e){
                System.out.println("ZUG: " + z.getZugName() + ": Flag-Problem!");
                e.printStackTrace();
            }
        }

        aktualisiere = false;
    }

    //Checkt ob ein Zug einen Nachfolger hat.
    private Zug getFlaggedTrain(String content) {
        if(content.contains("(") && content.contains(")")){
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

    private Bahnsteig sucheBahnsteig(String name){
        for(Bahnhof bahnhof : stellwerk.getBahnhoefe()){
            for(Bahnsteig bahnsteig : bahnhof.getBahnsteige()){
                if(bahnsteig.getName().equals(name)) return bahnsteig;
            }
        }
        return null;
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
