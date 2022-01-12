package servlet_ready_lession.servlet2021_12_21.filehandle;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @Classname dd
 * @Description TODO
 * @Date 2021/12/20 22:48
 * @Created by DELL
 */
@WebServlet("/file")
public class FileHandle extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //1.��ȡ����������ļ�����
        String fileName = request.getParameter("filename");
        System.out.println(fileName); //photo

        //2.ʹ���ֽ����������ļ����ڴ�
        //2.1�ҵ��ļ����ڵķ�����·��
        ServletContext servletContext = request.getServletContext();
        String realPath = servletContext.getRealPath("/img/" + fileName);//ͼƬ������·��
        //2.2ʹ���ֽ�������
        FileInputStream fis = new FileInputStream(realPath);
        //�Լ����Ϊ��ͨ���˲��裬���Ի�ȡ����������ļ����浽���ص�Ȩ��


        //3.����response��Ӧͷ
        //3.1������Ӧͷ���������ͣ�content-type  //��Ϊ����Ҳ��֪���ļ������ͣ�������Ҫ���á�
        String mimeType = servletContext.getMimeType(fileName);
        response.setHeader("content-type", mimeType);
        //3.1.1��������ļ�������


        //��ȡuser-agent����ͷ
        String agent = request.getHeader("user-agent");
        //3.2������Ӧͷ�Ĵ򿪷�ʽ
        response.setHeader("content-disposition", "attachment;filename=" +  DownLoadUtils.getFileName(agent, fileName));//filename���õ�Ϊ������ʾ�������

        //4.��������������д�����������
        ServletOutputStream sos = response.getOutputStream();
        byte[] buff = new byte[1024 * 8];
        int len = 0;
        while ((len = fis.read(buff)) != -1) {
            sos.write(buff, 0, len);
        }
        fis.close();//�������ر�
    }
}
