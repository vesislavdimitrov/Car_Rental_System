package view;

import db.AdminController;
import db.ClientController;
import logging.LoggerManager;
import users.Admin;
import users.Client;
import users.util.Registration;

import java.io.Console;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.logging.Logger;

public class LogInMenu{

    private static final Logger LOGGER = LoggerManager.getLogger(LogInMenu.class.getName());
    private static final Admin admin = Admin.getInstance();

    public static void printInitialPrompt(){
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome!");
        System.out.println("(1) -> Login");
        System.out.println("(2) -> Register");

        int choice = scanner.nextInt();
        if (choice == 1) {
            printLoginPrompt();
        } else if (choice == 2) {
            accountCreationPrompts();
            System.out.println("Account created!");
            printLoginPrompt();
        } else {
            System.err.println("Invalid choice!");
            printInitialPrompt();
        }
    }
    public static void printLoginPrompt() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter login info:");
        String[] loginInfo = scanner.nextLine().split(" ");
        String username = loginInfo[0];
        String password = loginInfo[1];

        if (username.equals(admin.getUsername()) && password.equals(admin.getPassword())) {
            openAdminView();
        } else {
            Client client = ClientController.getClientByUsername(username);
            if (client != null && client.getPassword().equals(password)) {
                openClientView(client);
            } else if (client != null) {
                System.err.println("Wrong password, please try again.");
                printLoginPrompt();
            } else {
                System.err.println("No account found with that info");
                accountCreationChoice();
            }
        }
    }
    private static void accountCreationChoice() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Create an account?");
        System.out.println("(1) -> Yes\n(2) -> No");

        int choice;
        do {
            choice = scanner.nextInt();
            if (choice == 1) {
                accountCreationPrompts();
                System.out.println("Account created!");
                printLoginPrompt();
                break;
            } else {
                System.err.println("Invalid choice!");
            }
        } while (choice != 2);
    }
    private static void accountCreationPrompts(){
       try{
           Scanner scan = new Scanner(System.in);
           Console console = System.console();

           System.out.println("Enter a username: ");
           String username = scan.nextLine();

           System.out.println("Enter a password: ");
           String password;
           if (console != null) {
               char[] passwordChars = console.readPassword();
               password = new String(passwordChars);
           } else {
               password = scan.nextLine();
           }

           System.out.println("Enter birth date (YYYY-MM-DD): ");
           String birthDateString = scan.nextLine();
           LocalDate birthDate = LocalDate.parse(birthDateString);

           System.out.println("Enter an e-mail address: ");
           String eMail = scan.nextLine();

           Registration.registerUser(username,password,birthDate,eMail);
       }catch (IllegalArgumentException e){
           LOGGER.warning(e.getMessage());
           accountCreationPrompts();
       }
    }

    private static void openClientView(Client client){
        System.out.println("Welcome, "+client.getUsername()+'!');
        ClientController clientController = new ClientController(client);
        Dashboard clientDashboard = new ClientDashboard(clientController);
        clientDashboard.printMenu();
        clientDashboard.getOptions();
    }

    private static void openAdminView(){
        System.out.println("Welcome, overlord!");
        AdminController adminController = new AdminController();
        Dashboard adminDashboard = new AdminDashboard(adminController);
        adminDashboard.printMenu();
        adminDashboard.getOptions();
    }
}
