package cliente.negocios;


public class OnlineClient {

    private String nick;
    private final int ClientID;

    public OnlineClient(int iD) {
        this.ClientID = iD;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public int getClientID() {
        return ClientID;
    }

}
