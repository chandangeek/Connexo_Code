/**
 * @class Uni.view.search.field.YesNo
 */
Ext.define('Uni.view.search.field.Combobox', {
    extend: 'Ext.form.field.ComboBox',
    requires: [
        'Ext.grid.Panel'
    ],
    xtype: 'search-combo',
    store: Ext.create('Ext.data.Store', {
        fields: ['name', 'value'],
        data: [
            {'name': Uni.I18n.translate('window.messabox.yes', 'FWC', 'Yes'), 'value': 'true'},
            {'name': Uni.I18n.translate('window.messabox.no', 'FWC', 'No'), 'value': 'false'}
        ]
    }),
    initComponent: function () {
        var me = this;

        me.defaultListConfig = {
            columns: [
                {
                    dataIndex: me.displayField,
                    flex: 1
                }
            ]
        };
        me.callParent(arguments);
    },

    // copied from ComboBox
    createPicker: function() {
        var me = this,
            picker,
            menuCls = Ext.baseCSSPrefix + 'menu',
            opts = Ext.apply({
                selModel: {
                    selType: 'checkboxmodel',
                    mode: me.multiSelect ? 'SIMPLE' : 'SINGLE'
                },
                minWidth: 300,
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
                tbar: [
                    {
                        itemId: 'filter-operator',
                        text: '='
                    },
                    {
                        itemId: 'filter-valid',
                        text: 'v'
                    },
                    {
                        itemId: 'filter-input',
                        xtype: 'textfield',
                        flex: 1,
                        listeners: {
                            change: function (elm, value) {
                                var store = me.picker.getStore();
                                store.clearFilter(true);
                                store.filter(me.displayField, value);
                                me.picker.down('#filter-clear').setVisible(!!value);
                            }
                        }
                    },
                    {
                        itemId: 'filter-clear',
                        hidden: true,
                        text: 'x',
                        listeners: {
                            click: function () {
                                me.picker.down('#filter-input').reset();
                            }
                        }
                    }
                ]
            }, me.listConfig, me.defaultListConfig);

        picker = me.picker = Ext.create('Ext.grid.Panel', opts);

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