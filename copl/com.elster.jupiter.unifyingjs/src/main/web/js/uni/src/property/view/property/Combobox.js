/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.Combobox', {
    extend: 'Uni.property.view.property.Base',

    getEditCmp: function () {
        var me = this;
        var sortedStore = me.getProperty().getPossibleValues().sort();

        return {
            xtype: 'combobox',
            itemId: me.key + 'combobox',
            name: this.getName(),
            store: sortedStore,
            queryMode: 'local',
            displayField: 'value',
            valueField: 'key',
            width: me.width,
            forceSelection: me.getProperty().getExhaustive(),
            readOnly: me.isReadOnly,
            blankText: me.blankText
        }
    },

    getField: function () {
        return this.down('combobox');
    }
});