package servlet;

import entity.Student;
import service.IStudentService;
import service.impl.StudentServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;


/**
 * @Classname Login
 * @Description TODO
 * @Date 2021/12/20 11:25
 * @Created by DELL
 */
@WebServlet("/loginThird")
public class LoginThird extends HttpServlet {
    private static final IStudentService studentService = new StudentServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, IOException {
        //1.���ñ���
        request.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        //2.��ȡsession
//        HttpSession session = request.getSession();
//        Student user = (Student) session.getAttribute("user");
//        if (user != null) {
//            //���û����ݣ��ض�����ҳ
//            response.sendRedirect("/students_system/all");
//            return;
//        }
        //3.��ȡ�������
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        if ("".equals(username) || "".equals(password) || username == null || password == null) {
            //ʧ��
            request.setAttribute("message", "ʧ��:�û��������벻��Ϊnull");
            request.getRequestDispatcher("/login/loginThird.jsp").forward(request, response);
            return;
        }
        //3.��ѯ���ݿ�
        Student student = studentService.login(username, password);
        if (student == null) {
            //ʧ��
            request.setAttribute("message", "ʧ��:�û�������");
            request.getRequestDispatcher("/login/loginThird.jsp").forward(request, response);
        } else {
            //�ض�����ҳ
            HttpSession session= request.getSession();
            session.setAttribute("user",student);
            Cookie cookie = new Cookie("JSESSIONID",session.getId());
            cookie.setMaxAge(60*60);
            response.addCookie(cookie);
            response.sendRedirect("/students_system/all");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, IOException {
        this.doPost(request, response);
    }

}
