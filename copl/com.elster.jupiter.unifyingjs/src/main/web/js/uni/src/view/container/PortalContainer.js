/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.container.PortalContainer', {
    extend: 'Ext.panel.Panel',
    xtype: 'portal-container',
    ui: 'large',
    padding: '16px 0 0 0 ',
    layout: 'column',
    columnCount: 3,
    portalWidgetHeight: 270,

    addPortalItem: function (model) {
        var me = this,
            component = this.createPortalWidgetFromItem(model),
            index = model.get('index');

        if (index === '' || index === null || typeof index === 'undefined') {
            me.add(component);
        } else {
            me.insert(index, component);
        }
    },

    createPortalWidgetFromItem: function (model) {
        var me = this,
            title = model.get('title'),
            items = model.get('items'),
            itemId = model.get('itemId'),
            afterrender = model.get('afterrender'),
            widget;

        itemId = (Ext.isString(itemId) && itemId.length) ? itemId : undefined;
        if (typeof items === 'undefined') {
            return widget;
        }

        me.applyBullets(items);

        widget = Ext.create('Ext.panel.Panel', {
            title: title,
            ui: 'tile',
            itemId:itemId,
            columnWidth: 1 / me.columnCount,
            height: me.portalWidgetHeight,
            overflowY:true,
            items: [
                {
                    xtype: 'menu',
                    ui: 'tilemenu',
                    floating: false,
                    items: items
                }
            ],
            refresh : function (items) {
                var me = this;
                var menu = me.down('menu');
                if(menu) {
                    menu.removeAll();
                    me.applyBullets(items);
                    menu.add(items);
                }
            }
        });

        if(afterrender){
            widget.on('afterrender',afterrender );
        }

        return widget;
    },

    applyBullets: function(items) {
        Ext.Array.each(items, function(item) {
            if (!item.text.startsWith('<span')) {
                item.text = '<span class="icon-target" style="margin-right: 3px; cursor:pointer; text-decoration:none; display:inline-block; color:#A9A9A9; font-size:12px;"></span>'
                    + item.text;
            }
        });
    }
});