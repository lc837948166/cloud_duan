package com.xw.cloud.Utils;


import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SshClient {
    public String  runCommand(String host, String username, String password, String command) {
        JSch jsch = new JSch();
        Session session = null;
        ChannelExec channel = null;
        StringBuilder output = new StringBuilder();

        try {
            // 连接到远程主机
            session = jsch.getSession(username, host, 22);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            // 执行命令
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.connect();

            // 获取命令输出
            InputStream inputStream = channel.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // 关闭连接
            channel.disconnect();
            session.disconnect();
        } catch (JSchException | IOException e) {
            e.printStackTrace();
        }
        return output.toString();
    }
}
