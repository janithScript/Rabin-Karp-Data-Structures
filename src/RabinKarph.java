import java.io.*;
import java.util.*;

public class RabinKarp {

    private static final int d = 256; // Number of characters in input alphabet
    private static final int q = 101;  // A prime number for hashing

    public static void main(String[] args) {
        try {
            // Step 1: Read input files
            String paragraph = readFile("paragraph5.txt");
            List<String> patterns = readPatterns("patterns5.txt");

            // Step 2: Calculate and store hash values for paragraph words
            List<WordHash> wordHashes = calculateHashValues(paragraph);
            writeHashValuesToFile(wordHashes, "hValParak.txt");

            // Step 3: Calculate and store hash values for patterns
            List<WordHash> patternHashes = calculateHashValues(String.join(" ", patterns));
            writeHashValuesToFile(patternHashes, "hValPattk.txt");

            // Step 4: Apply Rabin-Karp algorithm for each pattern
            for (WordHash pattern : patternHashes) {
                System.out.println("Searching for pattern: " + pattern.word);
                rabinKarpSearch(paragraph, pattern);
            }
        } catch (IOException e) {
            System.err.println("Error reading files: " + e.getMessage());
        }
    }

    private static String readFile(String fileName) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append(" ");
            }
        }
        return content.toString().trim();
    }

    private static List<String> readPatterns(String fileName) throws IOException {
        List<String> patterns = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                patterns.add(line.trim());
            }
        }
        return patterns;
    }

    private static long computeHash(String str, int length) {
        long hash = 0;
        for (int i = 0; i < length; i++) {
            hash = (d * hash + str.charAt(i)) % q;
        }
        return hash;
    }

    private static List<WordHash> calculateHashValues(String text) {
        String[] words = text.split("\\s+");
        List<WordHash> wordHashes = new ArrayList<>();

        for (String word : words) {
            if (!word.isEmpty()) {
                long hashValue = computeHash(word, word.length());
                wordHashes.add(new WordHash(word, word.length(), hashValue));
            }
        }

        return wordHashes;
    }

    private static void writeHashValuesToFile(List<WordHash> wordHashes, String fileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("Index, Word, Length, Hash Value\r\n");
            for (int i = 0; i < wordHashes.size(); i++) {
                WordHash wh = wordHashes.get(i);
                writer.write((i + 1) + ", " + wh.word + ", " + wh.length + ", " + wh.hashValue + "\r\n");
            }
        }
    }


    private static void rabinKarpSearch(String text, WordHash pattern) {
        int m = pattern.word.length();
        long patternHash = pattern.hashValue;

        long textHash = computeHash(text.substring(0, m), m);

        for (int i = 0; i <= text.length() - m; i++) {
            if (patternHash == textHash) {
                if (text.substring(i, i + m).equals(pattern.word)) {
                    System.out.println("Pattern found at index " + i);
                }
            }

            if (i < text.length() - m) {
                textHash = (d * textHash - text.charAt(i) * pow(d, m - 1)) % q;
                textHash = (textHash + text.charAt(i + m)) % q;
                if (textHash < 0) {
                    textHash += q; // Ensure positive value
                }
            }
        }
    }

    private static long pow(int base, int exp) {
        long result = 1;
        while (exp > 0) {
            result *= base;
            exp--;
        }
        return result;
    }

    // Helper class to store word information and its hash
    private static class WordHash {
        String word;
        int length;
        long hashValue;

        WordHash(String word, int length, long hashValue) {
            this.word = word;
            this.length = length;
            this.hashValue = hashValue;
        }
    }
}
