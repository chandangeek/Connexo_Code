Ext.define('Uni.view.navigation.Menu', {
    extend: 'Ext.container.Container',
    alias: 'widget.navigationMenu',
    requires: [
        'Ext.layout.container.Card'
    ],
    title: 'Menu',
    cls: 'nav-menu',
    layout: {
        type: 'hbox',
        align: 'stretch'
    },

    collapsed: false,
    collapsedWidth: 55,
    expandedWidth: 220,

    initComponent: function () {
        var me = this;

        me.setWidth(me.expandedWidth);

        me.items = [
            {
                xtype: 'container',
                itemId: 'menu-main',
                cls: 'nav-menu-main',
                width: me.expandedWidth,
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                defaults: {
                    xtype: 'button',
                    hrefTarget: '_self',
                    toggleGroup: 'menu-items',
                    enableToggle: true,
                    allowDepress: false,
                    cls: 'menu-item',
                    tooltipType: 'title',
                    scale: 'large'
                }
            },
            {
                xtype: 'container',
                itemId: 'menu-sub',
                cls: 'nav-menu-sub',
                flex: 1,
                layout: {
                    type: 'card',
                    deferredRender: true
                }
            }
        ];

        this.callParent(arguments);
    },

    removeAllMenuItems: function () {
        var mainMenu = this.getComponent('menu-main');
        mainMenu.removeAll();
    },

    addMenuItem: function (model) {
        var me = this,
            item = me.createMenuItemFromModel(model),
            mainMenu = me.getComponent('menu-main'),
            subMenu = me.getComponent('menu-sub');

        // TODO Support adding child menu items.

        var children = model.children().data.items;
        if (children.length > 0) {
            var card = me.createSubMenu();

            for (var i = 0; i < model.children().data.length; i++) {
                var child = model.children().data.items[i];
                card.add(me.createMenuItemFromModel(child));
            }

            subMenu.add(card);
            // TODO Show an icon that indicates there is a sub menu.
        } else {
            // TODO
        }

        // TODO Sort the buttons on their model's index value, instead of relying on insert.
        if (model.data.index == '' || model.data.index == null || model.data.index === undefined) {
            mainMenu.add(item);
        } else {
            mainMenu.insert(model.data.index, item);
        }
    },

    createMenuItemFromModel: function (model) {
        return {
            text: model.data.text,
            tooltip: model.data.text,
            href: model.data.href,
            glyph: model.data.glyph,
            data: model
        };
    },

    createSubMenu: function () {
        return Ext.create('Ext.container.Container', {
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            defaults: {
                xtype: 'button',
                hrefTarget: '_self',
                toggleGroup: 'menu-items-sub',
                enableToggle: true,
                allowDepress: false,
                cls: 'menu-item',
                tooltipType: 'title',
                scale: 'large'
            }
        });
    },

    // TODO Use CSS classes to expand/collapse the menu.

    collapse: function () {
        this.collapsed = true;
        this.setWidth(this.collapsedWidth);
    },

    expand: function () {
        this.collapsed = false;
        this.setWidth(this.expandedWidth);
    },

    collapseSub: function () {
        var mainMenu = this.getComponent('menu-main');
        mainMenu.setWidth(this.expandedWidth);
    },

    expandSub: function () {
        var mainMenu = this.getComponent('menu-main');
        mainMenu.setWidth(this.collapsedWidth);
    },

    selectMenuItem: function (model) {
        var me = this,
            itemId = model.id,
            mainMenu = me.getComponent('menu-main');

        // TODO Support selecting a sub menu.
        for (var i = 0; i < mainMenu.items.items.length; i++) {
            var menuItem = mainMenu.items.items[i];
            if (itemId == menuItem.data.id) {
                menuItem.toggle(true, false);
                break;
            }
        }
    }
});