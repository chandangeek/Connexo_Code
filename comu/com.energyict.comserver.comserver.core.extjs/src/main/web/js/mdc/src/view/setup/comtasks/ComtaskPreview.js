/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.ComtaskPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.comtaskPreview',
    hidden: false,
    title: Uni.I18n.translate('general.details','MDC','Details'),
    frame: true,
    requires: [
        'Mdc.view.setup.comtasks.ComtaskActionMenu',
        'Mdc.view.setup.comtasks.ComtaskPreviewForm'
    ],
    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Mdc.privileges.Communication.admin,
            menu: {
                xtype: 'comtaskActionMenu'
            }
        }
    ],
    items: {
        xtype: 'comtaskpreviewform',
        itemId: 'comtaskPreviewFieldsPanel'
    }
});