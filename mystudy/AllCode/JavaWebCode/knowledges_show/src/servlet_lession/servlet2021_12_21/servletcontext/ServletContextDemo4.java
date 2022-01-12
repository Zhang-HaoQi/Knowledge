package servlet_lession.servlet2021_12_21.servletcontext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @Classname ServletContextDemo4
 * @Description TODO
 * @Date 2021/12/20 22:37
 * @Created by DELL
 */
@WebServlet("/context/demo4")
public class ServletContextDemo4 extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletContext servletContext = request.getServletContext();
        //��ȡ��ǰ·��
        String realPath1 = servletContext.getRealPath("/");
        System.out.println(realPath1);
        //��ȡ��·�����ļ�
        String realPathA = servletContext.getRealPath("/WEB-INF/classes/ConfigurationA");
        System.out.println(realPathA);
        //��ȡWEB-INF���ļ�
        String realPathB = servletContext.getRealPath("/WEB-INF/ConfigurationB");
        System.out.println(realPathB);
        //��ȡweb���ļ�
        String realPathC= servletContext.getRealPath("/ConfigurationC");
        System.out.println(realPathC);
    }
}