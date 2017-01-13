Ext.define('Mdc.controller.setup.DeviceLogBookTab', {
    extend: 'Ext.app.Controller',
    stores: [
    ],
    models: [
    ],
    views: [
    ],
    refs: [
        {ref: 'logBookTabPanel', selector: '#logBookTabPanel'}
    ],
    veto: false,

    init: function () {
        var me = this;
        me.control({
            '#logBookTabPanel': {
                tabChange: this.changeTab
            }
        });
    },

    showOverview: function (deviceId, logBookId) {
        this.deviceId = deviceId;
        this.logBookId = logBookId;

        var c = this.getController('Mdc.controller.setup.DeviceLogbookOverview');
        this.veto = true;
        c.showOverview(deviceId, logBookId, this);
    },

    showTab: function(idx){
        this.getLogBookTabPanel().setActiveTab(idx);
        if(this.veto===true)this.veto=false;
    },

    showData: function (deviceId, logBookId) {
        this.logBookId = logBookId;
        var c = this.getController('Mdc.controller.setup.DeviceLogbookData');
        this.veto = true;
        c.showOverview(deviceId, logBookId, this);
    },

    changeTab: function (tabPanel,tab) {
        if(!this.veto) {
            var router = this.getController('Uni.controller.history.Router'),
                routeParams = router.arguments,
                route,
                filterParams = {};
            if (tab.itemId === 'logBook-data') {
                routeParams.logbookId = this.logBookId;
                route = 'devices/device/logbooks/logbookdata';
                route && (route = router.getRoute(route));
                route && route.forward(routeParams, filterParams);
            } else if (tab.itemId === 'logBook-specifications') {
                routeParams.logbookId = this.logBookId;
                route = 'devices/device/logbooks/logbookoverview';
                route && (route = router.getRoute(route));
                route && route.forward(routeParams, filterParams);
            }
        } else {
            this.veto = false;
        }
    }
});
