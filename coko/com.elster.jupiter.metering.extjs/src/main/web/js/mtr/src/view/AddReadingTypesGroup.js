/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.AddReadingTypesGroup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-reading-types-group',
    itemId: 'add-reading-types-group',

    requires: [
        'Mtr.view.AddReadingTypesGroupForm'
    ],

    loadRecord: function (record) {
        var me = this;
        me.down('add-reading-types-group-form').loadRecord(record);
    },

    initComponent: function () {
        var me = this;
        me.content = {
            xtype: 'form',
            ui: 'large',
            title: Uni.I18n.translate('readingtypesmanagement.addreadingtypes.title', 'MTR', 'Add reading type'),
            items: [
                {
                    xtype: 'add-reading-types-group-form'

                }
            ]
        };
        me.callParent(arguments);
    }
});