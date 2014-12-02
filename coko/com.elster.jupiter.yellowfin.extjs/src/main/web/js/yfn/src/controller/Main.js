Ext.define('Yfn.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation',
        'Uni.store.MenuItems'
    ],

    controllers: [
    ],

    refs: [
        {
            ref: 'viewport',
            selector: 'viewport'
        }
    ],

    init: function () {
        var me = this,
            historian = me.getController('Yfn.controller.history.YellowfinReports'); // Forces route registration.

        me.initMenu();
    },

    initMenu: function () {
        if (Uni.Auth.hasAnyPrivilege(['privilege.create.inventoryManagement', 'privilege.revoke.inventoryManagement', 'privilege.import.inventoryManagement'])) {
            var me = this;
            var reportsItems = [];

            if (Uni.Auth.hasPrivilege('privilege.import.inventoryManagement')) {
                reportsItems.push(
                    {
                        text: Uni.I18n.translate('report.communicationperformance.title', 'YFN', 'Communication (connection) performance'),
                        onClick: function(){
                            window.open("../reports/index.html#/reports/communicationperformance", "", "");}
                    }
                );
            }

            if (Uni.Auth.hasPrivilege('privilege.import.inventoryManagement')) {
                reportsItems.push(
                    {
                        text: Uni.I18n.translate('report.devicegatewaytopology.title', 'YFN', 'Device / Gateway topology'),
                        onClick: function(){
                            window.open("../reports/index.html#/reports/devicegatewaytopology", "", "");}
                    }
                );
            }

            if (Uni.Auth.hasPrivilege('privilege.import.inventoryManagement')) {
                reportsItems.push(
                    {
                        text: Uni.I18n.translate('report.deviceconfig.title', 'YFN', 'Device Config'),
                        onClick: function(){
                            window.open("../reports/index.html#/reports/deviceconfig", "", "");}
                    }
                );
            }

            var portalItem = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('report.home', 'YFN', 'Reports'),
                portal: 'devices',
                route: 'reports',
                items: reportsItems
            });

            Uni.store.PortalItems.add(
                portalItem
            );
        }
    },

    /**
     * @deprecated Fire an event instead, as shown below.
     */
    showContent: function (widget) {
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});