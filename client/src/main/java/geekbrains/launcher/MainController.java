package geekbrains.launcher;

import geekbrains.message.*;
import geekbrains.network.MessageProcessor;
import geekbrains.network.NetworkService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ResourceBundle;

public class MainController implements Initializable, MessageProcessor {
    public static final String REGEX = "%!%";


    @FXML
    public VBox registrationBlock;
    @FXML
    public Label registrationMessage;
    @FXML
    public TextField registrationLoginForm;
    @FXML
    public PasswordField registrationPassForm;
    @FXML
    public VBox startPanel;
    @FXML
    public VBox secondBlockMainPanel;
    @FXML
    public HBox upperLocalStorPanel1;
    @FXML
    public Button cloudStorageDownload;
    @FXML
    public Button cloudStorageDelete;
    @FXML
    public Button cloudStorageUpdate;
    @FXML
    public Button localStorageSend;
    @FXML
    public Button localStorageDelete;
    @FXML
    public Button localStorageUpdate;
    @FXML
    public VBox firstMainPanel;


    private String login;
    private String currentDirectoryName = "";
    private String watchableDirectory = "client" + File.separator + "LocalStorage";
    private int localStorageFolderLevelCounter = 0;
    private int cloudStorageFolderLevelCounter = 0;
    private NetworkService networkService;

    private LinkedList<File> pathsToCloudStorageFiles;

    private HashMap<Integer, LinkedList<File>> folderCloudStorageListViews;

    @FXML
    private PasswordField repeatPassForm;

    @FXML
    public TextField pathHomeFile;

    @FXML
    public TextField packageAndNameForCloudFile;

    @FXML
    public TextField packageAndNameOfCloudFile;

    @FXML
    public TextField pathHomeFileToCopy;

    @FXML
    private VBox loginPanel;

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private HBox mainPanel;

    @FXML
    private TextArea mainChatArea;

    @FXML
    private ListView fileList;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField oldPassField;

    @FXML
    private VBox changePasswordPanel;

    @FXML
    Button goToPreviousFolderInLocalStorageButton;
    @FXML
    Button goToPreviousFolderInCloudStorageButton;

    @FXML
    ListView<StorageItem> listOfCloudStorageElements;
    @FXML
    ListView<StorageItem> listOfLocalElements;

    @FXML
    private TextField newNickField;

    @FXML
    private VBox sendFilePanel;

    @FXML
    private VBox copyFilePanel;

    @FXML
    private VBox changeNickPanel;



    public void exit(ActionEvent actionEvent) {
        System.exit(1);
    }




    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.networkService = new NetworkService(this);
        networkService.startConnection();
        initializeListOfLocalStorageItems();
//        initializeListOfCloudStorageItems();
    }

    @Override
    public void processMessage(Object message) {
        Platform.runLater(() -> parseIncomingMessage(message));
    }

    private void parseIncomingMessage(Object object){
        if (object instanceof UpdateMessage) {
            UpdateMessage message = (UpdateMessage) object;
            folderCloudStorageListViews = new HashMap<>();
            folderCloudStorageListViews.putAll(message.getCloudStorageContents());
            initializeListOfCloudStorageItems(folderCloudStorageListViews);
        }
       else if (object instanceof FileMessage){
            FileMessage fileMessage = (FileMessage)object;
            if (fileMessage.isDirectory() && fileMessage.isEmpty()){
                Path pathToNewEmptyDirectory = Paths.get("client"+File.separator+"LocalStorage"+File.separator+""+fileMessage.getFileName());
                if (Files.exists(pathToNewEmptyDirectory)) {
                    System.out.println("Такая директория уже существует");
                }else {
                        try {
                            Files.createDirectory(pathToNewEmptyDirectory);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }else {
                try {
                    Files.write(Paths.get("client"+File.separator+"LocalStorage"+File.separator+""+fileMessage.getFileName()), fileMessage.getData(), StandardOpenOption.CREATE);
                }catch (NullPointerException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if ( object instanceof String) {
            String[] receivedWords = object.toString().split("/");
            String start = receivedWords[0];
            switch (start) {
                case "auth_ok" :
                    this.login = receivedWords[2];
                    loginPanel.setVisible(false);
                    mainPanel.setVisible(true);
                    break;
                case "error" :
                    showError(receivedWords[1]);
                    System.out.println("got error " + receivedWords[1]);
                    break;
                case "registration_ok" :
                    this.login = receivedWords[2];
                    registrationBlock.setVisible(false);
                    mainPanel.setVisible(true);
                case "success" :
                    System.out.println(receivedWords[1]);
            }
        }
    }



    public static void showError(String message) {
        var alert = new Alert(Alert.AlertType.ERROR,
                "An error occured: " + message,
                ButtonType.OK);
        alert.showAndWait();
    }

    public void sendAuthMessage(){
        if (!loginField.getText().isEmpty() && !passwordField.getText().isEmpty()){
            NetworkService.sendAuthMessageToServer(loginField.getText(),passwordField.getText());
            loginField.clear();
            passwordField.clear();
        }
    }
    public void sendRegMessageToServer(){
        if (!registrationLoginForm.getText().isEmpty() && !registrationPassForm.getText().isEmpty() && !repeatPassForm.getText().isEmpty()){
            if (registrationPassForm.getText().equals(repeatPassForm.getText())){
                NetworkService.sendRegMessageToServer(registrationLoginForm.getText(),repeatPassForm.getText());
            }else {
                registrationMessage.setText("You've entered unequal passwords");
                registrationPassForm.clear();
                repeatPassForm.clear();
            }
        }
    }



    public void sendChangeNick(ActionEvent actionEvent) {
        if (newNickField.getText().isBlank()) return;
        NetworkService.sendUpdateMessageToServer(newNickField.getText());
        this.login = newNickField.getText();
    }

    public void sendChangePass(ActionEvent actionEvent) {
        if (newPasswordField.getText().isBlank() || oldPassField.getText().isBlank()) return;
        NetworkService.sendUpdatePassMessageToServer(login, newPasswordField.getText());
    }

    public static long getActualSizeOfFolder(File file) throws Exception {
        long actualSizeOfFolder = 0;
        if (file.isDirectory()){
            for (File f: file.listFiles()){
                if (f.isFile()){
                    actualSizeOfFolder += f.length();
                }else if (f.isDirectory()){
                    actualSizeOfFolder += getActualSizeOfFolder(f);
                }
            }
        }
        return actualSizeOfFolder;
    }

    public void returnToMainWindow(ActionEvent actionEvent) {
        changeNickPanel.setVisible(false);
        changePasswordPanel.setVisible(false);
        mainPanel.setVisible(true);
    }

    public void showChangeNick(ActionEvent actionEvent) {
        mainPanel.setVisible(false);
        changeNickPanel.setVisible(true);
    }

    public void showChangePass(ActionEvent actionEvent) {
        mainPanel.setVisible(false);
        changePasswordPanel.setVisible(true);
    }





    public void goToNextDirectoryInLocalStorageOnDoubleClickOrOpenFile(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {
            listOfLocalElements.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        } else if (mouseEvent.getClickCount() == 2) {
            listOfLocalElements.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            if (listOfLocalElements.getSelectionModel().getSelectedItems().size() == 1) {
                File pathToClickedFile;
                pathToClickedFile = listOfLocalElements.getSelectionModel().getSelectedItem().getPathToFile();
                if (pathToClickedFile.isDirectory()) {
                    File[] nextDirectory = pathToClickedFile.listFiles();
                    if (nextDirectory.length == 0) {

                    }else if (nextDirectory.length != 0) {
                        localStorageFolderLevelCounter++;
                        if (localStorageFolderLevelCounter > 0) {
                            goToPreviousFolderInLocalStorageButton.setVisible(true);
                        }
                        if (localStorageFolderLevelCounter > 0 && nextDirectory.length != 0) {
                            watchableDirectory += File.separator + pathToClickedFile.getName();
                            currentDirectoryName = pathToClickedFile.getName();
                        } else {
                            currentDirectoryName = "client/LocalStorage";
                        }
                        ObservableList<StorageItem> listOfLocalItems = FXCollections.observableArrayList();
                        for (int i = 0; i < nextDirectory.length; i++) {
                            String nameOfLocalFileOrDirectory = nextDirectory[i].getName();
                            long initialSizeOfLocalFileOrDirectory = 0;
                            try {
                                if (nextDirectory[i].isDirectory()){
                                    initialSizeOfLocalFileOrDirectory = getActualSizeOfFolder(nextDirectory[i]);
                                }else{
                                    initialSizeOfLocalFileOrDirectory = nextDirectory[i].length();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            String dateOfLastModification = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                                    .format(new Date(nextDirectory[i].lastModified()));
                            File pathOfFileInLocalStorage = new File(nextDirectory[i].getAbsolutePath());
                            listOfLocalItems.addAll(new StorageItem(nameOfLocalFileOrDirectory, initialSizeOfLocalFileOrDirectory, false, dateOfLastModification,pathOfFileInLocalStorage));
                            listOfLocalElements.setItems(listOfLocalItems);
                            listOfLocalElements.setCellFactory(param -> new StorageListViewItem());
                        }

                    }
                }else {
                    Desktop desktop = null;
                    if (desktop.isDesktopSupported()){
                        desktop = desktop.getDesktop();
                        try {
                            desktop.open(pathToClickedFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public void goToNextDirectoryInCloudStorageOnDoubleClick(MouseEvent mouseEvent) {
        pathsToCloudStorageFiles = new LinkedList<>();
        if (mouseEvent.getClickCount() == 1) {
            listOfCloudStorageElements.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        } else if (mouseEvent.getClickCount() == 2) {
            listOfCloudStorageElements.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            if (listOfCloudStorageElements.getSelectionModel().getSelectedItems().size() == 1) {
                File pathToClickedFile = new File("");
                for (int i = 0; i < folderCloudStorageListViews.get(cloudStorageFolderLevelCounter).size(); i++) {
                    File file = folderCloudStorageListViews.get(cloudStorageFolderLevelCounter).get(i);
                    if (listOfCloudStorageElements.getSelectionModel().getSelectedItem().getName().equals(file.getName())) {
                        pathToClickedFile = folderCloudStorageListViews.get(cloudStorageFolderLevelCounter).get(i);
                    }
                }
                if (pathToClickedFile.isDirectory()) {
                    File[] nextDirectory = pathToClickedFile.listFiles();
                    if (nextDirectory.length == 0) {
                        System.out.println("пустая директория");
                    }
                    if (nextDirectory.length != 0) {
                        for (int i = 0; i < nextDirectory.length; i++) {
                            try {
                                pathsToCloudStorageFiles.add(nextDirectory[i]);
                            } catch (IndexOutOfBoundsException e) {
                                e.printStackTrace();
                            }
                        }
                        cloudStorageFolderLevelCounter++;
                        folderCloudStorageListViews.put(cloudStorageFolderLevelCounter, pathsToCloudStorageFiles);
                        ObservableList<StorageItem> listOfCloudItems = FXCollections.observableArrayList();
                        for (int i = 0; i < nextDirectory.length; i++) {
                            String nameOfCloudStorageFileOrDirectory = nextDirectory[i].getName();
                            long initialSizeOfLocalStorageFileOrDirectory = nextDirectory[i].length();
                            String dateOfLastModification = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                                    .format(new Date(nextDirectory[i].lastModified()));
                            File pathToFileInLocalStorage = new File(nextDirectory[i].getAbsolutePath());
                            listOfCloudItems.addAll(new StorageItem(nameOfCloudStorageFileOrDirectory, initialSizeOfLocalStorageFileOrDirectory, false, dateOfLastModification, pathToFileInLocalStorage));
                            listOfCloudStorageElements.setItems(listOfCloudItems);
                            listOfCloudStorageElements.setCellFactory(param -> new StorageListViewItem());
                        }
                    }
                    if (cloudStorageFolderLevelCounter > 0) {
                        goToPreviousFolderInCloudStorageButton.setVisible(true);
                    }
                }else {
                    System.out.println("Это не директория");
                }
            }
        }
    }

    public void choiceAuth(ActionEvent actionEvent) {
        startPanel.setVisible(false);
        loginPanel.setVisible(true);
    }

    public void choiceRegistration(ActionEvent actionEvent) {
        startPanel.setVisible(false);
        registrationBlock.setVisible(true);
    }

    public void selectAllFilesFromLocalStorage(ActionEvent actionEvent) {
        if (listOfLocalElements.getItems().size() == listOfLocalElements.getSelectionModel().getSelectedItems().size()) {
            listOfLocalElements.getSelectionModel().clearSelection();
        } else {
            listOfLocalElements.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            listOfLocalElements.getSelectionModel().selectAll();
        }
    }

    public void initializeListOfCloudStorageItems(HashMap<Integer, LinkedList<File>> listOfCloudStorageFiles) {
        if (cloudStorageFolderLevelCounter > 0){
            cloudStorageFolderLevelCounter = 0;
            goToPreviousFolderInCloudStorageButton.setVisible(false);
        }
        try {
            ObservableList<StorageItem> listOfCloudItems = FXCollections.observableArrayList();
            if (!listOfCloudStorageFiles.isEmpty()) {
                for (int i = 0; i < listOfCloudStorageFiles.get(0).size(); i++) {
                    long initialSizeOfCloudFileOrDir = 0;
                    String nameOfCloudFileOrDir = listOfCloudStorageFiles.get(0).get(i).getName();
                    if (listOfCloudStorageFiles.get(0).get(i).isDirectory()) {
                        try {
                            initialSizeOfCloudFileOrDir = getActualSizeOfFolder(listOfCloudStorageFiles.get(0).get(i));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        initialSizeOfCloudFileOrDir = listOfCloudStorageFiles.get(0).get(i).length();
                    }
                    String dateOfLastModification = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(listOfCloudStorageFiles.get(0)
                            .get(i).lastModified()));
                    File pathOfFileInCloudStorage = new File(listOfCloudStorageFiles.get(0).get(i).getAbsolutePath());
                    listOfCloudItems.addAll(new StorageItem(nameOfCloudFileOrDir, initialSizeOfCloudFileOrDir, false, dateOfLastModification, pathOfFileInCloudStorage));
                }
                listOfCloudStorageElements.setItems(listOfCloudItems);
                listOfCloudStorageElements.setCellFactory(param -> new StorageListViewItem());
            } else {
                listOfCloudStorageElements.setItems(listOfCloudItems);
                listOfCloudStorageElements.setCellFactory(param -> new StorageListViewItem());
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }

    }

    public void initializeListOfLocalStorageItems() {
        ObservableList<StorageItem> listOfLocalItems = FXCollections.observableArrayList();
        File pathToLocalStorage = new File(watchableDirectory);
        File[] listOfLocalStorageFiles = pathToLocalStorage.listFiles();
        if (listOfLocalStorageFiles.length == 0 && localStorageFolderLevelCounter == 0) {
            Image image = new Image("/icons/dropfilesicon.png");
            BackgroundImage bi = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
            listOfLocalElements.setBackground(new Background(bi));
            listOfLocalElements.setOpacity(0.9);
            listOfLocalElements.setItems(listOfLocalItems);
            listOfLocalElements.setCellFactory(param -> new StorageListViewItem());
        } else if (listOfLocalStorageFiles.length > 0){
            listOfLocalElements.setBackground(null);
            for (int i = 0; i < listOfLocalStorageFiles.length; i++) {
                long initialSizeOfLocalFileOrDirectory = 0;
                String nameOfLocalFileOrDirectory = listOfLocalStorageFiles[i].getName();
                if (listOfLocalStorageFiles[i].isDirectory()) {
                    try {
                        initialSizeOfLocalFileOrDirectory = getActualSizeOfFolder(listOfLocalStorageFiles[i]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    initialSizeOfLocalFileOrDirectory = listOfLocalStorageFiles[i].length();
                }
                String dateOfLastModification = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(listOfLocalStorageFiles[i].lastModified()));
                File pathToFileInLocalStorage = new File(listOfLocalStorageFiles[i].getAbsolutePath());
                listOfLocalItems.addAll(new StorageItem(nameOfLocalFileOrDirectory, initialSizeOfLocalFileOrDirectory, false, dateOfLastModification, pathToFileInLocalStorage));
            }
            listOfLocalElements.setItems(listOfLocalItems);
            listOfLocalElements.setCellFactory(param -> new StorageListViewItem());
        }else {
            listOfLocalElements.setItems(listOfLocalItems);
            listOfLocalElements.setCellFactory(param -> new StorageListViewItem());
        }
    }

    public void transferFilesToCloudStorage(ActionEvent actionEvent) {
        networkService.transferFilesToCloudStorage(login,getPathsOfSelectedFilesInLocalStorage());
    }

    public LinkedList getPathsOfSelectedFilesInLocalStorage() {
        try {
            listOfLocalElements.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            LinkedList<File> listOfSelectedElementsInLocalStorage = new LinkedList<File>();
            if (listOfLocalElements.getSelectionModel().getSelectedItems().size() != 0) {
                System.out.println(listOfLocalElements.getSelectionModel().getSelectedItems().size());
                for (int i = 0; i < listOfLocalElements.getSelectionModel().getSelectedItems().size(); i++) {
                    listOfSelectedElementsInLocalStorage.add(listOfLocalElements.getSelectionModel().getSelectedItems().get(i).getPathToFile());
                }
                return listOfSelectedElementsInLocalStorage;
            }

        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return null;
    }

    public void downloadFilesIntoLocalStorage(ActionEvent actionEvent) {
        networkService.sendFileRequest(getPathsOfSelectedFilesInCloudStorage());
    }

    public LinkedList getPathsOfSelectedFilesInCloudStorage(){
        listOfCloudStorageElements.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        LinkedList<File> listOfSelectedElementsInCloudStorage = new LinkedList<File>();
        if (listOfCloudStorageElements.getSelectionModel().getSelectedItems().size() != 0){
            for (int i = 0; i < listOfCloudStorageElements.getSelectionModel().getSelectedItems().size(); i++) {
                listOfSelectedElementsInCloudStorage.add(listOfCloudStorageElements.getSelectionModel().getSelectedItems().get(i).getPathToFile());
            }
        }
        return listOfSelectedElementsInCloudStorage;
    }


    public void selectAllFilesFromCloudStorage(ActionEvent actionEvent) {
        if (listOfCloudStorageElements.getItems().size() == listOfCloudStorageElements.getSelectionModel().getSelectedItems().size()) {
            listOfCloudStorageElements.getSelectionModel().clearSelection();
        } else {
            listOfCloudStorageElements.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            listOfCloudStorageElements.getSelectionModel().selectAll();
        }
    }


    public void goToPreviousDirectoryInLocalStorage(ActionEvent actionEvent) {
        ObservableList<StorageItem> listOfLocalItems = FXCollections.observableArrayList();
        LinkedList<File> files = new LinkedList<>();
        File file = new File(watchableDirectory);
        File previousDirectory = new File(file.getParent());
        File[] contentsOfPreviousDirectory = previousDirectory.listFiles();
        for (int i = 0; i < contentsOfPreviousDirectory.length; i++) {
            files.add((contentsOfPreviousDirectory[i]));
        }
        for (int i = 0; i < files.size(); i++) {
            String nameOfLocalFileOrDirectory = files.get(i).getName();
            long initialSizeOfLocalFileOrDirectory = 0;
            try {
                if (files.get(i).isDirectory()){
                    initialSizeOfLocalFileOrDirectory = getActualSizeOfFolder(files.get(i));
                }else {
                    initialSizeOfLocalFileOrDirectory = files.get(i).length();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            String dateOfLastModification = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                    .format(new Date(files.get(i).lastModified()));
            File pathOfFileInLocalStorage = files.get(i).getAbsoluteFile();
            listOfLocalItems.addAll(new StorageItem(nameOfLocalFileOrDirectory, initialSizeOfLocalFileOrDirectory, false, dateOfLastModification, pathOfFileInLocalStorage
            ));
        }
        listOfLocalElements.setItems(listOfLocalItems);
        listOfLocalElements.setCellFactory(param -> new StorageListViewItem());
        localStorageFolderLevelCounter--;
        if (localStorageFolderLevelCounter <= 0) {
            goToPreviousFolderInLocalStorageButton.setVisible(false);
            watchableDirectory = "client"+File.separator+"LocalStorage";
            currentDirectoryName = "LocalStorage";
        }else {
            watchableDirectory = previousDirectory.toString();
            currentDirectoryName = previousDirectory.getName();
        }
    }

    public void goToPreviousDirectoryInCloudStorage(ActionEvent actionEvent) {
        ObservableList<StorageItem> listOfCloudItems = FXCollections.observableArrayList();
        LinkedList<File> files = new LinkedList<>();
        for (int i = 0; i < folderCloudStorageListViews.get(cloudStorageFolderLevelCounter - 1).size(); i++) {
            files.add((folderCloudStorageListViews.get(cloudStorageFolderLevelCounter - 1).get(i)));
        }
        for (int i = 0; i < files.size(); i++) {
            String nameOfLocalFileOrDirectory = files.get(i).getName();
            long initialSizeOfLocalFileOrDirectory = files.get(i).length();
            String dateOfLastModification = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                    .format(new Date(files.get(i).lastModified()));
            File pathToFileInCloudStorage = new File(files.get(i).getAbsolutePath());
            listOfCloudItems.addAll(new StorageItem(nameOfLocalFileOrDirectory, initialSizeOfLocalFileOrDirectory, false, dateOfLastModification, pathToFileInCloudStorage));
        }
        listOfCloudStorageElements.setItems(listOfCloudItems);
        listOfCloudStorageElements.setCellFactory(param -> new StorageListViewItem());
        folderCloudStorageListViews.remove(cloudStorageFolderLevelCounter);
        cloudStorageFolderLevelCounter--;
        if (cloudStorageFolderLevelCounter <= 0) {
            goToPreviousFolderInCloudStorageButton.setVisible(false);
        }
    }
}
