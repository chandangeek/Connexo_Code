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
    extend: 'Uni.view.search.field.internal.CriteriaButton',
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

    updateButtonText: function () {
        //this.setValue(value);
        return this.value
            ? this.setText(this.emptyText + '&nbsp;(' + this.value[0].get('criteria').length + ')')
            : this.setText(this.emptyText);
    },

    store: null,
    menuConfig: {
        width: 300,
        maxHeight: 600
    },

    populateValue: function(value) {
        //var value = value[0];
        this.setValue(value);
        //this.setText(this.emptyText + '&nbsp;(' + value.length + ')');
    },

    onChange: function () {
        var me = this,
            value = me.selection.getRange().map(function(item){
                return item.get(me.valueField)
            });

        me.setValue(value.length ? Ext.create('Uni.model.search.Value', {
            operator: this.down('#filter-operator').getValue(),
            criteria: value
        }) : null);

        me.down('#filter-selected').setDisabled(!me.selection.length);
    },

    reset: function() {
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
                        itemId: 'toolbar',
                        layout: 'hbox',
                        items: [
                            {
                                itemId: 'filter-operator',
                                xtype: 'uni-search-internal-operator',
                                value: '==',
                                margin: '0 5 0 0',
                                operators: ['==', '!='],
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
                onStoreLoad: function (store) {
                    this.superclass.onStoreLoad.apply(this);
                    if (me.value && me.value[0]) {
                        _.map(me.value[0].get('criteria'), function(id) {
                            var record = store.getById(id);
                            if (record) {
                                selection.add(record);
                            }
                        });
                    }
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