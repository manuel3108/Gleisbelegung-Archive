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

        int counter = 0;
        for(TimeTableRow ttr : rows) {
            if(ttr.time >= fh.getTatsaechlicheAnkunf() && ttr.time <= fh.getTatsaechlicheAbfahrt() + 1000*60){
                for(TimeTableData ttd : ttr.fields){
                    if(fh.getBahnsteig().getId() == ttd.col.bahnsteig.getId()){
                        ttd.zuege.add(fh);
                        counter++;
                    }
                }
            }
        }

        int haltInMinuten = (int)((fh.getTatsaechlicheAbfahrt() - fh.getTatsaechlicheAnkunf())/(1000*60));
        if(haltInMinuten > counter){
            TimeTableRow lastRow = rows.get(rows.size() - 1);
            while(fh.getAbfahrt() + fh.getZug().getVerspaetungInMiliSekunden() > lastRow.time){
                lastRow = new TimeTableRow(lastRow.time + 1000*60, cols);
                rows.add(lastRow);

                if(lastRow.time >= fh.getTatsaechlicheAnkunf() && lastRow.time <= fh.getTatsaechlicheAbfahrt() + 1000*60){
                    for(TimeTableData ttd : lastRow.fields){
                        if(fh.getBahnsteig().getId() == ttd.col.bahnsteig.getId()){
                            ttd.zuege.add(fh);
                        }
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
        removeFahrplanhalt(fh);
        addFahrplanHalt(fh);
    }

    public void removeZug(Zug z){
        for(FahrplanHalt fh : z.getFahrplan()){
            //removeFahrplanhalt(fh);
        }
    }

    public void removeFahrplanhalt(FahrplanHalt remove){
        for(TimeTableRow ttr : rows){
            for(TimeTableData ttd : ttr.fields){
                int counter = -1;
                for (int i = 0; i < ttd.zuege.size(); i++) {
                    FahrplanHalt fh = ttd.zuege.get(i);
                    if(fh.getId() == remove.getId()){
                        counter = i;
                    }
                }

                if(counter >= 0) ttd.zuege.remove(counter);
            }
        }
    }

    public void entferneVergangenheit(){
        List<TimeTableRow> remove = new ArrayList<TimeTableRow>();

        for(TimeTableRow ttr : rows){
            if(ttr.time < stellwerk.getSpielzeit()){
                remove.add(ttr);
            }
        }

        for(TimeTableRow ttr : remove){
            rows.remove(ttr);
        }
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
    List<FahrplanHalt> zuege;
    TimeTableRow row;
    TimeTableColumn col;

    TimeTableData(TimeTableColumn col, TimeTableRow row){
        this.col = col;
        this.row = row;
        zuege = new ArrayList<FahrplanHalt>();
    }
}
