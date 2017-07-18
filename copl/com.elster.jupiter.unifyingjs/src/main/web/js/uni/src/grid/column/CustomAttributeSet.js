/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.column.CustomAttributeSet
 */
Ext.define('Uni.grid.column.CustomAttributeSet', {
    extend: 'Ext.grid.column.Column',
    xtype: 'custom-attribute-set-column',
    header: Uni.I18n.translate('general.name', 'UNI', 'Name'),
    align: 'left',

    requires: [
        'Uni.form.field.CustomAttributeSetDisplay'
    ],

    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
        if(this.$className  !== 'Uni.grid.column.CustomAttributeSet'){
            var me = Ext.Array.findBy(this.columns, function (item) {
                return item.$className === 'Uni.grid.column.CustomAttributeSet';
            })
        } else {
            me = this;
        }
        var field = new Uni.form.field.CustomAttributeSetDisplay();
        return field.renderer.apply(me, [value, field, view, record]);
    }
});