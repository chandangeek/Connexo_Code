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

    showDatabaseError: function (msges) {
        var self = this,
            addView = Ext.ComponentQuery.query('add-logbook-configurations')[0];
        self.getApplication().fireEvent('isushowmsg', {
            type: 'error',
            msgBody: msges,
            y: 10,
            closeBtn: true,
            btns: [
                {
                    text: 'Cancel',
                    cls: 'isu-btn-link',
                    hnd: function () {
                        window.location = '#/administration/devicetypes/' + addView.deviceTypeId + '/deviceconfigurations/' + addView.deviceConfigurationId + '/logbookconfigurations';
                    }
                }
            ],
            listeners: {
                close: {
                    fn: function () {
                        addView.enable();
                    }
                }
            }
        });
        addView.disable();
    },

    addLogbookType: function (btn) {
        var self = this,
            addView = Ext.ComponentQuery.query('add-logbook-configurations')[0],
            grid = addView.down('grid'),
            url = '/api/dtc/devicetypes/' + addView.deviceTypeId + '/deviceconfigurations/' + addView.deviceConfigurationId + '/logbookconfigurations',
            header = {
                style: 'msgHeaderStyle'
            },
            bodyItem = {},
            msges = [],
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
                window.location.href = '#/administration/devicetypes/' + addView.deviceTypeId + '/deviceconfigurations/' + addView.deviceConfigurationId + '/logbookconfigurations';
                header.text = 'Successfully added';
                self.getApplication().fireEvent('isushowmsg', {
                    type: 'notify',
                    msgBody: [header],
                    y: 10,
                    showTime: 5000
                });
            },
            failure: function (response) {
                var result = Ext.decode(response.responseText);
                if (result !== null) {
                    header.text = result.message;
                    msges.push(header);
                    bodyItem.style = 'msgItemStyle';
                    bodyItem.text = result.error;
                    msges.push(bodyItem);
                    self.getApplication().fireEvent('isushowmsg', {
                        type: 'error',
                        msgBody: msges,
                        y: 10,
                        closeBtn: true,
                        btns: [
                            {
                                text: 'Cancel',
                                cls: 'isu-btn-link',
                                hnd: function () {
                                    window.location = '#/administration/devicetypes/' + addView.deviceTypeId + '/deviceconfigurations/' + addView.deviceConfigurationId + '/logbookconfigurations/add';
                                }
                            }
                        ],
                        listeners: {
                            close: {
                                fn: function () {
                                    addView.enable();
                                }
                            }
                        }
                    });
                    addView.disable();
                }
                else {
                    header.text = 'Error during adding';
                    msges.push(header);
                    bodyItem.style = 'msgItemStyle';
                    bodyItem.text = 'The logbook configuration could not be added because of an error in the database.';
                    msges.push(bodyItem);
                    self.showDatabaseError(msges);
                }
            },
            callback: function () {
                preloader.destroy();
            }
        });
    }
});




