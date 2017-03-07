/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.PreviewContainer', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.security-accessors-preview-container',
    deviceTypeId: null,

    requires: [
        'Mdc.securityaccessors.view.SecurityAccessorsGrid',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.securityaccessors.view.Preview'
    ],


    initComponent: function () {
        var me = this;
        me.grid = {
            xtype: 'security-accessors-grid',
            itemId: 'mdc-security-accessors-grid',
            deviceTypeId: me.deviceTypeId
        };

        me.emptyComponent = {
            xtype: 'no-items-found-panel',
            itemId: 'no-files',
            title: Uni.I18n.translate('securityaccessors.empty.title', 'MDC', 'No security accessors found'),
            reasons: [
                Uni.I18n.translate('securityaccessors.empty.list.item', 'MDC', 'No security accessors have been defined yet')
            ],
            stepItems: [
                {
                    xtype: 'button',
                    text: Uni.I18n.translate('securityaccessors.addSecurityAccessor', 'MDC', 'Add security accessor'),
                    itemId: 'mdc-add-security-accessor'
                }
            ]
        };

        me.previewComponent = {
            xtype: 'security-accessors-preview',
            itemId: 'mdc-security-accessors-preview'
        };

        me.callParent(arguments);
    }
});