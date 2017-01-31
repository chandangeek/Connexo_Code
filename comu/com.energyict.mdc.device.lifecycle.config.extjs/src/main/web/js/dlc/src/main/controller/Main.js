/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.main.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.store.PortalItems',
        'Uni.store.MenuItems',
        'Dlc.dynamicprivileges.DeviceLifeCycle',
        'Dlc.dynamicprivileges.Stores'
    ],

    controllers: [
        'Dlc.main.controller.history.DeviceLifeCycle',
        'Dlc.devicelifecycles.controller.DeviceLifeCycles',
        'Dlc.devicelifecyclestates.controller.DeviceLifeCycleStates',
        'Dlc.devicelifecycletransitions.controller.DeviceLifeCycleTransitions'
    ],

    stores: [
        'Dlc.main.store.DeviceLifeCyclePrivileges'
    ],

    init: function () {
        this.initHistorians();
        this.initMenu();
        this.callParent(arguments);
    },

    initHistorians: function () {
        var historian = this.getController('Dlc.main.controller.history.DeviceLifeCycle');
    },

    initMenu: function () {
        if (Dlc.privileges.DeviceLifeCycle.canView()) {
            var deviceLifeCycleItem = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('general.deviceLifeCycleManagement', 'DLC', 'Device life cycle management'),
                portal: 'administration',
                items: [
                    {
                        text: Uni.I18n.translate('general.deviceLifeCycles', 'DLC', 'Device life cycles'),
                        href: '#/administration/devicelifecycles',
                        route: 'devicelifecycles'
                    }
                ]
            });

            Uni.store.PortalItems.add(
                deviceLifeCycleItem
            );
        }
    }
});