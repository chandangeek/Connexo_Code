/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.taskmanagement.DetailsDataCollectionKpi', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-collection-kpi-details',
    requires: [],
    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'container',
            layout: 'hbox',
            items: [
                {
                    ui: 'large',
                    itemId: 'data-collection-kpi-details-panel',
                    title: Uni.I18n.translate('general.details', 'MDC', 'Details'),
                    flex: 1,
                    items: {
                        xtype: 'form',
                        itemId: 'data-collection-kpi-details-form',
                        margin: '0 0 0 100',
                        defaults: {
                            labelWidth: 250,
                        },
                        items: [
                            {
                                fieldLabel: Uni.I18n.translate('general.followedBy', 'MDC', 'Followed by'),
                                xtype: 'displayfield',
                                htmlEncode: false,
                                itemId: 'followedBy-field-container'
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.precededBy', 'MDC', 'Preceded by'),
                                xtype: 'displayfield',
                                htmlEncode: false,
                                itemId: 'precededBy-field-container'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('general.deviceGroup', 'MDC', 'Device group'),
                                itemId: 'data-collection-kpi-device-group',
                                name: 'deviceGroup'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('datacollectionkpis.calculationFrequency', 'MDC', 'Calculation frequency'),
                                itemId: 'data-collection-kpi-frequency',
                                name: 'frequency'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('datacollectionkpis.kpiTarget', 'MDC', 'KPI target'),
                                itemId: 'kpi-target'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('datacollectionkpis.suspended', 'MDC', 'Suspended'),
                                itemId: 'data-collection-kpi-suspend',
                                name: 'suspendUntilTime',
                                renderer: function(value){
                                    return value  ? Uni.I18n.translate('general.suspended.yes','APR','Yes <br/>has been suspended until next run') : Uni.I18n.translate('general.suspended.no','APR','No')
                                }
                            }

                        ]
                    }
                },
                {
                    xtype: 'uni-button-action',
                    margin: '20 0 0 0',
                    privileges: function () {
                        return me.canAdministrate;
                    },
                    menu: me.actionMenu
                }
            ]
        };
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'uni-view-menu-side',
                        itemId: 'data-collection-kpi-details-side-menu',
                        title: Uni.I18n.translate('general.datadataCollectionKPI', 'MDC', 'Data collection KPI'),
                        objectType: Uni.I18n.translate('general.datadataCollectionKPI', 'MDC', 'Data collection KPI'),
                        menuItems: [
                            {
                                text: Uni.I18n.translate('general.details', 'MDC', 'Details'),
                                itemId: 'data-collection-kpi-details-overview-link'
                            }
                        ]
                    }
                ]
            }
        ];

        this.callParent(arguments);
    },

    setRecurrentTasks: function (itemId, recurrentTasks) {
        var me = this,
            recurrentTaskList = [];

        Ext.isArray(recurrentTasks) && Ext.Array.each(recurrentTasks, function (recurrentTask) {
            recurrentTaskList.push('- ' + Ext.htmlEncode(recurrentTask.name));
        });
        me.down(itemId).setValue((recurrentTaskList.length == 0) ? recurrentTaskList = '-' : recurrentTaskList.join('<br/>'));
        return;
    }
});
