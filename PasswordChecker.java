import java.io.*;
import java.net.*;
import java.util.*;

class HashNode {
    String word;
    int lineNumber;
    HashNode next;

    // store word and line in node
    public HashNode(String word, int lineNumber) {
        this.word = word;
        this.lineNumber = lineNumber;
        this.next = null;
    }
}

class SeparateChainingHashTable {
    private HashNode[] table;
    private int size;
    private int comparisons;
    private boolean useOldHash;

    // make hash table
    public SeparateChainingHashTable(boolean useOldHash) {
        this.size = 1000; // size fixed to 1000
        this.table = new HashNode[size];
        this.comparisons = 0;
        this.useOldHash = useOldHash;
    }

    // old hash
    private int oldHashCode(String str) {
        int hash = 0;
        int skip = Math.max(1, str.length() / 8);
        for (int i = 0; i < str.length(); i += skip) {
            hash = (hash * 37) + str.charAt(i);
        }
        return Math.abs(hash) % size;
    }

    // new hash
    private int newHashCode(String str) {
        int hash = 0;
        for (int i = 0; i < str.length(); i++) {
            hash = (hash * 31) + str.charAt(i);
        }
        return Math.abs(hash) % size;
    }

    // add word to table
    public void insert(String word, int lineNumber) {
        int index = useOldHash ? oldHashCode(word) : newHashCode(word);
        HashNode newNode = new HashNode(word, lineNumber);

        if (table[index] == null) {
            table[index] = newNode;
        } else {
            newNode.next = table[index];
            table[index] = newNode;
        }
    }

    // look for word and count checks
    public boolean search(String word) {
        comparisons = 0;
        int index = useOldHash ? oldHashCode(word) : newHashCode(word);
        HashNode current = table[index];

        while (current != null) {
            comparisons++;
            if (current.word.equals(word)) return true;
            current = current.next;
        }
        return false;
    }

    // get number of checks done
    public int getComparisons() {
        return comparisons;
    }
}

class LinearProbingHashTable {
    private String[] keys;
    private Integer[] values;
    private int size;
    private int comparisons;
    private boolean useOldHash;

    // make hash table
    public LinearProbingHashTable(boolean useOldHash) {
        this.size = 20000;
        this.keys = new String[size];
        this.values = new Integer[size];
        this.comparisons = 0;
        this.useOldHash = useOldHash;
    }

    // old hash
    private int oldHashCode(String str) {
        int hash = 0;
        int skip = Math.max(1, str.length() / 8);
        for (int i = 0; i < str.length(); i += skip) {
            hash = (hash * 37) + str.charAt(i);
        }
        return Math.abs(hash) % size;
    }

    // new hash
    private int newHashCode(String str) {
        int hash = 0;
        for (int i = 0; i < str.length(); i++) {
            hash = (hash * 31) + str.charAt(i);
        }
        return Math.abs(hash) % size;
    }

    // add word to table
    public void insert(String word, int lineNumber) {
        int index = useOldHash ? oldHashCode(word) : newHashCode(word);

        while (keys[index] != null) {
            index = (index + 1) % size; // move to next spot
        }

        keys[index] = word;
        values[index] = lineNumber;
    }

    // look for word and count checks
    public boolean search(String word) {
        comparisons = 0;
        int index = useOldHash ? oldHashCode(word) : newHashCode(word);

        while (keys[index] != null) {
            comparisons++;
            if (keys[index].equals(word)) return true;
            index = (index + 1) % size;

            // if we're back at start, stop
            if (index == (useOldHash ? oldHashCode(word) : newHashCode(word))) break;
        }
        return false;
    }

    // get number of checks done
    public int getComparisons() {
        return comparisons;
    }
}

public class PasswordChecker {
    private SeparateChainingHashTable oldHashSC;
    private SeparateChainingHashTable newHashSC;
    private LinearProbingHashTable oldHashLP;
    private LinearProbingHashTable newHashLP;

    // start up tables
    public PasswordChecker() {
        oldHashSC = new SeparateChainingHashTable(true);
        newHashSC = new SeparateChainingHashTable(false);
        oldHashLP = new LinearProbingHashTable(true);
        newHashLP = new LinearProbingHashTable(false);
        loadDictionary();
    }

    // get words from url
    private void loadDictionary() {
        try {
            URL url = new URL("https://www.mit.edu/~ecprice/wordlist.10000");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

            String word;
            int lineNumber = 1;
            while ((word = reader.readLine()) != null) {
                oldHashSC.insert(word, lineNumber);
                newHashSC.insert(word, lineNumber);
                oldHashLP.insert(word, lineNumber);
                newHashLP.insert(word, lineNumber);
                lineNumber++;
            }
            reader.close();
        } catch (Exception e) {
            System.err.println("error loading dictionary: " + e.getMessage());
        }
    }

    // check if password is good
    public boolean isStrong(String password) {
        if (password.length() < 8) return false; // too short
        if (oldHashSC.search(password.toLowerCase()) || newHashSC.search(password.toLowerCase())) return false; // found in list

        for (int i = 0; i < password.length(); i++) {
            if (Character.isDigit(password.charAt(i))) {
                String wordPart = password.substring(0, i).toLowerCase();
                if (oldHashSC.search(wordPart) || newHashSC.search(wordPart)) return false; // word+number found
            }
        }
        return true;
    }

    // test password and show results
    public void checkPassword(String password) {
        System.out.println("\n=== testing password: " + password + " ===");
        System.out.println("strong: " + (isStrong(password) ? "yes" : "no"));

        String testWord = password.toLowerCase();
        oldHashSC.search(testWord);
        System.out.println("separate chaining (old hash) checks: " + oldHashSC.getComparisons());

        newHashSC.search(testWord);
        System.out.println("separate chaining (new hash) checks: " + newHashSC.getComparisons());

        oldHashLP.search(testWord);
        System.out.println("linear probing (old hash) checks: " + oldHashLP.getComparisons());

        newHashLP.search(testWord);
        System.out.println("linear probing (new hash) checks: " + newHashLP.getComparisons());
    }

    // run program
    public static void main(String[] args) {
        PasswordChecker checker = new PasswordChecker();
        Scanner scanner = new Scanner(System.in);

        System.out.println("pick an option:");
        System.out.println("1. run test passwords");
        System.out.println("2. test your own password");

        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 1) {
            String[] passwords = {"account8", "accountability", "9a$D#qW7!uX&Lv3zT", "B@k45*W!c$Y7#zR9P", "X$8vQ!mW#3Dz&Yr4K5"};
            for (String password : passwords) checker.checkPassword(password);
        } else if (choice == 2) {
            System.out.println("enter password 'quit' to stop");
            while (true) {
                System.out.print("\npassword: ");
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("quit")) break;
                checker.checkPassword(input);
            }
        }

        scanner.close();
    }
}