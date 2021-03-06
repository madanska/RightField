package uk.ac.manchester.cs.owl.semspreadsheets.impl;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingConstants;
import javax.swing.text.Style;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFComment;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

import uk.ac.manchester.cs.owl.semspreadsheets.model.Cell;

/**
 * Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Information Management Group<br>
 * Date: 20-Sep-2009
 * 
 * Author: Stuart Owen
 * Date: 1 Oct 2010
 */
public class CellHSSFImpl implements Cell {
	
	private static Logger logger = Logger.getLogger(CellHSSFImpl.class);

    public static final Font DEFAULT_FONT = new Font("verdana", Font.PLAIN, 10);

    private static Map<HSSFFont, Font> fontCache = new HashMap<HSSFFont, Font>();

    private HSSFCell theCell;

    private HSSFWorkbook workbook;

    private Color foreground;

    public CellHSSFImpl(HSSFWorkbook workbook, HSSFCell theCell) {
        this.workbook = workbook;
        this.theCell = theCell;
    }

    public Font getDefaultFont() {
        HSSFFont font = workbook.getFontAt((short) 0);
        if (font == null) {
            return DEFAULT_FONT;
        }
        return getFont(font);
    }

    public Style getStyle() {
        return null;
    }

    public int getRow() {
        return theCell.getRowIndex();
    }

    public int getColumn() {
        return theCell.getColumnIndex();
    }

    public String getComment() {
        HSSFComment hssfComment = theCell.getCellComment();
        if (hssfComment == null) {
            return null;
        }
        else {
            return hssfComment.toString();
        }

    }

    public boolean isStrikeThrough() {
        HSSFFont hssfFont = theCell.getCellStyle().getFont(workbook);
        return hssfFont.getStrikeout();
    }

    public boolean isUnderline() {
        HSSFFont hssfFont = theCell.getCellStyle().getFont(workbook);
        return hssfFont.getUnderline() != 0;
    }

    public boolean isItalic() {
        HSSFFont hssfFont = theCell.getCellStyle().getFont(workbook);
        return hssfFont.getItalic();
    }

    public String getValue() {
        if (theCell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
            return "";
        }
        else if (theCell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN) {
            return Boolean.toString(theCell.getBooleanCellValue());
        }
        else if (theCell.getCellType() == HSSFCell.CELL_TYPE_ERROR) {
            return "<ERROR?>";
        }
        else if (theCell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
            return theCell.getCellFormula();
        }
        else if (theCell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
            return Double.toString(theCell.getNumericCellValue());
        }
        else if (theCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
            return theCell.getRichStringCellValue().getString();
        }
        return "";
    }

    public void setValue(String value) {
        if (theCell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
            theCell.setCellValue(new HSSFRichTextString(value));
        }
        else if (theCell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN) {
            theCell.setCellValue(Boolean.parseBoolean(value));
        }
        else if (theCell.getCellType() == HSSFCell.CELL_TYPE_ERROR) {
        }
        else if (theCell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
            theCell.setCellFormula(value);
        }
        else if (theCell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
            theCell.setCellValue(Double.parseDouble(value));
        }
        else if (theCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
            theCell.setCellValue(new HSSFRichTextString(value));
        }
    }

    public boolean isBold() {
        return getFont().isBold();
    }

    public void setBold(boolean b) {
        HSSFCellStyle cellStyle = theCell.getCellStyle();        
        if (cellStyle == null) {
            cellStyle = workbook.createCellStyle();
            theCell.setCellStyle(cellStyle);
        }
        HSSFFont font = cellStyle.getFont(workbook);
        if (font == null) {
            font = workbook.createFont();
            cellStyle.setFont(font);
        }
        if (b) {
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        }
        else {
            font.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
        }
        fontCache.clear();
    }

    public Font getFont() {
        HSSFCellStyle cellStyle = theCell.getCellStyle();
        if (cellStyle == null) {
            return getDefaultFont();
        }
        HSSFFont hssfFont = cellStyle.getFont(workbook);
        return getFont(hssfFont);
    }

    private Font getFont(HSSFFont hssfFont) {
        Font font = fontCache.get(hssfFont);
        if (font == null) {
            String name = hssfFont.getFontName();
            int size = hssfFont.getFontHeightInPoints();
            int style = Font.PLAIN;
            if (hssfFont.getBoldweight() == HSSFFont.BOLDWEIGHT_BOLD) {
                style = Font.BOLD;
                if (hssfFont.getItalic()) {
                    style = style | Font.ITALIC;
                }
            }
            else if (hssfFont.getItalic()) {
                style = Font.ITALIC;
            }
            font = new Font(name, style, size);
            fontCache.put(hssfFont, font);
        }
        return font;

    }
    
    @Override
	public Color getBackgroundFill() {		
    	HSSFCellStyle cellStyle = theCell.getCellStyle();
        if (cellStyle == null) {
            return Color.WHITE;
        }
		short colorIndex=cellStyle.getFillBackgroundColor();
		return translateColour(colorIndex);
	}

    @Override
	public void setBackgroundFill(Color colour) {
		HSSFColor col = translateColour(colour);
		if (col==null) {
			logger.warn("Unable to find similar colour in palette for "+colour.toString());
		}
		else {
			HSSFCellStyle cellStyle = workbook.createCellStyle();
			cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND );
			cellStyle.setFillForegroundColor(col.getIndex());			
			theCell.setCellStyle(cellStyle);
			logger.debug("Cell colour changed to "+col.toString());
		}
	}

    public Color getForeground() {
        if (foreground == null) {
            HSSFCellStyle cellStyle = theCell.getCellStyle();
            if (cellStyle == null) {
                return Color.BLACK;
            }
            HSSFFont hssfFont = cellStyle.getFont(workbook);
            short colorIndex = hssfFont.getColor();
            Color theColor = translateColour(colorIndex);
            foreground = theColor;
        }
        return foreground;
    }

    /**
     * Translates a java Color to the colour index in the workbook palette
     * @param colour
     * @return
     */
    private HSSFColor translateColour(Color colour) {
    	HSSFPalette palette = workbook.getCustomPalette();
    	
    	HSSFColor col = palette.findSimilarColor((byte)colour.getRed(), (byte)colour.getGreen(), (byte)colour.getBlue());
    	return col;
    }
    /**
     * Translates the colorIndex from the workbook palette to a <br> 
     * java Color.
     * @param colorIndex
     * @return java Color
     */
	private Color translateColour(short colorIndex) {
		HSSFPalette palette = workbook.getCustomPalette();
		HSSFColor color = palette.getColor(colorIndex);
		Color theColor = Color.BLACK;
		if (color != null) {
		    short[] triplet = color.getTriplet();
		    theColor = new Color(triplet[0], triplet[1], triplet[2]);
		}
		return theColor;
	}

    public int getAlignment() {
        HSSFCellStyle cellStyle = theCell.getCellStyle();
        if (cellStyle == null) {
            return SwingConstants.LEFT;
        }
        short hssfAlignment = cellStyle.getAlignment();
        if (hssfAlignment == HSSFCellStyle.ALIGN_LEFT) {
            return SwingConstants.LEFT;
        }
        else if (hssfAlignment == HSSFCellStyle.ALIGN_CENTER) {
            return SwingConstants.CENTER;
        }
        else if (hssfAlignment == HSSFCellStyle.ALIGN_RIGHT) {
            return SwingConstants.RIGHT;
        }
        else {
            return SwingConstants.LEFT;
        }
    }

    public String getValidationListName() {
        return null;
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean isDataValidation() {
        return false;
    }

	

	
}
