package geekbrains.message;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMessage extends AbstractMessage {
    private String fileName;
    private String login;
    private byte[] data;
    private boolean isDirectory;
    private boolean isEmpty;

    private String parentPath = "";

    public FileMessage(Path path) throws IOException {
        fileName = path.getFileName().toString();
        data = Files.readAllBytes(path);
        this.isDirectory = false;
        this.isEmpty = false;
    }
    public FileMessage(String fileName, boolean isDirectory, boolean isEmpty){
        this.fileName = fileName;
        this.isDirectory = isDirectory;
        this.isEmpty = isEmpty;
    }

    public FileMessage(String login, Path path, boolean isDirectory, boolean isEmpty){
        this.login = login;
        fileName = path.getFileName().toString();
        this.isDirectory = isDirectory;
        this.isEmpty = isEmpty;
    }
    public FileMessage(String login, Path path, String parentName) throws IOException, AccessDeniedException {
        fileName = path.getFileName().toString();
        data = Files.readAllBytes(path);
        this.login = login;
        this.parentPath = parentName;
    }

    public FileMessage(String login, Path path) throws IOException, AccessDeniedException {
        fileName = path.getFileName().toString();
        data = Files.readAllBytes(path);
        this.login = login;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getData() {
        return data;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public String getLogin() {
        return login;
    }

    public String getParentName() {
        return parentPath;
    }
}
