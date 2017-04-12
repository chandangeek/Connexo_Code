/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.DeviceSecurityAccessorsActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.device-security-accessors-action-menu',

    keyMode: undefined,

    initComponent: function () {
        var me = this;
        me.items = [
            {
                text: Uni.I18n.translate('general.Edit', 'MDC', 'Edit'),
                privileges: Mdc.privileges.Device.administrateDevice,
                itemId: 'mdc-device-security-accessors-action-menu-edit',
                action: me.keyMode ? 'editDeviceKey' : 'editDeviceCertificate',
                section: me.SECTION_EDIT
            },
            {
                text: me.keyMode
                    ? Uni.I18n.translate('general.activatePassiveKey', 'MDC', 'Activate passive key')
                    : Uni.I18n.translate('general.activatePassiveCertificate', 'MDC', 'Activate passive certificate'),
                //privileges: Mdc.privileges.DeviceType.canAdministrate(),
                action: me.keyMode ? 'activatePassiveKey' : 'activatePassiveCertificate',
                section: me.SECTION_EDIT
            },
            {
                text: me.keyMode
                    ? Uni.I18n.translate('general.clearPassiveKey', 'MDC', 'Clear passive key')
                    : Uni.I18n.translate('general.clearPassiveCertificate', 'MDC', 'Clear passive certificate'),
                //privileges: Mdc.privileges.DeviceType.canAdministrate(),
                action: me.keyMode ? 'clearPassiveKey' : 'clearPassiveCertificate',
                section: me.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.generatePassiveKey', 'MDC', 'Generate passive key'),
                //privileges: Mdc.privileges.DeviceType.canAdministrate(),
                visible: function () {
                    return !Ext.isEmpty(this.keyMode) && this.keyMode;
                },
                action: 'generatePassiveKey',
                section: me.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.showValues', 'MDC', 'Show values'),
                //privileges: Mdc.privileges.DeviceType.canAdministrate(),
                visible: function () {
                    return !Ext.isEmpty(this.keyMode) && this.keyMode;
                },
                action: 'showKeyValues',
                section: me.SECTION_VIEW
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        beforeshow: function() {
            var me = this;
            me.items.each(function(item){
                if (item.visible === undefined) {
                    item.show();
                } else {
                    item.visible.call(me) /*&& Mdc.privileges.DeviceType.canAdministrate()*/ ?  item.show() : item.hide();
                }
            })
        }
    }

});