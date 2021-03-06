package uk.ac.manchester.cs.owl.semspreadsheets.model;

import java.awt.Color;
import java.awt.Font;

/**
 * Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Information Management Group<br>
 * Date: 18-Sep-2009
 */
public interface CellContents {

    String getValue();

    void setValue(String s);

//    void setValue(String s);
    
    Font getFont();

    Color getForeground();

    Color getBackground();



//    CellContents deriveCellContents(String value);
//
//    CellContents deriveCellContents(Font font);

    int getAlignment();
}
