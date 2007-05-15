/*
 * Created on 7-jan-2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.litrik.eclipse.printers.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;

import com.litrik.eclipse.printers.Activator;
import com.litrik.eclipse.printers.views.PrintersView;

public class RefreshAction extends Action
{
	private static final String LABEL = "Refresh";

	private PrintersView fPrintersView;
	
	public RefreshAction(PrintersView view)
	{
		super(LABEL, SWT.NONE);
		setToolTipText(LABEL);
		setImageDescriptor(Activator.getImageDescriptor("icons/elcl16/refresh.gif"));
		
		fPrintersView = view;
	}

	public void run()
	{
		try
		{
			fPrintersView.refresh();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}