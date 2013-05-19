/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx.framework.check.distribution;

import org.deidentifier.arx.framework.check.history.IntArraySwapFile;

public class IntArrayDictionaryDiskBased implements IIntArrayDictionary {

    private int                    lastIndex;

    private final IntArraySwapFile swapFile;

    public IntArrayDictionaryDiskBased(final int capacity) {
        swapFile = new IntArraySwapFile("distributionDict");
        lastIndex = 0;
    }

    @Override
    public void clear() {
        swapFile.clear();
    }

    @Override
    public void decrementRefCount(final int index) {
        // empty by design
    }

    @Override
    public int[] get(final int index) {
        return swapFile.read(index);
    }

    @Override
    public int probe(final int[] key) {
        final int saveIndex = lastIndex;
        lastIndex++;
        swapFile.write(saveIndex, key);
        return saveIndex;
    }

    @Override
    public int size() {
        return lastIndex;
    }
}
