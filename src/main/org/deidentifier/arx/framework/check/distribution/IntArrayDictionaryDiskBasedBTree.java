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

import java.util.SortedMap;

import org.apache.jdbm.DB;
import org.apache.jdbm.DBMaker;

public class IntArrayDictionaryDiskBasedBTree implements IIntArrayDictionary {

    private final DB                                  db;
    private final SortedMap<IntArrayWrapper, Integer> dict;
    private final SortedMap<Integer, IntArrayWrapper> map;
    private int                                       currentIndex;

    public IntArrayDictionaryDiskBasedBTree(final int capacity) {
        db = DBMaker.openFile("jdbm" + System.nanoTime() + ".tmp").disableTransactions().deleteFilesAfterClose().make();
        map = db.createTreeMap("map");
        dict = db.createTreeMap("dict");
        currentIndex = 0;
    }

    @Override
    public void clear() {
        map.clear();
        dict.clear();
        currentIndex = 0;
    }

    @Override
    public void decrementRefCount(final int index) {
        // empty by design
    }

    @Override
    public int[] get(final int index) {
        return map.get(index).getArray();
    }

    @Override
    public int probe(final int[] key) {
        final IntArrayWrapper wrapper = new IntArrayWrapper(key);
        Integer idx = dict.get(wrapper);

        if (idx == null) { // no mapping available
            dict.put(wrapper, currentIndex);
            map.put(currentIndex, wrapper);
            // db.commit();
            idx = currentIndex;
            currentIndex++;
        }

        return idx;

    }

    @Override
    public int size() {
        return currentIndex;
    }
}
