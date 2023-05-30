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
            Socket socket = new Socket("localhost", 50000);  
            // Initialise data streams
            BufferedReader din = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream dout = new DataOutputStream(new DataOutputStream(socket.getOutputStream()));  
            // Create connection to ds-sim 
            handShake(din, dout);

            // Initialise message variables
            String jobMessage = ""; 
            ArrayList<String> availServers = new ArrayList<>();

            dout.write(("REDY\n").getBytes());
            jobMessage = readFromBuffer(din); // Receive JOB
            String jobType = getJobInfo(jobMessage)[0]; // JOBN JCPL etc
            int jobID = Integer.parseInt(getJobInfo(jobMessage)[2]); // JOBN jobID

            // Get available servers for first Job. 
            dout.write(("GETS Avail " + getSystemResourceInfo(jobMessage) + "\n").getBytes());
            jobMessage = readFromBuffer(din);
            dout.write(("OK\n").getBytes());  
            
            int noOfServers = Integer.parseInt(getJobInfo(jobMessage)[1]);

            for (int i = 0; i < noOfServers; i++)
                jobMessage = readFromBuffer(din); // Print the server description
            
            dout.write(("OK\n").getBytes()); 
            readFromBuffer(din); // Receive .

            dout.write(("SCHD " + jobID + " " + getJobInfo(jobMessage)[0] + " " + 0 + "\n").getBytes());
            readFromBuffer(din); // Get OK
            
            while (!(jobMessage.equals("NONE"))){
                // Receive JOB
                dout.write(("REDY\n").getBytes());
                jobMessage = readFromBuffer(din);
                jobType = getJobInfo(jobMessage)[0];
   
                if ((jobType.equals("JOBN"))){
                    jobID = Integer.parseInt(getJobInfo(jobMessage)[2]);
                    String jobSpecs = getSystemResourceInfo(jobMessage);
                    
                    // Find the first available server that meets the Job specs. (GETS Avail getJobSpecs(jobMessage)).
                    dout.write(("GETS Avail " + jobSpecs + "\n").getBytes());
                    jobMessage = readFromBuffer(din); // Receive DATA nRecs recLen
                    dout.write(("OK\n").getBytes());
                    noOfServers = Integer.parseInt(getJobInfo(jobMessage)[1]);
                    
                    String server = "";

                    if (noOfServers == 0 || jobMessage.equals(".")){ // Enters this block if no servers are currently available.
                        readFromBuffer(din); // Receive . 
                        
                        dout.write(("GETS Capable " + jobSpecs + "\n").getBytes());
                        jobMessage = readFromBuffer(din); // DATA nRecs recLen
                        noOfServers = Integer.parseInt(getJobInfo(jobMessage)[1]);
                        dout.write(("OK\n").getBytes());
                    
                        for (int i = 0; i < noOfServers; i++){
                            jobMessage = readFromBuffer(din); // Print the server description
                            availServers.add(jobMessage);
                        }
            
                        dout.write(("OK\n").getBytes());
                        readFromBuffer(din); // Receive .

                        // Get's the Server with the Best Fit (I understand this can be a function as it is repeated, 
                        // but for some reason that significantly increased one of my performance metrics.

                        int[] fitnessValues = new int[availServers.size()];

                        for (int i = 0; i < availServers.size(); i++)
                            fitnessValues[i] = Integer.parseInt(availServers.get(i).split(" ")[4]) - Integer.parseInt(jobSpecs.split(" ")[0]);
                        
                        int min = fitnessValues[0];

                        for(int i = 0; i < fitnessValues.length; i++){ 
                            if(min > fitnessValues[i])
                               min = fitnessValues[i];
                        }

                        for (int i = 0; i < fitnessValues.length; i++){
                            if (fitnessValues[i] == min){
                                server = availServers.get(i);
                                break;
                            }
                        }

                    } else { // Enters this block if a server is currently available.

                        for (int i = 0; i < noOfServers; i++){
                            jobMessage = readFromBuffer(din); // Print the server description
                            availServers.add(jobMessage);
                        }

                        dout.write(("OK\n").getBytes()); 
                        readFromBuffer(din); // Receive .

                        // Get's the Server with the Best Fit (I understand this can be a function as it is repeated, 
                        // but for some reason that significantly increased one of my performance metrics.

                        int[] fitnessValues = new int[availServers.size()];
                  
                        for (int i = 0; i < availServers.size(); i++){
                            fitnessValues[i] = Integer.parseInt(availServers.get(i).split(" ")[4]) - Integer.parseInt(jobSpecs.split(" ")[0]);
                        }

                        int min = fitnessValues[0];

                        for(int i = 0; i < fitnessValues.length; i++){ 
                            if(min > fitnessValues[i])
                               min = fitnessValues[i];
                        }

                        for (int i = 0; i < fitnessValues.length; i++){
                            if (fitnessValues[i] == min){
                                server = availServers.get(i);
                                break;
                            }
                        }
                    }
                    
                    dout.write(("SCHD " + jobID + " " + server.split(" ")[0] + " " + server.split(" ")[1] + "\n").getBytes());
                    jobMessage = readFromBuffer(din); // Get OK
                    availServers.clear();
                }
            }

            // SEND QUIT
            dout.write(("QUIT\n").getBytes());
            dout.flush();

            // RECEIVE QUIT
            System.out.println(readFromBuffer(din));
            dout.close();
            socket.close();

        } catch(Exception e){
            System.out.println(e);
        }  
    }
}
