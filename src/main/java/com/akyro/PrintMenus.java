package com.akyro;

public class PrintMenus {

    private static final String CYAN = "\u001B[36m";
    private static final String RESET = "\u001B[0m";

    public PrintMenus() {

    }

    public void printMainMenu() {
        System.out.println(CYAN + "=== Main Menu ===" + RESET);
        System.out.println("1: Create workout");
        System.out.println("2: Load workout");
        System.out.println("3: List all saved workouts");
        System.out.println("4: List saved workouts");
        System.out.println("5: Compare two workouts");
        System.out.println("6: Delete workout");
        System.out.println("7: Help/Reprint commands ");
        System.out.println("8: Quit main menu");

    }

    public void printLoadedWorkoutMenu(Workout workout) {
        System.out.println(CYAN + "=== Workout: " + workout.getName() + " ===" + RESET);
        System.out.println("1: Add exercise");
        System.out.println("2: List workout");
        System.out.println("3: Edit exercises");
        System.out.println("4: Delete exercise");
        System.out.println("5: View summary");
        System.out.println("6: Help/Reprint commands ");
        System.out.println("7: View analytics");
        System.out.println("8: Save workout");
        System.out.println("9: Return to main menu");
    }
}
