/**
 * @class Uni.form.field.AtPeriod
 */
Ext.define('Uni.form.field.AtPeriod', {
    extend: 'Ext.form.FieldContainer',
    xtype: 'uni-form-field-atperiod',

    fieldLabel: 'At',

    layout: {
        type: 'hbox'
    },

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
                value: 0,
                minValue: 0,
                maxValue: 23,
                allowBlank: false,
                width: 64,
                margin: '0 6 0 0'
            },
            {
                xtype: 'label',
                text: ':',
                cls: Ext.baseCSSPrefix + 'form-cb-label'
            },
            {
                xtype: 'numberfield',
                itemId: 'minute-field',
                hideLabel: true,
                value: 0,
                minValue: 0,
                maxValue: 59,
                allowBlank: false,
                width: 64,
                margin: '0 6 0 6'
            }
        ];
    },

    initListeners: function () {
        var me = this;

        me.getHourField().on('change', function () {
            me.fireEvent('periodchange', me.getValue());
        }, me);

        me.getMinuteField().on('change', function () {
            me.fireEvent('periodchange', me.getValue());
        }, me);

        me.down('label').getEl().on('click', function () {
            if (!me.getHourField().isDisabled()) {
                me.getHourField().focus();
            } else {
                me.getMinuteField().focus();
            }
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
    }
});