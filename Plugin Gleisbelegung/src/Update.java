/*
@author: Manuel Serret
@email: manuel-serret@t-online.de
@contact: Email, Github, STS-Forum

Hinweis: In jeder Klasse werden alle Klassenvariablen erklärt, sowie jede Methode. Solltest du neue Variablen oder Methoden hinzufügen, vergiss bitte nicht sie zu implementieren.

Checkt auf eine neue Version
 */

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.Scanner;

public class Update {

    //Checkt auf neue Versionen
    public void checkForNewVersion(int currentVersion){
        try {
            URL url = new URL("http://www.manuel-serret.bplaced.net/Gleisbelegung/version.txt");
            Scanner sc = new Scanner(url.openStream());

            int version = Integer.parseInt(sc.nextLine());
            sc.close();

            if(version <= currentVersion){
                //System.out.println("All Files are up to date!");
            } else{
                //System.out.println("Update needed!");

                Stage stage = new Stage();

                Label l = new Label("Es ist eine neuere Version verfügbar!\nLade es dir herunter, wann es dir passt...");
                l.setFont(Font.font(18));
                l.setStyle("-fx-text-fill: #fff");

                /*Button bYes = new Button("Ja!");
                bYes.setFont(Font.font(18));
                bYes.setTranslateY(60);
                bYes.setTranslateX(30);
                bYes.setOnAction(e -> unpackJAR());

                Button bNo = new Button("Nein!");
                bNo.setFont(Font.font(18));
                bNo.setTranslateY(60);
                bNo.setTranslateX(190);
                bNo.setOnAction(e -> stage.close());*/
                Button bOk = new Button("OK!");
                bOk.setFont(Font.font(18));
                bOk.setTranslateY(60);
                bOk.setTranslateX(130);
                bOk.setOnAction(e -> stage.close());

                Pane p = new Pane(l, bOk);
                p.setPrefHeight(120);
                p.setPrefWidth(300);
                p.setStyle("-fx-background-color: #303030");

                Scene s = new Scene(p);

                stage.setScene(s);
                stage.setTitle("Hinweis:");
                stage.setAlwaysOnTop(true);
                stage.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
