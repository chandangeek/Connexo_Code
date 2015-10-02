/**
 * @class Uni.view.search.ColumnPicker
 * todo: translations
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
            xtype: 'menu',
            plain: true,
            padding: 0,
            defaults: {
                margin: 0
            },
            floating: true,
            maxHeight: 400,
            width: 300,
            overflowY: 'auto',
            style: 'background:white; border-radius: 8px 0 8px 8px',
            bodyStyle: 'background:white',
            onMouseOver: Ext.emptyFn,
            layout: {
                type: 'vbox',
                align: 'stretch',
                overflowHandler: 'None'
            },
            tbar: [{
                xtype: 'button',
                text: 'Restore defaults',
                ui: 'link',
                handler: function() {
                    me.setColumns(me.defaultColumns);
                }
            }],
            buttons: [
                {
                    text: 'Done',
                    handler: function() {
                        me.grid.reconfigure(null, _.pluck(me.menu.down('#columns-selected').items.getRange(), 'column'));
                    }
                },
                {
                    text: 'Cancel',
                    ui: 'link',
                    handler: function() {
                        me.menu.hide();
                    }
                }
            ],
            items: [
                {
                    xtype: 'checkboxgroup',
                    padding: 5,
                    itemId: 'columns-selected',
                    columns: 1,
                    vertical: true,
                    //plain: true,
                    floating: false,
                    defaults: {
                        checkDirty: Ext.emptyFn,
                        listeners: {
                            change: function(item) {
                                var newItem = me.createMenuItem(item.column);
                                newItem.checked = false;
                                Ext.suspendLayouts();
                                me.menu.down('#columns-selected').remove(item);
                                me.menu.down('#columns-available').add(newItem);
                                Ext.resumeLayouts(true);
                                return false;
                            }
                        }
                    }

                },
                {
                    xtype: 'menuseparator'
                },
                {
                    xtype: 'checkboxgroup',
                    padding: 5,
                    columns: 1,
                    vertical: true,
                    itemId: 'columns-available',
                    //plain: true,
                    floating: false,
                    defaults: {
                        checkDirty: Ext.emptyFn,
                        listeners: {
                            change: function(item) {
                                var newItem = me.createMenuItem(item.column);
                                newItem.checked = true;
                                Ext.suspendLayouts();
                                me.menu.down('#columns-selected').add(newItem);
                                me.menu.down('#columns-available').remove(item);
                                Ext.resumeLayouts(true);
                                return false;
                            }
                        }
                    }
                }
            ]
        };

        me.callParent(arguments);
    },

    createMenuItem: function (column) {
        return {
            xtype: 'checkbox',
            boxLabel: column.header,
            inputValue: column.dataIndex,
            disabled: column.disabled,
            checked: column.default,
            column: column
        };
    },

    setColumns: function (columns) {
        var me = this,
            available = me.menu.down('#columns-available'),
            selected = me.menu.down('#columns-selected')
        ;

        me.defaultColumns = columns;
        Ext.suspendLayouts();
        available.removeAll();
        selected.removeAll();

        if (columns.length) {
            Ext.each(columns, function (item) {
                var menuItem = me.createMenuItem(item);
                menuItem.checked ? selected.add(menuItem) : available.add(menuItem);
            });
        }

        me.grid.reconfigure(null, _.filter(columns, function(c){return c.default}));
        Ext.resumeLayouts(true);
    }
});