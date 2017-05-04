/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.processes.view.MeterRoles', {
    extend: 'Uni.property.view.property.BaseCombo',

    requires: [
        'Imt.processes.store.AvailableMeterRoles'
    ],

    listeners: {
        afterrender: {
            fn: function () {
                var me = this,
                    store = Ext.getStore('Imt.processes.store.AvailableMeterRoles');

                store.addListener('loadmeterrolestore', function(){
                    store.load({
                        callback: function(){
                            me.restoreDefault();
                            me.doEnable(true);
                        }
                    });
                }, me);


                store.addListener('clearmeterrolestore', function () {
                    me.restoreDefault();
                    me.doEnable(false);
                }, me);
            }
        }
    },

    initComponent: function(){
        var me = this;

        me.callParent(arguments);
        me.doEnable(false);
    },

    updateEditButton: function () {
        var showEditButton = this.showEditButton;
        var button = this.getEditButton();
        if (this.isEdit) {
            button.setVisible(showEditButton);
            button.setTooltip(this.editButtonTooltip);
        } else {
            button.setVisible(false);
        }
    },

    getEditCmp: function () {
        var me = this;

        return {
            xtype: 'combobox',
            itemId: me.key + 'combobox',
            name: this.getName(),
            store: 'Imt.processes.store.AvailableMeterRoles',
            width: me.width,
            readOnly: me.isReadOnly,
            blankText: me.blankText,
            emptyText: Uni.I18n.translate('usagepoint.setTransitions.selectMeterRole', 'IMT', 'Select a meter role...'),
            displayField: 'name',
            valueField: 'id',
            disabled: true,
            listeners: {
                enable: function(el, opt){
                    console.log(el, opt);
                }
            }
        };
    },

    getField: function () {
        return this.down('combobox');
    }
});

