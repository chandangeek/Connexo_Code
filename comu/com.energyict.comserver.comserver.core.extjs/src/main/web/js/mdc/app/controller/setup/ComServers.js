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
    }
});
