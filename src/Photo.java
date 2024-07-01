public class Photo {
    private String filePath;
    private String metadata;

    public Photo(String filePath, String metadata) {
        this.filePath = filePath;
        this.metadata = metadata;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getMetadata() {
        return metadata;
    }
}
