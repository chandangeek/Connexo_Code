/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.StartAlarmProcess', {
    extend: 'Uni.property.view.property.Reference',

    getEditCmp: function () {
        var me = this;

        // clear store
        me.referencesStore.loadData([], false);
        _.map(me.getProperty().getPossibleValues(), function (item) {
            me.referencesStore.add({key: item.id, value: item.name});
        });

        return {
            xtype: 'combobox',
            itemId: me.key + 'combobox',
            name: this.getName(),
            editable: false,
            store: me.referencesStore,
            queryMode: 'local',
            displayField: 'value',
            valueField: 'key',
            width: me.width,
            forceSelection: me.getProperty().getExhaustive(),
            readOnly: me.isReadOnly,
            allowBlank: me.allowBlank,
            blankText: me.blankText
        }
    }

});