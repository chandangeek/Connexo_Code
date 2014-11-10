Ext.define('Dxp.view.tasks.Add', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-export-tasks-add',
    requires: [
        'Uni.form.field.DateTime',
        'Dxp.view.tasks.AddScheduleGrid',
        'Uni.property.form.Property',
        'Dxp.view.tasks.PropertyForm',
        'Uni.util.FormErrorMessage'
    ],
    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'form',
                title: Uni.I18n.translate('general.addDataExportTask', 'DES', 'Add data export task'),
                itemId: 'add-data-export-task-form',
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
                        itemId: 'task-name',
                        width: 500,
                        required: true,
                        maskRe: /[^:\\/*?"<>|]/,
                        emptyText: Uni.I18n.translate('general.emptyTextName', 'DES', 'Enter a name'),
                        fieldLabel: Uni.I18n.translate('general.name', 'DES', 'Name'),
                        allowBlank: false,
                        enforceMaxLength: true,
                        maxLength: 255
                    },
                    {
                        title: Uni.I18n.translate('general.dataSources', 'DES', 'Data sources'),
                        ui: 'medium'
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.deviceGroup', 'DES', 'Device group'),
                        required: true,
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'combobox',
                                itemId: 'device-group-combo',
                                name: 'deviceGroup',
                                width: 235,
                                store: 'Dxp.store.DeviceGroups',
                                editable: false,
                                allowBlank: false,
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'id'
                            },
                            {
                                xtype: 'displayfield',
                                itemId: 'no-device',
                                hidden: true,
                                value: '<div style="color: #FF0000">' + Uni.I18n.translate('general.noDeviceGroup', 'DES', 'No device group defined yet.') + '</div>',
                                width: 235
                            },
                            {
                                xtype: 'button',
                                itemId: 'add-task-add-device-group',
                                margin: '0 0 0 20',
                                text: Uni.I18n.translate('general.addDeviceGroup', 'DES', 'Add device group'),
                                ui: 'link'
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        itemId: 'readingValuesTextFieldsContainer',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        items: [
                            {
                                xtype: 'container',
                                layout: {
                                    type: 'hbox'
                                },
                                items: [
                                    {
                                        xtype: 'textfield',
                                        fieldLabel: Uni.I18n.translate('general.readingTypes', 'DES', 'Reading type(s)'),
                                        labelAlign: 'right',
                                        itemId: 'readingType1',
                                        name: 'readingType1',
                                        msgTarget: 'under',
                                        labelWidth: 250,
                                        maskRe: /^($|\S.*$)/,
                                        required: true,
                                        allowBlank: false,
                                        validateOnChange: false,
                                        validateOnBlur: false,
                                        maxLength: 80,
                                        enforceMaxLength: true,
                                        width: 500,
                                        margin: '0 0 5 0'
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        margin: '5 0 0 0',
                        fieldLabel: '&nbsp',
                        labelAlign: 'right',
                        layout: 'hbox',
                        items: [
                            {
                                text: '+ ' + Uni.I18n.translate('general.addAnother', 'DES', 'Add another'),
                                xtype: 'button',
                                action: 'addReadingTypeAction',
                                itemId: 'addReadingTypeAction'
                            }
                        ]
                    },
                    {
                        title: Uni.I18n.translate('general.schedule', 'DES', 'Schedule'),
                        ui: 'medium'
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.recurrence', 'DES', 'Recurrence'),
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
                                        boxLabel: Uni.I18n.translate('general.none', 'DES', 'None'),
                                        inputValue: false,
                                        checked: true
                                    },
                                    {
                                        itemId: 'every',
                                        boxLabel: Uni.I18n.translate('general.every', 'DES', 'Every'),
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
                                        name: 'recurrence-number',
                                        allowDecimals: false,
                                        minValue: 1,
                                        width: 65,
                                        margin: '0 10 0 0',
                                        listeners: {
                                            focus: {
                                                fn: function () {
                                                    var radioButton = Ext.ComponentQuery.query('data-export-tasks-add #every')[0];
                                                    radioButton.setValue(true);
                                                }
                                            },
                                            blur: me.recurrenceNumberFieldValidation
                                        }
                                    },
                                    {
                                        itemId: 'recurrence-type',
                                        xtype: 'combobox',
                                        name: 'recurrence-type',
                                        store: 'Dxp.store.DaysWeeksMonths',
                                        queryMode: 'local',
                                        displayField: 'displayValue',
                                        valueField: 'name',
                                        editable: false,
                                        width: 100,
                                        listeners: {
                                            focus: {
                                                fn: function () {
                                                    var radioButton = Ext.ComponentQuery.query('data-export-tasks-add #every')[0];
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
                        fieldLabel: Uni.I18n.translate('general.startOn', 'DES', 'Start on'),
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'date-time',
                                itemId: 'start-on',
                                layout: 'hbox',
                                name: 'start-on',
                                dateConfig: {
                                    allowBlank: true
                                },
                                hoursConfig: {
                                    fieldLabel: Uni.I18n.translate('general.at', 'DES', 'at'),
                                    labelWidth: 10,
                                    margin: '0 0 0 10'
                                },
                                minutesConfig: {
                                    width: 55
                                }
                            }
                        ]
                    },
                    {
                        title: Uni.I18n.translate('general.dataOptions', 'DES', 'Data options'),
                        ui: 'medium'
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.exportPeriod', 'DES', 'Export period'),
                        required: true,
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'combobox',
                                itemId: 'export-period-combo',
                                name: 'exportPeriod',
                                width: 235,
                                queryMode: 'remote',
                                store: 'Dxp.store.ExportPeriods',
                                editable: false,
                                allowBlank: false,
                                emptyText: Uni.I18n.translate('general.emptyTextExportPeriod', 'DES', 'Select an extra period'),
                                displayField: 'name',
                                valueField: 'id'
                            },
                            {
                                xtype: 'button',
                                itemId: 'add-task-add-export-period',
                                margin: '0 0 0 20',
                                text: Uni.I18n.translate('general.addExportPeriod', 'DES', 'Add export period'),
                                ui: 'link'
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'schedule-preview',
                        fieldLabel: Uni.I18n.translate('general.previewNext', 'DES', 'Preview (next 5)'),
                        hidden: true,
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'add-schedule-grid',
                                width: 800
                            }
                        ]
                    },
                    {
                        title: Uni.I18n.translate('general.formatter', 'DES', 'Formatter'),
                        ui: 'medium'
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.formatter', 'DES', 'Formatter'),
                        layout: 'hbox',
                        required: true,
                        items: [
                            {
                                xtype: 'combobox',
                                itemId: 'file-formatter-combo',
                                name: 'dataProcessor',
                                width: 235,
                                queryMode: 'local',
                                store: 'Dxp.store.FileFormatters',
                                editable: false,
                                allowBlank: false,
                                displayField: 'displayName',
                                valueField: 'name'
                            },
                            {
                                xtype: 'button',
                                tooltip: 'The export file contains 5 columns:<br><div style="text-indent: 40px">Interval timestamp (yyyy-mm-dd hh:mm:ss)</div><div style="text-indent: 40px">MRID (text)</div><div style="text-indent: 40px">Reading type (text)</div><div style="text-indent: 40px">Value (number)</div><div style="text-indent: 40px">Validation result (text)</div>',
                                iconCls: 'icon-info-small',
                                ui: 'blank',
                                itemId: 'file-formatter-info',
                                shadow: false,
                                margin: '0 0 0 10',
                                width: 16
                            }
                        ]
                    },
                    {
                        xtype: 'tasks-property-form'
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
                                text: Uni.I18n.translate('general.add', 'DES', 'Add'),
                                ui: 'action',
                                action: 'add'
                            },
                            {
                                xtype: 'button',
                                itemId: 'cancel-link',
                                text: Uni.I18n.translate('general.cancel', 'DES', 'Cancel'),
                                ui: 'link',
                                href: '#/administration/dataexporttasks/'
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