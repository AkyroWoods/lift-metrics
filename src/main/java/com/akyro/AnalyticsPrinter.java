package com.akyro;

public class AnalyticsPrinter {
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String RESET = "\u001B[0m";
    private final AnalyticsEngine engine;

    public AnalyticsPrinter(AnalyticsEngine engine) {
        this.engine = engine;
    }

    public void printWorkoutAnalytics(Workout workout) {
        engine.calculateVolumeBreakdown(workout);

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
    private String formatPercent(double value) {
        return String.format("%.2f%%", value * 100);
    }

    private String formatSafePercent(double value) {
        if (value <= 0) {
            System.out.println("No data available");
        }
        return formatPercent(value);
    }

    public void printComparison(WorkoutComparison result, Workout a, Workout b) {
        printSummary(a);
        System.out.println("--------------------------------------------------");
        System.out.println();
        printSummary(b);

        System.out.println(CYAN + "=== " + a.getName() + " V.S " + b.getName() + " ===" + RESET);
        printVolumeDifference(result, a, b);

        System.out.println();

        System.out.println("Common Exercises: ");
        if (result.getCommonExercises().isEmpty()) {
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

    private void printVolumeDifference(WorkoutComparison result, Workout a, Workout b) {
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
    }

    private void printSummary(Workout workout) {
        System.out.println(CYAN + "=== " + workout.getName() + " Summary" + " ===" + RESET);
        System.out.println("Total Volume: " + workout.calculateTotalWorkoutVolume() + " lbs");
        System.out.println("Exercises:");
        for (Exercise e : workout.getExercises()) {
            System.out.println(" - " + e.getName() + ": " + e.calculateTotalVolume() + " lbs");
        }
        System.out.println();
    }

}
