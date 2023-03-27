import java.io.*;  
import java.net.*;  
public class Server {
    public static void readFromBuffer(BufferedReader input) throws IOException {
        int charNum = input.read();
        while (charNum != 10){
            System.out.print((char) charNum);
            charNum = input.read();
        }
        System.out.println();
    }

    public static void main(String args[]){
        try {
        ServerSocket ss = new ServerSocket(6665);
        Socket s = ss.accept();
        // Handshaking 
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        readFromBuffer(in); 

        DataOutputStream dout = new DataOutputStream(s.getOutputStream());  
        dout.write(("OK\n").getBytes());
        dout.flush();
        // Write OK + System Message

        dout.write(("OK\n").getBytes());

        dout.write(("Welcome!\n").getBytes());
        dout.flush();

        // Logic
        
        readFromBuffer(in);
        readFromBuffer(in);
    
        //DataOutputStream dout = new DataOutputStream(s.getOutputStream());  
        dout.write(("JOBN\n").getBytes());
        dout.flush();
        
        readFromBuffer(in);

        dout = new DataOutputStream(s.getOutputStream());

        dout.write(("BYE\n").getBytes());
        dout.flush();
        dout.close();

        ss.close();

        } catch(Exception e) {
            System.out.println(e);
        }
    }
}