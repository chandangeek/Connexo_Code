/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.taskmanagement.AddTaskManagement', {
    extend: 'Ext.form.Panel',
    alias: 'widget.cfg-validation-tasks-add-task-mgm',
    requires: [
        'Uni.form.field.DateTime',
        'Cfg.view.validationtask.DataSourcesContainer',
        'Ldr.store.LogLevels',
        'Cfg.store.AllTasks'
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
                itemId: 'txt-task-name',
                width: 600,
                required: true,
                fieldLabel: Uni.I18n.translate('general.name', 'CFG', 'Name'),
                enforceMaxLength: true,
                vtype: 'checkForBlacklistCharacters',
                maxLength: 80,
                listeners: {
                    afterrender: function (field) {
                        field.focus(false, 200);
                    }
                }
            },
            {
                xtype: 'combobox',
                fieldLabel: Uni.I18n.translate('general.logLevel', 'CFG', 'Log level'),
                required: true,
                name: 'logLevel',
                width: 600,
                itemId: 'cfg-validation-task-add-loglevel',
                allowBlank: false,
                store: 'LogLevelsStore',
                queryMode: 'local',
                displayField: 'displayValue',
                valueField: 'id'
            },
            {
                xtype: 'combobox',
                itemId: 'followedBy-combo',
                fieldLabel: Uni.I18n.translate('general.followedBy', 'CFG', 'Followed by'),
                name: 'nextRecurrentTasks',
                width: 600,
                multiSelect: true,
                queryMode: 'local',
                store: 'Cfg.store.AllTasks',
                editable: false,
                emptyText: Uni.I18n.translate('validationTasks.taskSelectorPrompt', 'CFG', 'Select a task ...'),
                displayField: 'displayName',
                valueField: 'id'
            },
            {
                title: Uni.I18n.translate('validationTasks.general.dataSources', 'CFG', 'Data sources'),
                ui: 'medium'
            },
            {
                xtype: 'cfg-data-sources-container',
                itemId: 'field-validation-task-group',
                defaults: {
                    labelWidth: 250
                },
                comboWidth: 335,
                appName: me.appName,
                edit: me.edit
            },
            {
                title: Uni.I18n.translate('validationTasks.general.schedule', 'CFG', 'Schedule'),
                ui: 'medium'
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('validationTasks.general.recurrence', 'CFG', 'Recurrence'),
                itemId: 'recurrence-container',
                required: true,
                layout: 'hbox',
                items: [
                    {
                        itemId: 'rgr-validation-tasks-recurrence-trigger',
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
                                itemId: 'rbtn-none-recurrence',
                                boxLabel: Uni.I18n.translate('general.none', 'CFG', 'None'),
                                inputValue: false,
                                checked: true
                            },
                            {
                                itemId: 'rbtn-every',
                                margin: '7 0 0 0',
                                boxLabel: Uni.I18n.translate('validationTasks.general.every', 'CFG', 'Every'),
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
                                itemId: 'num-recurrence-number',
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
                                            var radioButton = Ext.ComponentQuery.query('cfg-validation-tasks-add-task-mgm #rbtn-every')[0];
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
                                store: 'Cfg.store.DaysWeeksMonths',
                                queryMode: 'local',
                                displayField: 'displayValue',
                                valueField: 'name',
                                editable: false,
                                width: 125,
                                listeners: {
                                    focus: {
                                        fn: function () {
                                            var radioButton = Ext.ComponentQuery.query('cfg-validation-tasks-add-task-mgm #rbtn-every')[0];
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
                fieldLabel: Uni.I18n.translate('validationTasks.general.startOn', 'CFG', 'Start on'),
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
                            fieldLabel: Uni.I18n.translate('validationTasks.general.at', 'CFG', 'at'),
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
