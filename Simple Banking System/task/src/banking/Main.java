package banking;

import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import org.sqlite.SQLiteDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    static Database db;

    public static boolean handleUserMenu(Card card) {
        System.out.println("1. Balance");
        System.out.println("2. Add income");
        System.out.println("3. Do transfer");
        System.out.println("4. Close account");
        System.out.println("5. Log out");
        System.out.println("0. Exit");

        Scanner scanner = new Scanner(System.in);
        int option = scanner.nextInt();

        if(option == 0) {
            System.out.println("\nBye!");
            System.exit(0);
        } else if(option == 1) {
            System.out.println(db.getBalance(card.getNUMBER(), card.getPin()));
        } else if(option == 2) {
            System.out.println("Enter income:");
            int amount = scanner.nextInt();
            db.addIncome(card.getNUMBER(), card.getPin(), amount);
            System.out.println("Income was added!");
        } else if(option == 3) {
            scanner.nextLine();
            System.out.println("Enter card number:");
            String receiverNumber = scanner.nextLine();
            if(!Card.passesLuhnAlgorithm(receiverNumber)) {
                System.out.println("Probably you made a mistake in the card number. Please try again!");
                return true;
            }
            if(!db.checkIfExists(receiverNumber)) {
                System.out.println("Such a card does not exist.");
                return true;
            }
            System.out.println("Enter how much money you want to transfer:");
            int amount = scanner.nextInt();
            db.doTransfer(card.getNUMBER(), card.getPin(), receiverNumber, amount);

        } else if (option == 4) {
            db.deleteCard(card.getNUMBER(), card.getPin());
            System.out.println("The account has been closed!");
            return false;

        } else if (option == 5) {
            System.out.println("You have successfully logged out!");
            return false;
        }

        return true;
    }

    public static boolean handleMainMenu() {
        System.out.println("1. Create an account");
        System.out.println("2. Log into account");
        System.out.println("0. Exit");

        Scanner scanner = new Scanner(System.in);
        int option = scanner.nextInt();
        if(option == 0) {
            System.out.println("Bye!");
            System.exit(0);

        }
        else if(option == 1) {
            Card card = new Card();
            db.addCard(card);

            System.out.println("Your card has been created");
            System.out.println("Your card number");
            System.out.println(card.getNUMBER());
            System.out.println("Your card PIN");
            System.out.println(card.getPin());
            System.out.println();
        }
        else if(option == 2) {
            scanner.nextLine();

            System.out.println("Enter your card number:");
            String number = scanner.nextLine();
            System.out.println("Enter your PIN:");
            String pin = scanner.nextLine();

            Card card = db.getCard(number, pin);

            if (card == null) {
                System.out.println("Wrong card number or PIN!");
                return true;
            }
            if (card.getPin().equals(pin)) {
                System.out.println("You have successfully logged in!");
                while(handleUserMenu(card)) {}
            }
        }

        return true;
    }
    public static void main(String[] args) {
        String dbName = null;
        for(int i = 0; i < args.length - 1; i++) {
            if(args[i].equals("-fileName")) {
                dbName = args[i + 1];
                break;
            }
        }

        String url = "jdbc:sqlite:" + dbName;

        db = new Database(url);

        while (handleMainMenu()) {
        }
    }
}