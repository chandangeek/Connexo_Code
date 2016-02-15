Ext.define('Sct.controller.ServiceCallTypes', {
    extend: 'Ext.app.Controller',

    views: [
        'Sct.view.Setup',
        'Sct.view.LogLevelWindow'
    ],
    stores: [
        'Sct.store.ServiceCallTypes',
        'Sct.store.LogLevels'
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
        var me = this,
            store = Ext.getStore('Sct.store.LogLevels'),
            view = Ext.widget('log-level-window', {
                record: record,
                store: store
            });

        me.getPage().setLoading();
        store.load(function(records, operation, success) {
            if(success) {
                view.show();
            }

            me.getPage().setLoading(false);
        });
    },

    updateLogLevel: function() {
        var me = this,
            window = me.getChangeLogLevelWindow(),
            record = window.record,
            combobox = window.down('#log-level-field'),
            loglevel;
        loglevel = combobox.findRecordByDisplay(combobox.getRawValue());
        record.set('logLevel', loglevel.data);
        record.save( {
            success: function (record) {
                me.getPage().down('#grd-service-call-types').getStore().load();
                window.close();
            },
            failure: function (record, operation) {
                //TODO
            }
        });
    },

    closeLogLevelWindow: function() {
        var me = this;
        me.getPage().setLoading(false);
    }
});