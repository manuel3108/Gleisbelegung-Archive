package com.gleisbelegung.lib.data;

import com.gleisbelegung.Einstellungen;
import com.gleisbelegung.LabelContainer;
import com.sun.javafx.geom.Vec2d;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;

public class Bahnhof {
    private int id;
    private String name;
    private String alternativName = "";
    private ArrayList<Bahnsteig> bahnsteige;
    private ArrayList<BahnhofTeil> bahnhofTeile;
    private ArrayList<BahnhofVerbindung> bahnhofVerbindungen;
    private boolean sichtbar;
    private Vec2d pos;

    public Bahnhof(int id, String name){
        this.id = id;
        this.name = name;
        this.bahnsteige = new ArrayList<>();
        this.bahnhofTeile = new ArrayList<>();
        this.bahnhofVerbindungen = new ArrayList<>();
        pos = new Vec2d(30, id*40 + 40);
    }

    public int getId(){
        return id;
    }
    public String getName() {
        return name;
    }
    public ArrayList<Bahnsteig> getBahnsteige() {
        return bahnsteige;
    }
    public Bahnsteig getBahnsteig(int index){
        return bahnsteige.get(index);
    }
    public int getAnzahlBahnsteige(){
        return bahnsteige.size();
    }

    public boolean isSichtbar() {
        for (Bahnsteig b : bahnsteige) {
            if (!b.isSichtbar())
                return false;

        }
        return true;
    }

    public void setSichtbar(boolean sichtbar) {
        for (Bahnsteig b : bahnsteige)
            b.setSichtbar(sichtbar);
    }

    public ArrayList<BahnhofTeil> getBahnhofTeile() {
        return bahnhofTeile;
    }
    public void addBahnhofLabel(LabelContainer bahnhofLabel, ArrayList<Bahnsteig> bahnsteige) {
        BahnhofTeil bt = new BahnhofTeil(bahnhofLabel, bahnsteige);
        bahnhofTeile.add(bt);

        bahnhofLabel.getLabel().setOnMouseClicked(e -> {
            if(e.getButton() == MouseButton.PRIMARY){
                hervorheben(bt);
                System.out.println(bt.bahnsteige.size());
            } else if(e.getButton() == MouseButton.SECONDARY) {
                einstellungen(bt);
            }
        });
    }

    public String getAlternativName() { return alternativName; }

    private void einstellungen(BahnhofTeil bt){
        Einstellungen.fenster.gleisbelegung.zeigeOrderIds();
        Stage stage = new Stage();

        Label name = new Label("Name:");
        name.setStyle("-fx-text-fill: white;");
        name.setFont(Font.font(Einstellungen.schriftgroesse));
        name.setTranslateY(25);
        name.setTranslateX(25);

        TextField tname = new TextField(alternativName);
        tname.setFont(Font.font(Einstellungen.schriftgroesse-3));
        tname.setTranslateX(25);
        tname.setTranslateY(60);

        Label l = new Label("Reihenfolge festlegen:");
        l.setStyle("-fx-text-fill: white;");
        l.setFont(Font.font(Einstellungen.schriftgroesse));
        l.setTranslateX(25);
        l.setTranslateY(95);

        TextField tf = new TextField(String.valueOf(bahnsteige.get(0).getOrderId()+1));
        tf.setFont(Font.font(Einstellungen.schriftgroesse-3));
        tf.setTranslateX(25);
        tf.setTranslateY(130);

        Button b = new Button("Speichern");
        b.setFont(Font.font(Einstellungen.schriftgroesse));
        b.setTranslateX(25);
        b.setTranslateY(190);
        b.setOnAction(e -> {
            int order = Integer.parseInt(tf.getText())-1;
            for(Bahnsteig ba : bt.bahnsteige){
                ba.setOrderId(order);
                alternativName = tname.getText();
            }

            stage.close();
            Einstellungen.fenster.gleisbelegung.versteckeOrderIds();
            Einstellungen.fenster.gleisbelegung.sortiereGleise();
        });

        Pane p = new Pane(name, tname,l,tf,b);
        p.setStyle("-fx-background-color: #303030;");
        p.setMinSize(500,200);
        p.setMaxSize(500, 200);

        Scene scene = new Scene(p, 300,250);

        stage.setScene(scene);
        stage.show();
        stage.setAlwaysOnTop(true);

        stage.setOnCloseRequest(e -> {
            Einstellungen.fenster.gleisbelegung.versteckeOrderIds();
        });
    }

    private void hervorheben(BahnhofTeil bt){
        for(Bahnsteig b: bt.bahnsteige){
            b.hebeHervor();
        }
    }

    @Override
    public String toString() {
        return "Bahnhof{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", bahnsteige=" + bahnsteige +
                '}';
    }

    public Vec2d getPos() {
        return pos;
    }

    public ArrayList<BahnhofVerbindung> getBahnhofVerbindungen() {
        return bahnhofVerbindungen;
    }
    public void addBahnhofVerbindung(Bahnhof bahnhof, double laenge, Vec2d linienPos){
        bahnhofVerbindungen.add(new BahnhofVerbindung(bahnhof, laenge, linienPos));
    }
    public double getVerbindungsLaenge(Bahnhof bahnhof){
        for(BahnhofVerbindung bv : bahnhofVerbindungen){
            if(bv.bahnhof.getId() == bahnhof.getId()){
                return bv.laenge;
            }
        }
        return -1;
    }
    public Vec2d getLinienPos(Bahnhof bahnhof){
        for(BahnhofVerbindung bv : bahnhofVerbindungen){
            if(bv.bahnhof.getId() == bahnhof.getId()){
                return bv.linienPos;
            }
        }
        return null;
    }
}

class BahnhofTeil{
    LabelContainer bahnhofsLabel;
    ArrayList<Bahnsteig> bahnsteige;

    public BahnhofTeil(LabelContainer bahnhofsLabel, ArrayList<Bahnsteig> bahnsteige){
        this.bahnhofsLabel = bahnhofsLabel;
        this.bahnsteige = bahnsteige;
    }
}

class BahnhofVerbindung{
    Bahnhof bahnhof;
    double laenge;
    Vec2d linienPos;

    public BahnhofVerbindung(Bahnhof bahnhof, double laenge, Vec2d linienPos){
        this.bahnhof = bahnhof;
        this.laenge = laenge;
        this.linienPos = linienPos;
    }
}