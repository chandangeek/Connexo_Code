/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.forms.fields.InstallationTimeField', {
    extend: 'Ext.form.RadioGroup',
    alias: 'widget.techinfo-installationtimefield',
    columns: 1,

    layout: {
        type: 'vbox',
        align: 'stretchmax'
    },

    listeners: {
        change: function (field, newValue) {
            var dateField = field.down('date-time');

            Ext.suspendLayouts();
            if (newValue['installation-time']) {
                dateField.disable();
                dateField.setValue(null);
            } else {
                dateField.enable();
                dateField.setValue(new Date());
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
                boxLabel: Uni.I18n.translate('general.now', 'IMT', 'Now'),
                name: 'installation-time',
                inputValue: true,
                submitValue: false
            },
            {
                xtype: 'container',
                layout: 'hbox',
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
                        dateConfig: {
                            width: 133
                        },
                        dateTimeSeparatorConfig: {
                            html: Uni.I18n.translate('general.at', 'IMT', 'At').toLowerCase(),
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