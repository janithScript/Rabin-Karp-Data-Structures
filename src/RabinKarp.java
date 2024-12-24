import java.io.*;
import java.util.*;

public class RabinKarp {

    private static final int d = 256; // Number of characters in input alphabet
    private static final int q = 101;  // A prime number for hashing

    public static void main(String[] args) {
        try {
            // Step 1: Read input files
            String paragraph = readFile("paragraph5.txt");
            List<WordHash> patternHashes = readHashValues("hValPattk.txt");

            // Step 2: Calculate and store hash values for paragraph words
            List<WordHash> wordHashes = calculateHashValues(paragraph);
            writeHashValuesToFile(wordHashes, "hValParak.txt");

            // Step 3: Construct the pattern-matching table
            List<String> matchingResults = constructPatternMatchingTable(paragraph, patternHashes);
            writeMatchingResultsToFile(matchingResults, "pattMatchk.txt");

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

    private static List<WordHash> readHashValues(String fileName) throws IOException {
        List<WordHash> hashes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(", ");
                if (parts.length == 4) { // Ensure we have all parts
                    String word = parts[1];
                    int length = Integer.parseInt(parts[2]);
                    long hashValue = Long.parseLong(parts[3]);
                    hashes.add(new WordHash(word, length, hashValue));
                }
            }
        }
        return hashes;
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

    private static List<String> constructPatternMatchingTable(String text, List<WordHash> patterns) {
        List<String> results = new ArrayList<>();
        results.add("Pattern jth Matching words and its location range(s)\r\n");
        results.add("index, j pattern Words Index range(s)\r\n\n");

        for (int j = 0; j < patterns.size(); j++) {
            WordHash pattern = patterns.get(j);
            List<Integer> matchIndices = rabinKarpSearch(text, pattern);

            if (matchIndices.isEmpty()) {
                results.add(String.format("%d %s — No matching found —\r\n", (j + 1), pattern.word));
            } else {
                StringBuilder rangesBuilder = new StringBuilder();
                rangesBuilder.append(String.format("%d %s ", (j + 1), pattern.word));

                // Get the surrounding context for each match
                for (int index : matchIndices) {
                    String matchContext = text.substring(Math.max(0, index),
                            Math.min(text.length(), index + pattern.word.length() + 10));
                    rangesBuilder.append(matchContext).append(" ");
                    rangesBuilder.append(String.format("%d – %d, ",
                            index, index + pattern.word.length() - 1));
                }

                results.add(rangesBuilder.toString().trim() + "\r\n");
            }
        }

        return results;
    }



    private static List<Integer> rabinKarpSearch(String text, WordHash pattern) {
        int m = pattern.word.length();
        long patternHash = pattern.hashValue;

        long textHash = computeHash(text.substring(0, m), m);
        List<Integer> matchIndices = new ArrayList<>();

        for (int i = 0; i <= text.length() - m; i++) {
            if (patternHash == textHash) {
                if (text.substring(i, i + m).equals(pattern.word)) {
                    matchIndices.add(i); // Store starting index of match
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

        return matchIndices;
    }

    private static long pow(int base, int exp) {
        long result = 1;
        while (exp > 0) {
            result *= base;
            exp--;
        }
        return result;
    }

    private static void writeMatchingResultsToFile(List<String> matchingResults, String fileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (String line : matchingResults) {
                writer.write(line);
            }
        }
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