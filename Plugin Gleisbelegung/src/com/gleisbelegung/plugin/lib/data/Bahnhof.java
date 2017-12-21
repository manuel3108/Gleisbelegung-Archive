package com.gleisbelegung.plugin.lib.data;

import java.util.ArrayList;

public class Bahnhof {
    private int id;
    private String name;
    private ArrayList<Bahnsteig> bahnsteige;

    public Bahnhof(int id, String name){
        this.id = id;
        this.name = name;
        this.bahnsteige = new ArrayList<>();
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
}
