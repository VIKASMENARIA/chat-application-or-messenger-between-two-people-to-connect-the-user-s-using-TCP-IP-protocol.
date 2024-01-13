package ROOT;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class FileTransferClient extends JFrame {

    private JTextField fileNameField;
    private JTextArea logArea;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private JButton viewFileButton;
    private JTextArea fileContentArea;

    private static final String SERVER_IP = "Localhost"; // Replace with the server IP
    private static final int SERVER_PORT = 8085;

    public FileTransferClient() {
        setTitle("File Transfer Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        fileNameField = new JTextField();
        fileNameField.setColumns(40);
        JButton uploadButton = new JButton("Upload");
        JButton downloadButton = new JButton("Download");
        

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("File Name: "));
        topPanel.add(fileNameField);
        topPanel.add(uploadButton);
        topPanel.add(downloadButton);
        add(topPanel, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);

        uploadButton.addActionListener(e -> uploadFile());
        downloadButton.addActionListener(e -> downloadFile());
       
        
        viewFileButton = new JButton("View File Content");
        fileContentArea = new JTextArea();
        fileContentArea.setEditable(false);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(viewFileButton, BorderLayout.NORTH);
        bottomPanel.add(new JScrollPane(fileContentArea), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        viewFileButton.addActionListener(e -> viewFileContent());
        
    }
    private void viewFileContent() {
        String fileName = fileNameField.getText();
        if (fileName.isEmpty()) {
            logMessage("Please enter a file name.");
            return;
        }

        try {
            // Read the file content from the downloaded file
            File file = new File(fileName);
            if (!file.exists()) {
                logMessage("File not found.");
                return;
            }

            StringBuilder content = new StringBuilder();
            try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = fileReader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            } catch (IOException ex) {
                logMessage("Error reading file: " + ex.getMessage());
                return;
            }

            // Display the file content in the text area
            fileContentArea.setText(content.toString());
            logMessage("File content displayed for '" + fileName + "'.");
        } catch (Exception ex) {
            logMessage("Error viewing file content: " + ex.getMessage());
        }
    }

    private void uploadFile() {
        String fileName = fileNameField.getText();
        if (fileName.isEmpty()) {
            logMessage("Please enter a file name.");
            return;
        }

        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            writer.println("upload");
            writer.println(fileName);

            File file = new File(fileName);
            if (!file.exists()) {
                logMessage("File not found.");
                return;
            }

            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                OutputStream outputStream = socket.getOutputStream();
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
                logMessage("File '" + fileName + "' uploaded successfully.");
            } catch (IOException e) {
                logMessage("Error uploading file: " + e.getMessage());
            }

            reader.close();
            writer.close();
            socket.close();
        } catch (IOException e) {
            logMessage("Error connecting to the server: " + e.getMessage());
        }
    }

    	private void downloadFile() {
    	    String fileName = fileNameField.getText();
    	    if (fileName.isEmpty()) {
    	        logMessage("Please enter a file name.");
    	        return;
    	    }

    	    try {
    	        socket = new Socket(SERVER_IP, SERVER_PORT);
    	        writer = new PrintWriter(socket.getOutputStream(), true);
    	        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    	        writer.println("download");
    	        writer.println(fileName);

    	        File file = new File(fileName);
    	        FileOutputStream fileOutputStream = new FileOutputStream(file);

    	        byte[] buffer = new byte[8192];
    	        int bytesRead;
    	        InputStream inputStream = socket.getInputStream();
    	        bytesRead = inputStream.read(buffer);

    	        // If the file doesn't exist or the first read returned -1 (empty file), log an error
    	        if (bytesRead == -1) {
    	            logMessage("File '" + fileName + "' not found on the server.");
    	            file.delete(); // Delete the empty file created during FileOutputStream initialization
    	        } else {
    	            while (bytesRead != -1) {
    	                fileOutputStream.write(buffer, 0, bytesRead);
    	                bytesRead = inputStream.read(buffer);
    	            }
    	            fileOutputStream.close();
    	            logMessage("File '" + fileName + "' downloaded successfully.");
    	        }

    	        reader.close();
    	        writer.close();
    	        socket.close();
    	    } catch (IOException e) {
    	        logMessage("Error downloading file: " + e.getMessage());
    	    }
    	}


    private void logMessage(String message) {
        logArea.append(message + "\n");
    }

    public static void main(String[] args) throws UnknownHostException, IOException {
    	Socket socket = new Socket("Localhost",8085);
        SwingUtilities.invokeLater(() -> {
            FileTransferClient client = new FileTransferClient();
            client.setVisible(true);
        });
    }
}