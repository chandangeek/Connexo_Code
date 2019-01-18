/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.audit.view.AuditSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.auditSetup',

    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.audit.view.AuditGrid',
        'Mdc.audit.view.AuditPreview',
        'Mdc.audit.view.AuditFilter'
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
                            itemId: 'no-audit-trail',
                            title: Uni.I18n.translate('audit.empty.title', 'MDC', 'No audit trails found'),
                            reasons: [
                                Uni.I18n.translate('audit.empty.list.item1', 'MDC', 'There is no audit trails available.'),
                                Uni.I18n.translate('audit.empty.list.item2', 'MDC', 'No audit trails comply with the filter.')
                            ]
                        },
                        previewComponent: {
                            xtype: 'auditPreview',
                            itemId: 'audit-preview',
                            convertorFn: me.convertorFn,
                            scope: me.scope
                        }
                    }
                ],
                dockedItems: [
                    {
                        dock: 'top',
                        xtype: 'auditFilter',
                        itemId: 'audit-filter'
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});


