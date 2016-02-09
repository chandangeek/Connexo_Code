Ext.define('Sct.controller.ServiceCallTypes', {
    extend: 'Ext.app.Controller',

    views: [
        'Sct.view.Setup',
        'Sct.view.LogLevelWindow'
    ],
    stores: [
        'Sct.store.ServiceCallTypes'
    ],
    models: [
    ],

    refs: [
        {
            ref: 'page',
            selector: 'servicecalltypes-setup'
        },
        {
            ref: 'changeLogLevelWindow',
            selector: 'log-level-window'
        }
    ],

    init: function () {
        this.control({
            'servicecalltypes-setup servicecalltypes-grid': {
                select: this.showPreview
            },
            'sct-action-menu': {
                click: this.chooseAction
            },
            'log-level-window #save-log-level-button': {
                click: this.updateLogLevel
            },
            'log-level-window': {
                close: this.closeLogLevelWindow
            }
        });
    },

    showServiceCallTypes: function(){
        var me = this,
            view = Ext.widget('servicecalltypes-setup');

        me.getApplication().fireEvent('changecontentevent', view);
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            page = me.getPage(),
            preview = page.down('servicecalltypes-preview'),
            serviceCallTypeName = record.get('type'),
            previewForm = page.down('servicecalltypes-preview-form');

        preview.setTitle(serviceCallTypeName);
        previewForm.updatePreview(record);
    },

    chooseAction: function (menu, item) {
        var me = this;

        switch (item.action) {
            case 'changeLogLevel':
                me.changeLogLevel(menu.record);
        }
    },

    changeLogLevel: function (record) {
        var me = this;

        me.getPage().setLoading();
        Ext.widget('log-level-window', {
                record: record
        }).show();
    },

    updateLogLevel: function() {
        //TODO: put update code for log level here
        var me = this,
            window = me.getChangeLogLevelWindow(),
            record = window.record;
        debugger;
    },

    closeLogLevelWindow: function() {
        var me = this;
        me.getPage().setLoading(false);
    }
});