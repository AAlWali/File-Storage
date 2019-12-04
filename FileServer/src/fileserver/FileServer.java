/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fileserver;

/**
 *
 * @author Abdelrahman Al-Wali
 */
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.sql.*;
import java.util.regex.Pattern;
import org.springframework.security.crypto.bcrypt.BCrypt;

class ClientHandler implements Runnable {

    Socket s;
    private static int workload = 12;

    public ClientHandler(Socket s) {
        this.s = s;
    }

    @Override
    public void run() {
        try {
            //3.create I/O streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            int ID = 0;
            int flag = 0;
            String path = "Users\\";

            //////////////////////////
            //Connect to database
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/Distributed", "root", "fuckofdude1212");
            Statement stmt = con.createStatement();
            ResultSet rs = null;

            /////////////////////////
            //login or register
            while (true) {
                while (flag == 0) {
                    flag = 0;
                    dos.writeUTF("Please Enter \"login\" or \"register\": ");
                    dos.flush();
                    String option = dis.readUTF();
                    ////////////////////////
                    //login code
                    if (option.equals("login")) {
                        int lineNumber = 0;
                        dos.writeUTF("Please enter Email: ");
                        dos.flush();
                        String Email = "";
                        do {

                            Email = dis.readUTF();
                            //apply checks
                            rs = stmt.executeQuery("select Email from Login where Email = \"" + Email + "\"");
                            if (rs.next()) {
                                break;
                            } else {
                                dos.writeUTF("Email doesn't exist, Please enter valid Email: ");
                                dos.flush();
                                lineNumber = 0;
                            }
                        } while (lineNumber == 0);
                        //----------------------
                        //b. if correct accnum request password
                        dos.writeUTF("Correct Email,Please enter password: ");
                        dos.flush();
                        String password = dis.readUTF();
                        rs = stmt.executeQuery("select Password from Login where Email = \"" + Email + "\"");
                        String pass = "";
                        if (rs.next()) {
                            pass = rs.getString("Password");
                        }

                        //apply checks
                        while (!checkPassword(password, pass)) {
                            //apply checks
                            dos.writeUTF("Password Invalid, Please enter valid Password: ");
                            password = dis.readUTF();
                        }
                        //----------------------
                        //b. if correct password get id
                        rs = stmt.executeQuery("select ID from Login where Email = \"" + Email + "\"");
                        if (rs.next()) {
                            ID = rs.getInt("ID");
                        }

                        path = "Users\\" + ID + "\\home directory\\";
                        flag = 1;
                        ///////////////
                        //register code
                    } else if (option.equals("register")) {
                        int lineNumber = 0;
                        dos.writeUTF("Please enter Email: ");
                        dos.flush();
                        String Email;
                        do {
                            Email = dis.readUTF();
                            //apply checks
                            rs = stmt.executeQuery("select Email from Login where Email = \"" + Email + "\"");
                            if (rs.next()) {
                                dos.writeUTF("Email Exist, Please enter another Email: ");
                                dos.flush();
                                lineNumber = 1;
                            } else {
                                break;
                            }
                        } while (lineNumber != 0);
                        dos.writeUTF("Valid Email,Please enter password: ");
                        dos.flush();
                        String password = dis.readUTF();
                        String Hash = hashPassword(password);
                        // the mysql insert statement
                        String query = ("INSERT INTO Login (Email,Password)" + " values (?, ?)");

                        // create the mysql insert preparedstatement
                        PreparedStatement preparedStmt = con.prepareStatement(query);
                        preparedStmt.setString(1, Email);
                        preparedStmt.setString(2, Hash);

                        // execute the preparedstatement
                        preparedStmt.execute();

                        rs = stmt.executeQuery("select ID from Login where Email = \"" + Email + "\"");
                        if (rs.next()) {
                            ID = rs.getInt("ID");
                        }
                        path = "Users\\" + ID + "\\home directory\\";
                        new File(path).mkdirs();
                        flag = 1;
                    }
                }

                /////////////////////
                //Take operation type
                dos.writeUTF("Please enter desired operation: ");
                dos.flush();
                String operation = dis.readUTF();
                String[] op = operation.split(" ", 0);
                int size = op.length;
                ///////////////
                //make operation
                if (op[0].equals("cd")) {
                    if (size > 1) {
                        int count = operation.length() - operation.replace(".", "").length();
                        int FlagExist = 0;
                        if (count == 0) {
                            String FolderName = "";
                            for (int i = 1; i < size; i++) {
                                if (i == 1) {
                                    FolderName = FolderName + op[i];
                                } else {
                                    FolderName = FolderName + " " + op[i];
                                }
                            }
                            File f = new File(path + FolderName + "\\");
                            if (f.exists()) {
                                FlagExist = 1;
                            }
                            if (FlagExist == 1) {
                                path = path + FolderName + "\\";
                                dos.writeUTF("Successful operation, Another operation [y/n]?");
                                dos.flush();
                            } else if (FlagExist == 0) {
                                dos.writeUTF("Unsuccessful operation Path doesn't exist, Another operation [y/n]?");
                                dos.flush();
                            }
                        } else if (count > 0) {
                            String path1 = path.substring(path.indexOf('h'));
                            String temp = path1;
                            int count1 = temp.length() - temp.replace("\\", "").length();
                            String temp2 = path1.replace("\\", ".");
                            if (count1 - 1 >= count / 2) {
                                String[] temp1 = temp2.split(Pattern.quote("."));
                                temp = "";
                                for(int i = 0;i<count1 - count/2;i++)
                                {
                                    temp = temp + temp1[i] + "\\";
                                }
                                path = "Users\\" + ID + "\\" + temp;
                                dos.writeUTF("Successful operation, Another operation [y/n]?");
                                dos.flush();
                            } else {
                                dos.writeUTF("Unsuccessful operation user can't change directory lower than home directory, Another operation [y/n]?");
                                dos.flush();
                            }
                        }
                    } else {
                        dos.writeUTF("Unsuccessful operation cd must take one parameter, Another operation [y/n]?");
                        dos.flush();
                    }
                } else if (op[0].equals("ls")) {
                    if (size == 1) {
                        File folder = new File(path);

                        String[] files = folder.list();
                        String names = "";
                        for (String file : files) {
                            names = names + file + "  ";
                        }
                        dos.writeUTF(names + "\nSuccessful operation, Another operation [y/n]?");
                        dos.flush();
                    } else {
                        dos.writeUTF("Unsuccessful operation ls doesn't take any parameter, Another operation [y/n]?");
                        dos.flush();
                    }
                } else if (op[0].equals("pwd")) {
                    if (size == 1) {
                        String path1 = path.substring(path.indexOf('h'));
                        dos.writeUTF(path1 + "\nSuccessful operation, Another operation [y/n]?");
                        dos.flush();
                    } else {
                        dos.writeUTF("Unsuccessful operation pwd doesn't take any parameter, Another operation [y/n]?");
                        dos.flush();
                    }
                } else if (op[0].equals("mkdir")) {
                    if (size > 1) {
                        int FlagExist = 0;
                        String FolderName = "";
                        for (int i = 1; i < size; i++) {
                            if (i == 1) {
                                FolderName = FolderName + op[i];
                            } else {
                                FolderName = FolderName + " " + op[i];
                            }
                        }
                        do {
                            FlagExist = 0;
                            File folder = new File(path);
                            String[] files = folder.list();
                            for (String file : files) {
                                if (FolderName.equals(file)) {
                                    FlagExist = 1;
                                    break;
                                }
                            }
                            if (FlagExist == 0) {
                                new File(path + "\\" + FolderName).mkdirs();
                                dos.writeUTF("Successful operation, Another operation [y/n]?");
                                dos.flush();
                            } else {
                                dos.writeUTF("Directory exist,Please enter different name: ");
                                dos.flush();
                                String name = dis.readUTF();
                                String[] op1 = name.split(" ", 0);
                                FolderName = "";
                                for (int i = 1; i < op1.length; i++) {
                                    if (i == 1) {
                                        FolderName = FolderName + op1[i];
                                    } else {
                                        FolderName = FolderName + " " + op1[i];
                                    }
                                }
                            }

                        } while (FlagExist == 1);
                    } else {

                        dos.writeUTF("Unsuccessful operation mkdir must take only one parameter, Another operation [y/n]?");
                        dos.flush();
                    }

                } else if (op[0].equals("rmdir")) {
                    if (size > 1) {
                        int FlagExist = 0;
                        String FolderName = "";
                        for (int i = 1; i < size; i++) {
                            if (i == 1) {
                                FolderName = FolderName + op[i];
                            } else {
                                FolderName = FolderName + " " + op[i];
                            }
                        }
                        File folder = new File(path);
                        String[] files = folder.list();
                        for (String file : files) {
                            if (FolderName.equals(file)) {
                                FlagExist = 1;
                                break;
                            }
                        }
                        if (FlagExist == 1) {
                            String pa = path + "\\" + FolderName;
                            deleteDirectoryStream(Paths.get(pa));
                            dos.writeUTF("Successful operation, Another operation [y/n]?");
                            dos.flush();
                        } else {
                            dos.writeUTF("Directory doesn't exist, Another operation [y/n]?");
                            dos.flush();
                        }

                    } else {

                        dos.writeUTF("Unsuccessful operation rmdir must take only one parameter, Another operation [y/n]?");
                        dos.flush();
                    }
                } else if (op[0].equals("mv")) {
                    if (size > 2) {
                        int FlagExist = 0;
                        int FlagExist2 = 0;
                        int FlagExist3 = 0;
                        do {
                            int Last = 0;
                            FlagExist = 0;
                            FlagExist2 = 0;
                            FlagExist3 = 0;
                            String FileName = "";
                            String FolderName = "";
                            for (int i = 1; i < size; i++) {
                                if (i == 1) {
                                    FileName = FileName + op[i];
                                    if (FileName.contains(".")) {
                                        Last = i;
                                        break;
                                    }
                                } else {
                                    FileName = FileName + " " + op[i];
                                    if (FileName.contains(".")) {
                                        Last = i;
                                        break;
                                    }
                                }
                            }
                            File folder = new File(path);
                            String[] files = folder.list();
                            for (String file : files) {
                                if (FileName.equals(file)) {
                                    FlagExist = 1;
                                    break;
                                }
                            }
                            for (int i = Last + 1; i < size; i++) {
                                if (i == Last + 1) {
                                    FolderName = FolderName + op[i];
                                } else {
                                    FolderName = FolderName + " " + op[i];
                                }
                            }
                            File f = new File("Users\\" + ID + "\\" + FolderName);
                            if (f.exists()) {
                                FlagExist2 = 1;
                            }
                            if (FlagExist2 == 1) {
                                File folder1 = new File("Users\\" + ID + "\\" + FolderName);
                                String[] filess = folder1.list();
                                for (String file : filess) {
                                    if (FileName.equals(file)) {
                                        FlagExist3 = 1;
                                        break;
                                    }
                                }
                            }
                            if (FlagExist == 1 && FlagExist2 == 1 && FlagExist3 == 0) {
                                File sourceFile = new File(path + "\\" + FileName);
                                File targetFile = new File("Users\\" + ID + "\\" + FolderName + "\\" + FileName);
                                Files.copy(sourceFile.toPath(), targetFile.toPath());
                                File file = new File(path + "\\" + FileName);
                                file.delete();
                                dos.writeUTF("Successful operation, Another operation [y/n]?");
                                dos.flush();
                            } else if (FlagExist == 0) {
                                dos.writeUTF("File doesn't exist, Please enter command again with different name: ");
                                dos.flush();
                                operation = dis.readUTF();
                                op = operation.split(" ", 0);
                                size = op.length;
                            } else if (FlagExist2 == 0) {
                                dos.writeUTF("Directory doesn't exist, Please enter command again with different new name: ");
                                dos.flush();
                                operation = dis.readUTF();
                                op = operation.split(" ", 0);
                                size = op.length;
                            } else if (FlagExist3 == 1) {
                                dos.writeUTF("New name does exist in the new Directory, Please enter command again with different new name: ");
                                dos.flush();
                                operation = dis.readUTF();
                                op = operation.split(" ", 0);
                                size = op.length;
                            }

                        } while (FlagExist == 0 || FlagExist2 == 0 || FlagExist3 == 1);
                    } else {

                        dos.writeUTF("Unsuccessful operation rnm must take more than two parameter, Another operation [y/n]?");
                        dos.flush();
                    }
                } else if (op[0].equals("cp")) {
                    if (size > 2) {
                        int FlagExist = 0;
                        int FlagExist2 = 0;
                        int FlagExist3 = 0;
                        do {
                            int Last = 0;
                            FlagExist = 0;
                            FlagExist2 = 0;
                            FlagExist3 = 0;
                            String FileName = "";
                            String FolderName = "";
                            for (int i = 1; i < size; i++) {
                                if (i == 1) {
                                    FileName = FileName + op[i];
                                    if (FileName.contains(".")) {
                                        Last = i;
                                        break;
                                    }
                                } else {
                                    FileName = FileName + " " + op[i];
                                    if (FileName.contains(".")) {
                                        Last = i;
                                        break;
                                    }
                                }
                            }
                            File folder = new File(path);
                            String[] files = folder.list();
                            for (String file : files) {
                                if (FileName.equals(file)) {
                                    FlagExist = 1;
                                    break;
                                }
                            }
                            for (int i = Last + 1; i < size; i++) {
                                if (i == Last + 1) {
                                    FolderName = FolderName + op[i];
                                } else {
                                    FolderName = FolderName + " " + op[i];
                                }
                            }
                            File f = new File("Users\\" + ID + "\\" + FolderName);
                            if (f.exists()) {
                                FlagExist2 = 1;
                            }
                            if (FlagExist2 == 1) {
                                File folder1 = new File("Users\\" + ID + "\\" + FolderName);
                                String[] filess = folder1.list();
                                for (String file : filess) {
                                    if (FileName.equals(file)) {
                                        FlagExist3 = 1;
                                        break;
                                    }
                                }
                            }
                            if (FlagExist == 1 && FlagExist2 == 1 && FlagExist3 == 0) {
                                File sourceFile = new File(path + "\\" + FileName);
                                File targetFile = new File("Users\\" + ID + "\\" + FolderName + "\\" + FileName);
                                Files.copy(sourceFile.toPath(), targetFile.toPath());
                                dos.writeUTF("Successful operation, Another operation [y/n]?");
                                dos.flush();
                            } else if (FlagExist == 0) {
                                dos.writeUTF("File doesn't exist, Please enter command again with different name: ");
                                dos.flush();
                                operation = dis.readUTF();
                                op = operation.split(" ", 0);
                                size = op.length;
                            } else if (FlagExist2 == 0) {
                                dos.writeUTF("Directory doesn't exist, Please enter command again with different new name: ");
                                dos.flush();
                                operation = dis.readUTF();
                                op = operation.split(" ", 0);
                                size = op.length;
                            } else if (FlagExist3 == 1) {
                                dos.writeUTF("New name does exist in the new Directory, Please enter command again with different new name: ");
                                dos.flush();
                                operation = dis.readUTF();
                                op = operation.split(" ", 0);
                                size = op.length;
                            }

                        } while (FlagExist == 0 || FlagExist2 == 0 || FlagExist3 == 1);
                    } else {

                        dos.writeUTF("Unsuccessful operation rnm must take more than two parameter, Another operation [y/n]?");
                        dos.flush();
                    }
                } else if (op[0].equals("rnm")) {
                    if (size > 2) {
                        int FlagExist = 0;
                        int FlagExist2 = 0;
                        do {
                            int Last = 0;
                            FlagExist = 0;
                            FlagExist2 = 0;
                            String FileName = "";
                            String FileName2 = "";
                            for (int i = 1; i < size; i++) {
                                if (i == 1) {
                                    FileName = FileName + op[i];
                                    if (FileName.contains(".")) {
                                        Last = i;
                                        break;
                                    }
                                } else {
                                    FileName = FileName + " " + op[i];
                                    if (FileName.contains(".")) {
                                        Last = i;
                                        break;
                                    }
                                }
                            }
                            for (int i = Last + 1; i < size; i++) {
                                if (i == Last + 1) {
                                    FileName2 = FileName2 + op[i];
                                    if (FileName2.contains(".")) {
                                        break;
                                    }
                                } else {
                                    FileName2 = FileName2 + " " + op[i];
                                    if (FileName2.contains(".")) {
                                        break;
                                    }
                                }
                            }
                            File folder = new File(path);
                            String[] files = folder.list();
                            for (String file : files) {
                                if (FileName.equals(file)) {
                                    FlagExist = 1;
                                    break;
                                }
                            }
                            for (String file : files) {
                                if (FileName2.equals(file)) {
                                    FlagExist2 = 1;
                                    break;
                                }
                            }
                            if (FlagExist == 1 && FlagExist2 == 0) {
                                File file1 = new File(path + "\\" + FileName);
                                File file2 = new File(path + "\\" + FileName2);
                                boolean success = file1.renameTo(file2);
                                dos.writeUTF("Successful operation, Another operation [y/n]?");
                                dos.flush();
                            } else if (FlagExist == 0) {
                                dos.writeUTF("File doesn't exist, Please enter command again with different name: ");
                                dos.flush();
                                operation = dis.readUTF();
                                op = operation.split(" ", 0);
                                size = op.length;
                            } else if (FlagExist2 == 1) {
                                dos.writeUTF("New name does exist, Please enter command again with different new name: ");
                                dos.flush();
                                operation = dis.readUTF();
                                op = operation.split(" ", 0);
                                size = op.length;
                            }

                        } while (FlagExist == 0 || FlagExist2 == 1);
                    } else {

                        dos.writeUTF("Unsuccessful operation rnm must take more than two parameter, Another operation [y/n]?");
                        dos.flush();
                    }
                } else if (op[0].equals("rm")) {
                    if (size > 1) {
                        int FlagExist = 0;
                        String FileName = "";
                        for (int i = 1; i < size; i++) {
                            if (i == 1) {
                                FileName = FileName + op[i];
                            } else {
                                FileName = FileName + " " + op[i];
                            }
                        }
                        File folder = new File(path);
                        String[] files = folder.list();
                        for (String file : files) {
                            if (FileName.equals(file)) {
                                FlagExist = 1;
                                break;
                            }
                        }
                        if (FlagExist == 1) {
                            File file = new File(path + "\\" + FileName);
                            file.delete();
                            dos.writeUTF("Successful operation, Another operation [y/n]?");
                            dos.flush();
                        } else {
                            dos.writeUTF("File doesn't exist, Another operation [y/n]?");
                            dos.flush();
                        }

                    } else {

                        dos.writeUTF("Unsuccessful operation rm must take only one parameter, Another operation [y/n]?");
                        dos.flush();
                    }
                } else if (op[0].equals("upload")) {

                } else if (op[0].equals("download")) {
                    if (size > 2) {
                        int FlagExist = 0;
                        do {
                            int Last = 0;
                            FlagExist = 0;
                            String FileName = "";
                            String FolderName = "";
                            for (int i = 1; i < size; i++) {
                                if (i == 1) {
                                    FileName = FileName + op[i];
                                    if (FileName.contains(".")) {
                                        Last = i;
                                        break;
                                    }
                                } else {
                                    FileName = FileName + " " + op[i];
                                    if (FileName.contains(".")) {
                                        Last = i;
                                        break;
                                    }
                                }
                            }
                            File folder = new File(path);
                            String[] files = folder.list();
                            for (String file : files) {
                                if (FileName.equals(file)) {
                                    FlagExist = 1;
                                    break;
                                }
                            }
                            for (int i = Last + 1; i < size; i++) {
                                if (i == Last + 1) {
                                    FolderName = FolderName + op[i];
                                } else {
                                    FolderName = FolderName + " " + op[i];
                                }
                            }
                            if (FlagExist == 1) {
                                File myFile = new File(FileName);
                                byte[] mybytearray = new byte[(int) myFile.length()];
                                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
                                bis.read(mybytearray, 0, mybytearray.length);
                                OutputStream os = s.getOutputStream();
                                os.write(mybytearray, 0, mybytearray.length);
                                os.flush();
                                dos.writeUTF("Successful operation, Another operation [y/n]?");
                                dos.flush();
                            } else {
                                dos.writeUTF("File doesn't exist, Please enter command again with different name: ");
                                dos.flush();
                                operation = dis.readUTF();
                                op = operation.split(" ", 0);
                                size = op.length;
                            }

                        } while (FlagExist == 0);
                    } else {

                        dos.writeUTF("Unsuccessful operation rnm must take more than two parameter, Another operation [y/n]?");
                        dos.flush();
                    }
                } else {
                    dos.writeUTF("Operation doesn't exist, Another operation [y/n]?");
                    dos.flush();
                }
                String usr_choice = dis.readUTF();
                //apply checks
                if (usr_choice.equals("n")) {
                    dos.writeUTF("bye");
                    dos.flush();
                    break;
                }

            }

            //5.close connection
            dis.close();

            dos.close();

            s.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static String hashPassword(String password_plaintext) {
        String salt = BCrypt.gensalt(workload);
        String hashed_password = BCrypt.hashpw(password_plaintext, salt);

        return (hashed_password);
    }

    public static boolean checkPassword(String password_plaintext, String stored_hash) {
        boolean password_verified = false;

        password_verified = BCrypt.checkpw(password_plaintext, stored_hash);

        return (password_verified);
    }

    void deleteDirectoryStream(Path path) throws IOException {
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}

public class FileServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            //1.open server socket
            ServerSocket sv = new ServerSocket(1234);
            System.out.println("Server Running...");
            while (true) {
                //2.accept connection
                Socket s = sv.accept();
                System.out.println("Client Accepted...");
                //3. open thread for this client (s)
                ClientHandler ch = new ClientHandler(s);
                Thread t = new Thread(ch);
                t.start();

            }

            //6.close server
            //sv.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

}
