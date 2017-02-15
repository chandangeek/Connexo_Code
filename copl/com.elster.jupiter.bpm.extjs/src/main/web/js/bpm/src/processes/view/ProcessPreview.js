/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.processes.view.ProcessPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.bpm-process-preview',
    requires: [
        'Bpm.processes.view.ProcessPreviewForm',
        'Bpm.processes.view.ProcessActionMenu'
    ],
    tools: [
        {
            xtype: 'uni-button-action',
            itemId: 'btn-preview-action',
            privileges: Bpm.privileges.BpmManagement.administrateProcesses,
            menu: {
                xtype: 'bpm-process-action-menu'
            }
        }
    ],

    items: {
        itemId: 'frm-preview-process',
        xtype: 'bpm-process-preview-form'
    }
});

