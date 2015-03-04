Ext.define('Mdc.view.setup.datacollectionkpis.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.dataCollectionKpisPreview',
    requires: [
        'Mdc.view.setup.datacollectionkpis.ActionMenu'
    ],

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'dataCollectionKpisActionMenu',
                itemId: 'dataCollectionKpisPreviewActionMenu'
            }
        }
    ],

    items: [
        {
            xtype: 'form',
            itemId: 'dataCollectionKpisDetailsForm',
            layout: 'column',
            items: [
                {
                    xtype: 'container',
                    columnWidth: 0.5,
                    layout: 'form',
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 250
                    },
                    items: [
                        {
                            fieldLabel: Uni.I18n.translate('datacollectionkpis.deviceGroup', 'MDC', 'Device group'),
                            dataIndex: 'deviceGroup',
                            flex: 1,
                            renderer: function (value) {
                                if (value) {
                                    return value.name;
                                } else {
                                    return null;
                                }
                            }
                        },
                        {
                            name: 'frequency',
                            fieldLabel: Uni.I18n.translate('datacollectionkpis.frequency', 'MDC', 'Frequency'),
                            renderer: function (value) {
                                return Mdc.util.ScheduleToStringConverter.convert(value);
                            }
                        },
                        {
                            name: 'latestCalculationDate',
                            fieldLabel: Uni.I18n.translate('datacollectionkpis.lastcalculated', 'MDC', 'Last calculated'),
                            renderer: function (value) {
                                if (value) {
                                    return Uni.I18n.formatDate('deviceloadprofiles.dateFormat', new Date(value), 'MDC', 'M d, Y H:i');
                                } else {
                                    return Uni.I18n.translate('general.never', 'MDC', 'Never');
                                }
                            }
                        }
                    ]
                },
                {
                    xtype: 'container',
                    columnWidth: 0.5,
                    layout: 'form',
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 250
                    },
                    items: [
                        {
                            name: 'connectionTarget',
                            fieldLabel: Uni.I18n.translate('datacollectionkpis.connectiontarget', 'MDC', 'Connection target'),
                            renderer: function (value) {
                                if (!Ext.isEmpty(value)) {
                                    return value + '%';
                                } else {
                                    return 'No KPI';
                                }
                            }

                        },
                        {
                            name: 'communicationTarget',
                            fieldLabel: Uni.I18n.translate('datacollectionkpis.communicationtarget', 'MDC', 'Communication target'),
                            renderer: function (value) {
                                if (!Ext.isEmpty(value)) {
                                    return value + '%';
                                } else {
                                    return 'No KPI';
                                }
                            }
                        }

                    ]
                }

            ]
        }
    ]
});