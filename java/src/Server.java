import java.io.IOException;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public class Server {
    //Статические параметры сервера
    public static final Map<String, String> pathsMap = new HashMap<>();
    public static final Map<String, String> contetnTypesMap = new HashMap<>();
    public static final Set<String> languages = Set.of("rus","eng");
    public static final String errorPath = "../error/";//(Поменять для Windows)

    //Парамеры базы данных
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/DeltaTime";
    private static final String USER = "postgres";
    private static final String PASS = "walrus2000";

    public static void main(String[] args) {
        //connectToDB();

        //Инициализация. (Поменять для Windows)
        pathsMap.put("","../home/");
        pathsMap.put("departments","../departments/");
        pathsMap.put("characters","../characters/");
        pathsMap.put("characters-list","../characters-list/");
        pathsMap.put("persons","../persons/");
        pathsMap.put("story","../story/");

        contetnTypesMap.put("ajax","application/json; charset=utf-8");
        contetnTypesMap.put("html","text/html; charset=utf-8");
        contetnTypesMap.put("js","text/javascript;");
        contetnTypesMap.put("css","text/css;");



        //Получение сигналов от клиентов.
        try {
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("Сервер запущен на порту 8080!");
            while (true) {
                Client client = new Client(serverSocket.accept());

                //Thread thread = new Thread(client);
                //thread.start();
                client.run();
            }

        } catch (IOException exeption){
            System.err.println(exeption.toString());
        }
    }

    private static void connectToDB(){
        System.out.println("Testing connection to PostgreSQL JDBC");

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found. Include it in your library path ");
            e.printStackTrace();
            return;
        }

        System.out.println("PostgreSQL JDBC Driver successfully connected");
        Connection connection = null;

        try {
            connection = DriverManager
                    .getConnection(DB_URL, USER, PASS);

        } catch (SQLException e) {
            System.out.println("Connection Failed");
            e.printStackTrace();
            return;
        }

        if (connection != null) {
            System.out.println("You successfully connected to database now");
        } else {
            System.out.println("Failed to make connection to database");
        }
    }
}
