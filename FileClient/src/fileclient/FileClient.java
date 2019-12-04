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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;


public class FileClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Please enter server IP: ");
        String IP = sc.nextLine();
        try
        {
            //1.create socket and connect to the server
            //with IP:127.0.0.1(localhost)
            //and with portnumber: 1234
            Socket s = new Socket(IP, 1234);
            //2. Create I/O streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            
            //3.perform IO with server 
            while (true)
            {
                //a. receive server command & print to user
                String srvr_msg = dis.readUTF();                
                if(srvr_msg.equals("bye"))
                {
                    System.out.println("Session ended");
                    break;
                }
                System.out.println(srvr_msg);
                
                //b. take command from usr and send to the server
                String usr_msg = sc.nextLine();
                dos.writeUTF(usr_msg);
                dos.flush();
            }
            //4.close connections
            dis.close();
            dos.close();
            s.close();
            
        } 
        catch (IOException ex)
        {
            System.out.println(ex.getMessage());
        }
    }
    
}
