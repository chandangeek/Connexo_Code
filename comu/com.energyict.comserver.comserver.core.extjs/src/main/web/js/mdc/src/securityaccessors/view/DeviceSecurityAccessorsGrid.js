/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.DeviceSecurityAccessorsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-security-accessors-grid',
    store: undefined,

    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Mdc.securityaccessors.view.SecurityAccessorsActionMenu'
    ],
    forceFit: true,

    keyMode: undefined,

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 3
            },
            {
                header: Uni.I18n.translate('general.lastReadDate', 'MDC', 'Last read date'),
                dataIndex: 'lastReadDate',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.validUntil', 'MDC', 'Valid until'),
                flex: 1,
                dataIndex: 'expirationTime',
                renderer: function(value){
                    if (Ext.isEmpty(value)) {
                        return '-';
                    }
                    return Uni.DateTime.formatDateShort(new Date(value));
                }
            },
            {
                header: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                dataIndex: 'status',
                flex: 1
            },
            //,
            //{
            //    xtype: 'uni-actioncolumn',
            //    menu: {
            //        xtype: 'security-accessors-action-menu',
            //        itemId: 'mdc-security-accessors-action-menu'
            //    }
            //}
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                isFullTotalCount: true,
                store: me.store,
                dock: 'top',
                emptyMsg: me.keyMode
                    ? Uni.I18n.translate('keys.pagingtoolbartop.emptyMsg', 'MDC', 'No keys')
                    : Uni.I18n.translate('certificates.pagingtoolbartop.emptyMsg', 'MDC', 'No certificates'),
                displayMsg: me.keyMode
                    ? Uni.I18n.translate('keys.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} keys')
                    : Uni.I18n.translate('certificates.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} certificates'),
                displayMoreMsg: me.keyMode
                    ? Uni.I18n.translate('keys.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} keys')
                    : Uni.I18n.translate('certificates.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} certificates')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: me.keyMode
                    ? Uni.I18n.translate('keys.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Keys per page')
                    : Uni.I18n.translate('certificates.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Certificates per page'),
                dock: 'bottom'
            }
        ];
        me.callParent(arguments);
    }
});