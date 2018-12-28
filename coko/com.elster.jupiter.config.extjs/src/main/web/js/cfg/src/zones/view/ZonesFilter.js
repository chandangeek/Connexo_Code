/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Cfg.zones.view.ZonesFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    alias: 'widget.zones-overview-filter',
    store: 'Cfg.zones.store.Zones',
    requires:[
        'Cfg.zones.store.ZoneTypes'
    ],

    initComponent: function () {
        var me = this;
        me.filters = [
            {
                type: 'combobox',
                itemId: 'zones-filter-zonetype-combo',
                dataIndex: 'zoneTypes',
                emptyText:  Uni.I18n.translate('general.zoneType', 'CFG', 'Zone type'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Cfg.zones.store.ZoneTypes'
            }
        ];
        me.callParent(arguments);
    },

});
