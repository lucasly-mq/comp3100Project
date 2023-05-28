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

    public static String[] getJobInfo(String jobMessage){
        return jobMessage.split(" ");
    }

    public static String getSystemResourceInfo(String jobMessage){
        String[] temp = Arrays.copyOfRange(getJobInfo(jobMessage), 4, 7);
        return temp[0] + " " + temp[1] + " " + temp[2];
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

            // Initialise message variables
            String jobMessage = ""; 
            dout.write(("REDY\n").getBytes());
            jobMessage = readFromBuffer(din); // Receive JOB

            String jobType = getJobInfo(jobMessage)[0]; // JOBN JCPL etc
            int jobID = Integer.parseInt(getJobInfo(jobMessage)[2]); // JOBN jobID
            //String[] systemResource = getSystemResourceInfo(jobMessage);
            
            // Finding largest server type variables
            int currentCoreSize = 1;
            int largestCoreSize = 0;
            String largestServerType = "";
            int noOfServers = 0; 
            int serverID = 0; 
    
            // IDENTIFY LARGEST SERVER

            String systemMessage = "GETS All " + getSystemResourceInfo(jobMessage) + "\n";
            System.out.println(systemMessage);
            dout.write(systemMessage.getBytes());
            jobMessage = readFromBuffer(din); // Receive DATA nRecs recSize 
            dout.write(("OK\n").getBytes());
            int nRecs = Integer.parseInt(getJobInfo(jobMessage)[1]); // Receive no. of server types 
            for (int i = 0; i < nRecs; i++){
                jobMessage = readFromBuffer(din); // eg. juju 0 booting 120 0 2500 13100 1 0 
                currentCoreSize = Integer.parseInt(getJobInfo(jobMessage)[4]);
            
                if (currentCoreSize > largestCoreSize)
                    largestCoreSize = currentCoreSize;
                
                largestServerType = getJobInfo(jobMessage)[0];
            }   

            dout.write(("OK\n").getBytes());
            System.out.println(readFromBuffer(din)); // Receive .
    
            boolean encounteredFirst = false;

            dout.write(("GETS All\n").getBytes());
            jobMessage = readFromBuffer(din); // Receive DATA nRecs recSize 
            dout.write(("OK\n").getBytes());

            for (int i = 0; i < nRecs; i++){
                jobMessage = readFromBuffer(din); // eg. juju 0 booting 120 0 2500 13100 1 0 
                currentCoreSize = Integer.parseInt(getJobInfo(jobMessage)[4]);
               
                if ((currentCoreSize == largestCoreSize) && !encounteredFirst){
                    largestServerType = getJobInfo(jobMessage)[0];
                    encounteredFirst = true;
                }
            }

            dout.write(("OK\n").getBytes());
            System.out.println(readFromBuffer(din)); // Receive .

            // Receive Largest Server Type Info 
            // systemMessage = "GETS Capable " + getSystemResourceInfo(jobMessage) + "\n";
            // System.out.println(systemMessage);
            // dout.write(systemMessage.getBytes());

            dout.write(("GETS Type " + largestServerType + "\n").getBytes());
            jobMessage = readFromBuffer(din); // DATA nRecs recSize     
            dout.write(("OK\n").getBytes());
            noOfServers = Integer.parseInt(getJobInfo(jobMessage)[1])-1; // nRecs value
            dout.write(("OK\n").getBytes());
      
            if (noOfServers > 0) {
                for (int i = 0; i < noOfServers+1; i++){
                    jobMessage = readFromBuffer(din);
                } // Print's out the server descriptions. 
                System.out.println(readFromBuffer(din)); // Receive . 
            } else {
                jobMessage = readFromBuffer(din);
                System.out.println(readFromBuffer(din)); // Receive . 
            }

            dout.write(("SCHD " + jobID + " " + largestServerType + " " + serverID + "\n").getBytes()); // Schedule first job
            jobMessage = readFromBuffer(din); // GET OK
            if (noOfServers > 0)
                serverID++;

            while (!(jobMessage.equals("NONE"))){
                // Get next JOB
                dout.write(("REDY\n").getBytes());
                jobMessage = readFromBuffer(din);
                jobType = getJobInfo(jobMessage)[0];

                if ((jobType.equals("JOBN"))){
                    jobID = Integer.parseInt(getJobInfo(jobMessage)[2]);
                    // Schedule JOB
                    dout.write(("SCHD " + jobID + " " + largestServerType + " " + serverID + "\n").getBytes());
  
                    if (noOfServers > 0 && (serverID < noOfServers)){ 
                        serverID++; 
                    } else if (noOfServers > 0 && (serverID == noOfServers))
                        serverID = 0;
                    
                    jobType = getJobInfo(jobMessage)[0];   
                    jobMessage = readFromBuffer(din); // SHOULD BE OK

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
