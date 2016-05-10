Ext.define('Cfg.view.validationtask.Add', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.cfg-validation-tasks-add',
    requires: [
        'Uni.form.field.DateTime',
        'Uni.util.FormErrorMessage',
        'Cfg.view.validationtask.DataSourcesContainer'
    ],

    edit: false,
    returnLink: null,
    appName: null,

    initComponent: function () {
        var me = this,
            dataSourcesContainer,
            groupComboConfig = {},
            groupLabel = '',
            noGroupDefinedTxt = '';

        //switch (me.appName) {
        //    case 'MultiSense':
        //        groupComboConfig = {
        //            name: 'deviceGroup',
        //            store: 'Cfg.store.DeviceGroups',
        //            emptyText: Uni.I18n.translate('validationTasks.addValidationTask.deviceGroupPrompt', 'CFG', 'Select a device group...'),
        //            disabled: !Cfg.privileges.Validation.canAdministrate()
        //        };
        //        groupLabel = Uni.I18n.translate('validationTasks.general.deviceGroup', 'CFG', 'Device group');
        //        noGroupDefinedTxt = Uni.I18n.translate('validationTasks.general.noDeviceGroup', 'CFG', 'No device group defined yet.');
        //        break;
        //    case 'MdmApp':
        //        groupComboConfig = {
        //            name: 'usagePointGroup',
        //            store: 'Cfg.store.UsagePointGroups',
        //            emptyText: Uni.I18n.translate('validationTasks.addValidationTask.usagePointGroupPrompt', 'CFG', 'Select a usage point group...')
        //        };
        //        groupLabel = Uni.I18n.translate('validationTasks.general.usagePointGroup', 'CFG', 'Usage point group');
        //        noGroupDefinedTxt = Uni.I18n.translate('validationTasks.general.noUsagePointGroup', 'CFG', 'No usage point group defined yet.');
        //        break;
        //}

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
                        width: 565
                    },
                    {
                        xtype: 'textfield',
                        name: 'name',
                        itemId: 'txt-task-name',
                        width: 565,
                        required: true,
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
                        xtype: 'cfg-data-sources-container',
                        itemId: 'field-validation-task-group',
                        appName: me.appName
                    },
                    //{
                    //    xtype: 'fieldcontainer',
                    //    itemId: 'field-validation-task-group',
                    //    fieldLabel: groupLabel,
                    //    required: true,
                    //    layout: 'hbox',
                    //    msgTarget: 'under',
                    //    items: [
                    //        Ext.apply({
                    //            xtype: 'combobox',
                    //            itemId: 'cbo-validation-task-group',
                    //            required: true,
                    //            width: 235,
                    //            editable: false,
                    //            queryMode: 'local',
                    //            displayField: 'name',
                    //            valueField: 'id',
                    //            setValue: function (value) {
                    //                var field = this,
                    //                    combo = new Ext.form.field.ComboBox;
                    //
                    //                if (Ext.isObject(value)) {
                    //                    value = value.id;
                    //                }
                    //                combo.setValue.apply(field, [value]);
                    //            }
                    //        }, groupComboConfig),
                    //        {
                    //            xtype: 'displayfield',
                    //            itemId: 'no-group-defined',
                    //            hidden: true,
                    //            value: '<div style="color: #FF0000">' + noGroupDefinedTxt + '</div>',
                    //            htmlEncode: false,
                    //            width: 235
                    //        }
                    //    ]
                    //},
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
                                ui: 'action',
                                text: me.edit
                                    ? Uni.I18n.translate('general.save', 'CFG', 'Save')
                                    : Uni.I18n.translate('general.add', 'CFG', 'Add'),
                                action: me.edit
                                    ? 'editTask'
                                    : 'addTask'
                            },
                            {
                                xtype: 'button',
                                itemId: 'cancel-link',
                                text: Uni.I18n.translate('general.cancel', 'CFG', 'Cancel'),
                                ui: 'link',
                                href: me.returnLink
                                    ? me.returnLink
                                    : '#/administration/validationtasks/'
                            }
                        ]
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
