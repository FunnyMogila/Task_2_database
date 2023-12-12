import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.sql.*;

public class DatabaseEmulator {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/task2";
    private static final String USER = "root";
    private static final String PASSWORD = "12cc14sd";

    private Map<String, Department> departments;

    public DatabaseEmulator() {
        this.departments = new HashMap<>();
    }

    public void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS departments (name VARCHAR(255) PRIMARY KEY)");
            statement.execute("CREATE TABLE IF NOT EXISTS employees (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "department_name VARCHAR(255)," +
                    "full_name VARCHAR(255)," +
                    "age INT," +
                    "salary DOUBLE," +
                    "FOREIGN KEY (department_name) REFERENCES departments(name))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addDepartment(String name) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO departments (name) VALUES (?)")) {
            preparedStatement.setString(1, name);
            preparedStatement.executeUpdate();
            departments.put(name, new Department(name));
            System.out.println("Отдел добавлен: " + name);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeDepartment(String name) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM departments WHERE name = ?")) {
            preparedStatement.setString(1, name);
            preparedStatement.executeUpdate();
            departments.remove(name);
            System.out.println("Отдел удален: " + name);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void showAllDepartments() {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM departments")) {
            System.out.println("Список всех отделов:");
            while (resultSet.next()) {
                System.out.println(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addEmployee(String deptName, String fullName, int age, double salary) {
        if (departments.containsKey(deptName)) {
            try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement(
                         "INSERT INTO employees (department_name, full_name, age, salary) VALUES (?, ?, ?, ?)")) {
                preparedStatement.setString(1, deptName);
                preparedStatement.setString(2, fullName);
                preparedStatement.setInt(3, age);
                preparedStatement.setDouble(4, salary);
                preparedStatement.executeUpdate();
                departments.get(deptName).addEmployee(new Employee(fullName, age, salary));
                System.out.println("Сотрудник добавлен в отдел " + deptName);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Отдел с именем " + deptName + " не существует.");
        }
    }

    public void removeEmployee(String deptName, String employeeFullName) {
        if (departments.containsKey(deptName)) {
            Department department = departments.get(deptName);

            try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement(
                         "DELETE FROM employees WHERE department_name = ? AND full_name = ?")) {
                preparedStatement.setString(1, deptName);
                preparedStatement.setString(2, employeeFullName);
                preparedStatement.executeUpdate();

                List<Employee> employees = department.getEmployees();
                employees.removeIf(employee -> employee.getFullName().equals(employeeFullName));

                System.out.println("Сотрудник " + employeeFullName + " удален из отдела " + deptName);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Отдел с именем " + deptName + " не существует.");
        }
    }

    public void showAllEmployeesInDepartment(String deptName) {
        if (departments.containsKey(deptName)) {
            Department department = departments.get(deptName);

            try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement(
                         "SELECT * FROM employees WHERE department_name = ?")) {
                preparedStatement.setString(1, deptName);
                ResultSet resultSet = preparedStatement.executeQuery();

                System.out.println("Список сотрудников в отделе " + deptName + ":");
                while (resultSet.next()) {
                    String fullName = resultSet.getString("full_name");
                    int age = resultSet.getInt("age");
                    double salary = resultSet.getDouble("salary");

                    Employee employee = new Employee(fullName, age, salary);
                    department.addEmployee(employee);

                    System.out.println(employee);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Отдел с именем " + deptName + " не существует.");
        }
    }

    public void showDepartmentSalary(String deptName) {
        if (departments.containsKey(deptName)) {
            Department department = departments.get(deptName);

            try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement(
                         "SELECT SUM(salary) AS total_salary FROM employees WHERE department_name = ?")) {
                preparedStatement.setString(1, deptName);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    double totalSalary = resultSet.getDouble("total_salary");
                    System.out.println("Сумма зарплат в отделе " + deptName + ": " + totalSalary);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Отдел с именем " + deptName + " не существует.");
        }
    }

    public static void main(String[] args) {
        DatabaseEmulator database = new DatabaseEmulator();
        database.initializeDatabase();

        Scanner scanner = new Scanner(System.in);


        while (true) {
            System.out.println("1. Добавить отдел");
            System.out.println("2. Удалить отдел");
            System.out.println("3. Показать все отделы");
            System.out.println("4. Добавить сотрудника");
            System.out.println("5. Удалить сотрудника");
            System.out.println("6. Показать всех сотрудников в отделе");
            System.out.println("7. Показать сумму зарплат в отделе");
            System.out.println("8. Выход");
            System.out.println("Выберите действие: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.println("Введите название отдела:");
                    String departmentName = scanner.nextLine();
                    database.addDepartment(departmentName);
                    break;

                case 2:
                    System.out.println("Введите название отдела для удаления:");
                    String departmentToDelete = scanner.nextLine();
                    database.removeDepartment(departmentToDelete);
                    break;

                case 3:
                    database.showAllDepartments();
                    break;

                case 4:
                    System.out.println("Введите название отдела:");
                    String deptName = scanner.nextLine();
                    System.out.println("Введите ФИО сотрудника:");
                    String fullName = scanner.nextLine();
                    System.out.println("Введите возраст сотрудника:");
                    int age = scanner.nextInt();
                    System.out.println("Введите ЗП сотрудника:");
                    double salary = scanner.nextDouble();
                    database.addEmployee(deptName, fullName, age, salary);
                    break;

                case 5:
                    System.out.println("Введите название отдела:");
                    String deptToRemoveEmployee = scanner.nextLine();
                    System.out.println("Введите ФИО сотрудника для удаления:");
                    String employeeToRemove = scanner.nextLine();
                    database.removeEmployee(deptToRemoveEmployee, employeeToRemove);
                    break;

                case 6:
                    System.out.println("Введите название отдела:");
                    String deptToShowEmployees = scanner.nextLine();
                    database.showAllEmployeesInDepartment(deptToShowEmployees);
                    break;

                case 7:
                    System.out.println("Введите название отдела:");
                    String deptToShowSalary = scanner.nextLine();
                    database.showDepartmentSalary(deptToShowSalary);
                    break;

                case 8:
                    System.out.println("Программа завершена.");
                    System.exit(0);

                default:
                    System.out.println("Некорректный ввод. Пожалуйста, выберите существующее действие.");
            }
        }
    }

}