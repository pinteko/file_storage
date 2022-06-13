package geekbrains.network;

import geekbrains.launcher.MainController;
import geekbrains.message.*;
import geekbrains.property.PropertyReader;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Objects;

public class NetworkService {

    private final String host;

    private final int port;

    private static MessageProcessor messageProcessor;
    private static Socket socket;
    private static ObjectEncoderOutputStream outcomingStream;
    private static ObjectDecoderInputStream incomingStream;

    public NetworkService(MessageProcessor messageProcessor) {
        host = PropertyReader.getInstance().getHost();
        port = PropertyReader.getInstance().getPort();
        this.messageProcessor = messageProcessor;
    }

    public void startConnection() {
        try {
            socket = new Socket(host, port);
            outcomingStream = new ObjectEncoderOutputStream(socket.getOutputStream());
            incomingStream = new ObjectDecoderInputStream(socket.getInputStream(), 20971520);
            readMessages();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void readMessages() {
        var thread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                    var message = incomingStream.readObject();
                    messageProcessor.processMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        thread.setDaemon(true);
        thread.start();
        System.out.println("start");
    }

    public static void stopConnection() {
        try {
            outcomingStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            incomingStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean sendDeletionMessage(String login, LinkedList<File> filesToDelete) {
        try {
            if (!filesToDelete.isEmpty()) {
                outcomingStream.writeObject(new DeletionMessage(login, filesToDelete));
                outcomingStream.flush();
                return true;
            } else {
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean transferFilesToCloudStorage(String login, LinkedList<File> filesToSendToCloud) {
        try {
            if (!filesToSendToCloud.isEmpty()) {
                for (int i = 0; i < filesToSendToCloud.size(); i++) {
                    Path path = Paths.get(filesToSendToCloud.get(i).getAbsolutePath());
                    if (filesToSendToCloud.get(i).isDirectory()) {
                        File[] files = filesToSendToCloud.get(i).listFiles();
                        assert files != null;
                        if (files.length == 0) {
                                outcomingStream.writeObject(new FileMessage(login, path, true, true));
                                outcomingStream.flush();
                            } else {
                            outcomingStream.writeObject(new FileMessage(login, path, true, false));
                            outcomingStream.flush();
                            NetworkService.directorySending(files, login, path);}
                    } else if (filesToSendToCloud.get(i).length() < Integer.MAX_VALUE) {
                        outcomingStream.writeObject(new FileMessage(login, path));
                        outcomingStream.flush();
                    }
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void directorySending(File[] files, String login, Path path) throws IOException {
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                if (Objects.requireNonNull(files[i].listFiles()).length == 0) {
                    outcomingStream.writeObject(new FileMessage(login, files[i].getAbsoluteFile().toPath(), true, true));
                    outcomingStream.flush();
                }
                else {
                    outcomingStream.writeObject(new FileMessage(login, files[i].getAbsoluteFile().toPath(), true, true));
                    outcomingStream.flush();
                    NetworkService.directorySending(Objects.requireNonNull(files[i].listFiles()), login, files[i].getAbsoluteFile().toPath());
                }
            }
            else {
                    outcomingStream.writeObject(new FileMessage(login, files[i].getAbsoluteFile().toPath(), path.getFileName().toString()));
                    outcomingStream.flush();
            }
        }
    }


    public static boolean sendUpdateMessageToServer(String login){
        try {
            outcomingStream.writeObject(new UpdateMessage(login));
            outcomingStream.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean sendUpdatePassMessageToServer(String login, String password){
        try {
            outcomingStream.writeObject(new UpdatePassMessage(login, password));
            outcomingStream.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static boolean sendFileRequest(LinkedList<File> filesToRequest){
        try {
            if (!filesToRequest.isEmpty()){
                outcomingStream.writeObject(new FileRequest(filesToRequest));
                outcomingStream.flush();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;

    }

    public static boolean sendAuthMessageToServer(String login, String password) {
        try {
            outcomingStream.writeObject(new AuthMessage(login,password));
            outcomingStream.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean sendRegMessageToServer(String login, String password) {
        try {
            outcomingStream.writeObject(new RegistrationMessage(login,password));
            outcomingStream.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


}

