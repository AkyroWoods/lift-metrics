package com.akyro;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class WorkoutStorage {
    private ObjectMapper mapper;
    private static final Pattern ILLEGAL_FILENAME_CHARS = Pattern.compile("[\\\\/:*?\"<>]");
    private static final String DATA_DIR = "data";

    public WorkoutStorage() {
        this.mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public boolean saveWorkout(Workout workout) {
        if (!createDirectory()) {
            return false;
        }
        String fileName = uniqueFileName(workout.getName());
        File file = new File(DATA_DIR, fileName);

        try {
            mapper.writeValue(file, workout);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to save workout: " + e.getMessage());
            return false;
        }
    }

    public Workout loadWorkout(String filename) {
        Path filePath = Paths.get(DATA_DIR, filename);
        try {
            return mapper.readValue(filePath.toFile(), Workout.class);
        } catch (IOException e) {
            System.err.println("Failed to load workout file: " + e.getMessage());
            return null;
        }
    }

    public List<Workout> loadAllWorkouts() {
        List<Workout> workouts = new ArrayList<>();
        Path path = Paths.get(DATA_DIR);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, "*.json")) {
            for (Path entry : stream) {
                String fileName = entry.getFileName().toString();
                Workout loadedWorkout = loadWorkout(fileName);
                if (loadedWorkout != null) {
                    workouts.add(loadedWorkout);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load all workouts: " + e.getMessage());
        }
        return workouts;
    }

    public boolean deleteWorkout(String filename) {
        File file = new File(DATA_DIR, filename);
        return file.delete();
    }

    public List<String> getSavedWorkouts() {
        return listFiles();
    }

    private boolean createDirectory() {
        Path path = Paths.get(DATA_DIR);

        try {
            Files.createDirectories(path);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to create data directory: " + e.getMessage());
            return false;
        }
    }

    private String uniqueFileName(String workout) {
        String base = sanitizeWorkoutName(workout);
        String extension = ".json";

        String potentialName = base + extension;
        Path path = Paths.get(DATA_DIR, potentialName);
        if (!Files.exists(path)) {
            return potentialName;
        }

        int counter = 1;
        while (true) {
            potentialName = base + " (" + counter + ")" + extension;
            path = Paths.get(DATA_DIR, potentialName);
            if (!Files.exists(path)) {
                return potentialName;
            }
            counter++;
        }
    }

    private String sanitizeWorkoutName(String workoutName) {
        String sanitizedName = ILLEGAL_FILENAME_CHARS.matcher(workoutName).replaceAll("");
        sanitizedName = sanitizedName.trim();
        sanitizedName = sanitizedName.replaceAll("-+", "-"); // Remove potential double hyphens
        sanitizedName = sanitizedName.replaceAll("^-+", ""); // Remove leading hyphens
        sanitizedName = sanitizedName.replaceAll("-+$", "");

        if (sanitizedName.isEmpty()) { // If sanitization removes all characters use default filename
            sanitizedName = "Workout";
        }
        return sanitizedName;

    }

    private List<String> listFiles() {
        Path dirPath = Paths.get(DATA_DIR);
        List<String> workouts = new ArrayList<>();

        try (Stream<Path> jsonFiles = Files.list(dirPath)) {

            jsonFiles.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .forEach(path -> workouts.add(path.getFileName().toString()));
        } catch (IOException e) {
            System.err.println("Error reading data directory: " + e.getMessage());
        }
        return workouts;
    }
}