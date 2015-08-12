/**
 * @class Uni.view.search.field.Selection
 */
Ext.define('Uni.view.search.field.Selection', {
    extend: 'Uni.view.search.field.internal.CriteriaButton',
    requires: [
        'Ext.grid.Panel',
        'Uni.view.search.field.internal.Input'
    ],

    mixins: [
        'Ext.util.Bindable'
    ],

    xtype: 'search-combo',

    updateButtonText: function () {
        return this.selection.length
            ? this.setText(this.emptyText + '&nbsp;(' + this.selection.length + ')')
            : this.setText(this.emptyText);
    },

    store: null,
    menuConfig: {
        width: 300,
        maxHeight: 600
    },

    onSelectionChange: function () {
        var me = this;
        me.onChange(me, me.selection.getRange().map(function(item){
            return item.get(me.valueField)
        }));
        me.down('#filter-selected').setDisabled(!me.selection.length);
    },

    reset: function() {
        this.selection.clear();
        this.down('search-criteria-input').reset();
        this.getStore().clearFilter(true);
        this.grid.getSelectionModel().deselectAll();
        this.callParent(arguments);
    },

    initComponent: function () {
        var me = this,
            selection = me.selection = Ext.create('Ext.util.MixedCollection', {
                listeners: {
                    add: {
                        fn: me.onSelectionChange,
                        scope: me
                    },
                    remove: {
                        fn: me.onSelectionChange,
                        scope: me
                    },
                    clear: {
                        fn: me.onSelectionChange,
                        scope: me
                    }
                }
            });

        me.items = {
            xtype: 'grid',
            layout: 'fit',
            columns: {
                style: {
                    borderRadius: '0'
                },
                items: {
                    dataIndex: me.displayField,
                    flex: 1
                }
            },
            dockedItems: {
                defaults: {
                    xtype: 'toolbar',
                    dock: 'top',
                    margin: 0,
                    padding: 0
                },
                padding: 5,
                items: [
                    {
                        xtype: 'toolbar',
                        layout: 'hbox',
                        items: [
                            {
                                itemId: 'filter-operator',
                                xtype: 'combo',
                                value: '=',
                                width: 50,
                                margin: '0 5 0 0',
                                disabled: true
                            },
                            {
                                xtype: 'search-criteria-input',
                                tooltip: 'Specify filter to narrow down selection list. Maximum 100 records are displayed.',
                                emptyText: 'Start typing to find devices...',
                                listeners: {
                                    change: function (elm, value) {
                                        var store = me.grid.getStore();
                                        Ext.suspendLayouts();
                                        if (store.remoteFilter) {
                                            if (Ext.isEmpty(value)) {
                                                store.clearFilter();
                                            } else {
                                                store.filter({
                                                    id: me.displayField,
                                                    property: me.displayField,
                                                    value: value
                                                });
                                            }
                                        } else {
                                            store.clearFilter(true);
                                            me.enableRegEx
                                                ? store.filter(me.displayField, new RegExp(value))
                                                : store.filter(me.displayField, value);
                                        }

                                        me.grid.down('#filter-clear').setVisible(!!value);
                                        Ext.resumeLayouts(true);
                                    }
                                }
                            }
                        ]
                    },
                    {
                        xtype: 'checkboxfield',
                        itemId: 'select-all',
                        hidden: !me.multiSelect,
                        boxLabel: 'Select all displayed values',
                        name: 'topping',
                        inputValue: '1',
                        handler: function (elm, value) {
                            var selectionModel = me.grid.getSelectionModel();
                            // Prevent focus changes on the view, since we're selecting/deselecting all records
                            selectionModel.preventFocus = true;
                            selection.suspendEvents();
                            if (!value) {
                                selectionModel.deselectAll();
                            } else {
                                selectionModel.selectAll();
                            }
                            selection.resumeEvents();
                            me.onSelectionChange();
                            delete selectionModel.preventFocus;
                        }
                    },
                    {
                        xtype: 'checkboxfield',
                        boxLabel: 'Include empty values',
                        disabled: true,
                        name: 'topping'
                    },
                    {
                        xtype: 'checkboxfield',
                        itemId: 'filter-selected',
                        boxLabel: 'Show all selected values',
                        disabled: true,
                        hidden: !me.multiSelect,
                        handler: function () {
                            var store = me.grid.getStore();
                            Ext.suspendLayouts();

                            if (this.checked) {
                                //store.clearFilter(true);
                                if (store.remoteFilter) {
                                    store.removeAll();
                                    store.add(selection.getRange());
                                } else {
                                    store.filter({
                                        filterFn: function (item) {
                                            return selection.getRange().indexOf(item) >= 0;
                                        }
                                    });
                                }
                            } else {
                                store.removeFilter(me.displayField, false);
                                store.load();
                            }
                            Ext.resumeLayouts(true);
                        }
                    }
                ]
            },
            selModel: {
                store: me.store,
                selType: 'checkboxmodel',
                mode: me.multiSelect ? 'SIMPLE' : 'SINGLE',
                toggleUiHeader: function(isChecked) {
                    me.grid.down('#select-all').setRawValue(isChecked);
                },
                onStoreAdd: function() {
                    this.superclass.onStoreAdd.apply(this);
                    this.select(_.intersection(this.getStore().getRange(), selection.getRange()), true, true);
                    this.updateHeaderState();
                },
                onStoreLoad: function () {
                    this.superclass.onStoreLoad.apply(this);
                    this.select(selection.getRange(), true, true);
                    this.updateHeaderState();
                },
                onStoreRefresh: function () {
                    this.superclass.onStoreRefresh.apply(this);
                    this.select(_.intersection(this.getStore().getRange(), selection.getRange()), true, true);
                    this.updateHeaderState();
                },
                listeners: {
                    beforeselect: function (s, record) {
                        selection.add(record);
                    },
                    beforedeselect: function (s, record) {
                        selection.remove(record);
                    }

                }
            },

            hideHeaders: true,
            header: false,
            title: false,
            frame: false,
            padding: 0,
            margin: 0,
            border: false,
            store: me.store,
            focusOnToFront: false,
            pageSize: me.pageSize,
            maxHeight: 450,
            style: {
                border: 'none'
            },
            viewConfig: {
                width: '100%'
            },
            bodyStyle: {
                borderWidth: '1px 0 0 0 !important',
                borderRadius: '0 0 8px 8px',
                border: 'none'
            }
        };

        me.callParent(arguments);
        me.bindStore(me.store || 'ext-empty-store', true);
        me.grid = me.down('grid');
        me.on('menushow', function(){
            me.store.load();
        })
    }
});