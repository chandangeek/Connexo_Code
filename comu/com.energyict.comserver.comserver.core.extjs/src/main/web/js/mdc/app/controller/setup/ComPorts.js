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
            },
            'comPorts button[action=add]': {
                click: this.add
            },
            'comPorts button[action=delete]': {
                click: this.delete
            },
            'comPortEdit button[action=save]': {
                click: this.update
            },
            'comPortEdit button[action=cancel]': {
                click: this.cancel
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

    },

    update: function(button){
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

    cancel: function(){
        console.log('cancel');
        Ext.History.back();
    },

    add: function(){
        console.log('add');
    },

    delete: function(){
        console.log('delete');
    }
});
