Ext.define('Mdc.controller.setup.ComPortPools', {
    extend: 'Ext.app.Controller',

    requires: [
        'Mdc.model.ComPortPool',
        'Mdc.model.ComPort',
        'Mdc.model.OutboundComPort',
        'Mdc.model.InboundComPort'
    ],

    views: [
        'setup.comportpool.ComPortPools',
        'setup.comportpool.OutboundComPortPoolEdit',
        'setup.comportpool.InboundComPortPoolEdit'
    ],

    stores: [
        'ComPortPools'
    ],

    refs: [
        {ref: 'comPortPoolGrid',selector: 'viewport #comportpoolgrid'}
    ],

//    outboundComPortStore: null,
//    inboundComPortStore: null,
    menuSelection: null,

    init: function () {
        var me = this;
        this.control({
            'setupComPortPools': {
                itemdblclick: this.editComPortPool
            },
//            'InboundCompPortPoolEdit button[action=save]': {
//                click: this.update
//            },
//            'InboundCompPortPoolEdit button[action=cancel]': {
//                click: this.cancel
//            },
//            'OutboundCompPortPoolEdit button[action=save]': {
//                click: this.update
//            },
//            'OutboundCompPortPoolEdit button[action=cancel]': {
//                click: this.cancel
//            },
//            'remoteComServerEdit button[action=save]': {
//                click: this.update
//            },
//            'remoteComServerEdit button[action=cancel]': {
//                click: this.cancel
//            },
            'setupComPortPools button[action=add] menuitem': {
                click: this.add
            }
//            'setupComServers button[action=delete]': {
//                click: this.delete
//            },
//            'outboundComPorts': {
//                itemdblclick: this.editComPort
//            },
//            'outboundComPorts button[action=add]': {
//                click: this.addOutboundComPort
//            },
//            'outboundComPorts button[action=delete]': {
//                click: this.deleteComPort
//            },
//            'inboundComPorts': {
//                itemdblclick: this.editComPort
//            },
//            'inboundComPorts button[action=add]': {
//                click: this.addInboundComPort
//            },
//            'inboundComPorts button[action=delete]': {
//                click: this.deleteComPort
//            },
//            'outboundComPortEdit button[action=save]': {
//                click: this.updateOutboundComPort
//            },
//            'outboundComPortEdit button[action=cancel]': {
//                click: this.cancelComPort
//            },
//            'inboundComPortEdit button[action=save]': {
//                click: this.updateInboundComPort
//            },
//            'inboundComPortEdit button[action=cancel]': {
//                click: this.cancelComPort
//            }

        });
    },

    comServerEditView: null,

    editComPortPool: function (grid, record) {
        var url = Mdc.getApplication().getHistorySetupController().tokenizeBrowse('comportpools', record.getId());
        Ext.History.add(url);
    },

    showEditView: function (id) {
        console.log('tweet');
        var me = this;

        if(id){
            Ext.ModelManager.getModel('Mdc.model.ComPortPool').load(id, {
                success: function (comPortPool) {
                    console.log(comPortPool.getData().id);
                    if(comPortPool.getData().direction === 'inbound'){
                        var view = Ext.widget('inboundComPortPoolEdit');
                    } else {
                        var view = Ext.widget('outboundComPortPoolEdit');
                    }
//                    view.down('form').loadRecord(comserver);
//                    me.outboundComPortStore = comserver.outboundComPorts();
//                    me.inboundComPortStore = comserver.inboundComPorts();
//                    view.down('#outboundcomportgrid').reconfigure(me.outboundComPortStore);
//                    view.down('#inboundcomportgrid').reconfigure(me.inboundComPortStore);
//                    me.comServerEditView = view;

                    view.down('form').loadRecord(comPortPool);
                    Mdc.getApplication().getMainController().showContent(view);
                }
            });
        }
        else {
            if(menuSelection.text==='inbound'){
                var view = Ext.widget('inboundComPortPoolEdit');
            } else {
                var view = Ext.widget('outboundComPortPoolEdit');
            }
            Mdc.getApplication().getMainController().showContent(view);
        }
//
    },

//    update: function (button) {
//        var me = this;
//        var pnl = button.up('panel'),
//            form = pnl.down('form'),
//            record = form.getRecord(),
//            values = form.getValues();
//        record.set(values);
//        record.save({
//            success: function (record, operation) {
//               // me.showComServerOverview();
//            }
//        });
//    },
//
//    cancel: function () {
//       // this.showComServerOverview();
//    },
//
//    showComServerOverview: function () {
//        var url = Mdc.getApplication().getHistorySetupController().tokenizeBrowse('comservers');
//        Ext.History.add(url);
//    },
//
    add: function (menuItem) {
        menuSelection = menuItem;
        var url = Mdc.getApplication().getHistorySetupController().tokenizeAddComPortPool();
        Ext.History.add(url);
    }
//
//    delete: function () {
//        var recordArray = this.getComServerGrid().getSelectionModel().getSelection();
//        if (recordArray.length > 0) {
//            recordArray[0].destroy();
//        }
//    },
//
//    editComPort: function (grid, record) {
//        if(record instanceof Mdc.model.InboundComPort){
//            var view = Ext.widget('inboundComPortEdit');
//        } else if (record instanceof Mdc.model.OutboundComPort){
//            var view = Ext.widget('outboundComPortEdit');
//        }
//        view.down('form').loadRecord(record);
//        Mdc.getApplication().getMainController().showContent(view);
//    },
//
//    updateOutboundComPort: function(button){
//        var pnl = button.up('panel'),
//            form = pnl.down('form'),
//            record = form.getRecord(),
//            values = form.getValues();
//        if(record){
//            record.set(values);
//        } else {
//            record = Ext.create(Mdc.model.OutboundComPort);
//            record.set(values);
//            this.outboundComPortStore.add(record);
//
//        }
//        this.getComServerEdit().down('#outboundcomportgrid').getView().refresh();
//        Mdc.getApplication().getMainController().showContent(this.comServerEditView);
//    },
//
//    updateInboundComPort: function(button){
//        var pnl = button.up('panel'),
//            form = pnl.down('form'),
//            record = form.getRecord(),
//            values = form.getValues();
//        if(record){
//            record.set(values);
//        } else {
//            record = Ext.create(Mdc.model.InboundComPort);
//            record.set(values);
//            this.inboundComPortStore.add(record);
//
//        }
//        this.getComServerEdit().down('#inboundcomportgrid').getView().refresh();
//        Mdc.getApplication().getMainController().showContent(this.comServerEditView);
//    },
//
//    cancelComPort: function(){
//        console.log('cancel');
//        Ext.History.back();
//    },
//
//    addOutboundComPort: function(){
//        var view = Ext.widget('outboundComPortEdit');
//        Mdc.getApplication().getMainController().showContent(view);
//    },
//
//    addInboundComPort: function(){
//        var view = Ext.widget('inboundComPortEdit');
//        Mdc.getApplication().getMainController().showContent(view);
//    },
//
//    deleteComPort: function(button){
//        var grid = button.up('panel'),
//            record = grid.getSelectionModel().getSelection()[0];
//        grid.store.removeAt(grid.store.indexOf(record));
//
//    }
});

