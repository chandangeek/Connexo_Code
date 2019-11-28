package com.energyict.mdc.engine.offline.gui.editors;

import com.energyict.mdc.engine.offline.core.FormatProvider;
import com.energyict.mdc.engine.offline.core.Translator;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.core.Utils;
import com.energyict.mdc.engine.offline.gui.models.ValueAdapter;
import com.energyict.mdc.engine.offline.gui.panels.CalendarComboBox;
import com.energyict.mdc.engine.offline.model.Equality;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

/**
 * AspectEditor (widget) for java.util.Date Objects
 */
public class DateAspectEditor extends AspectEditor<DateField> implements ChangeListener, PropertyChangeListener {

    private JLabel jLabel;
    private DateField dateField;

    private boolean bSkipChanges = false;
    private boolean bDateOnly = false;
    private boolean bShowSeconds = false;

    private Date lowerLimit;
    private Date upperLimit;
    private Date lastSeenInvalidDate; // to avoid double warnings for the same invalid date

    public DateAspectEditor() {
        jLabel = new JLabel();
        initDateField();
        setLowerLimit(defaultLowerLimit());
        setUpperLimit(defaultUpperLimit());
    }

    private void initDateField() {
        DateField.Precision precision = bDateOnly ? DateField.Precision.DAY : (bShowSeconds ? DateField.Precision.SECOND : DateField.Precision.MINUTE);
        dateField = new DateField(precision);
        dateField.addPropertyChangeListener(this);
        dateField.addChangeListener(this);
    }

    private DateField getDateField() {
        return dateField;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (bSkipChanges || !getDateField().isEnabled()) {
            return;
        }
        if (CalendarComboBox.DATE_PROPERTY.equals(evt.getPropertyName())) {
            doUpdate();
        }
        if (CalendarComboBox.DOCUMENT_PROPERTY.equals(evt.getPropertyName())) {
            // Notifying property change listeners the text in the input is a valid date
            setModelValue(getViewValue());
        }
    }

    /*
     * Creates a DateAspectEditor for use with dynamic attributes
     */
    static public DateAspectEditor create(ValueAdapter adapter, PropertyDescriptor descriptor) {
        DateAspectEditor editor = new DateAspectEditor();
        editor.setTranslate(false);
        editor.setCustomTranslate(true);
        editor.setForceReadOnly(false);
        editor.init(adapter, descriptor);
        return editor;
    }

    public JLabel getLabelComponent() {
        return jLabel;
    }

    /**
     * If set to true the time widgets will not be visible and the user cannot edit the time (portion)
     * of the date.
     *
     * @param bDateOnly true if only a Date (without time) needs to be edited by the user, false
     *                  if also time can be edited
     */
    public void setDateOnly(boolean bDateOnly) {
        this.bDateOnly = bDateOnly;
        dateField.setPrecision(determinePrecision());
    }

    private DateField.Precision determinePrecision() {
        return bDateOnly ? DateField.Precision.DAY : (bShowSeconds ? DateField.Precision.SECOND : DateField.Precision.MINUTE);
    }

    /**
     * If set to true a &quot;None&quot; button is displayed at the bottom of the calendar.
     * A null date is a valid value. If set to false a valid date is expected;
     *
     * @param show true = &quot;None&quot;-button visible, else not visible
     */
    public void setShowNoneButton(boolean show) {
        getDateField().setShowNoneButton(show);
    }

    /**
     * If set to true an extra spinner for the seconds in the time is shown. If set to false
     * no seconds widget is available for setting the seconds.
     *
     * @param showSeconds true = seconds can be set as part of the date
     */
    public void setShowSeconds(boolean showSeconds) {
        this.bShowSeconds = showSeconds;
        dateField.setPrecision(determinePrecision());
    }

    /**
     * Set the TimeZone of the calendar to use
     *
     * @param timeZone: timeZone to be used
     *                  if also time can be edited
     */
    public void setTimeZone(TimeZone timeZone) {
        getDateField().setTimeZone(timeZone);
    }

    /**
     * Set the minimum date this widget accepts
     * By default this is set to 1/1/1970.
     *
     * @param lowerLimit minimum date the widget accepts.
     */
    public void setLowerLimit(Date lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    /**
     * Set the maximum date this widget accepts
     * By default this is set to 31/12/9999.
     *
     * @param upperLimit maximum date the widget accepts.
     */
    public void setUpperLimit(Date upperLimit) {
        this.upperLimit = upperLimit;
    }

    /**
     * Return the editing component
     *
     * @param dateOnly if the widget should only accept a date (without time settings)
     * @return the editing component.
     */
    public JComponent getValueComponent(boolean dateOnly) {
        setDateOnly(dateOnly);
        return getValueComponent();
    }

    public boolean commitEdit() {
        try {
            getDateField().commitEdit();
            return doUpdate();
        } catch (ParseException pe) {
            //
        }
        return false;
    }

    @Override
    public DateField getValueComponent() {
        return getDateField();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        getDateField().setEnabled(!readOnly);
    }

    // ChangeListener interface
    public void stateChanged(ChangeEvent e) {
        if (!bSkipChanges) {
            doUpdate();
        }
    }

    protected void updateLabel() {
        jLabel.setText(getLabelString());
    }

    protected boolean doHasValidValue() {
        return (getDateField().hasValidValue() && isWithinBounds(getDateField().getDate()));
    }

    protected Object getViewValue() {
        if (getDateField().getDate() == null) {
            return null;
        }

        return getDateField().getDate();
    }

    protected void setViewValue(Object value) {
        bSkipChanges = true;
        if (value == null) {
            getDateField().setDate(null);
        } else {
            getDateField().setDate((Date) value);
        }
        bSkipChanges = false;
    }

    protected Object getModelValue() {
        // make sure we have a java.util.Date and not a subclass
        Date result = null;
        if (super.getModelValue() instanceof Date)
            result = (Date) super.getModelValue();
        return result == null ? null : new Date(result.getTime());
    }

    private boolean doUpdate() {
        if (getDateField().getDate() != null) {
            if (!isWithinBounds(getDateField().getDate())) {
                if (lastSeenInvalidDate==null || !Equality.equalityHoldsFor(lastSeenInvalidDate).and(getDateField().getDate())) {
                    lastSeenInvalidDate = getDateField().getDate();
                    JOptionPane.showMessageDialog(getDateField(), outOfBoundsMessage(getDateField().getDate()), getTranslator().getTranslation("error"), JOptionPane.ERROR_MESSAGE);
                }
                return false;
            }
        }
        updateModel();
        return true;
    }

    private Date defaultLowerLimit() {
        return new DateMidnight(1970, 1, 1, DateTimeZone.forTimeZone(getDateField().getTimeZone())).toDate();
    }

    private Date defaultUpperLimit() {
        return new DateTime(9999, 12, 31, 23, 59, 59, DateTimeZone.forTimeZone(getDateField().getTimeZone())).toDate();
    }

    private boolean isWithinBounds(Date date) {
        return (date == null || !(date.before(lowerLimit) || date.after(upperLimit)));
    }

    private String outOfBoundsMessage(Date date) {
        DateFormat fmt = FormatProvider.instance.get().getFormatPreferences().getDateFormat();
        String pattern = null;
        String limit = null;
        if (date.before(lowerLimit)) {
            limit = fmt.format(lowerLimit);
            pattern = getTranslator().getTranslation("datesShouldBeAfterX");
        } else if (date.after(upperLimit)) {
            limit = fmt.format(upperLimit);
            pattern = getTranslator().getTranslation("datesShouldBeBeforeX");
        }
        return Utils.format(pattern, new String[]{limit});
    }

    private Translator getTranslator() {
        return TranslatorProvider.instance.get().getTranslator();
    }
}