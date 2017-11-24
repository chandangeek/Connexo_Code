/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.readingtypesgroup.Details', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.reading-type-groups-details',
    requires: [
        'Mtr.view.readingtypesgroup.Menu',
        'Mtr.view.readingtypesgroup.PreviewForm'
    ],

    router: null,

    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'container',
            layout: 'hbox',
            items: [
                {
                    ui: 'large',
                    itemId: 'reding-type-groups-details-panel',
                    title: Uni.I18n.translate('readingtypesmanagement.overview', 'MTR', 'Overview'),
                    flex: 1,
                    items: {
                        xtype: 'reading-types-group-preview-form',
                        itemId: 'reading-types-group-details',
                        margin: '0 0 0 100',
                        router: me.router
                    }
                },
                // The action menu
                /*{
                    xtype: 'uni-button-action',
                    itemId: 'btn-action',
                    margin: '20 0 0 0',
                    hidden: typeof(SystemApp) == 'undefined',
                    menu: {
                        xtype: 'fim-import-service-action-menu'
                    }
                }*/
            ]
        };

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'reading-types-group-menu',
                        itemId: 'mnu-reading-types-group',
                        router: me.router,
                        toggle: 0
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});


