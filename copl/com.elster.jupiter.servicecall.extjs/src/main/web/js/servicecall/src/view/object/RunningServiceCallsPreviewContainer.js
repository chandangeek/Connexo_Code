/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Scs.view.object.RunningServiceCallsPreviewContainer', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.running-service-call-preview-container',
    store: null,

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Scs.view.Grid'
    ],

    emptyComponent: {
        xtype: 'no-items-found-panel',
        itemId: 'no-service-calls',
        title: Uni.I18n.translate('serviceCalls.empty.title', 'SCS', 'No service calls found'),
        reasons: [
            Uni.I18n.translate('serviceCalls.empty.list.item1', 'SCS', 'No service calls have been defined yet.'),
            Uni.I18n.translate('serviceCalls.empty.list.item2', 'SCS', "Service calls exist, but you don't have permission to view them.")
        ]
    },

    initComponent: function () {
        var me = this;
        me.grid = {
            xtype: 'servicecalls-grid',
            store: me.store,
            menuItemId: 'object-service-calls-action-menu',
            itemId: 'running-service-calls-grid',
            cancelAllHidden: false,
        };


        me.callParent(arguments);
    }
});