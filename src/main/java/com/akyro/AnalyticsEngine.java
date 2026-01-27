package com.akyro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AnalyticsEngine {
    private double pushVolume;
    private double pullVolume;
    private double legsVolume;
    private Exercise highestVolumeExercise;
    private Exercise lowestVolumeExercise;

    public void calculateVolumeBreakdown(Workout workout) {
        pushVolume = 0;
        pullVolume = 0;
        legsVolume = 0;
        if (workout.getExercises().isEmpty()) {
            highestVolumeExercise = null;
            lowestVolumeExercise = null;
            return;
        }
        highestVolumeExercise = workout.getExercises().get(0);
        lowestVolumeExercise = workout.getExercises().get(0);
        for (Exercise e : workout.getExercises()) {
            double currentExerciseVolume = e.calculateTotalVolume();

            String category = e.classifyExercise();
            switch (category) {
                case "Push" -> pushVolume += currentExerciseVolume;
                case "Pull" -> pullVolume += currentExerciseVolume;
                case "Legs" -> legsVolume += currentExerciseVolume;
            }

            if (currentExerciseVolume > highestVolumeExercise.calculateTotalVolume()) {
                highestVolumeExercise = e;
            }
            if (currentExerciseVolume < lowestVolumeExercise.calculateTotalVolume()) {
                lowestVolumeExercise = e;
            }

        }
    }

    public Map<Exercise, Double> getExerciseVolumePercentages(Workout workout) {
        Map<Exercise, Double> percentages = new HashMap<>();
        double totalVolume = workout.calculateTotalWorkoutVolume();

        if (totalVolume == 0) {
            return percentages;
        }

        for (Exercise e : workout.getExercises()) {
            double volume = e.calculateTotalVolume();
            double percentage = volume / totalVolume;
            percentages.put(e, percentage);
        }
        return percentages;
    }

    public Map<Exercise, Double> getSortedExerciseVolumePercentages(Workout workout) {
        Map<Exercise, Double> percentages = new HashMap<>();
        List<Map.Entry<Exercise, Double>> sortedPercentages = new ArrayList<>();
        double totalVolume = workout.calculateTotalWorkoutVolume();

        for (Exercise e : workout.getExercises()) {
            double volume = e.calculateTotalVolume();
            double percentage = volume / totalVolume;
            percentages.put(e, percentage);
        }

        for (Map.Entry<Exercise, Double> pair : percentages.entrySet()) {
            sortedPercentages.add(pair);
        }
        Collections.sort(sortedPercentages,
                (a, b) -> Double.compare(b.getValue(), a.getValue()));

        LinkedHashMap<Exercise, Double> rankedExercises = new LinkedHashMap<>();

        for (Map.Entry<Exercise, Double> entry : sortedPercentages) {
            rankedExercises.put(entry.getKey(), entry.getValue());
        }
        return rankedExercises;

    }

    public List<Map.Entry<Exercise, Double>> topNExercises(Workout workout, int n) {
        Map<Exercise, Double> sorted = getSortedExerciseVolumePercentages(workout);
        List<Map.Entry<Exercise, Double>> entries = new ArrayList<>(sorted.entrySet());

        int limit = Math.min(n, entries.size());
        return entries.subList(0, limit);
    }

    public List<Map.Entry<Exercise, Double>> bottomNExercises(Workout workout, int n) {
        Map<Exercise, Double> sorted = getSortedExerciseVolumePercentages(workout);
        List<Map.Entry<Exercise, Double>> entries = new ArrayList<>(sorted.entrySet());

        if (entries.size() <= n) {
            return Collections.emptyList();
        }
        int size = entries.size();

        int start = Math.max(0, size - n);

        return entries.subList(start, size);
    }

    public Map<String, Double> volumePercentageSplit() {
        Map<String, Double> ppl = new LinkedHashMap<>();
        ppl.put("Push", getPushPercentage());
        ppl.put("Pull", getPullPercentage());
        ppl.put("Legs", getLegsPercentage());
        return ppl;
    }
    
    public WorkoutComparison compareWorkouts(Workout a, Workout b) {
        double aVolume = a.calculateTotalWorkoutVolume();
        double bVolume = b.calculateTotalWorkoutVolume();
        double volumeDifference = Math.abs(bVolume - aVolume);
        Set<String> namesA = getExerciseNames(a);
        Set<String> namesB = getExerciseNames(b);

        List<String> uniqueToA = uniqueExercises(namesA, namesB);
        List<String> uniqueToB = uniqueExercises(namesB, namesA);
        List<String> commonExercises = sharedExercises(namesA, namesB);

        return new WorkoutComparison(aVolume, bVolume, volumeDifference,
             uniqueToA, uniqueToB, commonExercises);
    }

    public double getPushPercentage() {
        return pushVolume / totalVolume();
    }

    public double getPullPercentage() {
        return pullVolume / totalVolume();
    }

    public double getLegsPercentage() {
        return legsVolume / totalVolume();
    }

    public Exercise getHighestVolumeExercise() {
        return highestVolumeExercise;
    }

    public Exercise getLowestVolumeExercise() {
        return lowestVolumeExercise;
    }

    private double totalVolume() {
        double totalVolume = pushVolume + pullVolume + legsVolume;
        // Prevent division by 0 before analysis runs
        if (totalVolume == 0) {
            return 1;
        }
        return totalVolume;
    }

    private Set<String> getExerciseNames(Workout workout) {
       return workout.getExercises()
       .stream()
       .map(Exercise :: getName)
       .collect(Collectors.toSet());
    }

    private List<String> sharedExercises(Set<String> a, Set<String> b) {
        Set<String> exercises = new HashSet<>(a);
        exercises.retainAll(b);
        return new ArrayList<>(exercises);
    }

    private List<String> uniqueExercises(Set<String> a, Set<String> b) {
        Set<String> exercises = new HashSet<>(a);
        exercises.removeAll(b);
        return new ArrayList<>(exercises);
    }
}
