Ext.define('Mdc.view.setup.datacollectionkpis.KpiFieldContainer', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.kpi-field-container',
    required: true,
    groupName: 'default',
    width: 600,
    layout: {
        type: 'hbox',
        align: 'right'
    },

    initComponent: function () {
        var me = this;

        me.items = [

            {
                itemId: 'KpiRadioGroup',
                xtype: 'radiogroup',
                columns: 1,
                required: true,
                vertical: true,
                defaults: {
                    name: me.groupName,
                    submitValue: false
                },
                items: [
                    { itemId: 'yesTarget', boxLabel: Uni.I18n.translate('general.yesWithTarget', 'MDC', 'Yes, with target'), name: me.groupName, inputValue: true, checked: true },
                    { itemId: 'noTarget', boxLabel: Uni.I18n.translate('general.no', 'MDC', 'No'), name: me.groupName, inputValue: false}
                ],
                listeners: {
                    change: function (field, newValue) {
                        var numberField = me.down('numberfield[name=' + me.groupName + ']');
                        if (newValue[me.groupName]) {
                            numberField.enable();
                        } else {
                            numberField.setValue(0);
                            numberField.disable();
                        }
                    }
                }
            },
            {
                xtype: 'numberfield',
                name: me.groupName,
                margin: '0 5 0 10',
                width: 70,
                value: 0,
                minValue: 0,
                maxValue: 100,
                listeners: {
                    blur: function (field) {
                        if (field.getValue() < 0 || field.getValue() > 100) {
                            field.setValue(0);
                        }
                    }
                }
            },
            {
                xtype: 'displayfield',
                value: '%'
            }
        ];

        me.getValue = function () {
            var radiogroup = me.down('#KpiRadioGroup'),
                numberField = me.down('numberfield[name=' + me.groupName + ']');

            if (radiogroup.getValue()[me.groupName]) {
                return numberField.getValue();
            } else {
                return null;
            }
        };

        me.setValue = function (value) {
            var radiogroup = me.down('#KpiRadioGroup'),
                numberField = me.down('numberfield[name=' + me.groupName + ']'),
                radioGroupValue = {};

            if (!Ext.isEmpty(value)) {
                radioGroupValue[me.groupName] = true;
                numberField.setValue(value);
            } else {
                radioGroupValue[me.groupName] = false;
            }

            radiogroup.setValue(radioGroupValue);
        };

        me.callParent(arguments);
    }

});