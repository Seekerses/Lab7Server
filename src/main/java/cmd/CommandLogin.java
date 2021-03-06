package cmd;

import BD.DataHandler;
import BD.DataUserManager;
import clientserverdata.User;

import java.util.Scanner;

public class CommandLogin implements Command, Preparable, Registerable {

    private String login;
    private String password;
    private static final long serialVersionUID = 1337000051L;

    @Override
    public String execute(String[] args) {
        DataHandler handler = DataHandler.getInstance();
        DataUserManager userManager = new DataUserManager(handler);
        User user = new User();
        user.setUsername(login);
        user.setPassword(password);
        if (userManager.checkUserByUsernameAndPassword(user)) {System.out.println("Пользователь " +
                user.getUsername() + " авторизован.");
        return "Approved," + login + "," + password;
        }else{
            return "Wrong," + login + "," + password;
        }
    }

    @Override
    public void prepare(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter login:\n>");
        login = scanner.next().trim();
        System.out.println("Enter password:\n>");
        password = scanner.next().trim();
    }
}
