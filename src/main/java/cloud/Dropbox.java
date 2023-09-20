package cloud;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Dropbox implements IStorage{
    private static final String ACCESS_TOKEN = "sl.BmZSWDvsL8RaJJhjE6cGC_QXxVENQX5IO_fXx9ugbhstP0FcK7Ea-r8zfuxCBaeyU98rx-uEI3oE0tQMlgTD2ayDaHL6kFL0WAv3lgL63VB2U8exjVJo-XAK-TmWpf5S9JS2MwqnWyU29f0gTL7i";
    DbxClientV2 client;

    public Dropbox(){
        init();
    }

    public void init(){
        // Create Dropbox client
        DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
        client = new DbxClientV2(config, ACCESS_TOKEN);
        client.fileProperties();

        // Get current account info
        FullAccount account = null;
        try {
            account = client.users().getCurrentAccount();
            System.out.println(account.getName().getDisplayName());
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }

    public List<FileDataModel> searchFilesAll() throws DbxException, IOException {

        List<Metadata> files = searchFiles("");
        System.out.println("file count: "+files.size());

        if(files.isEmpty()){
            return null;
        }


        List<FileDataModel> res = new ArrayList<>();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DbxDownloader<FileMetadata> downloader = null;

        for(Metadata metadata: files){
//            System.out.println(metadata.toStringMultiline());
            downloader = client.files().download(metadata.getPathDisplay());
            out.reset();
//            System.out.println(downloader.toString());
//            System.out.println(downloader.getContentType());
            downloader.download(out);
            System.out.println("FILE: " + metadata.getPathDisplay());

            FileDataModel  fileDataModel = new FileDataModel();
            fileDataModel.setName(metadata.getName());
            fileDataModel.setPath(metadata.getPathDisplay());

            if (metadata.getPathDisplay().endsWith(".txt") || metadata.getPathDisplay().endsWith(".csv") || metadata.getPathDisplay().endsWith(".pdf") || metadata.getPathDisplay().endsWith(".docx")){
                String fileContent = out.toString();
                if(metadata.getPathDisplay().endsWith(".pdf") || metadata.getPathDisplay().endsWith(".docx")){
                    fileContent = Parser.extractContentUsingParser(new ByteArrayInputStream(out.toByteArray()));
                }
                fileDataModel.setContent(fileContent);
                res.add(fileDataModel);
            }
        }

        return res;

    }

    public List<Metadata> searchFiles(String path) throws DbxException, IOException {

        List<Metadata> filesMetadata = new ArrayList<>();

        // Get files and folder metadata from Dropbox root directory
        ListFolderResult result = client.files().listFolder(path);
        while(true){
            for (Metadata metadata : result.getEntries()) {
                if (metadata instanceof FileMetadata) {
                    filesMetadata.add(metadata);
                }
                else if (metadata instanceof FolderMetadata){
                    filesMetadata.addAll(searchFiles(metadata.getPathDisplay()));
                }
            }

            if (!result.getHasMore()) {
                break;
            }

            result = client.files().listFolderContinue(result.getCursor());
        }

        return filesMetadata;

    }

    @Override
    public List<FileDataModel> fetchFilesFromCloud() {
        try {
            return searchFilesAll();
        } catch (DbxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
