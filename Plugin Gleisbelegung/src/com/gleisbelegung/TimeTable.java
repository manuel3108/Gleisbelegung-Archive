package com.gleisbelegung;

import com.gleisbelegung.lib.Stellwerk;
import com.gleisbelegung.lib.data.Bahnsteig;
import com.gleisbelegung.lib.data.FahrplanHalt;
import com.gleisbelegung.lib.data.Zug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TimeTable {

	class TimeTableColumn{
	    private Bahnsteig bahnsteig;
	
	    TimeTableColumn(Bahnsteig b){
	        this.bahnsteig = b;
	    }
	    
		public Bahnsteig getBahnsteig() {
			return bahnsteig;
		}
	}
	
	class TimeTableRow {
	    long time;
	    private List<TimeTableData> fields;
	
	    TimeTableRow(long time) {
	        this.time = time;
	        fields = new ArrayList<>();
	
	        for (TimeTableColumn ttc : cols){
	            TimeTableData ttd = new TimeTableData(ttc, this);
	            fields.add(ttd);
	        }
	    }

		public Iterator<TimeTableData> dataIterator() {
			return fields.iterator();
		}
	}
	
	class TimeTableData {
	    private List<FahrplanHalt> zuege;
	    private TimeTableRow row;
	    private TimeTableColumn col;
	
	    TimeTableData(TimeTableColumn col, TimeTableRow row) {
	        this.col = col;
	        this.row = row;
	        this.zuege =  new ArrayList<FahrplanHalt>();
	    }

		public TimeTableColumn getCol() {
			return col;
		}

		public TimeTableRow getRow() {
			return row;
		}

		public List<FahrplanHalt> getZuege() {
			return zuege;
		}
	}
	
	
    private List<TimeTableColumn> cols; //Spalten
    private List<TimeTableRow> rows;    //Reihen
    private List<TimeTableData> refresh;
    private Stellwerk stellwerk;

    public TimeTable(Stellwerk stellwerk){
        this.stellwerk = stellwerk;

        cols = new ArrayList<TimeTableColumn>();
        rows = new ArrayList<TimeTableRow>();
        refresh = new ArrayList<TimeTableData>();

        for(Bahnsteig b : stellwerk.getBahnsteige()){
            cols.add(new TimeTableColumn(b));
        }

        //erste Zeile erstellen, um Inhalt zu haben
        TimeTableRow ttr = new TimeTableRow(stellwerk.getSpielzeit());
        rows.add(ttr);
    }


    public void addZug(Zug z){
        for(FahrplanHalt fh : z.getFahrplan()){
            addFahrplanHalt(fh);
        }
        z.setSchwerwiegendesUpdate(false);
        z.setNewTrain(false);
    }

    public void addFahrplanHalt(FahrplanHalt fh){
        //TODO Ein Zug wird geändert, hier kann die Kollisionsabfrage und Benachrichtigung erfolgen

        int counter = 0;
        for(TimeTableRow ttr : rows) {
            if(ttr.time >= fh.getTatsaechlicheAnkunft() && ttr.time <= fh.getTatsaechlicheAbfahrt() + 1000*60){
                for(TimeTableData ttd : ttr.fields){
                    if(fh.getBahnsteig().getId() == ttd.col.getBahnsteig().getId()){
                        ttd.getZuege().add(fh);
                        if(!refresh.contains(ttd)) refresh.add(ttd);
                        counter++;
                    }
                }
            }
        }

        int haltInMinuten = (int)((fh.getTatsaechlicheAbfahrt() - fh.getTatsaechlicheAnkunft())/(1000*60));
        if(haltInMinuten > counter){
            TimeTableRow lastRow = rows.get(rows.size() - 1);
            while(fh.getAbfahrt() + fh.getZug().getVerspaetungInMiliSekunden() > lastRow.time){
                lastRow = new TimeTableRow(lastRow.time + 1000*60);
                synchronized(rows) {
                	rows.add(lastRow);
                }

                if(lastRow.time >= fh.getTatsaechlicheAnkunft() && lastRow.time <= fh.getTatsaechlicheAbfahrt() + 1000*60){
                    for(TimeTableData ttd : lastRow.fields){
                        if(fh.getBahnsteig().getId() == ttd.col.getBahnsteig().getId()){
                            ttd.getZuege().add(fh);
                            if(!refresh.contains(ttd)) {
                            	refresh.add(ttd);
                            }
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
        fh.setNeedUpdate(false);
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
                for (int i = 0; i < ttd.getZuege().size(); i++) {
                    FahrplanHalt fh = ttd.getZuege().get(i);
                    if(fh.getId() == remove.getId()){
                        counter = i;
                    }
                }

                if(counter >= 0) ttd.getZuege().remove(counter);
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
        	synchronized(this.rows) {
        		rows.remove(ttr);
        	}
        }
    }


	public Iterator<TimeTableRow> rowIterator() {
		List<TimeTableRow> rows;
		synchronized(this.rows) {
			rows = Collections.unmodifiableList(this.rows);
		}
		return rows.iterator();
	}
}

