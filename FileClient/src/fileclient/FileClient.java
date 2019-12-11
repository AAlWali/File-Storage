/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fileclient;

/**
 *
 * @author Abdelrahman Al-Wali
 */
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import jdk.nashorn.internal.runtime.JSType;

public class FileClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Please enter server IP: ");
        String IP = sc.nextLine();
        try {
            //1.create socket and connect to the server
            //with IP:127.0.0.1(localhost)
            //and with portnumber: 1234
            Socket s = new Socket(IP, 1234);
            //2. Create I/O streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            BufferedOutputStream bos;
            InputStream in = s.getInputStream();
            //3.perform IO with server 
            while (true) {
                //a. receive server command & print to user
                String srvr_msg = dis.readUTF();
                if (srvr_msg.equals("bye")) {
                    System.out.println("Session ended");
                    break;
                }
                System.out.println(srvr_msg);

                //b. take command from usr and send to the server
                String usr_msg = sc.nextLine();
                dos.writeUTF(usr_msg);
                dos.flush();
                String[] op = usr_msg.split(" ", 0);
                int sizee = op.length;
                if (op[0].equals("upload")) {
                    if (sizee > 1) {
                        int FlagExist = 0, FlagExist2 = 0;
                        int Last = 0;
                        String FileName = "";
                        String FolderName = "";
                        for (int i = 1; i < sizee; i++) {
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
                        FlagExist = dis.readInt();
                        for (int i = Last + 1; i < sizee; i++) {
                            if (i == Last + 1) {
                                FolderName = FolderName + op[i];
                            } else {
                                FolderName = FolderName + " " + op[i];
                            }
                        }
                        File f = new File(FolderName);
                        if (f.exists()) {
                            FlagExist2 = 1;
                        }
                        if (FlagExist2 == 1) {
                            FlagExist2 = 0;
                            File folder = new File(FolderName + "\\");
                            String[] files = folder.list();
                            for (String file : files) {
                                if (FileName.equals(file)) {
                                    FlagExist2 = 1;
                                    break;
                                }
                            }
                        }
                        dos.writeInt(FlagExist2);
                        dos.flush();
                        File file = new File(FolderName + "\\" + FileName);
                        long bytes = file.length();
                        dos.writeLong(bytes);
                        dos.flush();
                        if (FlagExist == 0 && FlagExist2 == 1) {
                            byte[] b = new byte[JSType.toInt32(bytes) + 10000];
                            FileInputStream fr = new FileInputStream(file);
                            fr.read(b, 0, b.length);
                            OutputStream os = s.getOutputStream();
                            os.write(b, 0, b.length);
                            os.flush();
                        } else if (FlagExist == 1) {
                        } else if (FlagExist2 == 0) {
                        }
                    } else {
                    }
                } else if (op[0].equals("download")) {
                    if (sizee > 2) {
                        int FlagExist = 0, FlagExist2 = 0;
                        int Last = 0;
                        String FileName = "";
                        String FolderName = "";
                        for (int i = 1; i < sizee; i++) {
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
                        FlagExist = dis.readInt();
                        for (int i = Last + 1; i < sizee; i++) {
                            if (i == Last + 1) {
                                FolderName = FolderName + op[i];
                            } else {
                                FolderName = FolderName + " " + op[i];
                            }
                        }
                        File f = new File(FolderName);
                        if (f.exists()) {
                            FlagExist2 = 1;
                        }
                        dos.writeInt(FlagExist2);
                        dos.flush();
                        long Bytes = dis.readLong();
                        int bytess = JSType.toInt32(Bytes) + 10000;
                        if (FlagExist == 1 && FlagExist2 == 1) {
                            byte[] b = new byte[bytess];
                            InputStream is = s.getInputStream();
                            FileOutputStream fr = new FileOutputStream(FolderName + "\\" + FileName);
                            is.read(b, 0, b.length);
                            fr.write(b, 0, b.length);
                        } else if (FlagExist == 0) {
                        } else if (FlagExist2 == 0) {
                        }
                    } else {
                    }
                }
            }
            //4.close connections
            dis.close();
            dos.close();
            s.close();

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

}
