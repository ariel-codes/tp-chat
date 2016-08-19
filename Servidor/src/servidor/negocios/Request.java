package servidor.negocios;

import common.message.Message;

/**
 *
 * @author asantos07
 */
public class Request {

    private final Client client;
    private final Message msg;

    public Request(Client c, Message m) {
        this.client = c;
        this.msg = m;
    }

    public Client getClient() {
        return client;
    }

    public Message getMsg() {
        return msg;
    }

}
