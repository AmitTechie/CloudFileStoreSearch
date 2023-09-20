package cloud;

import java.util.List;

public interface IProcessCloudData {
    void createIndex(List<FileDataModel> dataList);
    void recreateIndex(List<FileDataModel> dataList);
    List<FileDataModel> searchData(String searchText);
}
