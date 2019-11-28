/* ***************************************************************************
 *
 *       File:  CalendarComboBox.java
 *  
 *   Contains:      ButtonActionListener
 *                  CalendarModel
 *                  CalendarSelectionListener
 *                  InputListener
 *
 * References:  'Java Rules' by Douglas Dunn
 *              Addison-Wesley, 2002 (Chapter 5, section 13 - 19)
 *
 *              'Professional Java Custom UI Components'
 *              by Kenneth F. Krutsch, David S. Cargo, Virginia Howlett
 *              WROX Press, 2001 (Chapter 1-3)
 *
 * Date         Author          Changes
 * ------------ -------------   ----------------------------------------------
 * Oct 24, 2002 Jane Griscti    Created
 * Oct 27, 2002 jg              Cleaned up calendar display
 * Oct 30, 2002 jg              added ctor CalendarComboBox( Calendar )
 * Oct 31, 2002 jg              Added listeners and Popup
 * Nov  1, 2002 jg              Cleaned up InputListener code to only accept
 *                              valid dates
 * Nov  2, 2002 jg              modified getPopup() to handle display when
 *                              component is positioned at the bottom of the screen
 * Nov  3, 2002 jg              changed some instance variables to class variables
 * Mar 29, 2003 jg              added setDate() contributed by James Waldrop
 * *************************************************************************** */
package com.energyict.mdc.engine.offline.gui.panels;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.core.Utils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.text.DateFormatter;
import java.awt.*;
import java.awt.event.*;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * A custom component that mimics a combo box, displaying
 * a perpetual calendar rather than a 'list'.
 */
public class CalendarComboBox extends JPanel implements DocumentListener {

    public static final String DATE_PROPERTY = "date";
    public static final String DOCUMENT_PROPERTY = "document";

    // -- class fields
    private final DateFormatSymbols dfs = new DateFormatSymbols();
    private static final String[] dayNames = new String[7];
    private static final int[] days = new int[7];
    private static final Toolkit toolkit = Toolkit.getDefaultToolkit();
    private static final Dimension screenSize = toolkit.getScreenSize();

    private Calendar current;

    // -- instance fields used with 'combo-box' panel
    private final JPanel inputPanel = new JPanel();
    private final JFormattedTextField input = new JFormattedTextField();
    private final BasicArrowButton comboBtn = new BasicArrowButton(SwingConstants.SOUTH);

    // -- instance fields used with calendar panel
    private final JPanel calPanel = new JPanel();
    private final JTable table = new JTable(6, 6); // display );

    private final JLabel monthLabel = new JLabel(" SEPTEMBER ", JLabel.CENTER);
    private final JLabel yearLabel = new JLabel("8888", JLabel.CENTER);
    private final BasicArrowButton prevMonthBtn = new CalendarNavigationButton(BasicArrowButton.WEST);
    private final BasicArrowButton nextMonthBtn = new CalendarNavigationButton(BasicArrowButton.EAST);
    private final BasicArrowButton prevYearBtn = new CalendarNavigationButton(BasicArrowButton.WEST);
    private final BasicArrowButton nextYearBtn = new CalendarNavigationButton(BasicArrowButton.EAST);
    private final JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 6, 6));
    private final JButton todayButton = new CalendarButton();
    private final JButton noneButton = new CalendarButton();

    private final SimpleDateFormat monthFormatter = new SimpleDateFormat("MMMM");
    private final SimpleDateFormat yearFormatter = new SimpleDateFormat("yyyy");

    private Popup popup;
    private boolean popupVisible = false;
    private boolean showNoneButton = true;

    private ComponentListener componentListener = null;
    private Container rootComponent = null;
    private boolean skipChanges = false;

    /**
     * Create a new calendar combo-box object set with today's date.
     */
    @SuppressWarnings("unused")
    public CalendarComboBox() {
        this(new GregorianCalendar());
    }

    /**
     * Create a new calendar combo-box object set with the given date.
     *
     * @param cal a calendar object
     * @see GregorianCalendar
     */
    public CalendarComboBox(final Calendar cal) {
        super(new BorderLayout());
        current = cal;

        this.init();

        buildInputPanel();
        buildCalendarDisplay();
        registerListeners();

        // intially, only display the input panel
        add(inputPanel, BorderLayout.CENTER);
    }

    public void setDateFormat(SimpleDateFormat format) {
        format.setLenient(false);
        ((DateFormatter) input.getFormatter()).setFormat(format);
        input.setColumns(format.toPattern().length());
    }

    public void setEnabled(boolean flag) {
        super.setEnabled(flag);
        input.setEnabled(flag);
        comboBtn.setEnabled(flag);
    }

    public void setEditable(boolean flag) {
        input.setEditable(flag);
    }

    public void setShowNoneButton(boolean showIt) {
        showNoneButton = showIt;
    }

    public boolean hasValidValue() {
        if (input.getValue() == null) {
            return showNoneButton;
        }
        return input.getInputVerifier().verify(input);
    }

    public void commitEdit() throws ParseException {
        Date oldTime = current.getTime();
        // When both dates are 01/01/1970 (e.g. if the formattedTextfield hadn't yet a value)
        // we force the firepropertychange by setting the old date on -1;
        if (current.getTimeInMillis()+ current.get(Calendar.ZONE_OFFSET) == 0){
            current.roll(Calendar.MILLISECOND, - 1);
            oldTime = current.getTime();
        }
        if (popupVisible) {
            hidePopup();
        }
        if (Utils.isNull(input.getText())) {
            current.clear();
            input.setValue(null);
        } else {
            input.commitEdit();
            current.setTime((Date) input.getValue());
        }
        this.firePropertyChange(DATE_PROPERTY, oldTime, current.getTime());
    }

    public void setText(String dateString){
        input.setText(dateString);
    }

    /*
    *  Creates a field and 'combo box' button above the calendar
    *  to allow user input.
    */

    private void buildInputPanel() {
        input.setInputVerifier(new FormattedTextFieldVerifier());
        input.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 2));
        input.setFont(getFont());

        inputPanel.setLayout(new BorderLayout());
        inputPanel.add(input, BorderLayout.CENTER);

        comboBtn.setActionCommand("combo");
        inputPanel.add(comboBtn, BorderLayout.EAST);
        inputPanel.setBorder((new JComboBox()).getBorder());
    }

    private void buildCalendarDisplay() {
        calPanel.setBorder(new LineBorder(Color.BLACK));
        calPanel.setLayout(new BorderLayout());
        calPanel.add(buildNavigationPanel(), BorderLayout.NORTH);
        calPanel.add(buildCalendarPanel());
        calPanel.add(buildButtonPanel(), BorderLayout.SOUTH);
    }

    /*
     * Creates a small panel above the month table to display the month and
     * year along with the 'prevBtn', 'nextBtn' month selection buttons
     * and a 'closeCalendarBtn'.
     */
    private JPanel buildNavigationPanel() {
        JPanel navigationPanel = new JPanel(new BorderLayout());
        navigationPanel.setBorder(BorderFactory.createEtchedBorder());

        monthLabel.setBorder(null);
        monthLabel.setFont(getFont());
        yearLabel.setBorder(null);
        yearLabel.setFont(getFont());

        prevMonthBtn.setActionCommand("prevMonth");
        nextMonthBtn.setActionCommand("nextMonth");
        prevYearBtn.setActionCommand("prevYear");
        nextYearBtn.setActionCommand("nextYear");

        JPanel monthPanel = new JPanel(new BorderLayout());
        monthPanel.add(prevMonthBtn, BorderLayout.WEST);
        monthPanel.add(monthLabel, BorderLayout.CENTER);
        monthPanel.add(nextMonthBtn, BorderLayout.EAST);

        JPanel yearPanel = new JPanel(new BorderLayout());
        yearPanel.add(prevYearBtn, BorderLayout.WEST);
        yearPanel.add(yearLabel, BorderLayout.CENTER);
        yearPanel.add(nextYearBtn, BorderLayout.EAST);

        navigationPanel.add(monthPanel, BorderLayout.CENTER);
        navigationPanel.add(yearPanel, BorderLayout.EAST);

        return navigationPanel;
    }

    private JPanel buildCalendarPanel() {
        //  Allow for individual cell selection and turn off
        //  grid lines.
        table.setCellSelectionEnabled(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        table.setDefaultRenderer(Date.class, new CalendarComboBox.CalendarTableCellRenderer());
        table.setFont(getFont());
        table.setFocusable(false); // Geert - To not lose the inputfield focus

        //  Set the column widths. Need to turn
        //  auto resizing off to make this work.
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.black));
        // Geert [2005-aug-25] to cope with different font sizes:
        int fontSize = table.getFont().getSize();
        if (fontSize > 13) {
            table.setRowHeight(fontSize + 4);
        } else {
            table.setRowHeight(16);
        }

        //  Column headers are only displayed automatically
        //  if the table is put in a JScrollPane. Don't want
        //  to use one here, so need to add the headers
        //  manually.
        JTableHeader header = table.getTableHeader();
        header.setResizingAllowed(false);
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.black));
        header.setDefaultRenderer(new CalendarHeaderRenderer());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        panel.setBackground(Color.WHITE);
        panel.add(header);
        panel.add(table);
        return panel;
    }

    private JPanel buildButtonPanel() {
        buttonPanel.setOpaque(false);
        String todayLabel = TranslatorProvider.instance.get().getTranslator().getTranslation("today", "Today");

        JPanel innerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
        innerPanel.add(buttonPanel);
        innerPanel.setBackground(Color.white);

        todayButton.setText(todayLabel);
        todayButton.setActionCommand("today");
        buttonPanel.add(todayButton);

        if (showNoneButton) {
	        String none = TranslatorProvider.instance.get().getTranslator().getTranslation("none", "None");

            noneButton.setText(none);
            noneButton.setActionCommand("noDate");
            buttonPanel.add(noneButton);
        }
        return innerPanel;
    }

    /*
    *  Register all required listeners with appropriate
    *  components
    */
    private void registerListeners() {
        CalendarSelectionListener selectionListener = new CalendarSelectionListener();
        table.getSelectionModel().addListSelectionListener(selectionListener);
        table.getColumnModel().getSelectionModel().addListSelectionListener(selectionListener);
        table.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) { // GDE - changed mouseClicked into mouseReleased
                // since the 1st is not triggered if the coordinates
                // of 'the press' differ too much from those of 'the release'
                if (e.getClickCount() == 1) { // GDE - made that 1
                    hidePopup();
                }
            }
        });

        // 'Combo-box' listeners
        input.getDocument().addDocumentListener(this);
        input.addKeyListener(new InputListener());     // Hiding the popup if visible
        input.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    commitEdit();
                } catch (ParseException pe) {
                    // solved by CalendarComboBox
                }
            }
        });

        input.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (popupVisible) {
                    hidePopup();
                }
            }

        });

        ButtonActionListener btnListener = new ButtonActionListener();
        comboBtn.addActionListener(btnListener);

        // Calendar navigation
        prevMonthBtn.addActionListener(btnListener);
        nextMonthBtn.addActionListener(btnListener);
        prevYearBtn.addActionListener(btnListener);
        nextYearBtn.addActionListener(btnListener);

        todayButton.addActionListener(btnListener);
        noneButton.addActionListener(btnListener);

        componentListener = new ComponentAdapter() {
            public void componentMoved(ComponentEvent e) {
                if (popupVisible) {
                    hidePopup();
                }
            }
        };

        input.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                try {
                    commitEdit();
                } catch (ParseException pe) {
                    // input's document prevents wrong input
                }
            }
        });
    }

    private void initCalendarTable() {
        CalendarModel model = new CalendarModel(current);
        model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == CalendarModel.MONTH_YEAR_CHANGED) {
                    CalendarModel model = (CalendarModel) e.getSource();
                    // After the model was changed, we need to reset the column widths...
                    setColumnsWidth();

                    monthLabel.setText(monthFormatter.format(model.getStartDate()));
                    yearLabel.setText(yearFormatter.format(model.getStartDate()));
                }
            }
        });
        model.fireYearOrMonthChanged(); // So to set the initial month and year in the calendar combo
        table.setModel(model);

        setColumnsWidth();
        if (input.getValue() != null) {
            skipChanges = true; // GDE - table updating <> input update!
            Calendar cal2 = model.getDisplayCalendar();
            cal2.setTime((Date) input.getValue());
            if (model.getMonth() == cal2.get(Calendar.MONTH) &&
                    model.getYear() == cal2.get(Calendar.YEAR)) {
                table.changeSelection(model.getRowIndex(current.getTime()),
                        getColumnIndex(current.get(Calendar.DAY_OF_WEEK)), false, false);
            } else {
                table.clearSelection();
            }
            skipChanges = false;
        }
    }

    private void setColumnsWidth() {
        int count = table.getColumnCount();
        for (int i = 0; i < count; i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            col.setPreferredWidth(table.getRowHeight() + 2);
        }
    }

    /*
    *  Gets a Popup to hold the calendar display and determines
    *  it's position on the screen.
    */

    private Popup getPopup() {
        if (!showNoneButton) {
            buttonPanel.remove(noneButton);
        }

        initCalendarTable();

        Point p = comboBtn.getLocationOnScreen();
        Dimension inputSize = input.getSize();
        Dimension calendarSize = calPanel.getPreferredSize();

        int x = p.x;
        x += comboBtn.getWidth() - calendarSize.width;
        if (x < 0) {
            x = 0;
        }
        int y = p.y;
        if ((p.y + comboBtn.getSize().height + calendarSize.height) < screenSize.height) {
            y += inputSize.height + 4;
        } else {
            y -= calendarSize.height + 4;
        } // need to fit it above input panel

        return PopupFactory.getSharedInstance().getPopup(this, calPanel, x, y);
    }

    private void showPopup() {
        rootComponent = SwingUtilities.getAncestorOfClass(JRootPane.class, this);
        rootComponent.getParent().addComponentListener(componentListener);

        popup = getPopup();
        popup.show();
        popupVisible = true;

    }

    private void hidePopup() {
        if (popup != null) {
            popup.hide();
            popupVisible = false;
        }
        rootComponent.getParent().removeComponentListener(componentListener);
    }

    public Date getDate() {
        return current.isSet(Calendar.YEAR) ? current.getTime() : null;
    }

    /**
     * Sets the current date and updates the UI to reflect the current calendar's date.
     *
     * @param newDate the date to set the calendar to
     */
    public void setDate(Date newDate) {
        if (newDate == null) {
            current.clear();
        } else {
            current.setTime(newDate);
        }

        this.init();
    }

    /**
     * Sets the current date and updates the UI to reflect the current calendar's date.
     */
    public void init() {
        input.getDocument().removeDocumentListener(this);
        if (current.isSet(Calendar.YEAR)) {
            input.setValue(current.getTime());
        } else {
            input.setValue(null);
        }
        input.getDocument().addDocumentListener(this);
    }

    public void setFont(Font font) {
        super.setFont(font);
        setTextFont(font);
        setCalendarFont(font);
    }

    public void setTextFont(Font font) {
        if (input != null) {
            input.setFont(font);
        }
    }

    public void setCalendarFont(Font font) {
        if (table != null) {
            table.setFont(font);
            if (monthLabel != null) {
                monthLabel.setFont(font);
            }
            if (yearLabel != null) {
                yearLabel.setFont(font);
            }
        }
    }

    public void setOpaque(boolean flag) {
        if (inputPanel != null) {
            inputPanel.setOpaque(flag);
        }
        super.setOpaque(flag);
    }

    // DocumentListener interface
    // For use within dateAspectEditor where changing the date in the JFormattedTextField should 'dirty' the shadow
    // Just like other aspectEditors do.
    public void insertUpdate(DocumentEvent e) {
        try {
            this.firePropertyChange(DOCUMENT_PROPERTY, input.getValue(), input.getFormatter().stringToValue(input.getText()));
        } catch (ParseException ex) {
            // invalid date
        }
    }

    public void removeUpdate(DocumentEvent e) {
        try {
            // Notifying property change listeners the text in the input is a valid date
            this.firePropertyChange(DOCUMENT_PROPERTY, input.getValue(), input.getFormatter().stringToValue(input.getText()));
        } catch (ParseException ex) {
            // invalid date
        }
    }

    public void changedUpdate(DocumentEvent e) {
        try {
            this.firePropertyChange(DOCUMENT_PROPERTY, input.getValue(), input.getFormatter().stringToValue(input.getText()));
        } catch (ParseException ex) {
            // invalid date
        }
    }

    private int getColumnIndex(int dayOfWeek) {
        int index = dayOfWeek;
        for (int i = 0; i < 7; i++) {
            if (days[i] == index) {
                index = i;
                break;
            }
        }
        return index;
    }

    /*
    *  Creates a custom model to back the table.
    */
    private class CalendarModel extends DefaultTableModel {

        final static int MONTH_YEAR_CHANGED = TableModelEvent.HEADER_ROW - 1;

        private Calendar displayCalendar;
        private Date[] rowStart = new Date[6];
        private int month;
        private int year;

        public CalendarModel(Calendar cal) {
            super(6, 6);
            setColumnIdentifiers(getDayNames());

            displayCalendar = (Calendar) cal.clone();
            if (!cal.isSet(Calendar.YEAR)) {
                // Start with today if date is not set
                displayCalendar.setTime(new Date());
            }
            this.month = displayCalendar.get(Calendar.MONTH);
            this.year = displayCalendar.get(Calendar.YEAR);
            displayCalendar.set(Calendar.HOUR_OF_DAY, 0);
            displayCalendar.set(Calendar.MINUTE, 0);
            displayCalendar.set(Calendar.SECOND, 0);
            displayCalendar.set(Calendar.MILLISECOND, 1);
            initModel();
        }

        public int getMonth() {
            return this.month;
        }

        public int getYear() {
            return this.year;
        }

        public void roll(int field, int amount) {
            displayCalendar.setTime(getStartDate());
            displayCalendar.roll(field, amount);
            if (field == Calendar.MONTH && amount == -1) {
                if (displayCalendar.get(Calendar.MONTH) == Calendar.DECEMBER) {
                    displayCalendar.roll(Calendar.YEAR, -1);
                }
            }
            if (field == Calendar.MONTH && amount == 1) {
                if (displayCalendar.get(Calendar.MONTH) == Calendar.JANUARY) {
                    displayCalendar.roll(Calendar.YEAR, 1);
                }
            }
            this.month = displayCalendar.get(Calendar.MONTH);
            this.year = displayCalendar.get(Calendar.YEAR);
            initModel();
            fireYearOrMonthChanged();
        }

        public Date getStartDate() {
            Calendar cal = Calendar.getInstance(current.getTimeZone());
            cal.clear();
            cal.set(Calendar.YEAR, this.year);
            cal.set(Calendar.MONTH, this.month);
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
            return cal.getTime();
        }

        public Class<?> getColumnClass(int column) {
            return Date.class;
        }

        public boolean isCellEditable(int row, int col) {
            return false;
        }

        public Calendar getDisplayCalendar() {
            return displayCalendar;
        }

        private void initModel() {
            displayCalendar.set(Calendar.DAY_OF_MONTH, 1);
            // compute the start column for the first day in the first week
            int startIndex = getColumnIndex(displayCalendar.get(Calendar.DAY_OF_WEEK));

            displayCalendar.add(Calendar.DAY_OF_MONTH, -startIndex);
            // fill the calendar for the new month
            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 7; col++) {
                    if (col == 0) {
                        rowStart[row] = displayCalendar.getTime();
                    }
                    this.setValueAt(displayCalendar.getTime(), row, col);
                    displayCalendar.add(Calendar.DAY_OF_MONTH, 1);
                }
            }
        }

        private int getRowIndex(Date date) {
            displayCalendar.setTime(date);
            displayCalendar.set(Calendar.HOUR_OF_DAY, 0);
            displayCalendar.set(Calendar.MINUTE, 0);
            displayCalendar.set(Calendar.SECOND, 0);
            displayCalendar.set(Calendar.MILLISECOND, 1);
            date = displayCalendar.getTime();

            int rowIndex = -1;
            for (int i = 5; i >= 0 && rowIndex < 0; i--) {
                if (!date.before(rowStart[i])) {
                    rowIndex = i;
                }
            }
            return rowIndex;
        }

        private String[] getDayNames() {
            String[] names = dfs.getShortWeekdays();  //"" sun mon tue wed thu fri sat
            // EISERVER-5030 - Datepicker doesn't show correct date header in Chinese:
            int index2Use = Locale.getDefault().getLanguage().equals(new Locale("zh").getLanguage()) ? 2 : 0;
            int ind = 0;
            for (int i = current.getFirstDayOfWeek(); i < current.getFirstDayOfWeek() + 7; i++) {
                if (i < 8) {
                    dayNames[ind] = "" + names[i].toUpperCase().charAt(index2Use);
                    days[ind] = i;
                } else {
                    dayNames[ind] = "" + names[i - 7].toUpperCase().charAt(index2Use);
                    days[ind] = i - 7;
                }
                ind++;
            }
            return dayNames;
        }

        private void fireYearOrMonthChanged() {
            fireTableChanged(new TableModelEvent(this, -1, -1, TableModelEvent.ALL_COLUMNS, MONTH_YEAR_CHANGED));
        }

    }

    /*
    *  Captures the 'prevMonth', 'prevMonth', 'comboBtn' ... actions.
    *
    */
    private class ButtonActionListener implements ActionListener {

        public void actionPerformed(ActionEvent evt) {
            String cmd = evt.getActionCommand();
            if (cmd.equals("prevMonth")) {
                ((CalendarModel) table.getModel()).roll(Calendar.MONTH, -1);
            } else if (cmd.equals("nextMonth")) {
                ((CalendarModel) table.getModel()).roll(Calendar.MONTH, 1);
            } else if (cmd.equals("prevYear")) {
                ((CalendarModel) table.getModel()).roll(Calendar.YEAR, -1);
            } else if (cmd.equals("nextYear")) {
                ((CalendarModel) table.getModel()).roll(Calendar.YEAR, 1);
            } else if (cmd.equals("today")) {
                hidePopup(); // GDE - close after choosing today (like none)
                input.setValue(new Date());
                try {
                    commitEdit();
                } catch (ParseException pe) {
                    // document doesn't allow
                }
            } else if (cmd.equals("noDate")) {
                hidePopup();
                input.setText(null);
                try {
                    commitEdit();
                } catch (ParseException pe) {
                    // document doesn't allow
                }
            } else if (cmd.equals("combo")) {
                if (popupVisible) {
                    hidePopup();
                } else {
                    showPopup();
                }
                input.requestFocus();
            }
        }
    }

    /*
    *  Captures a user selection in the calendar display and
    *  changes the value in the 'combo box' to match the selected date.
    *
    */

    private class CalendarSelectionListener implements ListSelectionListener {

        private int prevRow = -1;
        private int prevCol = -1;

        public void valueChanged(ListSelectionEvent e) {
            if (skipChanges) {
                return;
            }
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                int col = table.getSelectedColumn();
                if (row == prevRow && col == prevCol) {
                    // This method is triggered for column and row selection changes
                    // If the user selects a date on the same row as the current,
                    // the column selection change is responsible for the trigger
                    // If the user selects a date in the same column as the current,
                    // the row selection change is responsible for the trigger
                    // If the user selects a date in a different column and row as the current,
                    // both the column and row selection change trigger this method
                    // In the latter case, for the 2nd trigger we come here in this condition:
                    return; // already processed
                } else {
                    prevCol = col;
                    prevRow = row;
                }
                if (row >= 0 && col >= 0) {
                    CalendarModel model = (CalendarModel) table.getModel();
                    Object value = model.getValueAt(row, col);
                    if (value != null) {
                        Date oldTime = current.getTime();
                        Date newDate = (Date) value;
                        input.setValue(newDate);
                        current.setTime((Date) input.getValue());
                        CalendarComboBox.this.firePropertyChange(DATE_PROPERTY, oldTime, current.getTime());
                    }
                }
            }
        }
    }

    /*
    *  Captures user input in the 'combo box'
    *  If the input is a valid date and the user pressed
    *  ENTER or TAB, the calendar selection is updated
    */

    private class InputListener extends KeyAdapter {

        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                if (popupVisible) {
                    e.consume();
                }
            }
            if (popupVisible) {
                hidePopup();
            }
        }
    }

    private class CalendarHeaderRenderer extends javax.swing.table.DefaultTableCellRenderer {

        public CalendarHeaderRenderer() {
            setBackground(Color.WHITE);
            setHorizontalAlignment(JLabel.CENTER);
        }

        public java.awt.Component getTableCellRendererComponent(JTable tablePara,
                                                                Object value,
                                                                boolean isSelected,
                                                                boolean hasFocus,
                                                                int row,
                                                                int column) {

            if (days[column] == Calendar.SATURDAY || days[column] == Calendar.SUNDAY) {
                setForeground(Color.RED);
            } else {
                setForeground(Color.BLACK);
            }

            return super.getTableCellRendererComponent(tablePara,
                    value,
                    isSelected,
                    hasFocus,
                    row,
                    column);

        }
    }

    private class CalendarTableCellRenderer extends javax.swing.table.DefaultTableCellRenderer {

        public CalendarTableCellRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }

        public java.awt.Component getTableCellRendererComponent(JTable tablePara,
                                                                Object value,
                                                                boolean isSelected,
                                                                boolean hasFocus,
                                                                int row,
                                                                int column) {

            CalendarModel model = (CalendarModel) tablePara.getModel();
            Calendar displayCalendar = model.getDisplayCalendar();
            displayCalendar.setTime((Date) value);
            int month = displayCalendar.get(Calendar.MONTH);
            value = displayCalendar.get(Calendar.DAY_OF_MONTH);

            Color foreground;
            if (model.getMonth() == month) {
                if (days[column] == Calendar.SATURDAY || days[column] == Calendar.SUNDAY) {
                    foreground = Color.RED;
                } else {
                    foreground = Color.BLACK;
                }
            } else {
                foreground = Color.lightGray;
            }

            java.awt.Component component = super.getTableCellRendererComponent(tablePara,
                    value,
                    isSelected,
                    hasFocus,
                    row,
                    column);

            component.setForeground(isSelected ? Color.WHITE : foreground);
            component.setBackground(isSelected ? Color.LIGHT_GRAY : Color.WHITE);
            if (isToday(displayCalendar)) {
                ((JComponent) component).setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.red));
            }

            return component;
        }

        private boolean isToday(Calendar displayCalendar) {
            Calendar test = (Calendar) displayCalendar.clone();
            test.setTime(new Date());
            return (displayCalendar.get(Calendar.YEAR) == test.get(Calendar.YEAR) &&
                    displayCalendar.get(Calendar.MONTH) == test.get(Calendar.MONTH) &&
                    displayCalendar.get(Calendar.DAY_OF_MONTH) == test.get(Calendar.DAY_OF_MONTH));
        }

    }


    public class FormattedTextFieldVerifier extends InputVerifier {

        public boolean verify(JComponent input) {
            if (input instanceof JFormattedTextField) {
                JFormattedTextField ftf = (JFormattedTextField) input;
                JFormattedTextField.AbstractFormatter formatter = ftf.getFormatter();
                if (formatter != null) {
                    String text = ftf.getText();
                    if (Utils.isNull(text)) {
                        return true;
                    }
                    try {
                        formatter.stringToValue(text);
                        return true;
                    } catch (ParseException pe) {
                        return false;
                    }
                }
            }
            return true;
        }

        public boolean shouldYieldFocus(JComponent input) {
            return verify(input);
        }
    }

    /**
     * BasicArrowButton with some default settings
     */
    private class CalendarNavigationButton extends BasicArrowButton {

        CalendarNavigationButton(int direction) {
            super(direction);
            setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
            setRolloverEnabled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
        }
    }

    /**
     * Button used within the calendar panel
     */
    private class CalendarButton extends JButton {

        CalendarButton() {
            setMargin(new Insets(2, 2, 2, 2));
            setFocusPainted(false);
            setFocusable(false);
        }
    }
}
