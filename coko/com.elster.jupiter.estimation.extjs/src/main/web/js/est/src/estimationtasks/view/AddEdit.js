Ext.define('Est.estimationtasks.view.AddEdit', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.form.field.DateTime',
        'Uni.util.FormErrorMessage'
    ],
    alias: 'widget.estimationtasks-addedit',

    returnLink: null,
    edit: false,

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'form',
                title: Uni.I18n.translate('estimationtasks.general.addEstimationTask', 'EST', 'Add estimation task'),
                itemId: 'add-edit-estimationtask-form',
                ui: 'large',
                width: '100%',
                defaults: {
                    labelWidth: 250
                },
                items: [
                    {
                        itemId: 'form-errors',
                        xtype: 'uni-form-error-message',
                        name: 'formErrors',
                        margin: '0 0 10 0',
                        hidden: true,
                        width: 500
                    },
                    {
                        xtype: 'textfield',
                        name: 'name',
                        itemId: 'task-name',
                        width: 500,
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.name', 'EST', 'Name'),
                        allowBlank: false,
                        enforceMaxLength: true,
                        maxLength: 80
                    },
                    {
                        title: Uni.I18n.translate('estimationtasks.general.dataSources', 'EST', 'Data sources'),
                        ui: 'medium'
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('estimationtasks.general.deviceGroup', 'EST', 'Device group'),
                        required: true,
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'combobox',
                                itemId: 'device-group-id',
                                name: 'deviceGroupId',
                                width: 235,
                                store: 'Est.estimationtasks.store.DeviceGroups',
                                editable: false,
                                disabled: false,
                                emptyText: Uni.I18n.translate('estimationtasks.addEstimationTask.deviceGroupPrompt', 'EST', 'Select a device group...'),
                                allowBlank: false,
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'id'
                            },
                            {
                                xtype: 'displayfield',
                                itemId: 'no-device',
                                hidden: true,
                                value: '<div style="color: #FF0000">' + Uni.I18n.translate('estimationtasks.general.noDeviceGroup', 'EST', 'No device group defined yet.') + '</div>',
                                htmlEncode: false,
                                width: 235
                            }
                        ]
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
                                                Ext.ComponentQuery.query('estimationtasks-addedit #every')[0].setValue(true);
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
                                                Ext.ComponentQuery.query('estimationtasks-addedit #every')[0].setValue(true);
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
                                        boxLabel: Uni.I18n.translate('estimationtasks.general.period', 'EST', 'Period'),
                                        inputValue: true,
                                        listeners: {
                                            change: function (radioButton, newValue) {
                                                Ext.ComponentQuery.query('estimationtasks-addedit #estimationPeriod-id')[0].allowBlank = !newValue;
                                            }
                                        }
                                    }
                                ]
                            },
                            {
                                itemId: 'estimation-period-values',
                                xtype: 'fieldcontainer',
                                name: 'estimationPeriodValues',
                                margin: '30 0 10 0',
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
                                                Ext.ComponentQuery.query('estimationtasks-addedit #period')[0].setValue(true);
                                            }
                                        }
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'add-button',
                                text: Uni.I18n.translate('general.add', 'EST', 'Add'),
                                ui: 'action'
                            },
                            {
                                xtype: 'button',
                                itemId: 'cancel-link',
                                text: Uni.I18n.translate('general.cancel', 'EST', 'Cancel'),
                                ui: 'link',
                                href: '#/administration/estimationtasks/'
                            }
                        ]
                    }
                ]
            }
        ];
        me.callParent(arguments);
        me.setEdit(me.edit);
    },

    setEdit: function (edit) {
        if (edit) {
            this.edit = edit;
            this.down('#add-button').setText(Uni.I18n.translate('general.save', 'EST', 'Save'));
            this.down('#add-button').action = 'editTask';
        } else {
            this.edit = edit;
            this.down('#add-button').setText(Uni.I18n.translate('general.add', 'EST', 'Add'));
            this.down('#add-button').action = 'addTask';
        }
        if (this.returnLink) {
            this.down('#cancel-link').href = this.returnLink;
        }
    },

    recurrenceNumberFieldEstimation: function (field) {
        var value = field.getValue();
        if (Ext.isEmpty(value) || value < field.minValue) {
            field.setValue(field.minValue);
        }
    }
});