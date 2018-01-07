/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.GroupPreview', {
    extend: 'Ext.form.Panel',
    alias: 'widget.readingTypesGroup-preview',
    requires: [
        'Mtr.view.GroupPreviewForm',
        'Mtr.view.GroupActionMenu'
    ],
    frame: true,

    tools: [
        {
            xtype: 'uni-button-action',
            itemId: 'mtr-readingTypesGroup-preview-action-button',
            privileges: Mtr.privileges.ReadingTypes.admin,
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

