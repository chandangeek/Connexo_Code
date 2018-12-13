/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.grid.filter.Text
 */
Ext.define('Uni.grid.filter.Text', {
    extend: 'Ext.form.field.Text',
    xtype: 'uni-grid-filter-text',

    mixins: [
        'Uni.grid.filter.Base'
    ],

    fieldLabel: Uni.I18n.translate('grid.filter.text.label', 'UNI', 'Text'),

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