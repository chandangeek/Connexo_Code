/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idc.view.LogGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.issue-details-log-grid',
    store: null,
    ui: 'medium',
    requires: [ 'Uni.DateTime', 'Ext.ux.exporter.ExporterButton' ],
    maxHeight: 408,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                text: Uni.I18n.translate('general.timestamp', 'IDC', 'Timestamp'),
                dataIndex: 'timestamp',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTime(value, Uni.DateTime.SHORT, Uni.DateTime.LONG) : '-';
                }
            },
            {
                text: Uni.I18n.translate('general.description', 'IDC', 'Description'),
                dataIndex: 'details',
                flex: 3,
                renderer: function (value) {
                    return value ? Ext.String.htmlEncode(value) : '';
                }
            },
            {
                text: Uni.I18n.translate('general.logLevel', 'IDC', 'Log level'),
                dataIndex: 'logLevel',
                flex: 1,
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
