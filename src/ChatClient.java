import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.Choice;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ChatClient extends Frame {
    DataOutputStream dos = null;
    Socket socket;
    TextField inputChat = new TextField();
    TextArea showChat = new TextArea();
    Button button = new Button("enter");
    Choice choice = new Choice();
    String nickname = null;
    DataInputStream dis = null;
    private static boolean isRun = false;

    
    Thread tRecv = new Thread(new ClientThread());
    
    
    public static void main(String[] args) {

        new ChatClient().launchFrame();

    }

    public void launchFrame() {
    	setTitle("在线聊天室");
        setLocation(400, 300);
        setSize(300, 300);
        
        choice.add("选择昵称：");
        choice.add("冰封之吻");
        choice.add("海滩搭贝");
        choice.addItemListener(new NickNameChoice());
        add(choice, BorderLayout.WEST);
        add(inputChat, BorderLayout.CENTER);
        add(button, BorderLayout.EAST);
        add(showChat, BorderLayout.NORTH);
        pack();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnect();
                System.exit(0);
            }
        });
        inputChat.addActionListener(new TextFieldListener());
        button.addActionListener(new TextFieldListener());
        setVisible(true);

        connectToServer();
        
        tRecv.start();

    }

    private void connectToServer() {

        try {
            socket = new Socket("127.0.0.1", 8888);
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            isRun = true;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {

        
        try {
            dos.close();
            dis.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //关闭各种流之前必须先关闭接受线程
    }

    
    private class ClientThread implements Runnable{

        public void run() {
            
            try {
                while(isRun){   
                    String str = dis.readUTF();//阻塞  
                    showChat.setText(showChat.getText() + str +'\n');    
                    showChat.setCaretPosition(showChat.getText().length());
                }    
            }catch (SocketException e) {
                System.out.println("bye");
            }catch (EOFException e) {
                
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        
    }
    
    private class NickNameChoice implements ItemListener {
    	public void itemStateChanged(ItemEvent ie) {
    		Choice temp=(Choice)ie.getSource();
    		nickname=temp.getSelectedItem();
    	}
    }
    
    private class TextFieldListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String s = inputChat.getText().trim();
            //为了区分不同用户的发言
            s = nickname +":" + s ;
            //showChat.setText(s);
            inputChat.setText("");
            try {
                dos.writeUTF(s);
                dos.flush();
                //dos.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

    }
    
}