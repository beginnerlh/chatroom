package com.lh.client.service;

import com.lh.client.dao.AccountDao;
import com.lh.client.entity.User;
import com.lh.util.CommUtil;
import com.lh.vo.MessageVO;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.Set;

public class UserLogin {
    private JPanel UserLoginPanle;
    private JTextField userNameText;
    private JPanel btnPanel;
    private JPasswordField passwordText;
    private JButton loginButton;
    private JPanel userPanel;
    private JButton regButton;
    private JFrame frame;
    private AccountDao accountDao = new AccountDao();

    public UserLogin(){
        frame = new JFrame("用户登录");
        frame.setContentPane(UserLoginPanle);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        // 居中显示
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        //点击注册按钮，弹出注册页面
        regButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new UserReg();
            }
        });
        //点击登录按钮，验证用户输入信息是否正确
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //获取用户输入
                String userName = userNameText.getText();
                String password = String .valueOf(passwordText.getPassword());
                User user = accountDao.userLogin(userName,password);
                if (user != null) {
                    // 登录成功
                    JOptionPane.showMessageDialog(null,"登录成功",
                            "提示信息",JOptionPane.INFORMATION_MESSAGE);
                    // 与服务器建立连接，将用户名注册到服务器缓存
                    Connect2Server connect2Server = new Connect2Server();
                    MessageVO messageVO = new MessageVO();
                    messageVO.setType("1");
                    messageVO.setContent(userName);
                    String msgJson = CommUtil.obj2Json(messageVO);
                    try {
                        //发送信息
                        PrintStream out = new PrintStream(connect2Server.getOut(),
                                true,"UTF-8");
                        out.println(msgJson);
                        Scanner in = new Scanner(connect2Server.getIn());
                        if(in.hasNextLine()){
                            String jsonStr = in.nextLine();
                            MessageVO msgFromServer =(MessageVO) CommUtil.json2obj(jsonStr,
                                    MessageVO.class);
                            Set<String> names =(Set<String>) CommUtil.json2obj(msgFromServer.getContent(),
                                    Set.class);
                            System.out.println("在线好友为"+names);
                            //加载好友列表界面，将登陆界面不可见
                            frame.setVisible(false);
                            //跳转到好友列表界面需要传递用户名，与服务器建立的连接，所有在线好友的信息
                            new FriendsList(userName,names,connect2Server);
                        }
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                    }

                }else {
                    // 失败，停留在当前登录页面，提示用户信息错误
                    JOptionPane.showMessageDialog(frame,"登录失败",
                            "错误信息",JOptionPane.ERROR_MESSAGE);

                }
            }
        });
    }

    public static void main(String[] args) {
        new UserLogin();
    }
}
