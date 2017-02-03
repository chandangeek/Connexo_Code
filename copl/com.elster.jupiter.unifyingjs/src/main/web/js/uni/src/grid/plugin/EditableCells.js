/**
 * @class Uni.grid.plugin.EditableCells
 */
Ext.define('Uni.grid.plugin.EditableCells', {
    extend: 'Ext.AbstractPlugin',
    alias: 'plugin.editableCells',

    /**
     * @private
     */
    fieldsListeners: [],

    /**
     * @private
     */
    init: function (grid) {
        var me = this,
            gridView = grid.getView();

        gridView.on('refresh', me.updateCells, me);
        grid.on('destroy', function () {
            gridView.un('refresh', me.updateCells, me);
        }, me, {single: true});
    },

    /**
     * @private
     */
    updateCells: function (gridView) {
        var me = this,
            grid = gridView.up('gridpanel'),
            suffix = 0;

        me.fieldsListeners.map(function (listener) {
            listener.destroy();
        });

        if (grid.rendered && !gridView.isHidden()) {
            Ext.suspendLayouts();
            Ext.Array.each(grid.columns, function (column) {
                if (!column.isHidden() && column.getEl() && column.editor) {
                    Ext.each(gridView.getEl().query(gridView.getCellSelector(column)), function (el, index) {
                        var cell = Ext.get(el),
                            inner = cell.down('.' + Ext.baseCSSPrefix + 'grid-cell-inner'),
                            field,
                            record;

                        if (inner) {
                            inner.setHTML('');
                            record = gridView.getRecord(cell.up('.' + Ext.baseCSSPrefix + 'grid-data-row')),
                                field = Ext.widget(Ext.apply(column.editor,
                                    {
                                        renderTo: inner,
                                        constrain: true,
                                        value: record ? record.get(column.dataIndex) : null,
                                        itemId: column.dataIndex + '-' + column.editor.xtype + '-' + suffix++,
                                        cell: {
                                            record: record,
                                            dataIndex: column.dataIndex
                                        }
                                    }
                                ));

                            me.fieldsListeners.push(field.on({
                                change: me.onCellChange,
                                scope: me,
                                destroyable: true
                            }));
                        }
                    });
                }
            });

            Ext.resumeLayouts(true);
        }
    },

    onCellChange: function (field, newValue) {
        var me = this,
            record = field.cell.record,
            grid = me.getCmp(),
            store = grid.getStore();

        store.suspendEvent('update');
        record.set(field.cell.dataIndex, newValue);
        record.commit();
        store.resumeEvent('update');
        grid.fireEvent('edit', {
            field: field,
            value: newValue,
            record: record
        });
    }
});