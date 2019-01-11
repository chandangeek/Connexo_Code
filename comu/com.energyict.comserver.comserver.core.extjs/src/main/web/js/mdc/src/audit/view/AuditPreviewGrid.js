/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.audit.view.AuditPreviewGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.auditPreviewGrid',
    scroll: false,
    requires: [],

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('audit.preview.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 1,
            },
            {
                header: Uni.I18n.translate('audit.preview.value', 'MDC', 'Value'),
                dataIndex: 'value',
                flex: 1,
                renderer: function (value, metaData, record) {
                    return me.convertorFn.call(me.scope, value, record);
                }
            },
            {
                header: Uni.I18n.translate('audit.preview.previousValue', 'MDC', 'Old value'),
                dataIndex: 'previousValue',
                flex: 1,
                renderer: function (value, metaData, record) {
                    return me.convertorFn.call(me.scope, value, record);
                }
            }
        ];

        me.callParent(arguments);
    }
});

