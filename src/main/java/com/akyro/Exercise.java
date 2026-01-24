package com.akyro;

public class Exercise {
    private String name;
    private int sets;
    private int reps;
    private double weight;
    private String muscleGroup;

    public Exercise(String name, int sets, int reps, double weight, String muscleGroup)
            throws IllegalArgumentException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Exercise name cannot be blank");
        }
        if (sets < 1) {
            throw new IllegalArgumentException("Sets cannot be less than 1");
        }
        if (reps < 1) {
            throw new IllegalArgumentException("Reps cannot be less than 1");
        }
        if (weight < 0) {
            throw new IllegalArgumentException("Weight cannot be negative");
        }
        if (muscleGroup == null || muscleGroup.isBlank()) {
            throw new IllegalArgumentException("Muscle group cannot be blank");
        }
        this.name = name;
        this.sets = sets;
        this.reps = reps;
        this.weight = weight;
        this.muscleGroup = muscleGroup.toLowerCase();
    }

    public String getName() {
        return name;
    }

    public int getSets() {
        return sets;
    }

    public int getReps() {
        return reps;
    }

    public double getWeight() {
        return weight;
    }

    public String getMuscleGroup() {
        return muscleGroup;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setMuscleGroup(String muscleGroup) {
        this.muscleGroup = muscleGroup;
    }

    public double calculateTotalVolume() {
        return weight * (sets * reps);
    }

    public String classifyExercise() {
        String muscleGroup = this.muscleGroup;

          return switch (muscleGroup) {
            case "chest", "shoulders", "triceps" -> "Push";
            case "lats", "biceps", "erectors", "back", "traps", "pull" -> "Pull";
            case "quads", "hamstrings", "calves", "glutes", "legs", "adductors", "abductors" -> "Legs";
            default -> "Other";
        };
        
    }

    @Override
    public String toString() {
        return name + " - " +"(" + sets + "x" + reps + " @ " + weight + " lbs)";
    }

}
