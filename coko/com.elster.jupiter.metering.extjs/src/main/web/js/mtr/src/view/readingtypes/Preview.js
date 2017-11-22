/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.readingtypes.Preview', {
    extend: 'Ext.form.Panel',
    alias: 'widget.reading-types-preview',
    itemId: 'reading-types-preview',
    requires: [
        'Mtr.view.readingtypes.PreviewForm'
    ],
    frame: true,


    tools: [
        {
            xtype: 'uni-button-action',
            itemId: 'action-button',
            privileges : Mtr.privileges.ReadingTypes.admin,
            menu: {
                xtype: 'reading-types-action-menu'
            }
        }
    ],

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'reading-types-preview-form',
            router: me.router
        };

        me.callParent(arguments);
    }
});
