Ext.define('Uni.view.container.PortalContainer', {
    extend: 'Ext.panel.Panel',
    xtype: 'portal-container',
    ui: 'large',

    padding: '16px 0 0 0 ',
    layout: 'column',
    columnCount: 3,

    addPortalItem: function (model) {
        var me = this,
            component = this.createPortalWidgetFromItem(model),
            index = model.get('index');

        if (index === '' || index === null || typeof index === 'undefined') {
            this.add(component);
        } else {
            this.insert(index, component);
        }

        var count = this.items.items.length,
            remainder = count % me.columnCount;

        switch (remainder) {
            case 1:
                component.addCls('first');
                break;
            case 2:
                component.addCls('middle');
                break;
            default:
                component.addCls('last');
                break;
        }
    },

    createPortalWidgetFromItem: function (model) {
        var me = this,
            title = model.get('title'),
            items = model.get('items'),
            itemId = model.get('itemId'),
            widget;

        itemId = (Ext.isString(itemId) && itemId.length) ? itemId : undefined;
        if (typeof items === 'undefined') {
            return widget;
        }

        widget = Ext.create('Ext.panel.Panel', {
            title: title,
            ui: 'tile',
            itemId:itemId,
            columnWidth: 1 / me.columnCount,
            height: 256,
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
                menu.removeAll();
                menu.add(items);
            }
        });

        return widget;
    }
});