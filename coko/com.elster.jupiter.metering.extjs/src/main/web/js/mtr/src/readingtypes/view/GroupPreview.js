/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mtr.readingtypes.view.GroupPreview', {
    extend: 'Ext.form.Panel',
    alias: 'widget.readingTypesGroup-preview',
    requires: [
        'Mtr.readingtypes.view.GroupPreviewForm',
        'Mtr.readingtypes.view.GroupActionMenu'
    ],
    frame: true,


    tools: [
        {
            xtype: 'uni-button-action',
            itemId: 'mtr-readingTypesGroup-preview-action-button',
            privileges : Mtr.privileges.ReadingTypes.admin,
            menu: {
                xtype: 'readingTypesGroup-action-menu'
            }
        }
    ],

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'readingTypesGroup-preview-form',
            router: me.router
        };

        me.callParent(arguments);
    }
});

