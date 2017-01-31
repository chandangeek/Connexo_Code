/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sct.view.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.servicecalltypes-grid',
    store: 'Sct.store.ServiceCallTypes',
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.type', 'SCT', 'Type'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.version', 'SCT', 'Version'),
                dataIndex: 'versionName',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.status', 'SCT', 'Status'),
                dataIndex: 'statusName',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.logLevel', 'SCT', 'Log level'),
                dataIndex: 'logLevelName',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.lifeCycle', 'SCT', 'Life cycle'),
                dataIndex: 'lifecycle',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
               // privileges: Apr.privileges.AppServer.admin,
                menu: {
                    xtype: 'sct-action-menu',
                    itemId: 'sct-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('serviceCallTypes.pagingtoolbartop.displayMsg', 'SCT', '{0} - {1} of {2} service call types'),
                displayMoreMsg: Uni.I18n.translate('serviceCallTypes.pagingtoolbartop.displayMoreMsg', 'SCT', '{0} - {1} of more than {2} service call types'),
                emptyMsg: Uni.I18n.translate('serviceCallTypes.pagingtoolbartop.emptyMsg', 'SCT', 'There are no service call types to display'),
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('serviceCallTypes.pagingtoolbarbottom.itemsPerPage', 'SCT', 'Service call types per page'),
                dock: 'bottom'
            }
        ]

        me.callParent(arguments);
    }
});