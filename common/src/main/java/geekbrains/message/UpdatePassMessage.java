package geekbrains.message;

public class UpdatePassMessage extends AbstractMessage{
    private String password;
    private String login;

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public UpdatePassMessage (String login, String password) {
        this.password = password;
        this.login = login;
    }
}
