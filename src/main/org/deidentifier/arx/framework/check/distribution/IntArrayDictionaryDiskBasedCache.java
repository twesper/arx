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

import java.util.HashMap;

import org.deidentifier.arx.framework.check.history.IntArraySwapFile;
import org.deidentifier.arx.framework.check.history.MRUCache;

public class IntArrayDictionaryDiskBasedCache implements IIntArrayDictionary {

    private static final int              CACHE_SIZE = 1000;
    private int                           lastIndex;
    private final IntArraySwapFile        swapFile;
    private final MRUCache<Integer>       cache;
    private final HashMap<Integer, int[]> cacheToEntry;

    public IntArrayDictionaryDiskBasedCache(final int capacity) {
        swapFile = new IntArraySwapFile("distributionDict");
        lastIndex = 0;
        this.cache = new MRUCache<Integer>(CACHE_SIZE);
        this.cacheToEntry = new HashMap<Integer, int[]>();
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
    public int[] get(final int idx) {
        final Integer index = idx;
        int[] result = cacheToEntry.get(index);
        if (result == null) {
            result = readFromDiskAndCache(index);
        } else {
            cache.touch(index);
        }
        return result;
    }

    private int[] readFromDiskAndCache(final Integer index) {
        final int[] snapshot = swapFile.read(index);
        cache(index, snapshot);
        return snapshot;
    }

    private void cache(final Integer index, final int[] snapshot) {
        // if cache size is to large purge
        if (cache.size() >= CACHE_SIZE) {
            final Integer lruIdx = cache.removeHead();
            final int[] value = cacheToEntry.remove(lruIdx);
            if (!swapFile.containsSnapshot(lruIdx)) {
                swapFile.write(lruIdx, value);
            }
        }

        cache.append(index);
        cacheToEntry.put(index, snapshot);
    }

    @Override
    public int probe(final int[] key) {
        final int saveIndex = lastIndex;
        cache(saveIndex, key);
        lastIndex++;
        return saveIndex;
    }

    @Override
    public int size() {
        return lastIndex;
    }
}
