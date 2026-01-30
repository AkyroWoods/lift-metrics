package com.akyro;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Scanner;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class UserInterface {

    // ===== Color Constants =====
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String RESET = "\u001B[0m";

    private final Scanner scanner;
    private final InputReader inputReader;
    private final AnalyticsEngine engine = new AnalyticsEngine();
    private final WorkoutStorage storage = new WorkoutStorage();
    private final PrintMenus menuPrinter = new PrintMenus();
    private final WorkoutEditor workoutEditor;
    private boolean workoutSaved = true;

    public UserInterface(Scanner scanner) {
        this.scanner = scanner;
        this.inputReader = new InputReader(scanner);
        this.workoutEditor = new WorkoutEditor(inputReader);
    }

    public void start() {
        runMainMenu();
    }

    private static final int CREATE_WORKOUT = 1;
    private static final int LOAD_WORKOUT = 2;
    private static final int LIST_SAVED_WORKOUTS = 3;
    private static final int COMPARE_TWO_WORKOUTS = 4;
    private static final int DELETE_WORKOUT = 5;
    private static final int REPRINT_COMMANDS_MAIN = 6;
    private static final int QUIT_MAIN_MENU = 7;

    private static final int MAIN_MENU_MIN = 1;
    private static final int MAIN_MENU_MAX = 8;
    private void runMainMenu() {
        menuPrinter.printMainMenu();
        while (true) {
          int cmd = inputReader.readMenuChoice("Command: ", 
          MAIN_MENU_MIN, MAIN_MENU_MAX);

            switch (cmd) {
                case CREATE_WORKOUT:
                    createWorkout();
                    break;
                case LOAD_WORKOUT:
                    loadWorkout();
                    break;
                case LIST_SAVED_WORKOUTS:
                    listSavedWorkouts();
                    System.out.println();
                    menuPrinter.printMainMenu();
                    break;
                case COMPARE_TWO_WORKOUTS:
                    compareWorkouts();
                    break;
                case DELETE_WORKOUT:
                    deleteWorkout();
                    break;
                case REPRINT_COMMANDS_MAIN:
                    menuPrinter.printMainMenu();
                    break;
                case QUIT_MAIN_MENU:
                    quit();
                    break;
                default:
                    System.out.println(RED + "Unknown command. Type 5 to see available options" + RESET);
            }
        }
    }

    private void createWorkout() {
        String workoutName = readNonBlankString(CYAN + "Name of Workout: " + RESET);

        while (workoutName.isBlank()) {
            System.out.println(RED + "Invalid workout name" + RESET);
            System.out.print(CYAN + "Name of Workout: " + RESET);
            workoutName = readNonBlankString(CYAN + "Name of Workout: " + RESET);
        }
        Workout workout = new Workout(workoutName);
        System.out.println();

        loadedWorkoutMenu(workout);
    }

    private void loadWorkout() {
        String fileName = chooseWorkoutFile();
        if (fileName == null) {
            return;
        }
        Workout loadedWorkout = storage.loadWorkout(fileName);
        loadedWorkoutMenu(loadedWorkout);
    }

    private void listSavedWorkouts() {
        List<String> workouts = storage.getSavedWorkouts();
        if (workouts.isEmpty()) {
            System.out.println(RED + "No saved workouts found" + RESET);
            return;
        }
        System.out.println(CYAN + "=== Saved Workouts ===" + RESET);

        int fileCounter = 1;
        for (String workoutData : workouts) {
            System.out.println(fileCounter + ". " + workoutData);
            fileCounter++;
        }
    }

    private void compareWorkouts() {
        if (storage.getSavedWorkouts().size() == 0) {
            System.out.println(RED + "Insufficient workout data, please log 2 workouts minimum to compare");
        }
        Workout a = storage.loadWorkout(chooseWorkoutFile());
        System.out.println(YELLOW + "First Workout Selected" + RESET);
        Workout b = storage.loadWorkout(chooseWorkoutFile());
        WorkoutComparison result = engine.compareWorkouts(a, b);
        System.out.println();
        printComparison(result, a, b);
    }

    private void printComparison(WorkoutComparison result, Workout a, Workout b) {
        printCondensedWorkoutSummary(a);
        System.out.println("--------------------------------------------------");
        System.out.println();
        printCondensedWorkoutSummary(b);

        System.out.println(CYAN + "=== " + a.getName() + " V.S " + b.getName() + " ===" + RESET);

        double percent = result.volumeDifferenceAsPercent();
        if (a.calculateTotalWorkoutVolume() > b.calculateTotalWorkoutVolume()) {
            System.out.println(a.getName() + " volume was greater by +" + result.getVolumeDifference()
                    + " lbs (+" + formatPercent(percent) + ")");
        } else if (b.calculateTotalWorkoutVolume() > a.calculateTotalWorkoutVolume()) {
            System.out.println(b.getName() + " volume was greater by +" + result.getVolumeDifference()
                    + " lbs (+" + formatPercent(percent) + ")");
        } else {
            System.out.println("No difference in volume");
        }

        System.out.println();

        System.out.println("Common Exercises: ");
        if (result.getCommonExercises().size() == 0) {
            System.out.println(YELLOW + " - None in common" + RESET);
        }
        result.getCommonExercises().forEach(e -> System.out.println(" - " + e));
        System.out.println();

        System.out.println("Unique to " + a.getName() + ":");
        result.getUniqueToA().forEach(e -> System.out.println(" - " + e));
        System.out.println();

        System.out.println("Unique to " + b.getName() + ":");
        result.getUniqueToB().forEach(e -> System.out.println(" - " + e));
        System.out.println();
    }

    private void printCondensedWorkoutSummary(Workout workout) {
        System.out.println(CYAN + "=== " + workout.getName() + " Summary" + " ===" + RESET);
        System.out.println("Total Volume: " + workout.calculateTotalWorkoutVolume() + " lbs");
        System.out.println("Exercises:");
        for (Exercise e : workout.getExercises()) {
            System.out.println(" - " + e.getName() + ": " + e.calculateTotalVolume() + " lbs");
        }
        System.out.println();
    }

    private void deleteWorkout() {
        List<String> workouts = storage.getSavedWorkouts();
        if (workouts.isEmpty()) {
            System.out.println(RED + "No saved workouts to delete." + RESET);
            return;
        }
        System.out.println(CYAN + "=== Delete Workout ===" + RESET);
        int fileCounter = 1;
        for (String workoutData : workouts) {
            System.out.println(fileCounter + ". " + workoutData);
            fileCounter++;
        }

        int cmd = readPositiveInteger("Choose a workout to delete: ") - 1;

        while (cmd < 0 || cmd >= workouts.size()) {
            System.out.println(RED + "Invalid Option. Try again" + RESET);
            cmd = readPositiveInteger("Choose a workout to delete: ") - 1;
        }
        String workoutToDelete = workouts.get(cmd);

        System.out.print(YELLOW + "Are you sure you want to delete '" + workoutToDelete + "? (y/n): " + RESET);
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (!confirm.equals("y")) {
            System.out.println(CYAN + "Deletion cancelled." + RESET);
            return;
        }

        boolean success = storage.deleteWorkout(workoutToDelete);

        if (success) {
            System.out.println(GREEN + "Workout deleted." + RESET);
        } else {
            System.out.println(RED + "Failed to delete workout." + RESET);
        }
    }

    public void quit() {
        System.out.println(YELLOW + "Exiting program..." + RESET);
        System.exit(0);
    }

    private static final int ADD_EXERCISE = 1;
    private static final int LIST_WORKOUT = 2;
    private static final int EDIT_EXERCISE = 3;
    private static final int VIEW_SUMMARY = 4;
    private static final int REPRINT_COMMANDS_LOADED = 5;
    private static final int VIEW_ANALYTICS = 6;
    private static final int SAVE_WORKOUT = 7;
    private static final int QUIT_LOADED_MENU = 8;

    private static final int LOADED_WORKOUT_MENU_MIN = 1;
    private static final int LOADED_WORKOUT_MENU_MAX = 8;

    private void loadedWorkoutMenu(Workout workout) {
        menuPrinter.printLoadedWorkoutMenu(workout);

        while (true) {
            int cmd = inputReader.readMenuChoice("Command: ",
             LOADED_WORKOUT_MENU_MIN, LOADED_WORKOUT_MENU_MAX);

            switch (cmd) {
                case ADD_EXERCISE:
                    addExerciseToWorkout(workout);
                    break;
                case LIST_WORKOUT:
                    printWorkoutList(workout);
                    break;
                case EDIT_EXERCISE:
                    editExercise(workout);
                    break;
                case VIEW_SUMMARY:
                    viewWorkoutSummary(workout);
                    menuPrinter.printLoadedWorkoutMenu(workout);
                    break;
                case REPRINT_COMMANDS_LOADED:
                    menuPrinter.printLoadedWorkoutMenu(workout);
                    break;
                case VIEW_ANALYTICS:
                    showWorkoutAnalytics(workout);
                    break;
                case SAVE_WORKOUT:
                    saveWorkout(workout);
                    break;
                case QUIT_LOADED_MENU:
                    if (handleQuitLoadedMenu(workout)) {
                        menuPrinter.printMainMenu();
                        ;
                        return;
                    }
                    break;
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
        if (emptyWorkoutErrorMessage(workout)) {
            return;
        }
        workout.printWorkout();
        System.out.println();
    }

    private void editExercise(Workout workout) {
        if (emptyWorkoutErrorMessage(workout)) {
            return;
        }
        workout.printWorkout();
        workoutEditor.editExercise(workout);
        workoutSaved = false;

        System.out.println(GREEN  + "Exercise Updated! " + RESET);
    }

    private void viewWorkoutSummary(Workout workout) {
        if (emptyWorkoutErrorMessage(workout)) {
            return;
        }
        prepareAnalytics(workout);
        Exercise e = engine.getHighestVolumeExercise();

        System.out.println(CYAN + "\n=== Workout Summary ===" + RESET);
        System.out.println("Workout: " + workout.getName());
        System.out.println("Total Sets: " + workout.totalSets());
        System.out.println("Total Reps: " + workout.totalReps());
        System.out.println("Total Volume: " + workout.calculateTotalWorkoutVolume() + " lbs");
        System.out.println("Highest Volume Exercise: " +
                GREEN + e.getName() + RESET +
                " (" + e.calculateTotalVolume() + " lbs)");
        System.out.println();
    }

    private void saveWorkout(Workout workout) {
        if (emptyWorkoutErrorMessage(workout)) {
            return;
        }
        if (storage.saveWorkout(workout)) {
            System.out.println(GREEN + "Workout Saved!" + RESET);
            workoutSaved = true;
        } else {
            System.out.println(RED + "Could not save workout" + RESET);
        }
    }

    private boolean handleQuitLoadedMenu(Workout workout) {
        if (!workoutSaved) {
            System.out.print(YELLOW + "You have unsaved changes. Save before returning? (y/n): " + RESET);
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("y")) {
                saveWorkout(workout);
            }
        }
        return true;
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

            System.out
                    .println(fileCounter + ". " + workoutData + " (Created at: " + fileCreationDate(workoutData) + ")");
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

    private String fileCreationDate(String fileName) {
        try {
            Path filePath = Paths.get("data/", fileName);
            BasicFileAttributes attr = Files.readAttributes(filePath, BasicFileAttributes.class);
            Instant fileCreationTime = attr.creationTime().toInstant();
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            LocalDateTime time = LocalDateTime.ofInstant(fileCreationTime, ZoneId.systemDefault());
            return dateFormat.format(time);

        } catch (IOException e) {
            return "unknown";
        }
    }

    private void prepareAnalytics(Workout workout) {
        engine.calculateVolumeBreakdown(workout);
    }

    private void showWorkoutAnalytics(Workout workout) {
        prepareAnalytics(workout);

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
        System.out.println(" - Push: " + formatSafePercent(ppl.get("Push")));
        System.out.println(" - Pull: " + formatSafePercent(ppl.get("Pull")));
        System.out.println(" - Legs: " + formatSafePercent(ppl.get("Legs")));

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

    private String formatSafePercent(double value) {
        if (value <= 0) {
            System.out.println("No data available");
        }
        return formatPercent(value);
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
            if (!input.matches(".*[a-zA-Z].*")) {
                System.out.println(RED + "Exercise name must contain at least one letter" + RESET);
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