/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cps.customattributesets.view.widget.LevelsSelector', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.custom-attribute-sets-levels-selector',
    labelWidth: 150,
    items: [],

    requires: [
        'Uni.util.LevelMap'
    ],

    initComponent: function () {
        var me = this,
            defaultLevels = me.record.get(me.defaultValuesField),
            filledLevels = me.record.get(me.filledValuesField);

        Ext.each(defaultLevels, function (privilege) {
            me.items.push({
                xtype: 'checkbox',
                boxLabel: Uni.util.LevelMap.getTranslation(privilege),
                itemId: privilege
            });
        });

        me.callParent(arguments);

        Ext.each(filledLevels, function(privilege) {
            me.down('#' + privilege).setValue(true);
        });
    },

    getValue: function () {
        var me = this,
            levels = [];

        me.items.each(function(checkbox) {
            if (checkbox.getValue()) {
                levels.push(checkbox.itemId)
            }
        });

        return levels;
    }
});