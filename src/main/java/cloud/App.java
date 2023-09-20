package cloud;

import java.util.List;
import java.util.Scanner;

/**
 * Hello world!
 */
public class App {

    private static final String MESSAGE = "Hello World!";

    public App() {}

    public static void main(String[] args) {
        System.out.println(MESSAGE);

        DocApplication docApplication = new DocApplication(new Dropbox(), new ESProcessCloudData());
        List<FileDataModel>res = docApplication.fetchCloudFiles();
        docApplication.processFilesData(res);


        Scanner sc = new Scanner(System.in);
        String input = "";
        while (true) {

            System.out.println("To end the program enter: quit");
            System.out.println("To continue search, enter the search text..");

            input = sc.next();

            if (input.equals("quit")) {
                break;
            }

            List<FileDataModel> searchRes = docApplication.searchFilesData(input);
            if (searchRes != null && !searchRes.isEmpty()) {
                System.out.println("Found the search data..");
                for (FileDataModel fileDataModel : searchRes) {
                    System.out.println(fileDataModel.getName() + " - " + fileDataModel.getPath() + " - Content: " + fileDataModel.getContent());
                }
            }
        }
    }

    public String getMessage() {
        return MESSAGE;
    }
}
