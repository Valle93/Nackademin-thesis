package Services;

import java.sql.SQLException;

public class ServerMain {

    public static void main(String[] args) throws SQLException {

        long then = System.currentTimeMillis();

        MegaServer megaServer = new MegaServer(true);

        long HowLongToStartServerEtc = System.currentTimeMillis() - then;

        System.out.println(HowLongToStartServerEtc);

    }
}
