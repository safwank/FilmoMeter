package safwan.filmometer.data;

public class SourceFilm extends Film {
    private boolean isPrimarySource;

    public boolean isPrimarySource() {
        return isPrimarySource;
    }

    public void setPrimarySource(boolean primarySource) {
        isPrimarySource = primarySource;
    }
}
