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

package org.deidentifier.arx.framework.check.transformer;

import org.deidentifier.arx.framework.Configuration;
import org.deidentifier.arx.framework.check.distribution.IntArrayDictionary;
import org.deidentifier.arx.framework.data.Memory;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;

/**
 * The class Transformer12.
 * 
 * @author Prasser, Kohlmayer
 */
public class Transformer12 extends AbstractTransformer {

    /**
     * Instantiates a new transformer.
     * 
     * @param data
     *            the data
     * @param hierarchies
     *            the hierarchies
     */
    public Transformer12(final Memory data,
                         final GeneralizationHierarchy[] hierarchies,
                         final int[] sensitiveValues,
                         final IntArrayDictionary dictionarySensValue,
                         final IntArrayDictionary dictionarySensFreq,
                         final Configuration config) {
        super(data, hierarchies, sensitiveValues, dictionarySensValue, dictionarySensFreq, config);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deidentifier.ARX.framework.check.transformer.AbstractTransformer
     * #walkAll()
     */
    @Override
    protected void processAll() {
        for (int i = startIndex; i < stopIndex; i++) {

            buffer.set(i,outindex0, idindex0[data.get(i, index0)][stateindex0]);
            buffer.set(i,outindex1, idindex1[data.get(i, index1)][stateindex1]);
            buffer.set(i,outindex2, idindex2[data.get(i, index2)][stateindex2]);
            buffer.set(i,outindex3, idindex3[data.get(i, index3)][stateindex3]);
            buffer.set(i,outindex4, idindex4[data.get(i, index4)][stateindex4]);
            buffer.set(i,outindex5, idindex5[data.get(i, index5)][stateindex5]);
            buffer.set(i,outindex6, idindex6[data.get(i, index6)][stateindex6]);
            buffer.set(i,outindex7, idindex7[data.get(i, index7)][stateindex7]);
            buffer.set(i,outindex8, idindex8[data.get(i, index8)][stateindex8]);
            buffer.set(i,outindex9, idindex9[data.get(i, index9)][stateindex9]);
            buffer.set(i,outindex10, idindex10[data.get(i, index10)][stateindex10]);
            buffer.set(i,outindex11, idindex11[data.get(i, index11)][stateindex11]);

            switch (config.getCriterion()) {
            case K_ANONYMITY:
                groupify.add(i, 1);
                break;
            case L_DIVERSITY:
            case T_CLOSENESS:
                groupify.add(i, 1, sensitiveValues[i]);
                break;
            case D_PRESENCE:
                groupify.addD(i, 1, 1);
                break;
            default:
                throw new UnsupportedOperationException(config.getCriterion() + ": currenty not supported");
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deidentifier.ARX.framework.check.transformer.AbstractTransformer
     * #walkGroupify ()
     */
    @Override
    protected void processGroupify() {
        int processed = 0;
        while (element != null) {

            buffer.set(element.representant,outindex0, idindex0[data.get(element.representant, index0)][stateindex0]);
            buffer.set(element.representant,outindex1, idindex1[data.get(element.representant, index1)][stateindex1]);
            buffer.set(element.representant,outindex2, idindex2[data.get(element.representant, index2)][stateindex2]);
            buffer.set(element.representant,outindex3, idindex3[data.get(element.representant, index3)][stateindex3]);
            buffer.set(element.representant,outindex4, idindex4[data.get(element.representant, index4)][stateindex4]);
            buffer.set(element.representant,outindex5, idindex5[data.get(element.representant, index5)][stateindex5]);
            buffer.set(element.representant,outindex6, idindex6[data.get(element.representant, index6)][stateindex6]);
            buffer.set(element.representant,outindex7, idindex7[data.get(element.representant, index7)][stateindex7]);
            buffer.set(element.representant,outindex8, idindex8[data.get(element.representant, index8)][stateindex8]);
            buffer.set(element.representant,outindex9, idindex9[data.get(element.representant, index9)][stateindex9]);
            buffer.set(element.representant,outindex10, idindex10[data.get(element.representant, index10)][stateindex10]);
            buffer.set(element.representant,outindex11, idindex11[data.get(element.representant, index11)][stateindex11]);

            switch (config.getCriterion()) {
            case K_ANONYMITY:
                groupify.add(element.representant, element.count);
                break;
            case L_DIVERSITY:
            case T_CLOSENESS:
                groupify.add(element.representant, element.count, element.distribution);
                break;
            case D_PRESENCE:
                groupify.addD(element.representant, element.count, element.pcount);
                break;
            default:
                throw new UnsupportedOperationException(config.getCriterion() + ": currenty not supported");
            }

            // Next element
            processed++;
            if (processed == numElements) { return; }
            element = element.nextOrdered;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deidentifier.ARX.framework.check.transformer.AbstractTransformer
     * #walkSnapshot ()
     */
    @Override
    protected void processSnapshot() {
        startIndex *= ssStepWidth;
        stopIndex *= ssStepWidth;

        for (int i = startIndex; i < stopIndex; i += ssStepWidth) {
            buffer.set(snapshot[i],outindex0, idindex0[data.get(snapshot[i], index0)][stateindex0]);
            buffer.set(snapshot[i],outindex1, idindex1[data.get(snapshot[i], index1)][stateindex1]);
            buffer.set(snapshot[i],outindex2, idindex2[data.get(snapshot[i], index2)][stateindex2]);
            buffer.set(snapshot[i],outindex3, idindex3[data.get(snapshot[i], index3)][stateindex3]);
            buffer.set(snapshot[i],outindex4, idindex4[data.get(snapshot[i], index4)][stateindex4]);
            buffer.set(snapshot[i],outindex5, idindex5[data.get(snapshot[i], index5)][stateindex5]);
            buffer.set(snapshot[i],outindex6, idindex6[data.get(snapshot[i], index6)][stateindex6]);
            buffer.set(snapshot[i],outindex7, idindex7[data.get(snapshot[i], index7)][stateindex7]);
            buffer.set(snapshot[i],outindex8, idindex8[data.get(snapshot[i], index8)][stateindex8]);
            buffer.set(snapshot[i],outindex9, idindex9[data.get(snapshot[i], index9)][stateindex9]);
            buffer.set(snapshot[i],outindex10, idindex10[data.get(snapshot[i], index10)][stateindex10]);
            buffer.set(snapshot[i],outindex11, idindex11[data.get(snapshot[i], index11)][stateindex11]);

            switch (config.getCriterion()) {
            case K_ANONYMITY:
                groupify.add(snapshot[i], snapshot[i + 1]);
                break;
            case L_DIVERSITY:
            case T_CLOSENESS:
                groupify.add(snapshot[i], snapshot[i + 1], dictionarySensValue.get(snapshot[i + 2]), dictionarySensFreq.get(snapshot[i + 3]));
                break;
            case D_PRESENCE:
                groupify.addD(snapshot[i], snapshot[i + 1], snapshot[i + 2]);
                break;
            default:
                throw new UnsupportedOperationException(config.getCriterion() + ": currenty not supported");
            }
        }
    }
}
