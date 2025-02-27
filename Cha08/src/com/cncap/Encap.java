package com.cncap;

public class Encap {
    public static void main(String[] args) {
        Person N=new Person("wang",24,10000);
        System.out.println(N);
        N.Personmessage();
//        System.out.println(N.Personmessage());
    }
}

class Person{
    public String name;
    private int age;
    private double salary;

    public Person(String name, int age, double salary) {
        setName(name);
        setAge(age);
        setSalary(salary);
    }
//大Void和小void不是一回事，前者要返回null,后者不用返回
    public void Personmessage() {
        System.out.println("Name is "+this.name+" Age is "+this.age+" Salary is "+this.salary+" 1 ");
//        return "Name is "+this.name+" Age is "+this.age+" Salary is "+this.salary;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        if (age <100 && age > 0) {
            this.age = age;
        }
        else {
            System.out.println("Invalid Age");
        }
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        if(name.length()<10 && name.length()>0 ) {
            this.name = name;
        }
        else {
            System.out.println("Invalid Name");
        }
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }
}