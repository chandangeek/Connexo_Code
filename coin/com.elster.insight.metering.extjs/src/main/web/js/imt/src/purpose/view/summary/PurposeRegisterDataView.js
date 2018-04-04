/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Imt.purpose.view.summary.PurposeRegisterDataView', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.purpose-register-data-view',

    requires: [
        'Imt.purpose.view.summary.PurposeRegisterDataTopFilter',
        'Imt.purpose.view.summary.PurposeRegisterDataGrid',
        'Imt.purpose.view.summary.PurposeRegisterDataPreview'
    ],

    initComponent: function () {
        var me = this,
            registerDataStore = Ext.getStore('Imt.purpose.store.PurposeSummaryRegisterData');

        me.items = [
            {
                xtype: 'purpose-register-topfilter',
                itemId: 'purpose-register-topfilter',
                dock: 'top',
                store: registerDataStore
            },
            {
                xtype: 'preview-container',
                grid: {
                    xtype: 'purpose-register-data-grid',
                    itemId: 'purpose-register-data-grid',
                    usagePointName: me.usagePoint.get('name'),
                    purposeId: me.purpose.get('id'),
                    store: registerDataStore
                },
                emptyComponent: {
                    xtype: 'no-data-on-purpose-found-panel',
                    itemId: 'readings-empty-panel'
                },
                previewComponent: {
                    xtype: 'purpose-register-data-preview',
                    itemId: 'purpose-register-data-preview',
                    router: me.router,
                    hidden: true
                }
            }
        ];
        me.callParent(arguments);
    }
});
