/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.EstimationComment', {
    extend: 'Uni.property.view.property.BaseCombo',

    alias: 'widget.estimation-comment',
    requires: [
        'Uni.property.store.EstimationComment'
    ],
    getComboCmp: function () {
        var me = this,
            propertyValue = me.getProperty().get('value');
        return {
            xtype: 'combobox',
            itemId: me.key + 'combobox',
            name: this.getName(),
            store: Ext.create('Uni.property.store.EstimationComment'),
            queryMode: 'local',
            typeAhead: true,
            autoSelect: true,
            displayField: 'value',
            valueField: 'key',
            value: (!propertyValue ? undefined : propertyValue),
            width: me.width,
            readOnly: me.isReadOnly,
            allowBlank: !me.getProperty().data.required,
            blankText: me.blankText,
            forceSelection: this.getProperty().isEditable() || me.getProperty().getExhaustive(),
            editable: this.getProperty().isEditable() || !me.getProperty().getExhaustive(),
            listConfig: {
                loadMask: true,
                maxHeight: 300
            }
        }
    }
});