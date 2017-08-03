public class Zug {
    private int zugId;
    private String zugName;
    private int verspaetung;
    private String gleis;
    private String plangleis;
    private boolean amGleis;
    private String von;
    private String nach;
    private boolean sichtbar;
    private FahrplanHalt[] fahrplan;
    private boolean needUpdate;
    private boolean newTrain;

    public Zug(int zugId, String zugName){
        this.zugId = zugId;
        this.zugName = zugName;

        this.newTrain = true;
        this.needUpdate = true;
    }

    public String getZugName() {
        return zugName;
    }
    public void setZugName(String zugName) {
        this.zugName = zugName;
    }

    public int getZugId() {
        return zugId;
    }
    public void setZugId(int zugId) {
        this.zugId = zugId;
    }

    public int getVerspaetung() {
        return verspaetung;
    }
    public void setVerspaetung(int verspaetung) {
        this.verspaetung = verspaetung;
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

    public boolean getAmGleis() {
        return amGleis;
    }
    public void setAmGleis(boolean amGleis) {
        this.amGleis = amGleis;
    }

    public String getVon() {
        return von;
    }
    public void setVon(String von) {
        this.von = von;
    }

    public String getNach() {
        return nach;
    }
    public void setNach(String nach) {
        this.nach = nach;
    }

    public boolean getSichtbar() {
        return sichtbar;
    }
    public void setSichtbar(boolean sichtbar) {
        this.sichtbar = sichtbar;
    }

    public FahrplanHalt[] getFahrplan() {
        return fahrplan;
    }
    public FahrplanHalt getFahrplan(int index) {
        if(index < fahrplan.length){
            return fahrplan[index];
        } else {
            return null;
        }
    }
    public void setFahrplan(FahrplanHalt[] fahrplan) {
        this.fahrplan = fahrplan;
    }

    public boolean isNeedUpdate() {
        return needUpdate;
    }
    public void setNeedUpdate(boolean needsUpdate) {
        this.needUpdate = needsUpdate;
    }

    public boolean isNewTrain() {
        return newTrain;
    }
    public void setNewTrain(boolean newTrain) {
        this.newTrain = newTrain;
    }

    public void removeFromGrid(){
        if(fahrplan != null){
            for(FahrplanHalt fh : fahrplan){
                if(fh != null){
                    fh.removeDrawnTo();
                }
            }
        }
    }

    public String getVerspaetungToString(){
        String out = "";

        if (verspaetung > 0) {
            out = " (+" + verspaetung + ")";
        } else if (verspaetung < 0) {
            out = " (" + verspaetung + ")";
        }

        return out;
    }
}