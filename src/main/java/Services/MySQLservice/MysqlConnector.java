package Services.MySQLservice;
import java.sql.*;

public class MysqlConnector {

    private Connection connection;

    public MysqlConnector (){


        try {

            this.connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/productapi?autoReconnect=true&useSSL=false",
                    "root", "Jagheter5!");

            Statement statement = connection.createStatement();

            ResultSet resultset = statement.executeQuery("select * from product");



        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public Connection getConnection(){

        return this.connection;
    }

}
