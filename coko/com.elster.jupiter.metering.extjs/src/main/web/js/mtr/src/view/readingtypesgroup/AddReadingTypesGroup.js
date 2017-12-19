/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.readingtypesgroup.AddReadingTypesGroup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-reading-types-group',
    itemId: 'add-reading-types-group',

    requires: [
        'Mtr.view.readingtypesgroup.AddReadingTypesGroupForm'
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
            title: Uni.I18n.translate('readingtypesmanagement.addreadingtypesgroup.title', 'MTR', 'Add reading types group'),
            items: [
                {
                    xtype: 'add-reading-types-group-form'

                }
            ]
        };
        me.callParent(arguments);
    }
});