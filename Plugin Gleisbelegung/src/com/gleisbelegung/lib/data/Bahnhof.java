package com.gleisbelegung.lib.data;

import com.gleisbelegung.Einstellungen;
import com.gleisbelegung.Fenster;
import com.gleisbelegung.LabelContainer;
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
    private ArrayList<Bahnsteig> bahnsteige;
    private ArrayList<BahnhofTeil> bahnhofTeile;
    private boolean sichtbar;

    public Bahnhof(int id, String name){
        this.id = id;
        this.name = name;
        this.bahnsteige = new ArrayList<>();
        this.bahnhofTeile = new ArrayList<>();
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
    private void einstellungen(BahnhofTeil bt){
        Stage stage = new Stage();

        Label l = new Label("Reihenfolge festlegen:");
        l.setStyle("-fx-text-fill: white;");
        l.setFont(Font.font(Einstellungen.schriftgroesse));
        l.setTranslateY(25);
        l.setTranslateX(25);

        TextField tf = new TextField(String.valueOf(bahnsteige.get(0).getOrderId()+1));
        tf.setFont(Font.font(Einstellungen.schriftgroesse-3));
        tf.setTranslateX(25);
        tf.setTranslateY(60);

        Button b = new Button("Speichern");
        b.setFont(Font.font(Einstellungen.schriftgroesse));
        b.setTranslateX(25);
        b.setTranslateY(120);
        b.setOnAction(e -> {
            int order = Integer.parseInt(tf.getText())-1;
            for(Bahnsteig ba : bt.bahnsteige){
                ba.setOrderId(order);
            }

            stage.close();
            Einstellungen.fenster.sortiereGleise();
        });

        Pane p = new Pane(l,tf,b);
        p.setStyle("-fx-background-color: #303030;");
        p.setMinSize(500,200);
        p.setMaxSize(500, 200);

        Scene scene = new Scene(p, 300,200);

        stage.setScene(scene);
        stage.show();
        stage.setAlwaysOnTop(true);
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
}

class BahnhofTeil{
    LabelContainer bahnhofsLabel;
    ArrayList<Bahnsteig> bahnsteige;

    public BahnhofTeil(LabelContainer bahnhofsLabel, ArrayList<Bahnsteig> bahnsteige){
        this.bahnhofsLabel = bahnhofsLabel;
        this.bahnsteige = bahnsteige;
    }
}