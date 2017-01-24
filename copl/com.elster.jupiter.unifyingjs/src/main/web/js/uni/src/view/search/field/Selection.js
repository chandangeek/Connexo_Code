/**
 * @class Uni.view.search.field.Selection
 *
 //{
//    xtype: 'search-combo',
//    itemId: 'domain',
//    store: Ext.create('Ext.data.Store', {
//        fields: ['name', 'value'],
//        data: [
//            {'name': 'SPE010000010079', 'value': '1'},
//            {'name': 'SPE010000010080', 'value': '2'},
//            {'name': 'SPE010000010081', 'value': '1'},
//            {'name': 'SPE010000010082', 'value': '2'},
//            {'name': 'SPE010000010083', 'value': '1'},
//            {'name': 'SPE010000010084', 'value': '2'},
//            {'name': 'SPE010000010085', 'value': '1'},
//            {'name': 'SPE010000010086', 'value': '2'},
//            {'name': 'SPE010000010087', 'value': '1'},
//            {'name': 'DME010000010088', 'value': '2'},
//            {'name': 'DME010000010079', 'value': '1'},
//            {'name': 'DME010000010080', 'value': '2'},
//            {'name': 'DME010000010081', 'value': '1'},
//            {'name': 'DME010000010082', 'value': '2'},
//            {'name': 'DME010000010083', 'value': '1'},
//            {'name': 'DME010000010084', 'value': '2'},
//            {'name': 'DME010000010085', 'value': '1'},
//            {'name': 'DME010000010086', 'value': '2'},
//            {'name': 'DME010000010087', 'value': '1'},
//            {'name': 'DME010000010088', 'value': '2'}
//        ],
//        limit: 10
//    }),
//    text: 'mRID',
//    displayField: 'name',
//    valueField: 'id',
//    margin: '0 20 10 0',
//    forceSelection: true,
//    multiSelect: true
//},
 *
 */
Ext.define('Uni.view.search.field.Selection', {
    extend: 'Uni.view.search.field.internal.Criteria',
    requires: [
        'Ext.grid.Panel',
        'Uni.view.search.field.internal.Input',
        'Uni.view.search.field.internal.Operator',
        'Uni.model.search.Value'
    ],

    mixins: [
        'Ext.util.Bindable'
    ],

    xtype: 'uni-search-criteria-selection',
    store: null,
    minWidth: 300,
    setValue: function (value) {
        var me = this,
            store = me.getStore(),
            selection = me.selection;

        if (value && value[0]) {
            var records = _.filter(_.map(value[0].get('criteria'), function (id) {
                return store.getById(id);
            }), function (r) {
                return r !== null
            });

            selection.suspendEvents();
            selection.removeAll();
            selection.add(records);
            selection.resumeEvents();
        }
    },

    getValue: function () {
        var me = this, value;

        me.selection.sort();
        value = me.selection.getRange().map(function (item) {
            return item.get(me.valueField)
        });

        return value.length ? [Ext.create('Uni.model.search.Value', {
            operator: this.down('#filter-operator').getValue(),
            criteria: value
        })] : null
    },

    onChange: function () {
        var me = this,
            filterSelected = me.down('#filter-selected');

        me.onValueChange();

        if (me.grid.rendered) {
            if (filterSelected.getValue()) {
                me.syncSelection(me.grid.getSelectionModel().getCount() ? true : false);
            }

            me.syncView();
        }
    },

    reset: function () {
        this.selection.clear();
        this.down('#filter-input').reset();
        this.getStore().clearFilter(true);
        this.grid.getSelectionModel().deselectAll();
        this.callParent(arguments);
    },

    initComponent: function () {
        var me = this,
            selection = me.selection = Ext.create('Ext.util.MixedCollection', {
                listeners: {
                    add: {
                        fn: me.onChange,
                        scope: me
                    },
                    remove: {
                        fn: me.onChange,
                        scope: me
                    },
                    clear: {
                        fn: me.onChange,
                        scope: me
                    }
                },
                sorters: [{
                    direction: 'ASC',
                    property: 'displayValue',
                    root: 'data'
                }]
            });

        me.items = {
            xtype: 'grid',
            itemId: 'grid-selection',
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
                itemId: 'docked-items',
                padding: 5,
                items: [
                    {
                        xtype: 'toolbar',
                        itemId: 'toolbar',
                        layout: 'hbox',
                        items: [
                            {
                                itemId: 'filter-operator',
                                xtype: 'uni-search-internal-operator',
                                value: '==',
                                margin: '0 5 0 0',
                                operators: [
                                    '=='
                                    //'!='
                                ],
                                listeners: {
                                    change: {
                                        fn: me.onChange,
                                        scope: me
                                    }
                                }
                            },
                            {
                                xtype: 'uni-search-internal-input',
                                itemId: 'filter-input',
                                tooltip: Uni.I18n.translate('search.field.selection.filter.tooltip', 'UNI', 'Specify filter to narrow down selection list'),
                                emptyText: Uni.I18n.translate('search.field.selection.filter.placeholder', 'UNI', 'Start typing to find {0}...', [me.emptyText]),
                                listeners: {
                                    change: function (elm, value) {
                                        var store = me.grid.getStore();
                                        Ext.suspendLayouts();
                                        if (store.remoteFilter) {
                                            store.filter({
                                                id: me.displayField,
                                                property: me.displayField,
                                                value: value
                                            });
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
                        boxLabel: Uni.I18n.translate('search.field.selection.checkbox.select-all', 'UNI', 'Select all displayed values'),
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
                            me.onChange();

                            delete selectionModel.preventFocus;
                        }
                    },
                    {
                        xtype: 'checkboxfield',
                        itemId: 'empty-values',
                        boxLabel: Uni.I18n.translate('search.field.selection.checkbox.empty', 'UNI', 'Include empty values'),
                        disabled: true,
                        name: 'topping'
                    },
                    {
                        xtype: 'checkboxfield',
                        itemId: 'filter-selected',
                        boxLabel: Uni.I18n.translate('search.field.selection.checkbox.show-selected', 'UNI', 'Show all selected values'),
                        disabled: true,
                        hidden: !me.multiSelect,
                        listeners: {
                            change: function () {
                                var store = me.grid.getStore(),
                                    input = me.down('#filter-input');

                                Ext.suspendLayouts();

                                input.suspendEvent('change');
                                input.reset();
                                input.resumeEvent('change');
                                store.filters.removeAtKey(me.displayField);
                                me.syncSelection(this.checked);

                                Ext.resumeLayouts(true);
                            }
                        }
                    }
                ]
            },
            selModel: {
                store: me.store,
                selType: me.multiSelect ? 'checkboxmodel' : 'rowmodel',
                mode: me.multiSelect ? 'SIMPLE' : 'SINGLE',
                allowDeselect: true,
                toggleUiHeader: function (isChecked) {
                    me.grid.down('#select-all').setRawValue(isChecked);
                },
                onStoreAdd: function () {
                    me.syncView();
                },
                onStoreLoad: function () {
                    me.syncView();
                },
                onStoreRefresh: function () {
                    me.syncView();
                },
                listeners: {
                    select: function (sel, record) {
                        if (sel.mode == 'SINGLE') {
                            selection.removeAll();
                            selection.add(record);
                        }
                        else {
                            selection.add(record);
                        }
                    },
                    deselect: function (s, record) {
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
        me.on('afterrender', me.syncView, me);
    },

    syncView: function() {
        var me = this,
            model = me.grid.getSelectionModel(),
            count = me.store.getCount(),
            filterSelected = me.down('#filter-selected');

        model.deselectAll(true);
        model.select(me.getStoreRecords(), false, true);

        if (me.multiSelect) {
            model.updateHeaderState();
        }

        me.down('#select-all').setDisabled(!count);

        // "show selected" button is disabled when there is no selection or when selection is all available store items
        filterSelected.setDisabled(!me.selection.getCount() || me.selection.getCount() == me.store.getTotalCount());
        filterSelected.suspendEvent('change');
        filterSelected.setValue(
                count > 0
            &&  count === model.getCount()
            &&  count === me.selection.getCount()
        );
        filterSelected.resumeEvent('change');
    },

    syncSelection: function(check) {
        var me = this,
            store = me.grid.getStore(),
            selection = me.selection;

        if (check) {
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
            store.load();
        }
    },

    getStoreRecords: function() {
        var me = this;
        return _.filter(me.getStore().getRange(), function(i) {
            return _.indexOf(_.map(me.selection.getRange(), function(i) {return i.getId()}), i.getId()) >=0
        })
    },

    storeSync: function () {
        var me = this,
            selection = me.selection,
            records = me.getStoreRecords();

        selection.suspendEvents();
        selection.removeAll();
        selection.add(records);
        selection.resumeEvents();
        me.onChange();
    }
});