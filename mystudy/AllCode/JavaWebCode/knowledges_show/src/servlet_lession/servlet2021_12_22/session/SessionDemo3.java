package servlet_lession.servlet2021_12_22.session;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * @Classname SessionDemo1
 * @Description TODO
 * @Date 2021/12/21 21:06
 * @Created by DELL
 */
@WebServlet("/session/demo3")
public class SessionDemo3 extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //ʹ��session��������
        //1.��ȡsession����
        HttpSession session = req.getSession();
        session.setAttribute("name","Lily");
        //��ӡsession
        System.out.println(session);
        //2. ����cookie����ʱ�䡣
        Cookie cookie =new Cookie("JSESSIONID",session.getId());
        cookie.setMaxAge(60*60);
        resp.addCookie(cookie);
    }
}
