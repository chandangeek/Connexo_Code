/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.DateTime', {
    extend: 'Uni.property.view.property.Date',

    timeFormat: 'H:i:s',
    resetButtonHidden: true,
    widthOfTimePart: 151,
    xtype: 'property-DateTime',

    getEditCmp: function () {
        var me = this,
            result = [];

        result[0] = this.callParent(arguments);
        delete result[0].maxWidth;
        result[0].width = me.width ? me.width - me.widthOfTimePart : undefined;
        result[1] = {
            xtype: 'container',
            layout: 'hbox',
            align: 'stretch',
            margin: '0 0 0 5',
            items: [
                {
                    xtype: 'label',
                    text: Uni.I18n.translate('general.at.lowercase', 'UNI','at'),
                    style: {
                        fontWeight: 'normal'
                    },
                    margin: '7 3 0 0'
                },
                {
                    xtype: 'numberfield',
                    itemId: 'hourField',
                    readOnly: me.isReadOnly,
                    maxWidth: 60,
                    margin: '0 3 0 0',
                    flex: 1,
                    valueToRaw: function (value) {
                        if (!Ext.isDefined(value)) {
                            return null;
                        }
                        value = value || 0;
                        return (value < 10 ? '0' : '') + value;
                    },
                    maxValue: 23,
                    minValue: 0,
                    allowDecimals: false,
                    submitValue: false,
                    listeners: {
                        change: {
                            fn: me.checkValidHour,
                            scope: me
                        }
                    }
                },
                {
                    xtype: 'label',
                    text: ':',
                    margin: '5 0 0 0'
                },
                {
                    xtype: 'numberfield',
                    itemId:'minuteField',
                    readOnly: me.isReadOnly,
                    maxWidth: 60,
                    valueToRaw: function (value) {
                        if (!Ext.isDefined(value)) {
                            return null;
                        }
                        value = value || 0;
                        return (value < 10 ? '0' : '') + value;
                    },
                    flex: 1,
                    maxValue: 59,
                    minValue: 0,
                    allowDecimals: false,
                    submitValue: false,
                    margin: '0 0 0 3',
                    listeners: {
                        change: {
                            fn: me.checkValidMinute,
                            scope: me
                        }
                    }
                }
            ]
        };
        return result;
    },

    checkValidHour: function() {
      this.checkValid(this.getHoursField());
    },

    checkValidMinute: function() {
        this.checkValid(this.getMinutesField());
    },

    checkValid: function (field) {
        var me = this,
            number = field.getValue();

        if (Ext.isNumber(number)) {
            if (number > field.maxValue) field.setValue(field.maxValue);
            if (number < field.minValue) field.setValue(field.minValue);
        } else {
            field.setValue(field.minValue);
        }
    },

    getHoursField: function () {
        return this.down('#hourField');
    },

    getMinutesField: function () {
        return this.down('#minuteField');
    },

    doEnable: function(enable) {
        if (this.getField()) {
            if (enable) {
                this.getHoursField().enable();
                this.getMinutesField().enable();
            } else {
                this.getHoursField().disable();
                this.getMinutesField().disable();
            }
        }
    },

    setValue: function (value /*Date in milliseconds*/) {
        var me = this,
            dateValue = null,
            hours = 0,
            minutes = 0;

        if (!Ext.isEmpty(value)) {
            var date = new Date(value);
            dateValue = new Date(date.getFullYear(), date.getMonth(), date.getDate(), 0, 0, 0, 0);
            hours = date.getHours();
            minutes = date.getMinutes();
        }

        if (!this.isEdit) {
            me.getDisplayField().setValue(this.getValueAsDisplayString(value));
        } else {
            me.callParent([dateValue]);
            me.getHoursField().setValue(hours);
            me.getMinutesField().setValue(minutes);
        }
    },

    getValue: function () {
        var dateValue = this.getField().getValue(), // date in milliseconds
            hourValue = this.getHoursField().getValue(),
            minValue = this.getMinutesField().getValue();

        if (!Ext.isEmpty(dateValue)) {
            if (hourValue !== null && hourValue !== '' && minValue !== null && minValue !== '') {
                var newDate = new Date(dateValue);
                var resultDate = new Date(newDate.getFullYear(), newDate.getMonth(), newDate.getDate(), hourValue, minValue, 0);
                return resultDate.getTime(); // Date in milliseconds
            }
            return dateValue;
        } else {
            this.getHoursField().setValue(null);
            this.getMinutesField().setValue(null);
            return null;
        }
    },

    initListeners: function () {
        var me = this,
            hoursField = me.getHoursField(),
            minutesField = me.getMinutesField();

        if (hoursField) {
            me.addFieldListeners(hoursField);
        }
        if (minutesField) {
            me.addFieldListeners(minutesField);
        }
        this.callParent(arguments);
    },

    getValueAsDisplayString: function (value /*Date in milliseconds*/) {
        return !Ext.isEmpty(value) ? Uni.DateTime.formatDateTimeShort(new Date(value)) : '';
    }

});