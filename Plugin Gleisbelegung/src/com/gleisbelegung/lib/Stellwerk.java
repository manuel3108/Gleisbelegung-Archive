package com.gleisbelegung.lib;

import com.gleisbelegung.lib.data.Bahnhof;
import com.gleisbelegung.lib.data.Bahnsteig;
import com.gleisbelegung.lib.data.Zug;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

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

    public Stellwerk(String host, int port, String pluginName, String pluginBeschreibung, String autor, int version) throws IOException {
        this.host = host;
        this.port = port;

        startzeit = System.currentTimeMillis();
        bahnhoefe = new ArrayList<>();
        zuege = new ArrayList<>();

        v = new Verbindung(new Socket(host, port), this, pluginName, pluginBeschreibung, autor, version);
        v.update();
    }

    public Stellwerk(Socket socket, String pluginName, String pluginBeschreibung, String autor, int version){
        host = null;
        port = 0;

        startzeit = System.currentTimeMillis();
        bahnhoefe = new ArrayList<>();
        zuege = new ArrayList<>();

        v = new Verbindung(socket, this, pluginName, pluginBeschreibung, autor, version);
        v.update();
    }

    void erstelleBahnhoefe(String[] bahnsteige){
        String letzterBahnhofsName = "";
        String bahnsteigsName = "";
        String[] bahnhofsName = new String[bahnsteige.length];
        for(int i = 0; i < bahnsteige.length; i++){
            if(Character.isLetter(bahnsteige[i].charAt(bahnsteige[i].length()-1)) && bahnsteige[i].matches(".*\\d+.*")){
                bahnsteigsName = bahnsteige[i];
                bahnsteige[i] = bahnsteige[i].substring(0, bahnsteige[i].length() - 1);
            } else {
                bahnsteigsName = bahnsteige[i];
            }
            bahnhofsName[i] = bahnsteige[i].replaceAll("\\P{L}+", "");
            if(letzterBahnhofsName.equals(bahnhofsName[i]) && bahnhoefe.size() > 0){
                bahnhoefe.get(bahnhoefe.size()-1).getBahnsteige().add(new Bahnsteig(bahnhoefe.get(bahnhoefe.size()-1), bahnsteigsName, i));
            } else {
                letzterBahnhofsName = bahnhofsName[i];
                bahnhoefe.add(new Bahnhof(bahnhoefe.size(), letzterBahnhofsName));
                bahnhoefe.get(bahnhoefe.size()-1).getBahnsteige().add(new Bahnsteig(bahnhoefe.get(bahnhoefe.size()-1), bahnsteigsName, i));
            }
        }
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
    public Socket getSocket(){
        return v.getSocket();
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
    public void setSpielzeit(long spielzeit) {
        this.spielzeit = spielzeit;
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
