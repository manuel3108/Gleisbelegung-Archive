package com.gleisbelegung;

import com.gleisbelegung.lib.Stellwerk;
import com.gleisbelegung.lib.data.Bahnhof;
import com.gleisbelegung.lib.data.FahrplanHalt;
import com.gleisbelegung.lib.data.Zug;
import com.sun.javafx.geom.Vec2d;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import javafx.scene.shape.Line;
import javafx.scene.transform.Rotate;

public class Stellwerksuebersicht {
    private Stellwerk stellwerk;
    private Pane content;
    private Bahnhof bewegeBahnhof;
    private boolean warUeberBahnhof;
    private boolean istUeberBahnhof;
    private Vec2d letzteMausPos;
    private ArrayList<Label> bahnhofsLabel;
    private ArrayList<Label> gezeichneteBahnhofsInformationen;

    public Stellwerksuebersicht(Stellwerk stellwerk){
        this.stellwerk = stellwerk;
        letzteMausPos = new Vec2d();
        bahnhofsLabel = new ArrayList<>();
        gezeichneteBahnhofsInformationen = new ArrayList<>();

        /*gc.setFill(Paint.valueOf("#fff"));
        gc.setStroke(Paint.valueOf("#4e8abf"));//blau
        gc.setFont(Font.font(Einstellungen.schriftgroesse));*/

        for(Bahnhof b : stellwerk.getBahnhoefe()){
            if(b.getBahnhofVerbindungen() != null) b.getBahnhofVerbindungen().clear();

            String text = "";
            if(b.getName().equals("")) text = stellwerk.getStellwerksname();
            else text = b.getName();

            Text temp = new Text(text);
            temp.setFont(Font.font(Einstellungen.schriftgroesse));
            temp.applyCss();

            Label l = new Label(text);
            l.setTranslateX(b.getPos().x);
            l.setTranslateY(b.getPos().y);
            l.setMinWidth(200);
            l.setPrefWidth(200);
            l.setMaxWidth(200);
            l.setMinHeight(Math.round(temp.getBoundsInLocal().getHeight()) + 10);
            l.setPrefHeight(Math.round(temp.getBoundsInLocal().getHeight()) + 10);
            l.setMaxHeight(Math.round(temp.getBoundsInLocal().getHeight()) + 10);
            l.setAlignment(Pos.CENTER);
            l.setStyle("-fx-text-fill: #fff; -fx-border-width: 2; -fx-border-color: #4e8abf");
            l.setFont(Font.font(Einstellungen.schriftgroesse));
            /*l.setOnMouseDragged(mouse -> { //funktioniert nicht, gibt nur ruckelige werte aus, bzw. teilweise fehlerhaft
                b.getPos().x = mouse.getX();
                b.getPos().y = mouse.getY();
                System.out.println(mouse.getX() + " " + mouse.getY());
                l.setTranslateX(b.getPos().x);
                l.setTranslateY(b.getPos().y);
            });*/
            l.setOnMouseEntered(mouse -> zeigeBahnhofsInformationen(b));
            l.setOnMouseExited(mouse -> entferneBahnhofsInformationen());
            bahnhofsLabel.add(l);

            Platform.runLater(() -> {
                content.getChildren().add(l);
            });
        }

        content = new Pane();
        content.setStyle("-fx-background-color: #303030");
        content.setOnDragDetected(mouse -> {
            Bahnhof b = getBahnhof(mouse.getX(), mouse.getY());

            if(b != null){
                bewegeBahnhof = b;
            } else {
                bewegeBahnhof = null;
            }
        });
        content.setOnMouseReleased(mouse -> {
            if(bewegeBahnhof != null) bewegeBahnhof = null;
        });
        content.setOnMouseDragged(mouse -> {
            if(bewegeBahnhof != null){                
                bewegeBahnhof.getPos().x = mouse.getX() - 100;
                bewegeBahnhof.getPos().y = mouse.getY() - 15;
                bahnhofsLabel.get(bewegeBahnhof.getId()).setTranslateX(bewegeBahnhof.getPos().x);
                bahnhofsLabel.get(bewegeBahnhof.getId()).setTranslateY(bewegeBahnhof.getPos().y);
                updateLinie(bewegeBahnhof);
                zeichneZuege();
                verschiebeBahnhofsInformationen(bewegeBahnhof);
            }
        });

        erstelleVerbindungsLinien();
    }
    
    private Bahnhof getBahnhof(double x, double y){
        for(Bahnhof b : stellwerk.getBahnhoefe()){
            String text;

            if(b.getName().equals("")) text = stellwerk.getStellwerksname();
            else text = b.getName();

            Text temp = new Text(text);
            temp.setFont(Font.font(Einstellungen.schriftgroesse));
            temp.applyCss();

            if(x > b.getPos().x && x < b.getPos().x + 200 && y > b.getPos().y && y < b.getPos().y + temp.getBoundsInLocal().getHeight() + 10){
                return b;
            }
        }
        return null;
    }
    
    public void updateUi(double stageWidth, double stageHight){
        /*canvas.setWidth(stageWidth - Einstellungen.informationenBreite - 20);
        canvas.setHeight(stageHight - 80);*/

        update();
    }

    public void update(){

        zeichneZuege();
        /*

        zeichneZuege();

        Bahnhof b = getBahnhof(letzteMausPos.x, letzteMausPos.y);
        if(b != null) zeigeBahnhofsInformationen(b);*/
    }

    private void erstelleVerbindungsLinien(){
        neueLinie(stellwerk.getBahnhoefe().get(0), stellwerk.getBahnhoefe().get(1));
        neueLinie(stellwerk.getBahnhoefe().get(2), stellwerk.getBahnhoefe().get(1));
        neueLinie(stellwerk.getBahnhoefe().get(0), stellwerk.getBahnhoefe().get(2));
    }

    private void neueLinie(Bahnhof b1, Bahnhof b2){
        Vec2d linienPosB1 = new Vec2d();
        Vec2d linienPosB2 = new Vec2d();

        Line linie = new Line(linienPosB1.x, linienPosB1.y, linienPosB2.x, linienPosB2.y);
        linie.setStroke(Paint.valueOf("#4e8abf"));
        linie.setStrokeWidth(2);

        Platform.runLater(() -> content.getChildren().add(linie));

        double verbindungsLaenge = berechneVerbindungsLaenge(b1, b2);
        b1.addBahnhofVerbindung(b2, verbindungsLaenge, linie, linienPosB1);
        b2.addBahnhofVerbindung(b1, verbindungsLaenge, linie, linienPosB2);

        updateLinie(b1);
        updateLinie(b2);
    }

    private void updateLinie(Bahnhof bewegeBahnhof){
        ArrayList<Bahnhof> verbindungsBahnhoefe = bewegeBahnhof.getVerbindungsBahnhoefe();
        for(Bahnhof vb : verbindungsBahnhoefe){
            Line linie = bewegeBahnhof.getLinie(vb);
            Vec2d linienPosB1 = new Vec2d();
            Vec2d linienPosB2 = new Vec2d();

            if(bewegeBahnhof.getPos().x < vb.getPos().x && bewegeBahnhof.getPos().x + 200 < vb.getPos().x){
                linienPosB1.x = bewegeBahnhof.getPos().x + 200;
                linienPosB1.y = bewegeBahnhof.getPos().y + 15;
                linienPosB2.x = vb.getPos().x;
                linienPosB2.y = vb.getPos().y + 15;
            } else if(bewegeBahnhof.getPos().x > vb.getPos().x && bewegeBahnhof.getPos().x > vb.getPos().x + 200){
                linienPosB1.x = bewegeBahnhof.getPos().x;
                linienPosB1.y = bewegeBahnhof.getPos().y + 15;
                linienPosB2.x = vb.getPos().x + 200;
                linienPosB2.y = vb.getPos().y + 15;
            } else if(bewegeBahnhof.getPos().y < vb.getPos().y){
                linienPosB1.x = bewegeBahnhof.getPos().x + 100;
                linienPosB1.y = bewegeBahnhof.getPos().y + 35;
                linienPosB2.x = vb.getPos().x + 100;
                linienPosB2.y = vb.getPos().y;
            } else if(bewegeBahnhof.getPos().y > vb.getPos().y){
                linienPosB1.x = bewegeBahnhof.getPos().x + 100;
                linienPosB1.y = bewegeBahnhof.getPos().y;
                linienPosB2.x = vb.getPos().x + 100;
                linienPosB2.y = vb.getPos().y + 35;
            }

            linie.setStartX(linienPosB1.x);
            linie.setStartY(linienPosB1.y);
            linie.setEndX(linienPosB2.x);
            linie.setEndY(linienPosB2.y);

            bewegeBahnhof.setLinienPos(vb, linienPosB1);
            vb.setLinienPos(bewegeBahnhof, linienPosB2);
        }
    }

    private double berechneVerbindungsLaenge(Bahnhof b1, Bahnhof b2){
        double x;
        double y;

        if(b1.getPos().x < b2.getPos().x) x = b2.getPos().x - b1.getPos().x;
        else x = b1.getPos().x - b2.getPos().x;

        if(b1.getPos().y < b2.getPos().y) y = b2.getPos().y - b1.getPos().y;
        else y = b1.getPos().y - b2.getPos().y;

        return Math.round(Math.sqrt((x*x) + (y*y)));
    }

    private void zeichneZuege(){
        for(Zug z : stellwerk.getZuege()){
            if(z.getFahrplan().size() > 1){
                Bahnhof b1 = z.getFahrplan(0).getBahnsteig().getBahnhof();
                Bahnhof b2 = z.getFahrplan(1).getBahnsteig().getBahnhof();

                long abfahrtsZeit = (z.getFahrplan(0).getAbfahrt() - stellwerk.getSpielzeit())/1000;
                long ankunftsZeit = (z.getFahrplan(1).getAnkuft() - stellwerk.getSpielzeit())/1000;

                if(abfahrtsZeit < 0 && ankunftsZeit > 0){
                    double aufenthalt = (double) ((z.getFahrplan(1).getAnkuft()/1000) - (z.getFahrplan(0).getAbfahrt()/1000));
                    double wert = 1 - ((double) ankunftsZeit / aufenthalt);

                    Vec2d linienPosB1 = b1.getLinienPos(b2);
                    Vec2d linienPosB2 = b2.getLinienPos(b1);

                    double x = linienPosB1.x + wert * (linienPosB2.x - linienPosB1.x);
                    double y = linienPosB1.y + wert * (linienPosB2.y - linienPosB1.y);

                    double ak = linienPosB2.x - linienPosB1.x;
                    double gk = linienPosB2.y - linienPosB1.y;

                    double winkel = Math.toDegrees(Math.atan(gk/ak));

                    Label l = z.getStellwerksUebersichtLabel();
                    l.setTranslateX(x);
                    l.setTranslateY(y);
                    l.setRotate(winkel);



                    if(!content.getChildren().contains(l)){
                        Platform.runLater(() -> {
                            content.getChildren().add(l);
                        });
                    }
                } else if(content.getChildren().contains(z.getStellwerksUebersichtLabel())){
                    Platform.runLater(() -> {
                        content.getChildren().remove(z.getStellwerksUebersichtLabel());
                    });
                }
            }
        }
    }

    private void zeigeBahnhofsInformationen(Bahnhof b){
        ArrayList<FahrplanHalt> abfahrten = new ArrayList<>();
        for(Zug z : stellwerk.getZuege()){
            for(FahrplanHalt fh : z.getFahrplan()){
                if(fh.getBahnsteig().getId() == b.getId()){
                    abfahrten.add(fh);
                }
            }
        }

        abfahrten.sort(Comparator.comparing(FahrplanHalt::getAbfahrt));

        Label l = new Label("Nächste Abfahrten");
        l.setStyle("-fx-text-fill: #fff;");
        l.setFont(Font.font(Einstellungen.schriftgroesse));
        l.setTranslateX(b.getPos().x);
        l.setTranslateY(b.getPos().y + 30);
        content.getChildren().add(l);
        gezeichneteBahnhofsInformationen.add(l);

        for(int i = 0; i < abfahrten.size() && i < 5; i++){
            Date dNow = new Date(abfahrten.get(i).getAbfahrt());
            SimpleDateFormat ft = new SimpleDateFormat("HH:mm");

            l = new Label(ft.format(dNow) + " " + abfahrten.get(i).getZ().getZugName() + abfahrten.get(i).getZ().getVerspaetungToString());
            l.setStyle("-fx-text-fill: #fff;");
            l.setFont(Font.font(Einstellungen.schriftgroesse));
            l.setTranslateX(b.getPos().x);
            l.setTranslateY(b.getPos().y + 50 + i * 20);
            content.getChildren().add(l);
            gezeichneteBahnhofsInformationen.add(l);
        }
    }

    private void entferneBahnhofsInformationen(){
        Platform.runLater(() -> {
            content.getChildren().removeAll(gezeichneteBahnhofsInformationen);
            gezeichneteBahnhofsInformationen.clear();
        });
    }

    private void verschiebeBahnhofsInformationen(Bahnhof b){
        if(gezeichneteBahnhofsInformationen != null && gezeichneteBahnhofsInformationen.size() > 0){
            gezeichneteBahnhofsInformationen.get(0).setTranslateX(b.getPos().x);
            gezeichneteBahnhofsInformationen.get(0).setTranslateY(b.getPos().y + 30);

            int counter = -1;
            for(Label l : gezeichneteBahnhofsInformationen){
                l.setTranslateX(b.getPos().x);
                l.setTranslateY(b.getPos().y + 50 + counter*20);
                counter++;
            }
        }
    }

    public Pane getContent() {
        return content;
    }
}