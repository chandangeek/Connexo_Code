/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroups.view.DynamicGroupUsagePointsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.dynamic-group-usagepoints-grid',
    xtype: 'dynamic-group-usagepoints-grid',
    store: 'Imt.usagepointgroups.store.DynamicGroupUsagePoints',    
    plugins: [{
        ptype: 'showConditionalToolTip',
        pluginId: 'showConditionalToolTipId'
    }],

    forceFit: true,
    enableColumnMove: true,
    columns: [],
    config: {
        service: null
    },

    initComponent: function () {
        var me = this;

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('usagepoints.pagingtoolbartop.displayMsg', 'IMT', '{0} - {1} of {2} usage points'),
                displayMoreMsg: Uni.I18n.translate('usagepoints.pagingtoolbartop.displayMoreMsg', 'IMT', '{0} - {1} of more than {2} usage points'),
                emptyMsg: Uni.I18n.translate('usagepoints.pagingtoolbartop.emptyMsg', 'IMT', 'There are no usage points to display'),
                items: {
                    xtype: 'uni-search-column-picker',
                    itemId: 'usagepoints-dynamic-column-picker',
                    grid: me
                }
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('usagepoints.pagingtoolbarbottom.itemsPerPage', 'IMT', 'Usage points per page'),
                dock: 'bottom',
                deferLoading: true
            }
        ];

        me.callParent(arguments);
    }
});