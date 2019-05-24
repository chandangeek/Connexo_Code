/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.DeviceGroupCombobox', {
    extend: 'Uni.property.view.property.Base',

    getEditCmp: function () {
        var me = this,
            sortedStore;
        if(me.getProperty().getPredefinedPropertyValues())
            sortedStore = me.getProperty().getPredefinedPropertyValues().possibleValues();

        return {
            xtype: 'combobox',
            itemId: me.key + 'combobox',
            name: this.getName(),
            store: sortedStore,
            queryMode: 'local',
            displayField: 'name',
            valueField: 'id',
            multiSelect: true,
            tpl: Ext.create('Ext.XTemplate',
                '<ul class="x-list-plain">',
                '<tpl for=".">',
                '<li role="option" class="x-boundlist-item">',
                '<div class="x-combo-list-item">',
                '<img src="' + Ext.BLANK_IMAGE_URL + '" class="x-form-checkbox" style="top: 2px; left: -2px; position: relative;"/>',
                '{name}&nbsp;&nbsp;',

                '</div>',
                '</li>',
                '</tpl>',
                '</ul>'
            ),
            // tpl: new Ext.XTemplate('<tpl for=".">', '<div class="x-boundlist-item">', '<input type="checkbox" />', '{name}', '</div>', '</tpl>'),
            width: me.width,
            forceSelection: me.getProperty().getExhaustive(),
            readOnly: me.isReadOnly,
            allowBlank: me.allowBlank,
            blankText: me.blankText
        }
    },

    getField: function () {
        return this.down('combobox');
    },

    getValue: function () {
        if (this.getField().getValue().length === 0) {
            return "";
        }
        return this.getField().getValue();
    }

});