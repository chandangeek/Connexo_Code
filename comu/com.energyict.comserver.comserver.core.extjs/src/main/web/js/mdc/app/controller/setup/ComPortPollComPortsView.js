Ext.define('Mdc.controller.setup.ComPortPollComPortsView', {
    extend: 'Mdc.controller.setup.ComServerComPortsView',

    models: [
        'Mdc.model.ComPortPool',
        'Mdc.model.ComServerComPort'
    ],

    views: [
        'Mdc.view.setup.comportpollcomports.View'
    ],

    refs: [
        {
            ref: 'preview',
            selector: 'comPortPoolsComPortsView comServerComPortPreview'
        }
    ],

    init: function () {
        this.control({
            'comPortPoolsComPortsView comPortPoolComPortsGrid': {
                select: this.showPreview
            },
            'comPortPoolsComPortsView comServerComPortPreview [action=passwordVisibleTrigger]': {
                change: this.passwordVisibleTrigger
            },
            'comPortPoolsComPortsView button[action=addComPort]': {
                click: this.addComPort
            }
        });
    },

    showView: function (id) {
        var me = this,
            comPortPoolModel = me.getModel('Mdc.model.ComPortPool'),
            comPortModel = me.getModel('Mdc.model.ComServerComPort'),
            comPortsStore = me.getStore('Mdc.store.ComServerComPorts'),
            url = comPortPoolModel.getProxy().url + '/' + id + '/comports',
            storesArr = [
                'Mdc.store.ComPortPools',
                'Mdc.store.ComServers',
                'Mdc.store.BaudRates',
                'Mdc.store.NrOfDataBits',
                'Mdc.store.NrOfStopBits',
                'Mdc.store.Parities',
                'Mdc.store.FlowControls'
            ],
            widget;

        comPortPoolModel.load(id, {
            success: function (record) {
                me.getApplication().fireEvent('comPortPoolOverviewLoad', record);
            }
        });

        comPortModel.getProxy().url = url;
        comPortsStore.getProxy().url = url;
        me.checkLoadingOfNecessaryStores(function () {
            widget = Ext.widget('comPortPoolsComPortsView');
            me.getApplication().fireEvent('changecontentevent', widget);
        }, storesArr);
    },

    addComPort: function () {
//        todo: needs to implement JP-3694 "Add communication port on communication port pool"
    }
});