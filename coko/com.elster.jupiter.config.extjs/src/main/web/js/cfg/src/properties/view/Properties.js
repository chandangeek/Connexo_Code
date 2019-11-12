/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.properties.view.Properties', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.cfg-config-properties',
    requires: [
        'Cfg.properties.view.ActionMenu',
        'Cfg.properties.view.PropertiesForm'
    ],

    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'container',
            layout: 'hbox',
            items: [
                {
                    ui: 'large',
                    itemId: 'cfg-properties-details-panel',
                    title: me.title,
                    flex: 1,
                    items: [
                        {
                            xtype: 'cfg-config-properties-form',
                            itemId: 'cfg-config-properties-form',
                            margin: '0 0 0 100'
                        }
                    ]
                },
                {
                    xtype: 'uni-button-action',
                    margin: '20 0 0 0',
                    menu: {
                        xtype: 'cfg-properties-action-menu'
                    }
                }
            ]
        };

        this.callParent(arguments);
    }
});


