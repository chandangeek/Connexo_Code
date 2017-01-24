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

    showTab: function (idx) {
        this.getLoadProfileTabPanel().setActiveTab(idx);
        if (this.veto === true)this.veto = false;
    },

    initTabDeviceLoadProfileDetailsView: function (deviceId, loadProfileId) {
        this.deviceId = deviceId;
        this.loadProfileId = loadProfileId;
        var c = this.getController('Mdc.controller.setup.DeviceLoadProfileOverview');
        c.showOverview(deviceId, loadProfileId, this, null);
        this.veto = true;
    },

    initTabLoadProfileDataView: function (deviceId, loadProfileId) {
        this.deviceId = deviceId;
        this.loadProfileId = loadProfileId;
        var c = this.getController('Mdc.controller.setup.DeviceLoadProfileData'),
            loadProfile = this.getController('Mdc.controller.setup.DeviceLoadProfileOverview').getLoadProfile();
        c.showTableOverview(deviceId, loadProfileId, this, loadProfile);
        this.veto = true;
    },

    initTabLoadProfileGraphView: function (deviceId, loadProfileId) {
        this.deviceId = deviceId;
        this.loadProfileId = loadProfileId;
        var c = this.getController('Mdc.controller.setup.DeviceLoadProfileData'),
            loadProfile = this.getController('Mdc.controller.setup.DeviceLoadProfileOverview').getLoadProfile();
        c.showGraphOverview(deviceId, loadProfileId, this, loadProfile);
        this.veto = true;
    },

    changeTab: function (tabPanel, tab) {
        if (!this.veto) {
            var router = this.getController('Uni.controller.history.Router'),
                routeParams = router.arguments,
                route,
                filterParams = {};
            if (tab.itemId === 'loadProfile-data') {
                routeParams.loadProfileId = this.loadProfileId;
                route = 'devices/device/loadprofiles/loadprofiledata';
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

