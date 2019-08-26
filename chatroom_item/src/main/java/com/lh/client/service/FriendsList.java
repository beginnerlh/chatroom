package com.lh.client.service;

import com.lh.util.CommUtil;
import com.lh.vo.MessageVO;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FriendsList {
    private JPanel friendsPanel;
    private JScrollPane friendsList;
    private JButton createGroupBtn;
    private JScrollPane groupListPanel;
    private JFrame frame;

    private String userName;
    //存储所有在线好友
    private Set<String> users;
    //存储所有群名称以及群好友
    private Map<String, Set<String>> groupList = new ConcurrentHashMap<>();
    private Connect2Server connect2Server;
    //缓存所有私聊界面
    private Map<String, PrivateChatGUI> privateChatGUIMap = new ConcurrentHashMap<>();
    //缓存所有群聊界面
    private Map<String, GroupChatGUI> groupChatGUIMap = new ConcurrentHashMap<>();
    //好友列表后台任务，不断监听服务器发来的信息
    //好友上线信息，用户私聊群聊

    private class DaemonTask implements Runnable {
        private Scanner scanner = new Scanner(connect2Server.getIn());

        @Override
        public void run() {
            while (true) {
                if (scanner.hasNextLine()) {
                    String strFromServer = scanner.nextLine();
                    //此时服务器发来的是一个json字符串
                    if (strFromServer.startsWith("{")) {
                        MessageVO messageVO = (MessageVO) CommUtil.json2obj(strFromServer, MessageVO.class);
                        if (messageVO.getType().equals("2")) {
                            //服务器发来的私聊消息
                            String friendName = messageVO.getContent().split("-")[0];
                            String msg = messageVO.getContent().split("-")[1];
                            //判断次私聊是否为第一次创建
                            if (privateChatGUIMap.containsKey(friendName)) {
                                PrivateChatGUI privateChatGUI = privateChatGUIMap.get(friendName);
                                privateChatGUI.getFrame().setVisible(true);
                                privateChatGUI.readFromServer(friendName + "说:" + msg);
                            } else {
                                PrivateChatGUI privateChatGUI = new PrivateChatGUI(friendName, userName, connect2Server);
                                privateChatGUIMap.put(friendName, privateChatGUI);
                                privateChatGUI.readFromServer(friendName + "说" + msg);
                            }
                        } else if (messageVO.getType().equals("4")) {
                            // 收到服务器发来的群聊信息
                            // type:4
                            // content:sender-msg
                            // to:groupName-[1,2,3,...]
                            String groupName = messageVO.getTo().split("-")[0];
                            String sendName = messageVO.getContent().split("-")[0];
                            String groupMsg = messageVO.getContent().split("-")[1];
                            //若此群名称在群聊列表
                            if (groupList.containsKey(groupName)) {
                                if (groupChatGUIMap.containsKey(groupName)) {
                                    //聊天界面弹出
                                    GroupChatGUI groupChatGUI = groupChatGUIMap.get(groupName);
                                    groupChatGUI.getFrame().setVisible(true);
                                    groupChatGUI.readFromServer(sendName + "说:" + groupMsg);
                                } else {
                                    Set<String> names = groupList.get(groupName);
                                    GroupChatGUI groupChatGUI = new GroupChatGUI(groupName, names, userName, connect2Server);
                                    groupChatGUIMap.put(groupName, groupChatGUI);
                                    groupChatGUI.readFromServer(sendName + "说:" + groupMsg);
                                }
                            } else {
                                // 若群成员第一次收到群聊信息
                                // 1.将群名称以及群成员保存到当前客户端群聊列表
                                Set<String> friends = (Set<String>) CommUtil.json2obj(messageVO.getTo().split("-")[1],
                                        Set.class);
                                groupList.put(groupName, friends);
                                loadGroupList();
                                // 2.弹出群聊界面
                                GroupChatGUI groupChatGUI = new GroupChatGUI(groupName,
                                        friends, userName, connect2Server);
                                groupChatGUIMap.put(groupName, groupChatGUI);
                                groupChatGUI.readFromServer(sendName + "说:" + groupMsg);
                            }

                        }
                    } else {
                        //newLogin:userName
                        if (strFromServer.startsWith("newLogin:")) {
                            String newFriendName = strFromServer.split(":")[1];
                            users.add(newFriendName);
                            //弹框提示用户上线
                            JOptionPane.showMessageDialog(frame, newFriendName + "上线了!", "上线提醒", JOptionPane.INFORMATION_MESSAGE);
                            //刷新好友列表
                            loadUsers();
                        }
                    }
                }
            }
        }
    }
    //私聊点击事件
    private class PrivateLabelAction implements MouseListener{
        private String labelName;

        public PrivateLabelAction(String labelName){
            this.labelName = labelName;
        }
        //鼠标点击执行事件
        @Override
        public void mouseClicked(MouseEvent e) {
            //判断好友列表私聊界面缓存是否已经有指定标签
            if(privateChatGUIMap.containsKey(labelName)){
                PrivateChatGUI privateChatGUI = privateChatGUIMap.get(labelName);
                privateChatGUI.getFrame().setVisible(true);
            }else{
                //第一次点击，创建私聊界面
                PrivateChatGUI privateChatGUI = new PrivateChatGUI(labelName,userName,connect2Server);
                privateChatGUIMap.put(labelName,privateChatGUI);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }

    //群聊点击事件
    private class GroupLabelAction implements MouseListener {
        private String groupName;

        public GroupLabelAction(String groupName) {
            this.groupName = groupName;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (groupChatGUIMap.containsKey(groupName)) {
                GroupChatGUI groupChatGUI = groupChatGUIMap.get(groupName);
                groupChatGUI.getFrame().setVisible(true);
            }else {
                Set<String> names = groupList.get(groupName);
                GroupChatGUI groupChatGUI = new GroupChatGUI(
                        groupName,names,userName,connect2Server
                );
                groupChatGUIMap.put(groupName,groupChatGUI);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }

    public FriendsList(String userName,Set<String> users,Connect2Server connect2Server){
        this.userName = userName;
        this.users = users;
        this.connect2Server = connect2Server;
        frame = new JFrame(userName);
        frame.setContentPane(friendsPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400,400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        loadUsers();
        //启动后线程不断监听服务器发来的消息
        Thread daemonThread = new Thread(new DaemonTask());
        daemonThread.setDaemon(true);
        daemonThread.start();

        //创建群组
        createGroupBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new CreateGroupGUI(userName,users,connect2Server,
                        FriendsList.this);
            }
        });
    }

    //加载所有在线的用户信息
    public void loadUsers() {
        JLabel[] userLabels = new JLabel [users.size()];
        JPanel friends = new JPanel();
        friends.setLayout(new BoxLayout(friends,BoxLayout.Y_AXIS));
        // set遍历
        Iterator<String> iterator = users.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            String userName = iterator.next();
            userLabels[i] = new JLabel(userName);
            // 添加标签点击事件
            userLabels[i].addMouseListener(new PrivateLabelAction(userName));
            friends.add(userLabels[i]);
            i++;
        }
        friendsList.setViewportView(friends);
        friendsList.setViewportView(friends);
        // 设置滚动条垂直滚动
        friendsList.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        friends.revalidate();
        friendsList.revalidate();
    }

    public void loadGroupList() {
        // 存储所有群名称标签Jpanel
        JPanel groupNamePanel = new JPanel();
        groupNamePanel.setLayout(new BoxLayout(groupNamePanel,
                BoxLayout.Y_AXIS));
        JLabel[] labels = new JLabel[groupList.size()];
        // Map遍历
        Set<Map.Entry<String,Set<String>>> entries = groupList.entrySet();
        Iterator<Map.Entry<String,Set<String>>> iterator =
                entries.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Map.Entry<String,Set<String>> entry = iterator.next();
            labels[i] = new JLabel(entry.getKey());
            labels[i].addMouseListener(new GroupLabelAction(entry.getKey()));
            groupNamePanel.add(labels[i]);
            i++;
        }
        groupListPanel.setViewportView(groupNamePanel);
        groupListPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        groupListPanel.revalidate();
    }

    public void addGroup(String groupName,Set<String> friends) {
        groupList.put(groupName,friends);
    }

}
