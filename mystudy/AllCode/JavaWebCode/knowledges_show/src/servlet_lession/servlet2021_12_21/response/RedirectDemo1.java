package servlet_lession.servlet2021_12_21.response;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @Classname redirect1
 * @Description TODO
 * @Date 2021/12/20 21:38
 * @Created by DELL
 */
@WebServlet("/redirect/demo1")
public class RedirectDemo1 extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("message", "tom");
        //��ʽһ
        //1.1����״̬��
//        resp.setStatus(302);
//        //1.2������Ӧͷlocation
//        resp.setHeader("location", "/servlet/redirect/demo2");
        //��ʽ��(�򵥣�����ʹ��)ע�⣺ת����Ҫ������·��
        resp.sendRedirect(req.getContextPath()+"/redirect/demo2");
        //�ض���github
//        resp.sendRedirect("https://github.com/");
    }
}
