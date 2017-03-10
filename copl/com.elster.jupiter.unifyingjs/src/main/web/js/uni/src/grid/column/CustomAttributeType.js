/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.column.CustomAttributeType
 */
Ext.define('Uni.grid.column.CustomAttributeType', {
    extend: 'Ext.grid.column.Column',
    xtype: 'custom-attribute-type-column',
    header: Uni.I18n.translate('general.type', 'UNI', 'Type'),
    align: 'left',

    requires: [
        'Uni.form.field.CustomAttributeTypeDisplay'
    ],

    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
        if(this.$className  !== 'Uni.grid.column.CustomAttributeType'){
            var me = Ext.Array.findBy(this.columns, function (item) {
                return item.$className === 'Uni.grid.column.CustomAttributeType';
            })
        } else {
            me = this;
        }
        var field = new Uni.form.field.CustomAttributeTypeDisplay();
        return field.renderer.apply(me, [value, field, view, record]);
    }
});