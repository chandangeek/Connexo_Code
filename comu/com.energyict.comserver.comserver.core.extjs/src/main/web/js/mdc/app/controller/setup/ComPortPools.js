Ext.define('Mdc.controller.setup.ComPortPools', {
    extend: 'Ext.app.Controller',

    requires: [
        'Mdc.model.ComPortPool',
        'Mdc.model.ComPort',
        'Mdc.model.OutboundComPort',
        'Mdc.model.InboundComPort'
    ],

    views: [
        'setup.comportpool.ComPortPoolsSetup',
        'setup.comportpool.OutboundComPortPoolEdit',
        'setup.comportpool.InboundComPortPoolEdit',
        'setup.comport.OutboundComPortSelectionWindow'
    ],

    stores: [
        'ComPortPools',
        'ComPorts'
    ],

    refs: [
        {ref: 'comPortPoolGrid', selector: 'viewport #comportpoolgrid'},
        {ref: 'outboundComPortSelectionGrid', selector: '#outboundComPortSelectionGrid'},
        {ref: 'outboundComPortGrid', selector: 'viewport #pooloutboundcomportgrid'},
        {ref: 'comPortPoolPreview', selector: '#comportpoolpreview'}
    ],

    outboundComPortStore: null,
    inboundComPortStore: null,
    menuSelection: null,
    comPortDirection: null,
    comPortPool: null,
    outboundComPortSelectionWindow: null,
    comPortPoolEditView: null,

    init: function () {
        var me = this;
        this.control({
            'comPortPoolsGrid': {
                itemdblclick: this.editComPortPool,
                selectionchange: this.showComComPortPoolPreview
            },
            'comPortPoolsGrid actioncolumn': {
                edit: this.editComPortPool,
                deleteItem: this.deleteComPortPool
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
            'comPortPoolsGrid button[action=add] menuitem': {
                click: this.add
            },
            'comPortPoolsGrid button[action=delete]': {
                click: this.deleteComPortPool
            },
            'poolOutboundComPorts button[action=add]': {
                click: this.addOutboundComPort
            },
            'poolOutboundComPorts button[action=delete]': {
                click: this.removeOutboundComPort
            },
            'comPortPoolPreview button[action=edit]': {
                click: this.editComPortPool
            },
            'comPortPoolPreview button[action=startStop]': {
                click: this.startStopComserver
            }
        });
    },

    comServerEditView: null,

    editComPortPool: function (grid, record) {
        this.comPortPool = this.getComPortPoolGrid().getSelectionModel().getSelection()[0];
        this.showEditView(this.comPortPool.getId());
    },

    showEditView: function (id) {
        var me = this;
        if (id) {
            Ext.ModelManager.getModel('Mdc.model.ComPortPool').load(id, {
                success: function (comPortPool) {
                    me.comPortPool = comPortPool
                    if (me.comPortPool.getData().direction === 'inbound') {
                        me.comPortPoolEditView = Ext.widget('inboundComPortPoolEdit');
                        me.comPortDirection = 'inbound';
                        me.inboundComPortStore = me.comPortPool.inboundComPorts();
                    } else {
                        me.comPortPoolEditView = Ext.widget('outboundComPortPoolEdit');
                        me.comPortDirection = 'outbound';
                        me.outboundComPortStore = me.comPortPool.outboundComPorts();
                    }
                    me.comPortPoolEditView.down('form').loadRecord(me.comPortPool);
                }
            });
        }
        else {
            this.comPortPool = Ext.create(Mdc.model.ComPortPool);
            if (this.menuSelection.text === 'Inbound') {
                this.comPortPoolEditView = Ext.widget('inboundComPortPoolEdit');
                this.comPortDirection = 'inbound';
                this.inboundComPortStore = this.comPortPool.inboundComPorts();
            } else {
                this.comPortPoolEditView = Ext.widget('outboundComPortPoolEdit');
                this.comPortDirection = 'outbound';
                this.outboundComPortStore = this.comPortPool.outboundComPorts();
            }
            me.getApplication().getController('Mdc.controller.Main').showContent(view);
        }
    },

    update: function (button) {
        var me = this;
        var pnl = button.up('panel'),
            form = pnl.down('form'),
            values = form.getValues();
        this.comPortPool.set(values);
        this.comPortPool.set('direction', this.comPortDirection);
        this.saveComPortPool(this.comPortPoolEditView);
    },

    cancel: function () {
        this.comPortPoolEditView.close();
    },

    add: function (menuItem) {
        this.menuSelection = menuItem;
        var url = this.getApplication().getHistorySetupController().tokenizeAddComPortPool();
        Ext.History.add(url);
    },

    deleteComPortPool: function (record) {
        //var recordArray = this.getComPortPoolGrid().getSelectionModel().getSelection();
        var me = this;
        //var callbackCount = recordArray.length;

        //if (recordArray.length > 0) {
        //  recordArray[0].destroy({
        record.destory({
            callback: function () {
                // callbackCount--;
                // if(callbackCount==0){
                me.getComPortPoolGrid().getStore().load();
                // }
            }
        });
        //}
    },

    addOutboundComPort: function () {
        var store = this.outboundComPortStore;
        var popupStore = Ext.data.StoreManager.lookup('ComPorts');
        popupStore.filter('direction', 'outbound');
        popupStore.load({
            scope: this,
            callback: function () {
                store.each(function (rec) {
                    var toRemove;
                    popupStore.each(function (s2rec, idx) {
                        if (s2rec.get('id') === rec.get('id')) {
                            toRemove = idx;
                        }
                    }, this);
                    popupStore.removeAt(toRemove);
                }, this);
                this.outboundComPortSelectionWindow = Ext.widget('outboundComPortSelectionWindow');
                this.getOutboundComPortSelectionGrid().reconfigure(popupStore)
            }
        });
    },

    removeOutboundComPort: function () {
        var comport = this.getOutboundComPortGrid().getSelectionModel().getSelection()[0];
        this.outboundComPortStore.removeAt(this.outboundComPortStore.indexOf(comport));
        this.saveComPortPool();
    },

    selectComPort: function () {
        if (this.getOutboundComPortSelectionGrid().getSelectionModel().hasSelection()) {
            var comport = this.getOutboundComPortSelectionGrid().getSelectionModel().getSelection()[0];
            this.outboundComPortStore.add(comport);
            this.saveComPortPool(this.outboundComPortSelectionWindow);
        }
    },

    closeOutboundComPortSelectionWindow: function () {
        this.outboundComPortSelectionWindow.close();
    },

    showComComPortPoolPreview: function () {
        var selection = this.getComPortPoolGrid().getSelectionModel().getSelection();
        var me = this;
        if (selection.length == 1) {
            Ext.ModelManager.getModel('Mdc.model.ComPortPool').load(selection[0].getId(), {
                success: function (comPortPool) {
                    me.comPortPool = comPortPool;
                    me.getComPortPoolPreview().down('form').loadRecord(me.comPortPool);
                    me.getComPortPoolPreview().down('#previewpanel').expand();
                    me.getComPortPoolPreview().down('form').removeAll();

                    if (me.comPortPool.get('direction') === 'outbound') {
                        me.getComPortPoolPreview().down('form').add(Ext.widget('poolOutboundComPorts'));
                        me.outboundComPortStore = me.comPortPool.outboundComPorts();
                        me.getComPortPoolPreview().down('#pooloutboundcomportgrid').reconfigure(me.outboundComPortStore);
                    } else {
                        me.getComPortPoolPreview().down('form').add(Ext.widget('poolInboundComPorts'));
                        me.inboundComPortStore = me.comPortPool.inboundComPorts();
                        me.getComPortPoolPreview().down('#poolinboundcomportgrid').reconfigure(me.inboundComPortStore);
                    }
                    me.getComPortPoolPreview().down('#comPortPoolName').update('<h3>' + me.comPortPool.get('name') + '</h3>');
                    me.getComPortPoolPreview().down('#comPortPoolActive').update({active: me.comPortPool.get('active')});
                }
            });
        } else {
            this.comPortPool = Ext.create(Mdc.model.ComPortPool);
            this.getComPortPoolPreview().down('form').loadRecord(this.comPortPool);
            this.outboundComPortStore = this.comPortPool.outboundComPorts();
            this.inboundComPortStore = this.comPortPool.inboundComPorts();
            this.getComPortPoolPreview().down('form').removeAll();
            this.getComPortPoolPreview().down('#comPortPoolName').update('<h3>' + me.comPortPool.get('name') + '</h3>');
            me.getComPortPoolPreview().down('#comPortPoolActive').update({active: null});
            this.getComPortPoolPreview().down('#previewpanel').collapse();
        }
    },

    startStopComserver: function () {
        var me = this;
        this.comPortPool.set('active', !this.comPortPool.get('active'));
        this.saveComPortPool();
    },

    saveComPortPool: function (viewToClose) {
        var me = this;
        this.comPortPool.save({
            success: function (record) {
                me.getComPortPoolsStore().reload(
                    {
                        callback: function () {
                            me.showComComPortPoolPreview();
                            viewToClose && viewToClose.close();
                        }
                    });
            },
            failure: function (record, operation) {
                var json = Ext.decode(operation.response.responseText);
                if (json && json.errors) {
                    viewToClose.down('form').getForm().markInvalid(json.errors);
                }
            }
        });
    }

})
;

