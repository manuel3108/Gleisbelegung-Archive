package com.gleisbelegung.plugin;
/*
@author: Manuel Serret
@email: manuel-serret@t-online.de
@contact: Email, Github, STS-Forum

Hinweis: In jeder Klasse werden alle Klassenvariablen erklärt, sowie jede Methode. Solltest du neue Variablen oder Methoden hinzufügen, vergiss bitte nicht sie zu implementieren.

Erstellt und hält die Verbindung mit der Schnitstelle aufrecht.
 */

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.heidelbach_net.util.XML;

public class Verbindung extends Plugin_Gleisbelegung{
    public Socket socket;       //Java-Socket zur Kommunikation über TCP/IP
    private XMLHandler xml;     //Verarbeitet die Empfangenen Daten in einer Eigenen Klasse
    private boolean aktualisiere;

    //Führt einige notwendige Kommunikationsschritte mit der Verbindung durch und Verlangt u.a. Uhrzeit und Bahnsteige
    public Verbindung(Socket socket) throws Exception{
        try {
            this.socket = socket;
            xml = new XMLHandler(socket.getInputStream());
            zuege = new ArrayList<Zug>();
        } catch (IOException e) {
            errorWindow(-1, "Es liegt ein Fehler bei der Anmeldung vor.\n\nPrüfe ob die Plugin-Schnittstelle aktiv ist und ob überhaupt ein Simulator läuft.");
            System.exit(-1);
        }

        XML temp = xml.read();
        if(temp != null && Integer.parseInt(temp.get("code")) != 300){
            errorWindow(-2, "Es liegt ein Fehler bei der Anmeldung vor.\n\nPrüfe ob die Plugin-Schnittstelle aktiv ist und ob überhaupt ein Simulator läuft.");
            System.exit(-2);
        }

        if(setSocketCode("<register name=\"Gleisbelegung\" autor=\"Manuel Serret\" version=\"0.1\" protokoll=\"1\" text=\"Darstellung der Gleisbelegung\" />") != 1){
            errorWindow(-3, "Es liegt ein Fehler bei der Anmeldung vor.\n\nPrüfe ob die Plugin-Schnittstelle aktiv ist und ob überhaupt ein Simulator läuft.");
            System.exit(-3);
        }

        temp = xml.read();
        if(temp != null && Integer.parseInt(temp.get("code")) != 220){
            errorWindow(-4, "Anmeldung erfolgreich!\n\nSollte diese Meldung kommen, habe ich etwas falsch Programmiert");
            System.exit(-4);
        }

        if(setSocketCode("<anlageninfo />") != 1){
            errorWindow(-5, "Beim Senden der Daten an die Plugin-Schitstelle ist ein Fehler aufgetreten.");
            System.exit(-5);
        }

        temp = xml.read();
        if(temp == null){
            errorWindow(-6, "Beim Empfangen der Daten von der Plugin-Schitstelle ist ein Fehler aufgetreten.");
            System.exit(-6);
        }
        bahnhofName = temp.get("name");
        System.out.println("Die Verbindung mit dem Stellwerk " + temp.get("name") + " und der Anlagen-Id " + temp.get("aid") + " wurde erfolgreich hergestellt. Aktuelle Simulator-Build: " + temp.get("simbuild"));
        addMessageToErrorPane("Verbindung erfolgreich!");

        long timeBeforeSending = System.currentTimeMillis();
        if(setSocketCode("<simzeit sender='" + timeBeforeSending + "' />") != 1){
            errorWindow(-7, "Beim Senden der Daten an die Plugin-Schitstelle ist ein Fehler aufgetreten.");
            System.exit(-7);
        }

        temp = xml.read();
        if(temp == null){
            errorWindow(-8, "Beim Empfangen der Daten von der Plugin-Schitstelle ist ein Fehler aufgetreten.");
            System.exit(-8);
        }
        currentGameTime = ((System.currentTimeMillis() - timeBeforeSending)/1000)/2 + Long.parseLong(temp.get("zeit")) - 1000*60*60;

        if(setSocketCode("<bahnsteigliste />") != 1){
            errorWindow(-9, "Beim Senden der Daten an die Plugin-Schitstelle ist ein Fehler aufgetreten.");
            System.exit(-9);
        }

        temp = xml.read();
        List<XML> bahnsteigeXML = temp.getInternXML();
        bahnsteige = new String[bahnsteigeXML.size()];
        Iterator<XML> bahnsteigIterator = bahnsteigeXML.iterator();
        for (int i = 0; i < bahnsteige.length; i++){
            bahnsteige[i] = bahnsteigIterator.next().get("name");
        }
    }

    //Sendet Daten-Anfragen an die Plugin-Schnitstelle
    private int setSocketCode(String s) throws Exception{
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

    //Aktualisiert die Daten aller Züge
    public void update(){
        aktualisiere = true;

        try{
            setSocketCode("<zugliste />");
        } catch (Exception e){
            e.printStackTrace();
        }


        XML zugliste = xml.read();

        for(final XML xmlZug : zugliste.getInternXML()){
            try{
                boolean exists = false;
                for(Zug z : zuege){
                    try{
                        if(!xmlZug.get("zid").equals("") && Integer.parseInt(xmlZug.get("zid")) == z.getZugId()){
                            exists = true;
                            break;
                        }
                    } catch (Exception e){
                        System.out.println("GITHUB #20: " + xmlZug.get("zid") + xmlZug.get("name"));
                        e.printStackTrace();
                    }
                }

                if(!exists && !xmlZug.get("zid").equals("") && !xmlZug.get("name").equals("")){
                    //System.out.println("INFORMATION: " + zugliste.get(i).get(0).get(1)[1] + " wurde hinzugefügt!");
                    zuege.add(new Zug( Integer.parseInt(xmlZug.get("zid")),  xmlZug.get("name")));
                }
            } catch (Exception e){
                errorCounter++;
                System.out.println("FEHLER: Zuglistenaktualisierungsfehler!");
                e.printStackTrace();
                addMessageToErrorPane("FEHLER: Zuglistenaktualisierungsfehler!");
            }
        }

        ArrayList<Zug> removing = new ArrayList<>();



        for(int j = 0; j < zuege.size(); j++){
            Zug z = zuege.get(j);

            try{
                boolean updateNeeded = false;
                setSocketCode("<zugdetails zid='" + z.getZugId() + "'/>");
                XML zugdetails = xml.read();
                int counter = 0;
                if (zugdetails == null || !zugdetails.getKey().equals("zugdetails")) {
                	// Fehler
                } else {
                      String verspaetungString =  zugdetails.get("verspaetung");
                      if (verspaetungString != null){
                      		int verspaetung = Integer.parseInt(verspaetungString);
                          if(verspaetung != z.getVerspaetung()){
                              z.setVerspaetung(verspaetung);
                              updateNeeded = true;
                          }
                          counter++;
                      }
                      String gleis = zugdetails.get("gleis");
                      if (gleis != null) {
                          if(gleis.equals(z.getGleis())){
                              z.setGleis(gleis);
                              updateNeeded = true;
                          }
                          counter++;
                      } 
                      String amgleisString = zugdetails.get("amgleis");
                      if(amgleisString != null){
                      		boolean amgleis = Boolean.parseBoolean(amgleisString);
                          if (amgleis != z.getAmGleis()){
                              z.setAmGleis(amgleis);
                              updateNeeded = true;
                          }
                          counter++;
                      } 
                      String sichtbarString = zugdetails.get("sichtbar");
                      if(sichtbarString != null){
                      		boolean sichtbar = Boolean.parseBoolean(sichtbarString);
                          if (sichtbar != z.getSichtbar()){
                              z.setSichtbar(sichtbar);
                              updateNeeded = true;
                          }
                          counter++;
                      } 
                      if (zugdetails.get("von") != null){
                          if(!zugdetails.get("von").equals(z.getVon())){
                              z.setVon(zugdetails.get("von"));
                              updateNeeded = true;
                          }
                          counter++;
                      }
                      if (zugdetails.get("nach") != null){
                        if(!zugdetails.get("nach").equals(z.getNach())){
                          z.setNach(zugdetails.get("nach"));
                          updateNeeded = true;
                        }
                        counter++;
                      } 
                      if (zugdetails.get("plangleis") != null){
                        if(!zugdetails.get("plangleis").equals(z.getPlangleis())){
                          z.setVon(zugdetails.get("plangleis"));
                          updateNeeded = true;
                        }
                        counter++;
                      } 
                    
                }

                if(counter!=7 && counter!=5){
                    System.out.println("INFORMATION: " + z.getZugName() + " es wurden nicht alle Daten gesetzt " + z.getVerspaetung() + " " + z.getGleis() + " " + z.getAmGleis() + " " + z.getVon() + " " + z.getNach() + " " + z.getPlangleis() + " " + z.getSichtbar());
                } else if(counter == 5){
                    removing.add(z);
                }

                setSocketCode("<zugfahrplan zid='" + z.getZugId() + "'/>");
                XML zugfahrplan = xml.read();
                if (zugfahrplan == null || !zugfahrplan.getKey().equals("zugfahrplan")) {
                	// Fehler
                } else {
                	List<XML> zugfahrplanEintraege = zugfahrplan.getInternXML();
                FahrplanHalt[] fahrplan = new FahrplanHalt[zugfahrplanEintraege.size()];
                Iterator<XML> fahrplanIterator = zugfahrplanEintraege.iterator();
                for (int i = 0; i < fahrplan.length; i++) {
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

                    fahrplan[i] = new FahrplanHalt(zugfahrplanEintrag, z);
                      
                    } catch(Exception e){
                        e.printStackTrace();
                    }
            	}

                if(!updateNeeded){
                    if(z.getFahrplan() != null && z.getFahrplan().length != fahrplan.length){
                        updateNeeded = true;
                    } else if(z.getFahrplan() != null){
                        for (int i = 0; i < z.getFahrplan().length; i++) {
                            if(z.getFahrplan(i) != null && fahrplan[i] != null && z.getFahrplan(i).getAnkuft() != 0){
                                if(z.getFahrplan(i).getAnkuft() != fahrplan[i].getAnkuft()){
                                    updateNeeded = true;
                                } else if(z.getFahrplan(i).getAbfahrt() != fahrplan[i].getAbfahrt()){
                                    updateNeeded = true;
                                } else if(! z.getFahrplan(i).getGleis().equals(fahrplan[i].getGleis())){
                                    updateNeeded = true;
                                } else if(! z.getFahrplan(i).getPlangleis().equals(fahrplan[i].getPlangleis())){
                                    updateNeeded = true;
                                } else if(! z.getFahrplan(i).getFlags().equals(fahrplan[i].getFlags())){
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
              }
            } catch(Exception e){
                errorCounter++;
                System.out.println("ZUG: " + z.getZugName() + ": Verbindungsfehler!");
                e.printStackTrace();
                addMessageToErrorPane("ZUG: " + z.getZugName() + ": Verbindungsfehler!");
            }
        }

        for(Zug z : removing){
            //System.out.println("INFORMATION: " + z.getZugName() + " wurde entfernt.");
            zuege.remove(z);
        }

        for (Zug z : zuege) {
            try{
                if(z != null && z.getFahrplan() != null){
                    for(FahrplanHalt fh : z.getFahrplan()){
                        try{
                            if(fh != null && fh.getFlags() != null && !fh.getFlags().equals("")){
                                Zug flagged = getFlaggedTrain(fh.getFlags());
                                if(flagged != null && flagged.getFahrplan() != null && flagged.getFahrplan(0) != null){
                                    flagged.getFahrplan(0).setDrawable(false);
                                    fh.setFlaggedTrain(flagged);
                                    debugMessage("ZUG: " + z.getZugName() + " (" + z.getZugId() + ") Flag: " + flagged.getZugName() + " (" + flagged.getZugId() + ")", true);
                                } else{
                                    fh.setFlaggedTrain(null);
                                    debugMessage("ZUG: " + z.getZugName() + " (" + z.getZugId() + ") Flag ohne Zug", true);
                                }
                            }
                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }catch(Exception e){
                errorCounter++;
                System.out.println("ZUG: " + z.getZugName() + ": Flag-Problem!");
                e.printStackTrace();
                addMessageToErrorPane("ZUG: " + z.getZugName() + ": Flag-Problem!");
            }
        }

        aktualisiere = false;
    }

    //Checkt ob ein Zug einen Nachfolger hat.
    private Zug getFlaggedTrain(String content) throws Exception{
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

            for (Zug z : zuege) {
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
}
