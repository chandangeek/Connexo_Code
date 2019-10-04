/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isc.view.LogGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.servicecall-issue-log-grid',
    ui: 'medium',
    requires: [ 'Uni.DateTime', 'Ext.ux.exporter.ExporterButton' ],
    maxHeight: 364,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                text: Uni.I18n.translate('general.label.timestamp', 'ISC', 'Timestamp'),
                dataIndex: 'timestamp',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTime(value, Uni.DateTime.SHORT, Uni.DateTime.LONG) : '-';
                }
            },
            {
                text: Uni.I18n.translate('general.label.logLevel', 'ISC', 'Log level'),
                dataIndex: 'logLevel',
                flex: 1,
                renderer: function (value) {
                    return value ? Ext.String.htmlEncode(value) : '';
                }
            },
            {
                text: Uni.I18n.translate('general.label.message', 'ISC', 'Message'),
                dataIndex: 'details',
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
        me.bindStore(Ext.getStore('Isc.store.Logs'));
    }
});
