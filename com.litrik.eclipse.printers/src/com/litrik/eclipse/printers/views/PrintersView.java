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

package com.litrik.eclipse.printers.views;

import java.text.DecimalFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.litrik.eclipse.printers.Activator;
import com.litrik.eclipse.printers.actions.RefreshAction;
import com.litrik.eclipse.printers.actions.SetUnitAction;

/**
 * View displaying all known printers.
 */
public class PrintersView extends ViewPart
{
	/**
	 * The JFace viewer that will hold the printers.
	 */
	private TableViewer viewer;

	/**
	 * The action to refresh the list of printers.
	 */
	private Action fRefreshAction;
	private Action fPrintAction;
	private SetUnitAction[] fSetUnitActions;

	/**
	 * The width of each column.
	 */
	private static int columnWidths[] =
	{ 200, 60, 120, 120, 120 };

	/**
	 * The memento keys
	 */
	private static String columnWidthMementoKeys[] =
	{ "MEMENTO_WIDTH_COLUMN_0", "MEMENTO_WIDTH_COLUMN_1", "MEMENTO_WIDTH_COLUMN_2", "MEMENTO_WIDTH_COLUMN_3",
			"MEMENTO_WIDTH_COLUMN_4" };
	private static String unitMementoKey = "MEMENTO_UNIT";

	/**
	 * The title of each column.
	 */
	private static String columnTitles[] =
	{ "Name", "DPI", "Page Size", "Printable Area", "Trim" };

	/*
	 * The current unit.
	 */
	private int fUnit = SetUnitAction.UNIT_CM;

	/*
	 * The Units sub menu.
	 */
	private static String LABEL_UNITS = "Units";

	/**
	 * The content provider responsible for obtaining the actual list of known
	 * printers.
	 */
	class ViewContentProvider implements IStructuredContentProvider
	{
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer v, Object oldInput, Object newInput)
		{
		// Do nothing
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose()
		{
		// Do nothing
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object parent)
		{
			// Get the list of known printers.
			return Printer.getPrinterList();
		}
	}

	/**
	 * The label provider responsible for generating the labels for each column.
	 */
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider
	{
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
		 *      int)
		 */
		public String getColumnText(Object obj, int index)
		{
			PrinterData printerData = (PrinterData) obj;
			Printer printer = new Printer(printerData);
			String text = null;
			switch (index)
			{
				case 0:
					// The "Name" column.
					text = printerData.name;
					break;
				case 1:
					// The "DPI" column.
					text = Integer.toString(printer.getDPI().x);
					break;
				case 2:
					// The "Page Size" column.
					text = formatPixels(printer.getBounds().width, printer.getDPI().x) + " x "
							+ formatPixels(printer.getBounds().height, printer.getDPI().y);
					break;
				case 3:
					// The "Printable Area" column.
					text = formatPixels(printer.getClientArea().width, printer.getDPI().x) + " x "
							+ formatPixels(printer.getClientArea().height, printer.getDPI().y);
					break;
				case 4:
					// The "Trim" column.
					text = formatPixels(printer.computeTrim(0, 0, 0, 0).x, printer.getDPI().x) + " x "
							+ formatPixels(printer.computeTrim(0, 0, 0, 0).y, printer.getDPI().y);
					break;
				default:
					break;
			}
			printer.dispose();
			return text;
		}

		/**
		 * Convert pixels to the current unit
		 * 
		 * @param pixels the number of pixels to convert
		 * @param dpi the DPI
		 * @return the number of units for the specified pixels at the specified
		 *         DPI
		 */
		private String formatPixels(int pixels, int dpi)
		{
			DecimalFormat format = new DecimalFormat("########0.00");
			return format.format(pixels * (fUnit == SetUnitAction.UNIT_CM ? 2.54 : 1) / dpi);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
		 *      int)
		 */
		public Image getColumnImage(Object obj, int index)
		{
			return null;
		}

	}

	/**
	 * The sorter responsible for sorting each column.
	 */
	class PrinterSorter extends ViewerSorter
	{
		/**
		 * Current sort column.
		 */
		private int criteria;

		/**
		 * The constructor.
		 * 
		 * @param criteria the column to sort
		 */
		public PrinterSorter(int criteria)
		{
			super();
			this.criteria = criteria;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		public int compare(Viewer viewer, Object o1, Object o2)
		{

			PrinterData printerData1 = (PrinterData) o1;
			PrinterData printerData2 = (PrinterData) o2;
			Printer printer1 = new Printer(printerData1);
			Printer printer2 = new Printer(printerData2);

			int result = 0;

			switch (criteria)
			{
				case 0:
					// The "Name" column.
					result = collator.compare(printer1.getPrinterData().name, printer2.getPrinterData().name);
					break;
				case 1:
					// The "DPI" column.
					result = printer1.getDPI().x - printer2.getDPI().x;
					result = result < 0 ? -1 : (result > 0) ? 1 : 0;
					break;
				case 2:
					// The "Page Size" column.
					result = (printer1.getBounds().width * 1000 / printer1.getDPI().x)
							- (printer2.getBounds().width * 1000 / printer2.getDPI().x);
					result = result < 0 ? -1 : (result > 0) ? 1 : 0;
					break;
				case 3:
					// The "Printable Area" column.
					result = (printer1.getClientArea().width * 1000 / printer1.getDPI().x)
							- (printer2.getClientArea().width * 1000 / printer2.getDPI().x);
					result = result < 0 ? -1 : (result > 0) ? 1 : 0;
					break;
				case 4:
					// The "Trim" column.
					result = (printer1.computeTrim(0, 0, 0, 0).x * 1000 / printer1.getDPI().x)
							- (printer2.computeTrim(0, 0, 0, 0).x * 1000 / printer1.getDPI().x);
					result = result < 0 ? -1 : (result > 0) ? 1 : 0;
					break;
				default:
					result = 0;
			}
			printer1.dispose();
			printer2.dispose();
			return result;
		}

		/**
		 * Returns the current sort column.
		 * 
		 * @return the current sort column
		 */
		public int getCriteria()
		{
			return criteria;
		}
	}

	/**
	 * The constructor.
	 */
	public PrintersView()
	{
	// Do nothing
	}

	/*
	 * Create the SWT controls for this view.
	 */
	public void createPartControl(Composite parent)
	{
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

		final Table table = viewer.getTable();

		for (int i = 0; i < columnTitles.length; i++)
		{
			TableColumn column = new TableColumn(table, SWT.LEFT);
			column.setWidth(columnWidths[i]);
			column.setText(columnTitles[i]);
			column.addSelectionListener(new SelectionAdapter()
			{

				public void widgetSelected(SelectionEvent e)
				{
					for (int j = 0; j < table.getColumns().length; j++)
					{
						if (table.getColumns()[j] == (TableColumn) (e.widget))
						{
							viewer.setSorter(new PrinterSorter(j));
							viewer.getTable().setSortColumn(viewer.getTable().getColumns()[j]);
						}
					}
				}
			});
		}

		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setSortDirection(SWT.UP);
		table.setSortColumn(table.getColumns()[0]);

		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(getViewSite());
		viewer.setSorter(new PrinterSorter(0));

		makeActions();
		contributeToActionBars();
	}

	/**
	 * Make all contributions to the action bars.
	 */
	private void contributeToActionBars()
	{
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
		fillLocalMenuBar(bars.getMenuManager());
	}

	/**
	 * Fill the local tool bar.
	 * 
	 * @param manager the tool bar manager.
	 */
	private void fillLocalToolBar(IToolBarManager manager)
	{
		manager.add(fRefreshAction);
		manager.add(fPrintAction);
	}

	/**
	 * Fill the local menu bar.
	 * 
	 * @param manager the menu bar manager.
	 */
	private void fillLocalMenuBar(IMenuManager manager)
	{
		MenuManager unitsSubMenu = new MenuManager(LABEL_UNITS);
		for (int i = 0; i < fSetUnitActions.length; i++)
		{
			unitsSubMenu.add(fSetUnitActions[i]);
		}
		manager.add(unitsSubMenu);
		fSetUnitActions[fUnit].setChecked(true);
	}

	/**
	 * Make all the different actions.
	 */
	private void makeActions()
	{
		fRefreshAction = new RefreshAction(this);
		fPrintAction = new Action()
		{
			public void run()
			{
				GC gc = null;
				Printer printer = null;
				try
				{
					PrinterData printerData = (PrinterData)((IStructuredSelection)viewer.getSelection()).getFirstElement();
					if (printerData != null)
					{
						printer = new Printer(printerData);
						gc = new GC(printer);
						printer.startJob("Hello");
						printer.startPage();
						Color black = printer.getSystemColor(SWT.COLOR_BLACK);
						gc.setForeground(black);
						gc.setLineWidth(1);
						for (int i = 0; i < 6; i++)
						{
							// Vertical, left
							gc.drawLine(100 * i, 0, 100 * i, printer.getBounds().height);
							// Vertical, right
							gc.drawLine(printer.getBounds().width - 100 * i, 0, printer.getBounds().width - 100 * i, printer
									.getBounds().height);
							// Horizontal, top
							gc.drawLine(0, 100 * i, printer.getBounds().width, 100 * i);
							// Horizontal, bottom
							gc.drawLine(0, printer.getBounds().height - 100 * i, printer.getBounds().width,
									printer.getBounds().height - 100 * i);
						}
						printer.endPage();
						printer.endJob();
					}
				}
				catch (Exception e)
				{
					Activator.getDefault().getLog().log(
							new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, "Failed to print a test page.", e));
				}
				finally
				{
					if (gc != null)
					{
						gc.dispose();
					}
					if (printer != null)
					{
						printer.dispose();
					}
				}
			}
		};
		fPrintAction.setToolTipText("Print Test Page");
		fPrintAction.setImageDescriptor(Activator.getImageDescriptor("icons/elcl16/test.gif"));
		fSetUnitActions = new SetUnitAction[]
		{ new SetUnitAction(this, SetUnitAction.UNIT_CM), new SetUnitAction(this, SetUnitAction.UNIT_INCH) };
	}

	/*
	 * Initialize the view by restoring all settings
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException
	{
		super.init(site, memento);

		if (memento != null)
		{
			// Restore the width of each column.
			for (int i = 0; i < columnWidthMementoKeys.length; i++)
			{
				String mementoKey = columnWidthMementoKeys[i];
				if (memento.getInteger(mementoKey) != null)
				{
					columnWidths[i] = memento.getInteger(mementoKey).intValue();
				}
			}

			// Restore the current unit.
			if (memento.getInteger(unitMementoKey) != null)
			{
				fUnit = memento.getInteger(unitMementoKey).intValue();
			}
		}
	}

	/*
	 * Save the view's state by saving all settings
	 * 
	 * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento)
	{
		super.saveState(memento);
		Table table = viewer.getTable();

		// Save the width of each column.
		for (int i = 0; i < columnWidthMementoKeys.length; i++)
		{
			memento.putInteger(columnWidthMementoKeys[i], table.getColumn(i).getWidth());
		}

		// Save the current unit.
		memento.putInteger(unitMementoKey, fUnit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus()
	{
		viewer.getControl().setFocus();
	}

	/**
	 * Refresh the view's content.
	 */
	public void refresh()
	{
		viewer.refresh();
	}

	/**
	 * Refresh the view's content using a specific unit.
	 */
	public void refresh(int unit)
	{
		fUnit = unit;
		viewer.refresh();
	}
}