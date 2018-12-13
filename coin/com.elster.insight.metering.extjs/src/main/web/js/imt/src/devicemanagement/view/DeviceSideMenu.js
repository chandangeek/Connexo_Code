/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.devicemanagement.view.DeviceSideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.device-management-side-menu',
    router: null,
    //title: Uni.I18n.translate('general.label.device', 'IMT', 'Device'),
    objectType: Uni.I18n.translate('general.label.device', 'IMT', 'Device'),

    initComponent: function () {
        var me = this;
        me.menuItems = [
            {
                text: Uni.I18n.translate('devicemanagement.label.device.attributes', 'IMT', 'Device attributes'),
                itemId: 'device-overview-link',
                href: me.router.getRoute('usagepoints/device').buildUrl({usagePointId: me.usagePointId})
            }
        ];
        me.title = me.device.get('name') || Uni.I18n.translate('general.label.device', 'IMT', 'Device');
        me.callParent(arguments);
    }
});
