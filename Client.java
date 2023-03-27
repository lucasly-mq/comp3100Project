import java.io.*;  
import java.net.*;  

public class Client {
    public static void readFromBuffer(BufferedReader input) throws IOException {
        int charNum = input.read();
        while (charNum != 0){
            System.out.print((char) charNum);
            charNum = input.read();
        }
        System.out.println();
    }

    public static void main(String args[]) {
        try {
            Socket s = new Socket("localhost",50000);  
            // Handshaking
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());  
            dout.write(("HELO\n").getBytes());
            dout.flush();  

            readFromBuffer(in);


            String username = System.getProperty("user.name");
            dout.write(("AUTH " + username + "\n").getBytes());
            dout.flush();
            readFromBuffer(in);
            readFromBuffer(in);

            dout.write(("REDY\n").getBytes());

            
            // Logic


            readFromBuffer(in);

            dout = new DataOutputStream(s.getOutputStream()); 
            dout.write(("BYE\n").getBytes());
            dout.flush();

            readFromBuffer(in);
            dout.close();

            s.close();

        } catch(Exception e){
            System.out.println(e);
        }  
    }
}
