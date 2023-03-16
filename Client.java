import java.io.*;  
import java.net.*;  

public class Client {
    public static void main(String args[]) {
        try {
            Socket s = new Socket("localhost",6665);  
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());  

            dout.writeUTF("HELO");  
            dout.flush();  
         
            DataInputStream dis = new DataInputStream(s.getInputStream());  

            String str = (String) dis.readUTF();  
            System.out.println(str);
            dout = new DataOutputStream(s.getOutputStream()); 

            dout.writeUTF("BYE");
            dout.flush();

            dis = new DataInputStream(s.getInputStream());
            str = (String) dis.readUTF();  
            System.out.println(str);
            dout.close();

            s.close();

        } catch(Exception e){
            System.out.println(e);
        }  
    }
}