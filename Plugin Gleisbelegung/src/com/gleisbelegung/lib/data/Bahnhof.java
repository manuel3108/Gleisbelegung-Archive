package com.gleisbelegung.lib.data;

import com.gleisbelegung.LabelContainer;

import java.util.ArrayList;

public class Bahnhof {
    private int id;
    private String name;
    private ArrayList<Bahnsteig> bahnsteige;
    private ArrayList<LabelContainer> bahnhofLabel;
    private boolean sichtbar;

    public Bahnhof(int id, String name){
        this.id = id;
        this.name = name;
        this.bahnsteige = new ArrayList<>();
        bahnhofLabel = new ArrayList<>();
    }

    public int getId(){
        return id;
    }
    public String getName() {
        return name;
    }
    public ArrayList<Bahnsteig> getBahnsteige() {
        return bahnsteige;
    }
    public Bahnsteig getBahnsteig(int index){
        return bahnsteige.get(index);
    }
    public int getAnzahlBahnsteige(){
        return bahnsteige.size();
    }

    //bahnsteig zählt als sichtbar sobald alle Bahnsteige sichtbar ist
    public boolean isSichtbar() {
        for (Bahnsteig b : bahnsteige) {
            if (!b.isSichtbar())
                return false;

        }
        return true;
    }

    //alle Bahnsteige des bahnhofs werden auf (nicht) sichtbar gesetzt
    public void setSichtbar(boolean sichtbar) {
        for (Bahnsteig b : bahnsteige)
            b.setSichtbar(sichtbar);
    }

    @Override
    public String toString() {
        return "Bahnhof{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", bahnsteige=" + bahnsteige +
                '}';
    }

    public ArrayList<LabelContainer> getBahnhofLabel() {
        return bahnhofLabel;
    }
    public void addBahnhofLabel(LabelContainer bahnhofLabel) {
        this.bahnhofLabel.add(bahnhofLabel);
    }
}
