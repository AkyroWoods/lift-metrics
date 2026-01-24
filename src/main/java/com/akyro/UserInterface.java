package com.akyro;

import java.util.List;
import java.util.Scanner;

public class UserInterface {

    // ===== Color Constants =====
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String RESET = "\u001B[0m";

    private Scanner scanner;
    private final AnalyticsEngine engine;
    private final WorkoutStorage storage;
    private boolean workoutSaved = true;

    public UserInterface(Scanner scanner) {
        this.scanner = scanner;
        this.engine = new AnalyticsEngine();
        this.storage = new WorkoutStorage();
    }

    public void start() {
        runMainMenu();
    }

    private void runMainMenu() {
        while (true) {
            printMainMenu();
            int cmd = readPositiveInteger("Command: ");

            switch (cmd) {
                case 1:
                    break;
                case 2:

                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    break;
                case 6:
                    break;
            }
        }
    }

    private void printMainMenu() {
        System.out.println(CYAN + "=== Main Menu ===" + RESET);
        System.out.println("1: Create workout");
        System.out.println("2: Load dorkout");
        System.out.println("3: List saved workouts");
        System.out.println("4: Delete workout");
        System.out.println("5: Print commands ");
        System.out.println("6: Quit main menu");

    }

    private void loadedWorkoutMenu(Workout workout) {
        while (true) {
            System.out.println(CYAN + "=== Workout: " + workout.getName() + " ===" + RESET);
            System.out.println("1: Add exercise");
            System.out.println("2: List workout");
            System.out.println("3: Edit exercise");
            System.out.println("4: View summary");
            System.out.println("5: View analytics");
            System.out.println("6: Save workout");
            System.out.println("7: Return to main menu");

            int cmd = readPositiveInteger("Command: ");

            while (cmd < 1 || cmd > 7) {
                System.out.println(RED + "Invalid Option. Please choose 1-7." + RESET);
                cmd = readPositiveInteger("Command: ");
            }
            switch (cmd) {
                case 1:
                    addExerciseToWorkout(workout);
                    break;
                case 2:
                    printWorkoutList(workout);
                    break;
                case 3:
                    editExercise(workout);
                    break;
                case 4:
                    viewWorkoutSummary(workout);
                    break;
                case 5:
                    showWorkoutAnalytics(workout);
                    break;
                case 6:
                    saveWorkout(workout);
                    break;
                case 7:
                    return;
            }
        }
    }

    private void addExerciseToWorkout(Workout workout) {
        String name = readNonBlankString("Name: ");
        int sets = readPositiveInteger("Sets: ");
        int reps = readPositiveInteger("Reps: ");
        double weight = readNonNegativeDouble("Weight: ");
        String muscleGroup = readNonBlankString("Muscle Group: ");

        Exercise exercise = new Exercise(name, sets, reps, weight, muscleGroup);
        workout.addExercise(exercise);
        workoutSaved = false;

        System.out.println(GREEN + "Exercise added" + RESET);
    }

    private void printWorkoutList(Workout workout) {
        workout.printWorkout();
    }

    private void editExercise(Workout workout) {
        if (emptyWorkoutErrorMessage(workout)) {
            return;
        }
        workout.printWorkout();
        System.out.print("Enter number of exericse to edit: ");
        int exerciseInput = Integer.valueOf(scanner.nextLine()) - 1;

        while (exerciseInput < 0 || exerciseInput >= workout.size()) {
            System.out.println(RED + "Please enter a valid number");
            System.out.print("Enter number of exericse to edit: ");
            exerciseInput = Integer.valueOf(scanner.nextLine()) - 1;

        }

        System.out.println("1: Name");
        System.out.println("2: Sets");
        System.out.println("3: Rps");
        System.out.println("4: Weight");
        System.out.println("5: Muscle Group");
        System.out.print("Which would you like to edit: ");

        int menuInput = Integer.valueOf(scanner.nextLine());
        while (menuInput < 0 || menuInput > 5) {
            System.out.println(RED + "Enter a number between 1-5" + RESET);
            System.out.print("Which would you like to edit: ");
            menuInput = Integer.valueOf(scanner.nextLine());
        }

        switch (menuInput) {
            case 1:
                String name = readNonBlankString("Updated Name: ");
                workout.getExercises().get(exerciseInput).setName(name);
                System.out.println(GREEN + "Exericse name updated" + RESET);
                break;
            case 2:
                int sets = readPositiveInteger("Updated Sets: ");
                workout.getExercises().get(exerciseInput).setSets(sets);
                System.out.println(GREEN + "Sets updated");
                break;
            case 3:
                int reps = readPositiveInteger("Updated Reps: ");
                workout.getExercises().get(exerciseInput).setReps(reps);
                System.out.println(GREEN + "Reps updated");
                break;
            case 4:
                double weight = readNonNegativeDouble("Updated Weight: ");
                workout.getExercises().get(exerciseInput).setWeight(weight);
                System.out.println(GREEN + "Weight updates");
                break;
            case 5:
                String muscleGroup = readNonBlankString("Updated Muscle Group");
                workout.getExercises().get(exerciseInput).setMuscleGroup(muscleGroup);
                System.out.println("Muscle group updated");
                break;
            default:
                System.out.println(RED + "Invalid Number" + RESET);
        }
        workoutSaved = false;
    }

    private String chooseWorkoutFile() {
        List<String> workouts = storage.getSavedWorkouts();
        if (workouts.isEmpty()) {
            System.out.println(RED + "No workouts to load");
            return null;
        }
        System.out.println(CYAN + "=== Saved Workouts ===" + RESET);

        int fileCounter = 1;
        for (String workoutData : workouts) {
            System.out.println(fileCounter + ". " + workoutData);
            fileCounter++;
        }
        System.out.print("Enter the number of the workout to load: ");
        int input = Integer.valueOf(scanner.nextLine()) - 1;

        while (input < 0 || input >= workouts.size()) {
            System.out.println("Please enter a valid workout to load");
            System.out.print("Enter the number of the workout to load: ");
            input = Integer.valueOf(scanner.nextLine()) - 1;
        }

        String fileName = workouts.get(input);
        return fileName;

    }

    private void prepareAnalytics(Workout workout) {
        engine.calculateVolumeBreakdown(workout);
    }

    private void showWorkoutAnalytics(Workout workout) {

        System.out.println(CYAN + "\n=== Workout Analytics ===" + RESET);

        var top3 = engine.topNExercises(workout, 3);
        System.out.println(YELLOW + "Top 3 Exercises by Volume:" + RESET);
        for (var entry : top3) {
            System.out.println(" - " + entry.getKey().getName() + ": " + formatPercent(entry.getValue()));
        }

        var bottom3 = engine.bottomNExercises(workout, 3);

        System.out.println(YELLOW + "\nBottom 3 Exercises by Volume:" + RESET);

        if (bottom3.isEmpty()) {
            System.out.println(RED + "  Not enough exercises to display the bottom 3." + RESET);
        } else {
            for (var entry : bottom3) {
                System.out.println(" - " + entry.getKey().getName() + ": " + formatPercent(entry.getValue()));
            }
        }

        var ppl = engine.volumePercentageSplit();
        System.out.println(YELLOW + "\nPush / Pull / Legs Split:" + RESET);
        System.out.println(" - Push: " + formatPercent(ppl.get("Push")));
        System.out.println(" - Pull: " + formatPercent(ppl.get("Pull")));
        System.out.println(" - Legs: " + formatPercent(ppl.get("Legs")));

        Exercise highest = engine.getHighestVolumeExercise();
        System.out.println(YELLOW + "\nHighest Volume Exercise:" + RESET);
        System.out.println(" - " + GREEN + highest.getName() + RESET +
                " (" + highest.calculateTotalVolume() + " lbs)");
    }

    private boolean emptyWorkoutErrorMessage(Workout workout) {
        if (workout.size() == 0) {
            System.out.println(RED + "The workout has no exercises added." + RESET);
            return true;
        }
        return false;
    }

    private String formatPercent(double value) {
        return String.format("%.2f%%", value * 100);
    }

    private boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String readNonBlankString(String prompt) {
        while (true) {
            System.out.print(YELLOW + prompt + RESET);
            String input = scanner.nextLine();

            if (input.isBlank()) {
                System.out.println(RED + "Please enter a non blank name" + RESET);
                continue;
            }
            return input;
        }
    }

    private int readPositiveInteger(String prompt) {
        while (true) {
            System.out.print(YELLOW + prompt + RESET);
            String input = scanner.nextLine();

            if (!isInteger(input)) {
                System.out.println(RED + "Please enter a whole number" + RESET);
                continue;
            }

            int value = Integer.parseInt(input);

            if (value < 1) {
                System.out.println(RED + "Please enter a positive number" + RESET);
                continue;
            }
            return value;
        }
    }

    private double readNonNegativeDouble(String prompt) {
        while (true) {
            System.out.print(YELLOW + prompt + RESET);
            String input = scanner.nextLine();

            try {
                double weight = Double.parseDouble(input);
                if (weight < 0) {
                    System.out.println(RED + "Please enter a non negative number" + RESET);
                    continue;
                }
                return weight;

            } catch (NumberFormatException e) {
                System.out.println(RED + "Please enter a number" + RESET);
            }
        }
    }

}

 private void commandList() {
        System.out.println(CYAN + "=== Commands ===" + RESET);
        System.out.println("1: Add exercise");
        System.out.println("2: List workout");
        System.out.println("3: Edit exercise");
        System.out.println("4: Print workout summary");
        System.out.println("5: Show workout analytics");
        System.out.println("6: Save workout");
        System.out.println("7: Load Workout(s)");
        System.out.println("help - List commands again");
        System.out.println("quit - Quit the program");
    }