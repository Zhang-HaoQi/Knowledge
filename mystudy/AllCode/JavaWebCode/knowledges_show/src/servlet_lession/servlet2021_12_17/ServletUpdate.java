package servlet_lession.servlet2021_12_17;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

/**
 * @Classname ServletUpdate
 * @Description TODO
 * @Date 2021/12/17 15:25
 * @Created by DELL
 */
@WebServlet({"/blog/update","/blog/updateTwo"})
public class ServletUpdate implements Servlet {
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        System.out.println("修改一条数据");
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {

    }
}
