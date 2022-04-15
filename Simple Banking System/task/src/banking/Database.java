package banking;


import org.sqlite.SQLiteDataSource;

import java.sql.*;
import java.util.Random;

public class Database {
    SQLiteDataSource dataSource;

    String cardDeletion = "DELETE FROM card WHERE number = ?";
    String tableCreation = "CREATE TABLE IF NOT EXISTS card(" +
            "id INTEGER," +
            "number TEXT NOT NULL," +
            "pin TEXT NOT NULL," +
            "balance INTEGER DEFAULT 0)";
    String cardExistenceChecking = "SELECT * FROM card WHERE number = ?";
    String cardFetching = "SELECT * FROM card WHERE number = ? AND pin = ?";
    String cardCreation = "INSERT INTO card VALUES (?, ?, ?, ?)";
    String cardBalanceUpdate = "UPDATE card SET balance = balance + ? WHERE number = ?";
    String cardBalanceFetch = "SELECT balance FROM card WHERE number = ?";

    Database(String url) {
        dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        try (Connection con = dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                statement.executeUpdate(tableCreation);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void addCard(Card card) {
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = con.prepareStatement(cardCreation)) {
                preparedStatement.setInt(1, new Random().nextInt(10000000));
                preparedStatement.setString(2, card.getNUMBER());
                preparedStatement.setString(3, card.getPin());
                preparedStatement.setInt(4, 0);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    int getBalance(String number, String pin) {
        if(getCard(number, pin) == null) {
            System.out.println("Wrong number or pin");
            return 0;
        }
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = con.prepareStatement(cardBalanceFetch)) {
                preparedStatement.setString(1, number);
                try (ResultSet balance = preparedStatement.executeQuery()) {
                    if(balance.next()) {
                        return balance.getInt(1);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    boolean checkIfExists(String number) {
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = con.prepareStatement(cardExistenceChecking)) {
                preparedStatement.setString(1, number);

                try (ResultSet cards = preparedStatement.executeQuery()) {
                    return cards.next();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    Card getCard(String number, String pin) {
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = con.prepareStatement(cardFetching)) {
                preparedStatement.setString(1, number);
                preparedStatement.setString(2, pin);

                try (ResultSet cards = preparedStatement.executeQuery()) {
                    if(cards.next()) {
                        int balance = cards.getInt("balance");
                        return new Card(number, pin, balance);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    void deleteCard(String number, String pin) {
        if(getCard(number, pin) == null) {
            System.out.println("Wrong number or pin");
            return;
        }
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = con.prepareStatement(cardDeletion)) {
                preparedStatement.setString(1, number);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void addIncome(String number, String pin, int amount) {

        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = con.prepareStatement(cardBalanceUpdate)) {
                preparedStatement.setInt(1, amount);
                preparedStatement.setString(2, number);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void doTransfer(String senderNumber, String pin, String receiverNumber, int amount) {
        Card senderCard = getCard(senderNumber, pin);

        if(senderCard == null) {
            System.out.println("Wrong number or pin");
            return;
        }
        if(!checkIfExists(receiverNumber)) {
            System.out.println("Such a card does not exist.");
            return;
        }
        if(amount > senderCard.getBalance()) {
            System.out.println("Not enough money!");
            return;
        }
        if(senderNumber.equals(receiverNumber)) {
            System.out.println("You can't transfer money to the same account!");
            return;
        }

        String updateBalance = "UPDATE card SET balance = balance + ? WHERE number = ?";
        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement senderStatement = con.prepareStatement(cardBalanceUpdate);
                PreparedStatement receiverStatement = con.prepareStatement(cardBalanceUpdate)) {

                senderStatement.setInt(1, -amount);
                senderStatement.setString(2, senderNumber);
                senderStatement.executeUpdate();

                receiverStatement.setInt(1, amount);
                receiverStatement.setString(2, receiverNumber);
                receiverStatement.executeUpdate();

                con.commit();
            }
            catch (SQLException e) {
                try {
                    System.err.print("Transaction is being rolled back");
                    con.rollback();
                } catch (SQLException excep) {
                    excep.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }
    }
}
// 4000000105006920