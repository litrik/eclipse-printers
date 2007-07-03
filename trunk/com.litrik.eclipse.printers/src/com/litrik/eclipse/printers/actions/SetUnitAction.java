/**
 Copyright 2007 Litrik De Roy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.litrik.eclipse.printers.actions;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;

import com.litrik.eclipse.printers.Activator;
import com.litrik.eclipse.printers.views.PrintersView;

/**
 * The action to refresh the view's content.
 */
public class SetUnitAction extends Action
{
	/**
	 * All supported units.
	 */
	public static final int UNIT_CM = 0;
	public static final int UNIT_INCH = 1;

	/**
	 * The label of the action.
	 */
	private static final String LABEL_CM = "cm";
	private static final String LABEL_INCH = "inch";

	/**
	 * The view where this action is used.
	 */
	private PrintersView fPrintersView;

	/*
	 * The unit of this action.
	 */
	private final int fUnit;

	/**
	 * The constructor.
	 * 
	 * @param view The view where this action is used.
	 */
	public SetUnitAction(PrintersView view, int unit)
	{
		super("", AS_RADIO_BUTTON); //$NON-NLS-1$
		switch (unit)
		{
			case UNIT_INCH:
				setText(LABEL_INCH);
				break;
			default:
				setText(LABEL_CM);
				break;
		}
		fPrintersView = view;
		fUnit = unit;
	}

	/*
	 * The actual code to execute when the actions runs.
	 */
	public void run()
	{
		try
		{
			// Ask the view to refresh itself using a specific unit.
			fPrintersView.refresh(fUnit);
		}
		catch (Exception e)
		{
			Activator.getDefault().getLog().log(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, "Failed to refresh the view.", e));
		}
	}
}