import java.util.ArrayList;
import java.util.List;

public class PhotoManager {
    private List<Photo> photos;

    public PhotoManager() {
        photos = new ArrayList<>();
    }

    public void addPhoto(Photo photo) {
        photos.add(photo);
    }

    public Object[][] getPhotosData() {
        Object[][] data = new Object[photos.size()][2];
        for (int i = 0; i < photos.size(); i++) {
            Photo photo = photos.get(i);
            data[i][0] = photo.getFilePath();
            data[i][1] = photo.getMetadata();
        }
        return data;
    }

    public List<Photo> getPhotosByMetadata(String[] searchTerms) {
        List<Photo> filteredPhotos = new ArrayList<>();
        for (Photo photo : photos) {
            boolean matches = true;
            for (String term : searchTerms) {
                if (!photo.getMetadata().contains(term.trim())) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                filteredPhotos.add(photo);
            }
        }
        return filteredPhotos;
    }
}
