package de.tum.lmt.texturerecognizer;

import java.util.ListIterator;

import android.util.Log;


/***********************
 * remove subtraction of mean after implementation of some preprocessing in FeatureComputer
 */

public class Calculator {

	public double roundDigits(double toRound, int precision) {
		
		double factor = Math.pow(10, precision);
		double value = toRound * factor;
		value = Math.round(value);
		return value /= factor;
	}
	
	public double bandPowerDouble(ListIterator<Double> begin, ListIterator<Double> end) {
		
		double bandPower = 0;
		int beginIndex = begin.nextIndex();
		
		for(ListIterator<Double> lit = begin; (lit.hasNext() && lit != end); ) {
			bandPower += Math.pow(lit.next(), 2.0);
		}
		
		bandPower = bandPower / ((end.previousIndex() + 1) - beginIndex);
		
		return bandPower;
	}
	
	public double bandPowerFloat(ListIterator<Float> begin, ListIterator<Float> end) {
		
		double bandPower = 0;
		int beginIndex = begin.nextIndex();
		
		for(ListIterator<Float> lit = begin; (lit.hasNext() && lit != end); ) {
			bandPower = bandPower + Math.pow(lit.next(), 2.0);
		}
		
		bandPower = bandPower / ((end.previousIndex() + 1) - beginIndex);
		
		return bandPower;
	}
	
	public double varianceDouble(ListIterator<Double> begin, ListIterator<Double> end) {
		
		double mean = 0;
		int beginIndex = begin.nextIndex();
		
		for(ListIterator<Double> lit = begin; (lit.hasNext() && lit != end); ) {
			mean = mean + lit.next();
		}
		
		mean = mean / ((end.previousIndex() + 1) - beginIndex);
		
		while(begin.hasPrevious()) {
			begin.previous();
		}
		
		double variance = 0;
		
		for(ListIterator<Double> lit = begin; (lit.hasNext() && lit != end); ) {
			variance = variance + Math.pow(lit.next() - mean, 2.0);
		}
		
		variance = variance / ((end.previousIndex() + 1) - beginIndex); // NDK version (end - begin + 1) ??
		
		return variance;
	}
	
	public double varianceFloat(ListIterator<Float> begin, ListIterator<Float> end) {
				
		float mean = 0;
		int beginIndex = begin.nextIndex();
		
		for(ListIterator<Float> lit = begin; (lit.hasNext() && lit != end); ) {
			mean = mean + lit.next();
		}
		
		mean = mean / ((end.previousIndex() + 1) - beginIndex);
		
		while(begin.hasPrevious()) {
			begin.previous();
		}
		
		double variance = 0;
		
		for(ListIterator<Float> lit = begin; (lit.hasNext() && lit != end); ) {
			variance = variance + Math.pow(lit.next() - mean, 2.0);
		}
		
		variance = variance / ((end.previousIndex() + 1) - beginIndex); // NDK version (end - begin + 1) ??
		
		return variance;
	}
	
	public double getAbsoluteMaximumFloat(ListIterator<Float> begin, ListIterator<Float> end) {
		
		double maxAbs = 0;
		for(ListIterator<Float> lit = begin; (lit.hasNext() && lit != end); ) {
			
			double currentValue = lit.next();
			
			Log.i("Features", "nextValue: " + currentValue);
			
			if(currentValue > maxAbs) {
				maxAbs = currentValue;
			}
		}
		
		return maxAbs;
	}
	
	public double getAbsoluteMaximumDouble(ListIterator<Double> begin, ListIterator<Double> end) {
		
		double maxAbs = 0;
		for(ListIterator<Double> lit = begin; (lit.hasNext() && lit != end); ) {
			
			double currentValue = lit.next();
			
			if(currentValue > maxAbs) {
				maxAbs = currentValue;
			}
		}
		
		return maxAbs;
	}

	public double zeroCrossingRateDouble(ListIterator<Double> begin, ListIterator<Double> end) {

		int numberOfZeroCrossings = 0;

		int beginIndex = begin.nextIndex();

		for(ListIterator<Double> lit = begin; (lit.hasNext() && lit != end); ) {

			double currentValue = lit.next();

			if((lit.next() * currentValue) < 0) {
				numberOfZeroCrossings++;
			}
		}

		double zeroCrossingRate = numberOfZeroCrossings / ((end.previousIndex() + 1) - beginIndex);

		return zeroCrossingRate;
	}

	public double zeroCrossingRateFloat(ListIterator<Float> begin, ListIterator<Float> end) {

		int numberOfZeroCrossings = 0;

		int beginIndex = begin.nextIndex();

		for(ListIterator<Float> lit = begin; (lit.hasNext() && lit != end); ) {

			double currentValue = lit.next();

			if((lit.next() * currentValue) < 0) {
				numberOfZeroCrossings++;
			}
		}

		double zeroCrossingRate = numberOfZeroCrossings / ((end.previousIndex() + 1) - beginIndex);

		return zeroCrossingRate;
	}
}
