package com.test;

import com.mysql.jdbc.Connection;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HelloWorldTest
{

    public static void main(String[] args) {
        try {
            //调用Class.forName()方法加载驱动程序
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("成功加载MySQL驱动！");
        } catch (ClassNotFoundException e1) {
            System.out.println("找不到MySQL驱动!");
            e1.printStackTrace();
        }

        String url = "jdbc:mysql://localhost:3306/song";    //JDBC的URL
        //调用DriverManager对象的getConnection()方法，获得一个Connection对象
        Connection conn;
        try {
            conn = (Connection) DriverManager.getConnection(url, "root", "0130");
            //创建一个Statement对象
            Statement stmt = conn.createStatement(); //创建Statement对象
            System.out.print("成功连接到数据库！");

            //执行查询语句
            String sql = "select * from song;";//我的表格叫song
            ResultSet resultSet = stmt.executeQuery(sql);
            //打印查询出来的东西
            String name;
            while (resultSet.next())
            {
                name = resultSet.getString("title");//name是从MySQL提取出的title
                name=name.replaceAll(" ","%20");//将其中的空格替换为‘%20‘才能打开网页
                System.out.println(name);
                //String url2 = "https://baike.baidu.com/item/" + name;//string url
                String str;
                StringBuffer text= new StringBuffer("");
                try {
                    URL url3 = new URL("https://baike.baidu.com/item/"+name);//URL url=url2

                    InputStream in = url3.openStream();
                    if(in != null && !in.equals("if")) {
                        System.out.println("找到一个网页啦");
                        InputStreamReader isr = new InputStreamReader(in);//封装成字符流
                        BufferedReader bufr = new BufferedReader(isr);//用缓冲方式进行文本读取

                        while ((str = bufr.readLine()) != null) {
                            //System.out.println(str);
                            text.append(str+"\n");//将缓冲流拼接为text
                        }

                        Pattern p = Pattern.compile("<div class=\"anchor-list\">\\s*?<a[^>]*?><\\/a>\\s*?<a[^>]*?><\\/a>\\s*?<a[^>]*?><\\/a>\\s*?<\\/div><div class=\"para-title level-2\" label-module=\"para-title\">\\s*?<h2 class=\"title-text\"><span class=\"title-prefix\">[^<]*?<\\/span>歌曲鉴赏<\\/h2>\\s*?<a[^>]*?><em[^>]*?><\\/em>编辑<\\/a>\\s*?<\\/div>([\\s\\S]*?)<div class=\"anchor-list\">\\s*?<a[^>]*?><\\/a>\\s*?<a[^>]*?><\\/a>\\s*?<a[^>]*?><\\/a>\\s*?<\\/div><div class=\"para-title level-2\" label-module=\"para-title\">\\s*?<h2 class=\"title-text\"><span class=\"title-prefix\">[^<]*?<\\/span>[^<]*<\\/h2>\\s*?<a[^>]*?><em[^>]*?><\\/em>编辑<\\/a>\\s*?<\\/div>");
                        //第一次正则匹配
                        Matcher m = p.matcher(text);
                        String fresult = null;
                        if (m.find())
                        {
                            System.out.println("第一次匹配成功！！！！！");
                            fresult = m.group(1);
                            Pattern p2 = Pattern.compile("<[^>]*>");//二次匹配 去掉标签
                            Matcher result = p2.matcher(fresult);
                            //result.replaceAll("");//将匹配到的标签内容删掉
                            fresult = result.replaceAll("");
                            System.out.println(fresult);
                        }
                        else {
                            System.out.println("无歌曲鉴赏部分");
                        }
                        update(name, fresult);//更新数据库

                        bufr.close();
                        isr.close();
                        in.close();

                        }
                    }
                catch(Exception e){
                    e.printStackTrace();
                }
            }

            conn.close();
            System.out.print("sql close!");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * 转义正则特殊字符 （$()*+.[]?\^{}
     * \\需要第一个替换，否则replace方法替换时会有逻辑bug
     */
    /*public static String makeQueryStringAllRegExp(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }

        return str.replace("\\", "\\\\").replace("*", "\\*")
                .replace("+", "\\+").replace("|", "\\|")
                .replace("{", "\\{").replace("}", "\\}")
                .replace("(", "\\(").replace(")", "\\)")
                .replace("^", "\\^").replace("$", "\\$")
                .replace("[", "\\[").replace("]", "\\]")
                .replace("?", "\\?").replace(",", "\\,")
                .replace(".", "\\.").replace("&", "\\&");
    }*/

    public static  void update(String column1,String content){

        Connection conn = getConn();
        //String sql = "update song set baike_introduction='" + Song.getBaike_introduction() + "' where title='%d'+column1 ";?????
        PreparedStatement pstmt;
        try {
            pstmt = conn.prepareStatement("UPDATE song SET baike_introduction=? WHERE title=?");
            pstmt.setString(1,content);
            pstmt.setString(2,column1);
            pstmt.executeUpdate();

            pstmt.close();
            conn.close();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private static Connection getConn() {
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://localhost:3306/song";
        String username = "root";
        String password = "0130";
        Connection conn = null;
        try {
            Class.forName(driver); //classLoader,加载对应驱动
            conn = (Connection) DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }
}