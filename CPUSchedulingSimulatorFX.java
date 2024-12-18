import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;

import java.util.*;

class Process {
    int pid, arrivalTime, burstTime, remainingTime, completionTime, turnaroundTime, waitingTime;
    boolean completed;

    Process(int pid, int arrivalTime, int burstTime) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.completed = false;
    }
}

public class CPUSchedulingSimulatorFX extends Application {
    private TableView<Process> table;
    private Canvas ganttChart;
    private TextArea outputArea;
    private List<Process> processes = new ArrayList<>();
    private int pidCounter = 1;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("CPU Scheduling Simulator");

        // Input Section
        Label lblArrivalTime = new Label("Arrival Time:");
        TextField txtArrivalTime = new TextField();

        Label lblBurstTime = new Label("Burst Time:");
        TextField txtBurstTime = new TextField();

        Button btnAddProcess = new Button("Add Process");
        btnAddProcess.setOnAction(e -> addProcess(txtArrivalTime, txtBurstTime));

        HBox inputBox = new HBox(10, lblArrivalTime, txtArrivalTime, lblBurstTime, txtBurstTime, btnAddProcess);
        inputBox.setPadding(new Insets(10));

        // Process Table
        table = new TableView<>();
        TableColumn<Process, Integer> colPID = new TableColumn<>("PID");
        colPID.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().pid));
        TableColumn<Process, Integer> colArrival = new TableColumn<>("Arrival Time");
        colArrival.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().arrivalTime));
        TableColumn<Process, Integer> colBurst = new TableColumn<>("Burst Time");
        colBurst.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().burstTime));

        table.getColumns().addAll(colPID, colArrival, colBurst);

        VBox tableBox = new VBox(10, new Label("Processes:"), table);
        tableBox.setPadding(new Insets(10));

        // Algorithm Selection and Overhead Input
        ComboBox<String> algorithmSelector = new ComboBox<>();
        algorithmSelector.getItems().addAll("FCFS", "Non-Preemptive SJF", "Preemptive SJF", "Round Robin");
        algorithmSelector.setValue("FCFS");

        Label lblTimeQuantum = new Label("Time Quantum:");
        TextField txtTimeQuantum = new TextField();
        txtTimeQuantum.setDisable(true);

        Label lblOverhead = new Label("Overhead:");
        TextField txtOverhead = new TextField();

        algorithmSelector.setOnAction(e -> txtTimeQuantum.setDisable(!algorithmSelector.getValue().equals("Round Robin")));

        Button btnSimulate = new Button("Simulate");
        btnSimulate.setOnAction(e -> simulate(algorithmSelector.getValue(), txtTimeQuantum, txtOverhead));

        // Clear Button
        Button btnClear = new Button("Clear");
        btnClear.setOnAction(e -> clearAll(txtArrivalTime, txtBurstTime, txtTimeQuantum, txtOverhead, algorithmSelector));

        HBox algorithmBox = new HBox(10, new Label("Algorithm:"), algorithmSelector, lblTimeQuantum, txtTimeQuantum, lblOverhead, txtOverhead, btnSimulate, btnClear);
        algorithmBox.setPadding(new Insets(10));

        // Output Section
        ganttChart = new Canvas(1000, 100);
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefHeight(200);

        VBox outputBox = new VBox(10, new Label("Gantt Chart:"), ganttChart, new Label("Output:"), outputArea);
        outputBox.setPadding(new Insets(10));

        // Main Layout
        VBox root = new VBox(10, inputBox, tableBox, algorithmBox, outputBox);
        Scene scene = new Scene(root, 1100, 700);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void addProcess(TextField txtArrivalTime, TextField txtBurstTime) {
        try {
            int arrivalTime = Integer.parseInt(txtArrivalTime.getText());
            int burstTime = Integer.parseInt(txtBurstTime.getText());

            Process process = new Process(pidCounter++, arrivalTime, burstTime);
            processes.add(process);
            table.getItems().add(process);

            txtArrivalTime.clear();
            txtBurstTime.clear();
        } catch (NumberFormatException e) {
            outputArea.setText("Invalid input! Please enter valid integers for Arrival Time and Burst Time.");
        }
    }

    private void simulate(String algorithm, TextField txtTimeQuantum, TextField txtOverhead) {
        outputArea.clear();
        GraphicsContext gc = ganttChart.getGraphicsContext2D();
        gc.clearRect(0, 0, ganttChart.getWidth(), ganttChart.getHeight());

        List<Process> copyProcesses = new ArrayList<>();
        for (Process p : processes) {
            copyProcesses.add(new Process(p.pid, p.arrivalTime, p.burstTime));
        }

        int overhead = 0;
        try {
            if (!txtOverhead.getText().isEmpty()) {
                overhead = Integer.parseInt(txtOverhead.getText());
            }
        } catch (NumberFormatException e) {
            outputArea.setText("Invalid Overhead! Please enter a valid integer.");
            return;
        }

        switch (algorithm) {
            case "FCFS":
                fcfs(copyProcesses, gc, overhead);
                break;
            case "Non-Preemptive SJF":
                nonPreemptiveSJF(copyProcesses, gc, overhead);
                break;
            case "Preemptive SJF":
                preemptiveSJF(copyProcesses, gc, overhead);
                break;
            case "Round Robin":
                try {
                    int timeQuantum = Integer.parseInt(txtTimeQuantum.getText());
                    if (timeQuantum <= 0) {
                        throw new IllegalArgumentException("Time Quantum must be greater than 0.");
                    }
                    roundRobin(copyProcesses, timeQuantum, gc, overhead);
                } catch (NumberFormatException e) {
                    outputArea.setText("Invalid Time Quantum! Please enter a valid integer.");
                } catch (IllegalArgumentException e) {
                    outputArea.setText(e.getMessage());
                }
                break;
            default:
                outputArea.setText("Invalid algorithm selected!");
        }
    }

    private void clearAll(TextField txtArrivalTime, TextField txtBurstTime, TextField txtTimeQuantum, TextField txtOverhead, ComboBox<String> algorithmSelector) {
        // Clear the process list and table
        processes.clear();
        table.getItems().clear();

        // Reset the PID counter
        pidCounter = 1;

        // Clear the input fields
        txtArrivalTime.clear();
        txtBurstTime.clear();
        txtTimeQuantum.clear();
        txtOverhead.clear();

        // Reset the algorithm selector
        algorithmSelector.setValue("FCFS");
        txtTimeQuantum.setDisable(true);

        // Clear the output area and Gantt chart
        outputArea.clear();
        GraphicsContext gc = ganttChart.getGraphicsContext2D();
        gc.clearRect(0, 0, ganttChart.getWidth(), ganttChart.getHeight());
    }

    private void fcfs(List<Process> processes, GraphicsContext gc, int overhead) {
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        int currentTime = 0;
        List<String> ganttData = new ArrayList<>();

        // Add overhead before the first process
        if (overhead > 0) {
            for (int i = 0; i < overhead; i++) {
                ganttData.add("Overhead");
            }
            currentTime += overhead;
        }

        for (int i = 0; i < processes.size(); i++) {
            Process p = processes.get(i);

            // Idle time if no process is available
            if (currentTime < p.arrivalTime) {
                for (int j = currentTime; j < p.arrivalTime; j++) {
                    ganttData.add("Idle");
                }
                currentTime = p.arrivalTime;
            }

            // Simulate process execution
            for (int j = 0; j < p.burstTime; j++) {
                ganttData.add("P" + p.pid);
            }

            // Update process metrics
            currentTime += p.burstTime;
            p.completionTime = currentTime;
            p.turnaroundTime = p.completionTime - p.arrivalTime;
            p.waitingTime = p.turnaroundTime - p.burstTime;

            // Add scheduling overhead after each process, except the last one
            if (i < processes.size() - 1 && overhead > 0) {
                for (int j = 0; j < overhead; j++) {
                    ganttData.add("Overhead");
                }
                currentTime += overhead;
            }
        }

        drawGanttChart(gc, ganttData, 40);
        displayMetrics(processes, "FCFS", overhead);
    }

    private void nonPreemptiveSJF(List<Process> processes, GraphicsContext gc, int overhead) {
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime)); // 按到达时间排序
        int currentTime = 0;
        List<String> ganttData = new ArrayList<>();

        // Add overhead before the first process
        if (overhead > 0) {
            for (int i = 0; i < overhead; i++) {
                ganttData.add("Overhead");
            }
            currentTime += overhead;
        }

        List<Process> completedProcesses = new ArrayList<>(); // 用于存储已完成的进程

        while (!processes.isEmpty()) {
            int finalCurrentTime = currentTime;
            // 筛选出可运行的进程并选择剩余时间最短的进程
            Process shortest = processes.stream()
                    .filter(p -> p.arrivalTime <= finalCurrentTime)
                    .min(Comparator.comparingInt(p -> p.burstTime))
                    .orElse(null);

            if (shortest == null) {
                // 如果没有进程可运行，插入空闲时间
                ganttData.add("Idle");
                currentTime++;
            } else {
                // 模拟进程运行
                for (int i = 0; i < shortest.burstTime; i++) {
                    ganttData.add("P" + shortest.pid);
                }

                // 更新时间
                currentTime += shortest.burstTime;
                shortest.completionTime = currentTime;
                shortest.turnaroundTime = shortest.completionTime - shortest.arrivalTime;
                shortest.waitingTime = shortest.turnaroundTime - shortest.burstTime;

                // 将该进程标记为完成并从列表中移除
                completedProcesses.add(shortest);
                processes.remove(shortest);

                // 如果还有其他进程需要调度，并且存在调度开销
                if (!processes.isEmpty() && overhead > 0) {
                    for (int i = 0; i < overhead; i++) {
                        ganttData.add("Overhead");
                    }
                    currentTime += overhead;
                }
            }
        }

        // 绘制甘特图并显示结果
        drawGanttChart(gc, ganttData, 40);
        displayMetrics(completedProcesses, "Non-Preemptive SJF", overhead);
    }


    private void preemptiveSJF(List<Process> processes, GraphicsContext gc, int overhead) {
        int currentTime = 0;
        List<String> ganttData = new ArrayList<>();

        // Add overhead before the first process
        if (overhead > 0) {
            for (int i = 0; i < overhead; i++) {
                ganttData.add("Overhead");
            }
            currentTime += overhead;
        }

        while (true) {
            int finalCurrentTime = currentTime;
            Process shortest = processes.stream()
                    .filter(p -> p.arrivalTime <= finalCurrentTime && !p.completed)
                    .min(Comparator.comparingInt(p -> p.remainingTime))
                    .orElse(null);

            if (shortest == null) {
                if (processes.stream().allMatch(p -> p.completed)) break;
                ganttData.add("Idle");
                currentTime++;
            } else {
                ganttData.add("P" + shortest.pid);
                shortest.remainingTime--;
                currentTime++;

                if (shortest.remainingTime == 0) {
                    shortest.completed = true;
                    shortest.completionTime = currentTime;
                    shortest.turnaroundTime = shortest.completionTime - shortest.arrivalTime;
                    shortest.waitingTime = shortest.turnaroundTime - shortest.burstTime;

                    // Add scheduling overhead
                    if (processes.stream().anyMatch(p -> !p.completed) && overhead > 0) {
                        for (int i = 0; i < overhead; i++) {
                            ganttData.add("Overhead");
                        }
                        currentTime += overhead;
                    }
                }
            }
        }

        drawGanttChart(gc, ganttData, 40);
        displayMetrics(processes, "Preemptive SJF", overhead);
    }

    private void roundRobin(List<Process> processes, int timeQuantum, GraphicsContext gc, int overhead) {
        int currentTime = 0;
        Queue<Process> readyQueue = new LinkedList<>();
        List<String> ganttData = new ArrayList<>();

        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        readyQueue.add(processes.get(0));
        int index = 1;

        // Add overhead before the first process
        if (overhead > 0) {
            for (int i = 0; i < overhead; i++) {
                ganttData.add("Overhead");
            }
            currentTime += overhead;
        }

        while (!readyQueue.isEmpty()) {
            Process current = readyQueue.poll();

            if (current.remainingTime == current.burstTime && current.arrivalTime > currentTime) {
                for (int i = currentTime; i < current.arrivalTime; i++) {
                    ganttData.add("Idle");
                }
                currentTime = current.arrivalTime;
            }

            int executionTime = Math.min(timeQuantum, current.remainingTime);
            for (int i = 0; i < executionTime; i++) {
                ganttData.add("P" + current.pid);
            }
            currentTime += executionTime;
            current.remainingTime -= executionTime;

            while (index < processes.size() && processes.get(index).arrivalTime <= currentTime) {
                readyQueue.add(processes.get(index));
                index++;
            }

            if (current.remainingTime > 0) {
                readyQueue.add(current);
            } else {
                current.completionTime = currentTime;
                current.turnaroundTime = currentTime - current.arrivalTime;
                current.waitingTime = current.turnaroundTime - current.burstTime;
            }

            // Add scheduling overhead
            if (!readyQueue.isEmpty() && overhead > 0) {
                for (int i = 0; i < overhead; i++) {
                    ganttData.add("Overhead");
                }
                currentTime += overhead;
            }
        }

        drawGanttChart(gc, ganttData, 40);
        displayMetrics(processes, "Round Robin", overhead);
    }

    private void drawGanttChart(GraphicsContext gc, List<String> ganttData, double widthPerUnit) {
        double x = 0;
        double chartHeight = 40;

        String lastProcess = "";
        double startX = 0;

        for (String label : ganttData) {
            if (!label.equals(lastProcess)) {
                if (!lastProcess.isEmpty()) {
                    gc.strokeRect(startX, 30, x - startX, chartHeight);
                    gc.fillText(lastProcess, startX + 5, 50);
                }
                startX = x;
                lastProcess = label;
            }
            x += widthPerUnit;
        }

        if (!lastProcess.isEmpty()) {
            gc.strokeRect(startX, 30, x - startX, chartHeight);
            gc.fillText(lastProcess, startX + 5, 50);
        }

        for (int i = 0; i <= ganttData.size(); i++) {
            gc.fillText(String.valueOf(i), i * widthPerUnit, 80);
        }
    }

    private void displayMetrics(List<Process> processes, String algorithm, int overhead) {
        int totalTurnaroundTime = 0;
        int totalWaitingTime = 0;

        // Compute Useful Time and Total Time
        int usefulTime = 0;
        int totalTime = 0;

        for (Process p : processes) {
            totalTurnaroundTime += p.turnaroundTime;
            totalWaitingTime += p.waitingTime;
            usefulTime += p.burstTime;
            totalTime = Math.max(totalTime, p.completionTime);
        }

        int wastedTime = totalTime - usefulTime;
        double efficiency = (double) usefulTime / totalTime;

        // Display Results
        outputArea.appendText(algorithm + " Results:\n");
        outputArea.appendText("PID\tArrival\tBurst\tCompletion\tTurnaround\tWaiting\n");

        for (Process p : processes) {
            outputArea.appendText(String.format("P%d\t%d\t%d\t%d\t\t%d\t\t%d\n",
                    p.pid, p.arrivalTime, p.burstTime, p.completionTime, p.turnaroundTime, p.waitingTime));
        }

        double avgTurnaroundTime = (double) totalTurnaroundTime / processes.size();
        double avgWaitingTime = (double) totalWaitingTime / processes.size();

        outputArea.appendText("\nAverage Turnaround Time: " + String.format("%.2f", avgTurnaroundTime) + "\n");
        outputArea.appendText("Average Waiting Time: " + String.format("%.2f", avgWaitingTime) + "\n");
        outputArea.appendText("Wasted Time: " + wastedTime + " units\n");
        outputArea.appendText("Efficiency: " + String.format("%.2f", efficiency * 100) + "%\n");
    }
}
