/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.view.InstallationTimeField', {
    extend: 'Ext.form.RadioGroup',
    alias: 'widget.installationtimefield',
    columns: 1,
    defaultValueLabel: null,
    midnight: false,

    listeners: {
        change: function (field, newValue) {
            var me = this,
                dateField = field.down('date-time[name=' + me.dateFieldName + ']');

            Ext.suspendLayouts();
            if (newValue['installation-time']) {
                dateField.disable();
                dateField.setValue(null);
            } else {
                var currentDate = new Date();
                 if (me.midnight) {
                     currentDate.setHours(0,0,0,0);
                 }
                dateField.enable();
                dateField.setValue(currentDate);

            }
            Ext.resumeLayouts(true);
        }
    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'radiofield',
                itemId: 'installation-time-now',
                boxLabel: me.defaultValueLabel ? me.defaultValueLabel : Uni.I18n.translate('general.now', 'MDC', 'Now'),
                name: 'installation-time',
                inputValue: true,
                submitValue: false
            },
            {
                xtype: 'container',
                layout: 'hbox',
                width: 500,
                items: [
                    {
                        xtype: 'radiofield',
                        itemId: 'installation-time-at-date',
                        boxLabel: ' ',
                        name: 'installation-time',
                        inputValue: false,
                        submitValue: false
                    },
                    {
                        xtype: 'date-time',
                        name: me.dateFieldName,
                        itemId: 'installation-time-date',
                        required: true,
                        layout: 'hbox',
                        valueInMilliseconds: true,
                        minWidth: 500,
                        dateConfig: {
                            width: 148,
                            fieldLabel: me.dateOnLabel ? me.dateOnLabel : '',
                            labelWidth: 16
                        },
                        dateTimeSeparatorConfig: {
                            html: Uni.I18n.translate('general.lowercase.at', 'MDC', 'at'),
                            style: 'color: #686868'
                        },
                        hoursConfig: {
                            width: 64
                        },
                        minutesConfig: {
                            width: 64
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
        me.setValue({"installation-time": true});
    }
});