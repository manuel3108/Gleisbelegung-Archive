package com.gleisbelegung.lib;

import com.gleisbelegung.lib.data.Platform;
import com.gleisbelegung.lib.data.Station;
import com.gleisbelegung.lib.data.Train;
import com.gleisbelegung.lib.data.Trainlist;
import de.heidelbach_net.util.XML;

import java.net.Socket;
import java.util.*;


public class SignalBox {
    private final List<Station> stations = new ArrayList<>();
    private final Map<String, Station> stationMap = new HashMap<>();
    private final Map<String, Platform> platformMap = new HashMap<>();
    private String signalBoxName;
    private int signalBoxId;
    private int simBuild;
    long playingTime;
    private long lastRefresh;
    private long startingTime;
    private Trainlist trains;

    private Connection connection;

    public SignalBox(Connection v, String name, int anlagenId, int simbuild) {
        startingTime = System.currentTimeMillis();
        this.connection = v;
    }

    void erstelleBahnhoefe(String[] bahnsteige) {
        String letzterBahnhofsName = "";
        String bahnsteigsName = "";
        String[] bahnhofsName = new String[bahnsteige.length];
        Station lastB = null;
        for (int i = 0; i < bahnsteige.length; i++) {
            if (Character
                    .isLetter(bahnsteige[i].charAt(bahnsteige[i].length() - 1))
                    && bahnsteige[i].matches(".*\\d+.*")) {
                bahnsteigsName = bahnsteige[i];
                bahnsteige[i] =
                        bahnsteige[i].substring(0, bahnsteige[i].length() - 1);
            } else {
                bahnsteigsName = bahnsteige[i];
            }
            bahnhofsName[i] = bahnsteige[i].replaceAll("\\P{L}+", "");
            Station b;
            if (letzterBahnhofsName.equals(bahnhofsName[i])
                    && stations.size() > 0) {
                b = lastB;
            } else {
                letzterBahnhofsName = bahnhofsName[i];
                lastB = b = new Station(stations.size(), letzterBahnhofsName);
                stations.add(b);
                stationMap.put(bahnhofsName[i], b);
            }
            Platform bst = new Platform(b, bahnsteigsName, i);
            b.addBahnsteig(bst);
            platformMap.put(bahnsteigsName, bst);
        }
    }

    public boolean aktualisiereDaten() {
        return connection.update(this);
    }

    public void aktualisiereSimZeit() {
        this.playingTime = connection.aktualisiereSimZeit();
    }

    public Collection<Platform> getBahnsteige() {
        return platformMap.values();
    }

    public int getAnzahlBahnsteige() {
        return platformMap.size();
    }

    public Socket getSocket() {
        return connection.getSocket();
    }

    public String getSignalBoxName() {
        return signalBoxName;
    }

    public int getSignalBoxId() {
        return signalBoxId;
    }

    public Collection<Station> getStations() {
        return stationMap.values();
    }

    /**
     * Create a copy of the internal train list. Changes to the returned list will not affect the original one.
     *
     * @return Copy of actual train list.
     */
    public Collection<Train> getTrains() {
        return this.trains == null ?
                Collections.emptyList() :
                this.trains.getZuege();
    }

    public long getPlayingTime() {
        return playingTime;
    }

    public void setPlayingTime(long playingTime) {
        this.playingTime = playingTime;
    }

    public long getStartingTime() {
        return startingTime;
    }

    public long getLastRefresh() {
        return lastRefresh;
    }

    @Override public String toString() {
        return "SignalBox{" + "signalBoxName='" + signalBoxName + '\''
                + ", signalBoxId=" + signalBoxId
                + ", startingTime=" + startingTime + ", playingTime=" + playingTime
                + ", lastRefresh=" + lastRefresh + '}';
    }

    public Platform getBahnsteigByName(String string) {
        return this.platformMap.get(string);
    }

    public void updateZugliste(XML read) {
        if (null == this.trains) {
            this.trains = Trainlist.parse(read);
        } else {
            this.trains.update(read);
        }
    }

    public void removeZug(Train z) {
        this.trains.remove(z);
    }

    public Map<Integer, Train> getZugMap() {
        return this.trains.toMap();
    }
}
