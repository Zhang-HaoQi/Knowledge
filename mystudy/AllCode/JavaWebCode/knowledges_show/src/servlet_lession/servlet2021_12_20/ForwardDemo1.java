package servlet_lession.servlet2021_12_20;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Classname Forward
 * @Description TODO
 * @Date 2021/12/19 20:02
 * @Created by DELL
 */
@WebServlet("/forward/demo1")
public class ForwardDemo1 extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //����һ�������������ת��
        RequestDispatcher requestDispatcher = request.getRequestDispatcher("/forward/demo2");
        requestDispatcher.forward(request,response);
        System.out.println("hello i am demo1");//ת���󣬵�demo2ִ���꣬����Ĵ������ִ�С�
        //��������ֱ�ӽ���ת��  ע�⣺��ͬһ��servlet�в��ܽ�������ת��
        //request.getRequestDispatcher("/forward/demo3").forward(request,response);
    }
}
