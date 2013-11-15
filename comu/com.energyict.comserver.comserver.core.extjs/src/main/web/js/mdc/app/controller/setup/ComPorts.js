Ext.define('Mdc.controller.setup.ComPorts', {
    extend: 'Ext.app.Controller',

    requires: [
        'Mdc.model.ComPort'
    ],

    views: [
        'setup.comport.ComPorts',
        'setup.comport.ComPortEdit'
    ],

    init: function () {
        this.control({
            'comPorts': {
                itemdblclick: this.editComPort
            }
        });
    },

    editComPort: function (grid, record) {
        var url = Mdc.getApplication().getHistorySetupController().tokenizeBrowse('comports', record.getId());
        Ext.History.add(url);
    },

    showEditView: function (id) {
        var view = Ext.widget('comPortEdit');
        if(id){
            Ext.ModelManager.getModel('Mdc.model.ComPort').load(id, {
                success: function (comport) {
                    view.down('form').loadRecord(comport);
                    Mdc.getApplication().getMainController().showContent(view);
                }
            });
        }
//        else {
//            var view = Ext.widget('comServerEdit');
//            Mdc.getApplication().getMainController().showContent(view);
//        }

    }
});
