//服务器部分
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

    public static boolean isRun = false;

    ServerSocket serverSocket = null;
    
    List<ServerThread>st = new ArrayList();

    public static void main(String[] args) {
        new ChatServer().start();
    }

    public void start() {

        try {
            serverSocket = new ServerSocket(8888);
            isRun = true;
        } catch (BindException e) {
            System.out.println("端口使用中");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {

            while (isRun) {

                Socket socket = serverSocket.accept();//阻塞
System.out.println("connect success");
                ServerThread serverThread = new ServerThread(socket);
                new Thread(serverThread).start();
                st.add(serverThread);
                // dis.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ServerThread implements Runnable {

        private Socket s;
        private DataInputStream dis = null;
        private DataOutputStream dos = null;
        private boolean bConnected = false;
        
        public ServerThread(Socket s) {
            super();
            this.s = s;
            try {
                dis = new DataInputStream(s.getInputStream());
                dos = new DataOutputStream(s.getOutputStream());
                bConnected = true;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        
        public void send(String str){
            try {
                dos.writeUTF(str);
            } catch (IOException e) {
            	//有人退出则把当前线程向移掉
                st.remove(this);
            
            }
        }
        
        public void run() {
        
            try {
                while (bConnected) {
                    String str = dis.readUTF();//阻塞
                    for(int i = 0 ; i < st.size() ; i ++){                    
                        ServerThread serverThread1 = st.get(i);
                        serverThread1.send(str);    
                    }    
                }
            }catch (EOFException e) {
                System.out.println("client closed");
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    if (s != null){
                        s.close();
                        s = null;
                    }
                    if (dis != null)
                        dis.close();
                    if(dos != null)
                        dos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } 
            }

        }

    }

}