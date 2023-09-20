package cloud;

public class FileDataModel {
    String name;
    String path;
    String content;
    public FileDataModel(){};
    public FileDataModel(String name, String path, String content){
        this.name = name;
        this.path = path;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getContent() {
        return content;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
