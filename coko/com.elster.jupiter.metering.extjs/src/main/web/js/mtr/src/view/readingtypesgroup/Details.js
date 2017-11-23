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

    content: {
        xtype: 'container',
        layout: 'hbox',
        items: [
            {
                ui: 'large',
                title: Uni.I18n.translate('readingtypes.readingTypeGroup', 'MTR', 'Details'),
                flex: 1,
                items: [
                    {
                        xtype: 'reading-type-groups-preview-form'
                    }
                ]
            },
            {
                xtype: 'uni-button-action',
                privileges : Tme.privileges.Period.admin,
                margin: '20 0 0 0',
                menu: {
                    xtype: 'reading-type-groups-action-menu'
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
                        router: me.router,
                        toggle: 0
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});


