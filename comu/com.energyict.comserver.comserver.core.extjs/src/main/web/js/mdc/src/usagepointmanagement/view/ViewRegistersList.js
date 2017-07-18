/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.view.ViewRegistersList', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.view-registers-list',

    requires: [
        'Uni.util.FormInfoMessage',
        'Uni.view.container.PreviewContainer',
        'Mdc.usagepointmanagement.view.UsagePointSideMenu',
        'Mdc.usagepointmanagement.view.RegistersGrid',
        'Mdc.usagepointmanagement.view.RegisterPreview'
    ],

    router: null,
    usagePointId: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                itemId: 'view-registers-list-panel',
                title: Uni.I18n.translate('general.registers', 'MDC', 'Registers'),
                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'usage-point-registers-grid',
                            itemId: 'usage-point-registers-grid',
                            router: me.router
                        },
                        emptyComponent: {
                            xtype: 'uni-form-info-message',
                            itemId: 'usage-point-registers-empty-msg',
                            text: Uni.I18n.translate('usagePointRegisters.noItems', 'MDC', 'No available registers because no metrology configuration versions until current moment in time. See {0}versions{1} of metrology configurations',
                                ['<a href="'
                                + me.router.getRoute('usagepoints/usagepoint/history').buildUrl(null, Ext.apply({historyTab: 'metrologyconfigurationversion'}, me.router.queryParams))
                                + '">', '</a>'],
                                false)
                        },
                        previewComponent: {
                            xtype: 'usage-point-register-preview',
                            itemId: 'usage-point-register-preview',
                            frame: true,
                            router: me.router
                        }
                    }
                ]
            }
        ];

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'usage-point-management-side-menu',
                        itemId: 'usage-point-management-side-menu',
                        router: me.router,
                        usagePointId: me.usagePointId
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});