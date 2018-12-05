package com.gleisbelegung.lib.data;
/*
@author: Manuel Serret
@email: manuel-serret@t-online.de
@contact: Email, Github, STS-Forum

Hinweis: In jeder Klasse werden alle Klassenvariablen erklärt, sowie jede Methode. Solltest du neue Variablen oder Methoden hinzufügen, vergiss bitte nicht sie zu implementieren.

Speichert alle Daten für einen FahrplanHalt
 */

import com.gleisbelegung.LabelContainer;
import java.util.ArrayList;

public class FahrplanHalt {
    private Zug z;                                  //Der Zug zu dem der Halt gehört
    private long ankuft;                            //Die GEPLANTE Ankunft des Zuges
    private long abfahrt;                           //Die GEPLANTE Abfahrt des Zuges
    private Bahnsteig gleis;                           //Das GEPLANTE Bahnsteig des Zuges
    private Bahnsteig plangleis;                       //Das aktuelle Bahnsteig
    
    /**
     * @NotNull
     */
    private final ScheduleFlags flags;                 //Die Flags des Haltes
    private ArrayList<LabelContainer> drawnTo;      //Die LabelContainer, auf welchen der Halt gezeichnet wurde
    private boolean needUpdate;
    private static long idCounter = 0;
    private long id;

    //Speichert gegebene Seite
    public FahrplanHalt(long abfahrt, Bahnsteig gleis, ScheduleFlags flags, Bahnsteig plangleis, long ankuft, Zug z){
        this.z = z;
        this.abfahrt = abfahrt;
        this.gleis = gleis;
        this.flags = flags;
        this.plangleis = plangleis;
        this.ankuft = ankuft;

        this.drawnTo = new ArrayList<>();

        needUpdate = true;

        id = idCounter;
        idCounter++;
    }

    //get zug
    public Zug getZug() {
        return z;
    }

    //get-set Ankunft
    public long getAnkuft() {
        return ankuft;
    }
    public long getTatsaechlicheAnkunft(){
        if(ankuft == 0 && z.getVorgaenger() != null){
            return getVorgaenger().getTatsaechlicheAnkunft();
        }
        return  ankuft;
    }
    public void setAnkuft(long ankuft) {
        this.ankuft = ankuft;
    }

    //get-set Abfahrt
    public long getAbfahrt() {
        return abfahrt;
    }
    public long getTatsaechlicheAbfahrt(){
        if(abfahrt == 0 && z.getNachfolger() != null){
            return getNachfolger().getTatsaechlicheAbfahrt();
        }
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

    //get Flags
    public ScheduleFlags getFlags() {
        return flags;
    }

    //get
    public Zug getFlaggedTrain() {
        return flags.getE(); // f,k auch?
    }
   

    //Entferne den Halt überall wo er gemalt wurde
    public void removeDrawnTo() {
        try{
            for (LabelContainer lc : drawnTo) {
                if (lc != null) {
                    lc.removeTrain(z);
                }
                if (lc != null && z.getVorgaenger() != null && z.getVorgaenger().getFahrplan() != null && !z.getVorgaenger().getFahrplan().isEmpty() )
                    lc.removeTrain(z.getVorgaenger());
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
            if (drawnTo != null) {
                this.drawnTo.add(lc);
            }
            if (z.getNachfolger() != null) {
                lc.addTrain(z.getNachfolger());
                if (drawnTo != null && z.getNachfolger().getFahrplan() != null && !z.getNachfolger().getFahrplan().isEmpty() && z.getNachfolger().getFahrplan(0) != null) {
                    z.getNachfolger().getFahrplan(0).drawnTo.add(lc);
                }
            }
        } catch (Exception e) {
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

    public FahrplanHalt getVorgaenger() {
        return z.getVorgaenger() == null ? null : z.getVorgaenger().getFahrplanHalt(this);
    }
    
    private FahrplanHalt getNachfolger() {
  		return z.getNachfolger() == null ? null : z.getNachfolger().getFahrplanHalt(this);
  	}

    public boolean isNeedUpdate() {
        return needUpdate;
    }
    public void setNeedUpdate(boolean needUpdate) {
        this.needUpdate = needUpdate;
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
                '}';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
