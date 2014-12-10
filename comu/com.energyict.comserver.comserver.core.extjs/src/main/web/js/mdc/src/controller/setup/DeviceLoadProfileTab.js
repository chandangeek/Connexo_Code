Ext.define('Mdc.controller.setup.DeviceLoadProfileTab', {
    extend: 'Ext.app.Controller',
    stores: [
    ],
    models: [
    ],
    views: [
        'Mdc.view.setup.deviceloadprofiles.TabbedDeviceLoadProfilesView'
    ],
    refs: [
        {ref: 'loadProfileTabPanel', selector: '#loadProfileTabPanel'}
    ],
    veto: false,

    init: function () {
        var me = this;
        me.control({
            '#loadProfileTabPanel': {
                tabChange: this.changeTab
            }
        });
    },

    showTab: function(idx){
        this.getLoadProfileTabPanel().setActiveTab(idx);
        if(this.veto===true)this.veto=false;
    },

    initTabDeviceLoadProfileDetailsView: function(mrId,loadProfileId){
        debugger;
        this.mrId = mrId;
        this.loadProfileId = loadProfileId;
        var c = this.getController('Mdc.controller.setup.DeviceLoadProfileOverview');
        c.showOverview(mrId,loadProfileId,this);
        this.veto = true;
    },

    initTabLoadProfileDataView: function(mrId,loadProfileId){
        this.mrId = mrId;
        this.loadProfileId = loadProfileId;
        var c = this.getController('Mdc.controller.setup.DeviceLoadProfileData');
        c.showTableOverview(mrId,loadProfileId,this);
        this.veto = true;
    },

    initTabLoadProfileGraphView: function(mrId,loadProfileId){
        this.mrId = mrId;
        this.loadProfileId = loadProfileId;
        var c = this.getController('Mdc.controller.setup.DeviceLoadProfileData');
        c.showGraphOverview(mrId,loadProfileId,this);
        this.veto = true;
    },

    changeTab: function (tabPanel,tab) {
        debugger;
        if(!this.veto) {
            var router = this.getController('Uni.controller.history.Router'),
                routeParams = router.arguments,
                route,
                filterParams = {};
            if (tab.itemId === 'loadProfile-data') {
                routeParams.loadProfileId = this.loadProfileId;
                filterParams.onlySuspect = false;
                route = 'devices/device/loadprofiles/loadprofile/data';
                route && (route = router.getRoute(route));
                route && route.forward(routeParams, filterParams);
            } else if (tab.itemId === 'loadProfile-specifications') {
                routeParams.loadProfileId = this.loadProfileId;
                route = 'devices/device/loadprofiles/loadprofile';
                route && (route = router.getRoute(route));
                route && route.forward(routeParams, filterParams);
            }
        } else {
            this.veto = false;
        }
    }
});

