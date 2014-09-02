Ext.define('Dsh.view.widget.common.DateTimeField', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.datetime-field',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    defaults: {
        labelWidth: 30,
        labelAlign: 'left',
        labelStyle: 'font-weight: normal'
    },
    items: [
        {
            xtype: 'datefield',
            editable: false
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: '&nbsp;',
            layout: 'hbox',
            defaultType: 'numberfield',
            defaults: {
                flex: 1,
                value: 0,
                minValue: 0,
                enforceMaxLength: true,
                maxLength: 2,
                enableKeyEvents: true,
                stripCharsRe: /\D/,
                listeners: {
                    blur: function (field) {
                        if (Ext.isEmpty(field.getValue())) {
                            field.setValue(0);
                        }
                    }
                }
            },
            items: [
                {
                    maxValue: 23,
                    style: {
                        marginRight: '5px'
                    },
                    valueToRaw: function (value) {
                        return value < 10 ? Ext.String.leftPad(value, 2, '0') :
                            value > 23 ? 23 : value;
                    }
                },
                {
                    maxValue: 59,
                    style: {
                        marginLeft: '5px'
                    },
                    valueToRaw: function (value) {
                        return value < 10 ? Ext.String.leftPad(value, 2, '0') :
                            value > 59 ? 59 : value;
                    }
                }
            ]
        }
    ],
    initComponent: function () {
        this.callParent(arguments);
        this.down('datefield').setFieldLabel(this.label);
    }
});