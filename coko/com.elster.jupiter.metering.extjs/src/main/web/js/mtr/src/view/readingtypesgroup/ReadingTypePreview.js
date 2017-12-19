/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.readingtypesgroup.ReadingTypePreview', {
    extend: 'Ext.form.Panel',
    alias: 'widget.reading-types-groups-readingtype-preview',
    itemId: 'reading-types-groups-readingtype-preview',
    requires: [
        'Mtr.view.readingtypes.PreviewForm'
    ],
    frame: true,

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'reading-types-preview-form',
            router: me.router
        };

        me.callParent(arguments);
    }
});
