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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
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


    private String nick;
    private String filePath;
    private String watchableDirectory = "client" + File.separator + "LocalStorage";
    private NetworkService networkService;

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


    private Object ArrayList;

    public void connectToServer(ActionEvent actionEvent) {
    }

    public void disconnectFromServer(ActionEvent actionEvent) {
    }

    public void exit(ActionEvent actionEvent) {
        System.exit(1);
    }




    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.networkService = new NetworkService(this);
        networkService.startConnection();
    }

    @Override
    public void processMessage(Object message) {
        Platform.runLater(() -> parseIncomingMessage(message));
    }

    private void parseIncomingMessage(Object object){
        if (object instanceof FileMessage){
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
        } else if (object.toString().equals("success")){
            System.out.println("success");
        }
        else if ( object instanceof String) {
            String[] receivedWords = object.toString().split("/");
            String start = receivedWords[0];
            switch (start) {
                case "auth_ok" :
                    this.nick = receivedWords[2];
                    loginPanel.setVisible(false);
                    mainPanel.setVisible(true);
                    break;
                case "error" :
                    showError(receivedWords[1]);
                    System.out.println("got error " + receivedWords[1]);
                    break;
                case "registration_ok" :
                    this.nick = receivedWords[2];
                    registrationBlock.setVisible(false);
                    mainPanel.setVisible(true);
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
//        if (newNickField.getText().isBlank()) return;
//        networkService.sendMessage("/change_nick" + REGEX + newNickField.getText());
    }

    public void sendChangePass(ActionEvent actionEvent) {
//        if (newPasswordField.getText().isBlank() || oldPassField.getText().isBlank()) return;
//        networkService.sendMessage("/change_pass" + REGEX + oldPassField.getText() + REGEX + newPasswordField.getText());
    }

//    public void sendEternalLogout(ActionEvent actionEvent) {
//        networkService.sendMessage("/remove");
//    }

    public void returnToChat(ActionEvent actionEvent) {
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
    }

    public void goToNextDirectoryInCloudStorageOnDoubleClick(MouseEvent mouseEvent) {
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
    }

    public void initializeListOfLocalStorageItems(ActionEvent actionEvent) {
    }

    public void transferFilesToCloudStorage(ActionEvent actionEvent) {
        networkService.transferFilesToCloudStorage(CurrentLogin.getCurrentLogin(),getPathsOfSelectedFilesInLocalStorage());
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


    public void updateCloudStoragePanel(ActionEvent actionEvent) {
    }

    public void selectAllFilesFromCloudStorage(ActionEvent actionEvent) {
    }

    public void goToOpeningPanelToChangeProfileOrLeaveApp(ActionEvent actionEvent) {
    }

    public void goToPreviousDirectoryInLocalStorage(ActionEvent actionEvent) {
    }

    public void goToPreviousDirectoryInCloudStorage(ActionEvent actionEvent) {
    }
}
