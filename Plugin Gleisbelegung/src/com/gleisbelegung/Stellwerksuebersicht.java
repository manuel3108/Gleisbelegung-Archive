package com.gleisbelegung;

import com.gleisbelegung.lib.Stellwerk;
import com.gleisbelegung.lib.data.Bahnhof;
import com.gleisbelegung.lib.data.FahrplanHalt;
import com.gleisbelegung.lib.data.Zug;
import com.sun.javafx.geom.Vec2d;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

public class Stellwerksuebersicht {
    private Stellwerk stellwerk;
    private Pane content;
    private GraphicsContext gc;
    private Canvas canvas;
    private Bahnhof bewegeBahnhof;
    private boolean warUeberBahnhof;
    private boolean istUeberBahnhof;
    private Vec2d letzteMausPos;

    public Stellwerksuebersicht(Stellwerk stellwerk){
        this.stellwerk = stellwerk;
        letzteMausPos = new Vec2d();

        canvas = new Canvas(500, 500);
        canvas.setOnDragDetected(mouse -> {
            Bahnhof b = getBahnhof(mouse.getX(), mouse.getY());

            if(b != null){
               bewegeBahnhof = b;
            } else {
                bewegeBahnhof = null;
            }
        });
        canvas.setOnMouseReleased(mouse -> {
            if(bewegeBahnhof != null) bewegeBahnhof = null;
        });
        canvas.setOnMouseDragged(mouse -> {
            if(bewegeBahnhof != null){
                bewegeBahnhof.getPos().x = mouse.getX() - 100;
                bewegeBahnhof.getPos().y = mouse.getY() + 10;
                update();
            }
        });
        canvas.setOnMouseMoved(mouse -> {
            Bahnhof b = getBahnhof(mouse.getX(), mouse.getY());
            
            if(b != null && bewegeBahnhof == null && !istUeberBahnhof){
                zeigeBahnhofsInformationen(b);
                warUeberBahnhof = true;
                istUeberBahnhof = true;
            } else if(b != null && bewegeBahnhof == null && warUeberBahnhof){
                warUeberBahnhof = false;
                update();
            } else if(bewegeBahnhof == null){
                istUeberBahnhof = false;
                update();
            }

            letzteMausPos.x = mouse.getX();
            letzteMausPos.y = mouse.getY();
        });
        gc = canvas.getGraphicsContext2D();

        content = new Pane(canvas);
    }

    private Bahnhof getBahnhof(double x, double y){
        for(Bahnhof b : stellwerk.getBahnhoefe()){
            String text;

            if(b.getName().equals("")) text = stellwerk.getStellwerksname();
            else text = b.getName();

            Text temp = new Text(text);
            temp.setFont(Font.font(Einstellungen.schriftgroesse));
            temp.applyCss();

            if(x > b.getPos().x && x < b.getPos().x + 200 && y > b.getPos().y - 20 && y < b.getPos().y - 20 + temp.getBoundsInLocal().getHeight()){
                return b;
            }
        }
        return null;
    }

    private void zeigeBahnhofsInformationen(Bahnhof b){
        int counter = 0;

        ArrayList<FahrplanHalt> abfahrten = new ArrayList<>();
        for(Zug z : stellwerk.getZuege()){
            for(FahrplanHalt fh : z.getFahrplan()){
                if(fh.getBahnsteig().getId() == b.getId()){
                    abfahrten.add(fh);
                }
            }
        }

        abfahrten.sort(Comparator.comparing(FahrplanHalt::getAbfahrt));

        gc.fillText("Nächste Abfahrten", b.getPos().x, b.getPos().y + 30);
        for(int i = 0; i < abfahrten.size() && i < 5; i++){
            Date dNow = new Date(abfahrten.get(i).getAbfahrt());
            SimpleDateFormat ft = new SimpleDateFormat("HH:mm");
            gc.fillText(ft.format(dNow) + " " + abfahrten.get(i).getZ().getZugName() + abfahrten.get(i).getZ().getVerspaetungToString(), b.getPos().x, b.getPos().y + 50 + i*20);
        }
    }
    
    public void updateUi(double stageWidth, double stageHight){
        canvas.setWidth(stageWidth - Einstellungen.informationenBreite - 20);
        canvas.setHeight(stageHight - 80);

        update();
    }

    public void update(){
        gc.setFill(Paint.valueOf("#303030"));//gruen
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setFill(Paint.valueOf("#fff"));
        gc.setStroke(Paint.valueOf("#4e8abf"));//blau
        gc.setFont(Font.font(Einstellungen.schriftgroesse));

        for(Bahnhof b : stellwerk.getBahnhoefe()){
            if(b.getBahnhofVerbindungen() != null) b.getBahnhofVerbindungen().clear();

            String text = "";
            if(b.getName().equals("")) text = stellwerk.getStellwerksname();
            else text = b.getName();

            Text temp = new Text(text);
            temp.setFont(Font.font(Einstellungen.schriftgroesse));
            temp.applyCss();

            gc.fillText(text, b.getPos().x + 100 - (Math.round(temp.getBoundsInLocal().getWidth())/2), b.getPos().y);
            gc.strokeRect(b.getPos().x, b.getPos().y - 20, 200, Math.round(temp.getBoundsInLocal().getHeight()) + 5);
        }

        erstelleVerbindungsLinien();

        zeichneZuege();

        Bahnhof b = getBahnhof(letzteMausPos.x, letzteMausPos.y);
        if(b != null) zeigeBahnhofsInformationen(b);
    }

    private void erstelleVerbindungsLinien(){
        gc.setFill(Paint.valueOf("#fff"));


        neueLinie(stellwerk.getBahnhoefe().get(0), stellwerk.getBahnhoefe().get(1));
        neueLinie(stellwerk.getBahnhoefe().get(2), stellwerk.getBahnhoefe().get(1));
        neueLinie(stellwerk.getBahnhoefe().get(0), stellwerk.getBahnhoefe().get(2));
    }

    private void neueLinie(Bahnhof b1, Bahnhof b2){
        Vec2d linienPosB1 = new Vec2d();
        Vec2d linienPosB2 = new Vec2d();

        if(b1.getPos().x < b2.getPos().x && b1.getPos().x + 200 < b2.getPos().x){
            linienPosB1.x = b1.getPos().x + 200;
            linienPosB1.y = b1.getPos().y - 5;
            linienPosB2.x = b2.getPos().x;
            linienPosB2.y = b2.getPos().y - 5;
        } else if(b1.getPos().x > b2.getPos().x && b1.getPos().x > b2.getPos().x + 200){
            linienPosB1.x = b1.getPos().x;
            linienPosB1.y = b1.getPos().y - 5;
            linienPosB2.x = b2.getPos().x + 200;
            linienPosB2.y = b2.getPos().y - 5;
        } else if(b1.getPos().y < b2.getPos().y){
            linienPosB1.x = b1.getPos().x + 100;
            linienPosB1.y = b1.getPos().y + 10;
            linienPosB2.x = b2.getPos().x + 100;
            linienPosB2.y = b2.getPos().y - 20;
        } else if(b1.getPos().y > b2.getPos().y){
            linienPosB1.x = b1.getPos().x + 100;
            linienPosB1.y = b1.getPos().y - 20;
            linienPosB2.x = b2.getPos().x + 100;
            linienPosB2.y = b2.getPos().y + 10;
        }

        gc.strokeLine(linienPosB1.x, linienPosB1.y, linienPosB2.x, linienPosB2.y);

        double verbindungsLaenge = berechneVerbindungsLaenge(b1, b2);
        b1.addBahnhofVerbindung(b2, verbindungsLaenge, linienPosB1);
        b2.addBahnhofVerbindung(b1, verbindungsLaenge, linienPosB2);
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

                    gc.save();
                    gc.translate(x, y);
                    gc.rotate(winkel);
                    gc.translate(-x, -y);
                    gc.setFill(Paint.valueOf("#fff"));
                    gc.fillText(z.getZugName(), x, y);
                    gc.restore();
                }
            }
        }
    }

    public Pane getContent() {
        return content;
    }
}
