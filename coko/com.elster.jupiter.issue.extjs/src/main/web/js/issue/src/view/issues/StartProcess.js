/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.issues.StartProcess', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.isu-start-process-view',
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
                itemId: 'isu-start-processes-panel',
                properties: me.properties
            }
        ];
        me.callParent(arguments);
    }
});

