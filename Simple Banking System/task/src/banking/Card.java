package banking;

import java.util.Random;

public class Card {
    private final String NUMBER;
    private String pin;
    private int balance;

    private String getNumericString(int n) {
        String numericString = "0123456789";
        StringBuilder sb = new StringBuilder(n);
        for(int i = 0; i < n; i++) {
            int index = (int)(numericString.length() * Math.random());
            sb.append(numericString.charAt(index));
        }
        return sb.toString();
    }

    public static boolean passesLuhnAlgorithm(String result) {
        int sum = 0;
        for(int i = 0; i < result.length() - 1; i++) {
            int number = result.charAt(i) - '0';
            if(i % 2 == 0) number *= 2;
            if(number > 9) number -= 9;
            sum += number;
        }
        int chkSum = (10 - (sum % 10)) % 10;

        return chkSum == (result.charAt(result.length() - 1) - '0');
    }

    public String createNumber() {
        String bin = "400000"; // bank identification number
        String can = getNumericString(9); // customer account number
        String result = bin + can;

        int sum = 0;
        for(int i = 0; i < result.length(); i++) {
            int number = result.charAt(i) - '0';
            if(i % 2 == 0) number *= 2;
            if(number > 9) number -= 9;
            sum += number;
        }
        String chkSum = String.valueOf((10 - (sum % 10)) % 10);
        result += chkSum;

        return result;
    }

    Card(String NUMBER, String pin, int balance) {
        this.NUMBER = NUMBER;
        this.pin = pin;
        this.balance = balance;
    }

    Card() {
        NUMBER = createNumber();
        pin = getNumericString(4);
        balance = 0;
    }

    public String getNUMBER() {
        return NUMBER;
    }

    public String getPin() {
        return pin;
    }

    public int getBalance() {
        return balance;
    }
}
