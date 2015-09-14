/**
 * @class Uni.form.field.AtPeriod
 */
Ext.define('Uni.form.field.AtPeriod', {
    extend: 'Ext.form.FieldContainer',
    xtype: 'uni-form-field-atperiod',

    fieldLabel: Uni.I18n.translate('general.at', 'UNI', 'At'),

    layout: {
        type: 'hbox'
    },

    lastHourTask: undefined,
    lastMinuteTask: undefined,

    initComponent: function () {
        var me = this;

        me.buildItems();
        me.callParent(arguments);

        me.on('afterrender', me.initListeners, me);
    },

    buildItems: function () {
        var me = this;

        me.items = [
            {
                xtype: 'numberfield',
                itemId: 'hour-field',
                hideLabel: true,
                valueToRaw: me.formatDisplayOfTime,
                value: 0,
                minValue: 0,
                maxValue: 23,
                editable: false,
                allowBlank: false,
                width: 64,
                margin: '0 6 0 0'
            },
            {
                xtype: 'label',
                text: ':',
                cls: Ext.baseCSSPrefix + 'form-item-label',
                style: {
                    fontWeight: 'normal'
                }
            },
            {
                xtype: 'numberfield',
                itemId: 'minute-field',
                hideLabel: true,
                valueToRaw: me.formatDisplayOfTime,
                value: 0,
                minValue: 0,
                maxValue: 59,
                editable: false,
                allowBlank: false,
                width: 64,
                margin: '0 6 0 6'
            }
        ];
    },

    initListeners: function () {
        var me = this;

        me.getHourField().on('change', function () {
            if (me.lastHourTask) {
                me.lastHourTask.cancel();
            }

            me.lastHourTask = new Ext.util.DelayedTask(function () {
                me.fireEvent('periodchange', me.getValue());
            });

            me.lastHourTask.delay(256);
        }, me);

        me.getMinuteField().on('change', function () {
            if (me.lastMinuteTask) {
                me.lastMinuteTask.cancel();
            }

            me.lastMinuteTask = new Ext.util.DelayedTask(function () {
                me.fireEvent('periodchange', me.getValue());
            });

            me.lastMinuteTask.delay(256);
        }, me);
    },

    getHourField: function () {
        return this.down('#hour-field');
    },

    getMinuteField: function () {
        return this.down('#minute-field');
    },

    getValue: function () {
        var me = this,
            hourValue = me.getHourField().getValue(),
            minuteValue = me.getMinuteField().getValue();

        return {
            atHour: hourValue,
            atMinute: minuteValue
        };
    },

    // TODO Use the date-time xtype for this.
    formatDisplayOfTime: function (value) {
        var result = '00';

        if (value) {
            if (value < 10 && value > 0) {
                result = '0' + value;
            } else if (value >= 10) {
                result = value;
            }
        }
        return result;
    }
});