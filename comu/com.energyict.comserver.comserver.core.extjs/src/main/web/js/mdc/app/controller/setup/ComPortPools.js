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
        'setup.comportpool.InboundComPortPoolEdit',
        'setup.comport.OutboundComPortSelectionWindow'
    ],

    stores: [
        'ComPortPools',
        'ComPorts'
    ],

    refs: [
        {ref: 'comPortPoolGrid',selector: 'viewport #comportpoolgrid'},
        {ref: 'outboundComPortSelectionGrid',selector: '#outboundComPortSelectionGrid'},
        {ref: 'outboundComPortGrid',selector: 'viewport #pooloutboundcomportgrid'}
    ],

    outboundComPortStore: null,
    inboundComPortStore: null,
    menuSelection: null,
    comPortDirection: null,
    comPortPool: null,
    outboundComPortSelectionWindow: null,

    init: function () {
        var me = this;
        this.control({
            'setupComPortPools': {
                itemdblclick: this.editComPortPool
            },
            'inboundComPortPoolEdit button[action=save]': {
                click: this.update
            },
            'inboundComPortPoolEdit button[action=cancel]': {
                click: this.cancel
            },
            'outboundComPortPoolEdit button[action=save]': {
                click: this.update
            },
            'outboundComPortPoolEdit button[action=cancel]': {
                click: this.cancel
            },
            'outboundComPortSelectionWindow button[action=select]': {
                click: this.selectComPort
            },
            'outboundComPortSelectionWindow button[action=cancel]': {
                click: this.closeOutboundComPortSelectionWindow
            },
            'setupComPortPools button[action=add] menuitem': {
                click: this.add
            },
            'setupComPortPools button[action=delete]': {
                click: this.delete
            },
            'poolOutboundComPorts button[action=add]': {
                click: this.addOutboundComPort
            },
            'poolOutboundComPorts button[action=delete]': {
                click: this.removeOutboundComPort
            }
        });
    },

    comServerEditView: null,

    editComPortPool: function (grid, record) {
        var url = Mdc.getApplication().getHistorySetupController().tokenizeBrowse('comportpools', record.getId());
        Ext.History.add(url);
    },

    showEditView: function (id) {
        var me = this;
        if(id){
            Ext.ModelManager.getModel('Mdc.model.ComPortPool').load(id, {
                success: function (comPortPool) {
                    me.comPortPool = comPortPool
                    if(me.comPortPool.getData().direction === 'inbound'){
                        var view = Ext.widget('inboundComPortPoolEdit');
                        me.comPortDirection = 'inbound';
                        me.inboundComPortStore = me.comPortPool.inboundComPorts();
                        view.down('#poolinboundcomportgrid').reconfigure(me.inboundComPortStore);
                    } else {
                        var view = Ext.widget('outboundComPortPoolEdit');
                        me.comPortDirection = 'outbound';
                        me.outboundComPortStore = me.comPortPool.outboundComPorts();
                        view.down('#pooloutboundcomportgrid').reconfigure(me.outboundComPortStore);
                    }
                    view.down('form').loadRecord(me.comPortPool);
                    Mdc.getApplication().getMainController().showContent(view);
                }
            });
        }
        else {
            this.comPortPool = Ext.create(Mdc.model.ComPortPool);
            if(this.menuSelection.text==='Inbound'){
                var view = Ext.widget('inboundComPortPoolEdit');
                this.comPortDirection = 'inbound';
                this.inboundComPortStore = this.comPortPool.inboundComPorts();
                view.down('#poolinboundcomportgrid').reconfigure(this.inboundComPortStore);
            } else {
                var view = Ext.widget('outboundComPortPoolEdit');
                this.comPortDirection = 'outbound';
                this.outboundComPortStore = this.comPortPool.outboundComPorts();
                view.down('#pooloutboundcomportgrid').reconfigure(this.outboundComPortStore);
            }
            Mdc.getApplication().getMainController().showContent(view);
        }
    },

    update: function (button) {
        var me = this;
        var pnl = button.up('panel'),
            form = pnl.down('form'),
            values = form.getValues();
        this.comPortPool.set(values);
        this.comPortPool.set('direction',this.comPortDirection);
        this.comPortPool.save({
            success: function (record, operation) {
               me.getComPortPoolsStore().reload({
                   callback: function(){
                       me.showComPortPoolOverview();
                   }
               })
            }
        });
    },

    cancel: function () {
        this.showComPortPoolOverview();
    },

    showComPortPoolOverview: function () {
        var url = Mdc.getApplication().getHistorySetupController().tokenizeBrowse('comportpools');
        Ext.History.add(url);
    },

    add: function (menuItem) {
        this.menuSelection = menuItem;
        var url = Mdc.getApplication().getHistorySetupController().tokenizeAddComPortPool();
        Ext.History.add(url);
    },

    delete: function () {
        var recordArray = this.getComPortPoolGrid().getSelectionModel().getSelection();
        if (recordArray.length > 0) {
            recordArray[0].destroy();
        }
    },

    addOutboundComPort: function(){
        var store = this.outboundComPortStore;
        var popupStore = Ext.data.StoreManager.lookup('ComPorts');
        popupStore.filter('direction','outbound');
        popupStore.load({
            scope: this,
            callback: function(){
                store.each(function(rec){
                    var toRemove;
                    popupStore.each(function(s2rec,idx){
                      if(s2rec.get('id')===rec.get('id')){
                          toRemove=idx;
                      }
                   },this);
                    popupStore.removeAt(toRemove);
                },this);
                this.outboundComPortSelectionWindow = Ext.widget('outboundComPortSelectionWindow');
                this.getOutboundComPortSelectionGrid().reconfigure(popupStore)
            }
        });
    },

    removeOutboundComPort: function(){
        var comport = this.getOutboundComPortGrid().getSelectionModel().getSelection()[0];
        this.outboundComPortStore.removeAt(this.outboundComPortStore.indexOf(comport));
    },

    selectComPort: function(){
        if(this.getOutboundComPortSelectionGrid().getSelectionModel().hasSelection()){
            var comport = this.getOutboundComPortSelectionGrid().getSelectionModel().getSelection()[0];
            this.outboundComPortStore.add(comport);
        }
        this.outboundComPortSelectionWindow.close();
    },

    closeOutboundComPortSelectionWindow: function(){
        this.outboundComPortSelectionWindow.close();
    }

});

