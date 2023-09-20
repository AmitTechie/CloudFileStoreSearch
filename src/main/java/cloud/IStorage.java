package cloud;

import java.util.List;

public interface IStorage {
    List<FileDataModel> fetchFilesFromCloud();
}
