/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.view.StartProcess', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.issue-start-process-view',
    requires: [
        'Bpm.startprocess.view.StartProcess'
    ],
    device: null,
    properties: null,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'bpm-start-processes-panel',
                ui: 'large',
                itemId: 'issue-start-processes-panel',
                properties: me.properties
            }
        ];
        me.callParent(arguments);
    }
});

