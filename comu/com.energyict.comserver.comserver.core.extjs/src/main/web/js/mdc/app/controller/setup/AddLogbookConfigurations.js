Ext.define('Mdc.controller.setup.AddLogbookConfigurations', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mdc.store.LogbookConfigurations'
    ],

    views: [
        'setup.deviceconfiguration.AddLogbookConfigurations'
    ],

    init: function () {
        this.control({
            'add-logbook-configurations grid': {
                selectionchange: this.countSelectedLogbooks
            },
            'add-logbook-configurations button[action=add]': {
                click: this.addLogbookType
            }
        });
    },

    countSelectedLogbooks: function (grid) {
        var textLabel = Ext.ComponentQuery.query('add-logbook-configurations #LogBookCount')[0];
        textLabel.setText(
            grid.view.getSelectionModel().getSelection().length >= 1 ? (grid.view.getSelectionModel().getSelection().length +
                (grid.view.getSelectionModel().getSelection().length > 1 ? ' logbooks' : ' logbook') + ' selected') : 'No logbooks selected');
    },

    addLogbookType: function (btn) {
        var self = this,
            addView = Ext.ComponentQuery.query('add-logbook-configurations')[0],
            grid = addView.down('grid'),
            url = '/api/dtc/devicetypes/' + addView.deviceTypeId + '/deviceconfigurations/' + addView.deviceConfigurationId + '/logbookconfigurations',
            preloader = Ext.create('Ext.LoadMask', {
                msg: "Loading...",
                target: addView
            }),
            records = grid.getSelectionModel().getSelection(),
            ids = [];

        Ext.Array.each(records, function (item) {
            ids.push(item.internalId);
        });
        var jsonIds = Ext.encode(ids);
        var router = this.getController('Uni.controller.history.Router');

        preloader.show();
        Ext.Ajax.request({
            url: url,
            method: 'POST',
            jsonData: jsonIds,
            success: function () {
                router.getRoute('administration/devicetypes/view/deviceconfigurations/view/logbookconfigurations').forward();
                self.getApplication().fireEvent('acknowledge', 'Successfully added');
            },
            failure: function (response) {
                if(response.status == 400) {
                    var result = Ext.decode(response.responseText, true),
                        errorTitle = 'Failed to add',
                        errorText = 'Logbook configuration could not be added. There was a problem accessing the database';

                    if (result !== null) {
                        errorTitle = result.error;
                        errorText = result.message;
                    }

                    self.getApplication().getController('Uni.controller.Error').showError(errorTitle, errorText);
                }
            },
            callback: function () {
                preloader.destroy();
            }
        });
    }
});




