/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.audit.view.AuditSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.auditSetup',
    xtype: 'audit-setup-view',
    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Cfg.audit.view.AuditGrid',
        'Cfg.audit.view.AuditPreview',
        'Cfg.audit.view.AuditFilter'
    ],

    initComponent: function () {
        var me = this;

        me.content = [
            {
                ui: 'large',
                itemId: 'audit-trail-content',
                title: Uni.I18n.translate('audit.auditTrail', 'CFG', 'Audit trail'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'auditGrid',
                            itemId: 'audit-grid',
                            store: me.store,
                            domainConvertorFn: me.domainConvertorFn,
                            contextConvertorFn: me.contextConvertorFn,
                            scopeFn: me.scopeFn
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'no-audit-trail',
                            title: Uni.I18n.translate('audit.empty.title', 'CFG', 'No audit trails found'),
                            reasons: [
                                Uni.I18n.translate('audit.empty.list.item1', 'CFG', 'There is no audit trails available.'),
                                Uni.I18n.translate('audit.empty.list.item2', 'CFG', 'No audit trails comply with the filter.')
                            ]
                        },
                        previewComponent: {
                            xtype: 'auditPreview',
                            itemId: 'audit-preview',
                            convertorFn: me.convertorFn,
                            scopeFn: me.scopeFn
                        }
                    }
                ],
                dockedItems: [
                    {
                        dock: 'top',
                        store: me.store,
                        xtype: 'auditFilter',
                        itemId: 'audit-filter'
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});


