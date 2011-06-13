package safwan.filmometer.data;

public class SourceFilm extends Film {
    private boolean isPrimarySource;
    private String sourceDescription;

    public boolean isPrimarySource() {
        return isPrimarySource;
    }

    public void setPrimarySource(boolean primarySource) {
        isPrimarySource = primarySource;
    }

    public String getSourceDescription() {
        return sourceDescription;
    }

    public void setSourceDescription(String sourceDescription) {
        this.sourceDescription = sourceDescription;
    }
}
