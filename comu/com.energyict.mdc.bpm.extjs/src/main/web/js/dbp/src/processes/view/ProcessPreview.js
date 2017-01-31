/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dbp.processes.view.ProcessPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.dbp-process-preview',
    requires: [
        'Dbp.processes.view.ProcessPreviewForm',
        'Dbp.processes.view.ProcessActionMenu'
    ],
    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Dbp.privileges.DeviceProcesses.administrateProcesses,
            menu: {
                xtype: 'dbp-process-action-menu'
            }
        }
    ],

    items: {
        itemId: 'frm-preview-process',
        xtype: 'dbp-process-preview-form'
    }
});

