//package servlet_lession.servlet2021_12_22.filter;
//
//import javax.servlet.*;
//import javax.servlet.annotation.WebFilter;
//import java.io.IOException;
//
//@WebFilter("/*")
//public class FilterDemo implements Filter {
//    @Override
//    public void init(FilterConfig filterConfig) throws ServletException {
//
//    }
//
//    @Override
//    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//
//        //����ǰ��ͨ��ִ��request����ش���
//        System.out.println("����ǰ");
//        filterChain.doFilter(servletRequest,servletResponse);
//        //���к�ͨ��ִ��response����ش���
//        System.out.println("�����");
//    }
//
//    @Override
//    public void destroy() {
//
//    }
//}