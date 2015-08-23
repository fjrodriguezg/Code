package es.fjrodriguezg.interview;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

/**
 Suppose you would like to write a ransom note by cutting out letters
 from a magazine and pasting them together. Before doing this, you'd
 like to know if it is possible to write your note using a specific
 magazine.

 Write a function that takes as input two strings: first the text of the
 ransom note you'd like to write, and second the full text of the
 magazine. The function should determine if the ransom note can be
 produced using the given magazine. Please explain how long your
 function will take to determine the answer in terms on the length of the
 note "n" and the length of the magazine "m".
 */

public class RansomNote {

    private static final int RUN_COUNT = 5;

    public static void main(String[] args) {
        //String note = "this is a note";
        //String letters = "first the text of the ransom note you'd like to write, and second the full text of the magazine. The function should determine if the ransom note can be produced using the given magazine";
        String note = "this is a note";
        String letters = "this is a note";

        measure("Traditional", () -> ransomNote1(note, letters));
        measure("Sequential1", () -> ransomNote2(note, letters, false));
        measure("Parallel1", () -> ransomNote2(note, letters ,true));
        measure("Sequential2", () -> ransomNote3(note, letters, false));
        //Best solution
        measure("Parallel2", () -> ransomNote3(note, letters ,true));

    }

    public static boolean ransomNote1(String note, String letters){

        char[] note_c = note.toCharArray();
        boolean possible = true;
        for (int i = 0; i < note_c.length; i++) {
            if (letters.indexOf(note_c[i]) != -1)
                letters = letters.replaceFirst(String.valueOf(note_c[i]), "_");
            else {
                System.out.println("Letter not found: "+note_c[i]);
                possible = false;
                break;
            }
        }

        if (possible) {
            System.out.println("Possible");
        } else {
            System.out.println("Not possible");
            System.out.println(letters);
        }

        return possible;
    }

    static Map<String, Long> countLetters(String str, boolean parallel){
        Stream<String> words;
        if (parallel) {
            words = Stream.of(str).parallel();
        }else {
            words = Stream.of(str);
        }

        return words.map(w -> w.split(""))
                        .flatMap(Arrays::stream)
                        .collect(groupingBy(identity(), counting()));
    }

    public static boolean ransomNote2(String note, String letters, boolean parallel){
        boolean possible = true;
        Map<String, Long> compare;
        Map<String, Long> letterToCountNote =  countLetters(note, parallel);
        Map<String, Long> letterToCountLetters =  countLetters(letters, parallel);


        for (String key: letterToCountNote.keySet()){
            if (parallel){
                compare =
                        letterToCountLetters.entrySet()
                                .stream().parallel()
                                .filter(p -> p.getKey().equals(key) && p.getValue() >= letterToCountNote.get(key))
                                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
            }else{
                compare =
                        letterToCountLetters.entrySet()
                                .stream()
                                .filter(p -> p.getKey().equals(key) && p.getValue() >= letterToCountNote.get(key))
                                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
            }
            if (compare.size()<1){
                possible = false;
                System.out.println("Not possible");
            }

        }
        return possible;
    }

    //Best solution
    public static boolean ransomNote3(String note, String letters, boolean parallel){
        boolean possible = true;
        Map<String, Long> letterToCountNote =  countLetters(note, parallel);
        Map<String, Long> letterToCountLetters =  countLetters(letters, parallel);

        for (String key: letterToCountNote.keySet()){
            if (letterToCountNote.get(key)> letterToCountLetters.get(key)){
                System.out.println("Lower occurrences: "+key);
                System.out.println("Possible");
                possible = false;
                break;
            }
        }
        return possible;
    }

    //Measure
    static <T> T measureOneRun(String label, Supplier<T> supplier) {
        long startTime = System.nanoTime();
        T result = supplier.get();
        long endTime = System.nanoTime();
        System.out.printf("%s took %dms%n",
                label, (endTime - startTime + 500_000L) / 1_000_000L);
        return result;
    }

    static <T> T measure(String label, Supplier<T> supplier) {
        T result = null;

        for (int i = 0; i < RUN_COUNT; i++)
            result = measureOneRun(label, supplier);

        return result;
    }
}