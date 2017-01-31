/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroups.view.UsagePointsOfUsagePointGroupGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.usagepoints-of-usagepointgroup-grid',
    xtype: 'usagepoints-of-usagepointgroup-grid',    
    requires: [        
        'Imt.usagepointgroups.store.UsagePointsOfUsagePointGroup'
    ],
    plugins: [{
        ptype: 'showConditionalToolTip',
        pluginId: 'showConditionalToolTipId'
    }],    
    store: 'Uni.store.search.Results',
    forceFit: true,
    enableColumnMove: true,
    columns: [],
    config: {
        service: null
    },

    initComponent: function () {
        var me = this,
            service = me.getService(),
            searchFields = Ext.getStore('Uni.store.search.Fields');

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('usagepoints.pagingtoolbartop.displayMsg', 'IMT', '{0} - {1} of {2} usage points'),
                displayMoreMsg: Uni.I18n.translate('usagepoints.pagingtoolbartop.displayMoreMsg', 'IMT', '{0} - {1} of more than {2} usage points'),
                emptyMsg: Uni.I18n.translate('usagepoints.pagingtoolbartop.emptyMsg', 'IMT', 'There are no usage points to display'),
                items: [
                    {
                        text: Uni.I18n.translate('general.count', 'IMT', 'Count'),
                        privileges: Imt.privileges.UsagePointGroup.administrate,
                        itemId: 'usagepoints-count-btn',
                        xtype: 'button',
                        action: 'countUsagePointsOfGroup'
                    },
                    {
                        xtype: 'uni-search-column-picker',
                        itemId: 'usagepoints-column-picker',
                        grid: me
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('usagepoints.pagingtoolbarbottom.itemsPerPage', 'IMT', 'Usage points per page'),
                dock: 'bottom',
                deferLoading: true,
                pageSizeStore: Ext.create('Ext.data.Store', {
                    fields: ['value'],
                    data: [
                        {value: '10'},
                        {value: '20'},
                        {value: '50'},
                        {value: '100'},
                        {value: '200'},
                        {value: '1000'}
                    ]
                })
            }
        ];

        var storeListeners = searchFields.on('load', function (store, items) {
            me.down('pagingtoolbartop').resetPaging();
            me.down('pagingtoolbarbottom').resetPaging();
            me.down('uni-search-column-picker').setColumns(items.map(function (field) {
                return service.createColumnDefinitionFromModel(field)
            }));
        }, me, {
            destroyable: true
        });

        me.callParent(arguments);
        me.on('destroy', function () {
            storeListeners.destroy();
        });
    }
});



