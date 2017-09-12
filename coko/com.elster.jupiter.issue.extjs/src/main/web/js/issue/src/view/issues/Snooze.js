/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.issues.Snooze', {
    extend: 'Ext.form.RadioGroup',
    alias: 'widget.snooze-date',
    columns: 1,
    defaults: {
        name: 'setSnooze'
    },
    defaultDate: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'container',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                width: 380,
                items: [
                    {
                        xtype: 'date-time',
                        itemId: 'issue-snooze-until-date',
                        layout: 'hbox',
                        name: 'until',
                        dateConfig: {
                            fieldLabel: Uni.I18n.translate('general.until', 'ISU', 'Until'),
                            labelWidth: 25,
                            margin: '0 0 0 1',
                            width: 156,
                            allowBlank: false,
                            value: me.defaultDate,
                            minValue: moment().toDate(),
                            editable: false,
                            format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                        },
                        hoursConfig: {
                            fieldLabel: Uni.I18n.translate('general.at', 'ISU', 'at'),
                            labelWidth: 10,
                            margin: '0 0 0 0',
                            value: me.defaultDate.getHours()
                        },
                        minutesConfig: {
                            width: 55,
                            value: me.defaultDate.getMinutes()
                        },
                        listeners: {
                            focus: {
                                fn: function () {
                                    me.down('#issue-snooze-until-date').setValue(true);
                                }
                            }
                        }
                    }

                ],
                action: 'applyAction'
            }
        ];

        me.callParent(arguments);
    }
});