Ext.define('Uni.view.container.PortalContainer', {
    extend: 'Ext.container.Container',
    xtype: 'portal-container',

    baseCls: Uni.About.baseCssPrefix + 'portal-container',

    layout: 'column',

    addPortalItem: function (model) {
        var component = this.createPortalWidgetFromItem(model),
            index = model.get('index');

        if (index === '' || index === null || typeof index === 'undefined') {
            this.add(component);
        } else {
            this.insert(index, component);
        }

        this.add(component);
    },

    createPortalWidgetFromItem: function (model) {
        var title = model.get('title'),
            items = model.get('items'),
            widget;

        widget = Ext.create('Ext.panel.Panel', {
            title: title,
            frame: true,
            cls: Uni.About.baseCssPrefix + 'portal-panel',
            columnWidth: 1 / 3,
            height: 256,
            items: [
                {
                    xtype: 'menu',
                    floating: false,
                    items: items
                }
            ]
        });

        return widget;
    }
});