Ext.define('Mdc.controller.setup.DeviceChannelTab', {
    extend: 'Ext.app.Controller',
    stores: [
    ],
    models: [
    ],
    views: [
        'Mdc.view.setup.devicechannels.TabbedDeviceChannelsView'
    ],
    refs: [
        {ref: 'channelTabPanel', selector: '#channelTabPanel'}
    ],
    veto: false,

    init: function () {
        var me = this;
        me.control({
//            '#channelTabPanel': {
//                tabChange: this.changeTab
//            }
        });
    },

    showTab: function(idx){
        this.getChannelTabPanel().setActiveTab(idx);
        if(this.veto===true)this.veto=false;
    },

    initTabDeviceChannelDetailsView: function(mrId,channelId){
        console.log('overview');
        this.mrId = mrId;
        this.channelId = channelId;
        var c = this.getController('Mdc.controller.setup.DeviceChannelOverview');
        c.showOverview(mrId,channelId,this);
        this.veto = true;
    },

    initTabChannelDataView: function(mrId,channelId){
        console.log('table');
        this.mrId = mrId;
        this.channelId = channelId;
        var c = this.getController('Mdc.controller.setup.DeviceChannelData');
        c.showTableOverview(mrId,channelId,this);
        this.veto = true;
    },

    initTabChannelGraphView: function(mrId,channelId){
        console.log('graph');
        this.mrId = mrId;
        this.channelId = channelId;
        var c = this.getController('Mdc.controller.setup.DeviceChannelData');
        c.showGraphOverview(mrId,channelId,this);
        this.veto = true;
    },

    changeTab: function (tabPanel,tab) {
        if(!this.veto) {
            var router = this.getController('Uni.controller.history.Router'),
                routeParams = router.arguments,
                route,
                filterParams = {};
            if (tab.itemId === 'channel-data') {
           //     routeParams.channelId = this.channelId;
                filterParams.onlySuspect = false;
                route = 'devices/device/channels/channeldata';
                route && (route = router.getRoute(route));
                route && route.forward(routeParams, filterParams);
            } else if (tab.itemId === 'channel-specifications') {
             //   routeParams.channelId = this.channelId;
                route = 'devices/device/channels/channel';
                route && (route = router.getRoute(route));
                route && route.forward(routeParams, filterParams);
            }
        } else {
            this.veto = false;
        }
    }
});