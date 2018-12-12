/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.taskmanagement.DetailsDataQualityKpi', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-quality-kpi-details',
    requires: [],
    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'container',
            layout: 'hbox',
            items: [
                {
                    ui: 'large',
                    itemId: 'data-quality-kpi-details-panel',
                    title: Uni.I18n.translate('general.details', 'CFG', 'Details'),
                    flex: 1,
                    items: {
                        xtype: 'form',
                        itemId: 'data-quality-kpi-details-form',
                        margin: '0 0 0 100',
                        defaults: {
                            labelWidth: 250,
                        },
                        items: [
                            {
                                fieldLabel: Uni.I18n.translate('general.followedBy', 'CFG', 'Followed by'),
                                xtype: 'displayfield',
                                htmlEncode: false,
                                itemId: 'followedBy-field-container',
                                width: 600
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.precededBy', 'CFG', 'Preceded by'),
                                xtype: 'displayfield',
                                htmlEncode: false,
                                itemId: 'precededBy-field-container',
                                width: 600
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('general.deviceGroup', 'CFG', 'Device group'),
                                itemId: 'data-quality-kpi-device-group',
                                name: 'deviceGroup'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('datavalidationkpis.calculationFrequency', 'CFG', 'Calculation frequency'),
                                itemId: 'data-quality-kpi-frequency',
                                name: 'frequency'
                            }
                        ]
                    }
                },
                {
                    xtype: 'uni-button-action',
                    privileges: function () {
                        return me.canAdministrate;
                    },
                    margin: '20 0 0 0',
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
                        title: Uni.I18n.translate('general.dataqualitykpi.', 'CFG', 'Data quality KPI'),
                        objectType: Uni.I18n.translate('general.dataqualitykpi.', 'CFG', 'Data quality KPI'),
                        menuItems: [
                            {
                                text: Uni.I18n.translate('general.details', 'CFG', 'Details'),
                                itemId: 'data-quality-kpi-details-overview-link'
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
