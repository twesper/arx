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

package org.deidentifier.arx.gui.view.impl.menu;

import org.deidentifier.arx.DataType;
import org.deidentifier.arx.gui.Controller;
import org.deidentifier.arx.gui.resources.Resources;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

public class WizardHierarchy extends Wizard implements IWizard {

    private final WizardHierarchyModel model;
    private WizardDialog               dialog;
    private final Controller           controller;

    public WizardHierarchy(final Controller controller,
                           final String attribute,
                           final DataType<?> datatype,
                           final String suppressionString,
                           final String[] items) {
        super();
        model = new WizardHierarchyModel(attribute,
                                         datatype,
                                         suppressionString,
                                         items);
        this.controller = controller;
        setWindowTitle(Resources.getMessage("HierarchyWizard.0")); //$NON-NLS-1$
        setDefaultPageImageDescriptor(ImageDescriptor.createFromImage(controller.getResources()
                                                                                .getImage("wizard.png"))); //$NON-NLS-1$
    }

    @Override
    public void addPages() {
        final WizardHierarchyPageLabels check = new WizardHierarchyPageLabels(model);
        addPage(new WizardHierarchyPageOrder(controller, model));
        addPage(new WizardHierarchyPageFanout(model));
        addPage(check);
    }

    @Override
    public boolean canFinish() {
        return (model.getHierarchy() != null) && (dialog != null) &&
               (dialog.getCurrentPage() == getPages()[2]);
    }

    public WizardHierarchyModel getModel() {
        return model;
    }

    public boolean open(final Shell shell) {
        final WizardDialog dialog = new WizardDialog(shell, this);
        this.dialog = dialog;
        return dialog.open() == 0;
    }

    @Override
    public boolean performFinish() {
        return true;
    }
}
