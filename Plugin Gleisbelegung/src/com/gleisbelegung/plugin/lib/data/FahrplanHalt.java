package com.gleisbelegung.plugin.lib.data;
/*
@author: Manuel Serret
@email: manuel-serret@t-online.de
@contact: Email, Github, STS-Forum

Hinweis: In jeder Klasse werden alle Klassenvariablen erklärt, sowie jede Methode. Solltest du neue Variablen oder Methoden hinzufügen, vergiss bitte nicht sie zu implementieren.

Speichert alle Daten für einen FahrplanHalt
 */

import com.gleisbelegung.plugin.LabelContainer;

import java.util.ArrayList;

public class FahrplanHalt {
    private Zug z;                                  //Der Zug zu dem der Halt gehört
    private long ankuft;                            //Die GEPLANTE Ankunft des Zuges
    private long abfahrt;                           //Die GEPLANTE Abfahrt des Zuges
    private Bahnsteig gleis;                           //Das GEPLANTE Bahnsteig des Zuges
    private Bahnsteig plangleis;                       //Das aktuelle Bahnsteig
    private String flags;                           //Die Flags des Haltes
    private ArrayList<LabelContainer> drawnTo;      //Die LabelContainer, auf welchen der Halt gezeichnet wurde
    private boolean drawable;                       //Ist der Zug zeichenbar
    private boolean crossing;                       //Hat der Zug hier eine Durchfahrt
    private Zug flaggedTrain;                       //Hat der Zug einen nachfolger, wenn ja, dann hier gespeichert, wenn nein dann null

    //Speichert gegebene Seite
    public FahrplanHalt(long abfahrt, Bahnsteig gleis, String flags, Bahnsteig plangleis, long ankuft, Zug z){
        this.z = z;
        this.abfahrt = abfahrt;
        this.gleis = gleis;
        this.flags = flags;
        this.plangleis = plangleis;
        this.ankuft = ankuft;

        this.drawnTo = new ArrayList<>();
        this.drawable = true;
        flaggedTrain = null;

        crossing = flags.contains("D") || flags.equals("D");
    }

    //get-set Ankunft
    public long getAnkuft() {
        return ankuft;
    }
    public void setAnkuft(long ankuft) {
        this.ankuft = ankuft;
    }

    //get-set Abfahrt
    public long getAbfahrt() {
        return abfahrt;
    }
    public void setAbfahrt(long abfahrt) {
        this.abfahrt = abfahrt;
    }

    //get-set Bahnsteig
    public Bahnsteig getBahnsteig() {
        return gleis;
    }
    public void setBahnsteig(Bahnsteig gleis) {
        this.gleis = gleis;
    }

    //get-set Plangleis
    public Bahnsteig getPlanBahnsteig() {
        return plangleis;
    }
    public void setPlanBahnsteig(Bahnsteig plangleis) {
        this.plangleis = plangleis;
    }

    //get-set Flags
    public String getFlags() {
        return flags;
    }
    public void setFlags(String flags) {
        this.flags = flags;
    }

    //get-set FlaggedTrain
    public Zug getFlaggedTrain() {
        return flaggedTrain;
    }
    public void setFlaggedTrain(Zug flaggedTrain) {
        this.flaggedTrain = flaggedTrain;
    }

    //get-set Drawable
    public boolean isDrawable() {
        return drawable;
    }
    public void setDrawable(boolean drawable) {
        this.drawable = drawable;
    }

    //Entferne den Halt überall wo er gemalt wurde
    public void removeDrawnTo() {
        try{
            for(LabelContainer lc : drawnTo){
                if(lc != null && z != null){
                    lc.removeTrain(z);
                }
            }
        } catch (Exception e){
            System.out.println("Fehler koennen passieren :(");
            e.printStackTrace();
        }
        drawnTo = new ArrayList<>();
    }
    //Füge einen LabelContainer hinzu, auf welchem der Halt gezeichnet wurde
    public void addDrawnTo(LabelContainer lc) {
        try{
            lc.addTrain(z);

            if(drawnTo != null){
                this.drawnTo.add(lc);
            }
        } catch (Exception e){
            System.out.println("Fehler koennen passieren :(");
            e.printStackTrace();
        }
    }

    //get DrawnTo
    public LabelContainer getDrawnTo(int index){
        if(drawnTo.size() != 0){
            return  drawnTo.get(index);
        } else{
            return null;
        }
    }
    public ArrayList<LabelContainer> getDrawnTo(){
        return drawnTo;
    }

    public boolean isCrossing() {
        return this.crossing;
    }
    public void setCrossing(boolean crossing) {
        this.crossing = crossing;
    }

    @Override
    public String toString() {
        return "FahrplanHalt{" +
                "ankuft=" + ankuft +
                ", abfahrt=" + abfahrt +
                ", gleis='" + gleis + '\'' +
                ", plangleis='" + plangleis + '\'' +
                ", flags='" + flags + '\'' +
                ", drawnTo=" + drawnTo +
                ", drawable=" + drawable +
                ", crossing=" + crossing +
                ", flaggedTrain=" + flaggedTrain +
                '}';
    }
}
