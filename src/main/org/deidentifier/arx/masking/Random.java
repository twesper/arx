package org.deidentifier.arx.masking;

import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.Well44497b;

/**
 * Provides quick access to an alternative random number generator used instead of the standard
 * RNG {@code java.util.Random}, because of its limitations. A superior RNG from the Apache 
 * Commons Math library is used in its place.
 * <p>
 * The class inherits from {@code org.apache.commons.math3.random.RandomAdaptor}, a wrapper for
 * {@code RandomGenerator}s so they can be used instead of a {@code java.util.Random}. 
 * 
 * @author Wesper
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