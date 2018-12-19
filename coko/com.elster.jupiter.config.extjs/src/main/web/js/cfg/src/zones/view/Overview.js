/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.zones.view.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.zones-overview',
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Cfg.zones.view.Grid',
        'Cfg.zones.view.ZonePreview',
        'Cfg.zones.view.ZonesFilter'
    ],
    router: null,
    appName: null,

    initComponent: function () {
        var me = this;

        me.content = {

            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.Zones', 'CFG', 'Zones'),

            items: [
                {
                    xtype: 'zones-overview-filter',
                    itemId: 'zones-overview-filter'
                },
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'zones-grid',
                        itemId: 'grd-zones',
                        router: me.router
                    },

                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'zones-empty-component',
                        title: Uni.I18n.translate('zones.empty.title', 'CFG', 'No zones found'),
                        reasons: [
                            Uni.I18n.translate('zones.empty.list.item1', 'CFG', 'No zones have been added yet.'),
                            Uni.I18n.translate('zones.empty.list.item2', 'CFG', 'No zones comply with the filter.')
                        ],
                        stepItems: [
                            {
                                itemId: 'empty-zones-add-button',
                                text: Uni.I18n.translate('zones.addZone', 'CFG', 'Add zone'),
                                action: 'addZone',
                                privileges: Cfg.privileges.Validation.adminZones
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'zone-preview',
                        itemId: 'zone-preview',
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});