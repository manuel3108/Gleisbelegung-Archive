package com.gleisbelegung;

import com.gleisbelegung.lib.Stellwerk;
import com.gleisbelegung.lib.data.Bahnhof;
import com.gleisbelegung.lib.data.Zug;
import com.sun.javafx.geom.Vec2d;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Stellwerksuebersicht {
    private Stellwerk stellwerk;
    private Pane content;
    private GraphicsContext gc;
    private Canvas canvas;
    private Bahnhof bewegeBahnhof;
    private Pane informationen;

    public Stellwerksuebersicht(Stellwerk stellwerk, Pane informationen){
        this.stellwerk = stellwerk;
        this.informationen = informationen;

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
        canvas.setOnMouseClicked(mouse -> {
            Bahnhof b = getBahnhof(mouse.getX(), mouse.getY());
            
            if(b != null){
                System.out.println(b.getName() + " wurde geklickt!");
            }
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
    }

    private void erstelleVerbindungsLinien(){
        gc.setFill(Paint.valueOf("#fff"));


        neueLinie(stellwerk.getBahnhoefe().get(0), stellwerk.getBahnhoefe().get(1));
        neueLinie(stellwerk.getBahnhoefe().get(2), stellwerk.getBahnhoefe().get(1));
        neueLinie(stellwerk.getBahnhoefe().get(0), stellwerk.getBahnhoefe().get(2));
    }

    private void neueLinie(Bahnhof b1, Bahnhof b2){
        int verbindungsPositionB1 = 0; //0: oben, 1: rechts, 2: unten, 3: links
        int verbindungsPositionB2 = 0; //0: oben, 1: rechts, 2: unten, 3: links

        if(b1.getPos().x < b2.getPos().x && b1.getPos().x + 200 < b2.getPos().x){
            gc.strokeLine(b1.getPos().x + 200, b1.getPos().y - 5, b2.getPos().x, b2.getPos().y - 5);
            verbindungsPositionB1 = 1;
            verbindungsPositionB2 = 3;
        }
        else if(b1.getPos().x > b2.getPos().x && b1.getPos().x > b2.getPos().x + 200){
            gc.strokeLine(b1.getPos().x, b1.getPos().y - 5, b2.getPos().x + 200, b2.getPos().y - 5);
            verbindungsPositionB1 = 3;
            verbindungsPositionB2 = 1;
        }
        else if(b1.getPos().y < b2.getPos().y){
            gc.strokeLine(b1.getPos().x + 100, b1.getPos().y + 10, b2.getPos().x + 100, b2.getPos().y - 20);
            verbindungsPositionB1 = 2;
            verbindungsPositionB2 = 0;
        }
        else if(b1.getPos().y > b2.getPos().y){
            gc.strokeLine(b1.getPos().x + 100, b1.getPos().y - 20, b2.getPos().x + 100, b2.getPos().y + 10);
            verbindungsPositionB1 = 0;
            verbindungsPositionB2 = 2;
        }

        double verbindungsLaenge = berechneVerbindungsLaenge(b1, b2);
        b1.addBahnhofVerbindung(b2, verbindungsLaenge, verbindungsPositionB1);
        b2.addBahnhofVerbindung(b1, verbindungsLaenge, verbindungsPositionB2);

//        gc.setFill(Paint.valueOf("#fff"));
//        gc.strokeText(verbindungsLaenge + " px ", (b1.getPos().x + b2.getPos().x)/2, (b1.getPos().y + b2.getPos().y)/2);

//        System.out.println(verbindungsLaenge + " " + verbindungsPositionB1 + " " + b1.getName() + " " + verbindungsPositionB2 + " " + b2.getName());
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

                    double x = b1.getPos().x + wert * (b2.getPos().x - b1.getPos().x);
                    double y = b1.getPos().y + wert * (b2.getPos().y - b1.getPos().y);

                    gc.strokeText(z.getZugName(), x, y);
                }
            }
        }
    }

    public Pane getContent() {
        return content;
    }
}
