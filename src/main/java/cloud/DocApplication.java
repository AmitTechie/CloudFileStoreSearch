package cloud;

import java.util.*;
import java.util.concurrent.*;

public class DocApplication {
    private IStorage iStorage;
    private IProcessCloudData iProcessCloudData;

    public DocApplication(IStorage iStorage, IProcessCloudData iProcessCloudData){
        this.iStorage = iStorage;
        this.iProcessCloudData = iProcessCloudData;
    }

    public List<FileDataModel> fetchCloudFiles(){
        return iStorage.fetchFilesFromCloud();
    }

    public void processFilesData(List<FileDataModel> fileDataModelList){
        iProcessCloudData.createIndex(fileDataModelList);
    }

    public List<FileDataModel> searchFilesData(String searchText){

        if(searchText.isEmpty()){
            System.out.println("error: searchText is null");
            return null;
        }

        List<FileDataModel> res = new ArrayList<>();


        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Callable<List<FileDataModel>> searchDataCallable = () -> {
            return iProcessCloudData.searchData(searchText);
        };


        Callable<List<FileDataModel>> fetchDataCallable = () -> {
            return iStorage.fetchFilesFromCloud();
        };

        Future<List<FileDataModel>> searchDataFuture = executorService.submit(searchDataCallable);
        Future<List<FileDataModel>> fetchDataFuture = executorService.submit(fetchDataCallable);

        try {
            List<FileDataModel> searchResult = searchDataFuture.get();
            List<FileDataModel> fetchResult = fetchDataFuture.get();

            Map<String, FileDataModel> filePathMap = new HashMap<>();
            for(FileDataModel fileDataModel: fetchResult){
                filePathMap.put(fileDataModel.getPath(), fileDataModel);
            }

            System.out.println(filePathMap);
            System.out.println(searchResult);
            int deletedFileCount = 0;

            for(FileDataModel fileDataModel: searchResult){
                if (filePathMap.containsKey(fileDataModel.getPath())){
                    System.out.println("DATA MODEL FOUND "+ fileDataModel.getName()+" - "+fileDataModel.getPath());
                    res.add(fileDataModel);
                }else{
                    deletedFileCount++;
                    System.out.println("DATA MODEL is deleted from colud: "+ fileDataModel.getName()+" - "+fileDataModel.getPath());
                }
            }

            //threshold set as 5
            if(deletedFileCount > 0){
                new Thread(new Runnable() {
                    public void run(){
                        iProcessCloudData.recreateIndex(fetchResult);
                    }
                }).start();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        executorService.shutdown();
        return res;
    }

}
