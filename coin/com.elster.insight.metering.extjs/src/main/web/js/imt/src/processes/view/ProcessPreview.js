/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.processes.view.ProcessPreview', {
    extend: 'Ext.form.Panel',
    xtype: 'processPreview',
    alias: 'widget.processX-preview-form',
    frame: true,
    router: null,
    requires: [
        'Imt.processes.view.ProcessPreviewForm'
    ],

    initComponent: function () {
       var me = this;
       me.items = [
            {
                xtype: 'process-preview-form',
                itemId: 'processPreviewForm',
                router: me.router
            }
        ];
        me.callParent(arguments);
    }
});
