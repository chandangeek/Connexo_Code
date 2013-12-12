Ext.define('Mdc.controller.setup.ComServers', {
    extend: 'Ext.app.Controller',

    requires: [
        'Mdc.model.ComServer',
        'Mdc.model.ComPort',
        'Mdc.model.OutboundComPort',
        'Mdc.model.InboundComPort'
    ],

    views: [
        'setup.comserver.ComServers',
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
        {ref: 'serialFieldSet',selector:'viewport #serialFieldSet'},
        {ref: 'servletFieldSet',selector:'viewport #servletFieldSet'},
        {ref: 'modemInitStringGrid',selector:'viewport #modeminitstringgrid'}
    ],

    outboundComPortStore: null,
    inboundComPortStore: null,
    menuSelection: null,
    createComServerType: null,

    init: function () {
        var me = this;
        this.control({
            'setupComServers': {
                itemdblclick: this.editComServer
            },
            'comServerEdit button[action=save]': {
                click: this.update
            },
            'comServerEdit button[action=cancel]': {
                click: this.cancel
            },
            'remoteComServerEdit button[action=save]': {
                click: this.update
            },
            'remoteComServerEdit button[action=cancel]': {
                click: this.cancel
            },
            'setupComServers button[action=add] menuitem': {
                click: this.add
            },
            'setupComServers button[action=delete]': {
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
            'outboundComPortEdit button[action=back]': {
                click: this.updateOutboundComPort
            },
            'outboundComPortEdit button[action=cancel]': {
                click: this.cancelComPort
            },
            'inboundComPortEdit button[action=back]': {
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
            }
        });
    },

    comServerEditView: null,

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

    editComServer: function (grid, record) {
        var url = Mdc.getApplication().getHistorySetupController().tokenizeBrowse('comservers', record.getId());
        Ext.History.add(url);
    },

    showEditView: function (id) {
        var me = this;

        if(id){
            Ext.ModelManager.getModel('Mdc.model.ComServer').load(id, {
                success: function (comserver) {
                    if(comserver.getData().comServerType === 'Remote'){
                        var view = Ext.widget('remoteComServerEdit');
                    } else {
                        var view = Ext.widget('comServerEdit');
                    }
                    view.down('form').loadRecord(comserver);
                    me.outboundComPortStore = comserver.outboundComPorts();
                    me.inboundComPortStore = comserver.inboundComPorts();
                    view.down('#outboundcomportgrid').reconfigure(me.outboundComPortStore);
                    view.down('#inboundcomportgrid').reconfigure(me.inboundComPortStore);
                    me.comServerEditView = view;
                    Mdc.getApplication().getMainController().showContent(view);
                }
            });
        } else {
            if(menuSelection.text==='Remote'){
                this.createComServerType = 'Remote';
                var view = Ext.widget('remoteComServerEdit');
            } else if(menuSelection.text==='Online'){
                this.createComServerType = 'Online';
                var view = Ext.widget('comServerEdit');
            } else {
                this.createComServerType = 'Mobile';
                var view = Ext.widget('comServerEdit');
            }
            Mdc.getApplication().getMainController().showContent(view);
        }

    },

    update: function (button) {
        var me = this;
        var pnl = button.up('panel'),
            form = pnl.down('form'),
            record = form.getRecord(),
            values = form.getValues();
        if(!record){
            record = Ext.create(Mdc.model.ComServer);
            record.set(values);
            record.set('comServerType',this.createComServerType);
        } else {
            record.set(values);
        }

        record.save({
            success: function (record, operation) {
                record.commit();
                me.getComServersStore().reload(
                    {
                        callback: function(){
                            me.showComServerOverview();
                    }
                });
            }
        });
    },

    cancel: function () {
        this.showComServerOverview();
    },

    showComServerOverview: function () {
        var url = Mdc.getApplication().getHistorySetupController().tokenizeBrowse('comservers');
        Ext.History.add(url);
    },

    add: function (menuItem) {
        menuSelection = menuItem;
        var url = Mdc.getApplication().getHistorySetupController().tokenizeAddComserver();
        Ext.History.add(url);
    },

    delete: function () {
        var recordArray = this.getComServerGrid().getSelectionModel().getSelection();
        if (recordArray.length > 0) {
            recordArray[0].destroy();
        }
    },

    editComPort: function (grid, record) {
        if(record instanceof Mdc.model.InboundComPort){
            var view = Ext.widget('inboundComPortEdit');
            view.down('#serialFieldSet').setVisible(record.get('comPortType')==='SERIAL');
            view.down('#servletFieldSet').setVisible(record.get('comPortType')==='SERVLET');
            view.down('#modeminitstringgrid').reconfigure(record.modemInitStrings());
        } else if (record instanceof Mdc.model.OutboundComPort){
            var view = Ext.widget('outboundComPortEdit');
        }
        view.down('form').loadRecord(record);


        Mdc.getApplication().getMainController().showContent(view);
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
        this.getComServerEdit().down('#outboundcomportgrid').getView().refresh();
        Mdc.getApplication().getMainController().showContent(this.comServerEditView);
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
        this.getComServerEdit().down('#inboundcomportgrid').getView().refresh();
        Mdc.getApplication().getMainController().showContent(this.comServerEditView);
    },

    cancelComPort: function(){
        Ext.History.back();
    },

    addOutboundComPort: function(){
        var view = Ext.widget('outboundComPortEdit');
        Mdc.getApplication().getMainController().showContent(view);
    },

    addInboundComPort: function(){
        var view = Ext.widget('inboundComPortEdit');
        Mdc.getApplication().getMainController().showContent(view);
    },

    deleteComPort: function(button){
        var grid = button.up('panel'),
            record = grid.getSelectionModel().getSelection()[0];
        grid.store.removeAt(grid.store.indexOf(record));

    },

    addModemInitString: function(button){
        this.getModemInitStringGrid().store.add(Ext.create(Mdc.model.ModemInitString));
    },

    deleteModemInitString: function(button){
        var grid = this.getModemInitStringGrid();
        var record = grid.getSelectionModel().getSelection()[0];
        grid.store.removeAt(grid.store.indexOf(record));
    }
});
