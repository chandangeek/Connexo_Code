/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.timeofuse.view.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.tou-preview-panel',
    frame: true,
    timeOfUseAllowed: null,

    requires: [
        'Mdc.timeofuse.view.PreviewForm',
        'Uni.util.FormEmptyMessage',
        'Mdc.timeofuse.view.ActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'uni-button-action',
                privileges: Mdc.privileges.DeviceType.view,
                itemId: 'touPreviewMenuButton',
                hidden: !me.timeOfUseAllowed,
                menu: {
                    xtype: 'tou-devicetype-action-menu'
                }
            }
        ];

        me.items = [
            {
                xtype: 'devicetype-tou-preview-form',
                itemId: 'devicetype-tou-preview-form',
            },
            {
                xtype: 'uni-form-empty-message',
                text: Uni.I18n.translate('timeofuse.calendarIsGhostMessage', 'MDC', "No information available due to status as 'ghost'."),
                itemId: 'ghostStatusMessage',
                hidden: true
            }
        ];
        me.callParent(arguments);
    },

    showForm: function() {
        var me = this;

        me.down('#devicetype-tou-preview-form').show();
        me.down('#ghostStatusMessage').hide();
    },

    showEmptyMessage: function () {
        var me = this;

        me.down('#devicetype-tou-preview-form').hide();
        me.down('#ghostStatusMessage').show();
    }
});