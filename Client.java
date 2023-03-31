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

            /*
            // GET SERVER INFO
            dout.write(("GETS ALL\n").getBytes());

            String message = readFromBuffer(din);
            System.out.println(message);
            ArrayList<String> array = new ArrayList<>();

            // READ SERVER INFO
            while (message.equals(".") != true){
                dout.write(("OK\n").getBytes());
                array.add(message);
                message = readFromBuffer(din);
                //System.out.println(message);
            }

            System.out.println(array.toString());
            */

            int coreNo = 1;
            int currentRAMSize = 1;
            int largestRAMSize = currentRAMSize;
            
            // GET DATA
            dout.write(("GETS Capable " + coreNo + " 100 100\n").getBytes());
            dout.write(("OK\n").getBytes());
            String message = readFromBuffer(din);
            String[] sysInfoMessage = message.split(" ");

        
       
            
            // while doesn't == ., increment coreNo, observe response if > largest etc
            while (message.equals(".") != true) {
                dout.write(("OK\n").getBytes());
                message = readFromBuffer(din);
                sysInfoMessage = message.split(" ");

                if (sysInfoMessage.length != 1)
                    currentRAMSize = Integer.parseInt(sysInfoMessage[4]);
                

                if (currentRAMSize > largestRAMSize){
                    largestRAMSize = currentRAMSize;
                } 

                coreNo *= 2;
                dout.write(("GETS Capable " + coreNo + " 100 100\n").getBytes());
            }
            System.out.println(sysInfoMessage[0]);
            System.out.println(largestRAMSize);


            /*
            dout.write(("OK\n").getBytes());

            String[] sysInfoMessage = message.split(" ");
            List<String> tempArray = Arrays.asList(sysInfoMessage);
            ArrayList<String> sysInfoMessageArray = new ArrayList<>(tempArray);

            currentRAMSize = Integer.parseInt(sysInfoMessageArray.get(4));
            largestRAMSize = currentRAMSize; 



            System.out.println(currentRAMSize);
            dout.write(("OK\n").getBytes());
            */


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
