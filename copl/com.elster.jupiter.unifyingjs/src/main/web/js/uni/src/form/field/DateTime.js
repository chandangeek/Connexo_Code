Ext.define('Uni.form.field.DateTime', {
    extend: 'Ext.form.FieldContainer',
    mixins: {
        field: 'Ext.form.field.Field'
    },
    alias: 'widget.date-time',
    layout: 'vbox',
    initComponent: function () {
        var me = this;
        me.items = [
            Ext.apply({
                xtype: 'datefield',
                itemId: 'date-time-field-date',
                submitValue: false,
                width: '100%'
            }, me.dateConfig),
            {
                xtype: 'container',
                width: '100%',
                layout: {
                    type: 'hbox',
                    align: 'middle'
                },
                defaults: {
                    xtype: 'numberfield',
                    itemId: 'minuteField',
                    allowDecimals: false,
                    submitValue: false,
                    value: 0,
                    valueToRaw: function (value) {
                        var result = '00';

                        if (value) {
                            if (value < 10 && value > 0) {
                                result = '0' + value;
                            } else if (value > 10) {
                                result = value;
                            }
                        }
                        return result;
                    },
                    listeners: {
                        blur: me.numberFieldValidation
                    }
                },
                items: [
                    Ext.apply({
                        itemId: 'date-time-field-hours',
                        flex: 1,
                        maxValue: 23,
                        minValue: 0
                    }, me.hoursConfig),
                    Ext.apply({
                        xtype: 'component',
                        html: ':',
                        margin: '0 10 0 10'
                    }, me.separatorConfig),
                    Ext.apply({
                        itemId: 'date-time-field-minutes',
                        flex: 1,
                        maxValue: 59,
                        minValue: 0
                    }, me.minutesConfig)
                ]
            }
        ];
        me.callParent(arguments);
    },

    numberFieldValidation: function (field) {
        var value = field.getValue();

        if(Ext.isEmpty(value) || value < field.minValue) {
            field.setValue(field.minValue);
        } else if (value > field.maxValue) {
            field.setValue(field.maxValue);
        }
    },

    setValue: function (value) {
        if (Ext.isDate(value) || Ext.isDate(new Date(value))) {
            this.down('#date-time-field-date').setValue(moment(value).startOf('day').toDate());
            this.down('#date-time-field-hours').setValue(moment(value).hours());
            this.down('#date-time-field-minutes').setValue(moment(value).minutes());
        } else {
            //<debug>
            console.error('\'' + value + '\' is not a date');
            //</debug>
        }
    },

    getValue: function() {
        var me = this,
            date = me.down('#date-time-field-date').getValue(),
            hours = me.down('#date-time-field-hours').getValue(),
            minutes = me.down('#date-time-field-minutes').getValue();

        if (date) {
            date = date.getTime();
            if (hours) {
                date += hours * 3600000;
            }
            if (minutes) {
                date += minutes * 60000;
            }
        }

        date = new Date(date);

        return me.submitFormat ? Ext.Date.format(date, me.submitFormat) : date;
    }
});

