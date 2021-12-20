package servlet_ready_lession.servlet2021_12_21.response;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Classname ResponseDemo1
 * @Description TODO
 * @Date 2021/12/20 21:04
 * @Created by DELL
 */
@WebServlet("/reps/demo")
public class ResponseDemo1 extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, IOException {
        //��ʽ1�������ض���
        //1.1����״̬��
        response.setStatus(302);
        //1.2������Ӧͷlocation
        response.setHeader("location", "/Response/servletDemo2");
        //��ʽ2�������ض��򣨼򵥣�
        response.sendRedirect("/servlet/response/response.jsp");
    }

}
