Ext.define('Dlc.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.store.PortalItems',
        'Uni.store.MenuItems'
    ],

    controllers: [
        'Dlc.controller.history.DeviceLifeCycle',
        'Dlc.controller.DeviceLifeCycles'
    ],

    init: function () {
        this.initHistorians();
        this.initMenu();
        this.callParent(arguments);
    },

    initHistorians: function () {
        var historian = this.getController('Dlc.controller.history.DeviceLifeCycle');
    },

    initMenu: function () {
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
});