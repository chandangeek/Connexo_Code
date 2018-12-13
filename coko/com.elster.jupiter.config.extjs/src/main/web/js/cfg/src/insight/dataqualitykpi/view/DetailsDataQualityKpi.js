/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.insight.dataqualitykpi.view.DetailsDataQualityKpi', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.ins-data-quality-kpi-details',
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
                            width: 600
                        },
                        items: [
                            {
                                fieldLabel: Uni.I18n.translate('general.followedBy', 'CFG', 'Followed by'),
                                xtype: 'displayfield',
                                htmlEncode: false,
                                itemId: 'followedBy-field-container'
                            },
                            {
                                xtype: 'displayfield',
                                itemId: 'precededBy-field-container',
                                fieldLabel: Uni.I18n.translate('general.precededBy', 'CFG', 'Preceded by'),
                                htmlEncode: false
                            },
                            {
                                xtype: 'displayfield',
                                itemId: 'cmb-usage-point-group',
                                fieldLabel: Uni.I18n.translate('general.uagePointGroup', 'CFG', 'Usage point group')
                            },
                            {
                                xtype: 'displayfield',
                                itemId: 'view-purpose',
                                fieldLabel: Uni.I18n.translate('general.Purpose', 'CFG', 'Purpose')
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
