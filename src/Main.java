import java.io.IOException;
import java.util.ArrayList;

public class Main{

    public static void main(String[] args) throws IOException {
        ArrayList<DFA> dfas = DFA.readDFAFromFile("dfas.txt");
        DFA dfa1 = dfas.get(0);
        DFA dfa2 = dfas.get(1);

        if(dfa1.equals(dfa2)) {
            System.out.println("Загруженные автоматы эвивалентны");
        }
        else {
            System.out.println("Загруженные автоматы не эвивалентны");
        }

        DFA newDFA = dfas.get(0).minimize();
        System.out.printf(newDFA.toString());
    }
}