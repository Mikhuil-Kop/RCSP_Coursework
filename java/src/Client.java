import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

public class Client implements Runnable {
    private Socket clientSocket;
    private String language = "rus";

    Client(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            BufferedReader inStream = null;
            PrintWriter headerPrintWriter = null;
            BufferedWriter outDataStream = null;

            try {
                inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String httpRequest = inStream.readLine();
                String request = httpRequest.split(" ")[1].substring(1);
                System.out.println("Запрос: " + httpRequest);

                //Получаем ответ
                Response response = analiseRequest(request);

                //Отправляем ответ
                System.out.println("Ответ: " + response.getResponseFileData());

                headerPrintWriter = new PrintWriter(clientSocket.getOutputStream());
                outDataStream = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

                headerPrintWriter.println("HTTP/1.1 200 OK");
                headerPrintWriter.println("Server: Java HTTP Server : 1.0");
                headerPrintWriter.println("Date: " + new Date());
                headerPrintWriter.println("Content type:" + response.getContentType());
                headerPrintWriter.println("Accept-Language: *");
                headerPrintWriter.println();
                headerPrintWriter.flush();

                outDataStream.write(response.getResponseFile());
                outDataStream.flush();

                //Закрытие и защита
            } catch (Exception e) {
                System.err.println(e.toString());
            }finally {
                if (inStream != null)
                    inStream.close();
                if (headerPrintWriter != null)
                    headerPrintWriter.close();
                if (outDataStream != null)
                    outDataStream.close();
                Thread.currentThread().interrupt();
            }
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }

    private Response analiseRequest(String request) {
        RequestLine line = new RequestLine(request);

        if (line.peek().equals("api")) {//В случае, если получена команда от Ajax
            line.pop();

            return new Response("<html><body>a</body></html>", Server.contetnTypesMap.get("ajax"));
        } else {//В случае если запрос обычного типа

            //Получение языка
            if (Server.languages.contains(line.peek()))
                language = line.pop();

            String destination = line.pop();
            String realDestination = Server.pathsMap.getOrDefault(destination, Server.errorPath);
            String fileType = line.requestedFileType();

            if (fileType.equals("")) {//Запрос HTML
                return new Response(realDestination + "index.html", Server.contetnTypesMap.get("html"));
            } else //Запрос не HTML файла
                return new Response(realDestination + line.getLeftRequest(), Server.contetnTypesMap.get(fileType));
        }
    }

    private static class RequestLine {
        private String[] splitRequest;
        private int index;

        RequestLine(String request) {
            splitRequest = request.split("/");
            index = 0;
        }

        public String peek() {
            if (index >= splitRequest.length)
                return "";
            else
                return splitRequest[index];
        }

        public String pop() {
            index++;
            if (index > splitRequest.length)
                return "";
            else
                return splitRequest[index - 1];
        }

        public boolean isEmpty() {
            return index >= splitRequest.length;
        }

        public int popsLeft() {
            return splitRequest.length - index;
        }

        public String getLeftRequest() {
            StringBuilder req = new StringBuilder();
            for (int i = index; i < splitRequest.length; i++)
                req.append(splitRequest[i]).append("/");//Поменять для Windows;
            return req.toString();
        }

        public String requestedFileType() {
            String end = splitRequest[splitRequest.length - 1];
            var index = end.lastIndexOf('.');
            if (index < 0)
                return "";
            return end.substring(index);
        }
    }




    public void oldRun() {
        //Инициализация
        BufferedReader inStream;
        PrintWriter headerPrintWriter;
        BufferedWriter outDataStream;
        try {
            inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String request = inStream.readLine().split(" ")[1].substring(1);
            System.out.println("Запрос: " + request);
            //обрезать запрос типа '8080/about/'
            if (!request.equals("") && !request.contains("?") && !request.contains("css") && !request.contains("js"))
                request = request.substring(0, request.length() - 1);

            String currentPage = "";
            String contentType = "";
            String restResponse = "";
            boolean isRestUsed = false;

            //Форматирование запроса
            if(!request.contains("api")){
                if(request.contains(".css")) {
                    request = request.substring(request.indexOf("css"));
                    currentPage = getPage(request);
                    contentType = "Content-type: text/css";
                }
                else if(request.contains(".js")){
                    request = request.substring(request.indexOf("js"));
                    currentPage = getPage(request);
                    contentType = "Content-type: text/javascript";
                }
                else {
                    switch (request) {
                        case "about":
                            currentPage = getPage("about.html");
                            break;
                        case "task":
                            currentPage = getPage("task.html");
                            break;
                        case "table":
                            currentPage = getPage("table.html");
                            break;
                        default:
                            currentPage = getPage("index.html");
                            break;
                    }
                    contentType = "Content-type: text/html; charset=utf-8";
                }
            }
            else{
                contentType = "Content-type: application/json; charset=utf-8";
                //Rest rest = new Rest(request);
                //restResponse = rest.getResponse();
                isRestUsed = true;
            }

            //передача клиенту
            headerPrintWriter = new PrintWriter(clientSocket.getOutputStream());
            outDataStream = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            headerPrintWriter.println("HTTP/1.1 200 OK");
            headerPrintWriter.println("Server: Java HTTP Server : 1.0");
            headerPrintWriter.println("Date: " + new Date());
            headerPrintWriter.println(contentType);
            headerPrintWriter.println("Accept-Language: *");
            headerPrintWriter.println();
            headerPrintWriter.flush();

            System.out.println("Итоговый запрос: " + request);

            if(!isRestUsed){
                System.out.println("Отправлен: файл");
                outDataStream.write(currentPage);
                outDataStream.flush();
            }
            else{
                System.out.println("Отправлено: " + restResponse);
                outDataStream.write(restResponse);
                outDataStream.flush();
            }

            inStream.close();
            headerPrintWriter.close();
            outDataStream.close();
            Thread.currentThread().interrupt();

        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    public String getPage(String pageName) {
        String filePath =  "/Users/mikekopotov/Desktop/РКСП/PR8/src/public/" + pageName;
        String pageData = "";
        try {
            pageData = Files.readString(Paths.get(filePath));
        }catch (IOException e) {
            e.printStackTrace();
        }
        return pageData;
    }
}
