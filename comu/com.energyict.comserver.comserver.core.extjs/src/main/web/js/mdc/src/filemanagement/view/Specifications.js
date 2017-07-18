/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.filemanagement.view.Specifications', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.files-specifications-preview-panel',
    frame: false,
    requires: [
        'Mdc.filemanagement.view.SpecificationsForm',
        'Mdc.filemanagement.view.SpecificationsActionMenu'
    ],
    ui: 'large',
    showTitle: false,

    initComponent: function () {
        var me = this;
        if(me.showTitle) {
            me.title =  Uni.I18n.translate('general.fileManagement', 'MDC', 'File management');
        }
        me.tools = [
            {
                xtype: 'uni-button-action',
                privileges: Mdc.privileges.DeviceType.admin,
                itemId: 'fileSpecificationsButton',
                menu: {
                    xtype: 'files-spec-action-menu'
                }
            }
        ];

        me.items = {
            xtype: 'files-devicetype-specifications-form',
            itemId: 'files-devicetype-specifications-form'
        };
        me.callParent(arguments);
    }

});