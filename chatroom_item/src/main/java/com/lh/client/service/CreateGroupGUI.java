package com.lh.client.service;

import com.lh.util.CommUtil;
import com.lh.vo.MessageVO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CreateGroupGUI {
    private JPanel creatGroupPanel;
    private JPanel friendLabelPanel;
    private JTextField groupNameText;
    private JButton conformBtn;

    private String myName;
    private Set<String> friends;
    private Connect2Server connect2Server;
    private FriendsList friendList;

    public CreateGroupGUI(String myName,Set<String> friends,
                          Connect2Server connect2Server,
                          FriendsList friendList){
        this.myName = myName;
        this.friends = friends;
        this.connect2Server = connect2Server;
        this.friendList = friendList;
        JFrame frame = new JFrame("创建群组");
        frame.setContentPane(creatGroupPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400,300);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        //将在线好友一checkBox展示到界面中
        friendLabelPanel.setLayout(new BoxLayout(friendLabelPanel,BoxLayout.Y_AXIS));
        Iterator<String> iterator = friends.iterator();
        while(iterator.hasNext()){
            String labelName = iterator.next();
            JCheckBox checkBox = new JCheckBox(labelName);
            friendLabelPanel.add(checkBox);
        }

        //点击提交按钮提交信息到服务端
        conformBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //判断那些好友选中加入群聊
                Set<String> selectedFriends = new HashSet<>();
                Component[] components = friendLabelPanel.getComponents();
                for(Component component :components){
                    JCheckBox checkBox = (JCheckBox) component;
                    if(checkBox.isSelected()) {
                        String labelName = checkBox.getText();
                        selectedFriends.add(labelName);
                    }
                }
                selectedFriends.add(myName);
                //2 获取群名输入框输入的群名称
                String groupName = groupNameText.getText();
                //3.将群名+选中好友信息发送到服务器
                //type:3
                //content:groupName;
                //to:[user1,user2,user3..]
                MessageVO messageVO = new MessageVO();
                messageVO.setType("3");
                messageVO.setContent(groupName);
                messageVO.setTo(CommUtil.obj2Json(selectedFriends));
                try {
                    PrintStream out = new PrintStream(connect2Server.getOut(),true,"UTF-8");
                    out.println(CommUtil.obj2Json(messageVO));
                } catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                }
                //4 将当前创建群界面隐藏。刷新好友列表界面的群列表
                frame.setVisible(false);

                friendList.addGroup(groupName,selectedFriends);
                friendList.loadGroupList();

            }
        });
    }
}
