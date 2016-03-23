Ext.define('Mdc.controller.setup.ServiceCalls', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.servicecalls.ServiceCallsSetup'
    ],

    models: [
        'Mdc.model.Device'
    ],

    stores: [
        'Mdc.store.servicecalls.ServiceCallHistory',
        'Mdc.store.servicecalls.RunningServiceCalls',
        'Scs.store.ServiceCallTypes',
        'Scs.store.States'
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
        },
        {
            ref: 'serviceCallsSetup',
            selector: 'service-calls-setup'
        }
    ],

    historyAdded: false,

    init: function () {
        this.control({
            'service-calls-setup #device-service-calls-tab-panel': {
                tabchange: this.onTabChange
            }
        });
    },

    showServiceCalls: function (mRID) {
        var me = this;

        if (!Uni.util.History.isSuspended()) {
            me.showTabbedView(mRID, 0);
        }

    },

    showServiceCallHistory: function (mRID) {
        var me = this;
        if (!Uni.util.History.isSuspended()) {
            me.showTabbedView(mRID, 1);
        }

    },

    showTabbedView: function (mRID, activeTab) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            widget,
            historyStore = Ext.getStore('Mdc.store.servicecalls.ServiceCallHistory'),
            runningStore = Ext.getStore('Mdc.store.servicecalls.RunningServiceCalls'),
            date,
            filter = {};

        me.historyAdded = false;
        runningStore.getProxy().setUrl(mRID);
        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                widget = Ext.widget('service-calls-setup', {
                    device: device,
                    router: router,
                    activeTab: activeTab
                });
                if(activeTab === 1) {
                    historyStore.getProxy().setUrl(mRID);
                    widget.addHistoryGrid(me.getSixtyDaysFilter());
                    me.historyAdded = true;
                }
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', widget);
            }
        });
    },

    onTabChange: function (tabPanel, newTab) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            route,
            filter = {};

        router.arguments.mRID = tabPanel.mRID;
        Uni.util.History.suspendEventsForNextCall(true);
        if (newTab.itemId === 'history-service-calls-tab') {
            if (!me.historyAdded) {
                Ext.getStore('Mdc.store.servicecalls.ServiceCallHistory').getProxy().setUrl(tabPanel.mRID);
                me.getServiceCallsSetup().addHistoryGrid(me.getSixtyDaysFilter());
                me.historyAdded = true;
            }
            filter = me.getFilter().getFilterParams(false, true);
            route = router.getRoute('devices/device/servicecalls/history');
        } else if (newTab.itemId === 'running-service-calls-tab') {
            route = router.getRoute('devices/device/servicecalls');
        }
        route.forward({}, filter);
    },

    getSixtyDaysFilter: function() {
        var me = this,
            date,
            filter = {};

        date = new Date();
        date.setDate(date.getDate() - 60);
        filter.fromDate = date;
        date = new Date();
        filter.toDate = date;

        return filter;
    }
});

