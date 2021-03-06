package uk.ac.manchester.cs.owl.semspreadsheets.model;

import java.util.Collection;

/**
 * Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Information Management Group<br>
 * Date: 18-Sep-2009
 */
public interface Sheet {

    String getName();

    void setName(String name);

    Workbook getWorkbook();

    boolean isHidden();

    void setVeryHidden(boolean b);

    void setHidden(boolean b);

    int getMaxRows();

    int getMaxColumns();

    int getColumnWidth(int col);

    void clearAllCells();

    boolean isCellAt(int col, int row);

    Cell getCellAt(int col, int row);

    Cell addCellAt(int col, int row);

    void clearCellAt(int col, int row);

    void addValidation(String namedRange, int firstCol, int firstRow, int lastCol, int lastRow);

    void removeValidation(Validation validation);

    Collection<Validation> getValidations();

    Collection<Validation> getIntersectingValidations(Range range);

    Collection<Validation> getContainingValidations(Range range);

    Validation getValidationAt(int col, int row);

    void clearValidation();




}
