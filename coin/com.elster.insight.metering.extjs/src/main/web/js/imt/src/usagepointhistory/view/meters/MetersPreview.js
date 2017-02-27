/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointhistory.view.meters.MetersPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.usage-point-history-meters-preview',
    router: null,
    title: ' ',
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'form',
                itemId: 'usage-point-history-meters-preview-form',
                layout: 'column',
                defaults: {
                    xtype: 'container',
                    columnWidth: 0.5
                },
                items: [
                    {
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 200
                        },
                        items: [
                            {
                                itemId: 'current-field',
                                name: 'current',
                                fieldLabel: Uni.I18n.translate('general.current', 'IMT', 'Current'),
                                renderer: function (value) {
                                    return !!value ? Uni.I18n.translate('general.yes', 'IMT', 'Yes') : Uni.I18n.translate('general.no', 'IMT', 'No');
                                }
                            },
                            {
                                itemId: 'period-field',
                                fieldLabel: Uni.I18n.translate('general.period', 'IMT', 'Period'),
                                name: 'period',
                                htmlEncode: false
                            }
                        ]
                    },
                    {
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 200
                        },
                        items: [
                            {
                                itemId: 'meter-role-field',
                                name: 'meterRole',
                                fieldLabel: Uni.I18n.translate('general.meterRole', 'IMT', 'Meter role')
                            },
                            {
                                itemId: 'meter-field',
                                fieldLabel: Uni.I18n.translate('general.meter', 'IMT', 'Meter'),
                                name: 'meter',
                                renderer: function () {
                                    var record = me.down('form').getRecord(),
                                        url, meter;

                                    if (record) {
                                        url = record.get('url');
                                        meter = Ext.String.htmlEncode(record.get('meter'));
                                        return url ? '<a href="' + url + '" target="_blank">' + meter + '</a>' : meter;
                                    }
                                }
                            },
                            {
                                xtype: 'fieldcontainer',
                                itemId: 'processes-field-container',
                                fieldLabel: Uni.I18n.translate('general.ongoingProcesses', 'IMT', 'Ongoing processes')
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    loadRecord: function (record) {
        var me = this,
            processesContainer = me.down('#processes-field-container'),
            processes = me.prepareOngoingProcesses(record.get('ongoingProcesses'));

        Ext.suspendLayouts();
        me.setTitle(record.get('period'));
        me.down('form').loadRecord(record);
        processesContainer.removeAll();
        processesContainer.add(processes);
        Ext.resumeLayouts(true);
    },

    prepareOngoingProcesses: function (processes) {
        var me = this;
        if (!Ext.isEmpty(processes)) {
            return _.map(processes, function (process) {
                var value = Dbp.privileges.DeviceProcesses.allBpmProcesses()
                    ? Ext.String.format('<a href="{0}">{1}</a> - {2}', me.router.getRoute('usagepoints/view/processes').buildUrl(), process.id, process.name)
                    : process.id + ' - ' + process.name;
                return {
                    xtype: 'displayfield',
                    fieldLabel: '',
                    itemId: process.name,
                    htmlEncode: false,
                    value: value
                };
            });
        } else {
            return {
                xtype: 'displayfield',
                fieldLabel: '',
                value: '-'
            };
        }
    }
});
