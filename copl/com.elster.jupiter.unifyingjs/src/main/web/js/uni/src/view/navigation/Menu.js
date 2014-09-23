/**
 * @class Uni.view.navigation.Menu
 */
Ext.define('Uni.view.navigation.Menu', {
    extend: 'Ext.container.Container',
    alias: 'widget.navigationMenu',
    ui: 'navigationmenu',

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
    },

    removeAllMenuItems: function () {
        this.removeAll();
    },

    addMenuItem: function (model) {
        var me = this,
            item = me.createMenuItemFromModel(model);

        // TODO Sort the buttons on their model's index value, instead of relying on insert.
        if (model.data.index === '' || model.data.index === null || model.data.index === undefined) {
            this.add(item);
        } else {
            this.insert(model.data.index, item);
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

        this.items.items.forEach(function (item) {
            if (itemId === item.data.id) {
                me.deselectAllMenuItems();
                item.toggle(true, false);
            }
        });
    },

    deselectAllMenuItems: function () {
        this.items.items.forEach(function (item) {
            item.toggle(false, false);
        });
    }
});