package servidor.main;

import servidor.negocios.Server;
import common.exceptions.InvalidPort;
import java.io.IOException;

/**
 *
 * @author asantos07
 */
public class Main {

    public static void main(String[] args) {
        int port;
        if (args.length != 2 && args.length != 1) {
            System.exit(0);
        }
        try {
            if (args.length == 0) {
                Server server = new Server();
                server.startServer();
            } else {

                // switch the options chosen by the user.
                switch (args[0]) {
                    case "-p":
                        port = Integer.parseInt(args[1]);
                        if (port <= 1023 || port > 65535) {
                            throw new InvalidPort();
                        }
                        Server server = new Server(port);
                        server.startServer();
                        break;
                    default:
                        System.out.println("Opção Não Reconhecida");
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("Formato não reconhecido!");

        } catch (InvalidPort e) {
            System.out.println("Porta de ve estar entre 1024 e 65535!");

        } catch (IOException ex) {
            System.err.println("Não foi possível criar um servidor nesse endereço!");
        }

    }

}
