package com.akyro;

public class WorkoutEditor {
    private final InputReader inputReader;

    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String RESET = "\u001B[0m";

    public WorkoutEditor(InputReader inputReader) {
        this.inputReader = inputReader;
    }

    private final int EDIT_EXERCISE_MENU_MIN = 1;

    private static final int EDIT_NAME = 1;
    private static final int EDIT_SETS = 2;
    private static final int EDIT_REPS = 3;
    private static final int EDIT_WEIGHT = 4;
    private static final int EDIT_MUSCLE_GROUP = 5;
    private static final int DONE_EDITING = 6;

    private static final int EDIT_FIELD_MIN = 1;
    private static final int EDIT_FIELD_MAX = 6;

    public void editExercise(Workout workout) {
        int exerciseInput = inputReader.readMenuChoice("Enter number of exericse to edit: ",
                EDIT_EXERCISE_MENU_MIN, workout.size()) - 1;
        System.out.println();

        Exercise editedExercise = workout.getExercises().get(exerciseInput);
        System.out.println(CYAN + editedExercise + RESET);

        System.out.println("1: Name");
        System.out.println("2: Sets");
        System.out.println("3: Reps");
        System.out.println("4: Weight");
        System.out.println("5: Muscle Group");
        System.out.println("6: Done editing");

        while (true) {
            int editField = inputReader.readMenuChoice("Choose a field to edit: ",
                    EDIT_FIELD_MIN, EDIT_FIELD_MAX);

            switch (editField) {
                case EDIT_NAME -> editedExercise.setName(inputReader.readNonBlankString("Updated Name: "));
                case EDIT_SETS -> editedExercise.setSets(inputReader.readPositiveInteger("Updated Sets: "));
                case EDIT_REPS -> editedExercise.setReps(inputReader.readPositiveInteger("Updated Reps: "));
                case EDIT_WEIGHT -> editedExercise.setWeight(inputReader.readNonNegativeDouble("Updated Weight: "));
                case EDIT_MUSCLE_GROUP ->
                    editedExercise.setMuscleGroup(inputReader.readNonBlankString("Updated Muscle Group: "));
                case DONE_EDITING -> {
                    return;
                }
            }
        }

    }

    public void addExerciseToWorkout(Workout workout) {
        String name = inputReader.readNonBlankString("Name: ");
        int sets = inputReader.readPositiveInteger("Sets: ");
        int reps = inputReader.readPositiveInteger("Reps: ");
        double weight = inputReader.readNonNegativeDouble("Weight: ");
        String muscleGroup = inputReader.readNonBlankString("Muscle Group: ");

        Exercise exercise = new Exercise(name, sets, reps, weight, muscleGroup);
        workout.addExercise(exercise);

    }

    public void deleteExercise(Workout workout) {
        workout.printWorkout();

        int exerciseToDelete = inputReader.readMenuChoice("Choose an exercise to delete: ",
                1, workout.size()) - 1;
        System.out.println();

        Exercise deletedExercise = workout.getExercises().get(exerciseToDelete);
        String confirm = inputReader.readNonBlankString(YELLOW + "Are you sure you want to delete "
                + deletedExercise.getName() + "? (y/n): " + RESET);

        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println(CYAN + "Deletion cancelled." + RESET);
            return;
        }

        boolean success = workout.removeExercise(deletedExercise);
        if (success) {
            System.out.println(GREEN + "Workout deleted." + RESET);
        } else {
            System.out.println(RED + "Failed to delete workout." + RESET);
        }
    }
}
