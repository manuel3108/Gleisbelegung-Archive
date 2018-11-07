package com.gleisbelegung;

import com.gleisbelegung.lib.Stellwerk;
import com.gleisbelegung.lib.data.Bahnsteig;
import com.gleisbelegung.lib.data.Zug;

import java.util.ArrayList;
import java.util.List;

public class TimeTable {
    private List<TimeTableColumn> cols;
    private List<TimeTableRow> rows;
    private Stellwerk stellwerk;

    public TimeTable(Stellwerk stellwerk){
        this.stellwerk = stellwerk;

        cols = new ArrayList<TimeTableColumn>();
        rows = new ArrayList<TimeTableRow>();

        for(Bahnsteig b : stellwerk.getBahnsteige()){
            cols.add(new TimeTableColumn(b));
        }

        //erste Zeile erstellen, um Inhalt zu haben
        TimeTableRow ttr = new TimeTableRow(stellwerk.getSpielzeit());
        ttr.cols.addAll(cols);
        rows.add(ttr);

        for(TimeTableColumn col : cols){
            col.rows.add(ttr);
        }
    }


    public void addTrain(Zug z){
        //TODO Ein Zug wird geändert, hier kann die Kollisionsabfrage und Benachrichtigung erfolgen
        //TODO eigentlcih müsste hier immer nur nach einem Fahrplanhalt gefragt werden
        System.out.println("new train");
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

    TimeTableColumn(Bahnsteig b){
        rows = new ArrayList<TimeTableRow>();
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
