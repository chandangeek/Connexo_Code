Ext.define('Cfg.view.validationtask.Add', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.cfg-validation-tasks-add',
    requires: [
        'Uni.form.field.DateTime',        
        'Uni.util.FormErrorMessage'
    ],

    edit: false,
    returnLink: null,
    setEdit: function (edit) {
        if (edit) {
            this.edit = edit;
            this.down('#add-button').setText(Uni.I18n.translate('general.save', 'CFG', 'Save'));
            this.down('#add-button').action = 'editTask';
        } else {
            this.edit = edit;
            this.down('#add-button').setText(Uni.I18n.translate('general.add', 'CFG', 'Add'));
            this.down('#add-button').action = 'addTask';
        }
        if (this.returnLink) {
            this.down('#cancel-link').href = this.returnLink;
        }
    },
    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'form',
                title: Uni.I18n.translate('validationTasks.general.addValidationTask', 'CFG', 'Add validation task'),
                itemId: 'frm-add-validation-task',
                ui: 'large',
                width: '100%',
                defaults: {
                    labelWidth: 250
                },
                items: [
                    {
                        itemId: 'form-errors',
                        xtype: 'uni-form-error-message',
                        name: 'form-errors',
                        margin: '0 0 10 0',
                        hidden: true,
                        width: 500
                    },
                    {
                        xtype: 'textfield',
                        name: 'name',
                        itemId: 'txt-task-name',
                        width: 500,
                        required: true,
                        allowBlank: false,
                        fieldLabel: Uni.I18n.translate('general.name', 'CFG', 'Name'),
                        enforceMaxLength: true,
                        maxLength: 80,
                        listeners: {
                            afterrender: function(field) {
                                field.focus(false, 200);
                            }
                        }
                    },
                    {
                        title: Uni.I18n.translate('validationTasks.general.dataSources', 'CFG', 'Data sources'),
                        ui: 'medium'
                    },
                    {
                        xtype: 'combobox',
                        itemId: 'rgr-validation-tasks-grouptype-trigger',
                        fieldLabel: Uni.I18n.translate('validationTasks.general.grouptype', 'CFG', 'Group type'),
                        name: 'grouptype',
                        required: true,
                        store: [
                            ['dg', Uni.I18n.translate('general.group.device', 'CFG', 'End device')],
                            ['upg', Uni.I18n.translate('general.group.usagepoint', 'CFG', 'Usage point')]
                        ],
                        emptyText: Uni.I18n.translate('validationTasks.addValidationTask.groupTypePrompt', 'CFG', 'Select a group type...'),
                        width: 500,
                        allowBlank: false,
                        listeners: {
                            change: function(field, newValue, oldValue) {
                                var deviceGroupField = me.down('#field-validation-task-device-group'),
                                    deviceGroupCombo = me.down('#cbo-validation-task-device-group'),
                                    usagePointGroupField = me.down('#field-validation-task-usagepoint-group'),
                                    usagePointGroupCombo = me.down('#cbo-validation-task-usagepoint-group');

                                Ext.suspendLayouts();
                                switch (newValue) {
                                    case 'dg':
                                        deviceGroupField.show();
                                        deviceGroupCombo.enable();
                                        usagePointGroupField.hide();
                                        usagePointGroupCombo.disable();
                                        usagePointGroupCombo.clearValue();
                                        usagePointGroupCombo.clearInvalid();
                                        break;
                                    case 'upg':
                                        usagePointGroupField.show();
                                        usagePointGroupCombo.enable();
                                        deviceGroupField.hide();
                                        deviceGroupCombo.disable();
                                        deviceGroupCombo.clearValue();
                                        deviceGroupCombo.clearInvalid();
                                        break;
                                }
                                Ext.resumeLayouts(true);
                            }
                        }
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'field-validation-task-device-group',
                        fieldLabel: Uni.I18n.translate('validationTasks.general.deviceGroup', 'CFG', 'Device group'),
                        required: true,
                        layout: 'hbox',
                        hidden: true,
                        msgTarget: 'under',
                        items: [
                            {
                                xtype: 'combobox',
                                itemId: 'cbo-validation-task-device-group',
                                name: 'deviceGroup',
                                width: 235,
                                store: 'Cfg.store.DeviceGroups',
                                editable: false,
                                disabled: true,
                                emptyText: Uni.I18n.translate('validationTasks.addValidationTask.deviceGroupPrompt', 'CFG', 'Select a device group...'),
                                allowBlank: false,
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'id'
                            },
                            {
                                xtype: 'displayfield',
                                itemId: 'no-device',
                                hidden: true,
                                value: '<div style="color: #EB5642">' + Uni.I18n.translate('validationTasks.general.noDeviceGroup', 'CFG', 'No device group defined yet.') + '</div>',
                                htmlEncode: false,
                                labelwidth: 500,
                                width: 235
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'field-validation-task-usagepoint-group',
                        fieldLabel: Uni.I18n.translate('validationTasks.general.usagePointGroup', 'CFG', 'Usage point group'),
                        required: true,
                        layout: 'hbox',
                        hidden: true,
                        msgTarget: 'under',
                        items: [
                            {
                                xtype: 'combobox',
                                itemId: 'cbo-validation-task-usagepoint-group',
                                name: 'usagePointGroup',
                                width: 235,
                                store: 'Cfg.store.UsagePointGroups',
                                editable: false,
                                disabled: true,
                                emptyText: Uni.I18n.translate('validationTasks.addValidationTask.usagepointGroupPrompt', 'CFG', 'Select a usage point group...'),
                                allowBlank: false,
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'id'
                            },
                            {
                                xtype: 'displayfield',
                                itemId: 'no-usagepoint',
                                hidden: true,
                                value: '<div style="color: #EB5642">' + Uni.I18n.translate('validationTasks.general.noUsagePointGroup', 'CFG', 'No usage point group defined yet.') + '</div>',
                                htmlEncode: false,
                                labelwidth: 500,
                                width: 235
                            }
                        ]
                    },
                    {
                        title: Uni.I18n.translate('validationTasks.general.schedule', 'CFG', 'Schedule'),
                        ui: 'medium'
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('validationTasks.general.recurrence', 'CFG', 'Recurrence'),
                        itemId: 'recurrence-container',
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
                                                    var radioButton = Ext.ComponentQuery.query('cfg-validation-tasks-add #rbtn-every')[0];
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
                                        width: 100,
                                        listeners: {
                                            focus: {
                                                fn: function () {
                                                    var radioButton = Ext.ComponentQuery.query('cfg-validation-tasks-add #rbtn-every')[0];
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
                                text: Uni.I18n.translate('general.add', 'CFG', 'Add'),
                                ui: 'action'
                            },
                            {
                                xtype: 'button',
                                itemId: 'cancel-link',
                                text: Uni.I18n.translate('general.cancel', 'CFG', 'Cancel'),
                                ui: 'link',
                                href: '#/administration/validationtasks/'
                            }
                        ]
                    }
					
                  
                ]
            }
        ];
        me.callParent(arguments);
        me.setEdit(me.edit);
    },
    recurrenceNumberFieldValidation: function (field) {
        var value = field.getValue();

        if (Ext.isEmpty(value) || value < field.minValue) {
            field.setValue(field.minValue);
        }
    }
});
