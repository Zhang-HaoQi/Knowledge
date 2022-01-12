package factory.methodfactory;

/**
 * @Classname P30
 * @Description TODO
 * @Date 2022/1/7 14:57
 * @Created by zhq
 */
public class P30 implements HuaWeiPhone {
    private String name;
    private double price;
    private String color;
    //等等各种属性


    public P30(String name, double price, String color) {
        this.name = name;
        this.price = price;
        this.color = color;
    }

    @Override
    public void call() {
        System.out.println("this is HuaWei P30");

    }
}
