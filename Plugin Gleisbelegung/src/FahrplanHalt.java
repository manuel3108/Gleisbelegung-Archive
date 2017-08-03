import java.util.ArrayList;

public class FahrplanHalt {
    private Zug z;
    private long ankuft;
    private long abfahrt;
    private String gleis;
    private String plangleis;
    private String flags;
    private ArrayList<LabelContainer> drawnTo;
    private boolean drawable;
    private Zug flaggedTrain;

    public FahrplanHalt(ArrayList<String[]> fahrplan, Zug z){
        this.z = z;
        this.abfahrt = Long.parseLong(fahrplan.get(0)[1]);
        this.gleis = fahrplan.get(1)[1];
        this.flags = fahrplan.get(2)[1];
        this.plangleis = fahrplan.get(3)[1];
        this.ankuft = Long.parseLong(fahrplan.get(4)[1]);

        this.drawnTo = new ArrayList<>();
        this.drawable = true;
        flaggedTrain = null;
    }

    public long getAnkuft() {
        return ankuft;
    }
    public void setAnkuft(long ankuft) {
        this.ankuft = ankuft;
    }

    public long getAbfahrt() {
        return abfahrt;
    }
    public void setAbfahrt(long abfahrt) {
        this.abfahrt = abfahrt;
    }

    public String getGleis() {
        return gleis;
    }
    public void setGleis(String gleis) {
        this.gleis = gleis;
    }

    public String getPlangleis() {
        return plangleis;
    }
    public void setPlangleis(String plangleis) {
        this.plangleis = plangleis;
    }

    public String getFlags() {
        return flags;
    }
    public void setFlags(String flags) {
        this.flags = flags;
    }

    public void removeDrawnTo() {
        for(LabelContainer lc : drawnTo){
            lc.removeTrain(z);
        }
        drawnTo = new ArrayList<>();
    }
    public void addDrawnTo(LabelContainer lc) {
        lc.addTrain(z);
        this.drawnTo.add(lc);
    }

    public LabelContainer getDrawnTo(int index){
        if(drawnTo.size() != 0){
            return  drawnTo.get(index);
        } else{
            return null;
        }
    }
    public ArrayList<LabelContainer> getDrawnTo(){
        return drawnTo;
    }

    public boolean isDrawable() {
        return drawable;
    }
    public void setDrawable(boolean drawable) {
        this.drawable = drawable;
    }

    public Zug getFlaggedTrain() {
        return flaggedTrain;
    }
    public void setFlaggedTrain(Zug flaggedTrain) {
        this.flaggedTrain = flaggedTrain;
    }
}
