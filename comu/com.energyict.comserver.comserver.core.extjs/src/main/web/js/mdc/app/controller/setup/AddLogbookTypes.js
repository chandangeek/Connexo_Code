Ext.define('Mdc.controller.setup.AddLogbookTypes', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mdc.store.LogbookTypes'
    ],
    requires: [
        'Ext.window.MessageBox'
    ],
    views: [
        'setup.devicetype.AddLogbookTypes'
    ],

    init: function () {

        this.control({
            'add-logbook-types grid': {
                selectionchange: this.countSelectedLogbooks
            },
            'add-logbook-types button[action=add]': {
                click: this.addLogbookType
            }
        });
    },

    countSelectedLogbooks: function (grid) {
        var textLabel = Ext.ComponentQuery.query('add-logbook-types label')[0];
        textLabel.setText(
            grid.view.getSelectionModel().getSelection().length >= 1 ? (grid.view.getSelectionModel().getSelection().length +
                (grid.view.getSelectionModel().getSelection().length > 1 ? ' logbooks' : ' logbook') + ' selected') : 'No logbooks selected');
    },

    addLogbookType: function (btn) {
        var self = this,
            addView = Ext.ComponentQuery.query('add-logbook-types')[0],
            grid = addView.down('grid'),
            url = '/api/dtc/devicetypes/' + addView.deviceTypeId + '/logbooktypes',
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
        preloader.show();
        Ext.Ajax.request({
            url: url,
            method: 'POST',
            jsonData: jsonIds,
            success: function () {
                window.location.href = '#/administration/devicetypes/' + addView.deviceTypeId + '/logbooktypes';
                Ext.create('widget.uxNotification', {
                    html: 'Successfully added',
                    ui: 'notification-success'
                }).show();
            },
            failure: function (response) {
                if(response.status == 400) {
                    var result = Ext.decode(response.responseText, true),
                        errorTitle = 'Failed to add',
                        errorText = 'Logbook types could not be added. There was a problem accessing the database';

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



