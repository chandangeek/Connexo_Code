/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.grid.column.Icon', {
    extend: 'Ext.grid.column.Column',
    xtype: 'icon-column',
    header: null,
    minWidth: 100,
    align: 'left',
    requires: [
        'Uni.form.field.IconDisplay'
    ],

    deferredRenderer: function (value, record, view) {
        try {
            var me = this,
                cmp = view.getCell(record, me).down('.x-grid-cell-inner'),
                field = new Uni.form.field.IconDisplay({
                    fieldLabel: false,
                    iconCls: value.iconCls,
                    tipString: value.tipString
                });
            cmp.setHTML('');
            field.setValue(value.value);
            field.render(cmp);
        } catch (e) {
        }
    },

    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
        var me = metaData.column;
        var res = {};
        if (Ext.isDefined(value.editedTime)) {
            var editedDate = Ext.isDate(value.editedTime) ? value.editedTime : new Date(value.editedTime),
                formattedEditedDate = Uni.I18n.translate('general.dateAtTime', 'UNI', '{0} at {1}',
                    [Uni.DateTime.formatDateShort(editedDate), Uni.DateTime.formatTimeShort(editedDate)]
                );
            res.value = editedDate;
            res.iconCls = 'icon-pencil4';
            res.tipString = Uni.I18n.translate('general.editedOnX', 'UNI', 'Edited on {0}', formattedEditedDate);
        }
        if (Ext.isDefined(value.deletedTime)) {
            var deletedDate = Ext.isDate(value.deletedTime) ? value.deletedTime : new Date(value.deletedTime),
                formattedDeletedDate = Uni.I18n.translate('general.dateAtTime', 'UNI', '{0} at {1}',
                    [Uni.DateTime.formatDateShort(deletedDate), Uni.DateTime.formatTimeShort(deletedDate)]
                );
            res.value = deletedDate;
            res.iconCls = 'uni-icon-deleted';
            res.tipString = Uni.I18n.translate('general.deletedOnX', 'UNI', 'Deleted on {0}', formattedDeletedDate);
        }
        Ext.defer(me.deferredRenderer, 1, me, [res, record, view]);
    }
});