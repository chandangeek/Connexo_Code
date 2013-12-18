Ext.define('Uni.view.navigation.Menu', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.navigationMenu',
    requires: [
        'Ext.layout.container.Card'
    ],

    layout: 'fit',

    items: [
        {
            xtype: 'panel',
            itemId: 'menu-main',
            cls: 'nav-menu',
            headerPosition: 'left',
//            collapsed: true,
//            collapsible: true,
//            collapseMode: 'header',
//            titleCollapse: false,
//            floatable: true,

            layout: 'card',

            header: {
                xtype: 'container',
                cls: 'nav-menu-main',
                width: 55,
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                defaults: {
                    xtype: 'button',
                    hrefTarget: '_self',
                    toggleGroup: 'menu-items',
                    action: 'menu-main',
                    enableToggle: true,
                    allowDepress: false,
                    cls: 'menu-item',
                    tooltipType: 'title',
                    scale: 'large'
                }
            }
        }
    ],

    removeAllMenuItems: function () {
        var menu = this.getComponent('menu-main');
        menu.removeAll();
    },

    addMenuItem: function (model) {
        var me = this,
            item = me.createMainItemFromModel(model),
            menu = me.getComponent('menu-main'),
            menuHeader = menu.getHeader();

        var card = me.createMenu(model);
        model.children().data.items.forEach(function (child) {
            card.add(me.createMenuItemFromModel(child));
        });

        menu.add(card);

        // TODO Sort the buttons on their model's index value, instead of relying on insert.
        if (model.data.index === '' || model.data.index === null || model.data.index === undefined) {
            menuHeader.add(item);
        } else {
            menuHeader.insert(model.data.index, item);
        }
    },

    createMainItemFromModel: function (model) {
        return {
            tooltip: model.data.text,
            href: model.data.href,
            glyph: model.data.glyph,
            data: model
        };
    },

    createMenuItemFromModel: function (model) {
        return {
            text: model.data.text,
            href: model.data.href,
            data: model
        };
    },

    createMenu: function (model) {
        return Ext.create('Ext.container.Container', {
            cls: 'nav-menu-sub',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            width: 190,
            defaults: {
                xtype: 'button',
                hrefTarget: '_self',
                toggleGroup: 'menu-items-sub',
                enableToggle: true,
                allowDepress: false,
                cls: 'menu-item',
                tooltipType: 'title',
                scale: 'large'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<h2>' + model.data.text + '</h2>'
                }
            ]
        });
    },

    selectMenuItem: function (model) {
        var me = this,
            itemId = model.id,
            menu = me.getComponent('menu-main'),
            menuHeader = me.getComponent('menu-main').getHeader();

        menuHeader.items.items.forEach(function (item, index) {
            if (itemId === item.data.id) {
                item.toggle(true, false);
                menu.getLayout().setActiveItem(index);
                return;
            }
        });
    },

    showActiveMenu: function () {
        var me = this,
            menu = me.getComponent('menu-main'),
            menuHeader = menu.getHeader();

        menuHeader.items.items.forEach(function (item, index) {
            if (item.pressed) {
                menu.getLayout().setActiveItem(index);
                return;
            }
        });
    },

    peekMenuItem: function (itemId) {
        var me = this,
            menu = me.getComponent('menu-main'),
            menuHeader = menu.getHeader();

        menuHeader.items.items.forEach(function (item, index) {
            if (itemId === item.data.id) {
                menu.getLayout().setActiveItem(index);
                return;
            }
        });
    },

    floatCollapsedMenu: function () {
        this.getPlaceholder();
        this.floatCollapsedPanel();
    },

    highlightActiveMenu: function () {
        var me = this,
            menu = me.getComponent('menu-main'),
            menuHeader = menu.getHeader(),
            itemId = menu.items.indexOf(menu.getLayout().getActiveItem());

        menuHeader.items.items.forEach(function (item, index) {
            if (itemId === index && !item.pressed) {
                item.addCls('highlighted');
            } else {
                item.removeCls('highlighted');
            }
        });
    },

    collapseMenu: function () {
        // TODO Make this a more visually fluent action.
//        this.getComponent('menu-main').collapse();
    }
});