/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.messagequeues.MonitorGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.monitor-grid',
    store: 'Apr.store.MessageQueuesWithState',
    router: null,
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'APR', 'Name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.messages', 'APR', 'Messages'),
                dataIndex: 'numberOfMessages',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.errors', 'APR', 'Errors'),
                dataIndex: 'numberOFErrors',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Apr.privileges.AppServer.admin,
                menu: {
                    xtype: 'monitor-action-menu',
                    itemId: 'monitor-action-menu'
                }
            }
        ];


        me.callParent(arguments);
    }
});

