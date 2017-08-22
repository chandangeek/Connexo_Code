/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tme.view.relativeperiod.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.relative-periods-preview-form',
    defaults: {
        xtype: 'displayfield',
        labelWidth: 250
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                fieldLabel: Uni.I18n.translate('relativeperiod.name', 'TME', 'Name'),
                name: 'name'
            },
            {
                fieldLabel: Uni.I18n.translate('relativeperiod.category', 'TME', 'Category'),
                name: 'listOfCategories'
            },
            {
                fieldLabel: Uni.I18n.translate('relativeperiod.start', 'TME', 'Start'),
                name: 'from',
                renderer: function(value) {
                    if(!Ext.isEmpty(value)) {
                        return me.getTranslation(value);
                    } else {
                        return '-';
                    }
                }
            },
            {
                fieldLabel: Uni.I18n.translate('relativeperiod.end', 'TME', 'End'),
                name: 'to',
                renderer: function(value) {
                    if(!Ext.isEmpty(value)) {
                        return me.getTranslation(value);
                    } else {
                        return '-';
                    }
                }
            },
            {
                xtype: 'panel',
                layout: 'hbox',
                items: [
                    {
                        xtype: 'displayfield',
                        labelWidth: 250,
                        fieldLabel: Uni.I18n.translate('relativeperiod.form.preview', 'TME', 'Preview'),
                        emptyValueDisplay: ''
                    },
                    {
                        xtype: 'uni-form-relativeperiodpreview-basedOnId'
                    }
                ]
            }
        ];
        me.callParent(arguments);
    },

    getTranslation: function (relativePeriod) {
        var me = this,
            date = new Date();
        if(!Ext.isEmpty(relativePeriod.startNow)) {
            return Uni.I18n.translate('general.Now', 'TME', 'Now');
        }
        if (!Ext.isEmpty(relativePeriod.startFixedYear)) {
            date.setYear(relativePeriod.startFixedYear);
            date.setMonth(relativePeriod.startFixedMonth - 1);
            date.setDate(relativePeriod.startFixedDay);
            date.setHours(relativePeriod.atHour);
            date.setMinutes(relativePeriod.atMinute);
            return Uni.DateTime.formatDateTimeShort(date)
        } else if (!Ext.isEmpty(relativePeriod.startPeriodAgo)) {
            var periodWithAmount = me.getPeriodWithAmountTranslation(relativePeriod.startPeriodAgo, relativePeriod.startAmountAgo);
            var startTimeMode = me.getStartTimeModeTranslation(relativePeriod.startTimeMode);
            switch (relativePeriod.startPeriodAgo) {
                case 'years':
                    date.setHours(relativePeriod.atHour);
                    date.setMinutes(relativePeriod.atMinute);
                    if (relativePeriod.onCurrentDayOfYear) {
                        return Uni.I18n.translate('relative.period.amountperiodDirectionAtTime', 'TME', '{0} {1} at {2}', [periodWithAmount, startTimeMode, Uni.DateTime.formatTimeShort(date)]);
                    } else {
                        return Uni.I18n.translate('relative.period.amountperiodDirectionOnDayOfMonthAtTime', 'TME', '{0} {1} on day {2} of {3} at {4}', [periodWithAmount, startTimeMode, relativePeriod.startFixedDay, me.getMonthTranslation(relativePeriod.startFixedMonth), Uni.DateTime.formatTimeShort(date)]);
                    }
                    break;
                case 'months':
                    date.setHours(relativePeriod.atHour);
                    date.setMinutes(relativePeriod.atMinute);
                    if (relativePeriod.onCurrentDay) {
                        return Uni.I18n.translate('relative.period.amountperiodDirectionAtTime', 'TME', '{0} {1} at {2}', [periodWithAmount, startTimeMode, Uni.DateTime.formatTimeShort(date)]);
                    } else {
                        return Uni.I18n.translate('relative.period.amountperiodDirectionOnDayAtTime', 'TME', '{0} {1} on day {2} at {3}', [periodWithAmount, startTimeMode, relativePeriod.onDayOfMonth, Uni.DateTime.formatTimeShort(date)]);
                    }
                    break;
                case 'weeks':
                    date.setHours(relativePeriod.atHour);
                    date.setMinutes(relativePeriod.atMinute);
                    return Uni.I18n.translate('relative.period.amountWeeksDirectionOnDayofweekAtTime', 'TME', '{0} {1} on {2} at {3}', [periodWithAmount, startTimeMode, me.getDayOfWeekTranslation(relativePeriod.onDayOfWeek), Uni.DateTime.formatTimeShort(date)]);
                    break;
                case 'days':
                    date.setHours(relativePeriod.atHour);
                    date.setMinutes(relativePeriod.atMinute);
                    return Uni.I18n.translate('relative.period.amountperiodDirectionAtTime', 'TME', '{0} {1} at {2}', [periodWithAmount, startTimeMode, Uni.DateTime.formatTimeShort(date)]);
                    break;
                case 'hours':
                    return Uni.I18n.translate('relative.period.amountHourDirectionAtMinutes', 'TME', '{0} {1} at {2} minute(s)', [periodWithAmount, startTimeMode, relativePeriod.atMinute]);
                    break;
                case 'minutes':
                    return Uni.I18n.translate('relative.period.amountMinutesDirection', 'TME', '{0} {1}', [periodWithAmount, startTimeMode]);
                    break;
            }
        }
    },

    getStartTimeModeTranslation: function (startTimeMode) {
        if (startTimeMode === 'ago') {
            return Uni.I18n.translate('general.period.ago', 'TME', 'ago');
        } else if (startTimeMode === 'ahead') {
            return Uni.I18n.translate('general.period.ahead', 'TME', 'ahead');
        }
    },

    getPeriodWithAmountTranslation: function (periodName, amount) {
        var periods = Ext.create('Uni.store.Periods'),
            period;

        period = periods.findRecord('value', periodName);
        return period.get('translate').call(period, amount);
    },

    getDayOfWeekTranslation: function(dayOfWeek) {
        var daysOfWeek = Ext.create('Uni.store.DaysOfWeek');

        return daysOfWeek.getById(dayOfWeek).get('translation')
    },

    getMonthTranslation: function(monthNumber) {
        switch (monthNumber) {
            case 1:
                return Uni.I18n.translate('general.month.january', 'TME', 'January');
            case 2:
                return Uni.I18n.translate('general.month.february', 'TME', 'February');
            case 3:
                return Uni.I18n.translate('general.month.march', 'TME', 'March');
            case 4:
                return Uni.I18n.translate('general.month.april', 'TME', 'April');
            case 5:
                return Uni.I18n.translate('general.month.may', 'TME', 'May');
            case 6:
                return Uni.I18n.translate('general.month.june', 'TME', 'June');
            case 7:
                return Uni.I18n.translate('general.month.july', 'TME', 'July');
            case 8:
                return Uni.I18n.translate('general.month.august', 'TME', 'August');
            case 9:
                return Uni.I18n.translate('general.month.september', 'TME', 'September');
            case 10:
                return Uni.I18n.translate('general.month.october', 'TME', 'October');
            case 11:
                return Uni.I18n.translate('general.month.november', 'TME', 'November');
            case 12:
                return Uni.I18n.translate('general.month.december', 'TME', 'December')
        }
    }
});