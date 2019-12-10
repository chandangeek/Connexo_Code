package com.energyict.mdc.engine.offline.gui.editors;

import com.energyict.mdc.engine.offline.core.FormatProvider;
import com.energyict.mdc.engine.offline.gui.models.SpinnerRolloverModel;
import com.energyict.mdc.engine.offline.gui.panels.CalendarComboBox;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 8/04/13
 * Time: 11:27
 */
public class DateField extends JPanel {

    private final Calendar calendar;
    private Precision precision;
    private JPanel timePnl;

    public enum Precision {
        DAY, MINUTE, SECOND;

        public boolean hasSeconds() {
            return SECOND.equals(this);
        }

        public boolean hasTimeComponent() {
            return !DAY.equals(this);
        }
    }

    private class SubComponentListener implements ChangeListener, PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (CalendarComboBox.DATE_PROPERTY.equals(evt.getPropertyName())) {
                if (datePicker.getDate() == null) {
                    clearTime();
                }
                notifyListeners();
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            notifyListeners();
        }
    }

    private JPanel valueComponent;
    private CalendarComboBox datePicker;
    private JSpinner hours;
    private JSpinner minutes;
    private JSpinner seconds;
    private TimeZone timeZone;
    private ChangeEvent event;
    private List<ChangeListener> changeListeners = new LinkedList<>();
    private final SubComponentListener subComponentListener = new SubComponentListener();

    public DateField(Precision precision) {
        super(new BorderLayout());
        this.precision = precision;
        timeZone = TimeZone.getDefault();
        calendar = Calendar.getInstance();
        datePicker = new CalendarComboBox(calendar);
        datePicker.setDate(null);
        datePicker.setDateFormat(defaultDateFormat());
        hours = new JSpinner(new SpinnerRolloverModel(0, 0, 23, 1));
        minutes = new JSpinner(new SpinnerRolloverModel(0, 0, 59, 1));
        seconds = new JSpinner(new SpinnerRolloverModel(0, 0, 59, 1));
        hours.addChangeListener(subComponentListener);
        minutes.addChangeListener(subComponentListener);
        seconds.addChangeListener(subComponentListener);
        add(datePicker, BorderLayout.CENTER);
        alignTimeComponentWith(precision);
        datePicker.addPropertyChangeListener(subComponentListener);
    }

    private void alignTimeComponentWith(Precision precision) {
        if (precision.hasTimeComponent()) {
            JComponent timeComponent = getTimeComponent(precision.hasSeconds());
            if (!this.equals(timeComponent.getParent())) {
                add(timeComponent, BorderLayout.EAST);
            }
        } else {
            remove(timePnl);
        }
    }

    public void addChangeListener(ChangeListener listener) {
        if(changeListeners != null) {
            changeListeners.add(listener);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
        if (datePicker != null) {
            datePicker.addPropertyChangeListener(listener);
        }
    }

    public void commitEdit() throws ParseException {
        datePicker.commitEdit();
    }

    void setText(String dateString) throws ParseException{
        datePicker.setText(dateString);
        commitEdit();
    }

    public Date getDate() {
        if (datePicker.getDate() == null) {
            return null;
        }
        MutableDateTime mutableDateTime = new MutableDateTime(datePicker.getDate(), DateTimeZone.forTimeZone(getTimeZone()));
        mutableDateTime.setHourOfDay((Integer) hours.getValue());
        mutableDateTime.setMinuteOfHour((Integer) minutes.getValue());
        mutableDateTime.setSecondOfMinute((Integer) seconds.getValue());
        mutableDateTime.setMillisOfSecond(0);
        return mutableDateTime.toDate();
    }

    public ChangeEvent getEvent() {
        if (event == null) {
            event = new ChangeEvent(this);
        }
        return event;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public boolean hasValidValue() {
        return datePicker.hasValidValue();
    }

    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        super.removePropertyChangeListener(listener);
        hours.removePropertyChangeListener(listener);
        minutes.removePropertyChangeListener(listener);
        seconds.removePropertyChangeListener(listener);
    }

    public void setDate(Date value) {
        if (value == null) {
            calendar.clear();
            datePicker.init();
            clearTime();
            return;
        }
        DateTime dateTime = new DateTime(value);
        datePicker.setDate(dateTime.withMillisOfDay(0).toDate());
        hours.setValue(dateTime.getHourOfDay());
        minutes.setValue(dateTime.getMinuteOfHour());
        seconds.setValue(dateTime.getSecondOfMinute());
    }

    private void clearTime() {
        hours.setValue(0);
        minutes.setValue(0);
        seconds.setValue(0);
    }

    @Override
    public void setEnabled(boolean enabled) {
        datePicker.setEnabled(enabled);
        hours.setEnabled(enabled);
        minutes.setEnabled(enabled);
        seconds.setEnabled(enabled);
    }

    public void setShowNoneButton(boolean show) {
        datePicker.setShowNoneButton(show);
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public void setPrecision(Precision precision) {
        if (this.precision == null ? precision == null : this.precision.equals(precision)) {
            return;
        }
        alignTimeComponentWith(precision);
        this.precision = precision;
    }

    private SimpleDateFormat defaultDateFormat() {
        SimpleDateFormat format = (SimpleDateFormat) FormatProvider.instance.get().getFormatPreferences().getDateFormat();
        if (format == null) {
            format = new SimpleDateFormat("dd/MM/yyyy");
        }
        return format;
    }

    private JComponent getTimeComponent(boolean bShowSeconds) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        if (timePnl == null) {
            timePnl = new JPanel(new GridBagLayout());
            c.gridx = 0;
            c.weightx = 1;
            c.fill = GridBagConstraints.VERTICAL;
            timePnl.add(hours, c);

            c.gridx += 1;
            c.weightx = 1;
            timePnl.add(minutes, c);
            c.gridx += 1;
            c.weightx = 1;
            timePnl.add(seconds, c);
        }
        seconds.setVisible(bShowSeconds);
        return timePnl;
    }

    private void notifyListeners() {
        for (ChangeListener changeListener : changeListeners) {
            changeListener.stateChanged(getEvent());
        }
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        datePicker.setName(name + "DatePicker");
        hours.setName(name + "Hours");
        minutes.setName(name + "Minutes");
        seconds.setName(name + "Seconds");
    }
}
