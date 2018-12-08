package com.gleisbelegung;

import com.gleisbelegung.lib.SignalBox;
import com.gleisbelegung.lib.data.Platform;
import com.gleisbelegung.lib.data.Train;
import com.gleisbelegung.lib.data.TrainStop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;


public class TimeTable {

    private List<TimeTableColumn> cols; //Spalten
    private List<TimeTableRow> rows;    //Reihen
    private List<TimeTableData> refresh;
    private SignalBox signalBox;

    public TimeTable(SignalBox signalBox) {
        this.signalBox = signalBox;

        cols = new ArrayList<TimeTableColumn>();
        rows = new ArrayList<TimeTableRow>();
        refresh = new ArrayList<TimeTableData>();

        for (Platform b : signalBox.getBahnsteige()) {
            cols.add(new TimeTableColumn(b));
        }

        //erste Zeile erstellen, um Inhalt zu haben
        TimeTableRow ttr = new TimeTableRow(signalBox.getPlayingTime());
        rows.add(ttr);
    }

    public void addZug(Train z) {
        for (TrainStop fh : z.getSchedule()) {
            addFahrplanHalt(fh);
        }
        z.setSchwerwiegendesUpdate(false);
        z.setNewTrain(false);
    }

    public void addFahrplanHalt(TrainStop fh) {
        //TODO Ein Fahrplanhalt wird geändert, hier kann die Kollisionsabfrage und Benachrichtigung erfolgen

        int counter = 0;
        synchronized (this.rows) {
            for (TimeTableRow ttr : rows) {
                if (ttr.time >= fh.getTatsaechlicheAnkunft() && ttr.time
                        <= fh.getTatsaechlicheAbfahrt() + 1000 * 60) {
                    for (TimeTableData ttd : ttr.fields) {
                        if (fh.getBahnsteig().getId() == ttd.col.getPlatform()
                                .getId()) {
                            ttd.addZug(fh);
                            if (!refresh.contains(ttd))
                                refresh.add(ttd);
                            counter++;
                        }
                    }
                }
            }
        }
        int haltInMinuten = (int) TimeUnit.MILLISECONDS.toMinutes(
                (fh.getTatsaechlicheAbfahrt() - fh.getTatsaechlicheAnkunft()));
        synchronized (rows) {
            if (haltInMinuten > counter && rows.size() > 0) {

                TimeTableRow lastRow = rows.get(rows.size() - 1);
                while (fh.getDepartureTime() + fh.getZug()
                        .getVerspaetungInMiliSekunden() > lastRow.time) {
                    lastRow = new TimeTableRow(lastRow.time + 1000 * 60);
                    rows.add(lastRow);
                    if (lastRow.time >= fh.getTatsaechlicheAnkunft()
                            && lastRow.time
                            <= fh.getTatsaechlicheAbfahrt() + 1000 * 60) {
                        for (TimeTableData ttd : lastRow.fields) {
                            if (fh.getBahnsteig().getId() == ttd.col
                                    .getPlatform().getId()) {
                                ttd.addZug(fh);
                                if (!refresh.contains(ttd))
                                    refresh.add(ttd);
                            }
                        }
                    }
                }
            }
        }
    }

    public void updateZug(Train z) {
        for (TrainStop fh : z.getSchedule()) {
            updateFahrplanhalt(fh);
        }
    }

    public void updateFahrplanhalt(TrainStop fh) {
        removeFahrplanhalt(fh);
        addFahrplanHalt(fh);
        fh.setNeedUpdate(false);
    }

    public void removeZug(Train z) {
        for (TrainStop fh : z.getSchedule()) {
            //removeFahrplanhalt(fh);
        }
    }

    public void removeFahrplanhalt(TrainStop remove) {
        synchronized (rows) {
            for (TimeTableRow ttr : rows) {
                for (TimeTableData ttd : ttr.fields) {
                    int counter = -1;
                    for (int i = 0; i < ttd.getZuege().size(); i++) {
                        TrainStop fh = ttd.getZuege().get(i);
                        if (fh.getId() == remove.getId()) {
                            counter = i;
                        }
                    }

                    if (counter >= 0)
                        ttd.getZuege().remove(counter);
                    if (!refresh.contains(ttd))
                        refresh.add(ttd);
                }
            }
        }
    }

    public void entferneVergangenheit() {
        synchronized (rows) {
            rows.removeIf(new Predicate<TimeTableRow>() {

                @Override public boolean test(TimeTableRow ttr) {
                    return ttr.time < signalBox.getPlayingTime();
                }

            });
        }
    }

    public Iterator<TimeTableRow> rowIterator() {
        List<TimeTableRow> rows;
        synchronized (this.rows) {
            rows = Collections.unmodifiableList(new ArrayList<>(this.rows));
        }
        return rows.iterator();
    }

    public List<TimeTableData> getRefresh() {
        return refresh;
    }

    private void setToRefresh(TimeTableData ttd, int row, int col) {
        refresh.add(ttd);
    }

    public List<TimeTableRow> getRows() {
        return rows;
    }

    class TimeTableColumn {

        private Platform platform;

        TimeTableColumn(Platform b) {
            this.platform = b;
        }

        public Platform getPlatform() {
            return platform;
        }
    }

    class TimeTableRow {

        long time;
        private List<TimeTableData> fields;
        private boolean isNewRow;

        TimeTableRow(long time) {
            this.time = time;
            fields = new ArrayList<>();

            for (TimeTableColumn ttc : cols) {
                TimeTableData ttd = new TimeTableData(ttc, this);
                fields.add(ttd);
            }

            isNewRow = true;
        }

        public Iterator<TimeTableData> dataIterator() {
            return fields.iterator();
        }

        public boolean isNewRow() {
            return isNewRow;
        }

        public void setNewRow(boolean newRow) {
            isNewRow = newRow;
        }
    }

    class TimeTableData {

        private List<TrainStop> zuege;
        private TimeTableRow row;
        private TimeTableColumn col;
        private LabelContainer labelContainer;

        TimeTableData(TimeTableColumn col, TimeTableRow row) {
            this.col = col;
            this.row = row;
            this.zuege = new ArrayList<TrainStop>();
            labelContainer = null;
        }

        public TimeTableColumn getCol() {
            return col;
        }

        public TimeTableRow getRow() {
            return row;
        }

        public List<TrainStop> getZuege() {
            return zuege;
        }

        public LabelContainer getLabelContainer() {
            return labelContainer;
        }

        public void setLabelContainer(LabelContainer labelContainer) {
            this.labelContainer = labelContainer;
        }

        public void addZug(TrainStop fh) {
            boolean add = true;
            if (fh.getFlaggedTrain() != null) {
                if (fh.getFlaggedTrain().getSchedule().size() >= 1) {
                    if (zuege.contains(fh.getFlaggedTrain().getFahrplan(0))) {
                        add = false;
                    }
                }
            }

            if (add)
                zuege.add(fh);
        }
    }
}

