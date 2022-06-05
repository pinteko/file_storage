package geekbrains.launcher;

import geekbrains.message.*;
import geekbrains.network.MessageProcessor;
import geekbrains.network.NetworkService;
import geekbrains.types.Message;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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


    private String nick;
    private String filePath;
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
    private VBox mainPanel;

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
                Path pathToNewEmptyDirectory = Paths.get("ClientSide"+File.separator+"LocalStorage"+File.separator+""+fileMessage.getFileName());
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
                    Files.write(Paths.get("ClientSide"+File.separator+"LocalStorage"+File.separator+""+fileMessage.getFileName()), fileMessage.getData(), StandardOpenOption.CREATE);
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
                case "/auth_ok" :
                    this.nick = receivedWords[2];
                    loginPanel.setVisible(false);
                    mainPanel.setVisible(true);
                    break;
                case "/error" :
                    showError(receivedWords[1]);
                    System.out.println("got error " + receivedWords[1]);
                    break;
                case "/registration_ok" :
                    this.nick = receivedWords[2];
                    registrationBlock.setVisible(false);
                    mainPanel.setVisible(true);
            }
        }
    }



    private void showError(String message) {
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
        if (!registrationLoginForm.getText().isEmpty() && !passwordField.getText().isEmpty() && !repeatPassForm.getText().isEmpty()){
            if (passwordField.getText().equals(repeatPassForm.getText())){
                NetworkService.sendRegMessageToServer(loginField.getText(),repeatPassForm.getText());
            }else {
                registrationMessage.setText("You've entered unequal passwords");
                passwordField.clear();
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




    public void showSendFile(ActionEvent actionEvent) {
        mainPanel.setVisible(false);
        sendFilePanel.setVisible(true);
    }

    public void showCopyFile(ActionEvent actionEvent) {
        mainPanel.setVisible(false);
        copyFilePanel.setVisible(true);
    }
    public void sendFile(ActionEvent actionEvent) {
//       String pathHome = pathHomeFile.getText();
//       String packageCloud = packageAndNameForCloudFile.getText();
        networkService.transferFilesToCloudStorage(CurrentLogin.getCurrentLogin(), getPathsOfSelectedFilesInCloudStorage());
    }

    public void copyFile(ActionEvent actionEvent) {
        networkService.sendFileRequest(getPathsOfSelectedFilesInCloudStorage());
//        String pathHome = pathHomeFileToCopy.getText();
//        String packageCloud = packageAndNameOfCloudFile.getText();
//        Message message = new Message(pathHome, packageCloud);
//        networkService.send(message, s -> System.out.println("s = " + s));
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

    public void goToNextDirectoryInLocalStorageOnDoubleClickOrOpenFile(MouseEvent mouseEvent) {
    }

    public void goToNextDirectoryInCloudStorageOnDoubleClick(MouseEvent mouseEvent) {
    }
}
