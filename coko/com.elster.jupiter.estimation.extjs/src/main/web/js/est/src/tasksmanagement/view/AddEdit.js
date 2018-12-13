/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.tasksmanagement.view.AddEdit', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.form.field.DateTime',
        'Est.estimationtasks.view.DataSourcesContainer',
        'Ldr.store.LogLevels',
        'Est.tasksmanagement.store.AllTasks'
    ],
    alias: 'widget.taskmanagement-addedit',

    defaults: {
        labelWidth: 250
    },
    returnLink: null,
    appName: null,
    edit: false,

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'textfield',
                name: 'name',
                itemId: 'task-name',
                width: 600,
                required: true,
                fieldLabel: Uni.I18n.translate('general.name', 'EST', 'Name'),
                allowBlank: false,
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
                fieldLabel: Uni.I18n.translate('general.logLevel', 'EST', 'Log level'),
                required: true,
                name: 'logLevel',
                width: 600,
                itemId: 'est-tasks-add-loglevel',
                allowBlank: false,
                store: 'LogLevelsStore',
                queryMode: 'local',
                displayField: 'displayValue',
                valueField: 'id'
            },
            {
                xtype: 'combobox',
                itemId: 'followedBy-combo',
                fieldLabel: Uni.I18n.translate('general.followedBy', 'EST', 'Followed by'),
                name: 'nextRecurrentTasks',
                width: 600,
                multiSelect: true,
                queryMode: 'local',
                store: 'Est.tasksmanagement.store.AllTasks',
                editable: false,
                emptyText: Uni.I18n.translate('estimationtasks.taskSelectorPrompt', 'EST', 'Select a task ...'),
                displayField: 'displayName',
                valueField: 'id'
            },
            {
                xtype: 'checkbox',
                itemId: 'revalidate-field',
                fieldLabel: Uni.I18n.translate('general.reValidate', 'EST', 'Re-validate estimated data'),
                name: 'revalidate'
            },
            {
                title: Uni.I18n.translate('estimationtasks.general.dataSources', 'EST', 'Data sources'),
                ui: 'medium'
            },
            {
                xtype: 'est-data-sources-container',
                appName: me.appName,
                comboWidth: 335
            },
            {
                title: Uni.I18n.translate('estimationtasks.general.schedule', 'EST', 'Schedule'),
                ui: 'medium'
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('estimationtasks.general.recurrence', 'EST', 'Recurrence'),
                required: true,
                itemId: 'recurrence-container',
                layout: 'hbox',
                items: [
                    {
                        itemId: 'recurrence-trigger',
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
                                itemId: 'none-recurrence',
                                boxLabel: Uni.I18n.translate('estimationtasks.general.none', 'EST', 'None'),
                                inputValue: false,
                                checked: true
                            },
                            {
                                itemId: 'every',
                                margin: '7 0 0 0',
                                boxLabel: Uni.I18n.translate('estimationtasks.general.every', 'EST', 'Every'),
                                inputValue: true
                            }
                        ]
                    },
                    {
                        itemId: 'recurrence-values',
                        xtype: 'fieldcontainer',
                        name: 'recurrenceValues',
                        margin: '30 0 10 0',
                        layout: 'hbox',
                        items: [
                            {
                                itemId: 'recurrence-number',
                                xtype: 'numberfield',
                                name: 'recurrenceNumber',
                                allowDecimals: false,
                                minValue: 1,
                                value: 1,
                                width: 65,
                                margin: '0 10 0 0',
                                listeners: {
                                    blur: me.recurrenceNumberFieldEstimation,
                                    focus: function () {
                                        Ext.ComponentQuery.query('taskmanagement-addedit #every')[0].setValue(true);
                                    }
                                }
                            },
                            {
                                itemId: 'recurrence-type',
                                xtype: 'combobox',
                                name: 'recurrenceType',
                                store: 'Est.estimationtasks.store.DaysWeeksMonths',
                                queryMode: 'local',
                                displayField: 'displayValue',
                                valueField: 'name',
                                editable: false,
                                width: 100,
                                listeners: {
                                    focus: function () {
                                        Ext.ComponentQuery.query('taskmanagement-addedit #every')[0].setValue(true);
                                    }
                                }
                            }
                        ]
                    }
                ]
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('estimationtasks.general.startOn', 'EST', 'Start on'),
                required: true,
                layout: 'hbox',
                items: [
                    {
                        xtype: 'date-time',
                        itemId: 'start-on',
                        layout: 'hbox',
                        name: 'startOn',
                        dateConfig: {
                            allowBlank: true,
                            value: new Date(),
                            editable: false,
                            format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                        },
                        hoursConfig: {
                            fieldLabel: Uni.I18n.translate('estimationtasks.general.at', 'EST', 'at'),
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
                title: Uni.I18n.translate('general.dataOptions', 'EST', 'Data options'),
                ui: 'medium'
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('estimationtasks.general.estimationPeriod', 'EST', 'Estimation period'),
                required: true,
                itemId: 'estimation-period-container',
                layout: 'hbox',
                items: [
                    {
                        itemId: 'estimation-period-trigger',
                        xtype: 'radiogroup',
                        name: 'estimationPeriodTrigger',
                        columns: 1,
                        vertical: true,
                        width: 100,
                        defaults: {
                            name: 'estimationPeriod'
                        },
                        items: [
                            {
                                itemId: 'all',
                                boxLabel: Uni.I18n.translate('estimationtasks.general.all', 'EST', 'All'),
                                inputValue: false,
                                checked: true
                            },
                            {
                                itemId: 'period',
                                margin: '5 0 0 0',
                                boxLabel: Uni.I18n.translate('estimationtasks.general.period', 'EST', 'Period'),
                                inputValue: true,
                                listeners: {
                                    change: function (radioButton, newValue) {
                                        Ext.ComponentQuery.query('taskmanagement-addedit #estimationPeriod-id')[0].allowBlank = !newValue;
                                    }
                                }
                            }
                        ]
                    },
                    {
                        itemId: 'estimation-period-values',
                        xtype: 'fieldcontainer',
                        name: 'estimationPeriodValues',
                        margin: '27 0 10 0',
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'combobox',
                                itemId: 'estimationPeriod-id',
                                name: 'estimationPeriodId',
                                width: 235,
                                queryMode: 'local',
                                store: 'Uni.property.store.RelativePeriods',
                                editable: false,
                                disabled: false,
                                emptyText: Uni.I18n.translate('estimationtasks.general.selectPeriodLabel', 'EST', 'Select an estimation period...'),
                                displayField: 'name',
                                valueField: 'id',
                                listeners: {
                                    focus: function () {
                                        Ext.ComponentQuery.query('taskmanagement-addedit #period')[0].setValue(true);
                                    }
                                }
                            }
                        ]
                    }
                ]
            }
        ];
        me.callParent(arguments);
    },

    recurrenceNumberFieldEstimation: function (field) {
        var value = field.getValue();
        if (Ext.isEmpty(value) || value < field.minValue) {
            field.setValue(field.minValue);
        }
    }
});