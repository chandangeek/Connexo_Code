Ext.define('Mdc.controller.setup.AddLogbookTypes', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mdc.store.AvailableLogbookTypes'
    ],
    requires: [
        'Ext.window.MessageBox'
    ],
    views: [
        'setup.devicetype.AddLogbookTypes'
    ],

    refs: [
        {ref: 'addLogbookTypePanel', selector: '#addLogbookPanel'}
    ],

    init: function () {
        this.control({
            'add-logbook-types grid': {
                selectionchange: this.hideLogbookTypesErrorPanel
            },
            'add-logbook-types button[action=add]': {
                click: this.addLogbookType
            }
        });
    },

    addLogbookType: function (btn) {
        var self = this,
            addView = Ext.ComponentQuery.query('add-logbook-types')[0],
            grid = addView.down('grid'),
            url = '/api/dtc/devicetypes/' + addView.deviceTypeId + '/logbooktypes',
            preloader = Ext.create('Ext.LoadMask', {
                msg: Uni.I18n.translate('general.loading', 'MDC', 'Loading...'),
                target: addView
            }),
            records = grid.getSelectionModel().getSelection(),
            ids = [];
        if (records.length === 0) {
            self.showLogbookTypesErrorPanel();
        } else {
            Ext.Array.each(records, function (item) {
                ids.push(item.internalId);
            });
            var jsonIds = Ext.encode(ids);
            preloader.show();
            var router = this.getController('Uni.controller.history.Router');
            Ext.Ajax.request({
                url: url,
                method: 'POST',
                jsonData: jsonIds,
                success: function () {
                    router.getRoute('administration/devicetypes/view/logbooktypes').forward();
                    self.getApplication().fireEvent('acknowledge', 'Logbook type(s) added');
                },
                failure: function (response) {
                    if (response.status == 400) {
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
    },

    showLogbookTypesErrorPanel: function () {
        var me = this,
            formErrorsPanel = me.getAddLogbookTypePanel().down('#add-logbook-type-errors'),
            errorPanel = me.getAddLogbookTypePanel().down('#add-logbook-type-selection-error');

        formErrorsPanel.show();
        errorPanel.show();
    },

    hideLogbookTypesErrorPanel: function () {
        var me = this,
            formErrorsPanel = me.getAddLogbookTypePanel().down('#add-logbook-type-errors'),
            errorPanel = me.getAddLogbookTypePanel().down('#add-logbook-type-selection-error');

        formErrorsPanel.hide();
        errorPanel.hide();

    }

});



