package servlet_lession.servlet2021_12_19;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;

/**
 * @Classname RequestDemo1
 * @Description TODO
 * @Date 2021/12/19 13:17
 * @Created by DELL
 */
@WebServlet("/req/demo1")
public class RequestDemo1 extends HttpServlet {


    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //��ȡ����������
//        System.out.println("username:"+req.getParameter("username"));
//        System.out.println("password:"+req.getParameter("password"));
//        System.out.println("��������");
//        Map<String, String[]> parameterMap = req.getParameterMap();
//        for (String key:parameterMap.keySet()
//             ) {
//            String[] paramers = parameterMap.get(key);
//            System.out.println(Arrays.toString(paramers));
//        }
//        System.out.println("���󷽷�"+req.getMethod());
//        System.out.println("��������·��"+req.getContextPath());
//        System.out.println("����ӿ�·��"+req.getServletPath());
//        System.out.println("����URI"+req.getRequestURI());
//        System.out.println("����URL"+req.getRequestURL().toString());
//        //��ȡ����ͷ����
//        System.out.println("��ȡ������汾"+req.getHeader("user-agent"));
//        System.out.println("��ȡ�����ַ" + req.getHeader("referer"));
//        Enumeration<String> headerNames = req.getHeaderNames();
//        System.out.println(headerNames);
        //��ȡ����������
        //��ȡ������Ϣ�壬���������
        //1.��ȡ�ַ���
        BufferedReader bf =req.getReader();
        //2.��ȡ����
        String line= null;
        //line=���������Ϊ�գ���˵��û��������
        while ((line=bf.readLine())!=null){
            System.out.println(line);
        }

    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req,resp);
    }

}
