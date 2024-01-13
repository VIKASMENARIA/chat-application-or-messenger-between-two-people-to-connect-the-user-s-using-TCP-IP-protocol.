package CN;
import java.io.*;
import java.net.*;

public class FileTransferServer {
    private static final int PORT = 8085;

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection from " + clientSocket.getInetAddress().getHostAddress());
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            System.out.println("Error starting the server: " + e.getMessage());
        }
    }

    private static class ClientHandler extends Thread {
        private Socket clientSocket;

        ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

                writer.println("Welcome to the file server. Enter 'upload' or 'download'.");

                String request = reader.readLine();
                if (request != null && request.equalsIgnoreCase("upload")) {
                    handleFileUpload(reader);
                } else if (request != null && request.equalsIgnoreCase("download")) {
                    handleFileDownload(reader, writer);
                } else {
                    writer.println("Invalid request. Connection closed.");
                }
            } catch (IOException e) {
                System.out.println("Error handling client: " + e.getMessage());
            }
        }

        private void handleFileUpload(BufferedReader reader) throws IOException {
            System.out.println("Client wants to upload a file.");
            String fileName = reader.readLine();

            // Receive the file content from the client and save it to a file
            try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = clientSocket.getInputStream().read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
                fileOutputStream.flush();
                System.out.println("File '" + fileName + "' uploaded successfully.");
            } catch (IOException e) {
                System.out.println("Error handling file upload: " + e.getMessage());
            }
        }

        private void handleFileDownload(BufferedReader reader, PrintWriter writer) throws IOException {
            System.out.println("Client wants to download a file.");
            String fileName = reader.readLine();

            File file = new File(fileName);
            if (!file.exists()) {
                writer.println("File not found.");
                return;
            }

            // Send the file content to the client
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    clientSocket.getOutputStream().write(buffer, 0, bytesRead);
                }
                clientSocket.getOutputStream().flush();
                System.out.println("File '" + fileName + "' sent successfully.");
            } catch (IOException e) {
                System.out.println("Error handling file download: " + e.getMessage());
            }
        }
    }
}