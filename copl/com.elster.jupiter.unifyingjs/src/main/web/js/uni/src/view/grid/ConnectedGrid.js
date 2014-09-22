/**
 * @class Uni.view.grid.ConnectedGrid
 *
 * This connected grid component is used when we have list of some items,
 * and wanted to choose only several of them.
 * Two grid panels are created, and we can move items from one grid to another using buttons, or drag'n'drop.
 *
 * Example:
 *
 * {
 *   xtype: 'fieldcontainer',
 *       fieldLabel: Uni.I18n.translate('comtask.messages', 'MDC', 'Messages'),
 *   labelWidth: 200,
 *   items:[
 *     {
 *       xtype: 'displayfield',
 *       value: Uni.I18n.translate('comtask.messages.text', 'MDC', 'Send pending messages of these message categories every time this communication task executes')
 *     },
 *     {
 *       xtype: 'connected-grid',
 *       allItemsTitle: Uni.I18n.translate('comtask.message.cathegories', 'MDC', 'Message categories'),
 *       allItemsStoreName: 'Mdc.store.MessageCategories',
 *       selectedItemsTitle: Uni.I18n.translate('comtask.selected.message.cathegories', 'MDC', 'Selected message categories'),
 *       selectedItemsStoreName: 'Mdc.store.SelectedMessageCategories',
 *       displayedColumn: 'name'
 *     }
 *   ]
 * },
 *
 */


Ext.define('Uni.view.grid.ConnectedGrid', {
    extend: 'Ext.container.Container',
    xtype: 'connected-grid',

    layout: {
        type: 'hbox'
    },

    allItemsTitle: null,

    allItemsStoreName: null,

    selectedItemsTitle: null,

    selectedItemsStoreName: null,

    displayedColumn: null,


    initComponent: function () {
        var me = this,
            allItems = me.id + 'allItemsGrid',
            selectedItems = me.id + 'selectedItemsGrid';


        if (Ext.isEmpty(me.displayedColumn)) {
            me.displayedColumn = 'name';
        }

        me.items = [
            {
                xtype: 'gridpanel',
                itemId: 'allItemsGrid',
                store: me.allItemsStoreName,
                title: me.allItemsTitle,
                hideHeaders: true,
                selModel: {
                    mode: "MULTI"
                },
                columns: [
                    {
                        dataIndex: me.displayedColumn,
                        flex: 1
                    }
                ],
                viewConfig: {
                    plugins: {
                        ptype: 'gridviewdragdrop',
                        dragGroup: allItems,
                        dropGroup: selectedItems
                    },
                    listeners: {
                        drop: function (node, data, dropRec, dropPosition) {
                            me.getAllItemsStore().sort(me.displayedColumn, 'ASC');
                        }
                    }
                },
                height: 400,
                width: 200
            },
            {
                xtype: 'container',
                margin: '0 10',
                layout: {
                    type: 'vbox',
                    align: 'center',
                    pack: 'center'
                },
                defaults: {
                    margin: '5'
                },
                items: [
                    {
                        xtype: 'container',
                        height: 100
                    },
                    {
                        xtype: 'button',
                        itemId: 'selectAllItems',
                        width: 50,
                        text: '>>',
                        handler: function () {
                            me.selectAllItems();
                        }
                    },
                    {
                        xtype: 'button',
                        itemId: 'selectItems',
                        width: 50,
                        text: '>',
                        handler: function () {
                            me.selectItems();
                        }
                    },
                    {
                        xtype: 'button',
                        itemId: 'deselectItems',
                        width: 50,
                        text: '<',
                        handler: function () {
                            me.deselectItems();

                        }
                    },
                    {
                        xtype: 'button',
                        itemId: 'deselectAllItems',
                        width: 50,
                        text: '<<',
                        handler: function () {
                            me.deselectAllItems();
                        }
                    }
                ]
            },
            {
                xtype: 'gridpanel',
                itemId: 'selectedItemsGrid',
                store: me.selectedItemsStoreName,
                title: me.selectedItemsTitle,
                hideHeaders: true,
                selModel: {
                    mode: "MULTI"
                },
                columns: [
                    {
                        dataIndex: me.displayedColumn,
                        flex: 1
                    }
                ],
                viewConfig: {
                    plugins: {
                        ptype: 'gridviewdragdrop',
                        dragGroup: selectedItems,
                        dropGroup: allItems
                    },
                    listeners: {
                        drop: function (node, data, dropRec, dropPosition) {
                            me.getSelectedItemsStore().sort(me.displayedColumn, 'ASC');
                        }
                    }
                },
                height: 400,
                width: 200
            }
        ];

        me.callParent(arguments);
    },

    getAllItemsGrid: function () {
        return this.down('#allItemsGrid');
    },

    getSelectedItemsGrid: function () {
        return this.down('#selectedItemsGrid');
    },

    getAllItemsStore: function () {
        var allItemsGrid = this.getAllItemsGrid();

        if (allItemsGrid) {
            return allItemsGrid.getStore();
        } else {
            return null;
        }
    },

    getSelectedItemsStore: function () {
        var selectedItemsGrid = this.getSelectedItemsGrid();

        if (selectedItemsGrid) {
            return selectedItemsGrid.getStore();
        } else {
            return null;
        }
    },

    selectAllItems: function () {
        var allItemsStore = this.getAllItemsStore(),
            selectedItemsStore = this.getSelectedItemsStore();

        if (allItemsStore && selectedItemsStore) {
            allItemsStore.each(function (record) {
                selectedItemsStore.add(record);
            });
            allItemsStore.removeAll();
        }

        selectedItemsStore.sort(this.displayedColumn, 'ASC');
    },

    selectItems: function () {
        var allItemsGrid = this.getAllItemsGrid(),
            selectedRecords = allItemsGrid.getSelectionModel().getSelection(),
            allItemsStore = this.getAllItemsStore(),
            selectedItemsStore = this.getSelectedItemsStore();

        if (allItemsStore && selectedItemsStore) {
            Ext.Array.each(selectedRecords, function (record) {
                allItemsStore.remove(record);
                selectedItemsStore.add(record);
            });
        }

        selectedItemsStore.sort(this.displayedColumn, 'ASC');
    },

    deselectItems: function () {
        var selectedItemsGrid = this.getSelectedItemsGrid(),
            selectedRecords = selectedItemsGrid.getSelectionModel().getSelection(),
            allItemsStore = this.getAllItemsStore(),
            selectedItemsStore = this.getSelectedItemsStore();

        if (allItemsStore && selectedItemsStore) {
            Ext.Array.each(selectedRecords, function (record) {
                allItemsStore.add(record);
                selectedItemsStore.remove(record);
            });
        }

        allItemsStore.sort(this.displayedColumn, 'ASC');
    },

    deselectAllItems: function () {
        var allItemsStore = this.getAllItemsStore(),
            selectedItemsStore = this.getSelectedItemsStore();

        if (allItemsStore && selectedItemsStore) {
            selectedItemsStore.each(function (record) {
                allItemsStore.add(record);
            });
            selectedItemsStore.removeAll();
        }

        allItemsStore.sort(this.displayedColumn, 'ASC');
    }
});