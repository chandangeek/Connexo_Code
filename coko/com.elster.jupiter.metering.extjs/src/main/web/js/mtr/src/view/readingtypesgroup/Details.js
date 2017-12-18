/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.readingtypesgroup.Details', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.reading-type-groups-details',
    requires: [
        'Mtr.view.readingtypesgroup.Menu',
        'Mtr.view.readingtypesgroup.PreviewForm',
        'Mtr.view.readingtypesgroup.GroupActionMenu'
    ],

    router: null,

    content: {
        xtype: 'container',
        layout: 'hbox',
        items: [
            {
                ui: 'large',
                title: Uni.I18n.translate('readingtypesmanagement.overview', 'MTR', 'Overview'),
                flex: 1,
                items: {
                    xtype: 'readingTypesGroup-preview-form',
                    itemId: 'reading-types-group-details',

                }
            },
            {
                xtype: 'uni-button-action',
                itemId: 'btn-action',
                margin: '20 0 0 0',
                menu: {
                    xtype: 'readingTypesGroup-action-menu'
                }
            }
        ]
    },

    initComponent: function () {
        var me = this;
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


