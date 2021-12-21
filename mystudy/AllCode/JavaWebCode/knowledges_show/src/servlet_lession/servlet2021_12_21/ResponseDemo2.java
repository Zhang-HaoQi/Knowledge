package servlet_lession.servlet2021_12_21;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @Classname ResponseDemo2
 * @Description TODO
 * @Date 2021/12/20 22:02
 * @Created by DELL
 */

@WebServlet("/reqs/demo2")
public class ResponseDemo2 extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //�������
        //��ʽһ��
        resp.setContentType("text/html;charset=utf-8");
        //��ʽ����
        //1.1��ȡ������֮ǰ��������Ĭ�ϱ��롰ISO-8859-1������Ϊ��utf-8
//        resp.setCharacterEncoding("utf-8");
        //1.2��������������������͵����ݵı��룬���������ʹ�ô˱���
//        resp.setHeader("content-type","text/html;charset=utf-8");
        //��ȡ�ַ������
        PrintWriter writer = resp.getWriter();
        //�������
        writer.write("hello world");
        //���html��ʽ����
        writer.write("<h1>hello world<h1>");
        //�����������
        writer.write("��ã��ҵ�����");//��������
    }
}

