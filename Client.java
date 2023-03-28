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
            // GET SERVER INFO
            dout.write(("GETS All\n").getBytes());

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
