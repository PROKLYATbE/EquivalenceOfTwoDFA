import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DFA {
    public final HashMap<Integer, HashMap<Character, Integer>> transitions;
    public final ArrayList<Boolean> isTerminal;

    public DFA(HashMap<Integer, HashMap<Character, Integer>> transitions, ArrayList<Boolean> isTerminal) {
        this.transitions = transitions;
        this.isTerminal = isTerminal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DFA dfa = (DFA) o;
        return bfsEquivalenceCheck(this, dfa);
    }

    private static boolean bfsEquivalenceCheck(DFA dfa1, DFA dfa2) {
        HashMap<Integer, HashMap<Character, Integer>> transitions1 = dfa1.getTransitions();
        HashMap<Integer, HashMap<Character, Integer>> transitions2 = dfa2.getTransitions();
        ArrayList<Boolean> isTerminal1 = dfa1.getIsTerminal();
        ArrayList<Boolean> isTerminal2 = dfa2.getIsTerminal();

        // Множество уже посещённых пар
        Set<String> visited = new HashSet<>();

        // Очередь для хранения пар состояний
        Queue<ArrayList<Integer>> queue = new LinkedList<>();

        // Начальные состояния
        queue.add(new ArrayList<Integer>(Arrays.asList(0, 0)));
        visited.add("0-0");

        while (!queue.isEmpty()) {
            ArrayList<Integer> pair = queue.poll();
            int u = pair.get(0);
            int v = pair.get(1);

            if (isTerminal1.get(u) != isTerminal2.get(v)) {
                return false;
            }

            for (char c : transitions1.getOrDefault(u, new HashMap<>()).keySet()) {
                int nextState1 = transitions1.get(u).get(c);
                int nextState2 = transitions2.getOrDefault(v, new HashMap<>()).getOrDefault(c, -1);

                if (nextState2 == -1) {
                    return false;
                }

                String pairKey = nextState1 + "-" + nextState2;

                if (!visited.contains(pairKey)) {
                    visited.add(pairKey);
                    queue.add(new ArrayList<Integer>(Arrays.asList(nextState1, nextState2)));
                }
            }
        }

        return true;
    }

    public static ArrayList<DFA> readDFAFromFile(String filename) throws IOException {
        ArrayList<DFA> dfas = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#")) {
                continue;
            }

            HashMap<Integer, HashMap<Character, Integer>> transitions = new HashMap<>();
            ArrayList<Boolean> isTerminal = new ArrayList<>();
            boolean readingTransitions = false;

            do {
                if (line.startsWith("transitions:")) {
                    readingTransitions = true;
                    continue;
                }

                if (readingTransitions) {
                    if (line.trim().isEmpty()) {
                        readingTransitions = false;
                        continue; // Конец блока переходов
                    }
                    String[] parts = line.split("'");
                    int fromState = Integer.parseInt(parts[0].trim());
                    char symbol = parts[1].trim().charAt(0);
                    int toState = Integer.parseInt(parts[2].trim());

                    transitions.putIfAbsent(fromState, new HashMap<>());
                    transitions.get(fromState).put(symbol, toState);
                } else if (line.startsWith("isTerminal:")) {
                    String[] terminalStates = line.split(":")[1].trim().split(", ");
                    for (String state : terminalStates) {
                        isTerminal.add(Boolean.parseBoolean(state));
                    }
                    break;
                }
            } while ((line = reader.readLine()) != null);

            dfas.add(new DFA(transitions, isTerminal));
        }

        reader.close();
        return dfas;
    }

    public HashMap<Integer, HashMap<Character, Integer>> getTransitions() {
        return transitions;
    }

    public ArrayList<Boolean> getIsTerminal() {
        return isTerminal;
    }

    @Override
    public String toString() {
        return "DFA{" + "transitions=" + transitions + ", isTerminal=" + isTerminal + '}';
    }
}
