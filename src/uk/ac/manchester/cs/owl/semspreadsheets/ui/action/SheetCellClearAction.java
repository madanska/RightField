package uk.ac.manchester.cs.owl.semspreadsheets.ui.action;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import uk.ac.manchester.cs.owl.semspreadsheets.change.SetCellValue;
import uk.ac.manchester.cs.owl.semspreadsheets.model.Cell;
import uk.ac.manchester.cs.owl.semspreadsheets.model.Range;
import uk.ac.manchester.cs.owl.semspreadsheets.model.WorkbookManager;

/**
 * Clears the text value and any appied validations to a range of cells.
 * 
 * @author Stuart Owen
 * 
 */
@SuppressWarnings("serial")
public class SheetCellClearAction extends SelectedCellsAction {

	public SheetCellClearAction(WorkbookManager workbookManager) {
		super("Clear", workbookManager);					
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		int res = JOptionPane
				.showConfirmDialog(
						null,
						"Are you sure you want to clear the contents of these cells?\nThey will not be recoverable.",
						"Clear cells?", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE);
		if (res == JOptionPane.YES_OPTION) {
			Range selectedRange = getSelectedRange();
			if (selectedRange.isCellSelection()) {
				for (int col = selectedRange.getFromColumn(); col < selectedRange
						.getToColumn() + 1; col++) {
					for (int row = selectedRange.getFromRow(); row < selectedRange
							.getToRow() + 1; row++) {
						Cell cell = selectedRange.getSheet()
								.getCellAt(col, row);
						if (cell != null) {
							String oldValue = cell.getValue();
							SetCellValue change = new SetCellValue(
									selectedRange.getSheet(), col, row,
									oldValue, null);
							getWorkbookManager().applyChange(change);

							// FIXME: for some reason, remove validations on an
							// entire range in one go isn't working
							// so for now remove each one individually
							Range rangeForOneCell = new Range(
									selectedRange.getSheet(), col, row, col,
									row);
							getWorkbookManager().removeValidations(
									rangeForOneCell);
						}
					}
				}
			}
		}

	}

}
