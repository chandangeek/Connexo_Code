Ext.define('Dxp.view.tasks.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.dxp-tasks-preview-form',

    requires: [
        'Uni.property.form.Property',
        'Uni.form.field.Duration',
        'Uni.property.form.GroupedPropertyForm'
    ],

    myTooltip: Ext.create('Ext.tip.ToolTip', {
        renderTo: Ext.getBody()
    }),

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.name', 'DES', 'Name'),
                name: 'name',
                labelWidth: 250
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
                        fieldLabel: Uni.I18n.translate('general.trigger', 'DES', 'Trigger'),
                        name: 'trigger'
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
                                return Ext.String.htmlEncode(value.name);
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
                        fieldLabel: Uni.I18n.translate('general.readingTypes', 'DES', 'Reading types'),
                        name: 'readingTypes',
                        hidden: true,
                        itemId: 'data-selector-readingTypes-preview',
                        renderer: function (value) {
                            if (value) {
                                return Uni.I18n.translatePlural('general.nrOfReadingTypes', value.length, 'DES', 'No reading types', '1 reading type', '{0} reading types');
                            }
                        },
                        listeners: {
                            boxready: function (field) {
                                field.inputEl.on({
                                    mouseover: function (e) {
                                        var str = '';
                                        Ext.Array.each(field.value, function (item) {
                                            str += item.fullAliasName + '<br>';
                                        });
                                        var tip = field.up('form').myTooltip;
                                        tip.update(str);
                                        tip.showAt(e.getXY());
                                    },
                                    mouseout: function () {
                                        field.up('form').myTooltip.hide();
                                    }
                                });
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
                        fieldLabel: Uni.I18n.translate('general.exportUpdate', 'DES', 'Updated data'),
                        name: 'exportUpdate',
                        hidden: true,
                        itemId: 'updated-data',
                        renderer: function (value) {
                            return value==='true'?Uni.I18n.translate('general.exportWithinWindow', 'DES', 'Export within the update window'):
                                Uni.I18n.translate('general.noExportForUpdated', 'DES', 'Do not export');
                        }
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
                fieldLabel: Uni.I18n.translate('general.formatter', 'DES', 'Formatter'),
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 250
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('general.formatter', 'DES', 'Formatter'),
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