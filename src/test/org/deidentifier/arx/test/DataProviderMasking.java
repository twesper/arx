/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.test;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.masking.AbstractInstanceMasker;

/**
 * Provides data for test cases
 * 
 * @author Prasser, Kohlmayer
 */
public class DataProviderMasking {

	public DataHandle getInputConstantShiftDecimal() {
	    
        DefaultData data = Data.create();
        data.add("values");
        
        data.add("1.0");
        data.add("10.0");
        data.add("-1.5");
        data.add("999999");
        data.add("-1000000");
        
        data.getDefinition().setDataType("values", DataType.DECIMAL);
        return data.getHandle();
	}

	public DataHandle getOutputConstantShiftDecimal(DataHandle input, double shift) {
		
		DefaultData data = Data.create();
        data.add("values");
        
        for (int row=0; row<input.getNumRows(); row++){
        	double value = DataType.DECIMAL.fromString(input.getValue(row, 0));
        	value += shift;
        	data.add(DataType.DECIMAL.toString(value));
        }
        
        data.getDefinition().setDataType("values", DataType.DECIMAL);
        return data.getHandle();
	}

	public <T> DataHandle getOutputGeneric (DataHandle input, AbstractInstanceMasker<T> masker, DataType<T> type) {
		
		DefaultData data = Data.create();
        data.add("values");
        
        for (int row=0; row<input.getNumRows(); row++){
        	// TODO: Replace with T mask(T)
        	//data.add(type.toString(masker.mask(type.fromString(input.getValue(row, 0)))));
        	data.add(masker.maskString(input.getValue(row, 0), type));
        }
        
        data.getDefinition().setDataType("values", type);
        return data.getHandle();
	}
	

	public DataHandle getInputConstantShiftDate() {
	    
        DefaultData data = Data.create();
        data.add("values");
        
        data.add("02.07.1980");
        data.add("03.07.1890");
        data.add("04.07.2020");
        //data.add("01.01.0000"); // There is no year 0 in the Gregorian calendar: http://en.wikipedia.org/wiki/0_%28year%29
        data.add("01.01.0001");
        data.add("01.01.1970");
        
        data.getDefinition().setDataType("values", DataType.DATE("dd.MM.yyyy"));
        return data.getHandle();
	}

	public DataHandle getOutputConstantShiftDate() {
	    
        DefaultData data = Data.create();
        data.add("values");
        
        data.add("05.07.1980");
        data.add("06.07.1890");
        data.add("07.07.2020");
        data.add("04.01.0001");
        data.add("04.01.1970");
        
        data.getDefinition().setDataType("values", DataType.DATE("dd.MM.yyyy"));
        return data.getHandle();
	}
	
}
