package servlet_ready_lession.servlet2021_12_21.session;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @Classname SessionDemo1
 * @Description TODO
 * @Date 2021/12/21 21:06
 * @Created by DELL
 */
@WebServlet("/session/demo1")
public class SessionDemo1 extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //1.获取session对象
        HttpSession session = req.getSession();
        //2.存储数据
        session.setAttribute("name","Tome");
    }
}
