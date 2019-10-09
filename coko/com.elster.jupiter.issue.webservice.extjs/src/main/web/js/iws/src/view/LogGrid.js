/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Iws.view.LogGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.webservice-issue-log-grid',
    store: null,
    ui: 'medium',
    requires: [ 'Uni.DateTime', 'Ext.ux.exporter.ExporterButton' ],
    maxHeight: 364,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                text: Uni.I18n.translate('general.label.timestamp', 'IWS', 'Timestamp'),
                dataIndex: 'timestamp',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTime(value, Uni.DateTime.SHORT, Uni.DateTime.LONG) : '-';
                }
            },
            {
                text: Uni.I18n.translate('general.label.logLevel', 'IWS', 'Log level'),
                dataIndex: 'logLevel',
                flex: 1,
                renderer: function (value) {
                    return value ? Ext.String.htmlEncode(value.name) : '';
                }
            },
            {
                text: Uni.I18n.translate('general.label.message', 'IWS', 'Message'),
                dataIndex: 'message',
                flex: 3,
                renderer: function (value) {
                    return value ? Ext.String.htmlEncode(value) : '';
                }
            } 
        ];

        me.dockedItems = [
            {
                xtype: 'toolbar',
                itemId: 'components-list-top-toolbar',
                items: [
                    '->',
                    {
                        xtype: 'exporterbutton',
                        itemId: 'components-exporter-button',
                        ui: 'icon',
                        iconCls: 'icon-file-download',
                        text: '',
                        component: me
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
