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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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

import com.litrik.eclipse.printers.actions.RefreshAction;

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

	/**
	 * The width of each column.
	 */
	private static int columnWidths[] =
	{ 200, 60, 120, 120, 120 };

	/**
	 * The memento keys to store the width of each column.
	 */
	private static String columnWidthMementoKeys[] =
	{ "MEMENTO_WIDTH_COLUMN_0", "MEMENTO_WIDTH_COLUMN_1", "MEMENTO_WIDTH_COLUMN_2", "MEMENTO_WIDTH_COLUMN_3",
			"MEMENTO_WIDTH_COLUMN_4" };

	/**
	 * The title of each column.
	 */
	private static String columnTitles[] =
	{ "Name", "DPI", "Page Size", "Printable Area", "Trim" };

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
					text = pixelsToCm(printer.getBounds().width, printer.getDPI().x) + " x "
							+ pixelsToCm(printer.getBounds().height, printer.getDPI().y);
					break;
				case 3:
					// The "Printable Area" column.
					text = pixelsToCm(printer.getClientArea().width, printer.getDPI().x) + " x "
							+ pixelsToCm(printer.getClientArea().height, printer.getDPI().y);
					break;
				case 4:
					// The "Trim" column.
					text = pixelsToCm(printer.computeTrim(0, 0, 0, 0).x, printer.getDPI().x) + " x "
							+ pixelsToCm(printer.computeTrim(0, 0, 0, 0).y, printer.getDPI().y);
					break;
				default:
					break;
			}
			printer.dispose();
			return text;
		}

		/**
		 * Convert pixels to cm
		 * 
		 * @param pixels the number of pixels to convert
		 * @param dpi the DPI
		 * @return the number of cm for the specified pixels at the specified
		 *         DPI
		 */
		private String pixelsToCm(int pixels, int dpi)
		{
			DecimalFormat format = new DecimalFormat("########0.00");
			return format.format(pixels * 2.54 / dpi);
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
	{}

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
	}

	/**
	 * Fill the local tool bar.
	 * 
	 * @param manager the tool bar manager.
	 */
	private void fillLocalToolBar(IToolBarManager manager)
	{
		manager.add(fRefreshAction);
	}

	/**
	 * Make all the different actions.
	 */
	private void makeActions()
	{
		fRefreshAction = new RefreshAction(this);
	}

	/*
	 * Initialize the view by restoring the width of each column.
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException
	{
		super.init(site, memento);

		if (memento != null)
		{
			for (int i = 0; i < columnWidthMementoKeys.length; i++)
			{
				String mementoKey = columnWidthMementoKeys[i];
				if (memento.getInteger(mementoKey) != null)
				{
					columnWidths[i] = memento.getInteger(mementoKey).intValue();
				}
			}
		}
	}

	/*
	 * Save the view's state by saving the width of each column.
	 * 
	 * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento)
	{
		super.saveState(memento);
		Table table = viewer.getTable();

		for (int i = 0; i < columnWidthMementoKeys.length; i++)
		{
			memento.putInteger(columnWidthMementoKeys[i], table.getColumn(i).getWidth());
		}
	}

	/* (non-Javadoc)
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
}