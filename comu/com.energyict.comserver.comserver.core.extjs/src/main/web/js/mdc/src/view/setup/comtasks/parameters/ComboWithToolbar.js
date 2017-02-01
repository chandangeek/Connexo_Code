/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.parameters.ComboWithToolbar', {
    extend: 'Ext.form.field.ComboBox',
    queryMode: 'local',
    multiSelect: true,
    alias: 'widget.combo-with-toolbar',
    labelWidth: 200,
    required: true,
    width: 400,
    displayField: 'name',
    valueField: 'id',
    allowBlank: false,
    editable: false,
    validateOnBlur: false,
    validateOnChange: false,

    initComponent: function () {
        var me = this;

        me.defaultListConfig = {
            columns: {
                style: {
                    borderRadius: '0'
                },
                items: {
                    dataIndex: me.displayField,
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
                fields: this.store.model.getFields()
            }),
            opts = Ext.apply({
                selModel: {
                    store: me.store,
                    selType: 'checkboxmodel',
                    mode: 'SIMPLE',
                    showHeaderCheckbox: true,
                    listeners: {
                        beforeselect: function(s, record) {
                            selection.add(record);
                        },
                        beforedeselect: function(s, record) {
                            selection.remove(record);
                        }
                    }
                },
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
                bodyStyle: {
                    borderWidth: '1px 0 0 0 !important',
                    borderRadius: '0 0 8px 8px',
                    border: 'none'
                },
                defaults: {
                    margin: 0
                }
            }, me.listConfig, me.defaultListConfig);

        picker = me.picker = Ext.create('Ext.grid.Panel', opts);
        picker.selection = selection;
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

        me.on('expand', function () {
            var selectedRecords = [];
            Ext.Array.each(me.getValue(), function (idOfSelectedRecord) {
                selectedRecords.push(Ext.Array.findBy(me.getStore().getRange(), function (record) {
                    return record.getId() == idOfSelectedRecord;
                }));
            });
            me.picker.getSelectionModel().select(selectedRecords);
        }, me);

        return picker;
    }
});
