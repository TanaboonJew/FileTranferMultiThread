import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer {
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                new FileTransferHandler(socket).start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

class FileTransferHandler extends Thread {
    private Socket socket;

    public FileTransferHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

            String command = dis.readUTF();
            if (command.equals("UPLOAD")) {
                receiveFile(dis);
            } else if (command.equals("DOWNLOAD")) {
                sendFile(dis, dos);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void receiveFile(DataInputStream dis) throws IOException {
        String fileName = dis.readUTF();
        long fileSize = dis.readLong();
        try (FileOutputStream fos = new FileOutputStream("server_files/" + fileName)) {
            byte[] buffer = new byte[4096];
            int read;
            long remaining = fileSize;
            while ((read = dis.read(buffer, 0, Math.min(buffer.length, (int)remaining))) > 0) {
                fos.write(buffer, 0, read);
                remaining -= read;
            }
            System.out.println("File " + fileName + " received.");
        }
    }

    private void sendFile(DataInputStream dis, DataOutputStream dos) throws IOException {
        String fileName = dis.readUTF();
        File file = new File("server_files/" + fileName);
        if (file.exists()) {
            dos.writeUTF("OK");
            dos.writeLong(file.length());
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = fis.read(buffer)) > 0) {
                    dos.write(buffer, 0, read);
                }
            }
            System.out.println("File " + fileName + " sent.");
        } else {
            dos.writeUTF("File Not Found");
        }
    }
}