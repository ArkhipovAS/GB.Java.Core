package client.controller;

import client.Command;
import client.view.AuthDialog;
import client.view.ClientChat;
import client.model.NetworkService;

import javax.swing.*;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class ClientController {

    private static final String ALL_USERS_LIST_ITEM = "All";
    private final NetworkService networkService;
    private final AuthDialog authDialog;
    private final ClientChat clientChat;
    private String nickname;

    public ClientController(String serverHost, int serverPort) {
        this.networkService = new NetworkService(serverHost, serverPort, this);
        this.authDialog = new AuthDialog(this);
        this.clientChat = new ClientChat(this);
    }

    public void runApplication() throws IOException {
        connectToServer();
        runAuthProcess();
    }

    private void runAuthProcess() {
        networkService.setSuccessfulAuthEvent(new AuthEvent() {
            @Override
            public void authIsSuccessful(String nickname) {
                ClientController.this.setUserName(nickname);
                ClientController.this.openChat();
            }
        });
        authDialog.setVisible(true);

    }

    private void openChat() {
        authDialog.dispose();
        networkService.setMessageHandler(new Consumer<String>() {
            @Override
            public void accept(String message) {
                clientChat.appendMessage(message);
            }
        });
        clientChat.setVisible(true);
    }

    private void setUserName(String nickname) {
        SwingUtilities.invokeLater(() -> {
            clientChat.setTitle(nickname);
        });
        this.nickname = nickname;
    }

    private void connectToServer() throws IOException {
        try {
            networkService.connect();
        } catch (IOException e) {
            System.err.println("Failed to establish server connection");
            throw e;
        }
    }

    public void sendAuthMessage(String login, String pass) throws IOException {
        sendCommand(Command.authCommand(login, pass));
    }

    public void sendMessage(String message) {
        sendCommand(Command.broadcastMessageCommand(message));
    }

    private void sendCommand(Command command) {
        try {
            networkService.sendCommand(command);
        } catch (IOException e) {
            showErrorMessage(e.getMessage());
        }
    }

    public void shutdown() {
        networkService.close();
    }

    public String getUsername() {
        return nickname;
    }

    public void showErrorMessage(String errorMessage) {
        if (clientChat.isActive()) {
            clientChat.showError(errorMessage);
        } else if (authDialog.isActive()) {
            authDialog.showError(errorMessage);
        }
        System.err.println(errorMessage);
    }

    public void sendPrivateMessage(String username, String message) {
        sendCommand(Command.privateMessageCommand(username, message));
    }

    public void updateUsersList(List<String> users) {
        users.remove(nickname);
        users.add(0, ALL_USERS_LIST_ITEM);
        clientChat.updateUsers(users);
    }
}
