/**
 * @class Uni.grid.column.Action
 */
Ext.define('Uni.grid.column.Action', {
    extend: 'Ext.grid.column.Action',
    alias: 'widget.uni-actioncolumn',

    header: 'Actions',
    width: 100,
    align: 'left',
    iconCls: ' uni-actioncolumn-gear',

    menu: {
        defaultAlign: 'tr-br?',
        plain: true,
        items: []
    },

    constructor: function (config) {
        var me = this,
            cfg = Ext.apply({}, config);

        // reset the items for new menu instance.
        me.menu.items = [];

        // workaround to work with menu instance
        if (_.isString(cfg.items)) {
            var menu = Ext.ClassManager.get(cfg.items);
            Ext.apply(me.menu.items, menu.prototype.items);
        } else {
            Ext.apply(me.menu.items, cfg.items);
        }

        cfg.items = null;
        me.callParent([cfg]);

        me.initMenu();
    },

    /**
     * @private
     */
    initMenu: function () {
        var me = this,
            menuXtype = me.menu.xtype;
        menuXtype == null ? menuXtype = 'menu' : null;
        me.menu = Ext.widget(menuXtype, me.menu);
        me.menu.on('click', function (menu, item, e, eOpts) {
            me.fireEvent('menuclick', menu, item, e, eOpts);
            if (item.action && !Ext.isObject(item.action)) {

                me.fireEvent(item.action, menu.record);
            }
        });
    },
  
    handler: function (grid, rowIndex, colIndex, item, e, record) {
        var me = this,
            rec,
            cell = grid.getCellByPosition({row: rowIndex, column: colIndex}),
            selectionModel = grid.getSelectionModel(),
            selection = selectionModel.getSelection(),
            selectedRecord;
        rec = record;
        if(rec === undefined){
            rec = grid.getStore().getAt(rowIndex);
        }

        if (selection.length > 0) {
            selectedRecord = selection[0];

            if (grid.getStore().indexOf(selectedRecord) !== rowIndex) {
                selectionModel.select(rowIndex);
            }
        } else if (selection.length === 0 && grid.getStore().getCount() > 0) {
            selectionModel.select(rowIndex);
        }

        if (me.menu.cell === cell) {
            me.menu.hide();
            me.menu.cell = null;
        } else {
            if(me.fireEvent('beforeshow', grid, cell, colIndex, rowIndex, rec)) {
                cell.addCls('active');
                me.menu.record = rec;
                me.menu.showBy(cell);
                me.menu.cell = cell;
            }
        }

        // this is for menu toggling, change the code below with accuracy!
        me.menu.on('hide', function () {
            var actions = grid.getEl().query(me.iconCls + ':hover');
            if (!actions.length) {
                me.menu.cell = null;
            }
            cell.removeCls('active');
        });
    }
});