Ext.define('Mdc.controller.setup.ComServers', {
    extend: 'Ext.app.Controller',

    requires: [
        'Mdc.model.ComServer',
        'Mdc.model.ComPort',
        'Mdc.model.OutboundComPort',
        'Mdc.model.InboundComPort'
    ],

    views: [
        'setup.comserver.ComServersGrid',
        'setup.comserver.ComServerEdit',
        'setup.comserver.RemoteComServerEdit',
        'setup.comport.OutboundComPorts',
        'setup.comport.InboundComPorts',
        'setup.comport.OutboundComPortEdit',
        'setup.comport.InboundComPortEdit'
    ],

    stores: [
        'ComServers'
    ],

    refs: [
        {ref: 'comServerGrid',selector: 'viewport #comservergrid'},
        {ref: 'comServerEdit',selector:'comServerEdit'},
        {ref: 'remoteComServerEdit',selector:'remoteComServerEdit'},
        {ref: 'serialFieldSet',selector:'#serialFieldSet'},
        {ref: 'servletFieldSet',selector:'#servletFieldSet'},
        {ref: 'modemInitStringGrid',selector:'#modeminitstringgrid'},
        {ref: 'comServerPreview',selector:'#comserverpreview'}
    ],

    comserver: null,
    outboundComPortStore: null,
    inboundComPortStore: null,
    menuSelection: null,
    createComServerType: null,
    comServerEditView: null,
    comPortEditView: null,

    init: function () {
        var me = this;
        this.control({
            'comServersGrid': {
                itemdblclick: this.editComServer,
                selectionchange: this.showComServerPreview
            },
            'comServerEdit button[action=save]': {
                click: this.updateComServer
            },
            'comServerEdit button[action=cancel]': {
                click: this.cancel
            },
            'remoteComServerEdit button[action=save]': {
                click: this.updateComServer
            },
            'remoteComServerEdit button[action=cancel]': {
                click: this.cancel
            },
            'comServersGrid actioncolumn':{
                edit: this.editComServer,
                delete: this.delete,
                startStopComserver: this.startStopComserver
            }, 'comServersGrid button[action=add] menuitem': {
                click: this.add
            },
            'comServersGrid button[action=delete]': {
                click: this.delete
            },
            'outboundComPorts': {
                itemdblclick: this.editComPort
            },
            'outboundComPorts button[action=add]': {
                click: this.addOutboundComPort
            },
            'outboundComPorts button[action=delete]': {
                click: this.deleteComPort
            },
            'inboundComPorts': {
                itemdblclick: this.editComPort
            },
            'inboundComPorts button[action=add]': {
                click: this.addInboundComPort
            },
            'inboundComPorts button[action=delete]': {
                click: this.deleteComPort
            },
            'outboundComPortEdit button[action=save]': {
                click: this.updateOutboundComPort
            },
            'outboundComPortEdit button[action=cancel]': {
                click: this.cancelComPort
            },
            'inboundComPortEdit button[action=save]': {
                click: this.updateInboundComPort
            },
            'inboundComPortEdit button[action=cancel]': {
                click: this.cancelComPort
            },
            '#comPortTypeComboBox':{
                change: this.changeInboundComPortViewAccordingToComPortType
            },
            'modemInitStrings button[action=add]':{
                click: this.addModemInitString
            },
            'modemInitStrings button[action=delete]':{
                click: this.deleteModemInitString
            },
            'comServerPreview button[action=edit]':{
                click: this.editComServer
            },
            'comServerPreview button[action=startStop]':{
                click: this.startStopComserver
            }
        });
    },

    changeInboundComPortViewAccordingToComPortType: function(field,newValue,oldValue){
        if(newValue!==oldValue){
            var fieldSets = [
                {id:'SERIAL',field:this.getSerialFieldSet()},
                {id:'SERVLET',field:this.getServletFieldSet()}
            ];
            for(var i = 0;i<fieldSets.length;i++){
                fieldSets[i].field && fieldSets[i].field.setVisible(fieldSets[i].id===newValue);
            }
        }
    },

    showComServerPreview: function(grid,record){
        var selection = this.getComServerGrid().getSelectionModel().getSelection();
        var me = this;
        if(selection.length == 1){
            Ext.ModelManager.getModel('Mdc.model.ComServer').load(selection[0].getId(), {
                success: function (comserver) {
                    me.comserver = comserver;
                    me.getComServerPreview().down('form').loadRecord(me.comserver);
                    me.getComServerPreview().down('#previewpanel').expand();
                    me.outboundComPortStore = me.comserver.outboundComPorts();
                    me.inboundComPortStore = me.comserver.inboundComPorts();
                    me.getComServerPreview().down('#outboundcomportgrid').reconfigure(me.outboundComPortStore);
                    me.getComServerPreview().down('#inboundcomportgrid').reconfigure(me.inboundComPortStore);
                    me.getComServerPreview().down('#comserverName').update('<h3>'+me.comserver.get('name')+'</h3>');
                    me.getComServerPreview().down('#comserverActive').update({active:me.comserver.get('active')});
                }
            });
        } else {
            this.comserver = Ext.create(Mdc.model.ComServer);
            this.getComServerPreview().down('form').loadRecord(this.comserver);
            this.outboundComPortStore = this.comserver.outboundComPorts();
            this.inboundComPortStore = this.comserver.inboundComPorts();
            this.getComServerPreview().down('#outboundcomportgrid').reconfigure(this.outboundComPortStore);
            this.getComServerPreview().down('#inboundcomportgrid').reconfigure(this.inboundComPortStore);
            this.getComServerPreview().down('#comserverName').update('<h3>'+me.comserver.get('name')+'</h3>');
            me.getComServerPreview().down('#comserverActive').update({active:null});
            this.getComServerPreview().down('#previewpanel').collapse();
        }

    },

    editComServer: function () {
        this.comserver = this.getComServerGrid().getSelectionModel().getSelection()[0];
        this.showEditView(this.comserver.getId());
    },

    showEditView: function (id) {
        var me = this;
        if(id){
            Ext.ModelManager.getModel('Mdc.model.ComServer').load(id, {
                success: function (comserver) {
                    me.comserver=comserver
                    if(me.comserver.getData().comServerType === 'Remote'){
                        var view = Ext.widget('remoteComServerEdit');
                    } else {
                        var view = Ext.widget('comServerEdit');
                    }
                    view.down('form').loadRecord(comserver);
                    me.outboundComPortStore = me.comserver.outboundComPorts();
                    me.inboundComPortStore = me.comserver.inboundComPorts();
                    me.comServerEditView = view;
                }
            });
        } else {
            me.comserver = Ext.create(Mdc.model.ComServer);
            if(this.menuSelection.text==='Remote'){
                me.createComServerType = 'Remote';
                me.comServerEditView = Ext.widget('remoteComServerEdit');
            } else if(this.menuSelection.text==='Online'){
                me.createComServerType = 'Online';
                me.comServerEditView = Ext.widget('comServerEdit');
            } else {
                me.createComServerType = 'Mobile';
                me.comServerEditView = Ext.widget('comServerEdit');
            }
            me.comserver.set('comServerType',this.createComServerType);
        }

    },

    updateComServer: function (button) {
        var pnl = button.up('panel'),
            form = pnl.down('form'),
            record = this.comserver,
            values = form.getValues();
        if(!record){
            record = this.comserver;
            record.set(values);
            record.set('comServerType',this.createComServerType);
        } else {
            record.set(values);
        }

        this.saveComServer(this.comServerEditView);
    },

    cancel: function () {
        this.comServerEditView.close();
    },

    add: function (menuItem) {
        this.menuSelection = menuItem;
        this.showEditView();
    },

    delete: function () {
        var recordArray = this.getComServerGrid().getSelectionModel().getSelection();
        var me = this;
        var callbackCount = recordArray.length;
        if (recordArray.length > 0) {
                recordArray[0].destroy({
                    callback: function(){
                        callbackCount--;
                        if(callbackCount==0){
                            me.getComServerGrid().getStore().load();
                        }
                    }
                });
        }
    },

    editComPort: function (grid, record) {
        if(record instanceof Mdc.model.InboundComPort){
            this.comPortEditView = Ext.widget('inboundComPortEdit');
            this.comPortEditView.down('#serialFieldSet').setVisible(record.get('comPortType')==='SERIAL');
            this.comPortEditView.down('#servletFieldSet').setVisible(record.get('comPortType')==='SERVLET');
            this.comPortEditView.down('#modeminitstringgrid').reconfigure(record.modemInitStrings());
        } else if (record instanceof Mdc.model.OutboundComPort){
            this.comPortEditView = Ext.widget('outboundComPortEdit');
        }
        this.comPortEditView.down('form').loadRecord(record);
    },

    updateOutboundComPort: function(button){
        var pnl = button.up('panel'),
            form = pnl.down('form'),
            record = form.getRecord(),
            values = form.getValues();
        if(record){
            record.set(values);
        } else {
            record = Ext.create(Mdc.model.OutboundComPort);
            record.set(values);
            this.outboundComPortStore.add(record);

        }
        this.saveComServer(this.comPortEditView)
    },

    updateInboundComPort: function(button){
        var pnl = button.up('panel'),
            form = pnl.down('form'),
            record = form.getRecord(),
            values = form.getValues();
        if(record){
            record.set(values);
        } else {
            record = Ext.create(Mdc.model.InboundComPort);
            record.set(values);
            this.inboundComPortStore.add(record);
        }
        this.saveComServer(this.comPortEditView);


    },

    cancelComPort: function(){
        this.comPortEditView.close();
    },

    addOutboundComPort: function(){
        this.comPortEditView = Ext.widget('outboundComPortEdit');
    },

    addInboundComPort: function(){
        this.comPortEditView = Ext.widget('inboundComPortEdit');
    },

    deleteComPort: function(button){
        var grid = button.up('panel'),
            record = grid.getSelectionModel().getSelection()[0];
        grid.store.removeAt(grid.store.indexOf(record));
        this.saveComServer();

    },

    addModemInitString: function(){
        this.getModemInitStringGrid().store.add(Ext.create(Mdc.model.ModemInitString));
    },

    deleteModemInitString: function(){
        var grid = this.getModemInitStringGrid();
        var record = grid.getSelectionModel().getSelection()[0];
        grid.store.removeAt(grid.store.indexOf(record));
    },

    saveComServer: function(viewToClose){
        var me =this;
        this.comserver.save({
            callback: function (record) {
                me.getComServersStore().reload(
                    {
                        callback: function(){
                            me.showComServerPreview();
                            viewToClose && viewToClose.close();
                        }
                    });
            }
        });
    },

    startStopComserver: function(){
        this.comserver.set('active',!this.comserver.get('active'));
        this.saveComServer();
    }
});
