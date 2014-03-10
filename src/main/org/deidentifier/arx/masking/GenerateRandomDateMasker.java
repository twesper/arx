package org.deidentifier.arx.masking;

import java.util.Date;

import org.apache.commons.math3.distribution.IntegerDistribution;
import org.joda.time.ReadableDateTime;
import org.joda.time.ReadablePeriod;
import org.joda.time.Days;

/**
 * A masker that generates random dates using a given probability distribution and base date.
 * <p>
 * For date generation, a random integer is sampled from the distribution and is then
 * optionally shifted by a constant value. The random value is multiplied with the
 * base period (usually a day) to obtain a perturbation period. Finally, this random time
 * period is added to the given base date.
 * <p>
 * Example:<pre>
 * // Creating a masker that generates a random day in the year 2000:
 * 
 * DateTime startOf2000 = new DateTime(2000, 1, 1, 0, 0);			// = 1 January 2000 0:00
 * Days daysIn2000 = Days.daysBetween(startOf2000, startOf2000.plusYears(1));	// = 366; 2000 is a leap year
 * 
 * UniformIntegerDistribution distribution =
 * 	new UniformIntegerDistribution(0, daysIn2000.getDays() - 1);	// Samples from [0; 365]
 * 
 * GenerateRandomDateMasker masker =
 * 	new GenerateRandomDateMasker(distribution, startOf2000, Days.ONE);
 * </pre>
 * @author Wesper
 *
 */
public class GenerateRandomDateMasker extends AbstractReplaceInstMasker<Date> {

	private IntegerDistribution distribution;
	private int					shiftConstant = 0;
	private ReadableDateTime	baseDate;
	private ReadablePeriod		basePeriod = Days.ONE;
	
	public GenerateRandomDateMasker(IntegerDistribution distribution,
										ReadableDateTime baseDate) {
		this(distribution, 0, baseDate);
	}
	
	public GenerateRandomDateMasker(IntegerDistribution distribution,
										ReadableDateTime baseDate, ReadablePeriod basePeriod) {
		this(distribution, 0, baseDate, basePeriod);
	}
	
	public GenerateRandomDateMasker(IntegerDistribution distribution, int shiftConstant,
										ReadableDateTime baseDate) {
		this(distribution, shiftConstant, baseDate, Days.ONE);
	}
	
	public GenerateRandomDateMasker(IntegerDistribution distribution, int shiftConstant,
										ReadableDateTime baseDate, ReadablePeriod basePeriod) {
		this.distribution	= distribution;
		this.shiftConstant	= shiftConstant;
		this.baseDate		= baseDate;
		this.basePeriod		= basePeriod;
	}
	
	@Override
	public Date createReplacement() {
		int randomShift = distribution.sample() + shiftConstant;
		ReadablePeriod randomPeriod = basePeriod.toPeriod().multipliedBy(randomShift);
		ReadableDateTime generatedDate = baseDate.toDateTime().plus(randomPeriod);
		
		return generatedDate.toDateTime().toDate();
	}

}
