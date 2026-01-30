package com.akyro;

public class WorkoutEditor {
    private final InputReader inputReader;

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
        workout.printWorkout();
        int exerciseInput = inputReader.readMenuChoice("Enter number of exericse to edit: ",
                EDIT_EXERCISE_MENU_MIN, workout.size()) - 1;
        System.out.println();

        Exercise editedExercise = workout.getExercises().get(exerciseInput);

        System.out.println("1: Name");
        System.out.println("2: Sets");
        System.out.println("3: Reps");
        System.out.println("4: Weight");
        System.out.println("5: Muscle Group");

        while (true) {
            int editField = inputReader.readMenuChoice("Choose a field to edit",
                    EDIT_FIELD_MIN, EDIT_FIELD_MAX) - 1;

            switch (editField) {
                case EDIT_NAME -> editedExercise.setName(inputReader.readNonBlankString("Updated Name: "));
                case EDIT_WEIGHT -> editedExercise.setSets(inputReader.readPositiveInteger("Updated Sets: "));
                case EDIT_SETS -> editedExercise.setReps(inputReader.readPositiveInteger("Updated Reps: "));
                case EDIT_REPS -> editedExercise.setWeight(inputReader.readNonNegativeDouble("Updated Weight: "));
                case EDIT_MUSCLE_GROUP ->
                    editedExercise.setMuscleGroup(inputReader.readNonBlankString("Updated Muscle Group: "));
                case DONE_EDITING -> {
                    return;
                }
            }
        }

    }
}
