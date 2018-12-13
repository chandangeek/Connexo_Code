/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicecommunicationtaskhistory.DeviceCommunicationTaskHistoryLogGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceCommunicationTaskHistoryLogGrid',
    itemId: 'deviceCommunicationTaskHistoryLogGrid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.DateTime'
    ],
    store: 'DeviceCommunicationTaskLog',
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
                exportText: Uni.I18n.translate('devicecommunicationtaskhistory.loglevel', 'MDC', 'Log level'),
                renderer: function (value) {
                    if ((value != 'Warning') && (value != 'Error')){
                        return '';
                    }

                    var exportText = (value == 'Warning') ? Uni.I18n.translate('devicecommunicationtaskhistory.loglevel.warning', 'MDC', 'Warning') : Uni.I18n.translate('devicecommunicationtaskhistory.loglevel.error', 'MDC', 'Error'),
                        icon = (value == 'Warning') ? 'icon-warning' : 'icon-notification',
                        color = (value == 'Warning') ? '' : 'color: #eb5642; ';

                    return '<span class="' + icon + '" style="' + color + 'margin-left: 10px" data-qtip="' + exportText + '">'
                        +'<div style="display:none;">' + exportText + '</div></span>';
                }
            },
            {
                itemId: 'timestamp',
                text: Uni.I18n.translate('devicecommunicationtaskhistory.timeStamp', 'MDC', 'Timestamp'),
                exportText: Uni.I18n.translate('devicecommunicationtaskhistory.timeStamp', 'MDC', 'Timestamp'),
                dataIndex: 'timestamp',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTime(value, Uni.DateTime.SHORT, Uni.DateTime.LONGWITHMILLIS) : '-';
                }
            },
            {
                itemId: 'details',
                text: Uni.I18n.translate('devicecommunicationtaskhistory.message', 'MDC', 'Message'),
                exportText: Uni.I18n.translate('devicecommunicationtaskhistory.message', 'MDC', 'Message'),
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
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('devicecommunicationtaskhistory.pagingtoolbartop.log.displayMsg', 'MDC', '{0} - {1} of {2} log lines'),
                displayMoreMsg: Uni.I18n.translate('devicecommunicationtaskhistory.pagingtoolbartop.log.displayMoreMsg', 'MDC', '{0} - {1} of {2} log lines')
//                emptyMsg: Uni.I18n.translate('deviceconnectionhistory.pagingtoolbartop.log.emptyMsg', 'MDC', 'There are no log lines')
            }
        ];
        me.callParent();
    }

})
;
