/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.audit.view.AuditSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.auditSetup',

    requires: [
        'Mdc.audit.view.AuditGrid',
        'Mdc.audit.view.AuditPreviewGrid',
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    initComponent: function () {
        var me = this;

        me.content = [
            {
                ui: 'large',
                title: Uni.I18n.translate('audit.auditTrail', 'MDC', 'Audit trail'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'auditGrid',
                            itemId: 'audit-grid',
                            domainConvertorFn: me.domainConvertorFn,
                            contextConvertorFn: me.contextConvertorFn
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'no-communication-task',
                            title: Uni.I18n.translate('audit.empty.title', 'MDC', 'No audit trails found'),
                            reasons: [
                                Uni.I18n.translate('audit.empty.list.item1', 'MDC', 'No audit trails have been generated yet.')
                            ]
                        },
                        previewComponent: {
                            xtype: 'auditPreviewGrid',
                            itemId: 'audit-preview-grid',
                            convertorFn: me.convertorFn,
                            scope: me.scope
                        }
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});


