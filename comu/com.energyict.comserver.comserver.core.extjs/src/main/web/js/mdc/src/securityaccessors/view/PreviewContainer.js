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
            store: me.deviceTypeId
                ? 'Mdc.securityaccessors.store.SecurityAccessorsOnDeviceType'
                : 'Mdc.securityaccessors.store.SecurityAccessors',
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
                    privileges: Mdc.privileges.SecurityAccessor.canAdmin(),
                    text: me.deviceTypeId
                        ? Uni.I18n.translate('general.addSecurityAccessors', 'MDC', 'Add security accessors')
                        : Uni.I18n.translate('general.addSecurityAccessor', 'MDC', 'Add security accessor'),
                    itemId: 'mdc-add-security-accessor'
                }
            ]
        };

        me.previewComponent = {
            xtype: 'security-accessors-preview',
            itemId: 'mdc-security-accessors-preview',
            deviceTypeId: me.deviceTypeId
        };

        me.callParent(arguments);
    }
});