package com.gleisbelegung;

import com.gleisbelegung.lib.Stellwerk;
import com.gleisbelegung.lib.data.Bahnsteig;
import com.gleisbelegung.lib.data.FahrplanHalt;
import com.gleisbelegung.lib.data.Zug;

import java.util.ArrayList;
import java.util.List;

public class TimeTable {
    private List<TimeTableColumn> cols; //Spalten
    private List<TimeTableRow> rows;    //Reihen
    private Stellwerk stellwerk;

    public TimeTable(Stellwerk stellwerk){
        this.stellwerk = stellwerk;

        cols = new ArrayList<TimeTableColumn>();
        rows = new ArrayList<TimeTableRow>();

        for(Bahnsteig b : stellwerk.getBahnsteige()){
            cols.add(new TimeTableColumn(b, rows));
        }

        //erste Zeile erstellen, um Inhalt zu haben
        TimeTableRow ttr = new TimeTableRow(stellwerk.getSpielzeit());
        ttr.cols.addAll(cols);
        rows.add(ttr);
    }


    public void addTrain(Zug z){
        System.out.println("new train");

        for(FahrplanHalt fh : z.getFahrplan()){
            addFahrplanHalt(fh);
        }
    }

    public void addFahrplanHalt(FahrplanHalt fh){
        //TODO Ein Zug wird geändert, hier kann die Kollisionsabfrage und Benachrichtigung erfolgen
        System.out.println("new FH");

        TimeTableRow lastRow = rows.get(rows.size() - 1);
        while(fh.getAbfahrt() + fh.getZug().getVerspaetungInMiliSekunden() > lastRow.time){
            lastRow = new TimeTableRow(lastRow.time + 1000*60);
            rows.add(lastRow);
        }
    }

    public void removeTrain(Zug z){

    }

    public void updateTrain(Zug z){
        removeTrain(z);
        addTrain(z);
    }
}


class TimeTableColumn{
    Bahnsteig bahnsteig;
    List<TimeTableRow> rows;

    TimeTableColumn(Bahnsteig b, List<TimeTableRow> rows){
        this.rows = rows;
        bahnsteig = b;
    }
}

class TimeTableRow{
    long time;
    List<TimeTableColumn> cols;

    TimeTableRow(long time){
        cols = new ArrayList<TimeTableColumn>();
        this.time = time;

    }
}

class TimeTableData{
    List<Zug> zuege;
    TimeTableRow row;
    TimeTableColumn col;

    TimeTableData(){
        zuege = new ArrayList<Zug>();
    }
}
