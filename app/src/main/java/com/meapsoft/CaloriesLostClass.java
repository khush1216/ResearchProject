package com.meapsoft;

/**
 * Created by Khushbu on 4/20/2018.
 */

public class CaloriesLostClass {


    private double weight;
    private double age;
    private String gender;


    public CaloriesLostClass(){

    }

    public CaloriesLostClass(double w, double a, String g){
        this.weight = w;
        this.age = a;
        this.gender =  g;

    }

    //calculate the calorie lost based on the distance moved, weight, gender, age and time.
    // here heart rate has been considered a constant average.

    public double calculateLostCals(long time, double heartRate){

        double calLost = 0.0;
        heartRate = 148;
        time = time /(1000*60);
        if(gender.equals("male")) {
             calLost = ((age * 0.2017) - (weight * 0.09036) + ((heartRate * 0.6309) - 55.0569)) * (time/4.184);
        }
        else if(gender.equals("female")){
            calLost = ((age * 0.074) - (weight * 0.05741) + ((heartRate * 0.4472) - 20.4022)) * (time/4.184);
        }

        return Math.round(calLost*100.0)/100.0;

    }



}
