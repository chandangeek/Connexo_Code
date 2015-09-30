/**
 * @class Uni.view.search.ColumnPicker
 */
Ext.define('Uni.view.search.ColumnPicker', {
    extend: 'Ext.button.Button',
    requires: [
        'Ext.grid.Panel'
    ],

    mixins: [
        'Ext.util.Bindable'
    ],

    xtype: 'uni-search-column-picker',
    text: 'Columns',
    arrowAlign: 'right',
    menuAlign: 'tr-br',

    initComponent: function () {
        var me = this;

        me.menu = {
            plain: true,
            padding: 0,
            //overflowY: 'auto',
            autoScroll: true,
            maxHeight: 400,
            defaults: {
                margin: 0
            },
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            //items: {
            //    xtype: 'container',
            //    maxHeight: 400,
            //    overflowY: 'auto',
            //    layout: 'vbox',
                items: [
                    {
                        xtype: 'menu',
                        itemId: 'columns-selected',
                        //plain: true,
                        floating: false,
                        listeners: {
                            click: function(menu, item) {
                                menu.remove(item);
                                var newItem = me.createMenuItem(item.column);
                                newItem.checked = false;
                                me.menu.down('#columns-available').add(newItem);
                                me.grid.reconfigure(null, _.pluck(menu.items.getRange(), 'column'));
                            }
                        }
                    },
                    {
                        xtype: 'menuseparator'
                    },
                    {
                        xtype: 'menu',
                        itemId: 'columns-available',
                        //plain: true,
                        floating: false,
                        listeners: {
                            click: function(menu, item) {
                                menu.remove(item);
                                var newItem = me.createMenuItem(item.column);
                                newItem.checked = true;
                                me.menu.down('#columns-selected').add(newItem);
                                me.grid.reconfigure(null, _.pluck(me.menu.down('#columns-selected').items.getRange(), 'column'));
                            }
                        }
                    }
                ]
            //}
        };

        me.callParent(arguments);
    },

    createMenuItem: function (column) {
        return {
            xtype: 'menucheckitem',
            text: column.header,
            value: column.dataIndex,
            default: column.default,
            checked: column.default,
            column: column
        };
    },

    setColumns: function (columns) {
        var me = this,
            available = me.menu.down('#columns-available'),
            selected = me.menu.down('#columns-selected')
        ;

        Ext.suspendLayouts();
        available.removeAll();
        selected.removeAll();

        if (columns.length) {
            Ext.each(columns, function (item) {
                var menuItem = me.createMenuItem(item);
                menuItem.default ? selected.add(menuItem) : available.add(menuItem);
            });
        }

        me.grid.reconfigure(null, _.filter(columns, function(c){return c.default}));
        Ext.resumeLayouts(true);
    }
});