Ext.define('Mdc.controller.setup.ServiceCalls', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.servicecalls.ServiceCallsSetup'
    ],

    models: [
        'Mdc.model.Device'
    ],

    stores: [
        'Scs.store.ServiceCalls'
    ],

    requires: [
        'Uni.controller.Navigation',
        'Uni.util.History'
    ],

    refs: [
        {
            ref: 'breadcrumbs',
            selector: 'breadcrumbTrail'
        },
        {
            ref: 'filter',
            selector: 'service-call-filter'
        }
    ],

    init: function () {
        this.control({
            'service-calls-setup #device-service-calls-tab-panel': {
                tabchange: this.onTabChange
            }
        });
    },

    showServiceCalls: function (mRID) {
        var me = this,
            store = Ext.getStore('Scs.store.ServiceCalls');

        if(!Uni.util.History.isSuspended()) {
            me.setCorrectStore(mRID, store, false)
            me.showTabbedView(mRID, store, 0);
        }

    },

    showServiceCallHistory: function (mRID) {
        var me = this,
            store = Ext.getStore('Scs.store.ServiceCalls');

        if(!Uni.util.History.isSuspended()) {
            me.setCorrectStore(mRID, store, true);
            me.showTabbedView(mRID, store, 1);
        }

    },

    setCorrectStore: function (mRID, store, isHistory) {
        if(isHistory) {
            store.setProxy({
                type: 'rest',
                url: '/api/ddr/devices/' + mRID +'/servicecallhistory',
                timeout: 120000,
                reader: {
                    type: 'json',
                    root: 'serviceCalls'
                }
            });
        } else {
            store.setProxy({
                type: 'rest',
                url: '/api/ddr/devices/' + mRID +'/runningservicecalls',
                timeout: 120000,
                reader: {
                    type: 'json',
                    root: 'serviceCalls'
                }
            });
        }
    },

    showTabbedView: function (mRID, store, activeTab) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            widget,
            date,
            filter = {};

        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                date = new Date();
                date.setDate(date.getDate() - 60);
                filter.fromDate = date;
                date = new Date();
                filter.toDate = date;
                widget = Ext.widget('service-calls-setup', {device: device, router: router, store: store, activeTab: activeTab, filterDefault: filter});
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', widget);
            }
        });
    },

    onTabChange: function (tabPanel, newTab) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            navigationController = me.getController('Uni.controller.Navigation'),
            store = Ext.getStore('Scs.store.ServiceCalls'),
            route,
            filter = {},
            d,
            now;

        me.getFilter().clearFilters();
        router.arguments.mRID = tabPanel.mRID;
        Uni.util.History.suspendEventsForNextCall(true);
        if(newTab.itemId === 'history-service-calls-tab') {
            me.setCorrectStore(tabPanel.mRID, store, true);
            d = new Date();
            d.setDate(d.getDate() - 1);
            now = new Date()
            filter.creationTime = d.getTime() + '-' + now.getTime();
            route = router.getRoute('devices/device/servicecalls/history');
        } else if (newTab.itemId === 'running-service-calls-tab') {
            me.setCorrectStore(tabPanel.mRID, store, false);
            route = router.getRoute('devices/device/servicecalls');
        }


        store.load();
        route.forward({}, filter);
        navigationController.updateBreadcrumb(route);
        me.getBreadcrumbs().doLayout();
    }
});

