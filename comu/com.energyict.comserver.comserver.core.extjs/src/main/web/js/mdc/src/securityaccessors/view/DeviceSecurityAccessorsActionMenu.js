/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.DeviceSecurityAccessorsActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.device-security-accessors-action-menu',

    requires:[
        'Mdc.privileges.Device',
        'Mdc.securityaccessors.view.PrivilegesHelper'
    ],

    keyMode: undefined,

    initComponent: function () {
        var me = this;
        me.items = [
            {
                text: Uni.I18n.translate('general.Edit', 'MDC', 'Edit'),
                privileges: Mdc.privileges.Device.canAdministrateDevice(),
                itemId: 'mdc-device-security-accessors-action-menu-edit',
                action: me.keyMode ? 'editDeviceKey' : 'editDeviceCertificate',
                invisibleWhenSwapped: true,
                section: me.SECTION_EDIT
            },
            {
                text: me.keyMode
                    ? Uni.I18n.translate('general.activatePassiveKey', 'MDC', 'Activate passive key')
                    : Uni.I18n.translate('general.activatePassiveCertificate', 'MDC', 'Activate passive certificate'),
                privileges: Mdc.privileges.Device.canAdministrateDevice(),
                checkPassive: true,
                action: me.keyMode ? 'activatePassiveKey' : 'activatePassiveCertificate',
                section: me.SECTION_EDIT
            },
            {
                text: me.keyMode
                    ? Uni.I18n.translate('general.clearPassiveKey', 'MDC', 'Clear passive key')
                    : Uni.I18n.translate('general.clearPassiveCertificate', 'MDC', 'Clear passive certificate'),
                privileges: Mdc.privileges.Device.canAdministrateDevice(),
                checkPassive: true,
                action: me.keyMode ? 'clearPassiveKey' : 'clearPassiveCertificate',
                section: me.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.generatePassiveKey', 'MDC', 'Generate passive key'),
                privileges: Mdc.privileges.Device.canAdministrateDevice(),
                visible: function () {
                    return !Ext.isEmpty(this.keyMode) && this.keyMode;
                },
                invisibleWhenSwapped: true,
                checkCanGeneratePassiveKey: true,
                action: 'generatePassiveKey',
                section: me.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.showValues', 'MDC', 'Show values'),
                itemId: 'mdc-device-security-accessors-action-menu-item-show-hide',
                privileges: Mdc.privileges.Device.canAdministrateDevice(),
                visible: function () {
                    return !Ext.isEmpty(this.keyMode) && this.keyMode;
                },
                checkViewRights: true,
                action: 'showKeyValues',
                section: me.SECTION_VIEW
            },
            {
                text:  Uni.I18n.translate('general.unmarkServiceKey', 'MDC', 'Unmark service key'),
                privileges: Mdc.privileges.Device.canAdministrateDevice(),
                checkServiceKey: true,
                action: 'unmarkServiceKey',
                section: me.SECTION_EDIT
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        beforeshow: function(menu) {
            var me = this,
                swapped = menu.record.get('swapped'),
                hasViewRights = Mdc.securityaccessors.view.PrivilegesHelper.hasPrivileges(menu.record.get('viewLevels')),
                passiveAvailable = menu.record.get('hasTempValue'),
                canGeneratePassiveKey = menu.record.get('canGeneratePassiveKey'),
                serviceKey = menu.record.get('serviceKey'),
                visible = true;

            me.items.each(function(item) {
                visible = true;
                if (Ext.isDefined(item.checkCanGeneratePassiveKey)) {
                    visible = visible && canGeneratePassiveKey;
                }
                if (Ext.isDefined(item.checkPassive)) {
                    visible = visible && passiveAvailable;
                }
                if (Ext.isDefined(item.checkViewRights)) {
                    visible = visible && hasViewRights;
                }
                if (Ext.isDefined(item.visible)) {
                    visible = visible && item.visible.call(me) && Mdc.privileges.Device.canAdministrateDevice();
                }
                if (Ext.isDefined(item.invisibleWhenSwapped)) {
                    visible = visible && !swapped;
                }

                if (Ext.isDefined(item.checkServiceKey)) {
                    console.log("SET VISIBILITY FOR unmarkAction. serviceKey = ",serviceKey);
                    visible = serviceKey;
                }

                item.setVisible(visible);
            });
        }
    }

});