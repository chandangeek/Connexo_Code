Ext.define('Dxp.view.tasks.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.dxp-tasks-preview-form',

    requires: [
        'Uni.property.form.Property',
        'Uni.form.field.Duration',
        'Uni.property.form.GroupedPropertyForm',
        'Dxp.view.tasks.DestinationsField',
        'Uni.store.TaskLogLevels'
    ],

    myTooltip: Ext.create('Ext.tip.ToolTip', {
        renderTo: Ext.getBody()
    }),

    initComponent: function () {
        var me = this,
            logLevelsStore = Ext.getStore('Uni.store.TaskLogLevels');

        me.items = [
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.name', 'DES', 'Name'),
                name: 'name',
                labelWidth: 250
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.logLevel', 'DES', 'Log level'),
                name: 'logLevel',
                labelWidth: 250,
                renderer: function(value) {
                    if (Ext.isEmpty(value)) {
                        return '-';
                    }
                    var storeIndex = logLevelsStore.findExact('id', value);
                    return storeIndex === -1 ? '-' : logLevelsStore.getAt(storeIndex).get('displayValue');
                }
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('general.schedule', 'DES', 'Schedule'),
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 250
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('general.recurrence', 'DES', 'Recurrence'),
                        name: 'recurrence'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.lastRun', 'DES', 'Last run'),
                        name: 'lastRun',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '-';
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.status', 'DES', 'Status'),
                        name: 'status'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.reason', 'DES', 'Reason'),
                        itemId: 'reason-field',
                        name: 'reason',
                        hidden: true
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.startedOn', 'DES', 'Started on'),
                        name: 'startedOn'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.finishedOn', 'DES', 'Finished on'),
                        name: 'finishedOn'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.startedOn', 'DES', 'Started on'),
                        name: 'startedOn_formatted',
                        hidden: true
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.finishedOn', 'DES', 'Finished on'),
                        name: 'finishedOn_formatted',
                        hidden: true
                    },
                    {
                        xtype: 'uni-form-field-duration',
                        name: 'duration'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.nextRun', 'DES', 'Next run'),
                        name: 'nextRun_formatted'
                    }
                ]
            },

            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('general.dataSelection', 'DES', 'Data selection'),
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 250
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('general.dataSelector', 'DES', 'Data selector'),
                        name: 'dataSelector',
                        renderer: function (value) {
                            if (value) {
                                return Ext.String.htmlEncode(value.displayName);
                            }
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.deviceGroup', 'DES', 'Device group'),
                        name: 'deviceGroup',
                        hidden: true,
                        itemId: 'data-selector-deviceGroup-preview',
                        renderer: function (value) {
                            if (value) {
                                return Ext.String.htmlEncode(value);
                            }
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.usagePointGroup', 'DES', 'Usage point group'),
                        name: 'usagePointGroup',
                        hidden: true,
                        itemId: 'data-selector-usage-point-group-preview',
                        renderer: function (value) {
                            if (value) {
                                return Ext.String.htmlEncode(value);
                            }
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.readingTypes', 'DES', 'Reading types'),
                        name: 'readingTypes',
                        hidden: true,
                        itemId: 'data-selector-readingTypes-preview',
                        renderer: function (value) {
                            if (value) {
                                var tooltipText = '';
                                Ext.Array.each(value, function (item) {
                                    tooltipText += item.fullAliasName + '<br>';
                                });
                                return Uni.I18n.translatePlural('general.nrOfReadingTypes', value.length, 'DES', 'No reading types', '1 reading type', '{0} reading types')
                                    + '<span class="icon-info" style="cursor:default; display:inline-block; color:#A9A9A9; font-size:16px; margin-left:7px;" data-qtip="' + tooltipText + '"></span>';
                            }
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.eventTypes', 'DES', 'Event types'),
                        name: 'eventTypes',
                        hidden: true,
                        itemId: 'data-selector-eventTypes-preview',
                        renderer: function (value) {
                            if (value) {
                                var tooltipText = '';
                                Ext.Array.each(value, function (item) {
                                    tooltipText += item.eventFilterCode + '<br>';
                                });
                                return Uni.I18n.translatePlural('general.nrOfEventTypes', value.length, 'DES', 'No event types', '1 event type', '{0} event types')
                                    + '<span class="uni-icon-info-small" style="display: inline-block; width: 16px; height: 16px; margin: 0 0 0 10px" data-qtip="' + tooltipText + '"></span>';
                            }
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.exportWindow', 'DES', 'Export window'),
                        name: 'exportPeriod',
                        hidden: true,
                        itemId: 'data-selector-exportPeriod-preview',
                        renderer: function (value) {
                            if (value) {
                                return Ext.String.htmlEncode(value);
                            }
                        }
                    },
                    {
                        fieldLabel: ' ',
                        name: 'exportContinuousData',
                        hidden: true,
                        itemId: 'continuousData-preview',
                        renderer: function (value) {
                            var option = value==='true'?Uni.I18n.translate('general.continuousData', 'DES', 'last exported data (continuous data)'):
                                Uni.I18n.translate('general.startOfExportWindow', 'DES', 'start of export window');
                            return Uni.I18n.translate('general.startingFrom', 'DES', 'Starting from') + ' ' + option;
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.exportUpdate', 'DES', 'Updated data'),
                        name: 'exportUpdateForPreview',
                        hidden: true,
                        itemId: 'updated-data'
                    },
                    {
                        fieldLabel: ' ',
                        name: 'updatedValuesForPreview',
                        hidden: true,
                        itemId: 'updated-values'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.missingData', 'DES', 'Missing data'),
                        name: 'exportComplete',
                        hidden: true,
                        itemId: 'data-selector-export-complete',
                        renderer: function (value) {
                                return value==='true'?Uni.I18n.translate('general.skipExportWindowMissingData', 'DES', 'Skip export window for reading types with missing data (complete data)'):
                                    Uni.I18n.translate('general.skipMissingData', 'DES', 'Skip intervals with missing data (data with gaps)');

                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.validatedData', 'DES', 'Validated data'),
                        name: 'validatedDataOption',
                        hidden: true,
                        itemId: 'data-selector-validated-data',
                        renderer: function (value) {
                            if (value) {
                                if(value==='INCLUDE_ALL'){
                                    return Uni.I18n.translate('general.exportAll', 'DES', 'Export all data (including suspect/not validated data)');
                                } else if (value === 'EXCLUDE_INTERVAL'){
                                    return Uni.I18n.translate('general.skipSuspectOrNotValidated', 'DES', 'Skip intervals with suspect/not validated data');
                                } else if (value === 'EXCLUDE_ITEM') {
                                    return Uni.I18n.translate('general.skipExportWindow', 'DES', 'Skip export window for reading types with suspect/not validated data');
                                }
                                return '';
                            }
                        }
                    }
                ]
            },
            {
                xtype: 'property-form',
                itemId: 'data-selector-properties-preview',
                isEdit: false,
                frame: false,
                defaults: {
                    xtype: 'container',
                    resetButtonHidden: true,
                    labelWidth: 250
                }

            },

            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('general.outputFormat', 'DES', 'Output format'),
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 250
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('general.dataFormatter', 'DES', 'Data formatter'),
                        name: 'dataProcessor',
                        renderer: function (value) {
                            if (value) {
                                return Ext.String.htmlEncode(value.displayName);
                            }
                        }
                    }
                ]
            },
            {
                xtype: 'grouped-property-form',
                isEdit: false,
                itemId: 'task-properties-preview',
                frame: false,
                defaults: {
                    xtype: 'container',
                    resetButtonHidden: true,
                    labelWidth: 250
                }
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('general.Destinations', 'DES', 'Destinations'),
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 250
                },
                items: [
                    {
                        xtype: 'destinationsField',
                        fieldLabel: Uni.I18n.translate('general.Destinations', 'DES', 'Destinations'),
                        name: 'destinations'
                    }
                ]
            }
        ];
        me.callParent();
    },

    fillReadings: function (record) {
        this.down('#readingTypesArea').removeAll();
        for (var i = 0; i < record.get('readingTypes').length; i++) {
            var readingType = record.get('readingTypes')[i];

            this.down('#readingTypesArea').add(
                {
                    xtype: 'reading-type-displayfield',
                    fieldLabel: undefined,
                    value: readingType,
                    margin: '0 0 -10 0'
                }
            );
        }
    }
});