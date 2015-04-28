/**
 * @class Uni.grid.filtertop.Date
 */
Ext.define('Uni.grid.filtertop.Date', {
    extend: 'Ext.form.FieldContainer',
    xtype: 'uni-grid-filtertop-date',

    mixins: [
        'Uni.grid.filtertop.Base'
    ],

    fieldLabel: Uni.I18n.translate('grid.filter.date.label', 'UNI', 'Date'),

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    items: [
        {
            xtype: 'datefield',
            editable: false,
            value: undefined,
            format: Uni.util.Preferences.lookup(Uni.DateTime.dateLongKey, Uni.DateTime.dateLongDefault),
            emptyText: Uni.I18n.translate('grid.filter.date.datefield.emptytext', 'UNI', 'Select a date')
        },
        {
            xtype: 'container',
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'numberfield',
                    minValue: 0,
                    maxValue: 23,
                    editable: false,
                    emptyText: Uni.I18n.translate('grid.filter.date.hourfield.emptytext', 'UNI', '00'),
                    flex: 1,
                    valueToRaw: function (value) {
                        if (!Ext.isDefined(value)) {
                            return null;
                        }

                        value = value || 0;
                        return (value < 10 ? '0' : '') + value;
                    }
                },
                {
                    xtype: 'label',
                    text: ':',
                    margin: '6 4 0 4'
                },
                {
                    xtype: 'numberfield',
                    minValue: 0,
                    maxValue: 59,
                    editable: false,
                    emptyText: Uni.I18n.translate('grid.filter.date.minutefield.emptytext', 'UNI', '00'),
                    flex: 1,
                    valueToRaw: function (value) {
                        if (!Ext.isDefined(value)) {
                            return null;
                        }

                        value = value || 0;
                        return (value < 10 ? '0' : '') + value;
                    }
                }
            ]
        }
    ],

    setFilterValue: function (date) {
        var me = this,
            dateField = me.getDateField(),
            hourField = me.getHourField(),
            minuteField = me.getMinuteField();

        if (Object.prototype.toString.call(date) !== '[object Date]') {
            date = new Date(parseInt(date));
        }

        dateField.setValue(date);
        hourField.setValue(date.getHours());
        minuteField.setValue(date.getMinutes());
    },

    getParamValue: function () {
        var me = this,
            date = me.getDateField().getValue(),
            hours = me.getHourField().getValue(),
            minutes = me.getMinuteField().getValue();

        if (Ext.isDefined(date) && !Ext.isEmpty(date)) {
            hours = typeof hours === "undefined" ? 0 : hours;
            minutes = typeof minutes === "undefined" ? 0 : minutes;

            date.setHours(hours);
            date.setMinutes(minutes);
            date.setSeconds(0);
            date.setMilliseconds(0);

            return date.getTime();
        }

        return undefined;
    },

    resetValue: function () {
        var me = this;

        me.getDateField().reset();
        me.getHourField().reset();
        me.getMinuteField().reset();
    },

    getDateField: function () {
        return this.down('datefield');
    },

    getHourField: function () {
        return this.down('numberfield:nth-child(1)');
    },

    getMinuteField: function () {
        return this.down('numberfield:nth-child(2)');
    }
});