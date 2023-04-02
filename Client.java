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
            String jobType = test[0];
            int jobID = Integer.parseInt(test[2]);
            
            // IDENTIFY LARGEST SERVER
            dout.write(("GETS All\n").getBytes());
            message = readFromBuffer(din); // Receive DATA nRecs recSize 
            System.out.println(message);
            dout.write(("OK\n").getBytes());

            String[] sysInfoMessage = message.split(" ");
            int nRecs = Integer.parseInt(sysInfoMessage[1]);
            String lrgestServerType = "";
            int noOfServers = 0; 

            for (int i = 0; i < nRecs; i++){
                message = readFromBuffer(din); 
                sysInfoMessage = message.split(" ");
                currentCoreSize = Integer.parseInt(sysInfoMessage[4]);
                if (currentCoreSize > largestCoreSize){
                    largestCoreSize = currentCoreSize;
                    lrgestServerType = sysInfoMessage[0];
                }
                lrgestServerType = sysInfoMessage[0];
            }   

            dout.write(("OK\n").getBytes());
            dout.write(("GETS Type " + lrgestServerType + "\n").getBytes());
            dout.write(("OK\n").getBytes());
            readFromBuffer(din); // Receive .

            message = readFromBuffer(din);
            test = message.split(" ");
            noOfServers = Integer.parseInt(test[1]);

            int serverID = 0; 
            dout.write(("OK\n").getBytes());
            
            dout.write(("SCHD " + jobID + " " + lrgestServerType + " " + serverID + "\n").getBytes());

            message = readFromBuffer(din);
            test = message.split(" ");
            jobID = Integer.parseInt(test[1]);
         


            while (!(jobMessage.equals("NONE"))){
                dout.write(("REDY\n").getBytes());
                jobMessage = readFromBuffer(din);

                //System.out.println(Arrays.toString(test));
                test = jobMessage.split(" ");
                jobType = test[0];

                if (test.length > 1 && jobType.equals("JOBN")){
                    jobID = Integer.parseInt(test[2]);
                    if (serverID < noOfServers){
                        dout.write(("SCHD " + jobID + " " + lrgestServerType + " " + serverID + "\n").getBytes());
                        jobMessage = readFromBuffer(din); // SHOULD BE OK
                        jobType = test[0];
                        serverID++;
                    } else {
                        serverID = 0;
                    }
                }
            }

            readFromBuffer(din); // Receive NONE;

            // SEND QUIT
            dout.write(("QUIT\n").getBytes());
            dout.flush();

            // RECEIVE QUIT
            System.out.println(readFromBuffer(din));
            dout.close();
            s.close();

        } catch(Exception e){
            System.out.println(e);
        }  
    }
}
