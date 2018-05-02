/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.customtask.AddCustomTask', {
    extend: 'Ext.form.Panel',
    alias: 'widget.ctk-add-custom-task',
    requires: [
        'Uni.form.field.DateTime',
        'Ldr.store.LogLevels',
        'Apr.store.AllTasks',
        'Apr.store.DaysWeeksMonths'
    ],
    defaults: {
        labelWidth: 250
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'textfield',
                name: 'name',
                itemId: 'ctk-task-name',
                width: 600,
                required: true,
                fieldLabel: Uni.I18n.translate('general.name', 'APR', 'Name'),
                enforceMaxLength: true,
                maxLength: 80,
                listeners: {
                    afterrender: function (field) {
                        field.focus(false, 200);
                    }
                }
            },
            {
                xtype: 'combobox',
                fieldLabel: Uni.I18n.translate('customTask.general.logLevel', 'APR', 'Log level'),
                required: true,
                name: 'logLevel',
                width: 600,
                itemId: 'ctk-loglevel',
                allowBlank: false,
                store: 'LogLevelsStore',
                queryMode: 'local',
                displayField: 'displayValue',
                valueField: 'id'
            },
            {
                xtype: 'combobox',
                itemId: 'ctk-followedBy-combo',
                fieldLabel: Uni.I18n.translate('customTask.general.followedBy', 'APR', 'Followed by'),
                name: 'nextRecurrentTasks',
                width: 600,
                multiSelect: true,
                queryMode: 'local',
                store: 'Apr.store.AllTasks',
                editable: false,
                emptyText: Uni.I18n.translate('customTask.taskSelectorPrompt', 'APR', 'Select a task ...'),
                displayField: 'displayName',
                valueField: 'id'
            },
            {
                title: Uni.I18n.translate('customTask.general.schedule', 'APR', 'Schedule'),
                ui: 'medium'
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('customTask.general.recurrence', 'APR', 'Recurrence'),
                itemId: 'ctk-recurrence-container',
                required: true,
                layout: 'hbox',
                items: [
                    {
                        itemId: 'ctk-recurrence-trigger',
                        xtype: 'radiogroup',
                        name: 'recurrenceTrigger',
                        columns: 1,
                        vertical: true,
                        width: 100,
                        defaults: {
                            name: 'recurrence'
                        },
                        items: [
                            {
                                itemId: 'ctk-none-recurrence',
                                boxLabel: Uni.I18n.translate('general.none', 'APR', 'None'),
                                inputValue: false,
                                checked: true
                            },
                            {
                                itemId: 'ctk-every',
                                margin: '7 0 0 0',
                                boxLabel: Uni.I18n.translate('customTask.general.every', 'APR', 'Every'),
                                inputValue: true
                            }
                        ]
                    },
                    {
                        itemId: 'ctk-recurrence-values',
                        xtype: 'fieldcontainer',
                        name: 'recurrenceValues',
                        margin: '30 0 10 0',
                        layout: 'hbox',
                        items: [
                            {
                                itemId: 'ctk-num-recurrence-number',
                                xtype: 'numberfield',
                                name: 'recurrence-number',
                                allowDecimals: false,
                                minValue: 1,
                                value: 1,
                                width: 65,
                                margin: '0 10 0 0',
                                listeners: {
                                    focus: {
                                        fn: function () {
                                            var radioButton = Ext.ComponentQuery.query('ctk-add-custom-task #ctk-every')[0];
                                            radioButton.setValue(true);
                                        }
                                    },
                                    blur: me.recurrenceNumberFieldValidation
                                }
                            },
                            {
                                itemId: 'cbo-recurrence-type',
                                xtype: 'combobox',
                                name: 'recurrence-type',
                                store: 'Apr.store.DaysWeeksMonths',
                                queryMode: 'local',
                                displayField: 'displayValue',
                                valueField: 'name',
                                editable: false,
                                width: 125,
                                listeners: {
                                    focus: {
                                        fn: function () {
                                            var radioButton = Ext.ComponentQuery.query('ctk-add-custom-task #ctk-every')[0];
                                            radioButton.setValue(true);
                                        }
                                    }
                                }
                            }
                        ]
                    }
                ]
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('customTask.general.startOn', 'APR', 'Start on'),
                required: true,
                layout: 'hbox',
                items: [
                    {
                        xtype: 'date-time',
                        itemId: 'start-on',
                        layout: 'hbox',
                        name: 'start-on',
                        dateConfig: {
                            allowBlank: true,
                            value: new Date(),
                            editable: false,
                            format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                        },
                        hoursConfig: {
                            fieldLabel: Uni.I18n.translate('customTask.general.at', 'APR', 'at'),
                            labelWidth: 10,
                            margin: '0 0 0 10',
                            value: new Date().getHours()
                        },
                        minutesConfig: {
                            width: 55,
                            value: new Date().getMinutes()
                        }
                    }
                ]
            },
            {
                xtype: 'container',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                itemId: 'ctk-properties',
                items: []
            },
            {
                xtype: 'uni-form-empty-message',
                itemId: 'ctk-no-properties',
                text: Uni.I18n.translate('customTask.taskNoAttributes', 'APR', 'This task has no task execution attributes.'),
                hidden: true
            }
        ];
        me.callParent(arguments);
    },

    recurrenceNumberFieldValidation: function (field) {
        var value = field.getValue();

        if (Ext.isEmpty(value) || value < field.minValue) {
            field.setValue(field.minValue);
        }
    }
});
