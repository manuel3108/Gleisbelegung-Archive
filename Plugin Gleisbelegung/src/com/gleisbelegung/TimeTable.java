package com.gleisbelegung;

import com.gleisbelegung.lib.Stellwerk;
import com.gleisbelegung.lib.data.Bahnsteig;
import com.gleisbelegung.lib.data.FahrplanHalt;
import com.gleisbelegung.lib.data.Zug;

import java.util.ArrayList;
import java.util.List;

public class TimeTable {
    List<TimeTableColumn> cols; //Spalten
    List<TimeTableRow> rows;    //Reihen
    private Stellwerk stellwerk;

    public TimeTable(Stellwerk stellwerk){
        this.stellwerk = stellwerk;

        cols = new ArrayList<TimeTableColumn>();
        rows = new ArrayList<TimeTableRow>();

        for(Bahnsteig b : stellwerk.getBahnsteige()){
            cols.add(new TimeTableColumn(b, rows));
        }

        //erste Zeile erstellen, um Inhalt zu haben
        TimeTableRow ttr = new TimeTableRow(stellwerk.getSpielzeit(), cols);
        rows.add(ttr);
    }


    public void addZug(Zug z){
        for(FahrplanHalt fh : z.getFahrplan()){
            addFahrplanHalt(fh);
        }
    }

    public void addFahrplanHalt(FahrplanHalt fh){
        //TODO Ein Zug wird geändert, hier kann die Kollisionsabfrage und Benachrichtigung erfolgen

        TimeTableRow lastRow = rows.get(rows.size() - 1);
        while(fh.getAbfahrt() + fh.getZug().getVerspaetungInMiliSekunden() > lastRow.time){
            lastRow = new TimeTableRow(lastRow.time + 1000*60, cols);
            rows.add(lastRow);

            if(lastRow.time >= fh.getTatsaechlicheAnkunf() && lastRow.time <= fh.getTatsaechlicheAbfahrt()){
                for(TimeTableData ttd : lastRow.fields){
                    if(fh.getBahnsteig().getId() == ttd.col.bahnsteig.getId()){
                        ttd.zuege.add(fh.getZug());
                    }
                }
            }
        }
    }

    public void updateZug(Zug z){
        for(FahrplanHalt fh : z.getFahrplan()){
            updateFahrplanhalt(fh);
        }
    }

    public void updateFahrplanhalt(FahrplanHalt fh){

    }

    public void removeZug(Zug z){
        for(FahrplanHalt fh : z.getFahrplan()){
            removeFahrplanhalt(fh);
        }
    }

    public void removeFahrplanhalt(FahrplanHalt fh){
        //kein Ahnung
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
    List<TimeTableData> fields;

    TimeTableRow(long time, List<TimeTableColumn> cols){
        this.cols = cols;
        this.time = time;
        fields = new ArrayList<>();

        for(TimeTableColumn ttc : cols){
            TimeTableData ttd = new TimeTableData(ttc, this);
            fields.add(ttd);
        }
    }
}

class TimeTableData{
    List<Zug> zuege;
    TimeTableRow row;
    TimeTableColumn col;

    TimeTableData(TimeTableColumn col, TimeTableRow row){
        this.col = col;
        this.row = row;
        zuege = new ArrayList<Zug>();
    }
}
