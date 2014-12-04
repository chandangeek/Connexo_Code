Ext.define('Yfn.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation',
        'Uni.store.MenuItems',
        'Yfn.store.ReportInfos'
    ],

    controllers: [
    ],

    stores:[
        'ReportInfos'
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

        me.initDeviceReports();
    },

    initDeviceReports: function () {
        //if (Uni.Auth.hasAnyPrivilege(['privilege.create.inventoryManagement', 'privilege.revoke.inventoryManagement', 'privilege.import.inventoryManagement'])) {
            var me = this;

            var reportsStore = Ext.create('Yfn.store.ReportInfos',{});
            //var reportsStore = Ext.getStore('ReportInfos');
            var reportsItems = [];

            var portalItem = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('report.home', 'YFN', 'Reports'),
                portal: 'devices',
                route: 'reports',
                items: reportsItems,
                itemId:'deviceReportsPortlet'
            });

            Uni.store.PortalItems.add(
                portalItem
            );

            if(reportsStore) {
                var proxy = reportsStore.getProxy();
                proxy.setExtraParam('category', 'MDC');
                proxy.setExtraParam('subCategory', 'Device');

                reportsStore.load(function (records) {

                    Ext.each(records, function (record) {
                        var reportDescription = record.get('description');
                        var reportUUID = record.get('reportUUID');
                        var reportName = record.get('name');
                        var reportDescription = record.get('name');
                        reportsItems.push({
                            text: reportName,
                            tooltip: reportDescription,
                            href: '#/administration/generatereport?reportUUID=' + reportUUID
                            //,hrefTarget: '_blank'
                        });
                    });

                    var cmp =  Ext.ComponentQuery.query('portal-container #deviceReportsPortlet')[0];
                    if(cmp){
                        reportsItems.sort(function (item1, item2) {
                            if (item1.text < item2.text) {
                                return -1;
                            } else if (item1.text > item2.text) {
                                return 1;
                            } else {
                                return 0;
                            }
                        });
                        cmp.refresh(reportsItems);
                    }

                });
            }


        //}
    },

    /**
     * @deprecated Fire an event instead, as shown below.
     */
    showContent: function (widget) {
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});