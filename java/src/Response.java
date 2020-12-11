import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Response {
    private String responseFileName = "";
    private String contentType = "";

    public Response(String responseFileName, String contentType) {
        this.responseFileName = responseFileName;
        this.contentType = contentType;
    }

    public String getResponseFile() throws IOException {
        return Files.readString(Paths.get(responseFileName));
    }

    public String getResponseFileName() {
        return responseFileName;
    }

    public String getResponseFileData() {
        File file = new File(responseFileName);
        return file.getAbsolutePath() + ": " + file.exists();
    }

    public String getContentType() {
        return contentType;
    }
}
