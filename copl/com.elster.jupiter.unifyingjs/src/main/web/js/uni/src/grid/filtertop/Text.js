/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.filtertop.Text
 */
Ext.define('Uni.grid.filtertop.Text', {
    extend: 'Ext.form.field.Text',
    xtype: 'uni-grid-filtertop-text',

    mixins: [
        'Uni.grid.filtertop.Base'
    ],

    emptyText: Uni.I18n.translate('grid.filter.text.label', 'UNI', 'Text'),

    initComponent: function () {
        var me = this;

        me.callParent(arguments);

        me.on('specialkey', function (field, event) {
            if (event.getKey() === event.ENTER) {
                me.fireFilterUpdateEvent();
            }
        }, me);
    }
});