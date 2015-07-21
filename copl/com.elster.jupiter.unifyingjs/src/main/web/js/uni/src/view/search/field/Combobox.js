/**
 * @class Uni.view.search.field.Combobox
 *
 *                         {
                            colspan: 2,
                            xtype: 'search-combo',
                            itemId: 'domain',
                            //store: 'Uni.store.search.Domains',
                            emptyText: Uni.I18n.translate('search.overview.searchDomains.emptyText', 'UNI', 'Search domains'),
                            displayField: 'name',
                            valueField: 'id',
                            forceSelection: true,
                            multiSelect: true
                        },

 */
Ext.define('Uni.view.search.field.Combobox', {
    extend: 'Ext.form.field.ComboBox',
    requires: [
        'Ext.grid.Panel'
        //'Ext.grid.feature.Summary'
    ],
    xtype: 'search-combo',
    queryMode: 'local',
    editable: false,
    //fieldStyle: {
    //    background: '#71adc7'
    //},
    getDisplayValue: function() {
        return this.rendered ? this.getPicker().selection.count() + ' selected' : this.emptyText;
    },
    disableKeyFilter: true,
    enableKeyEvents: false,
    forceSelection: true,
    store: Ext.create('Ext.data.Store', {
        fields: ['name', 'value'],
        data: [
            {'name': 'Value1', 'value': '1'},
            {'name': 'Value2', 'value': '2'},
            {'name': 'Value3', 'value': '1'},
            {'name': 'Value4', 'value': '2'},
            {'name': 'Value5', 'value': '1'},
            {'name': 'Value6', 'value': '2'},
            {'name': 'Value7', 'value': '1'},
            {'name': 'Value8', 'value': '2'},
            {'name': 'Value9', 'value': '1'},
            {'name': 'Value10', 'value': '2'},
            {'name': 'Value11', 'value': '1'},
            {'name': 'Value12', 'value': '2'},
            {'name': 'Zalue13', 'value': '1'},
            {'name': 'Zalue14', 'value': '2'},
            {'name': 'Zalue15', 'value': '1'},
            {'name': 'Zalue16', 'value': '2'},
            {'name': 'Zalue17', 'value': '1'},
            {'name': 'Zalue18', 'value': '2'},
            {'name': 'Zalue19', 'value': '1'},
            {'name': 'Zalue20', 'value': '2'},
            {'name': 'Zalue21', 'value': '1'},
            {'name': 'Zalue22', 'value': '2'},
            {'name': 'Zalue23', 'value': '1'},
            {'name': 'Zalue24', 'value': '2'},
            {'name': 'Zalue25', 'value': '1'},
            {'name': 'Zalue26', 'value': '2'}
        ],
        limit: 10,
    }),

    initComponent: function () {
        var me = this;

        me.defaultListConfig = {
            columns: {
                style: {
                    borderRadius: '0'
                },
                items: {
                    dataIndex: me.displayField,
                    //summaryRenderer: function(value, summaryData, dataIndex) {
                    //    return 'Only first 100 records are displayed';
                    //},
                    flex: 1
                }
            }
        };
        me.callParent(arguments);
    },

    createPicker: function() {
        var me = this,
            picker,
            menuCls = Ext.baseCSSPrefix + 'menu',
            selection = Ext.create('Ext.data.Store', {
                fields: ['name', 'value'],
                listeners: {
                    datachanged: function() {
                        picker.down('#filter-selected').setDisabled( !this.count() );
                    }
                }
            });
            opts = Ext.apply({
                selModel: {
                    selType: 'checkboxmodel',
                    mode: me.multiSelect ? 'SIMPLE' : 'SINGLE',
                    showHeaderCheckbox: true,
                    toggleUiHeader: function(isChecked) {
                        picker.down('#select-all').setRawValue(isChecked);
                    },

                    //processSelection: function() {
                    //    //var me = this,
                    //    //    handler = me.handler;
                    //    //if (handler) {
                    //    //    handler.call(me.scope || me, me, newVal);
                    //    //}
                    //    this.callParent(arguments);
                    //    debugger;
                    //},
                    //onSelectChange: function() {
                    //    debugger;
                    //    this.callOverridden(arguments);
                    //}

                    //onStoreLoad: function(){
                    //    debugger;
                    //    //this.callParent(arguments);
                    //    //this.updateHeaderState();
                    //
                    //},
                    onStoreLoad: function() {
                        this.superclass.onStoreLoad.apply(this);
                        this.select(selection.getRange(), true, true);
                        this.updateHeaderState();
                    },

                    onStoreRefresh: function(){
                        this.superclass.onStoreRefresh.apply(this);
                        this.select(_.intersection(this.getStore().getRange(),selection.getRange()), true, true);
                        this.updateHeaderState();
                    },

                    listeners: {
                        beforeselect: function(s, record) {
                            selection.add(record);
                            //selection.remove(s.getStore().getRange());
                            //
                        },
                        beforedeselect: function(s, record) {
                            selection.remove(record);
                            //selection.remove(s.getStore().getRange());
                            //
                        }

                    }
                },
                minWidth: 300,
                maxHeight: 400,
                floating: true,
                hidden: true,
                hideHeaders: true,
                header: false,
                title: false,
                frame: true,
                padding: 0,
                margin: 0,
                border: false,
                ownerCt: me.ownerCt,
                cls: me.el.up('.' + menuCls) ? menuCls : '',
                store: me.store,
                displayField: me.displayField,
                focusOnToFront: false,
                pageSize: me.pageSize,
                bodyStyle: {
                    borderWidth: '1px 0 0 0 !important',
                    borderRadius: '0 0 8px 8px',
                    border: 'none'
                },
                defaults: {
                    margin: 0
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
                            items: [
                                {
                                    itemId: 'filter-operator',
                                    text: '='
                                },
                                {
                                    xtype: 'fieldset',
                                    layout: 'hbox',
                                    flex: 1,
                                    padding: 0,
                                    style: {
                                        borderRadius: '5px'
                                    },
                                    tooltip: 'Specify filter to narrow down selection list. Maximum 100 records are displayed.',
                                    items: [
                                        {
                                            xtype: 'button',
                                            itemId: 'filter-selected',
                                            iconCls: 'icon-checkbox-unchecked2',
                                            iconClsUnpressed: 'icon-checkbox-unchecked2',
                                            iconClsPressed: 'icon-checkbox',
                                            enableToggle: true,
                                            style: {
                                                fontSize: '16px'
                                            },
                                            padding: 5,
                                            margin: 0,
                                            tooltip: 'Filter all selected values',
                                            ui: 'plain',
                                            disabled: true,
                                            handler: function () {
                                                var store = me.picker.getStore();
                                                if (this.pressed) {
                                                    this.setIconCls(this.iconClsPressed);
                                                    store.clearFilter(true);
                                                    store.filter({
                                                        filterFn: function(item) { return selection.getRange().indexOf(item) >= 0; }
                                                    });
                                                } else {
                                                    this.setIconCls(this.iconClsUnpressed);
                                                    store.clearFilter();
                                                }
                                            }
                                        },
                                        {
                                            itemId: 'filter-input',
                                            xtype: 'textfield',
                                            flex: 1,
                                            fieldStyle: {
                                                border: 0,
                                                margin: 0
                                            },
                                            listeners: {
                                                change: function (elm, value) {
                                                    var store = me.picker.getStore();
                                                    store.clearFilter(true);
                                                    store.filter(me.displayField, new RegExp(value));
                                                    me.picker.down('#filter-clear').setVisible(!!value);
                                                }
                                            }
                                        },
                                        {
                                            xtype: 'button',
                                            itemId: 'filter-clear',
                                            hidden: true,
                                            ui: 'plain',
                                            tooltip: 'Clear filter',
                                            iconCls: ' icon-close4',
                                            padding: 6,
                                            margin: 0,
                                            style: {
                                                fontSize: '16px'
                                            },
                                            listeners: {
                                                click: function () {
                                                    me.picker.down('#filter-input').reset();
                                                }
                                            }
                                        }
                                    ]
                                }
                            ]
                        },
                        {
                            xtype: 'container',
                            itemId: 'limit-notification',
                            html: 'Only the first 100 records are displayed. Use filter to narrow down.'
                        },
                        {
                            xtype: 'checkboxfield',
                            itemId: 'select-all',
                            boxLabel  : 'Select all displayed values',
                            name      : 'topping',
                            inputValue: '1',
                            handler: function(elm, value) {
                                var sel = me.picker.getSelectionModel();
                                // Prevent focus changes on the view, since we're selecting/deselecting all records
                                sel.preventFocus = true;
                                if (!value) {
                                    sel.deselectAll();
                                } else {
                                    sel.selectAll();
                                }
                                delete sel.preventFocus;
                            }
                        },
                        {
                            xtype: 'checkboxfield',
                            boxLabel  : 'Is empty',
                            name      : 'topping',
                            inputValue: '1'
                        }
                    ]
                }

            }, me.listConfig, me.defaultListConfig);

        picker = me.picker = Ext.create('Ext.grid.Panel', opts);
        picker.selection = selection;
        //picker.getStore().on('refresh', function () {
        //    picker.getSelectionModel().select(this.getRange(), true, true);
        //});

        // hack: pass getNode() to the view
        picker.getNode = function() {
            picker.getView().getNode(arguments);
        };

        me.mon(picker, {
            itemclick: me.onItemClick,
            refresh: me.onListRefresh,
            scope: me
        });

        me.mon(picker.getSelectionModel(), {
            selectionChange: me.onListSelectionChange,
            scope: me
        });

        return picker;
    }
});