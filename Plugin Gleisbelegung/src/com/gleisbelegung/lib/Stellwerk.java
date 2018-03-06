package com.gleisbelegung.lib;

import com.gleisbelegung.lib.data.Bahnhof;
import com.gleisbelegung.lib.data.Bahnsteig;
import com.gleisbelegung.lib.data.Zug;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class Stellwerk {
    private static String host;
    private static int port;

    String stellwerksname;
    int anlagenid;
    int simbuild;

    private long startzeit;
    long spielzeit;
    long letzteAktualisierung;

    private ArrayList<Bahnhof> bahnhoefe;
    ArrayList<Zug> zuege;

    private static Verbindung v;

    public Stellwerk(String host, int port, String pluginName, String pluginBeschreibung, String autor, int version){
        Stellwerk.host = host;
        Stellwerk.port = port;

        try {
            startzeit = System.currentTimeMillis();
            bahnhoefe = new ArrayList<>();
            zuege = new ArrayList<>();

            v = new Verbindung(new Socket(host, port), this, pluginName, pluginBeschreibung, autor, version);
            v.update();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void errorWindow(int exitCode, String message){
        System.out.println("ExitCode: " + exitCode + " Message: " + message);
    }

    void erstelleBahnhoefe(String[] bahnsteige){
        String currentRegex = "";
        String[] regex = new String[bahnsteige.length];
        for(int i = 0; i < bahnsteige.length; i++){
            regex[i] = bahnsteige[i].replaceAll("\\P{L}+", "");
            if(currentRegex.equals(regex[i]) && bahnhoefe.size() > 0){
                bahnhoefe.get(bahnhoefe.size()-1).getBahnsteige().add(new Bahnsteig(bahnhoefe.get(bahnhoefe.size()-1), bahnsteige[i], i));
            } else {
                currentRegex = regex[i];
                bahnhoefe.add(new Bahnhof(bahnhoefe.size(), currentRegex));
                bahnhoefe.get(bahnhoefe.size()-1).getBahnsteige().add(new Bahnsteig(bahnhoefe.get(bahnhoefe.size()-1), bahnsteige[i], i));
            }
        }

        /*for(Bahnhof b : bahnhoefe){
            for(Bahnsteig ba : b.getBahnsteige()){
                System.out.println(b.getName() + " " + ba.getName());
            }
        }*/
    }

    public boolean aktualisiereDaten(){
        if(!v.isAktualisiere()){
            v.update();
            return true;
        }
        return false;
    }

    public void aktualisiereSimZeit(){
        v.aktualisiereSimZeit();
    }

    public int getAnzahlBahnsteige(){
        int counter = 0;
        for(Bahnhof b : bahnhoefe){
            counter += b.getAnzahlBahnsteige();
        }
        return counter;
    }
    public String getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }
    public String getStellwerksname() {
        return stellwerksname;
    }
    public int getAnlagenid() {
        return anlagenid;
    }
    public int getSimbuild() {
        return simbuild;
    }
    public ArrayList<Bahnhof> getBahnhoefe() {
        return bahnhoefe;
    }
    public ArrayList<Zug> getZuege() {
        return zuege;
    }
    public long getSpielzeit() {
        return spielzeit;
    }
    public long getStartzeit() {
        return startzeit;
    }
    public long getLetzteAktualisierung() {
        return letzteAktualisierung;
    }
    public boolean isAktualisiere(){
        return v.isAktualisiere();
    }

    @Override
    public String toString() {
        return "Stellwerk{" +
                "stellwerksname='" + stellwerksname + '\'' +
                ", anlagenid=" + anlagenid +
                ", simbuild=" + simbuild +
                ", startzeit=" + startzeit +
                ", spielzeit=" + spielzeit +
                ", letzteAktualisierung=" + letzteAktualisierung +
                '}';
    }
}
