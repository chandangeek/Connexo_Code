/**
 * @class Uni.grid.column.Action
 */
Ext.define('Uni.grid.column.Action', {
    extend: 'Ext.grid.column.Action',
    alias: 'widget.uni-actioncolumn',

    header: 'Actions',
    width: 100,
    align: 'left',
    iconCls: 'x-uni-action-icon',

    menu: {
        defaultAlign: 'tr-br?',
        plain: true,
        items: []
    },

    constructor: function(config) {
        var me = this,
            cfg = Ext.apply({}, config);

        // workaround to work with menu instance
        if (_.isString(cfg.items)) {
            var menu = Ext.ClassManager.get(cfg.items);
            Ext.apply(me.menu.items, menu.prototype.items);
        } else {
            Ext.apply(me.menu.items, cfg.items);
        }

        cfg.items = null;
        me.callParent([cfg]);

        this.initMenu();
    },

    /**
     * @private
     */
    initMenu: function () {
        var me = this;
        me.menu = Ext.widget('menu', me.menu);
        me.menu.on('click', function(menu, item, e, eOpts) {
            me.fireEvent('menuclick', menu, item, e, eOpts);
            if (item.action) {
                me.fireEvent(item.action, menu.record);
            }
        });
    },

    handler: function(grid, rowIndex, colIndex) {
        var me = this;
        var record = grid.getStore().getAt(rowIndex);
        var cell = grid.getCellByPosition({row: rowIndex, column: colIndex});

        cell.addCls('active');

        me.menu.record = record;
        me.menu.showBy(cell);
        me.menu.on('hide', function(){
            cell.removeCls('active');
        });
    }
});