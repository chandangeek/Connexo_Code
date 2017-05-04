/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceconnectionhistory.DeviceConnectionLogGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceConnectionLogGrid',
    itemId: 'deviceConnectionLogGrid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.DateTime'
    ],
    store: 'DeviceConnectionLog',
    hasHtmlInColumnHeaders: true,
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'loglevel',
                dataIndex: 'logLevel',
                exportText: Uni.I18n.translate('deviceconnectionhistory.loglevel', 'MDC', 'Log level'),
                renderer: function (value) {
                    if ((value != 'Warning') && (value != 'Error')){
                        return '';
                    }

                    var exportText = (value == 'Warning') ? Uni.I18n.translate('deviceconnectionhistory.loglevel.warning', 'MDC', 'Warning') : Uni.I18n.translate('deviceconnectionhistory.loglevel.error', 'MDC', 'Error'),
                        icon = (value == 'Warning') ? 'icon-warning' : 'icon-notification',
                        color = (value == 'Warning') ? '' : 'color: #eb5642; ';

                    return '<span class="' + icon + '" style="' + color + 'margin-left: 10px" data-qtip="' + exportText + '">'
                        +'<div style="display:none;">' + exportText + '</div></span>';
                }
            },
            {
                itemId: 'timestamp',
                text: Uni.I18n.translate('deviceconnectionhistory.timeStamp', 'MDC', 'Timestamp'),
                exportText: Uni.I18n.translate('deviceconnectionhistory.timeStamp', 'MDC', 'Timestamp'),
                dataIndex: 'timestamp',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTime(value, Uni.DateTime.SHORT, Uni.DateTime.LONGWITHMILLIS) : '-';
                }
            },
            {
                itemId: 'details',
                text: Uni.I18n.translate('deviceconnectionhistory.message', 'MDC', 'Message'),
                exportText: Uni.I18n.translate('deviceconnectionhistory.message', 'MDC', 'Message'),
                dataIndex: 'details',
                flex: 3
            }
        ]
    },
    initComponent: function () {
        var me = this;
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                noBottomPaging: true,
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('deviceconnectionhistory.pagingtoolbartop.logLines.displayMsg', 'MDC', '{0} - {1} of {2} log lines'),
                displayMoreMsg: Uni.I18n.translate('deviceconnectionhistory.pagingtoolbartop.logLines.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} log lines')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                defaultPageSize: 100,
                itemsPerPageMsg: Uni.I18n.translate('deviceconnectionhistory.pagingtoolbarbottom.itemsPerPageRuleSet', 'MDC', 'Log lines per page'),
                dock: 'bottom'
            }
        ];
        me.callParent();
    }

})
;
