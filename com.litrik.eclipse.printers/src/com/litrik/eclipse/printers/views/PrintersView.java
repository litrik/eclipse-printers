package com.litrik.eclipse.printers.views;

import java.text.DecimalFormat;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
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
 * 
 */

public class PrintersView extends ViewPart
{
	private TableViewer viewer;

	private Action fRefreshAction;

	private static int columnWidths[] =
	{ 200, 60, 120, 120, 120 };
	private static String columnWidthMementoKeys[] =
	{ "MEMENTO_WIDTH_COLUMN_0", "MEMENTO_WIDTH_COLUMN_1", "MEMENTO_WIDTH_COLUMN_2", "MEMENTO_WIDTH_COLUMN_3",
			"MEMENTO_WIDTH_COLUMN_4" };
	private static String columnTitles[] =
	{ "Name", "DPI", "Page Size", "Printable Area", "Trim" };

	class ViewContentProvider implements IStructuredContentProvider
	{
		public void inputChanged(Viewer v, Object oldInput, Object newInput)
		{}

		public void dispose()
		{}

		public Object[] getElements(Object parent)
		{
			return Printer.getPrinterList();
		}
	}

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider
	{
		public String getColumnText(Object obj, int index)
		{
			PrinterData printerData = (PrinterData) obj;
			Printer printer = new Printer(printerData);
			String text = null;
			switch (index)
			{
				case 0:
					text = printerData.name;
					break;
				case 1:
					text = Integer.toString(printer.getDPI().x);
					break;
				case 2:
					text = pixelsToCm(printer.getBounds().width, printer.getDPI().x) + " x "
							+ pixelsToCm(printer.getBounds().height, printer.getDPI().y);
					break;
				case 3:
					text = pixelsToCm(printer.getClientArea().width, printer.getDPI().x) + " x "
							+ pixelsToCm(printer.getClientArea().height, printer.getDPI().y);
					break;
				case 4:
					text = pixelsToCm(printer.computeTrim(0, 0, 0, 0).x, printer.getDPI().x) + " x "
							+ pixelsToCm(printer.computeTrim(0, 0, 0, 0).y, printer.getDPI().y);
					break;
				default:
					break;
			}
			printer.dispose();
			return text;
		}

		private String pixelsToCm(int pixels, int dpi)
		{
			DecimalFormat format = new DecimalFormat("########0.00");
			return format.format(pixels * 2.54 / dpi);
		}

		public Image getColumnImage(Object obj, int index)
		{
			return null;
		}

	}

	class PrinterSorter extends ViewerSorter
	{
		private int criteria;

		public PrinterSorter(int criteria)
		{
			super();
			this.criteria = criteria;
		}

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
					result = collator.compare(printer1.getPrinterData().name, printer2.getPrinterData().name);
					break;
				case 1:
					result = printer1.getDPI().x - printer2.getDPI().x;
					result = result < 0 ? -1 : (result > 0) ? 1 : 0;
					break;
				case 2:
					result = (printer1.getBounds().width * 1000 / printer1.getDPI().x) - (printer2.getBounds().width * 1000 / printer2.getDPI().x);
					result = result < 0 ? -1 : (result > 0) ? 1 : 0;
					break;
				case 3:
					result = (printer1.getClientArea().width  * 1000 / printer1.getDPI().x) - (printer2.getClientArea().width  * 1000 / printer2.getDPI().x);
					result = result < 0 ? -1 : (result > 0) ? 1 : 0;
					break;
				case 4:
					result = (printer1.computeTrim(0, 0, 0, 0).x  * 1000 / printer1.getDPI().x) - (printer2.computeTrim(0, 0, 0, 0).x  * 1000 / printer1.getDPI().x);
					result = result < 0 ? -1 : (result > 0) ? 1 : 0;
					break;
				default:
					result = 0;
			}
			printer1.dispose();
			printer2.dispose();
			return result;
		}

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

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
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
						if(table.getColumns()[j] == (TableColumn)(e.widget))
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

	private void contributeToActionBars()
	{
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalToolBar(IToolBarManager manager)
	{
		manager.add(fRefreshAction);
	}

	private void makeActions()
	{
		fRefreshAction = new RefreshAction(this);
	}

	private void showMessage(String message)
	{
		MessageDialog.openInformation(viewer.getControl().getShell(), "Printers", message);
	}

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

	public void saveState(IMemento memento)
	{
		super.saveState(memento);
		Table table = viewer.getTable();

		for (int i = 0; i < columnWidthMementoKeys.length; i++)
		{
			memento.putInteger(columnWidthMementoKeys[i], table.getColumn(i).getWidth());
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus()
	{
		viewer.getControl().setFocus();
	}

	public void refresh()
	{
		viewer.refresh();
	}

}