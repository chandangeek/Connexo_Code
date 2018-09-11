/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
        'Uni.grid.column.ReadingType',
        'Uni.grid.column.EventType',
        'Dxp.view.tasks.DestinationsGrid',
        'Uni.grid.column.RemoveAction',
        'Ldr.store.LogLevels'
    ],

    edit: false,
    returnLink: null,
    router: null,
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
                title: Uni.I18n.translate('general.addExportTask', 'DES', 'Add export task'),
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
                        fieldLabel: Uni.I18n.translate('general.name', 'DES', 'Name'),
                        allowBlank: false,
                        enforceMaxLength: true,
                        maxLength: 80,
                        listeners: {
                            afterrender: function(field) {
                                field.focus(false, 200);
                            }
                        }
                    },
                    {
                        xtype: 'combobox',
                        fieldLabel: Uni.I18n.translate('general.logLevel', 'DES', 'Log level'),
                        required: true,
                        name: 'logLevel',
                        width: 500,
                        itemId: 'dxp-data-export-tasks-add-loglevel',
                        allowBlank: false,
                        store: 'LogLevelsStore',
                        queryMode: 'local',
                        displayField: 'displayValue',
                        valueField: 'id'
                    },

                    {
                        title: Uni.I18n.translate('general.schedule', 'DES', 'Schedule'),
                        ui: 'medium'
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.recurrence', 'DES', 'Recurrence'),
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
                                        boxLabel: Uni.I18n.translate('general.none', 'DES', 'None'),
                                        inputValue: false,
                                        checked: true
                                    },
                                    {
                                        itemId: 'every',
                                        margin: '7 0 0 0',
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
                        itemId: 'dxp-data-selector-container',
                        required: true,
                        msgTarget: 'under',
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
                        msgTarget: 'under',
                        required: true,
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
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'id'
                            },
                            {
                                xtype: 'component',
                                html: Uni.I18n.translate('general.noDeviceGroup', 'DES', 'No device group defined yet'),
                                itemId: 'no-device',
                                hidden: true,
                                width: 235,
                                style: {
                                    'color': '#FF0000',
                                    'margin': '6px 10px 6px 0px'
                                }
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.usagePointGroup', 'DES', 'Usage point group'),
                        hidden: true,
                        itemId: 'usage-point-group-container',
                        msgTarget: 'under',
                        required: true,
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'combobox',
                                itemId: 'usage-point-group-combo',
                                name: 'readingTypeDataSelector.value.usagePointGroup',
                                width: 235,
                                store: 'Dxp.store.UsagePointGroups',
                                editable: false,
                                disabled: false,
                                emptyText: Uni.I18n.translate('addDataExportTask.usagePointGroupPrompt', 'DES', 'Select a usage point group...'),
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'id'
                            },
                            {
                                xtype: 'component',
                                html: Uni.I18n.translate('general.noUsagePointGroup', 'DES', 'No usage point group defined yet'),
                                itemId: 'no-usage-point',
                                hidden: true,
                                width: 235,
                                style: {
                                    'color': '#FF0000',
                                    'margin': '6px 10px 6px 0px'
                                }
                            }
                        ]
                    },

                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('estimationtasks.general.purpose', 'DES', 'Purpose'),
                        itemId: 'purpose-group-container',
                        hidden: true,
                        layout: {
                            type: 'hbox',
                        },
                        items: [
                            {
                                xtype: 'combobox',
                                width: 235,
                                itemId: 'purpose-combo',
                                editable: false,
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'id',
                                name: 'purpose',
                                store: 'Dxp.store.MetrologyPurposes',
                                emptyText: Uni.I18n.translate('estimationtasks.addValidationTask.purposePrompt', 'DES', 'Select a purpose...'),
                                disabled: false,
                                listeners: {
                                    change: function () {
                                        me.down('#reset-purpose-btn').enable();
                                    }
                                }
                            },
                            {
                                xtype: 'uni-default-button',
                                itemId: 'reset-purpose-btn',
                                disabled: true,
                                tooltip: Uni.I18n.translate('estimationtasks.addValidationTask.clear', 'DES', 'Clear'),
                                hidden: false,
                                handler:  function (btn) {
                                    me.down('#purpose-combo').clearValue();
                                    btn.disable();
                                }
                            }
                        ]
                    },

                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.readingTypes', 'DES', 'Reading types'),
                        hidden: true,
                        itemId: 'readingTypesFieldContainer',
                        required: true,
                        layout: 'hbox',
                        msgTarget: 'under',
                        items: [
                            {
                                xtype: 'component',
                                html: Uni.I18n.translate('dataExport.noReadingTypes','DES','No reading types have been added'),
                                itemId: 'noReadingTypesLabel',
                                style: {
                                    'font': 'italic 13px/17px Lato',
                                    'color': '#686868',
                                    'margin-top': '6px',
                                    'margin-right': '10px'
                                }
                            },
                            {
                                xtype: 'gridpanel',
                                itemId: 'readingTypesGridPanel',
                                store: 'Dxp.store.ReadingTypesForTask',
                                hideHeaders: true,
                                padding: 0,
                                hidden: true,
                                scroll: 'vertical',
                                viewConfig: {
                                    disableSelection: true,
                                    enableTextSelection: true
                                },
                                columns: [
                                    {
                                        xtype: 'reading-type-column',
                                        dataIndex: 'readingType',
                                        flex: 1
                                    },
                                    {
                                        xtype: 'uni-actioncolumn-remove',
                                        align: 'right',
                                        handler: function (grid, rowIndex) {
                                            grid.getStore().removeAt(rowIndex);
                                            if (grid.getStore().count() === 0) {
                                                me.updateReadingTypesGrid();
                                            }
                                        }
                                    }
                                ],
                                width: 800,
                                height: 220
                            },
                            {
                                xtype: 'button',
                                itemId: 'addReadingTypeButton',
                                text: Uni.I18n.translate('general.addReadngTypes', 'DES', 'Add reading types'),
                                margin: '0 0 0 10'
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.eventTypes', 'DES', 'Event types'),
                        hidden: true,
                        itemId: 'eventTypesFieldContainer',
                        required: true,
                        layout: 'hbox',
                        msgTarget: 'under',
                        items: [
                            {
                                xtype: 'component',
                                html: Uni.I18n.translate('dataExport.noEventTypes','DES','No event types have been added'),
                                itemId: 'noEventTypesLabel',
                                style: {
                                    'font': 'italic 13px/17px Lato',
                                    'color': '#686868',
                                    'margin-top': '6px',
                                    'margin-right': '10px'
                                }
                            },
                            {
                                xtype: 'gridpanel',
                                itemId: 'eventTypesGridPanel',
                                store: 'Dxp.store.EventTypesForTask',
                                hideHeaders: true,
                                padding: 0,
                                scroll: 'vertical',
                                viewConfig: {
                                    disableSelection: true,
                                    enableTextSelection: true
                                },
                                hidden: true,
                                columns: [
                                    {
                                        xtype: 'event-type-column',
                                        dataIndex: 'eventFilterCode',
                                        flex: 1
                                    },
                                    {
                                        xtype: 'uni-actioncolumn-remove',
                                        align: 'right',
                                        handler: function (grid, rowIndex) {
                                            grid.getStore().removeAt(rowIndex);
                                            if (grid.getStore().count() === 0) {
                                                me.updateEventTypesGrid();
                                            }
                                        }
                                    }
                                ],
                                width: 800,
                                height: 220
                            },
                            {
                                xtype: 'button',
                                itemId: 'addEventTypeButton',
                                text: Uni.I18n.translate('general.addEventTypes', 'DES', 'Add event types'),
                                margin: '0 0 0 10'
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'export-periods-container',
                        fieldLabel: Uni.I18n.translate('general.exportWindow', 'DES', 'Export window'),
                        msgTarget: 'under',
                        hidden: true,
                        required: true,
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
                                emptyText: Uni.I18n.translate('addDataExportTask.exportWindowPrompt', 'DES', 'Select an export window...'),
                                displayField: 'name',
                                valueField: 'id'
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'continuous-data-container',
                        fieldLabel: ' ',
                        hidden: true,
                        required: true,
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'displayfield',
                                value: Uni.I18n.translate('general.startingFrom', 'DES', 'Starting from')
                            },
                            {
                                itemId: 'continuous-data-radiogroup',
                                xtype: 'radiogroup',
                                name: 'exportContinuousData',
                                columns: 1,
                                vertical: true,
                                // width: 100,
                                defaults: {
                                    name: 'exportContinuousData'
                                },
                                items: [
                                    {
                                     //   itemId: 'noExportForUpdated',
                                        boxLabel: Uni.I18n.translate('general.startOfExportWindow', 'DES', 'start of export window'),
                                        inputValue: false,
                                        checked: true
                                    },
                                    {
                                      //  itemId: 'exportWithinWindow',
                                        boxLabel: Uni.I18n.translate('general.continuousData', 'DES', 'last exported data (continuous data)'),
                                        inputValue: true
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'updated-data-container',
                        fieldLabel: Uni.I18n.translate('general.updatedData', 'DES', 'Updated data'),
                        hidden: true,
                        required: true,
                        width: 1200,
                        layout: 'vbox',
                        items: [
                            {
                                xtype: 'fieldcontainer',
                                layout: 'hbox',
                                items: [
                                    {
                                        itemId: 'updated-data-trigger',
                                        xtype: 'radiogroup',
                                        name: 'updatedDataTrigger',
                                        columns: 1,
                                        vertical: true,
                                        // width: 100,
                                        defaults: {
                                            name: 'exportUpdate'
                                        },
                                        items: [
                                            {
                                                itemId: 'noExportForUpdated',
                                                boxLabel: Uni.I18n.translate('general.noExportForUpdated', 'DES', "Don't export"),
                                                inputValue: false,
                                                checked: true
                                            },
                                            {
                                                itemId: 'exportWithinWindow',
                                                boxLabel: Uni.I18n.translate('general.exportWithinWindow', 'DES', 'Export within the update window'),
                                                inputValue: true
                                            }
                                        ]
                                    },
                                    {
                                        itemId: 'update-window',
                                        xtype: 'combobox',
                                        name: 'updateWindow',
                                        store: 'Dxp.store.UpdateWindows',

                                        queryMode: 'local',
                                        displayField: 'name',
                                        valueField: 'id',
                                        margin: '30 0 10 10',
                                        width: 200,
                                        editable: false,
                                        emptyText: Uni.I18n.translate('general.updateWindow', 'DES', 'Select an update window...')
                                        //    width: 100,
                                        //    listeners: {
                                        //        focus: {
                                        //            fn: function () {
                                        //                var radioButton = Ext.ComponentQuery.query('data-export-tasks-add #every')[0];
                                        //                radioButton.setValue(true);
                                        //            }
                                        //        }
                                        //    }
                                    },
                                    {
                                        xtype: 'displayfield',
                                        htmlEncode: false,
                                        margin: '30 0 10 10',
                                        value: '<a href="../../apps/admin/index.html#/administration/relativeperiods">' + Uni.I18n.translate('general.addUpdateWindow', 'DES', 'Add update window') + '</a>'
                                    }
                                ]
                            },
                            {
                                xtype: 'fieldcontainer',
                                layout: 'hbox',
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        value: Uni.I18n.translate('general.export', 'DES', 'Export'),
                                    },
                                    {
                                        itemId: 'export-updated',
                                        xtype: 'radiogroup',
                                        name: 'exportUpdated',
                                        columns: 1,
                                        vertical: true,
                                       // width: 400,
                                        defaults: {
                                            name: 'updatedDataAndOrAdjacentData'
                                        },
                                        items: [
                                            {
                                                itemId: 'updateValuesOnly',
                                                boxLabel: Uni.I18n.translate('general.updateValuesOnly', 'DES', 'updated values only'),
                                                inputValue: false,
                                                checked: true
                                            },
                                            {
                                                itemId: 'exportValuesAndAdjacent',
                                                boxLabel: Uni.I18n.translate('general.exportValuesAndAdjacent', 'DES', 'updated values and adjacent data within timeframe'),
                                                inputValue: true
                                            }
                                        ]
                                    },
                                    {
                                        itemId: 'timeFrame',
                                        xtype: 'combobox',
                                        name: 'updateTimeFrame',
                                        store: 'Dxp.store.UpdateTimeframes',

                                        queryMode: 'local',
                                        displayField: 'name',
                                        valueField: 'id',
                                        margin: '30 0 10 10',
                                        width: 200,
                                        editable: false,
                                        emptyText: Uni.I18n.translate('general.updateWinding', 'DES', 'Select a time frame...')
                                    },
                                    {
                                        xtype: 'displayfield',
                                        htmlEncode: false,
                                        margin: '30 0 10 10',
                                        value: '<a href="../../apps/admin/index.html#/administration/relativeperiods">' + Uni.I18n.translate('general.addUpdateTimeframe', 'DES', 'Add update timeframe') + '</a>'
                                    }
                                ]
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
                        fieldLabel: Uni.I18n.translate('general.missingData', 'DES', 'Missing data'),
                        xtype: 'radiogroup',
                        name: 'exportComplete2',
                        hidden: true,
                        vertical: true,
                        required: true,
                        columns: 1,
                        itemId: 'data-selector-export-complete',
                        items: [
                            {
                                xtype: 'radiofield',
                                boxLabel: Uni.I18n.translate('general.skipMissingData', 'DES', 'Skip intervals with missing data (data with gaps)'),
                                name: 'exportComplete',
                                checked: true,
                                inputValue: 'EXCLUDE_INTERVAL'
                            },
                            {
                                xtype: 'radiofield',
                                boxLabel: Uni.I18n.translate('general.skipExportWindowMissingData', 'DES', 'Skip export window for reading types with missing data (complete data)'),
                                name: 'exportComplete',
                                inputValue: 'EXCLUDE_ITEM'
                            },
                            {
                                xtype: 'radiofield',
                                itemId: 'skip-window-up-complete-data-radiofield',
                                boxLabel: Uni.I18n.translate('general.usagePoints.skipExportWindowMissingDataMsg', 'DES', 'Skip export window for usage points with missing data (complete data)'),
                                name: 'exportComplete',
                                inputValue: 'EXCLUDE_OBJECT'
                            }
                        ]
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.validatedData', 'DES', 'Validated data'),
                        xtype: 'radiogroup',
                        name: 'validatedData',
                        hidden: true,
                        vertical: true,
                        required: true,
                        columns: 1,
                        itemId: 'data-selector-validated-data',
                        items: [
                            {
                                xtype: 'radiofield',
                                boxLabel: Uni.I18n.translate('general.exportAll', 'DES', 'Export all data (including suspect/not validated data)'),
                                name: 'validatedDataOption',
                                checked: true,
                                inputValue: 'INCLUDE_ALL'
                            },
                            {
                                xtype: 'radiofield',
                                boxLabel: Uni.I18n.translate('general.skipSuspectOrNotValidated', 'DES', 'Skip intervals with suspect/not validated data'),
                                name: 'validatedDataOption',
                                inputValue: 'EXCLUDE_INTERVAL'
                            },
                            {
                                xtype: 'radiofield',
                                boxLabel: Uni.I18n.translate('general.skipExportWindow', 'DES', 'Skip export window for reading types with suspect/not validated data'),
                                name: 'validatedDataOption',
                                inputValue: 'EXCLUDE_ITEM'
                            },
                            {
                                xtype: 'radiofield',
                                itemId: 'skip-window-up-validated-data-radiofield',
                                boxLabel: Uni.I18n.translate('general.usagePoints.skipExportWindowMsg', 'DES', 'Skip export window for usage points with suspect/not validated data'),
                                name: 'validatedDataOption',
                                inputValue: 'EXCLUDE_OBJECT'
                            }
                        ]
                    },
                    {
                        title: Uni.I18n.translate('general.outputFormat', 'DES', 'Output format'),
                        itemId: 'file-formatter-title',
                        hidden: true,
                        ui: 'medium'
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.dataFormatter', 'DES', 'Data formatter'),
                        layout: 'hbox',
                        itemId: 'formatter-container',
                        hidden: true,
                        required: true,
                        msgTarget: 'under',
                        items: [
                            {
                                xtype: 'combobox',
                                itemId: 'file-formatter-combo',
                                name: 'dataProcessor',
                                width: 235,
                                queryMode: 'local',
                                hidden: true,
                                store: 'Dxp.store.FileFormatters',
                                editable: false,
                                emptyText: Uni.I18n.translate('addDataExportTask.dataFormatterPrompt', 'DES', 'Select a data formatter...'),
                                displayField: 'displayName',
                                valueField: 'name'
                            },
                            {
                                xtype: 'button',
                                tooltip:'',
                                text: '<span class="icon-info" style="cursor:default; display:inline-block; color:#A9A9A9; font-size:16px;"></span>',
                                disabled: true, // to avoid a hand cursor
                                ui: 'blank',
                                itemId: 'file-formatter-info',
                                shadow: false,
                                margin: '5 0 0 10',
                                width: 16,
                                hidden: true
                            }
                        ]
                    },
                    {
                        xtype: 'grouped-property-form'
                    },

                    {
                        title: Uni.I18n.translate('general.destinations', 'DES', 'Destinations'),
                        ui: 'medium'
                    },

                    {
                        xtype: 'fieldcontainer',
                        itemId: 'destinations',
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.destinations', 'DES', 'Destinations'),
                        layout: 'hbox',
                        msgTarget: 'under',
                        items: [

                            {
                                xtype: 'component',
                                html: Uni.I18n.translate('dataExport.noDestinations','DES','No destinations have been added'),
                                itemId: 'noDestinationsLabel',
                                style: {
                                    'font': 'italic 13px/17px Lato',
                                    'color': '#686868',
                                    'margin-top': '6px',
                                    'margin-right': '10px'
                                }
                            },
                            {
                                xtype: 'dxp-tasks-destinations-grid',
                                itemId: 'task-destinations-grid',
                                hidden: true,
                                width: 800
                            },
                            {
                                xtype: 'button',
                                itemId: 'add-destination-button',
                                text: Uni.I18n.translate('dataExport.addDestination', 'DES', 'Add destination'),
                                margin: '0 0 0 10'
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
    },

    updateReadingTypesGrid: function() {
        var me = this,
            readingTypesGrid = me.down('#readingTypesGridPanel'),
            emptyReadingTypesLabel = me.down('#noReadingTypesLabel');
        if (readingTypesGrid.getStore().count() === 0) {
            emptyReadingTypesLabel.show();
            readingTypesGrid.hide();
        } else {
            emptyReadingTypesLabel.hide();
            readingTypesGrid.show();
        }
    },

    updateEventTypesGrid: function() {
        var me = this,
            eventTypesGrid = me.down('#eventTypesGridPanel'),
            emptyEventTypesLabel = me.down('#noEventTypesLabel');
        if (eventTypesGrid.getStore().count() === 0) {
            emptyEventTypesLabel.show();
            eventTypesGrid.hide();
        } else {
            emptyEventTypesLabel.hide();
            eventTypesGrid.show();
        }
    }

});