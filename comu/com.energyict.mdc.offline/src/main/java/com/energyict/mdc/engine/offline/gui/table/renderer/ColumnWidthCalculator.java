package com.energyict.mdc.engine.offline.gui.table.renderer;

/**
 * TabelCellRenderers that implement this interface
 * should return an acceptable width for the TableColumn they are used in.
 * <p/>
 * The TableBuilder.customize(JTable table) methods checks a number of  rows
 * in the table to set the columns best suitable width. When the renderer implements
 * this interface, it is responsible to return an acceptable width, without having
 * to check the table's content.
 * <p/>
 * For instance the <Code>DateRenderer</Code> can calculate a width by means of the <Code>DateFormat</Code> that
 * has be set to render a <Code>Date</Code>
 *
 * @author pdo
 * @see
 * @see TableBuilder.customize(JTable)
 * @see TableUtils.initColumnSizes(JTable table,int maxRows)
 */
public interface ColumnWidthCalculator {

    /**
     * Returns the calculated width for the <Code>TableColumn</Code> for which
     * the renderer is defined;
     */
    int calcPreferredWidth();

}
