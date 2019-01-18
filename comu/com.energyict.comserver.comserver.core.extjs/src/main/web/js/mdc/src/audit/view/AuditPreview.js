/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.audit.view.AuditPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.auditPreview',
    requires: [
        'Mdc.audit.view.AuditPreviewGrid'
    ],

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'auditPreviewGrid',
                itemId: 'audit-preview-grid',
                convertorFn: me.convertorFn,
                scope: me.scope
            },
            {
                xtype: 'no-items-found-panel',
                itemId: 'audit-preview-no-items',
                margin: '15 0 20 0',
                hidden: true,
                title: Uni.I18n.translate('audit.empty.title', 'MDC', 'No audit trail logs found'),
                reasons: [
                    Uni.I18n.translate('audit.empty.list.item1', 'MDC', 'There is no audit trail logs available.')
                ]
            }
        ];

        me.callParent(arguments);
    }
});

