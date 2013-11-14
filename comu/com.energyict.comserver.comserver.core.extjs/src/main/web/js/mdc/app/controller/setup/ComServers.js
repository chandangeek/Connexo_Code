Ext.define('Mdc.controller.setup.ComServers', {
    extend: 'Ext.app.Controller',

    requires: [
        'Mdc.model.ComServer',
        'Mdc.model.ComPort'
    ],

    views: [
        'setup.comserver.ComServers',
        'setup.comserver.ComServerEdit'
    ],

    stores: [
        'ComServers'
    ],

    refs: [
        {
            ref: 'comServerGrid',
            selector: 'viewport #comservergrid'
        }
    ],

    init: function () {
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
            'setupComServers button[action=add]': {
                click: this.add
            },
            'setupComServers button[action=delete]': {
                click: this.delete
            }
        });
    },

    editComServer: function (grid, record) {
        var url = Mdc.getApplication().getHistorySetupController().tokenizeBrowse('comservers', record.getId());
        Ext.History.add(url);
    },

    showEditView: function (id) {
        var view = Ext.widget('comServerEdit');
        Ext.ModelManager.getModel('Mdc.model.ComServer').load(id, {
            success: function (comserver) {
                var comPorts = comserver.comPorts();
                comPorts.load({
                    callback: function(records,operation,success){
                        view.down('form').loadRecord(comserver);
                        view.down('#comportgrid').reconfigure(comPorts,null);
                        Mdc.getApplication().getMainController().showContent(view);
                        console.log(comPorts.data);
                    }
                });
            }
        });
    },

    update: function (button) {
        var me = this;
        var pnl = button.up('panel'),
            form = pnl.down('form'),
            record = form.getRecord() || Ext.create(Mdc.model.ComServer),
            values = form.getValues();
        record.set(values);
        record.save({
            success: function (record, operation) {
                me.showComServerOverview();
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

    add: function () {
        var url = Mdc.getApplication().getHistorySetupController().tokenizeAddComserver();
        Ext.History.add(url);
    },

    delete: function () {
        console.log(this.getComServerGrid());
        var recordArray = this.getComServerGrid().getSelectionModel().getSelection();
        if (recordArray.length > 0) {
            recordArray[0].destroy();
        }
    }
});
