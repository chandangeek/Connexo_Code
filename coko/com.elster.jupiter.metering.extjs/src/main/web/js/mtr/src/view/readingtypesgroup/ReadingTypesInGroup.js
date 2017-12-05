/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.readingtypesgroup.ReadingTypesInGroup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.reading-types-in-group',
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

        this.callParent(arguments);
    }
});


