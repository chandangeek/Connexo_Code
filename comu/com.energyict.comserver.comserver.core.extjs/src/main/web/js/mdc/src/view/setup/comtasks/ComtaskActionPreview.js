/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.ComtaskActionPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.comtaskActionPreview',
    title: ' ',
    frame: true,
    requires: [
        'Mdc.view.setup.comtasks.ComtaskActionActionMenu',
        'Mdc.view.setup.comtasks.ComtaskActionPreviewForm'
    ],
    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Mdc.privileges.Communication.admin,
            menu: {
                xtype: 'comtaskActionActionMenu'
            }
        }
    ],
    items: {
        xtype: 'comtaskActionPreviewForm',
        itemId: 'mdc-comtask-action-preview-form'
    }
});
