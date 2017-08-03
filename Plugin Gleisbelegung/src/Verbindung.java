import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Verbindung extends Main{
    public Socket socket;
    private XMLHandler xml;

    public Verbindung(Socket socket){
        try {
            this.socket = socket;
            xml = new XMLHandler(socket.getInputStream());
            zuege = new ArrayList<Zug>();
        } catch (IOException e) {
            errorWindow(-1, "Es liegt ein Fehler bei der Anmeldung vor.\n\nPrüfe ob die Plugin-Schnittstelle aktiv ist und ob überhaupt ein Simulator läuft.");
            System.exit(-1);
        }

        ArrayList<String[]> temp = xml.readLine();
        if(temp != null && Integer.parseInt(temp.get(0)[1]) != 300){
            errorWindow(-2, "Es liegt ein Fehler bei der Anmeldung vor.\n\nPrüfe ob die Plugin-Schnittstelle aktiv ist und ob überhaupt ein Simulator läuft.");
            System.exit(-2);
        }

        if(setSocketCode("<register name=\"Gleisbelegung\" autor=\"Manuel Serret\" version=\"0.1\" protokoll=\"1\" text=\"Darstellung der Gleisbelegung\" />") != 1){
            errorWindow(-3, "Es liegt ein Fehler bei der Anmeldung vor.\n\nPrüfe ob die Plugin-Schnittstelle aktiv ist und ob überhaupt ein Simulator läuft.");
            System.exit(-3);
        }

        temp = xml.readLine();
        if(temp != null && Integer.parseInt(temp.get(0)[1]) != 220){
            errorWindow(-4, "Anmeldung erfolgreich!\n\nSollte diese Meldung kommen, habe ich etwas falsch Programmiert");
            System.exit(-4);
        }

        if(setSocketCode("<anlageninfo />") != 1){
            errorWindow(-5, "Beim Senden der Daten an die Plugin-Schitstelle ist ein Fehler aufgetreten.");
            System.exit(-5);
        }

        temp = xml.readLine();
        if(temp == null){
            errorWindow(-6, "Beim Empfangen der Daten von der Plugin-Schitstelle ist ein Fehler aufgetreten.");
            System.exit(-6);
        }
        System.out.println("Die Verbindung mit dem Stellwerk " + temp.get(2)[1] + " und der Anlagen-Id " + temp.get(1)[1] + " wurde erfolgreich hergestellt. Aktuelle Simulator-Build: " + temp.get(0)[1]);
        addMessageToErrorPane("Verbindung erfolgreich!");

        long timeBeforeSending = System.currentTimeMillis();
        if(setSocketCode("<simzeit sender='" + timeBeforeSending + "' />") != 1){
            errorWindow(-7, "Beim Senden der Daten an die Plugin-Schitstelle ist ein Fehler aufgetreten.");
            System.exit(-7);
        }

        temp = xml.readLine();
        if(temp == null){
            errorWindow(-8, "Beim Empfangen der Daten von der Plugin-Schitstelle ist ein Fehler aufgetreten.");
            System.exit(-8);
        }
        currentGameTime = ((System.currentTimeMillis() - timeBeforeSending)/1000)/2 + Long.parseLong(temp.get(1)[1]) - 1000*60*60;

        if(setSocketCode("<bahnsteigliste />") != 1){
            errorWindow(-9, "Beim Senden der Daten an die Plugin-Schitstelle ist ein Fehler aufgetreten.");
            System.exit(-9);
        }

        ArrayList<ArrayList<ArrayList<String[]>>> temp1 = xml.readLines();
        bahnsteige = new String[temp1.size()];
        bahnsteigeSichtbar = new boolean[temp1.size()];
        for(int i = 0; i < temp1.size(); i++){
            bahnsteige[i] = temp1.get(i).get(0).get(0)[1];
            bahnsteigeSichtbar[i] = true;
        }
    }

    private int setSocketCode(String s){
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            out.write(s+"\n");
            out.flush();
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void update(){
        setSocketCode("<zugliste />");
        ArrayList<ArrayList<ArrayList<String[]>>> zugliste = xml.readLines();

        for(int i = 0; i < zugliste.size(); i++){
            try{
                //System.out.println(zugliste.get(i).get(0).get(0)[1] + " " + zugliste.get(i).get(0).get(1)[1]);
                boolean exists = false;
                for(Zug z : zuege){
                    if(!zugliste.get(i).get(0).get(0)[1].equals("") && Integer.parseInt(zugliste.get(i).get(0).get(0)[1]) == z.getZugId()){
                        exists = true;
                        break;
                    }
                }

                if(! exists && !zugliste.get(i).get(0).get(0)[1].equals("") && !zugliste.get(i).get(0).get(1)[1].equals("")){
                    //System.out.println("INFORMATION: " + zugliste.get(i).get(0).get(1)[1] + " wurde hinzugefügt!");
                    zuege.add(new Zug(Integer.parseInt(zugliste.get(i).get(0).get(0)[1]), zugliste.get(i).get(0).get(1)[1]));
                }
            } catch (Exception e){
                errorCounter++;
                System.out.println("FEHLER: Zuglistenaktualisierungsfehler!");
                e.printStackTrace();
                addMessageToErrorPane("FEHLER: Zuglistenaktualisierungsfehler!");
            }
        }

        ArrayList<Zug> removing = new ArrayList<>();



        for(Zug z : zuege){
            try{
                boolean updateNeeded = false;
                setSocketCode("<zugdetails zid='" + z.getZugId() + "'/>");
                ArrayList<String[]> zugdetails = xml.readLine();
                int counter = 0;
                for(String[] s : zugdetails){
                    s[0] = s[0].replace(" ", "");
                    if(s[0].equals("verspaetung")){
                        if(Integer.parseInt(s[1]) != z.getVerspaetung()){
                            z.setVerspaetung(Integer.parseInt(s[1]));
                            updateNeeded = true;
                        }
                        counter = counter + 1;
                    } else if(s[0].equals("gleis")){
                        if(!s[1].equals(z.getGleis())){
                            z.setGleis(s[1]);
                            updateNeeded = true;
                        }
                        counter = counter + 1;
                    } else if(s[0].equals("amgleis")){
                        if(Boolean.parseBoolean(s[1]) != z.getAmGleis()){
                            z.setAmGleis(Boolean.parseBoolean(s[1]));
                            updateNeeded = true;
                        }
                        counter = counter + 1;
                    } else if(s[0].equals("von")){
                        if(! s[1].equals(z.getVon())){
                            z.setVon(s[1]);
                            updateNeeded = true;
                        }
                        counter = counter + 1;
                    } else if(s[0].equals("nach")){
                        if(! s[1].equals(z.getNach())){
                            z.setNach(s[1]);
                            updateNeeded = true;
                        }
                        counter = counter + 1;
                    } else if(s[0].equals("plangleis")){
                        if(! s[1].equals(z.getPlangleis())){
                            z.setPlangleis(s[1]);
                            updateNeeded = true;
                        }
                        counter = counter + 1;
                    } else if(s[0].equals("sichtbar")){
                        if(Boolean.parseBoolean(s[1]) != z.getSichtbar()){
                            z.setSichtbar(Boolean.parseBoolean(s[1]));
                            updateNeeded = true;
                        }
                        counter = counter + 1;
                    }
                }

                if(counter!=7 && counter!=5){
                    System.out.println("INFORMATION: " + z.getZugName() + " es wurden nicht alle Daten gesetzt " + z.getVerspaetung() + " " + z.getGleis() + " " + z.getAmGleis() + " " + z.getVon() + " " + z.getNach() + " " + z.getPlangleis() + " " + z.getSichtbar());
                } else if(counter == 5){
                    removing.add(z);
                }

                setSocketCode("<zugfahrplan zid='" + z.getZugId() + "'/>");
                ArrayList<ArrayList<ArrayList<String[]>>> zugfahrplan = xml.readLines();

                FahrplanHalt[] fahrplan = new FahrplanHalt[zugfahrplan.size()];
                for (int i = 0; i < fahrplan.length; i++) {
                    try{
                        if(zugfahrplan.get(i).get(0).size() >= 4){
                            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

                            try{
                                long time = dateFormat.parse(zugfahrplan.get(i).get(0).get(0)[1]).getTime();
                                zugfahrplan.get(i).get(0).get(0)[1] = String.valueOf(time);
                            } catch(Exception e){
                                zugfahrplan.get(i).get(0).get(0)[1] = String.valueOf(0);
                            }

                            try{
                                long time = dateFormat.parse(zugfahrplan.get(i).get(0).get(4)[1]).getTime();
                                zugfahrplan.get(i).get(0).get(4)[1] = String.valueOf(time);
                            } catch (Exception e){
                                zugfahrplan.get(i).get(0).get(4)[1] = String.valueOf(0);
                            }

                            fahrplan[i] = new FahrplanHalt(zugfahrplan.get(i).get(0), z);
                        } else{
                            errorCounter++;
                            System.out.println("ARRAY out of Bounds: Zug: " + z.getZugName());
                            if(errorCounter >= maxErrorCounter){
                                break;
                            }
                        }
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }

                if(! updateNeeded){
                    if(z.getFahrplan().length != fahrplan.length){
                        updateNeeded = true;
                    } else if(z.getFahrplan() != null){
                        for (int i = 0; i < z.getFahrplan().length; i++) {
                            if(z.getFahrplan(i).getAnkuft() != fahrplan[i].getAnkuft()){
                                updateNeeded = true;
                            } else if(z.getFahrplan(i).getAbfahrt() != fahrplan[i].getAbfahrt()){
                                updateNeeded = true;
                            } else if(! z.getFahrplan(i).getGleis().equals(fahrplan[i].getGleis())){
                                updateNeeded = true;
                            } else if(! z.getFahrplan(i).getPlangleis().equals(fahrplan[i].getPlangleis())){
                                updateNeeded = true;
                            } else if(! z.getFahrplan(i).getFlags().equals(fahrplan[i].getFlags())){
                                updateNeeded = true;
                            }
                        }
                    }
                }

                if(updateNeeded){
                    if(! z.isNewTrain()){
                        z.removeFromGrid();
                    }
                    z.setFahrplan(fahrplan);
                    z.setNeedUpdate(true);
                }
            } catch(Exception e){
                errorCounter++;
                System.out.println("ZUG: " + z.getZugName() + ": Verbindungsfehler!");
                e.printStackTrace();
                addMessageToErrorPane("ZUG: " + z.getZugName() + ": Verbindungsfehler!");
            }
        }

        for(Zug z : removing){
            //System.out.println("INFORMATION: " + z.getZugName() + " wurde entfernt.");
            zuege.remove(z);
        }

        for (Zug z : zuege) {
            try{
                if(z.getFahrplan() != null){
                    for(FahrplanHalt fh : z.getFahrplan()){
                        try{
                            if(fh.getFlags() != null && !fh.getFlags().equals("")){
                                Zug flagged = getFlaggedTrain(fh.getFlags());
                                if(flagged != null){
                                    flagged.getFahrplan(0).setDrawable(false);
                                    fh.setFlaggedTrain(flagged);
                                    debugMessage("ZUG: " + z.getZugName() + " (" + z.getZugId() + ") Flag: " + flagged.getZugName() + " (" + flagged.getZugId() + ")", true);
                                } else{
                                    fh.setFlaggedTrain(null);
                                    debugMessage("ZUG: " + z.getZugName() + " (" + z.getZugId() + ") Flag ohne Zug", true);
                                }
                            }
                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }catch(Exception e){
                errorCounter++;
                System.out.println("ZUG: " + z.getZugName() + ": Flag-Problem!");
                e.printStackTrace();
                addMessageToErrorPane("ZUG: " + z.getZugName() + ": Flag-Problem!");
            }
        }
    }

    private Zug getFlaggedTrain(String content) {
        if(content.contains("(") && content.contains(")")){
            char[] in = content.toCharArray();
            String out = "";
            boolean inKlammer = false;

            for (int i = 0; i < in.length; i++) {
                if (in[i] == '(') {
                    inKlammer = true;
                    i++;
                }
                if (in[i] == ')') {
                    inKlammer = false;
                }

                if (inKlammer) {
                    out += in[i];
                }
            }

            for (Zug z : zuege) {
                if (z.getZugId() == Integer.parseInt(out)) {
                    return z;
                }
            }
        }
        return null;
    }
}
