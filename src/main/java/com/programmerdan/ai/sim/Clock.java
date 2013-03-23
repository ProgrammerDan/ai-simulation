package com.programmerdan.ai.sim;

/**
 * Every simulation needs a Clock to keep things moving.
 * This is a simple implementation of a clock for this simulation
 * framework.
 * 
 * @author ProgrammerDan/Daniel Boston
 * @version 1.0 October 20, 2012
 *
 */
public class Clock {
	private Double clock = 0.0; 
	private Double clockStart = 0.0;	
	private Double clockStep = 1.0;
	private Double clockEnd = 4000.0;
	
	/**
	 * No parameter constructor, builds a Clock with
	 * default values of 0 start, 1 step, 4000 end.
	 * 
	 * Calls {@link #reset()}.
	 */
	public Clock() {
		clockStart = 0.0;
		clockStep = 1.0;
		clockEnd = 4000.0;
		
		reset();
	}
	
	/**
	 * Full configuration constructor, builds a Clock
	 * to specification. There are a few cases that will cause an
	 * error:
	 *  * step = 0
	 *  * end < start and step > 0
	 *  * end > start and step < 0
	 * These are error states as they will prevent the
	 * Clock from ever ending.
	 *  
	 * @param start The Clock starting value
	 * @param step The Clock step value
	 * @param end The Clock end value
	 */
	public Clock(Double start, Double step, Double end) {
		if ( (step.doubleValue() == 0.0) || // zero step value
				(end.compareTo(start) < 0 && step.compareTo(0.0) > 0) ||  // end before start with positive step
				(end.compareTo(start) > 0 && step.compareTo(0.0) <= 0) ) { // end after start with negative step
			throw new ClockException("Invalid Clock Configuration");
		} else {
			clockStart = start;
			clockEnd = end;
			clockStep = step;
			
			reset();
		}
	}
	
	/**
	 * Sets the clock to the current {@link #clockStart} value.
	 */
	public final void reset() {
		clock = clockStart;
	}
	
	/**
	 * Returns the current value of the clock
	 * 
	 * @return the current clock value
	 */
	public final Double now() {
		return clock.doubleValue();
	}
	
	/**
	 * If clock is at the start, this will return true.
	 * 
	 * @return true if at start, false otherwise.
	 */
	public final boolean isAtStart() {
		return clock.equals(clockStart);
	}
	
	/**
	 * If clock is at the end, this will return true.
	 * 
	 * @return true if at end, false otherwise
	 */
	public final boolean isAtEnd() {
		return clock.equals(clockEnd);
	}
	
	/**
	 * Steps the clock. Enforces clock boundaries, both
	 * start and end boundaries, ensuring the clock never
	 * exceeds those bounds.
	 * 
	 * @return The new Clock value.
	 */
	public final Double step() {
		clock += clockStep;
		
		if (clock.compareTo(clockEnd) > 0) {
			clock = clockEnd;
		}
		
		if (clock.compareTo(clockStart) < 0) {
			clock = clockStart;
		}
		
		return now();
	}
	
	/**
	 * Sets the default starting value of the start.
	 * Does not reset the clock!
	 * 
	 * @param start The default start value
	 */
	public final void setStart(Double start) {
		clockStart = start;
	}
	
	/**
	 * Sets the step value for the clock. Using a
	 * double means that the size of step could be
	 * adjusted to any granularity. This does not
	 * reset the clock, so could be used to adaptively
	 * adjust the clock step during the simulation, 
	 * if designed to do so.
	 * 
	 * @param step The step size to use.
	 */
	public final void setStep(Double step) {
		clockStep = step;
	}
	
	/**
	 * Sets the ending/highest allowed value of the clock.
	 * When stepping the clock, if a step would exceed this value,
	 * the clock is truncated at this value.
	 * 
	 * @param end The ending/highest value for the clock.
	 */
	public final void setEnd(Double end) {
		clockEnd = end;
	}
}
