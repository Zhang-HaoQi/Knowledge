package servlet_ready_lession.servlet2021_12_21.response;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

/**
 * @Classname VerifyCodingDemo
 * @Description TODO
 * @Date 2021/12/20 22:15
 * @Created by DELL
 */
@WebServlet("/reqs/verify")
public class VerifyCodingDemo extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //1.����һ���������ڴ��д�ͼƬ����֤��ͼƬ����
        int width =100;
        int height= 50;
        BufferedImage image  =new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);//���ߣ���ʽ
        //2.����ͼƬ
        //2.1��������ɫ
        Graphics graphics = image.getGraphics();//���ʶ���
        graphics.setColor(Color.pink);//���û�����ɫ
        graphics.fillRect(0,0,width,height);//���һ����ɫ�ľ���  ����λ�úʹ�С
        //2.2���߿�
        graphics.setColor(Color.BLUE);//������ɫ
        graphics.drawRect(0,0,width-1,height-1);//���߿�
        //2.3д��֤��
        String str ="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";  //��֤������������ַ�����
        Random random = new Random();//����֤����֤��
        for (int i = 1; i < 5; i++) {
            int s = random.nextInt(str.length());//�����ȡ�ַ����ĽǱ꣬�������ַ������ȵķ�Χ��
            char c = str.charAt(s);//��ȡ������ַ�
            graphics.drawString(c+"",i*20,25);//�ַ��������ݺ�λ��
        }
        //2.4��������
        graphics.setColor(Color.black);
        for (int i = 0; i < 10; i++) {
            int x1 = random.nextInt(100);
            int x2 = random.nextInt(100);
            int y1 = random.nextInt(50);
            int y2 = random.nextInt(50);
            graphics.drawLine(x1,y1,x2,y2);
        }
        //3.��ͼƬ���뵽ҳ��չʾ
        ImageIO.write(image,"jpg",response.getOutputStream());//������󣬺�׺������������
    }
}
