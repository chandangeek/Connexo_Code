Ext.define('Uni.property.view.property.Time', {
    extend: 'Uni.property.view.property.Base',

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'container',
            layout: 'hbox',
            name: this.getName(),
            width: me.width,
            required: me.required,
            items: [
                {
                    xtype: 'numberfield',
                    itemId: me.key + 'hourField',
                    readOnly: me.isReadOnly,
                    width: me.width / 2 - 5,
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
                    margin: '0 3 0 0'
                },
                {
                    xtype: 'label',
                    text: ':',
                    margin: '5 0 0 0'
                },
                {
                    xtype: 'numberfield',
                    itemId: me.key + 'minuteField',
                    readOnly: me.isReadOnly,
                    width: me.width / 2 - 5,
                    valueToRaw: function (value) {
                        if (!Ext.isDefined(value)) {
                            return null;
                        }
                        value = value || 0;
                        return (value < 10 ? '0' : '') + value;
                    },
                    maxValue: 59,
                    minValue: 0,
                    allowDecimals: false,
                    submitValue: false,
                    margin: '0 0 0 3'
                }
            ]
        };
    },

    getHoursField: function () {
        return this.down('#' + this.key + 'hourField');
    },

    getMinutesField: function () {
        return this.down('#' + this.key + 'minuteField');
    },

    getField: function () {
        return this;
    },

    setValue: function (value /*time in seconds*/) {
        var theDate, hours = 0, minutes = 0, doSet = false;
        if (value === '') {
            doSet = true;
        } else if (value !== null && value !== '') {
            theDate = new Date(value * 1000);
            hours = theDate.getHours();
            minutes = theDate.getMinutes();
            doSet = true;
        }
        if (doSet) {
            if (!this.isEdit) {
                this.callParent([this.getValueAsDisplayString(value)]);
            } else {
                this.getHoursField().setValue(hours);
                this.getMinutesField().setValue(minutes);
            }
        }
    },

    getValue: function () {
        var hourValue = this.getHoursField().getValue(),
            minValue = this.getMinutesField().getValue();
        if (hourValue !== null && hourValue !== '' && hourValue !== 0 &&
            minValue !== null && minValue !== '' && minValue !==0) {
            var newDate = new Date(1970, 0, 1, hourValue, minValue, 0, 0);
            return newDate.getTime() / 1000; // time in seconds
        }
        this.getHoursField().setValue(null);
        this.getMinutesField().setValue(null);
        return null;
    },

    initListeners: function () {
        var me = this;
        var hoursField = me.getHoursField();
        var minutesField = me.getMinutesField();

        if (hoursField) {
            me.addFieldListeners(hoursField);
        }
        if (minutesField) {
            me.addFieldListeners(minutesField);
        }
    },

    getValueAsDisplayString: function (value /*time in seconds*/) {
        var theDate = new Date(value * 1000),
            hours = theDate.getHours(),
            minutes = theDate.getMinutes();
        return (hours < 10 ? '0' : '') + hours + ':' + (minutes < 10 ? '0' : '') + minutes;
    }

});