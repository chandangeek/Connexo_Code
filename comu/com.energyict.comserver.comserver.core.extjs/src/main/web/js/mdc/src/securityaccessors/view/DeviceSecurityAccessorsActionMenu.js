/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.DeviceSecurityAccessorsActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.device-security-accessors-action-menu',

    keyMode: undefined,

    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.Edit', 'MDC', 'Edit'),
                //privileges: Mdc.privileges.DeviceType.canAdministrate(),
                action: 'editDeviceSecurityAccessor',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.finishRenewal', 'MDC', 'Finish renewal/Renew'),
                //privileges: Mdc.privileges.DeviceType.canAdministrate(),
                visible: function () {
                    return !Ext.isEmpty(this.keyMode) && this.keyMode;
                },
                action: 'finishRenewal',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.showValues', 'MDC', 'Show values'),
                //privileges: Mdc.privileges.DeviceType.canAdministrate(),
                visible: function () {
                    return !Ext.isEmpty(this.keyMode) && this.keyMode;
                },
                action: 'showValues',
                section: this.SECTION_VIEW
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