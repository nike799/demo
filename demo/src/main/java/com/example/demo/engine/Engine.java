package com.example.demo.engine;

import org.springframework.stereotype.Service;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class Engine {
    private static final String language = "C_PLUS_PLUS";
    File solution;
    Path tmp = Paths.get(Paths.get("").toAbsolutePath() + "/demo/src/main/java/com/example/demo/engine/memory.txt");

    //  @StreamListener("SUBMISSION_OUTPUT")
    public SubmissionResult runEngine(String submission) throws IOException, InterruptedException {
        SubmissionResult sr = new SubmissionResult();

        Files.createDirectories(tmp);
        File output = new File(tmp + "/output.txt");
        File error = new File(tmp + "/error.txt");
        File input = new File(tmp + "/input.txt");

        List<String> inputs = new ArrayList<>() {{
//            add("3, A\n");
//            add("3, B\n");
            add("5\n7\n");
//            add("8\n7\n");
        }};

        createSolutionFile(submission);

        long beforeTest = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        long afterTest = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        System.out.println("DRY TEST BEFORE MB: " + beforeTest / (1024 * 1024));

        System.out.println("DRY TEST AFTER MB: " + afterTest / (1024 * 1024));

        if (compileResult(error)) {
            for (String i : inputs) {
                try {
                    writeToFile(input, i);

                    long beforeUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

                    Process p = runScript(error, output, input);
                    long currentPID = p.pid();

                    long afterUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

                    if (isExecTimeExceeded(sr, p)) {
                        break;
                    }

                    MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
                    System.out.println("THIS IS IT: " + heapMemoryUsage.getUsed() / (1024 * 1024));

                    long totalMemory = Runtime.getRuntime().totalMemory();
                    long freeMemory = Runtime.getRuntime().freeMemory();
                    long usedMemory = (totalMemory - freeMemory) / (1024 * 1024);

                    System.out.println("Memory used: " + usedMemory);

                    System.out.println("Total in MB is :" + Runtime.getRuntime().totalMemory() / (1024 * 1024));
                    System.out.println("before: " + beforeUsedMem / (1024 * 1024));
                    System.out.println("after: " + afterUsedMem / (1024 * 1024));
                    System.out.println("diff: " + (afterUsedMem - beforeUsedMem) / (1024 * 1024));
                    if (usedMemory > 10000) {
                        throw new OutOfMemoryError("Out of RAM");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    sr.setTimeLimitException(e.getMessage());
                    System.out.println("Test");
                    System.out.println(sr.timeLimitException);
                    break;
                }

                if (!fileReader(error).trim().equals("")) {
                    sr.getOutputResults().put(i, fileReader(error));
                } else {
                    sr.getOutputResults().put(i, fileReader(output));
                }

                try {
                    clear(output);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }

        if (!

                fileReader(error).

                        equals("")) {
            sr.setCompilationError(fileReader(error)); //TODO: Python doesnt compile, fix it later
        }

        System.out.println(sr.getOutputResults().
                entrySet().
                stream().
                map((e) -> e.getKey() + " -> " + e.getValue()).
                collect(Collectors.joining("\n")));

        System.out.println(sr.getCompilationError());

//        deleteDirectoryRecursion(tmp.toAbsolutePath());
        return sr;
    }

    private void checkProcessMemory(long currentPID) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "/bin/zsh", "-c", "sudo cat /proc/" + currentPID + "/smaps | grep -i pss | awk '{Total+=$2} END {print Total/1024\" MB\"}'"
                //"bash","-c", "sudo", "cat", "/proc/" + currentPID + "/smaps | grep -i pss | awk '{Total+=$2} END {print Total/1024\" MB\"}'"
        );// TODO Check if we can use one instance of the ProcessBuilder

        System.out.println("The current" +
                " process ID is : " + currentPID);
//        processBuilder.redirectOutput(new File(Paths.get("").toAbsolutePath() + "/test.txt"));
        processBuilder.redirectErrorStream(true);
//                    checkMemory.redirectOutput(new File(Paths.get("").toAbsolutePath() + "/memory.txt"));
//                    checkMemory.redirectError(new File(Paths.get("").toAbsolutePath() + "/memoryError.txt"));
        Process process = processBuilder.start();
        InputStreamReader isr = new InputStreamReader(process.getInputStream());
        BufferedReader br = new BufferedReader(isr);
        String line = br.readLine();
        System.out.println(line);
    }

//    private boolean isMemoryExceeded(long beforeUsedMem, SubmissionResult sr, Process p) {
//        Runtime.getRuntime().mem
//    }

    private boolean isExecTimeExceeded(SubmissionResult sr, Process p) throws InterruptedException, IOException {
        if (!p.waitFor(2, TimeUnit.SECONDS)) {
            while (p.isAlive()) {
                checkProcessMemory(p.pid());//TODO refactor
                String exception = "Process timeout out after " + 2 + " seconds";
                sr.setTimeLimitException(exception);
            }
            p.destroy(); // consider using destroyForcibly instead
            return true;
        }
        return false;
    }

    private void writeToFile(File file, String input) {
        BufferedWriter bw = null;
        try {
            FileWriter fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            bw.write(input);

        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void createSolutionFile(String submission) throws IOException {
        String suffix = "";
        switch (language) {
            case "C_SHARP":
                suffix = ".cs";
                break;
            case "C_PLUS_PLUS":
                suffix = ".cpp";
                break;
            case "PYTHON":
                suffix = ".py";
                break;
            case "JAVA":
                suffix = ".java";
                break;
        }

        solution = new File(tmp + "/Solution" + suffix);
        System.out.println(solution.getAbsolutePath());
        FileWriter fileWriterCode = new FileWriter(solution);
        fileWriterCode.write(submission);
        fileWriterCode.close();
    }

    private Process runScript(File error, File output, File input) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        ProcessBuilder pb = new ProcessBuilder();
        switch (language) {
            case "C_SHARP":
                compileResult(error);
                command.add("mono");
                command.add(solution.getAbsolutePath().substring(0, solution.getAbsolutePath().indexOf(".")) + ".exe");
                break;
            case "C_PLUS_PLUS":
                compileResult(error);
//                command.add("-Wl,--heap=100");
                command.add(tmp + "/cpp");
                break;
            case "PYTHON":
                command.add("python");
                command.add(solution.getAbsolutePath());
                break;
            case "JAVA":
                command.add("java");
//                command.add("-Xmx4m");
                command.add(solution.getAbsolutePath());
                break;
        }

        pb.command(command);
        pb.redirectError(error);
        pb.redirectInput(input);
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(output));

        return pb.start();
    }

    private boolean compileResult(File error) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        switch (language) {
            case "JAVA":
                command.add("javac");
                break;

            case "C_SHARP":
                command.add("mcs");
                break;

            case "C_PLUS_PLUS":
                command.add("g++");
                command.add("-o");
                command.add(tmp + "/cpp");
                break;

            default:
                return true;
        }

        command.add(1, solution.getAbsolutePath());
        processBuilder.redirectError(error);

        return processBuilder.start().waitFor() == 0;
    }

    private static String fileReader(File file) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private void clear(File file) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(file);
        writer.print("");
        writer.close();
    }

    private void deleteDirectoryRecursion(Path path) throws IOException {
        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for (Path entry : entries) {
                    deleteDirectoryRecursion(entry);
                }
            }
        }
        Files.delete(path);
    }

    public static class SubmissionResult {

        Map<String, String> outputResults = new LinkedHashMap<>();

        String timeLimitException;

        String memoryLimitException;

        String compilationError;


        public Map<String, String> getOutputResults() {
            return outputResults;
        }

        public String getCompilationError() {
            return compilationError;
        }

        public String getTimeLimitException() {
            return timeLimitException;
        }

        public String getMemoryLimitException() {
            return memoryLimitException;
        }

        public void setOutputResults(Map<String, String> outputResults) {
            this.outputResults = outputResults;
        }

        public void setCompilationError(String compilationError) {
            this.compilationError = compilationError;
        }

        public void setTimeLimitException(String timeLimitException) {
            this.timeLimitException = timeLimitException;
        }

        public void setMemoryLimitException(String memoryLimitException) {
            this.memoryLimitException = memoryLimitException;
        }
    }
}
