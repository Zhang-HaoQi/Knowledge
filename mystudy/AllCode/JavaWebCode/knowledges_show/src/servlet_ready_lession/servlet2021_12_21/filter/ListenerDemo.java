package servlet_ready_lession.servlet2021_12_21.filter;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * @Classname ListenerDemo
 * @Description TODO
 * @Date 2021/12/22 9:09
 * @Created by DELL
 */
@WebListener
public class ListenerDemo implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        //������Դ
        System.out.println("��������ִ���ˡ�");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        //�ͷ���Դ
    }
}
