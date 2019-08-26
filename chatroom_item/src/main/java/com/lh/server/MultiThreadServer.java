package com.lh.server;

import com.lh.util.CommUtil;
import com.lh.vo.MessageVO;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadServer {

    private static final Integer PORT;
    private static final String IP ;
    //缓存当前服务器所在线程的客户端信息
    private static Map<String, Socket> clients = new ConcurrentHashMap<>();
    //缓存当前服务器注册的所有群名称以及群好友
    private static Map<String,Set<String>> groups = new ConcurrentHashMap<>();

    static {
        Properties properties = CommUtil.loadProperties("socket.properties");
        PORT = Integer.valueOf(properties.getProperty("port"));
        IP = properties.getProperty("ip");
    }


    private static class ExecuteClient implements Runnable{
        private Socket client;
        private Scanner in;
        private PrintStream out;

        public ExecuteClient(Socket client){
            this.client = client;
            try {
                this.in = new Scanner(client.getInputStream());
                this.out = new PrintStream(client.getOutputStream(),
                        true,"UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            while(true){
                if(in.hasNextLine()){
                    String strFromClient  = in.nextLine();
                    MessageVO msgFromClient = (MessageVO) CommUtil.json2obj(strFromClient,MessageVO.class);
                    if(msgFromClient.getType().equals("1")){
                        //新用户注册
                        String userName = msgFromClient.getContent();
                        //将当前聊天室在线好友信息发回给新用户
                        MessageVO msg2Client = new MessageVO();
                        msg2Client.setType("1");
                        msg2Client.setContent(CommUtil.obj2Json(clients.keySet()));
                        out.println(CommUtil.obj2Json(msg2Client));
                        //将新用户上线信息发给其他在线用户
                        sendUserLogin("newLogin:"+userName);
                        //将新用户信息保存到当前服务端缓存
                        clients.put(userName,client);
                        System.out.println(userName+"上线了");
                        System.out.println("当前聊天室在线人数为："+clients.size());
                    }else if(msgFromClient.getType().equals("2")){
                        // 用户私聊
                        // type:2
                        //  Content:myName-msg
                        //  to:friendName
                        String friendName = msgFromClient.getTo();
                        Socket clientSocket = clients.get(friendName);
                        try {
                            PrintStream out = new PrintStream(clientSocket.getOutputStream(),true,"UTF-8");
                            MessageVO msg2Client = new MessageVO();
                            msg2Client.setType("2");
                            msg2Client.setContent(msgFromClient.getContent());
                            System.out.println("收到私信息,内容为"+msgFromClient.getContent());
                            out.println(CommUtil.obj2Json(msg2Client));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else if(msgFromClient.getType().equals("3")){
                        //注册群
                        String groupName = msgFromClient.getContent();
                        //该群的所有成员
                        Set<String> friends = (Set<String>) CommUtil.json2obj(msgFromClient.getTo(),Set.class);
                        groups.put(groupName,friends);
                        System.out.println("有新的群注册成功，群名为"+groupName+
                                ",一共有"+groups.size()+"个群");
                    }else if(msgFromClient.getType().equals("4")){
                        //群聊消息
                        System.out.println("服务器收到的群聊消息为："+msgFromClient);
                        String groupName = msgFromClient.getTo();
                        Set<String> names = groups.get(groupName);
                        Iterator<String> iterator = names.iterator();
                        while (iterator.hasNext()) {
                            String socketName = iterator.next();
                            Socket client = clients.get(socketName);
                            try {
                                PrintStream out = new PrintStream(client.getOutputStream(),
                                        true,"UTF-8");
                                MessageVO messageVO = new MessageVO();
                                messageVO.setType("4");
                                messageVO.setContent(msgFromClient.getContent());
                                // 群名-[]
                                messageVO.setTo(groupName+"-"+CommUtil.obj2Json(names));
                                out.println(CommUtil.obj2Json(messageVO));
                                System.out.println("服务端发送的群聊信息为:"+messageVO);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }


        /**
         * 向所有在线用户发送新用户上线信息
         * @param msg
         */
        private void sendUserLogin(String msg) {
            for (Map.Entry<String,Socket> entry: clients.entrySet()) {
                Socket socket = entry.getValue();
                try {
                    PrintStream out = new PrintStream(socket.getOutputStream(),
                            true,"UTF-8");
                    out.println(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {

        //建立基站
        ServerSocket server = new ServerSocket(PORT);

        ExecutorService executor = Executors.newFixedThreadPool(50);

        for(int i = 0;i<50;i++){
            System.out.println("等待客户端连接");
            Socket client = server.accept();
            System.out.println("有新连接，端口号为"+client.getPort());
            executor.submit(new ExecuteClient(client));
        }
    }
}
