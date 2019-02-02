/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.audit.view.AuditPreviewGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.auditPreviewGrid',
    requires: [
        'Mdc.audit.store.AuditDetails'
    ],
    store: 'Mdc.audit.store.AuditDetails',
    maxHeight: 355,

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('audit.preview.propertyName', 'MDC', 'Property name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('audit.preview.changedTo', 'MDC', 'Changed to'),
                dataIndex: 'value',
                flex: 1,
                renderer: function (value, metaData, record) {
                    return me.convertorFn.call(me.scopeFn, value, record);
                }
            },
            {
                header: Uni.I18n.translate('audit.preview.changedFrom', 'MDC', 'Changed from'),
                dataIndex: 'previousValue',
                flex: 1,
                renderer: function (value, metaData, record) {
                    return me.convertorFn.call(me.scopeFn, value, record);
                }
            }
        ];

        /* me.on('refresh', function () {
         });
         me.on('datachanged', function () {
         });*/

        me.callParent(arguments);
    }
});

