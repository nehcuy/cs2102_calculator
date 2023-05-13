import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FDCalculator {
    static class FunctionalDependency {
        private final Set<Character> lhs;
        private final Set<Character> rhs;

        public FunctionalDependency(Set<Character> lhs, Set<Character> rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }

        public Set<Character> getLhs() {
            return lhs;
        }

        public Set<Character> getRhs() {
            return rhs;
        }

        @Override
        public String toString() {
            return lhs + " -> " + rhs;
        }
    }

    public List<FunctionalDependency> calculateAllClosures(Set<Character> attributes, Set<FunctionalDependency> functionalDependencies) {
        List<FunctionalDependency> closure = new ArrayList<>();
        // Iterate through all possible permutations of attributes, then find the closure of each permutation
        for (int i = 0; i < Math.pow(2, attributes.size()); i++) {
            // Convert the integer to a binary string
            String binaryString = Integer.toBinaryString(i);
            // Pad the binary string with 0s to match the number of attributes
            binaryString = String.format("%0" + attributes.size() + "d", Integer.parseInt(binaryString));
            // Convert the binary string to a set of attributes
            Set<Character> setOfAttributes = new HashSet<>();
            for (int j = 0; j < binaryString.length(); j++) {
                if (binaryString.charAt(j) == '1') {
                    setOfAttributes.add((Character) attributes.toArray()[j]);
                }
            }
            // Calculate the closure of the set of attributes
            Set<Character> closureOfAttributes = calculateClosure(setOfAttributes, functionalDependencies);
            // Add the functional dependency to the list of closures
            closure.add(new FunctionalDependency(setOfAttributes, closureOfAttributes));
        }
        // Exclude the empty set, then sort the closures nicely
        closure.remove(0);
        closure.sort(Comparator.comparing(a -> a.getLhs().size()));
        return closure;
    }

    public Set<Character> calculateClosure(Set<Character> setOfAttributes, Set<FunctionalDependency> fd) {
        Set<Character> closure = new HashSet<>(setOfAttributes);
        // Keep looping until the closure does not change
        while (true) {
            // Keep track of the size of the closure before adding new attributes
            int closureSize = closure.size();
            // Iterate through all functional dependencies
            for (FunctionalDependency functionalDependency : fd) {
                // Check if the left hand side of the functional dependency is a subset of the closure
                if (closure.containsAll(functionalDependency.getLhs())) {
                    // Add the right hand side of the functional dependency to the closure
                    closure.addAll(functionalDependency.getRhs());
                }
            }
            // If the closure size does not change, then the closure is complete
            if (closureSize == closure.size()) {
                break;
            }
        }
        return closure;
    }

    private FunctionalDependency fdCreator(String fd) {
        String[] split = fd.split("->");
        Set<Character> lhs = new HashSet<>();
        Set<Character> rhs = new HashSet<>();
        for (char c : split[0].trim().toCharArray()) {
            lhs.add(c);
        }
        for (char c : split[1].trim().toCharArray()) {
            rhs.add(c);
        }
        return new FunctionalDependency(lhs, rhs);
    }

    public Set<Set<Character>> findKeys(Set<Character> attributes, List<FunctionalDependency> closures) {
        Set<Set<Character>> keys = new HashSet<>();
        Set<Set<Character>> superkeys = new HashSet<>();
        for (FunctionalDependency fd : closures) {
            if (fd.getRhs().equals(attributes)) {
                superkeys.add(fd.getLhs());
            }
        }
        for (Set<Character> superkey : superkeys) {
            Set<Character> copy = new HashSet<>(superkey);
            for (Character attribute : superkey) {
                copy.remove(attribute);
                if (isMinimalSuperkey(copy, superkeys)) {
                    // Check if set of same attributes already exists
                    boolean exists = false;
                    for (Set<Character> key : keys) {
                        if (key.size() == copy.size() && key.containsAll(copy)) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) keys.add(copy);
                }
                copy.add(attribute);
            }
        }
        return keys;
    }

    public static boolean isMinimalSuperkey(Set<Character> superkey, Set<Set<Character>> superkeys) {
        for (int i = 1; i < superkey.size(); i++) {
            Set<Set<Character>> subsets = generateSubsets(superkey, i);
            for (Set<Character> subset : subsets) {
                if (!superkeys.contains(subset)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static Set<Set<Character>> generateSubsets(Set<Character> set, int size) {
        Set<Set<Character>> subsets = new HashSet<>();
        if (size == 0) {
            subsets.add(new HashSet<>());
            return subsets;
        }
        if (set.isEmpty()) {
            return subsets;
        }
        Character first = set.iterator().next();
        Set<Character> rest = new HashSet<>(set);
        rest.remove(first);
        for (Set<Character> subset : generateSubsets(rest, size - 1)) {
            Set<Character> newSubset = new HashSet<>(subset);
            newSubset.add(first);
            subsets.add(newSubset);
        }
        subsets.addAll(generateSubsets(rest, size));
        return subsets;
    }

    public static void main(String[] args) {
        FDCalculator test = new FDCalculator();
        // Set of attributes
        Set<Character> attributes = Set.of('A', 'B', 'C', 'D', 'E');

        // Set of functional dependencies
        Set<FunctionalDependency> fd = Set.of(
                test.fdCreator("C -> B"),
                test.fdCreator("AB -> C"),
                test.fdCreator("BC -> D"),
                test.fdCreator("CD -> E")
        );

        // Calculate the closure of all attributes
        List<FunctionalDependency> output = test.calculateAllClosures(attributes, fd);
//        for (FunctionalDependency functionalDependency : output) {
//            System.out.println("Closure of: " + functionalDependency);
//        }

        // Find all keys
        Set<Set<Character>> keys = test.findKeys(attributes, output);
        for (Set<Character> key : keys) {
            System.out.println(key + " is a key");
        }
    }
}
