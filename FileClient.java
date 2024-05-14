import java.io.*;
import java.net.Socket;

public class FileClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connected to the server");

            System.out.print("Enter command (UPLOAD/DOWNLOAD): ");
            String command = consoleReader.readLine().toUpperCase();
            dos.writeUTF(command);

            if (command.equals("UPLOAD")) {
                uploadFile(consoleReader, dos);
            } else if (command.equals("DOWNLOAD")) {
                downloadFile(consoleReader, dis, dos);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void uploadFile(BufferedReader consoleReader, DataOutputStream dos) throws IOException {
        System.out.print("Enter file path to upload: ");
        String filePath = consoleReader.readLine();
        File file = new File(filePath);

        if (file.exists()) {
            dos.writeUTF(file.getName());
            dos.writeLong(file.length());

            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = fis.read(buffer)) > 0) {
                    dos.write(buffer, 0, read);
                }
                System.out.println("File uploaded successfully.");
            }
        } else {
            System.out.println("File does not exist.");
        }
    }

    private static void downloadFile(BufferedReader consoleReader, DataInputStream dis, DataOutputStream dos) throws IOException {
        System.out.print("Enter file name to download: ");
        String fileName = consoleReader.readLine();
        dos.writeUTF(fileName);

        String response = dis.readUTF();
        if (response.equals("OK")) {
            long fileSize = dis.readLong();
            File dir = new File("client_files");
            if (!dir.exists()) {
                dir.mkdir();
            }
            try (FileOutputStream fos = new FileOutputStream("client_files/" + fileName)) {
                byte[] buffer = new byte[4096];
                int read;
                long remaining = fileSize;
                while ((read = dis.read(buffer, 0, Math.min(buffer.length, (int)remaining))) > 0) {
                    fos.write(buffer, 0, read);
                    remaining -= read;
                }
                System.out.println("File downloaded successfully.");
            }
        } else {
            System.out.println("File not found on server.");
        }
    }
}