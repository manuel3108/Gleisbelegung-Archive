package com.gleisbelegung;

import com.gleisbelegung.lib.SignalBox;
import com.gleisbelegung.lib.data.Station;
import com.gleisbelegung.lib.data.Train;
import com.gleisbelegung.lib.data.TrainStop;
import com.sun.javafx.geom.Vec2d;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.text.SimpleDateFormat;
import java.util.*;


public class Stellwerksuebersicht {

    private SignalBox signalBox;
    private Pane content;
    private Station bewegeStation;
    private ArrayList<Label> bahnhofsLabel;
    private Map<Label, Station> bahnhofLabelMap = new HashMap<>();
    private ArrayList<Label> uebergabePunkte;
    private ArrayList<Label> gezeichneteBahnhofsInformationen;

    public Stellwerksuebersicht(SignalBox signalBox) {
        this.signalBox = signalBox;
        bahnhofsLabel = new ArrayList<>();
        uebergabePunkte = new ArrayList<>();
        gezeichneteBahnhofsInformationen = new ArrayList<>();
        int counter = 0;

        /*gc.setFill(Paint.valueOf("#fff"));
        gc.setStroke(Paint.valueOf("#4e8abf"));//blau
        gc.setFont(Font.font(Settings.fontSize));*/

        for (Station b : signalBox.getStations()) {
            if (b.getPlatformConnections() != null)
                b.clearBahnhofVerbindungen();

            String text = "";
            if (b.getName().equals(""))
                text = signalBox.getSignalBoxName();
            else
                text = b.getName();

            Text temp = new Text(text);
            temp.setFont(Font.font(Settings.fontSize));
            temp.applyCss();

            Label l = new Label(text);
            l.setMinWidth(200);
            l.setPrefWidth(200);
            l.setMaxWidth(200);
            l.setMinHeight(
                    Math.round(temp.getBoundsInLocal().getHeight()) + 10);
            l.setPrefHeight(
                    Math.round(temp.getBoundsInLocal().getHeight()) + 10);
            l.setMaxHeight(
                    Math.round(temp.getBoundsInLocal().getHeight()) + 10);
            l.setAlignment(Pos.CENTER);
            l.setStyle(
                    "-fx-text-fill: #fff; -fx-border-width: 2; -fx-border-color: #4e8abf"); //blau
            l.setFont(Font.font(Settings.fontSize));
            if (b.getPosition().x != 0 && b.getPosition().y != 0) {
                l.setTranslateX(b.getPosition().x);
                l.setTranslateY(b.getPosition().y);
            } else {
                l.setTranslateX(b.getPosition().x);
                l.setTranslateY(b.getPosition().y + counter * 50);
                b.getPosition().y = b.getPosition().y + counter * 50;
            }
            l.setOnMouseEntered(mouse -> zeigeBahnhofsInformationen(b));
            l.setOnMouseClicked(mouse -> {
                if (mouse.getButton() == MouseButton.SECONDARY) {
                    b.einstellungen(b.getBahnhofTeile(0));
                }
            });
            l.setOnMouseExited(mouse -> entferneBahnhofsInformationen());
            bahnhofsLabel.add(l);
            bahnhofLabelMap.put(l, b);

            Platform.runLater(() -> {
                if (content != null)
                    content.getChildren().add(l);
            });

            counter++;
        }

        aktualisiereBahnhofsNamen();

        content = new Pane();
        content.setStyle("-fx-background-color: #303030");
        content.setOnDragDetected(mouse -> {
            Station b = getBahnhof(mouse.getX(), mouse.getY());

            if (b != null) {
                bewegeStation = b;
            } else {
                bewegeStation = null;
            }
        });
        content.setOnMouseReleased(mouse -> {
            if (bewegeStation != null)
                bewegeStation = null;
        });
        content.setOnMouseDragged(mouse -> {
            if (bewegeStation != null) {
                bewegeStation.getPosition().x = mouse.getX() - 100;
                bewegeStation.getPosition().y = mouse.getY() - 15;
                bahnhofsLabel.get(bewegeStation.getId())
                        .setTranslateX(bewegeStation.getPosition().x);
                bahnhofsLabel.get(bewegeStation.getId())
                        .setTranslateY(bewegeStation.getPosition().y);
                updateLinie(bewegeStation);
                zeichneZuege();
                verschiebeBahnhofsInformationen(bewegeStation);
            }
        });

        erstelleVerbindungsLinien();
    }

    private Station getBahnhof(double x, double y) {
        for (Station b : signalBox.getStations()) {
            String text;

            if (b.getNameByUser().equals("")) {
                if (b.getName().equals(""))
                    text = signalBox.getSignalBoxName();
                else
                    text = b.getName();
            } else {
                if (b.getName().equals(""))
                    text = b.getNameByUser() + " (" + signalBox
                            .getSignalBoxName() + ")";
                else
                    text = b.getNameByUser() + " (" + b.getName() + ")";
            }


            Text temp = new Text(text);
            temp.setFont(Font.font(Settings.fontSize));
            temp.applyCss();

            if (x > b.getPosition().x && x < b.getPosition().x + 200 && y > b.getPosition().y
                    && y < b.getPosition().y + temp.getBoundsInLocal().getHeight()
                    + 10) {
                return b;
            }
        }
        return null;
    }

    public void updateUi(double stageWidth, double stageHight) {
        /*canvas.setWidth(stageWidth - Settings.trainInformationWidth - 20);
        canvas.setHeight(stageHight - 80);*/

        update();
    }

    public void update() {
        zeichneZuege();
    }

    private void erstelleVerbindungsLinien() {
        for (Train z : signalBox.getTrains()) {
            if (z.getSchedule().size() > 1) {
                TrainStop letzterFH = null;
                for (TrainStop fh : z.getSchedule()) {
                    if (letzterFH != null) {
                        if (!letzterFH.getBahnsteig().getStation()
                                .getVerbindungsBahnhoefe()
                                .contains(fh.getBahnsteig().getStation())) {
                            neueLinie(letzterFH.getBahnsteig().getStation(),
                                    fh.getBahnsteig().getStation());
                        }
                    }

                    letzterFH = fh;
                }
            }
        }
    }

    private void neueLinie(Station b1, Station b2) {
        Vec2d linienPosB1 = new Vec2d();
        Vec2d linienPosB2 = new Vec2d();

        Line linie = new Line(linienPosB1.x, linienPosB1.y, linienPosB2.x,
                linienPosB2.y);
        linie.setStroke(Paint.valueOf("#4e8abf"));
        linie.setStrokeWidth(2);

        Platform.runLater(() -> content.getChildren().add(linie));

        double verbindungsLaenge = berechneVerbindungsLaenge(b1, b2);
        b1.addBahnhofVerbindung(b2, verbindungsLaenge, linie, linienPosB1);
        b2.addBahnhofVerbindung(b1, verbindungsLaenge, linie, linienPosB2);

        updateLinie(b1);
        updateLinie(b2);
    }

    private void updateLinie(Station b) {
        Set<Station> verbindungsBahnhoefe = b.getVerbindungsBahnhoefe();

        for (Station vb : verbindungsBahnhoefe) {
            Line linie = b.getLinie(vb);
            if (linie == null) {
                continue;
            }
            Vec2d linienPosB1 = new Vec2d();
            Vec2d linienPosB2 = new Vec2d();

            if (b.getPosition().x < vb.getPosition().x && b.getPosition().x + 200 < vb
                    .getPosition().x) {
                linienPosB1.x = b.getPosition().x + 200;
                linienPosB1.y = b.getPosition().y + 15;
                linienPosB2.x = vb.getPosition().x;
                linienPosB2.y = vb.getPosition().y + 15;
            } else if (b.getPosition().x > vb.getPosition().x
                    && b.getPosition().x > vb.getPosition().x + 200) {
                linienPosB1.x = b.getPosition().x;
                linienPosB1.y = b.getPosition().y + 15;
                linienPosB2.x = vb.getPosition().x + 200;
                linienPosB2.y = vb.getPosition().y + 15;
            } else if (b.getPosition().y < vb.getPosition().y) {
                linienPosB1.x = b.getPosition().x + 100;
                linienPosB1.y = b.getPosition().y + 35;
                linienPosB2.x = vb.getPosition().x + 100;
                linienPosB2.y = vb.getPosition().y;
            } else if (b.getPosition().y > vb.getPosition().y) {
                linienPosB1.x = b.getPosition().x + 100;
                linienPosB1.y = b.getPosition().y;
                linienPosB2.x = vb.getPosition().x + 100;
                linienPosB2.y = vb.getPosition().y + 35;
            }

            linie.setStartX(linienPosB1.x);
            linie.setStartY(linienPosB1.y);
            linie.setEndX(linienPosB2.x);
            linie.setEndY(linienPosB2.y);

            b.setLinienPos(vb, linienPosB1);
            vb.setLinienPos(b, linienPosB2);
        }
    }

    private double berechneVerbindungsLaenge(Station b1, Station b2) {
        double x;
        double y;

        if (b1.getPosition().x < b2.getPosition().x)
            x = b2.getPosition().x - b1.getPosition().x;
        else
            x = b1.getPosition().x - b2.getPosition().x;

        if (b1.getPosition().y < b2.getPosition().y)
            y = b2.getPosition().y - b1.getPosition().y;
        else
            y = b1.getPosition().y - b2.getPosition().y;

        return Math.round(Math.sqrt((x * x) + (y * y)));
    }

    private void zeichneZuege() {
        for (Train z : signalBox.getTrains()) {
            if (z.getSchedule() != null && z.getSchedule().size() > 1) {
                Station b1 = z.getFahrplan(0).getBahnsteig().getStation();
                Station b2 = z.getFahrplan(1).getBahnsteig().getStation();

                long abfahrtsZeit = (z.getFahrplan(0).getDepartureTime()
                        + z.getVerspaetungInMinuten() * 1000 * 60 - signalBox
                        .getPlayingTime()) / 1000;
                long ankunftsZeit = (z.getFahrplan(1).getArrivalTime()
                        + z.getVerspaetungInMinuten() * 1000 * 60 - signalBox
                        .getPlayingTime()) / 1000;

                if (abfahrtsZeit < 0 && ankunftsZeit > 0) {
                    double aufenthalt =
                            (double) ((z.getFahrplan(1).getArrivalTime() / 1000) - (
                                    z.getFahrplan(0).getDepartureTime() / 1000));
                    double wert = 1 - ((double) ankunftsZeit / aufenthalt);

                    Vec2d linienPosB1 = b1.getLinienPos(b2);
                    Vec2d linienPosB2 = b2.getLinienPos(b1);

                    double x = linienPosB1.x + wert * (linienPosB2.x
                            - linienPosB1.x);
                    double y = linienPosB1.y + wert * (linienPosB2.y
                            - linienPosB1.y);

                    double ak = linienPosB2.x - linienPosB1.x;
                    double gk = linienPosB2.y - linienPosB1.y;

                    double winkel = Math.toDegrees(Math.atan(gk / ak));

                    Label l = z.getStellwerksUebersichtLabel();
                    l.setTranslateX(x);
                    l.setTranslateY(y);
                    l.setRotate(winkel);
                    Platform.runLater(() -> {
                        l.setText(z.getTrainName() + z.getVerspaetungToString());
                    });

                    if (!content.getChildren().contains(l)) {
                        Platform.runLater(() -> {
                            synchronized (content) {
                                if (!content.getChildren().contains(l)) {
                                    content.getChildren().add(l);
                                }
                            }
                        });
                    }
                } else if (content.getChildren()
                        .contains(z.getStellwerksUebersichtLabel())) {
                    Platform.runLater(() -> {
                        content.getChildren()
                                .remove(z.getStellwerksUebersichtLabel());
                    });
                }
            }
        }
    }

    private void zeigeBahnhofsInformationen(Station b) {
        ArrayList<TrainStop> abfahrten = new ArrayList<>();
        for (Train z : signalBox.getTrains()) {
            if (z.getSchedule()
                    != null) { //z != null braucht nicht überprüft werden, da die durch die foreach bereits ausgeschlossen werden
                for (TrainStop fh : z.getSchedule()) {
                    if (fh.getBahnsteig().getStation().getId() == b.getId() &&
                            fh.getDepartureTime()
                                    + fh.getZug().getVerspaetungInMinuten()
                                    * 1000 * 60 > signalBox.getPlayingTime()) {
                        abfahrten.add(fh);
                    }
                }
            }
        }

        abfahrten.sort(Comparator.comparing(TrainStop::getDepartureTime));

        if (abfahrten.size() > 0) {
            Label l = new Label("Nächste Abfahrten");
            l.setStyle("-fx-text-fill: #fff;");
            l.setFont(Font.font(Settings.fontSize));
            l.setTranslateX(b.getPosition().x);
            l.setTranslateY(b.getPosition().y + 30);
            content.getChildren().add(l);
            gezeichneteBahnhofsInformationen.add(l);

            for (int i = 0; i < abfahrten.size() && i < 5; i++) {
                Date dNow = new Date(abfahrten.get(i).getDepartureTime()
                        + abfahrten.get(i).getZug().getVerspaetungInMinuten()
                        * 1000 * 60);
                SimpleDateFormat ft = new SimpleDateFormat("HH:mm");

                l = new Label(ft.format(dNow) + " " + abfahrten.get(i).getZug()
                        .getTrainName() + abfahrten.get(i).getZug()
                        .getVerspaetungToString());
                l.setStyle("-fx-text-fill: #fff;");
                l.setFont(Font.font(Settings.fontSize));
                l.setTranslateX(b.getPosition().x);
                l.setTranslateY(b.getPosition().y + 50 + i * 20);
                content.getChildren().add(l);
                gezeichneteBahnhofsInformationen.add(l);
            }
        }
    }

    private void entferneBahnhofsInformationen() {
        Platform.runLater(() -> {
            content.getChildren().removeAll(gezeichneteBahnhofsInformationen);
            gezeichneteBahnhofsInformationen.clear();
        });
    }

    private void verschiebeBahnhofsInformationen(Station b) {
        if (gezeichneteBahnhofsInformationen != null
                && gezeichneteBahnhofsInformationen.size() > 0) {
            gezeichneteBahnhofsInformationen.get(0).setTranslateX(b.getPosition().x);
            gezeichneteBahnhofsInformationen.get(0)
                    .setTranslateY(b.getPosition().y + 30);

            int counter = -1;
            for (Label l : gezeichneteBahnhofsInformationen) {
                l.setTranslateX(b.getPosition().x);
                l.setTranslateY(b.getPosition().y + 50 + counter * 20);
                counter++;
            }
        }
    }

    public Pane getContent() {
        return content;
    }

    public void aktualisiereBahnhofsNamen() {
        for (Label l : bahnhofsLabel) {
            Station b = bahnhofLabelMap.get(l);
            String text = "";

            if (b.getNameByUser().equals("")) {
                if (b.getName().equals(""))
                    text = signalBox.getSignalBoxName();
                else
                    text = b.getName();
            } else {
                if (b.getName().equals(""))
                    text = b.getNameByUser() + " (" + signalBox
                            .getSignalBoxName() + ")";
                else
                    text = b.getNameByUser() + " (" + b.getName() + ")";
            }

            final String tempText = text;
            Platform.runLater(() -> {
                l.setText(tempText);
            });
        }
    }
}
