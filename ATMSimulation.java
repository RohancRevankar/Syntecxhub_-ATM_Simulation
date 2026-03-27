import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// Represents a bank account
class BankAccount {
    private String accountHolder;
    private String accountNumber;
    private int pin;
    private double balance;
    private List<String> transactionHistory;
    private int failedAttempts;
    private boolean locked;

    public BankAccount(String accountHolder, String accountNumber, int pin, double balance) {
        this.accountHolder = accountHolder;
        this.accountNumber = accountNumber;
        this.pin = pin;
        this.balance = balance;
        this.transactionHistory = new ArrayList<>();
        this.failedAttempts = 0;
        this.locked = false;
    }

    public boolean verifyPin(int enteredPin) {
        if (locked) return false;
        if (this.pin == enteredPin) {
            failedAttempts = 0;
            return true;
        } else {
            failedAttempts++;
            if (failedAttempts >= 3) locked = true;
            return false;
        }
    }

    public boolean isLocked() { return locked; }
    public int getRemainingAttempts() { return Math.max(0, 3 - failedAttempts); }
    public String getAccountHolder() { return accountHolder; }
    public String getAccountNumber() { return accountNumber; }
    public double getBalance() { return balance; }

    public boolean deposit(double amount) {
        if (amount <= 0) return false;
        balance += amount;
        transactionHistory.add(String.format("DEPOSIT   + Rs. %.2f  | Balance: Rs. %.2f", amount, balance));
        return true;
    }

    public boolean withdraw(double amount) {
        if (amount <= 0 || amount > balance) return false;
        balance -= amount;
        transactionHistory.add(String.format("WITHDRAW  - Rs. %.2f  | Balance: Rs. %.2f", amount, balance));
        return true;
    }

    public boolean changePin(int oldPin, int newPin) {
        if (this.pin != oldPin) return false;
        this.pin = newPin;
        transactionHistory.add("PIN CHANGED successfully");
        return true;
    }

    public void printStatement() {
        System.out.println("\n========== MINI STATEMENT ==========");
        System.out.printf("Account: %s | Holder: %s%n", accountNumber, accountHolder);
        System.out.println("-------------------------------------");
        if (transactionHistory.isEmpty()) {
            System.out.println("No transactions yet.");
        } else {
            int start = Math.max(0, transactionHistory.size() - 5);
            for (int i = start; i < transactionHistory.size(); i++) {
                System.out.println(transactionHistory.get(i));
            }
        }
        System.out.println("=====================================");
    }
}

// ATM machine logic
class ATM {
    private List<BankAccount> accounts;
    private BankAccount currentAccount;
    private boolean sessionActive;
    private Scanner scanner;

    public ATM() {
        accounts = new ArrayList<>();
        scanner = new Scanner(System.in);
        sessionActive = false;
        seedAccounts();
    }

    private void seedAccounts() {
        accounts.add(new BankAccount("Rohan Revankar", "ACC001", 1234, 50000.00));
        accounts.add(new BankAccount("Pratvik Kumar",  "ACC002", 5678, 25000.00));
        accounts.add(new BankAccount("Anita Sharma",   "ACC003", 9999, 100000.00));
    }

    private BankAccount findAccount(String accNumber) {
        for (BankAccount acc : accounts) {
            if (acc.getAccountNumber().equalsIgnoreCase(accNumber)) return acc;
        }
        return null;
    }

    public void start() {
        printBanner();
        while (true) {
            if (!sessionActive) {
                showLoginMenu();
            } else {
                showMainMenu();
            }
        }
    }

    private void printBanner() {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║       SYNTECXHUB BANK ATM            ║");
        System.out.println("║         Welcome! Please Insert Card  ║");
        System.out.println("╚══════════════════════════════════════╝");
    }

    private void showLoginMenu() {
        System.out.println("\n[1] Insert Card (Login)");
        System.out.println("[2] Exit ATM");
        System.out.print("Choose: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1": login(); break;
            case "2":
                System.out.println("\nThank you for using Syntecxhub ATM. Goodbye!");
                System.exit(0);
            default:
                System.out.println("Invalid option.");
        }
    }

    private void login() {
        System.out.print("\nEnter Account Number: ");
        String accNum = scanner.nextLine().trim();
        BankAccount acc = findAccount(accNum);

        if (acc == null) {
            System.out.println("Account not found.");
            return;
        }
        if (acc.isLocked()) {
            System.out.println("Your account is LOCKED due to multiple failed PIN attempts.");
            System.out.println("Please contact your bank branch.");
            return;
        }

        System.out.print("Enter PIN: ");
        int pin;
        try { pin = Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("Invalid PIN."); return; }

        if (acc.verifyPin(pin)) {
            currentAccount = acc;
            sessionActive = true;
            System.out.println("\n✓ Login successful! Welcome, " + acc.getAccountHolder());
        } else {
            if (acc.isLocked()) {
                System.out.println("Account LOCKED after 3 failed attempts. Contact your branch.");
            } else {
                System.out.println("Incorrect PIN. Attempts remaining: " + acc.getRemainingAttempts());
            }
        }
    }

    private void showMainMenu() {
        System.out.println("\n========== ATM MENU ==========");
        System.out.println("[1] Check Balance");
        System.out.println("[2] Deposit");
        System.out.println("[3] Withdraw");
        System.out.println("[4] Mini Statement");
        System.out.println("[5] Change PIN");
        System.out.println("[6] Logout");
        System.out.print("Choose: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1": checkBalance(); break;
            case "2": deposit(); break;
            case "3": withdraw(); break;
            case "4": currentAccount.printStatement(); break;
            case "5": changePin(); break;
            case "6": logout(); break;
            default: System.out.println("Invalid option.");
        }
    }

    private void checkBalance() {
        System.out.println("\n--- Balance Enquiry ---");
        System.out.printf("Account: %s%n", currentAccount.getAccountNumber());
        System.out.printf("Holder : %s%n", currentAccount.getAccountHolder());
        System.out.printf("Balance: Rs. %.2f%n", currentAccount.getBalance());
    }

    private void deposit() {
        System.out.print("\nEnter amount to deposit: Rs. ");
        try {
            double amount = Double.parseDouble(scanner.nextLine().trim());
            if (currentAccount.deposit(amount)) {
                System.out.printf("✓ Rs. %.2f deposited. New Balance: Rs. %.2f%n", amount, currentAccount.getBalance());
            } else {
                System.out.println("Invalid amount. Must be greater than 0.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    private void withdraw() {
        System.out.print("\nEnter amount to withdraw: Rs. ");
        try {
            double amount = Double.parseDouble(scanner.nextLine().trim());
            if (currentAccount.withdraw(amount)) {
                System.out.printf("✓ Rs. %.2f dispensed. Remaining Balance: Rs. %.2f%n", amount, currentAccount.getBalance());
            } else {
                System.out.println("Insufficient balance or invalid amount.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    private void changePin() {
        System.out.print("\nEnter current PIN: ");
        int oldPin, newPin, confirmPin;
        try {
            oldPin     = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter new PIN (4 digits): ");
            newPin     = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Confirm new PIN: ");
            confirmPin = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid PIN format.");
            return;
        }

        if (newPin != confirmPin) {
            System.out.println("PINs do not match.");
            return;
        }
        if (String.valueOf(newPin).length() != 4) {
            System.out.println("PIN must be exactly 4 digits.");
            return;
        }
        if (currentAccount.changePin(oldPin, newPin)) {
            System.out.println("✓ PIN changed successfully.");
        } else {
            System.out.println("Current PIN incorrect.");
        }
    }

    private void logout() {
        System.out.println("\nSession ended. Please collect your card.");
        System.out.println("Goodbye, " + currentAccount.getAccountHolder() + "!");
        currentAccount = null;
        sessionActive = false;
    }
}

public class ATMSimulation {
    public static void main(String[] args) {
        ATM atm = new ATM();
        atm.start();
    }
}