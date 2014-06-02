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
        var textLabel = Ext.ComponentQuery.query('add-logbook-configurations label')[0];
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
        preloader.show();
        Ext.Ajax.request({
            url: url,
            method: 'POST',
            jsonData: jsonIds,
            success: function () {
                preloader.destroy();
                window.location.href = '#/administration/devicetypes/' + addView.deviceTypeId + '/deviceconfigurations/' + addView.deviceConfigurationId + '/logbookconfigurations';

                Ext.create('widget.uxNotification', {
                    html: 'Successfully added',
                    ui: 'notification-success'
                }).show();
            },
            failure: function (response) {
                preloader.destroy();
                var result;
                if (response != null) {
                    result = Ext.decode(response.responseText, true);
                }
                if (result !== null) {
                    Ext.widget('messagebox', {
                        buttons: [
                            {
                                text: 'Close',
                                action: 'cancel',
                                handler: function(btn){
                                    btn.up('messagebox').hide()
                                }
                            }
                        ]
                    }).show({
                        ui: 'notification-error',
                        title: result.error,
                        msg: result.message,
                        icon: Ext.MessageBox.ERROR
                    })

                } else {
                    Ext.widget('messagebox', {
                        buttons: [
                            {
                                text: 'Close',
                                action: 'cancel',
                                handler: function(btn){
                                    btn.up('messagebox').hide()
                                }
                            }
                        ]
                    }).show({
                        ui: 'notification-error',
                        title: 'Failed to add',
                        msg: 'Logbook configuration could not be added. There was a problem accessing the database',
                        icon: Ext.MessageBox.ERROR
                    })
                }
            },
            callback: function () {
                preloader.destroy();
            }
        });
    }
});




