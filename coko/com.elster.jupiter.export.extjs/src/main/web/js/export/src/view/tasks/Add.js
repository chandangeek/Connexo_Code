Ext.define('Dxp.view.tasks.Add', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-export-tasks-add',
    requires: [
        'Uni.form.field.DateTime',
        'Tme.privileges.Period',
        'Dxp.view.tasks.AddScheduleGrid',
        'Uni.property.form.Property',
        'Uni.property.form.GroupedPropertyForm',
        'Uni.util.FormErrorMessage',
        'Uni.grid.column.ReadingType'
    ],

    edit: false,
    returnLink: null,
    setEdit: function (edit) {
        if (edit) {
            this.edit = edit;
            this.down('#add-export-task-button').setText(Uni.I18n.translate('general.save', 'DES', 'Save'));
            this.down('#add-export-task-button').action = 'editTask';
        } else {
            this.edit = edit;
            this.down('#add-export-task-button').setText(Uni.I18n.translate('general.add', 'DES', 'Add'));
            this.down('#add-export-task-button').action = 'addTask';
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
                        //maskRe: /[^:\\/*?"<>|]/,
                        fieldLabel: Uni.I18n.translate('general.name', 'DES', 'Name'),
                        allowBlank: false,
                        enforceMaxLength: true,
                        maxLength: 80
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
                                        value: 1,
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
                                    allowBlank: true,
                                    value: new Date(),
                                    editable: false,
                                    format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                                },
                                hoursConfig: {
                                    fieldLabel: Uni.I18n.translate('general.at', 'DES', 'at'),
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
                        title: Uni.I18n.translate('general.dataSelection', 'DES', 'Data selection'),
                        ui: 'medium'
                    },

                    {
                         xtype: 'fieldcontainer',
                         fieldLabel: Uni.I18n.translate('general.dataSelector', 'DES', 'Data selector'),
                         //required: true,
                         layout: 'hbox',
                         items: [
                             {
                                 xtype: 'combobox',
                                 itemId: 'data-selector-combo',
                                 name: 'readingTypeDataSelector.value.dataSelector',
                                 width: 235,
                                 queryMode: 'local',
                                 store: 'Dxp.store.DataSelectors',
                                 editable: false,
                                 disabled: false,
                                 allowBlank: false,
                                 emptyText: Uni.I18n.translate('addDataExportTask.dataSelectorPrompt', 'DES', 'Select a data selector...'),
                                 displayField: 'displayName',
                                 valueField: 'name'
                             }
                        ]
                     },

                     {
                         xtype: 'property-form',
                         itemId: 'data-selector-properties',
                         isEdit: true,
                         hidden: true,
                         defaults: {
                             labelWidth: 250
                        }

                     },

                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.deviceGroup', 'DES', 'Device group'),
                        hidden: true,
                        itemId: 'device-group-container',
                        //required: true,
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'combobox',
                                itemId: 'device-group-combo',
                                name: 'readingTypeDataSelector.value.endDeviceGroup',
                                width: 235,
                                store: 'Dxp.store.DeviceGroups',
                                editable: false,
                                disabled: false,
                                emptyText: Uni.I18n.translate('addDataExportTask.deviceGroupPrompt', 'DES', 'Select a device group...'),
                                //allowBlank: false,
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'id'
                            },
                            {
                                xtype: 'displayfield',
                                itemId: 'no-device',
                                htmlEncode: false,
                                hidden: true,
                                value: '<div style="color: #FF0000">' + Uni.I18n.translate('general.noDeviceGroup', 'DES', 'No device group defined yet.') + '</div>',
                                width: 235
                            }
                        ]
                    },

                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.readingTypes', 'DES', 'Reading types'),
                        hidden: true,
                        itemId: 'readingTypesFieldContainer',
                        //required: true,
                        msgTarget: 'under',
                        width: 1200,
                        items: [
                            {
                                xtype: 'panel',
                                width: 800,
                                items: [
                                    {
                                        xtype: 'gridpanel',
                                        itemId: 'readingTypesGridPanel',
                                        store: 'Dxp.store.ReadingTypesForTask',
                                        hideHeaders: true,
                                        padding: 0,
                                        columns: [
                                            {
                                                xtype: 'reading-type-column',
                                                dataIndex: 'readingType',
                                                flex: 1
                                            },
                                            {
                                                xtype: 'actioncolumn',
                                                align: 'right',
                                                items: [
                                                    {
                                                        iconCls: 'uni-icon-delete',
                                                        handler: function (grid, rowIndex) {
                                                            grid.getStore().removeAt(rowIndex);
                                                        }
                                                    }
                                                ]
                                            }
                                        ],
                                        height: 220
                                    }
                                ],
                                rbar: [
                                    {
                                        xtype: 'container',
                                        items: [
                                            {
                                                xtype: 'button',
                                                itemId: 'addReadingTypeButton',
                                                text: Uni.I18n.translate('general.addReadngTypes', 'CFG', 'Add reading types'),
                                                margin: '0 0 0 10'
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    },

                    {
                        xtype: 'fieldcontainer',
                        itemId: 'export-periods-container',
                        fieldLabel: Uni.I18n.translate('general.exportPeriod', 'DES', 'Export period'),
                        hidden: true,
                        //required: true,
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'combobox',
                                itemId: 'export-period-combo',
                                name: 'readingTypeDataSelector.value.exportPeriod',
                                width: 235,
                                queryMode: 'local',
                                store: 'Dxp.store.ExportPeriods',
                                editable: false,
                                disabled: false,
                                //allowBlank: false,
                                emptyText: Uni.I18n.translate('addDataExportTask.exportPeriodPrompt', 'DES', 'Select an export period...'),
                                displayField: 'name',
                                valueField: 'id'
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
                                margin: '5 0 0 10',
                                width: 16
                            }
                        ]
                    },
                    {
                        xtype: 'grouped-property-form'
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'add-export-task-button',
                                text: Uni.I18n.translate('general.add', 'DES', 'Add'),
                                ui: 'action'
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
        me.setEdit(me.edit);
    },
    recurrenceNumberFieldValidation: function (field) {
        var value = field.getValue();

        if (Ext.isEmpty(value) || value < field.minValue) {
            field.setValue(field.minValue);
        }
    }
});