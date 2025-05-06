import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Lab1 {
    private static class Graph {
        Map<String, Map<String, Integer>> adjacencyList = new HashMap<>();
        Map<String, Double> pageRank = new HashMap<>();
        Random random = new Random();

        void addEdge(String from, String to) {
            from = from.toLowerCase();
            to = to.toLowerCase();
            adjacencyList.putIfAbsent(from, new HashMap<>());
            adjacencyList.get(from).put(to, adjacencyList.get(from).getOrDefault(to, 0) + 1);
            adjacencyList.putIfAbsent(to, new HashMap<>());
        }

        Set<String> getNodes() {
            return adjacencyList.keySet();
        }

        // 功能2：展示有向图
        void showDirectedGraph() {
            for (String node : adjacencyList.keySet()) {
                System.out.print(node + " -> ");
                adjacencyList.get(node).forEach((k, v) -> System.out.print(k + "(" + v + ") "));
                System.out.println();
            }
        }

        // 功能3：查询桥接词
        String queryBridgeWords(String word1, String word2) {
            word1 = word1.toLowerCase();
            word2 = word2.toLowerCase();

            if (!adjacencyList.containsKey(word1) || !adjacencyList.containsKey(word2)) {
                return "No " + word1 + " or " + word2 + " in the graph!";
            }

            String finalWord = word2;
            List<String> bridges = adjacencyList.get(word1).keySet().stream()
                    .filter(word3 -> adjacencyList.containsKey(word3) && adjacencyList.get(word3).containsKey(finalWord))
                    .collect(Collectors.toList());

            if (bridges.isEmpty()) return "No bridge words from " + word1 + " to " + word2 + "!";

            return "The bridge words from " + word1 + " to " + word2 + " are: " +
                    String.join(", ", bridges.subList(0, bridges.size()-1)) +
                    (bridges.size() > 1 ? " and " : "") + bridges.get(bridges.size()-1);
        }

        // 功能4：生成新文本
        String generateNewText(String input) {
            String[] words = input.replaceAll("[^a-zA-Z ]", " ").toLowerCase().split("\\s+");
            List<String> result = new ArrayList<>();

            for (int i = 0; i < words.length - 1; i++) {
                result.add(words[i]);
                int finalI = i;
                List<String> bridges = adjacencyList.getOrDefault(words[i], new HashMap<>()).keySet().stream()
                        .filter(word3 -> adjacencyList.containsKey(word3) && adjacencyList.get(word3).containsKey(words[finalI +1]))
                        .collect(Collectors.toList());

                if (!bridges.isEmpty()) {
                    result.add(bridges.get(random.nextInt(bridges.size())));
                }
            }
            result.add(words[words.length-1]);
            return String.join(" ", result);
        }

        // 功能5：计算最短路径
        String calcShortestPath(String word1, String word2) {
            word1 = word1.toLowerCase();
            word2 = word2.toLowerCase();

            Map<String, Integer> dist = new HashMap<>();
            Map<String, String> prev = new HashMap<>();
            PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));

            for (String node : adjacencyList.keySet()) {
                dist.put(node, Integer.MAX_VALUE);
            }
            dist.put(word1, 0);
            pq.add(word1);

            while (!pq.isEmpty()) {
                String u = pq.poll();
                for (Map.Entry<String, Integer> edge : adjacencyList.getOrDefault(u, new HashMap<>()).entrySet()) {
                    String v = edge.getKey();
                    int weight = edge.getValue();
                    if (dist.get(u) + weight < dist.getOrDefault(v, Integer.MAX_VALUE)) {
                        dist.put(v, dist.get(u) + weight);
                        prev.put(v, u);
                        pq.add(v);
                    }
                }
            }

            if (dist.get(word2) == Integer.MAX_VALUE) return "No path from " + word1 + " to " + word2;

            LinkedList<String> path = new LinkedList<>();
            for (String at = word2; at != null; at = prev.get(at)) {
                path.addFirst(at);
            }
            return "Shortest path: " + String.join(" → ", path) + " (Length: " + dist.get(word2) + ")";
        }

        // 功能6：计算PageRank
        void calculatePageRank(double d, int iterations) {
            int N = adjacencyList.size();
            Map<String, Double> pr = new HashMap<>();
            Map<String, Double> finalPr = pr;
            adjacencyList.keySet().forEach(node -> finalPr.put(node, 1.0 / N));

            for (int i = 0; i < iterations; i++) {
                Map<String, Double> newPr = new HashMap<>();
                for (String node : adjacencyList.keySet()) {
                    double sum = 0.0;
                    for (String inNode : adjacencyList.keySet()) {
                        if (adjacencyList.get(inNode).containsKey(node)) {
                            sum += pr.get(inNode) / adjacencyList.get(inNode).size();
                        }
                    }
                    newPr.put(node, (1 - d) / N + d * sum);
                }
                pr = newPr;
            }
            pageRank = pr;
        }

        // 功能7：随机游走
        String randomWalk() {
            List<String> nodes = new ArrayList<>(adjacencyList.keySet());
            if (nodes.isEmpty()) return "";

            String current = nodes.get(random.nextInt(nodes.size()));
            List<String> path = new ArrayList<>();
            Set<String> visitedEdges = new HashSet<>();

            while (true) {
                path.add(current);
                Map<String, Integer> edges = adjacencyList.get(current);
                if (edges.isEmpty()) break;

                List<String> candidates = new ArrayList<>(edges.keySet());
                String next = candidates.get(random.nextInt(candidates.size()));
                String edge = current + "->" + next;

                if (visitedEdges.contains(edge)) break;
                visitedEdges.add(edge);
                current = next;
            }

            try (PrintWriter writer = new PrintWriter("random_walk.txt")) {
                writer.println(String.join(" ", path));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            return String.join(" ", path);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Graph graph = new Graph();

        System.out.print("Enter file path: ");
        String filePath = scanner.nextLine().trim();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            List<String> words = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                String processed = line.replaceAll("[^a-zA-Z ]", " ").toLowerCase();
                String[] lineWords = processed.split("\\s+");
                for (String word : lineWords) {
                    if (!word.isEmpty()) words.add(word);
                }
            }

            for (int i = 0; i < words.size() - 1; i++) {
                graph.addEdge(words.get(i), words.get(i+1));
            }

        } catch (IOException e) {
            System.out.println("Error reading file");
            return;
        }

        while (true) {
            System.out.println("\nChoose function:");
            System.out.println("1. Show graph");
            System.out.println("2. Query bridge words");
            System.out.println("3. Generate new text");
            System.out.println("4. Calculate shortest path");
            System.out.println("5. Calculate PageRank");
            System.out.println("6. Random walk");
            System.out.println("0. Exit");

            int choice = scanner.nextInt();
            scanner.nextLine();  // consume newline

            switch (choice) {
                case 1:
                    graph.showDirectedGraph();
                    break;
                case 2:
                    System.out.print("Enter word1 and word2: ");
                    String[] words = scanner.nextLine().split(" ");
                    System.out.println(graph.queryBridgeWords(words[0], words[1]));
                    break;
                case 3:
                    System.out.print("Enter text: ");
                    String input = scanner.nextLine();
                    System.out.println("New text: " + graph.generateNewText(input));
                    break;
                case 4:
                    System.out.print("Enter word1 and word2: ");
                    String[] pathWords = scanner.nextLine().split(" ");
                    System.out.println(graph.calcShortestPath(pathWords[0], pathWords[1]));
                    break;
                case 5:
                    graph.calculatePageRank(0.85, 100);
                    graph.pageRank.forEach((k, v) -> System.out.printf("%s: %.4f\n", k, v));
                    break;
                case 6:
                    System.out.println("Random walk: " + graph.randomWalk());
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice");
            }
        }
    }
}