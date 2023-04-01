import java.io.*;  
import java.net.*;  
import java.util.*;

public class Client {
    public static String readFromBuffer(BufferedReader input) throws IOException {
        int charNum = input.read();
        String message = ""; 
        while (charNum != 10){
            //System.out.print((char) charNum);
            message += (char) charNum;
            charNum = input.read();
        }
        return message;
    }

    public static void main(String args[]) {
        try {
            Socket s = new Socket("localhost", 50000);  
            // Handshaking
            BufferedReader din = new BufferedReader(new InputStreamReader(s.getInputStream()));
            DataOutputStream dout = new DataOutputStream(new DataOutputStream(s.getOutputStream()));  
            
            // SEND HELO
            dout.write(("HELO\n").getBytes());
            dout.flush();  
            // RECEIVE OK
            System.out.println(readFromBuffer(din));
            // SEND AUTH
            String username = System.getProperty("user.name");
            dout.write(("AUTH " + username + "\n").getBytes());
            dout.flush();
            // RECEIVE OK 
            System.out.println(readFromBuffer(din));
            // SEND REDY 
            dout.write(("REDY\n").getBytes());
            dout.flush();
            // RECEIVE SOMETHING
            System.out.println(readFromBuffer(din));

            int coreNo = 1;
            int currentRAMSize = 1;
            int largestRAMSize = 0;
            
            // GET DATA
            dout.write(("GETS Capable " + coreNo + " 100 100\n").getBytes());
            dout.write(("OK\n").getBytes());
            String message = readFromBuffer(din);
            String[] sysInfoMessage = message.split(" ");

            // IDENTIFY LARGEST SERVER
            while (message.equals(".") != true) {
                dout.write(("OK\n").getBytes());
                message = readFromBuffer(din);
                dout.write(("OK\n").getBytes());

                if (message.equals(".") != true) 
                    sysInfoMessage = message.split(" ");
                
                if (sysInfoMessage.length != 1)
                    currentRAMSize = Integer.parseInt(sysInfoMessage[4]);
            
                if (currentRAMSize > largestRAMSize)
                    largestRAMSize = currentRAMSize;
                
                coreNo *= 2;
              
                dout.write(("GETS Capable " + coreNo + " 100 100\n").getBytes());
                dout.write(("OK\n").getBytes());
            }


            dout.write(("SCHD 0 " + sysInfoMessage[0] + " 0\n").getBytes());
        
            // SEND QUIT
            dout.write(("QUIT\n").getBytes());
            dout.flush();
            // RECEIVE QUIT
            readFromBuffer(din);

            dout.close();
            s.close();

        } catch(Exception e){
            System.out.println(e);
        }  
    }
}
