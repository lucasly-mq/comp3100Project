import java.io.*;  
import java.net.*;  
public class Server {

    public static void main(String args[]){
        try {
        ServerSocket ss = new ServerSocket(6665);
        Socket s = ss.accept();

        DataInputStream dis = new DataInputStream(s.getInputStream());
        String str = (String) dis.readUTF();  
        System.out.println(str);
    
        DataOutputStream dout = new DataOutputStream(s.getOutputStream());  
        dout.writeUTF("G'DAY");
        dout.flush();

        dis = new DataInputStream(s.getInputStream());
        str = (String) dis.readUTF();  
        System.out.println(str);

        dout = new DataOutputStream(s.getOutputStream());
        dout.writeUTF("BYE");
        dout.flush();
        dout.close();

        ss.close();

        } catch(Exception e) {
            System.out.println(e);
        }
    }
}