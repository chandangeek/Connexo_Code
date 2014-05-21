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
            header = {
                style: 'msgHeaderStyle'
            },
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
                header.text = '';

                Ext.create('widget.uxNotification', {
                    html: 'Successfully added',
                    ui: 'notification-success'
                }).show();
            },
            failure: function (response) {
                var result = Ext.decode(response.responseText);

                if (result !== null) {
                    Ext.Msg.show({
                        title: result.error,
                        msg: result.message,
                        icon: Ext.MessageBox.WARNING,
                        buttons: Ext.MessageBox.CANCEL,
                        ui: 'notification-error',
                        config: {
                            me: this
                        }
                    });
                }
                else {
                    Ext.Msg.show({
                        title: 'Error during adding',
                        msg: 'The logbook type could not be added because of an error in the database.',
                        icon: Ext.MessageBox.WARNING,
                        buttons: Ext.MessageBox.CANCEL,
                        ui: 'notification-error',
                        config: {
                            me: this
                        }
                    });
                }
            },
            callback: function () {
                preloader.destroy();
            }
        });
    }
});



