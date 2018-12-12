/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.monitorissueprocesses.view.AlarmProcesses', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.bpm-alarm-processes',
    store:'Bpm.monitorissueprocesses.store.AlarmProcesses',
    requires: [
        'Bpm.monitorissueprocesses.view.IssueProcessesGrid',
        'Bpm.monitorissueprocesses.view.IssueProcessPreview'
    ],

    router: null,
    initComponent: function () {
        var me = this,
        processStore = me.store;

        me.content = {
            xtype: 'panel',
            itemId: 'bpm-issue-processes-form',
            ui: 'large',
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'bpm-issue-processes-grid',
                        itemId: 'issue-processes-grid',
                        store: processStore,
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'issue-processes-empty-grid',
                        title: Uni.I18n.translate('bpm.process.empty.title', 'BPM', 'No processes found'),
                        reasons: [
                            Uni.I18n.translate('bpm.process.empty.runnninglist.item1', 'BPM', 'No processes have been defined yet.'),
                            Uni.I18n.translate('bpm.process.empty.runnninglist.item2', 'BPM', 'Processes exist, but you do not have permission to view them.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'bpm-issue-process-preview',
                        itemId: 'issue-process-preview'

                    }
                }
            ]
        };
        me.callParent(arguments);
    }
});

