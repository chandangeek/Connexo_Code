/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dbp.processes.view.Processes', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.dbp-processes',
    requires: [
        'Dbp.processes.view.ProcessPreview',
        'Dbp.processes.view.ProcessesGrid',
        'Dbp.processes.view.ProcessPreviewForm'
    ],

    router: null,
    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'panel',
            itemId: 'dbp-processes-form',
            ui: 'large',
            title: Uni.I18n.translate('general.processes', 'DBP', 'Processes'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'dbp-processes-grid',
                        itemId: 'dbp-processes-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'dbp-processes-empty-grid',
                        title: Uni.I18n.translate('dbp.process.empty.title', 'DBP', 'No processes found'),
                        reasons: [
                            Uni.I18n.translate('dbp.process.empty.list.item1', 'DBP', 'No processes have been defined yet.'),
                            Uni.I18n.translate('dbp.process.empty.list.item2', 'DBP', 'Processes exist, but you do not have permission to view them.')
                        ]
                    },
                    previewComponent: {
                        xtype: 'dbp-process-preview',
                        itemId: 'dbp-process-preview'
                    }
                }
            ]
        };
        me.callParent(arguments);
    }
});

