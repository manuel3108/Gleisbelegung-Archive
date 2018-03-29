package com.gleisbelegung;

import com.gleisbelegung.lib.Stellwerk;
import com.gleisbelegung.lib.data.Bahnhof;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.awt.*;

public class Stellwerksuebersicht {
    private Stellwerk stellwerk;
    private Pane content;
    private GraphicsContext gc;
    private Canvas canvas;
    private Bahnhof bewegeBahnhof;

    public Stellwerksuebersicht(Stellwerk stellwerk){
        this.stellwerk = stellwerk;

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
            
            if(b != null && bewegeBahnhof == null) System.out.println(b.getName() + " wurde geklickt!");
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
            String text = "";
            if(b.getName().equals("")) text = stellwerk.getStellwerksname();
            else text = b.getName();

            Text temp = new Text(text);
            temp.setFont(Font.font(Einstellungen.schriftgroesse));
            temp.applyCss();

            gc.fillText(text, b.getPos().x + 100 - (Math.round(temp.getBoundsInLocal().getWidth())/2), b.getPos().y);
            gc.strokeRect(b.getPos().x, b.getPos().y - 20, 200, Math.round(temp.getBoundsInLocal().getHeight()) + 5);
        }
    }

    public Pane getContent() {
        return content;
    }
}
