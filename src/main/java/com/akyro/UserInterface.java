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

    //Color Constants
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String RESET = "\u001B[0m";

    private final InputReader inputReader;
    private final AnalyticsEngine engine = new AnalyticsEngine();
    private final WorkoutStorage storage = new WorkoutStorage();
    private final PrintMenus menuPrinter = new PrintMenus();
    private final WorkoutEditor workoutEditor;
    private final AnalyticsPrinter analyticsPrinter;
    private boolean workoutSaved = true;

    public UserInterface() {
        this.inputReader = new InputReader(new Scanner(System.in));
        this.workoutEditor = new WorkoutEditor(inputReader);
        this.analyticsPrinter = new AnalyticsPrinter(engine);
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
        String workoutName = inputReader.readNonBlankString(CYAN + "Name of Workout: " + RESET);
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
            System.out.println(fileCounter + ". " + workoutData + 
            " (Created at: " + fileCreationDate(workoutData) + ")");
            fileCounter++;
        }

    }

    private void compareWorkouts() {
        if (storage.getSavedWorkouts().size() < 2) {
            System.out.println(RED + "Insufficient workout data, please log 2 workouts minimum to compare");
            return;
        }
        Workout a = storage.loadWorkout(chooseWorkoutFile());
        System.out.println(YELLOW + "First Workout Selected" + RESET);
        Workout b = storage.loadWorkout(chooseWorkoutFile());
        WorkoutComparison result = engine.compareWorkouts(a, b);
        System.out.println();
        analyticsPrinter.printComparison(result, a, b);
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

        int cmd = inputReader.readMenuChoice("Choose a workout to delete",
                1, storage.getSavedWorkouts().size()) - 1;
        String workoutToDelete = workouts.get(cmd);

        String confirm = inputReader.readNonBlankString(YELLOW + "Are you sure you want to delete "
                + workoutToDelete + "? (y/n): " + RESET);

        if (!confirm.equalsIgnoreCase("y")) {
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

    private void quit() {
        System.out.println(YELLOW + "Exiting program..." + RESET);
        System.exit(0);
    }

    private static final int ADD_EXERCISE = 1;
    private static final int LIST_WORKOUT = 2;
    private static final int EDIT_EXERCISE = 3;
    private static final int DELETE_EXERCISE = 4;
    private static final int VIEW_SUMMARY = 5;
    private static final int REPRINT_COMMANDS_LOADED = 6;
    private static final int VIEW_ANALYTICS = 7;
    private static final int SAVE_WORKOUT = 8;
    private static final int QUIT_LOADED_MENU = 9;

    private static final int LOADED_WORKOUT_MENU_MIN = 1;
    private static final int LOADED_WORKOUT_MENU_MAX = 9;

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
                case DELETE_EXERCISE: 
                    workoutEditor.deleteExercise(workout);
                    break;
                case VIEW_SUMMARY:
                    viewWorkoutSummary(workout);
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
                        System.out.println();
                        menuPrinter.printMainMenu();
                        return;
                    }
                    break;
            }
        }
    }

    private void addExerciseToWorkout(Workout workout) {
        workoutEditor.addExerciseToWorkout(workout);
        workoutSaved = false;
        System.out.println(GREEN + "Exercise added" + RESET);
    }

    private void printWorkoutList(Workout workout) {
        if (emptyWorkout(workout)) {
            return;
        }
        workout.printWorkout();
        System.out.println();
    }

    private void editExercise(Workout workout) {
        if (emptyWorkout(workout)) {
            return;
        }
        workout.printWorkout();
        workoutEditor.editExercise(workout);
        workoutSaved = false;

        System.out.println(GREEN + "Exercise Updated! " + RESET);
    }

    private void viewWorkoutSummary(Workout workout) {
        if (emptyWorkout(workout)) {
            return;
        }

        System.out.println(CYAN + "\n=== Workout Summary ===" + RESET);
        System.out.println("Workout: " + workout.getName());
        System.out.println("Total Sets: " + workout.totalSets());
        System.out.println("Total Reps: " + workout.totalReps());
        System.out.println("Total Volume: " + FormatUtils.formatNumber(workout.calculateTotalWorkoutVolume()) + " lbs");
        System.out.println();
    }

    private void saveWorkout(Workout workout) {
        if (emptyWorkout(workout)) {
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
            String input = inputReader.readNonBlankString(YELLOW + "You have unsaved changes. Save before returning? (y/n): " + RESET);

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

        System.out.println();
        System.out.println(CYAN + "=== Saved Workouts ===" + RESET);

        int fileCounter = 1;
        for (String workoutData : workouts) {

            System.out.println(fileCounter + ". " + workoutData + " (Created at: " + fileCreationDate(workoutData) + ")");
            fileCounter++;
        }

        int maxNumberOfWorkouts = workouts.size();
        int input = inputReader.readMenuChoice("Choose workout to load: ",
                1, maxNumberOfWorkouts) - 1;
                System.out.println();
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

    private void showWorkoutAnalytics(Workout workout) {
        analyticsPrinter.printWorkoutAnalytics(workout);
        System.out.println();
    }

    private boolean emptyWorkout(Workout workout) {
        if (workout.size() == 0) {
            System.out.println(RED + "The workout has no exercises added." + RESET);
            return true;
        }
        return false;
    }
}