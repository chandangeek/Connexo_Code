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

    refs: [
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
            router = me.getController('Uni.controller.history.Router'),
            store = Ext.getStore('Scs.store.ServiceCalls'),
            viewport = Ext.ComponentQuery.query('viewport')[0],
            widget;

       store.setProxy({
            type: 'rest',
            url: '/api/ddr/devices/' + mRID +'/runningservicecalls',
            timeout: 120000,
            reader: {
                type: 'json',
                root: 'serviceCalls'
            }
        });
        Ext.ModelManager.getModel('Mdc.model.Device').load(mRID, {
            success: function (device) {
                widget = Ext.widget('service-calls-setup', {device: device, router: router, store: store});
                me.getApplication().fireEvent('loadDevice', device);
                me.getApplication().fireEvent('changecontentevent', widget);
            }
        });
    },

    onTabChange: function (tabPanel, nawTab) {
        var me = this,
            store = Ext.getStore('Scs.store.ServiceCalls');

        if(nawTab.itemId === 'history-service-call-preview-container') {
            store.setProxy({
                type: 'rest',
                url: '/api/ddr/devices/' + tabPanel.mRID +'/servicecallhistory',
                timeout: 120000,
                reader: {
                    type: 'json',
                    root: 'serviceCalls'
                }
            });

        } else if (newTab.itemId === 'running-service-calls-tab') {
            store.setProxy({
                type: 'rest',
                url: '/api/ddr/devices/' + tabPanel.mRID +'/runningservicecalls',
                timeout: 120000,
                reader: {
                    type: 'json',
                    root: 'serviceCalls'
                }
            });
        }
    }
});

