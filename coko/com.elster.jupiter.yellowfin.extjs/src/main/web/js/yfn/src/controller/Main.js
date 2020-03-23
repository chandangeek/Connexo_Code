/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Yfn.controller.Main', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.window.Window',
        'Uni.controller.Navigation',
        'Uni.store.MenuItems',
        'Uni.model.PortalItem',
        'Yfn.privileges.Yellowfin',
        'Yfn.controller.history.YellowfinReports',
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
        if (Yfn.privileges.Yellowfin.canView()) {
            var me = this;
            var portalItem = Ext.create('Uni.model.PortalItem', {
                title: Uni.I18n.translate('report.home', 'YFN', 'Reports'),
                portal: 'devices',
                route: 'reports',
                items: [],
                itemId:'deviceReportsPortlet',
                afterrender:me.loadDeviceReports
            });

            Uni.store.PortalItems.add(
                portalItem
            );

        }
    },

    loadDeviceReports : function(portalContainer){
        var me = this;

        var reportsStore = Ext.create('Yfn.store.ReportInfos',{});
        if(reportsStore) {
            var proxy = reportsStore.getProxy();
            proxy.setExtraParam('category', 'MDC');
            //proxy.setExtraParam('subCategory', 'Device');
            reportsStore.load(function (records) {
                var reportsItems = [];
                Ext.each(records, function (record) {
                    var reportDescription = record.get('description');
                    var reportUUID = record.get('reportUUID');
                    var reportName = record.get('name');
                    reportsItems.push({
                        text: reportName,
                        tooltip: reportDescription,
                        href: '#/workspace/generatereport?reportUUID=' + reportUUID//+'&subCategory=Device'
                        //,hrefTarget: '_blank'
                    });
                });
                reportsItems.sort(function (item1, item2) {
                    if (item1.text < item2.text) {
                        return -1;
                    } else if (item1.text > item2.text) {
                        return 1;
                    } else {
                        return 0;
                    }
                });

                portalContainer.refresh(reportsItems);

            });
        }
    },

    /**
     * @deprecated Fire an event instead, as shown below.
     */
    showContent: function (widget) {
        this.getApplication().fireEvent('changecontentevent', widget);
    }
});