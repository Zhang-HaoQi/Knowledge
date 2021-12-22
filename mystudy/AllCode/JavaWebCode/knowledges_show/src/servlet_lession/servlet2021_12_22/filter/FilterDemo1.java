package servlet_lession.servlet2021_12_22.filter;


import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * @Classname FilterDemo
 * @Description TODO
 * @Date 2021/12/21 22:51
 * @Created by DELL
 */
@WebFilter("/filter/*")
public class FilterDemo1 implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //����ǰ��ͨ��ִ��request����ش���
        System.out.println("i am filter1 begin");
      filterChain.doFilter(servletRequest,servletResponse);
        //���к�ͨ��ִ��response����ش���
        System.out.println("i am filter1 over");
    }

    @Override
    public void destroy() {

    }
}
