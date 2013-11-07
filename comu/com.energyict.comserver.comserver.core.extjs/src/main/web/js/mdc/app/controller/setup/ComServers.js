Ext.define('Mdc.controller.setup.ComServers', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.ComServers',
        'setup.ComServerEdit'
    ],

    stores: [
        'ComServers'
    ],

    init: function() {
        this.control({
            'setupComServers': {
                itemdblclick: this.editComServer
            },
            'comServerEdit button[action=save]':{
               click: this.update
            },
            'comServerEdit button[action=cancel]':{
                click: this.cancel
            }
        });
    },

    editComServer: function(grid,record){
        var url = Mdc.getApplication().getHistorySetupController().tokenizeBrowse('comservers',record.getId());
        Ext.History.add(url);
    },

    showEditView: function(id){
        var view = Ext.widget('comServerEdit');
        Ext.ModelManager.getModel('Mdc.model.ComServer').load(id, {
            success: function(comserver) {
                view.down('form').loadRecord(comserver);
                Mdc.getApplication().getMainController().showContent(view);
            }
        });
    },

    update: function(button){
        var pnl    = button.up('panel'),
            form   = pnl.down('form'),
            record = form.getRecord(),
            values = form.getValues();
        record.set(values);
        record.save();
    },

    cancel: function(){
        var url = Mdc.getApplication().getHistorySetupController().tokenizeBrowse('comservers');
        Ext.History.add(url);
    }
});
