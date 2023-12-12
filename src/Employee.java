import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


class Employee {
    private String fullName;
    private int age;
    private double salary;

    public Employee(String fullName, int age, double salary) {
        this.fullName = fullName;
        this.age = age;
        this.salary = salary;
    }

    public String getFullName() {
        return fullName;
    }

    public int getAge() {
        return age;
    }

    public double getSalary() {
        return salary;
    }

    @Override
    public String toString() {
        return "ФИО: " + fullName + ", Возраст: " + age + ", ЗП: " + salary;
    }
}