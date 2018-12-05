package com.gleisbelegung.lib;

import com.gleisbelegung.lib.data.Bahnhof;
import com.gleisbelegung.lib.data.Bahnsteig;
import com.gleisbelegung.lib.data.Trainlist;
import com.gleisbelegung.lib.data.Zug;

import de.heidelbach_net.util.XML;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class Stellwerk {
    private static String host;
    private static int port;

    String stellwerksname;
    int anlagenid;
    int simbuild;

    private long startzeit;
    long spielzeit;
    long letzteAktualisierung;

    private final List<Bahnhof> bahnhoefe = new ArrayList<>();
    private final Map<String, Bahnhof> bahnhofMap = new HashMap<>();
    private final Map<String, Bahnsteig> bahnsteigMap = new HashMap<>();
    private Trainlist zuege;

    private Verbindung v;

    public Stellwerk(Verbindung v, String name, int anlagenId, int simbuild) {
        startzeit = System.currentTimeMillis();
        this.v = v;
    }
    
    void erstelleBahnhoefe(String[] bahnsteige) {
        String letzterBahnhofsName = "";
        String bahnsteigsName = "";
        String[] bahnhofsName = new String[bahnsteige.length];
        Bahnhof lastB = null;
        for(int i = 0; i < bahnsteige.length; i++){
            if(Character.isLetter(bahnsteige[i].charAt(bahnsteige[i].length()-1)) && bahnsteige[i].matches(".*\\d+.*")){
                bahnsteigsName = bahnsteige[i];
                bahnsteige[i] = bahnsteige[i].substring(0, bahnsteige[i].length() - 1);
            } else {
                bahnsteigsName = bahnsteige[i];
            }
            bahnhofsName[i] = bahnsteige[i].replaceAll("\\P{L}+", "");
            Bahnhof b;
            if(letzterBahnhofsName.equals(bahnhofsName[i]) && bahnhoefe.size() > 0){
                b = lastB;
            } else {
                letzterBahnhofsName = bahnhofsName[i];
                lastB = b = new Bahnhof(bahnhoefe.size(), letzterBahnhofsName);
                bahnhoefe.add(b);
                bahnhofMap.put(bahnhofsName[i], b);
            }
            Bahnsteig bst = new Bahnsteig(b, bahnsteigsName, i);
            b.addBahnsteig(bst);
            bahnsteigMap.put(bahnsteigsName, bst);
        }
    }

    public boolean aktualisiereDaten() {
    	return v.update(this);
    }

    public void aktualisiereSimZeit(){
        this.spielzeit = v.aktualisiereSimZeit();
    }

    public Collection<Bahnsteig> getBahnsteige() {
    	return bahnsteigMap.values();
    }
    public int getAnzahlBahnsteige(){
        return bahnsteigMap.size();
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
    public Collection<Bahnhof> getBahnhoefe() {
        return bahnhofMap.values();
    }
    
    /**
     * Create a copy of the internal train list. Changes to the returned list will not affect the original one. 
     * 
     * @return Copy of actual train list. 
     */
    public Collection<Zug> getZuege() {
    	return this.zuege == null ? Collections.emptyList() : this.zuege.getZuege();
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

	public Bahnsteig getBahnsteigByName(String string) {
		return this.bahnsteigMap.get(string);
	}

	public void updateZugliste(XML read) {
		if (null == this.zuege) {
			this.zuege = Trainlist.parse(read);
		} else {
			this.zuege.update(read);
		}
	}

	public void removeZug(Zug z) {
		this.zuege.remove(z);
	}

	public Map<Integer, Zug> getZugMap() {
		return this.zuege.toMap();
	}
}
