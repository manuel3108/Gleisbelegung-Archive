package com.gleisbelegung.lib;
/*
@author: Manuel Serret
@email: manuel-serret@t-online.de
@contact: Email, Github, STS-Forum

Achtung: Dies ist eine höchst sensible Klasse. Vor dem ändern unbedingt ein Backup der Klasse machen, aus diesem Grund gibt es hier auch keine Anmerkungen
 */

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class XMLHandler {
    BufferedReader in;

    public XMLHandler(InputStream in){
        try {
            this.in = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String[]> readLine(){
        try {
            String sTemp = "";
            sTemp = getOpeningTag(in.readLine());

            if(sTemp != null){
                ArrayList<String[]> temp = getContent(sTemp);

                if(temp.size() > 1){
                    temp.remove(temp.size()-1);
                }

                return temp;
            } else{
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<ArrayList<ArrayList<String[]>>> readLines(){
        try {
            String tag = getTagName(in.readLine());
            ArrayList<String> input = new ArrayList<>();
            do {
                input.add(in.readLine());
            } while(input.get(input.size()-1) != null &&!input.get(input.size()-1).contains(tag));
            input.remove(input.size()-1);

            ArrayList<ArrayList<ArrayList<String[]>>> out = new ArrayList<>();
            int counter = 0;
            int innerCounter = 0;
            for (int i = 0; i < input.size(); i++) {
                String line = input.get(i);
                tag = getTagName(line);
                out.add(counter, new ArrayList<>());

                if(!input.get(i).endsWith("/>")){
                    do{
                        out.get(counter).add(innerCounter, getContent(input.get(i)));
                        innerCounter = innerCounter + 1;
                        i = i +1;
                    }while(i < input.size() && ! input.get(i).contains(tag));
                } else{
                    out.get(counter).add(innerCounter, getContent(input.get(i)));
                }
                innerCounter = 0;
                counter = counter + 1;
                //System.out.println(out.size() + " " + out.get(counter-1).size() + " " + counter + " " + innerCounter);
            }

            return out;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getTagName(String s){
        try{
            if(s != null && !s.equals("")){
                char[] c = s.replace("<", "").replace(">", "").toCharArray();
                String out = "";
                for (int i = 0; i < s.indexOf(" "); i++) {
                    out += c[i];
                }
                return out.replace(" ", "");
            }
            return "";
        } catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    private ArrayList<String[]> getContent(String s){
        try{
            ArrayList<String[]> out = new ArrayList<>();
            char[] c = s.toCharArray();
            int step = 0;
            int counter = 0;
            for (int i = 0; i < c.length; i++) {
                if(step == 0){
                    out.add(counter,new String[2]);
                    out.get(counter)[0] = "" + c[i];
                    out.get(counter)[1] = "";
                    step = 1;
                } else if(step == 1){
                    if(c[i] == '='){
                        step = 2;
                        i = i + 1;
                    } else{
                        out.get(counter)[0] += c[i];
                    }
                } else if(step == 2 && c[i] != '\''){
                    out.get(counter)[1] += c[i];
                } else if(step == 2 && c[i] == '\''){
                    step = 0;
                    counter++;
                }
            }
            return out;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private String getOpeningTag(String s){
        try{
            if(s != null && !s.equals("")){
                String out = "";
                char[] c = s.toCharArray();
                for (int i = s.indexOf(" ")+1; i < s.indexOf(">"); i++) {
                    out += c[i];
                }
                return out;
            } else{
                return "";
            }
        } catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }
}
