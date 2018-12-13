/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.view.widget.HeaderSection', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.header-section',
    itemId: 'header-section',
    layout: 'fit',
    router: null,
    ui: 'large',

    initComponent: function () {
        var me = this;
        me.title = me.router.getRoute().title;
        this.items = [
            {
                xtype: 'toolbar',
                items: [
                    {
                        xtype: 'device-group-filter',
                        router: me.router
                    },
                    '->',
                    {
                        xtype: 'component',
                        itemId: 'last-updated-field',
                        margins: '0 15 0 0'
                    },
                    {
                        xtype: 'button',
                        itemId: 'refresh-btn',
                        text: Uni.I18n.translate('general.refresh', 'DSH', 'Refresh'),
                        iconCls: 'icon-spinner11'
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});