/**
 * @class Uni.view.navigation.Menu
 */
Ext.define('Uni.view.navigation.Menu', {
    extend: 'Ext.container.Container',
    xtype: 'navigationMenu',
    ui: 'navigationmenu',

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'container',
                flex: 1,
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                defaults: {
                    xtype: 'button',
                    ui: 'menuitem',
                    hrefTarget: '_self',
                    toggleGroup: 'menu-items',
                    action: 'menu-main',
                    enableToggle: true,
                    allowDepress: false,
                    cls: 'menu-item',
                    tooltipType: 'title',
                    scale: 'large'
                }
            },
            {
                // TODO
                xtype: 'button',
                text: 'Toggle',
                enableToggle: true,
                ui: 'action'
            }
        ];

        me.callParent(arguments);
    },

    removeAllMenuItems: function () {
        this.getMenuContainer().removeAll();
    },

    addMenuItem: function (model) {
        var me = this,
            item = me.createMenuItemFromModel(model);

        // TODO Sort the buttons on their model's index value, instead of relying on insert.
        if (model.data.index === '' || model.data.index === null || model.data.index === undefined) {
            me.getMenuContainer().add(item);
        } else {
            me.getMenuContainer().insert(model.data.index, item);
        }
    },

    createMenuItemFromModel: function (model) {
        var iconCls = model.data.glyph ? 'uni-icon-' + model.data.glyph : 'uni-icon-none',
            href = model.data.portal ? '#/' + model.data.portal : model.data.href;

        return {
            tooltip: model.data.text,
            text: model.data.text,
            href: href,
            data: model,
            iconCls: iconCls,
            hidden: model.data.hidden
        };
    },

    selectMenuItem: function (model) {
        var me = this,
            itemId = model.id;

        this.getMenuContainer().items.items.forEach(function (item) {
            if (itemId === item.data.id) {
                me.deselectAllMenuItems();
                item.toggle(true, false);
            }
        });
    },

    deselectAllMenuItems: function () {
        this.getMenuContainer().items.items.forEach(function (item) {
            item.toggle(false, false);
        });
    },

    getMenuContainer: function () {
        return this.down('container:first');
    }
});