package org.deidentifier.arx.masking;

import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.Well44497b;

/**
 * Provides quick access to a random number generator class used instead of
 * {@code java.util.Random} because of its limitations. A superior RNG of the Apache Commons
 * Math library is used in its place.
 * <p>
 * The class inherits from {@code RandomAdaptor}, a wrapper for the {@code RandomGenerator}s of
 * the Commons Math library so it can be used instead of a {@code java.util.Random}. 
 * 
 * @author Tobias Wesper
 */
public final class Random extends RandomAdaptor {
	
	private static final long serialVersionUID = -8827826017115532703L;

	/**
	 * Creates a new RNG object using the {@code org.apache.commons.math3.random.Well44497b}
	 * RNG. Seeded using system time.
	 */
	public Random() {
		super(new Well44497b());
	}
	
}