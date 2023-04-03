import java.io.*;  
import java.net.*;  
import java.util.*;

public class Client {
    public static String readFromBuffer(BufferedReader input) throws IOException {
        int charNum = input.read();
        String message = ""; 
        while (charNum != 10){
            message += (char) charNum;
            charNum = input.read();
        }
        return message;
    }

    public static void handShake(BufferedReader din, DataOutputStream dout) throws IOException {
          // SEND HELO
            dout.write(("HELO\n").getBytes());
            // RECEIVE OK
            readFromBuffer(din);
            // SEND AUTH
            String username = System.getProperty("user.name");
            dout.write(("AUTH " + username + "\n").getBytes());
            // RECEIVE OK 
            readFromBuffer(din);
    }





    public static void main(String args[]) {
        try {
            // Initialise socket 
            Socket s = new Socket("localhost", 50000);  
            // Initialise data streams
            BufferedReader din = new BufferedReader(new InputStreamReader(s.getInputStream()));
            DataOutputStream dout = new DataOutputStream(new DataOutputStream(s.getOutputStream()));  
            // Create connection to ds-sim 
            handShake(din, dout);

            String jobMessage = ""; 
            dout.write(("REDY\n").getBytes());
            jobMessage = readFromBuffer(din); // Receive JOB
            System.out.println(jobMessage + " FIRST JOB!");
            
            String[] jobInfo = jobMessage.split(" ");
            String jobType = jobInfo[0]; // JOBN JCPL etc
            int jobID = Integer.parseInt(jobInfo[2]); // JOBN jobID
            
            // IDENTIFY LARGEST SERVER
            dout.write(("GETS All\n").getBytes());
            jobMessage = readFromBuffer(din); // Receive DATA nRecs recSize 
            dout.write(("OK\n").getBytes());
            
            // Finding largest server type variables
            int currentCoreSize = 1;
            int largestCoreSize = 0;
            String largestServerType = "";
            int noOfServers = 0; 

            jobInfo = jobMessage.split(" ");
            int nRecs = Integer.parseInt(jobInfo[1]); // Receive no. of server types 
         
            // Receive Server Info 
            for (int i = 0; i < nRecs; i++){
                jobMessage = readFromBuffer(din); // eg. juju 0 booting 120 0 2500 13100 1 0 
                jobInfo = jobMessage.split(" ");
                currentCoreSize = Integer.parseInt(jobInfo[4]);

                if (currentCoreSize > largestCoreSize){
                    largestCoreSize = currentCoreSize;
                }

                largestServerType = jobInfo[0];
                
            }   

            dout.write(("OK\n").getBytes());
            readFromBuffer(din); // Receive .
            
            // Receive Largest Server Type Info 

            dout.write(("GETS Type " + largestServerType + "\n").getBytes());
            dout.write(("OK\n").getBytes());
            jobMessage = readFromBuffer(din); // DATA nRecs recSize 
            dout.write(("OK\n").getBytes());

            jobInfo = jobMessage.split(" ");
            noOfServers = Integer.parseInt(jobInfo[1])-1; // nRecs value 

       

         
            

           


            int serverID = 0; 
         
            // SOMETHING HERE 
            System.out.println(jobMessage + ": Before"); // DATA 


            if (noOfServers > 0) {
                for (int i = 0; i < noOfServers+1; i++){
                    jobMessage = readFromBuffer(din);
                    System.out.println(jobMessage);
                }
                System.out.println(readFromBuffer(din) + "RECEVIED"); // Receive . 
                dout.write(("OK\n").getBytes());
            } else
                jobMessage = readFromBuffer(din);

                
            System.out.println(jobMessage + ": After");
            jobInfo = jobMessage.split(" ");
            jobID = Integer.parseInt(jobInfo[1]);

            System.out.println(readFromBuffer(din)); // Receive . 
            dout.write(("SCHD " + jobID + " " + largestServerType + " " + serverID + "\n").getBytes()); // Schedule first job
            System.out.println(readFromBuffer(din)); // GET OK


            while (!(jobMessage.equals("NONE"))){
                System.out.println(jobMessage + " BEFORE");
                dout.write(("REDY\n").getBytes());
                jobMessage = readFromBuffer(din);
                System.out.println(jobMessage + ": AFTER");
                jobInfo = jobMessage.split(" ");
                jobType = jobInfo[0];
                System.out.println("serverID: " + serverID + " | noOfServers: " + noOfServers);

                if ((jobType.equals("JOBN") && (serverID < noOfServers)) || (jobType.equals("JOBN") && (serverID >= noOfServers))){
                    System.out.println("TEST ENTER LOOP" + serverID);
                    jobID = Integer.parseInt(jobInfo[2]);

                    if (noOfServers == 0){
                        dout.write(("SCHD " + jobID + " " + largestServerType + " " + serverID + "\n").getBytes());
                    }
                    if (noOfServers >= 1 && (serverID < noOfServers)){
                        dout.write(("SCHD " + jobID + " " + largestServerType + " " + serverID + "\n").getBytes());
                        serverID++;
                    } else {
                        serverID = 0;
                    }

                    /* 
                    if (serverID > noOfServers)
                        serverID--;
                    dout.write(("SCHD " + jobID + " " + largestServerType + " " + serverID + "\n").getBytes());
                    serverID++;
                    */
                    
                    jobType = jobInfo[0];
                    jobMessage = readFromBuffer(din); // SHOULD BE OK
                    if (jobMessage.equals("OK"))
                        System.out.println(jobMessage + ": INNER JOBMESSAGE");
                
                } else {
                    serverID = 0;
                }
            }

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
