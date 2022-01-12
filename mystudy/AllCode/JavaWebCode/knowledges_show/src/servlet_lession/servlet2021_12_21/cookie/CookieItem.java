package servlet_lession.servlet2021_12_21.cookie;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @Classname CookieItem
 * @Description TODO
 * @Date 2021/12/21 20:59
 * @Created by DELL
 */
@WebServlet("/cookie/visit")
public class CookieItem extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //������Ӧ��Ϣ��ı����ʽ
        response.setContentType("text/html;charset=utf-8");
        //1���ж��Ƿ���cookie  �Ȼ�ȡ���е�cookie��
        Cookie[] cookies = request.getCookies();
        //1.1����һ�������Ƿ���lastname��cookie�ж�
        boolean flag = false;
        //2.����cookie����
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies
            ) {
                //3.��ȡcookie������
                String name = cookie.getName();
                //4.�ж������Ƿ��ǣ�lastname
                if ("lastTime".equals(name)) {
                    //�и�cookie ��ʾ���ǵ�һ�η���
                    flag = true;
                    //��Ӧ����
                    //��ȡcookie������
                    String value = cookie.getValue();
                    System.out.println("����ǰ" + value);
                    //URL����
                    value = URLDecoder.decode(value, "utf-8");
                    System.out.println("�����" + value);
                    response.getWriter().write("<h1>��ӭ���������ϴεķ���ʱ��Ϊ" + value + "</h1>");//�˴�Ϊ������Ϣ����Ҫ������Ӧ��Ϣ��


                    //����cookie��ֵ�����ڵ�ֵ
                    //��ȡ��ǰʱ����ַ�������������cookie��ֵ�����·���
                    Date date = new Date();
                    System.out.println("�޸�ǰ" + date);
                    //����ʱ��ĸ�ʽ��Ĭ�ϵ�Ϊ������ʱ���ʽ
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy��MM��dd��  HH��mm��ss");//��������ʱ��ĸ�ʽ
                    //����ʱ�����������ַ�ʽ����ѡ��һ��
//                    sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
//                    sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
                    sdf.setTimeZone(TimeZone.getTimeZone("Etc/GMT-8"));
                    String str_date = sdf.format(date);
                    System.out.println("����ǰ" + str_date);
                    //URL����
                    str_date = URLEncoder.encode(str_date, "utf-8");
                    System.out.println("�����" + str_date);
                    //����cookie
                    cookie.setValue(str_date);
                    //����cookie�Ĵ��ʱ��.���һ����
                    cookie.setMaxAge(60 * 60 * 24 * 30);
                    //����cookie
                    response.addCookie(cookie);

                    //�ҵ�����Ϊlastname��cookie֮�󣬾Ͳ�Ҫѭ����
                    break;
                }
            }
        }
        if (cookies == null || cookies.length == 0 || flag == false) {
            Date date = new Date();
            //����ʱ��ĸ�ʽ��Ĭ�ϵ�Ϊ������ʱ���ʽ
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy��mm��dd��  HH��mm��ss");//��������ʱ��ĸ�ʽ
            //����ʱ�����������ַ�ʽ����ѡ��һ��
//                    sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
//                    sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            sdf.setTimeZone(TimeZone.getTimeZone("Etc/GMT-8"));
            //��ʱ�������ַ���
            String str_date = sdf.format(date);
            //URL����
            str_date = URLEncoder.encode(str_date, "utf-8");
            System.out.println("�����" + str_date);
            //����cookie
            Cookie cookie = new Cookie("lastTime", str_date);
            cookie.setValue(str_date);
            //����cookie�Ĵ��ʱ��.���һ����
            cookie.setMaxAge(60 * 60 * 24 * 30);
            //����cookie
            response.addCookie(cookie);
            //��Ӧ����
            //��ȡcookie������
            String value = cookie.getValue();
            //URL����
            value = URLDecoder.decode(value, "utf-8");
            System.out.println("�����" + value);
            response.getWriter().write("<h1>��ã���ӭ���״η���</h1>" + value);//�˴�Ϊ������Ϣ����Ҫ������Ӧ��Ϣ��
        }
    }

}
