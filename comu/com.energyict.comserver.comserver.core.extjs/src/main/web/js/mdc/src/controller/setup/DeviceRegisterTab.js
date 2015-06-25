Ext.define('Mdc.controller.setup.DeviceRegisterTab', {
    extend: 'Ext.app.Controller',
    stores: [
    ],
    models: [
    ],
    views: [
    ],
    refs: [
        {ref: 'registerTabPanel', selector: '#registerTabPanel'}
    ],
    veto: false,

    init: function () {
        var me = this;
        me.control({
            '#registerTabPanel': {
                tabChange: this.changeTab
            }
        });
    },

    initTabDeviceRegisterConfigurationDetailsView: function(mrId,registerId){
        this.mrId = mrId;
        this.registerId = registerId;
        var c = this.getController('Mdc.controller.setup.DeviceRegisterConfiguration');
        c.showDeviceRegisterConfigurationDetailsView(mrId,registerId,this);
        this.veto = true;
    },

    showTab: function(idx){
        this.getRegisterTabPanel().setActiveTab(idx);
        if(this.veto===true)this.veto=false;
    },

    initTabShowDeviceRegisterDataView: function(mrId,registerId){
        this.mrId = mrId;
        this.registerId = registerId;
        var c = this.getController('Mdc.controller.setup.DeviceRegisterData');
        c.showDeviceRegisterDataView(mrId,registerId,this);
        this.veto = true;
    },

    changeTab: function (tabPanel,tab) {
        if(!this.veto) {
            var router = this.getController('Uni.controller.history.Router'),
                routeParams = router.arguments,
                route,
                filterParams = {};
            if (tab.itemId === 'register-data') {
                routeParams.registerId = this.registerId;
                filterParams.suspect = 'suspect';
                route = 'devices/device/registers/registerdata';
                route && (route = router.getRoute(route));
                route && route.forward(routeParams, filterParams);
            } else if (tab.itemId === 'register-specifications') {
                routeParams.registerId = this.registerId;
                route = 'devices/device/registers/register';
                route && (route = router.getRoute(route));
                route && route.forward(routeParams, filterParams);
            }
        } else {
            this.veto = false;
        }
    }
});
