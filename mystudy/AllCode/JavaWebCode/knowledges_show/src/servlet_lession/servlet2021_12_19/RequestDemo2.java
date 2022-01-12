package servlet_lession.servlet2021_12_19;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

/**
 * @Classname RequestDemo02
 * @Description TODO
 * @Date 2021/12/19 14:58
 * @Created by DELL
 */
@WebServlet("/req/demo2")
public class RequestDemo2 extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //һ�����ݲ������ƻ�ȡ����ֵ
        String parameter= request.getParameter("username");
       System.out.println(parameter); //1427421650
        //�������ݲ���ֵ���ƻ�ȡ����ֵ����  ����ڸ�ѡ��
        String[] hobbies = request.getParameterValues("hobby");
        for (String hobby:hobbies) {
           System.out.println(hobby);//�������������
        }
        //������ȡ���в�������
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()){
            String name= parameterNames.nextElement();
           System.out.println(name); //username password hobby
        }
        //�ġ���ȡ���в������Ƽ������ֵ���Լ�ֵ�Եķ�ʽ����
        Map<String, String[]> parameterMap = request.getParameterMap();
        //��������1
        for(String key:parameterMap.keySet()) {
            System.out.println(key);
            for (int a=0;a<parameterMap.get(key).length;a++){
                System.out.println(parameterMap.get(key)[a]);
            }
            System.out.println(".......................");
        }
        //��������2
        Map<String, String[]> parameterMap1= request.getParameterMap();
        Set<String> keyset = parameterMap1.keySet();
        for (String name: keyset) {
            //��ȡ����ֵ
            String [] values = parameterMap.get(name);
            System.out.println(name);
            for (String value:values) {
                System.out.println(value);
            }
            System.out.println("...............................");

        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request,response);
    }
}
