package com.use;//用于定义当前类所在的包,不声明就不能import
import com.ming.Dog;//像是这样的引入，实际上只会引入这个包的一个类
//import com.ming.*;//像是这样的引入，会引入这个包的全部类
//只能有一个public类，且名字必须和文件名一致
public class Test {
    public static void main(String[] args) {
        //使用之前必须先new
        Dog d = new Dog();
        //继承自Object的默认用法
        System.out.println(d);
        //使用前可以加上前缀来做
        com.qiang.Dog d2 = new com.qiang.Dog();
        System.out.println(d2);

    }
}
