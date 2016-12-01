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
                    menu.add(items);
                }
            }
        });

        if(afterrender){
            widget.on('afterrender',afterrender );
        }

        return widget;
    }
});