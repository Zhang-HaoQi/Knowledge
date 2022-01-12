package utils;




import com.alibaba.druid.pool.DruidDataSourceFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DRUIDDateSource {
    //1.�����Ա���� DateSource
    private static DataSource ds;

    static {
        try {
            //1.���������ļ�
            Properties pro = new Properties();
            pro.load(DRUIDDateSource.class.getClassLoader().getResourceAsStream("druid.properties"));
            ds = DruidDataSourceFactory.createDataSource(pro);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws Exception {
        Connection connection = null;
        try {
            connection = ds.getConnection();
            return connection;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    //��ȡ���ӳصķ���
    public static DataSource getDataSource() {
        return ds;
    }

    //�ر����ݿ����ӵķ�װ��ͨ����̬���Σ���rs��pstmt��con����˳�򴫽���
    public static void closeAll(AutoCloseable... closeables) {
        for (AutoCloseable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }

}