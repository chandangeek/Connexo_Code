/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.ReadingTypePreview', {
    extend: 'Ext.form.Panel',
    alias: 'widget.reading-types-groups-readingtype-preview',
    itemId: 'reading-types-groups-readingtype-preview',
    requires: [
        'Mtr.view.PreviewForm'
    ],
    frame: true,

    tools: [
        {
            xtype: 'uni-button-action',
            itemId: 'mtr-readingTypesinGroup-preview-action-button',
            privileges: Mtr.privileges.ReadingTypes.admin,
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
