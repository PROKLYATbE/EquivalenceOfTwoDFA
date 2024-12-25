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

    public Set<Set<Integer>> findEquivalenceClasses() {
        Set<Set<Integer>> partitions = new HashSet<>();

        // Начальное разбиение
        Set<Integer> terminalStates = new HashSet<>();
        Set<Integer> nonTerminalStates = new HashSet<>();

        for (int state = 0; state < isTerminal.size(); state++) {
            if (isTerminal.get(state)) {
                terminalStates.add(state);
            } else {
                nonTerminalStates.add(state);
            }
        }

        if (!terminalStates.isEmpty()) {
            partitions.add(terminalStates);
        }
        if (!nonTerminalStates.isEmpty()) {
            partitions.add(nonTerminalStates);
        }

        // Алфавит
        Set<Character> alphabet = new HashSet<>();
        for (HashMap<Character, Integer> map : transitions.values()) {
            alphabet.addAll(map.keySet());
        }

        Queue<Pair<Set<Integer>, Character>> queue = new LinkedList<>();

        // Заполнение очереди парами
        for (Character c : alphabet) {
            queue.offer(new Pair<>(terminalStates, c));
            queue.offer(new Pair<>(nonTerminalStates, c));
        }

        while (!queue.isEmpty()) {
            Pair<Set<Integer>, Character> pair = queue.poll();
            Set<Integer> currentClass = pair.getKey();
            char symbol = pair.getValue();

            for (Set<Integer> partition : new HashSet<>(partitions)) {
                Pair<Set<Integer>, Set<Integer>> splitResult = split(partition, currentClass, symbol);
                Set<Integer> newClass1 = splitResult.getKey();
                Set<Integer> newClass2 = splitResult.getValue();

                if (!newClass1.isEmpty() && !newClass2.isEmpty()) {
                    partitions.remove(partition);
                    partitions.add(newClass1);
                    partitions.add(newClass2);

                    // Добавление новых пар в очередь
                    queue.offer(new Pair<>(newClass1, symbol));
                    queue.offer(new Pair<>(newClass2, symbol));
                }
            }
        }
        return partitions;
    }

    private Pair<Set<Integer>, Set<Integer>> split(Set<Integer> partition, Set<Integer> classToCheck, char symbol) {
        Set<Integer> newClass1 = new HashSet<>();
        Set<Integer> newClass2 = new HashSet<>();

        for (Integer state : partition) {
            Integer nextState = transitions.get(state).get(symbol);
            if (classToCheck.contains(nextState)) {
                newClass1.add(state);
            } else {
                newClass2.add(state);
            }
        }
        return new Pair<>(newClass1, newClass2);
    }

    // Вспомогательный класс для хранения пар значений
    public static class Pair<K, V> {
        private K key;
        private V value;
        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }
        public K getKey() {
            return key;
        }
        public V getValue() {
            return value;
        }
    }

    public DFA minimize() {
        Set<Set<Integer>> equivalenceClasses = findEquivalenceClasses();
        HashMap<Set<Integer>, Integer> classToStateId = new HashMap<>();
        HashMap<Integer, HashMap<Character, Integer>> newTransitions = new HashMap<>();
        ArrayList<Boolean> newIsTerminal = new ArrayList<>();

        int newStateId = 0;

        for (Set<Integer> equivalenceClass : equivalenceClasses) {
            classToStateId.put(equivalenceClass, newStateId++);
            boolean isTerminalState = equivalenceClass.stream().anyMatch(isTerminal::get);
            newIsTerminal.add(isTerminalState);
        }

        for (Set<Integer> equivalenceClass : equivalenceClasses) {
            int stateId = classToStateId.get(equivalenceClass);
            HashMap<Character, Integer> stateTransitions = new HashMap<>();

            for (Integer state : equivalenceClass) {
                for (Character symbol : transitions.get(state).keySet()) {
                    Integer nextState = transitions.get(state).get(symbol);
                    for (Set<Integer> nextClass : equivalenceClasses) {
                        if (nextClass.contains(nextState)) {
                            stateTransitions.put(symbol, classToStateId.get(nextClass));
                            break;
                        }
                    }
                }
            }
            newTransitions.put(stateId, stateTransitions);
        }
        return new DFA(newTransitions, newIsTerminal);
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
