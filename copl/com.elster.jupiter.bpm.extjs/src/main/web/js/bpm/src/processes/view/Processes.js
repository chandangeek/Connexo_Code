/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.processes.view.Processes', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.bpm-processes',
    requires: [
        'Bpm.processes.view.ProcessPreview',
        'Bpm.processes.view.ProcessesGrid',
        'Bpm.processes.view.ProcessPreviewForm'
    ],

    disableAction: false,
    router: null,
    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'panel',
            itemId: 'bpm-processes-form',
            ui: 'large',
            title: Uni.I18n.translate('general.processes', 'BPM', 'Processes'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'bpm-processes-grid',
                        itemId: 'bpm-processes-grid',
                        disableAction: me.disableAction,
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'bpm-processes-empty-grid',
                        title: Uni.I18n.translate('bpm.process.empty.title', 'BPM', 'No processes found'),
                        reasons: [
                            Uni.I18n.translate('bpm.process.empty.list.item1', 'BPM', 'No processes have been defined yet.'),
                            Uni.I18n.translate('bpm.process.empty.list.item2', 'BPM', 'Processes exist, but you do not have permission to view them.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'bpm-process-preview',
                        itemId: 'bpm-process-preview'
                    }
                }
            ]
        };
        me.callParent(arguments);
    }
});

