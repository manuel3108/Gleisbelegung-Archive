import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LabelContainer extends Main{
    private Label l;
    private int labelIndex;
    private ArrayList<Zug> trains;
    private long time = -1;
    private int bahnsteig;
    private int borderSolution;
    private ArrayList<LabelContainer> labelTime;

    public LabelContainer(int labelIndex, int bahnsteig, ArrayList<LabelContainer> labelTime){
        this.labelTime = labelTime;
        this.bahnsteig = bahnsteig;
        this.labelIndex = labelIndex;
        trains = new ArrayList<>();

        l = new Label();
        l.setFont(Font.font(settingsFontSize-5));
        l.setMinWidth(settingsGridWidth);
        l.setMaxWidth(settingsGridWidth);
        l.setAlignment(Pos.CENTER);

        /*if(labelIndex % 2 == 0){
            //l.setStyle("-fx-text-fill: #fff; -fx-border-color: #505050; -fx-border-width: 0 1 1 0;");
            l.setStyle("-fx-text-fill: #fff; " + prepareBorder());
        } else{
            //l.setStyle("-fx-background-color: #292929; -fx-text-fill: #fff; -fx-border-color: #505050; -fx-border-width: 0 1 1 0;");
            l.setStyle("-fx-background-color: #292929; -fx-text-fill: #fff; " + prepareBorder());
        }*/

        if(bahnsteig > -1){
            updateLabel();
        }
    }

    public int getLabelIndex() {
        return labelIndex;
    }
    public void setLabelIndex(int labelIndex) {
        this.labelIndex = labelIndex;
    }

    public Label getLabel(){
        return l;
    }
    public long getTime() {
        return time;
    }
    public void setTime(long time){
        this.time = time;
    }

    public void addTrain(Zug z){
        trains.add(z);
        updateLabel();

        l.setOnMouseEntered(e -> showTrainInformations());
    }
    public void removeTrain(Zug z) {
        int size = trains.size();
        for (int i = 0; i < trains.size(); i++) {
            if(trains.get(i).getZugId() == z.getZugId()){
                trains.remove(i);
            }
        }

        while(trains.size() > size - 1){
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        updateLabel();

        l.setOnMouseEntered(e -> showTrainInformations());
    }

    public void updateLabel(){
        Platform.runLater(() -> {
            if(trains.size() == 0){
                l.setText("");
                l.setTooltip(null);
                if(labelIndex % 2 == 0){
                    //l.setStyle("-fx-text-fill: #fff; -fx-border-color: #505050; -fx-border-width: 0 1 1 0;");
                    l.setStyle("-fx-text-fill: #fff; " + prepareBorder());
                } else{
                    //l.setStyle("-fx-background-color: #292929; -fx-text-fill: #fff; -fx-border-color: #505050; -fx-border-width: 0 1 1 0;");
                    l.setStyle("-fx-background-color: #292929; -fx-text-fill: #fff; " + prepareBorder());
                }
            } else if(trains.size() == 1){
                l.setText(trains.get(0).getZugName() + trains.get(0).getVerspaetungToString());
                l.setTooltip(new Tooltip(trains.get(0).getZugName() + trains.get(0).getVerspaetungToString()));
                //l.setStyle("-fx-text-fill: #fff; -fx-border-color: #505050; -fx-border-width: 0 1 1 0; -fx-background-color: #" + prepareTrainStyle(trains.get(0).getZugName()) + ";");
                l.setStyle("-fx-text-fill: #fff; " + prepareBorder() + "-fx-background-color: #" + prepareTrainStyle(trains.get(0).getZugName()) + ";");
            } else{
                String text = "";

                for(Zug z : trains){
                    text += z.getZugName() + trains.get(0).getVerspaetungToString() + ", ";
                }
                text = text.substring(0,text.length() - 2);

                final String temp = text;
                l.setText(temp);
                l.setTooltip(new Tooltip(temp));

                //l.setStyle("-fx-text-fill: #fff; -fx-border-color: #505050; -fx-border-width: 0 1 1 0; -fx-background-color: red;");
                l.setStyle("-fx-text-fill: #fff; " + prepareBorder() + "-fx-background-color: red;");

                if(bahnsteigeSichtbar[bahnsteig]){
                    playColisonSound();
                }
            }
        });
    }
    public void updateLabel(String text){
        Platform.runLater(() -> {
            l.setText(text);

            if(bahnsteig == -1){
                prepareBorderForLabelTime();
            }
        });
    }
    public void updateLabel(String text, long time){
        this.time = time;
        Platform.runLater(() -> l.setText(text));
    }

    private String prepareTrainStyle(String zugName){
        int index = zugName.indexOf('(')-1;
        char[] name = zugName.toCharArray();

        if(index > 0){
            zugName = "";
            for (int i = 0; i < name.length; i++) {
                if(i < index){
                    zugName += name[i];
                }
            }
        }

        String out = zugName.replaceAll("[^\\d.]", "");
        char[] temp = out.toCharArray();

        int counter = 6 - temp.length;
        if (counter > 0 && counter != 3) {
            for (int i = 0; i < counter; i++) {
                out += "9";
            }
        } else if (counter < 0) {
            while (counter < 0) {
                out = "";
                for (int i = 0; i < temp.length - 1; i++) {
                    out += temp[i];
                }
                counter++;
            }
        }

        return out;
    }

    private String prepareBorder(){
        String fullHour = "-fx-border-color: yellow #505050 #05af3b yellow; -fx-border-width: 0 1 1 0; ";
        String fiveMin = "-fx-border-color: yellow #505050 #969696 yellow; -fx-border-width: 0 1 1 0; ";
        String normal = "-fx-border-color: yellow #505050 #505050 yellow; -fx-border-width: 0 1 1 0; ";

        String out = "";

        if(labelIndex >= 0 && labelTime.size() > 0 && labelIndex < labelTime.size()){
            if(labelTime.get(labelIndex).getLabel().getText().endsWith("00")){
                out = fullHour;
            } else if(labelTime.get(labelIndex).getLabel().getText().endsWith("5") || labelTime.get(labelIndex).getLabel().getText().endsWith("0")){
                out = fiveMin;
            } else{
                out = normal;
            }
        } else if(labelIndex > labelTime.size()){
            if(labelTime.get(labelTime.size()-1).getLabel().getText().endsWith("00")){
                out = fullHour;
            } else if(labelTime.get(labelTime.size()-1).getLabel().getText().endsWith("5") || labelTime.get(labelTime.size()-1).getLabel().getText().endsWith("0")){
                out = fiveMin;
            } else{
                out = normal;
            }
        }

        return out;
    }
    private void prepareBorderForLabelTime(){
        String in = l.getText();

        String fullHour = "-fx-text-fill: #fff; -fx-border-color: yellow #505050 #05af3b yellow; -fx-border-width: 0 5 1 0; ";
        String fiveMin = "-fx-text-fill: #fff; -fx-border-color: yellow #505050 #969696 yellow; -fx-border-width: 0 5 1 0; ";
        String normal = "-fx-text-fill: #fff; -fx-border-color: yellow #505050 #505050 yellow; -fx-border-width: 0 5 1 0; ";

        if(in.endsWith("00")){
            l.setStyle(fullHour);
        } else if(in.endsWith("5") || in.endsWith("0")){
            l.setStyle(fiveMin);
        } else{
            l.setStyle(normal);
        }
    }

    public ArrayList<Zug> getTrains(){
        return trains;
    }

    public int getBahnsteig() {
        return bahnsteig;
    }
    public void setBahnsteig(int bahnsteig) {
        this.bahnsteig = bahnsteig;
    }

    public void showTrainInformations(){
        int heightCounter = 0;

        informations.getChildren().clear();

        for(Zug z : trains){
            debugMessage("INFORMATION: Maus befindet sich ueber " + z.getZugName() +  " und zeigt die Informationen: " + settingsShowInformations, true);



            Label trainName = new Label(z.getZugName() + z.getVerspaetungToString());
            trainName.setStyle("-fx-text-fill: white");
            trainName.setFont(Font.font(settingsFontSize-2));
            trainName.setTranslateY(heightCounter);

            Label vonBis = new Label(z.getVon() + " - " + z.getNach());
            vonBis.setStyle("-fx-text-fill: white");
            vonBis.setFont(Font.font(settingsFontSize-5));
            vonBis.setTranslateY(heightCounter + 25);

            informations.getChildren().addAll(trainName, vonBis);

            for(int i = 0; i < z.getFahrplan().length; i++){
                long lAnkunft = z.getFahrplan(i).getAnkuft() + z.getVerspaetung()*1000*60;
                long lAbfahrt = z.getFahrplan(i).getAbfahrt() + z.getVerspaetung()*1000*60;
                if(z.getVerspaetung() > 3 && (lAbfahrt-lAnkunft)/1000/60 > 3){
                    lAbfahrt = lAnkunft + 4*1000*60;
                }


                Date anunft = new Date(lAnkunft);
                Date abfahrt = new Date(lAbfahrt);
                SimpleDateFormat ft = new SimpleDateFormat("HH:mm");

                Label l = new Label("Gleis: " + z.getFahrplan(i).getGleis() + " " + ft.format(anunft) + " - " + ft.format(abfahrt));
                l.setFont(Font.font(settingsFontSize-5));
                l.setTranslateY(heightCounter + 55);
                l.setPrefWidth(215);

                if(z.getGleis().equals(z.getFahrplan(i).getGleis()) && z.getAmGleis()){
                    l.setStyle("-fx-text-fill: white; -fx-background-color: green");
                } else if(z.getFahrplan(i).getGleis().equals(bahnsteige[bahnsteig])){
                    l.setStyle("-fx-text-fill: white; -fx-background-color: #505050");
                } else{
                    l.setStyle("-fx-text-fill: white");
                }

                informations.getChildren().add(l);

                heightCounter += 20;
            }

            heightCounter += 75;
        }

        informations.setPrefHeight(heightCounter);
    }

    public void highlight(){
        Runnable r = () -> {
            try {
                Platform.runLater(() -> l.setStyle("-fx-text-fill: #fff; -fx-border-color: #505050; -fx-border-width: 0 1 1 0; -fx-background-color: green"));
                Thread.sleep(1000);
                updateLabel();
                Thread.sleep(1000);
                Platform.runLater(() -> l.setStyle("-fx-text-fill: #fff; -fx-border-color: #505050; -fx-border-width: 0 1 1 0; -fx-background-color: green"));
                Thread.sleep(1000);
                updateLabel();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        new Thread(r).start();
    }
}