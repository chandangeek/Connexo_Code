/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
    text: Uni.I18n.translate('general.columns', 'UNI', 'Columns'),
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
                itemId: 'column-picker-restore-defaults-button',
                text: Uni.I18n.translate('general.restoreDefaults', 'UNI', 'Restore defaults'),
                ui: 'link',
                handler: Ext.bind(me.restoreColumns, me)
            }],
            buttons: [
                {
                    text: Uni.I18n.translate('general.done', 'UNI', 'Done'),
                    itemId: 'column-picker-done-button',
                    handler: Ext.bind(me.changeColumns, me)
                },
                {
                    text: Uni.I18n.translate('general.cancel', 'UNI', 'Cancel'),
                    itemId: 'column-picker-cancel-button',
                    ui: 'link',
                    handler: function() {
                        me.menu.hide();
                    }
                }
            ],
            items: {
                xtype: 'checkboxgroup',
                padding: 5,
                itemId: 'columns-container',
                columns: 1,
                vertical: true,
                floating: false,
                defaults: {
                    checkDirty: Ext.emptyFn
                }
            },
            listeners: {
                beforeshow: {
                    scope: me,
                    fn: me.onMenuBeforeShow
                },
                show: {
                    scope: me,
                    fn: me.onMenuShow
                },
                hide: {
                    scope: me,
                    fn: me.onMenuHide
                }
            }
        };

        me.callParent(arguments);
    },

    createMenuItem: function (column) {
        return {
            xtype: 'checkbox',
            boxLabel: column.header,
            inputValue: column.dataIndex,
            disabled: column.disabled,
            checked: column.isDefault,
            column: column,
            handler: function (checkbox, checked) {
                checkbox.column.isDefault = checked;
            }
        };
    },

    setColumns: function (columns, restoreState) {
        var me = this;

        me.columns = columns;

        if (!restoreState) {
            me.defaultColumns = Ext.clone(columns);
            me.currentColumns = columns;
            me.grid.reconfigure(null, _.map(
                _.filter(columns, function(c){return c.isDefault}),
                function(c) {return Ext.apply(Ext.apply({}, c), {disabled:false})})
            );
            me.refreshConditionalToolTips();
        }
    },

    onMenuBeforeShow: function (menu) {
        var me = this,
            columnsContainer = menu.down('checkboxgroup'),
            columns = me.columns,
            sortedColumns = _.sortBy(columns, function (column) {return column.header}),
            groups = _.groupBy(sortedColumns, function (column) {return column.isDefault ? 'checked' : 'unchecked'});

        Ext.suspendLayouts();
        columnsContainer.removeAll();
        Ext.Array.each(groups.checked, function (column) {
            columnsContainer.add(me.createMenuItem(column));
        });
        if (groups.checked
            && groups.checked.length
            && groups.unchecked
            && groups.unchecked.length) {
            columnsContainer.add({xtype: 'menuseparator'});
        }
        Ext.Array.each(groups.unchecked, function (column) {
            columnsContainer.add(me.createMenuItem(column));
        });
        Ext.resumeLayouts(true);
    },

    onMenuShow: function () {
        var me = this;

        me.currentColumns = Ext.clone(me.columns);
    },

    onMenuHide: function () {
        var me = this;

        me.setColumns(me.currentColumns, true);
    },

    changeColumns: function() {
        var me = this,
            menu = me.menu,
            grid = me.grid,
            currentColumns = grid.getView().getHeaderCt().items,
            newColumns = _.filter(me.columns, function (column) {return column.isDefault}),
            toAdd = [];

        if (newColumns.length) {
            currentColumns.each(function (currentColumn) {
                Ext.Array.every(newColumns, function (newColumn, index) {
                    if (currentColumn.dataIndex === newColumn.dataIndex) {
                        toAdd.push(Ext.clone(newColumn));
                        Ext.Array.remove(newColumns, newColumn);
                        return false;
                    }
                    return true;
                })
            });
        }
        Ext.Array.push(toAdd, newColumns);
        grid.reconfigure(null, _.map(toAdd, function(c) {
            return Ext.apply(Ext.apply({}, c), {disabled:false})
        }));
        me.refreshConditionalToolTips();

        menu.suspendEvent('hide');
        menu.hide();
        menu.resumeEvent('hide');
    },

    restoreColumns: function() {
        var me = this;

        Ext.suspendLayouts();
        me.setColumns(me.defaultColumns);
        me.menu.hide();
        Ext.resumeLayouts(true);
        me.refreshConditionalToolTips();
    },

    refreshConditionalToolTips: function () {
        var me = this,
            grid = me.grid,
            view = grid.getView(),
            gridPanel = view.up('gridpanel');

        if (gridPanel && grid.getPlugin('showConditionalToolTipId')) {
            gridPanel.columns = gridPanel.columnManager.getColumns();
            grid.getPlugin('showConditionalToolTipId').setTooltip(view);
        }
    }
});