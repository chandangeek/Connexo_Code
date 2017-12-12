/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.readingtypesgroup.GroupPreview', {
    extend: 'Ext.form.Panel',
    alias: 'widget.readingTypesGroup-preview',
    requires: [
        'Mtr.view.readingtypesgroup.GroupPreviewForm'
    ],
    frame: true,

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'readingTypesGroup-preview-form',
            router: me.router
        };

        me.callParent(arguments);
    }
});

