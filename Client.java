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

    public static String handShake(BufferedReader din, DataOutputStream dout) throws IOException {
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
            return (readFromBuffer(din));
    }





    public static void main(String args[]) {
        try {
            Socket s = new Socket("localhost", 50000);  
            // Handshaking
            BufferedReader din = new BufferedReader(new InputStreamReader(s.getInputStream()));
            DataOutputStream dout = new DataOutputStream(new DataOutputStream(s.getOutputStream()));  
            handShake(din, dout);

            String jobMessage = "";
            String message = "";

            int currentCoreSize = 1;
            int largestCoreSize = 0;

            dout.write(("REDY\n").getBytes());
            jobMessage = readFromBuffer(din); // Receive JOB
            String[] test = jobMessage.split(" ");
            int jobID = Integer.parseInt(test[2]);
            
            // IDENTIFY LARGEST SERVER
            dout.write(("GETS All\n").getBytes());
            message = readFromBuffer(din); // Receive DATA nRecs recSize 
            System.out.println(message);
            dout.write(("OK\n").getBytes());

            String[] sysInfoMessage = message.split(" ");
            int nRecs = Integer.parseInt(sysInfoMessage[1]);
            String lrgestServerType = "";

            for (int i = 0; i < nRecs; i++){
                message = readFromBuffer(din);
               
                sysInfoMessage = message.split(" ");
                currentCoreSize = Integer.parseInt(sysInfoMessage[4]);

                if (currentCoreSize > largestCoreSize){
                    largestCoreSize = currentCoreSize;
                    lrgestServerType = sysInfoMessage[0];
                }
            }   
            
            dout.write(("OK\n").getBytes());
            System.out.println(readFromBuffer(din)); // Receive .
            dout.write(("SCHD " + jobID + " " + lrgestServerType + " 0\n").getBytes());
            System.out.println(readFromBuffer(din));

            while (!(jobMessage.equals("NONE"))){
                dout.write(("REDY\n").getBytes());
                jobMessage = readFromBuffer(din);
                test = jobMessage.split(" ");
                if (test.length > 1){
                    jobID = Integer.parseInt(test[2]);
                    dout.write(("SCHD " + jobID + " " + lrgestServerType + " 0\n").getBytes());
                    jobMessage = readFromBuffer(din);
                }
            }

            dout.write(("OK\n").getBytes());
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
