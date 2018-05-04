package com;

import java.util.ArrayList;

/**
 * Created by Khushbu on 4/24/2018.
 */

//general class that contains methods to convert float to double, double to Double, double to float.
public class ConversionMethods {

    public Double[] convertPrimitiveToRef(double[] array){


        Double[] arrayDouble = new Double[256];
        int count = 0;
        for(double x : array){
            arrayDouble[count] = new Double(x);
            count ++;
        }
        return arrayDouble;
    }

    public ArrayList<Float> convertDoubleToFloat(ArrayList<Double> doubleList){

        ArrayList<Float> floatList = new ArrayList<Float>();
        for(double x : doubleList){
            floatList.add((float)x);
        }

        return floatList;
    }

    public ArrayList<Double> convertFloatToDoubleList(ArrayList<Float> floatList){

        ArrayList<Double> doubleList = new ArrayList<Double>();
        for(float x : floatList){
            doubleList.add((double)x);
        }
        return doubleList;
    }

    public double[] convertFloatsToDoubles(float[] input)
    {
        if (input == null)
        {
            return null; // Or throw an exception - your choice
        }
        double[] output = new double[input.length];
        for (int i = 0; i < input.length; i++)
        {
            output[i] = input[i];
        }
        return output;
    }
    }
