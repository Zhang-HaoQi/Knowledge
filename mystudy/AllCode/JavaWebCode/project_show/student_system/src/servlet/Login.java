package servlet;

import com.mysql.cj.util.StringUtils;
import entity.Student;
import service.IStudentService;
import service.impl.StudentServiceImpl;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;


/**
 * @Classname Login
 * @Description TODO
 * @Date 2021/12/20 11:25
 * @Created by DELL
 */
@WebServlet("/login")
public class Login extends HttpServlet {
    private static final IStudentService studentService = new StudentServiceImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, IOException {
        //1.���ñ���
        request.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        //2.��ȡ�������
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        if ("".equals(username) || "".equals(password) || username == null || password == null) {
            request.setAttribute("message", "fail");
            request.getRequestDispatcher("/login/fail.jsp").forward(request, response);
            return;
        }
        //3.��ѯ���ݿ�
        Student student = studentService.login(username, password);
        if (student == null) {
            request.setAttribute("message", "fail");
            request.getRequestDispatcher("/login/fail.jsp").forward(request, response);
        } else {
            //��ȡ����ѧ��
            List<Student> allStundents = studentService.getAllStundents();
            request.setAttribute("message", allStundents);
//            //��ȡ��ǰѧ��
//            request.setAttribute("message", student);
            request.getRequestDispatcher("/login/homepage.jsp").forward(request, response);
        }
    }

}
