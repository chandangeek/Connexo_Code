Ext.define('Mdc.controller.setup.ComPortPoolComPortsView', {
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
            ref: 'comPortPoolsComPortsView',
            selector: 'comPortPoolsComPortsView'
        },
        {
            ref: 'preview',
            selector: 'comPortPoolsComPortsView comPortPoolComPortPreview'
        },
        {
            ref: 'comPortsGrid',
            selector: 'comPortPoolsComPortsView comPortPoolComPortsGrid'
        }
    ],

    init: function () {
        this.control({
            'comPortPoolsComPortsView comPortPoolComPortsGrid': {
                select: this.showPreview
            },
            'comPortPoolsComPortsView comPortPoolComPortPreview [action=passwordVisibleTrigger]': {
                change: this.passwordVisibleTrigger
            },
            'comPortPoolsComPortsView button[action=addComPort]': {
                click: this.addComPort
            },
            'comPortPoolComPortsActionMenu': {
                click: this.chooseAction
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

        comPortModel.getProxy().url = url;
        comPortsStore.getProxy().url = url;
        me.checkLoadingOfNecessaryStores(function () {
            widget = Ext.widget('comPortPoolsComPortsView');
            me.getApplication().fireEvent('changecontentevent', widget);
            comPortPoolModel.load(id, {
                success: function (record) {
                    widget.down('comportpoolsubmenu').setServer(record);
                    me.getApplication().fireEvent('comPortPoolOverviewLoad', record);
                }
            });
        }, storesArr);
    },

    addComPort: function () {
//        todo: needs to implement JP-3694 "Add communication port on communication port pool"
    },

    deleteComPort: function (record) {
        var me = this,
            page = me.getComPortPoolsComPortsView(),
            grid = me.getComPortsGrid(),
            gridToolbarTop = grid.down('pagingtoolbartop');

        page.setLoading(Uni.I18n.translate('general.removing', 'MDC', 'Removing...'));
        record.destroy({
            callback: function (model, operation) {
                page.setLoading(false);
                if (operation.wasSuccessful()) {
                    gridToolbarTop.totalCount = 0;
                    grid.getStore().loadPage(1);
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('comServerComPorts.deleteSuccess.msg', 'MDC', 'Communication port removed'));
                }
            }
        });
    }
});