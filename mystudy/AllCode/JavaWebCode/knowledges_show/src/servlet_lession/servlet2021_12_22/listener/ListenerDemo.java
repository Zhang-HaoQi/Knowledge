package servlet_lession.servlet2021_12_22.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ListenerDemo implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        //������Դ
        System.out.println("�����������ˡ���������ִ���ˡ�");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        //�ͷ���Դ.
        System.out.println("�������ر��ˡ���������ִ���ˡ�");
    }
}
