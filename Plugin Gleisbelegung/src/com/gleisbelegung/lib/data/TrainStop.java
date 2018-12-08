package com.gleisbelegung.lib.data;
/*
@author: Manuel Serret
@email: manuel-serret@t-online.de
@contact: Email, Github, STS-Forum

Hinweis: In jeder Klasse werden alle Klassenvariablen erklärt, sowie jede Methode. Solltest du neue Variablen oder Methoden hinzufügen, vergiss bitte nicht sie zu implementieren.

Speichert alle Daten für einen TrainStop
 */

import com.gleisbelegung.LabelContainer;

import java.util.ArrayList;


public class TrainStop {

    private static long idCounter = 0;
    /**
     * @NotNull
     */
    private final ScheduleFlags flags;                 //Die Flags des Haltes
    private Train train;
    //Der Train zu dem der Halt gehört
    private long arrivalTime;
    //Die GEPLANTE Ankunft des Zuges
    private long departureTime;
    //Die GEPLANTE Abfahrt des Zuges
    private Platform platform;
    //Das GEPLANTE Platform des Zuges
    private Platform scheduledPlatform;                       //Das aktuelle Platform
    private ArrayList<LabelContainer> drawnTo;
    //Die LabelContainer, auf welchen der Halt gezeichnet wurde
    private boolean needUpdate;
    private long id;

    //Speichert gegebene Seite
    public TrainStop(long departureTime, Platform platform, ScheduleFlags flags,
            Platform scheduledPlatform, long arrivalTime, Train train) {
        this.train = train;
        this.departureTime = departureTime;
        this.platform = platform;
        this.flags = flags;
        this.scheduledPlatform = scheduledPlatform;
        this.arrivalTime = arrivalTime;

        this.drawnTo = new ArrayList<>();

        needUpdate = true;

        id = idCounter;
        idCounter++;
    }

    //get zug
    public Train getZug() {
        return train;
    }

    //get-set Ankunft
    public long getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(long arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public long getTatsaechlicheAnkunft() {
        if (arrivalTime == 0 && train.getVorgaenger() != null) {
            return getVorgaenger().getTatsaechlicheAnkunft();
        }

        return arrivalTime + train.getVerspaetungInMiliSekunden();
    }

    //get-set Abfahrt
    public long getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(long departureTime) {
        this.departureTime = departureTime;
    }

    public long getTatsaechlicheAbfahrt() {
        if (departureTime == 0 && train.getNachfolger() != null) {
            if (getNachfolger() == null) {
                return getTatsaechlicheAnkunft();
            }
            return getNachfolger().getTatsaechlicheAbfahrt();
        }

        if (train.getVerspaetungInMinuten() <= 0) {
            return departureTime;
        } else if (train.getVerspaetungInMinuten() <= 3) {
            return departureTime + train.getVerspaetungInMiliSekunden();
        } else {
            return arrivalTime + train.getVerspaetungInMiliSekunden() + 2 * 60 * 1000;
        }
    }

    //get-set Platform
    public Platform getBahnsteig() {
        return platform;
    }

    public void setBahnsteig(Platform gleis) {
        this.platform = gleis;
    }

    //get-set Plangleis
    public Platform getPlanBahnsteig() {
        return scheduledPlatform;
    }

    public void setPlanBahnsteig(Platform plangleis) {
        this.scheduledPlatform = plangleis;
    }

    //get Flags
    public ScheduleFlags getFlags() {
        return flags;
    }

    //get
    public Train getFlaggedTrain() {
        return flags.getE(); // f,k auch?
    }


    //Entferne den Halt überall wo er gemalt wurde
    public void removeDrawnTo() {
        try {
            for (LabelContainer lc : drawnTo) {
                if (lc != null) {
                    lc.removeTrain(train);
                }
                if (lc != null && train.getVorgaenger() != null
                        && train.getVorgaenger().getSchedule() != null && !train
                        .getVorgaenger().getSchedule().isEmpty())
                    lc.removeTrain(train.getVorgaenger());
            }
        } catch (Exception e) {
            System.out.println("Fehler koennen passieren :(");
            e.printStackTrace();
        }
        drawnTo = new ArrayList<>();
    }

    //Füge einen LabelContainer hinzu, auf welchem der Halt gezeichnet wurde
    public void addDrawnTo(LabelContainer lc) {
        try {
            lc.addTrain(train);
            if (drawnTo != null) {
                this.drawnTo.add(lc);
            }
            if (train.getNachfolger() != null) {
                lc.addTrain(train.getNachfolger());
                if (drawnTo != null && train.getNachfolger().getSchedule() != null
                        && !train.getNachfolger().getSchedule().isEmpty()
                        && train.getNachfolger().getFahrplan(0) != null) {
                    train.getNachfolger().getFahrplan(0).drawnTo.add(lc);
                }
            }
        } catch (Exception e) {
            System.out.println("Fehler koennen passieren :(");
            e.printStackTrace();
        }
    }

    //get DrawnTo
    public LabelContainer getDrawnTo(int index) {
        if (drawnTo.size() != 0) {
            return drawnTo.get(index);
        } else {
            return null;
        }
    }

    public ArrayList<LabelContainer> getDrawnTo() {
        return drawnTo;
    }

    public TrainStop getVorgaenger() {
        return train.getVorgaenger() == null ?
                null :
                train.getVorgaenger().getFahrplanHalt(this);
    }

    private TrainStop getNachfolger() {
        return train.getNachfolger() == null ?
                null :
                train.getNachfolger().getFahrplanHalt(this);
    }

    public boolean isNeedUpdate() {
        return needUpdate;
    }

    public void setNeedUpdate(boolean needUpdate) {
        this.needUpdate = needUpdate;
    }

    @Override public String toString() {
        return "TrainStop{" + "arrivalTime=" + arrivalTime + ", departureTime=" + departureTime
                + ", platform='" + platform + '\'' + ", scheduledPlatform='" + scheduledPlatform
                + '\'' + ", flags='" + flags + '\'' + ", drawnTo=" + drawnTo
                + '}';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
