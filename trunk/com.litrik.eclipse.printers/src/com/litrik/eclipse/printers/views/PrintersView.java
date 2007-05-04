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
	{ 200, 40, 120, 120, 120 };
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

	class NameSorter extends ViewerSorter
	{
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

		Table table = viewer.getTable();

		new TableColumn(table, SWT.LEFT);
		new TableColumn(table, SWT.RIGHT);
		new TableColumn(table, SWT.LEFT);
		new TableColumn(table, SWT.LEFT);
		new TableColumn(table, SWT.LEFT);

		for (int i = 0; i < table.getColumns().length; i++)
		{
			TableColumn column = table.getColumns()[i];
			column.setWidth(columnWidths[i]);
			column.setText(columnTitles[i]);
		}

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(getViewSite());
		viewer.setSorter(new NameSorter());

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