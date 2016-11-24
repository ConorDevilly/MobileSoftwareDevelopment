package com.example.conor.a1rmtracker;

/**
 * Simple class to represent exercises
 * Created by Conor on 21/11/16.
 */
public class Exercise {
    private int id;
    private String date;
    private String exercise;
    private float weight;
    private int reps;
    private float orm;

    public Exercise(int id, String date, String exercise, float weight, int reps, float orm) {
        this.id = id;
        this.date = date;
        this.exercise = exercise;
        this.weight = weight;
        this.reps = reps;
        this.orm = orm;
    }

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getExercise() {
        return exercise;
    }

    public void setExercise(String exercise) {
        this.exercise = exercise;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public float getOrm() {
        return orm;
    }

    public void setOrm(float orm) {
        this.orm = orm;
    }
}
