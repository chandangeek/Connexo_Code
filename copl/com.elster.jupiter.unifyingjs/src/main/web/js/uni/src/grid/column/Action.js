/**
 * @class Uni.grid.column.Action
 */
Ext.define('Uni.grid.column.Action', {
    extend: 'Ext.grid.column.Action',
    alias: 'widget.uni-actioncolumn',

    header: Uni.I18n.translate('general.actions', 'UNI', 'Actions'),
    width: 100,
    align: 'center',
    iconCls: 'icon-cog2',

    menu: {
        defaultAlign: 'tr-br?',
        plain: true,
        items: []
    },

    // Renderer closure iterates through items creating an <img> element for each and tagging with an identifying
    // class name x-action-col-{n}
    defaultRenderer: function (v, meta, record, rowIdx, colIdx, store, view) {
        var me = this,
            prefix = Ext.baseCSSPrefix,
            scope = me.origScope || me,
            items = me.items,
            len = items.length,
            i = 0,
            item, ret, disabled, tooltip;

        if (Ext.isFunction(me.showCondition) && !me.showCondition(record)) {
            return ''
        }
        // Allow a configured renderer to create initial value (And set the other values in the "metadata" argument!)
        // Assign a new variable here, since if we modify "v" it will also modify the arguments collection, meaning
        // we will pass an incorrect value to getClass/getTip
        ret = Ext.isFunction(me.origRenderer) ? me.origRenderer.apply(scope, arguments) || '' : '';

        meta.tdCls += ' ' + Ext.baseCSSPrefix + 'action-col-cell';
        for (; i < len; i++) {
            item = items[i];

            disabled = item.disabled || (item.isDisabled ? item.isDisabled.call(item.scope || scope, view, rowIdx, colIdx, item, record) : false);
            tooltip = disabled ? null : (item.tooltip || (item.getTip ? item.getTip.apply(item.scope || scope, arguments) : null));

            // Only process the item action setup once.
            if (!item.hasActionConfiguration) {

                // Apply our documented default to all items
                item.stopSelection = me.stopSelection;
                item.disable = Ext.Function.bind(me.disableAction, me, [i], 0);
                item.enable = Ext.Function.bind(me.enableAction, me, [i], 0);
                item.hasActionConfiguration = true;
            }

            ret += '<span style="font-size: 18px; line-height: 17px" class="' + me.iconCls + ' ' + me.actionIconCls + ' ' + prefix + 'action-col-' + String(i) + ' ' + (disabled ? prefix + 'item-disabled ' : ' ') +
                (Ext.isFunction(item.getClass) ? item.getClass.apply(item.scope || scope, arguments) : (item.iconCls || me.iconCls || '')) + '"' +
                (tooltip ? ' data-qtip="' + tooltip + '"' : '') + ' ></span>';
        }
        return ret;
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
            if (item && item.action && !Ext.isObject(item.action)) {
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