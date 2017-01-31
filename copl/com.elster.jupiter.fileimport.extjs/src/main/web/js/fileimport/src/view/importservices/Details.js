/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fim.view.importservices.Details', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.fin-details-import-service',
    requires: [
        'Fim.view.importservices.Menu',
        'Fim.view.importservices.PreviewForm',
        'Fim.view.importservices.ActionMenu'
    ],

    router: null,
    importServiceId: null,

    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'container',
            layout: 'hbox',
            items: [
                {
                    ui: 'large',
                    title: Uni.I18n.translate('general.overview', 'FIM', 'Overview'),
                    flex: 1,
                    items: {
                        xtype: 'fim-import-service-preview-form',
                        itemId: 'frm-import-service-details',
                        margin: '0 0 0 100'
                    }
                },
                {
                    xtype: 'uni-button-action',
                    itemId: 'btn-action',
                    margin: '20 0 0 0',
                    hidden: typeof(SystemApp) == 'undefined',
                    menu: {
                        xtype: 'fim-import-service-action-menu'
                    }
                }
            ]
        };

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'fim-import-service-menu',
                        itemId: 'import-service-view-menu',
                        router: me.router,
                        importServiceId: me.importServiceId
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});

