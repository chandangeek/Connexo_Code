Ext.define('Mdc.controller.setup.LoadProfileTypesOnDeviceType', {
    extend: 'Ext.app.Controller',

    required: [
      'Mdc.store.Intervals'
    ],

    views: [
        'setup.loadprofiletype.LoadProfileTypeOnDeviceTypeSetup',
        'setup.loadprofiletype.LoadProfileTypeSideFilter',
        'setup.loadprofiletype.LoadProfileTypeSorting',
        'setup.loadprofiletype.LoadProfileTypeFiltering',
        'setup.loadprofiletype.LoadProfileTypeGrid',
        'setup.loadprofiletype.LoadProfileTypePreview',
        'setup.loadprofiletype.LoadProfileTypesAddToDeviceTypeSetup',
        'setup.loadprofiletype.LoadProfileTypesAddToDeviceTypeGrid'
    ],

    refs: [
        {ref: 'loadTypeGrid', selector: 'loadProfileTypeSetup loadProfileTypeGrid'},
        {ref: 'loadTypePreview', selector: 'loadProfileTypeOnDeviceTypeSetup #loadProfileTypePreview'},
        {ref: 'loadTypeCountContainer', selector: 'loadProfileTypeOnDeviceTypeSetup #loadProfileTypesCountContainer'},
        {ref: 'loadTypeEmptyListContainer', selector: 'loadProfileTypeOnDeviceTypeSetup #loadProfileTypeEmptyListContainer'},
        {ref: 'addLoadProfileTypesGrid', selector: '#loadprofile-type-add-grid'},
        {ref: 'uncheckLoadProfileButton', selector: '#uncheckAllLoadProfileTypes'},
        {ref: 'addLoadProfileTypesSetup', selector: '#loadProfileTypesAddToDeviceTypeSetup' },
        {ref: 'addLoadProfileTypePanel', selector: '#addLoadProfileTypePanel'}
    ],

    requires: [
        'Mdc.store.LoadProfileTypesOnDeviceTypeAvailable'
    ],

    stores: [
        'Mdc.store.LoadProfileTypesOnDeviceType',
        'LoadProfileTypesOnDeviceTypeAvailable',
        'Intervals',
        'Mdc.store.LoadProfileTypes'
    ],

    models: [
        'Mdc.model.DeviceType'
    ],

    init: function () {
        this.control({
            'loadProfileTypeOnDeviceTypeSetup loadProfileTypeGrid': {
                itemclick: this.loadGridItemDetail
            },
            'button[action=loadprofiletypeondevicetypenotificationerrorretry]': {
                click: this.retrySubmit
            },
            'loadProfileTypesAddToDeviceTypeSetup grid': {
                selectionchange: this.hideLoadProfileTypesErrorPanel
            },
            '#addButton[action=addLoadProfileTypeAction]': {
                click: this.addLoadProfileTypesToDeviceType
            }
        });

        this.intervalStore = this.getStore('Intervals').load();
        this.store = this.getStore('LoadProfileTypesOnDeviceType');
    },

    showConfirmationPanel: function () {
        var me = this,
            grid = me.getLoadTypeGrid(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('loadProfileTypes.confirmWindow.removeMsgOnDeviceType', 'MDC', 'This load profile type will no longer be available on this device type.'),
            title: Uni.I18n.translate('general.remove', 'MDC', 'Remove') + " '" + lastSelected.get('name') + "'?",
            config: {
                me: me
            },
            fn: me.confirmationPanelHandler
        });
    },

    confirmationPanelHandler: function (state, text, conf) {
        var me = conf.config.me,
            grid = me.getLoadTypeGrid(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();

        if (state === 'confirm') {
            this.close();
            Ext.Ajax.request({
                url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/loadprofiletypes/' + lastSelected.getData().id,
                method: 'DELETE',
                waitMsg: 'Removing...',
                success: function () {
                    me.handleSuccessRequest(Uni.I18n.translate('loadProfileTypes.removeSuccessMsg', 'MDC', 'Load profile type removed'));
                    me.store.loadPage(1);
                },
                failure: function (response) {
                    var errorText,
                        errorTitle;

                    if (response.status == 400) {
                        errorText = Ext.decode(response.responseText, true).message;
                        errorTitle = Uni.I18n.translate('loadProfileTypes.removeErrorMsg', 'MDC', 'Error during removing of load profile');

                        me.getApplication().getController('Uni.controller.Error').showError(errorTitle, errorText);
                    }
                }
            });
        } else if (state === 'cancel') {
            this.close();
        }
    },

    retrySubmit: function (btn) {
        btn.up('messagebox').hide();
        this.addLoadProfileTypesToDeviceType();
    },

    onAllLoadProfileTypesAdd: function () {
        var me = this,
            store = me.getAddLoadProfileTypesGrid().store;

        me.addLoadProfileTypesToDeviceType(store.data.items);
    },

    onSelectedLoadProfileTypesAdd: function (selection) {
        this.addLoadProfileTypesToDeviceType(selection);
    },

    addLoadProfileTypesToDeviceType: function (selection) {
        var me = this,
            grid = this.getAddLoadProfileTypesGrid(),
            idsArray = [],
            jsonData;
        var selection = grid.getSelectionModel().getSelection();
        if (Ext.isEmpty(selection)) {
            me.showLoadProfileTypesErrorPanel();
            return;
        }

        preloader = Ext.create('Ext.LoadMask', {
            msg: "Adding load profile types to device type ",
            target: grid.up('#loadProfileTypesAddToDeviceTypeSetup')
        });

        Ext.each(selection, function (loadprofileType) {
            idsArray.push(loadprofileType.getData().id);
        });

        jsonData = Ext.JSON.encode(idsArray);

        Ext.Ajax.request({
            url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/loadprofiletypes',
            method: 'POST',
            jsonData: jsonData,
            success: function () {
                me.handleSuccessRequest('Load profile types added');
            },
//            failure: function (response) {
//                me.handleFailureRequest(response, 'Error during adding load profile types to device type', 'loadprofiletypeondevicetypenotificationerrorretry');
//            },
            callback: function () {
                preloader.destroy();
            }
        });
    },

    handleSuccessRequest: function (headerText) {
        window.location.href = '#/administration/devicetypes/' + this.deviceTypeId + '/loadprofiles';
        this.getApplication().fireEvent('acknowledge', headerText);
    },

    handleFailureRequest: function (response, headerText, retryAction) {
        var result = Ext.JSON.decode(response.responseText),
            errormsgs = '',
            me = this;

        Ext.each(result.errors, function (error) {
            errormsgs += error.msg + '<br>';
        });

        if (errormsgs === '') {
            errormsgs = result.message;
        }

        Ext.widget('messagebox', {
            buttons: [
                {
                    text: 'Retry',
                    action: retryAction,
                    ui: 'remove'
                },
                {
                    text: 'Cancel',
                    action: 'cancel',
                    ui: 'link',
                    href: '#/administration/devicetypes/' + me.deviceTypeId + '/loadprofiles',
                    handler: function (button, event) {
                        this.up('messagebox').hide();
                    }
                }
            ]
        }).show({
            ui: 'notification-error',
            title: headerText,
            msg: errormsgs,
            icon: Ext.MessageBox.ERROR
        });
    },

    loadGridItemDetail: function (grid, record) {
        var form = Ext.ComponentQuery.query('loadProfileTypeOnDeviceTypeSetup loadProfileTypePreview form')[0],
            recordData = record.getData(),
            preloader = Ext.create('Ext.LoadMask', {
                msg: "Loading...",
                target: form
            });

        if (this.displayedItemId != recordData.id) {
            grid.clearHighlight();
            preloader.show();
        }

        this.displayedItemId = recordData.id;
        form.loadRecord(record);
        preloader.destroy();
    },

    showDeviceTypeLoadProfileTypesView: function (deviceTypeId) {
        var self = this,
            loadProfileTypesStore = self.getStore('Mdc.store.LoadProfileTypes'),
            viewport = Ext.ComponentQuery.query('viewport')[0],
            preloader = Ext.create('Ext.LoadMask', {
                target: viewport
            }),
            widget;

        var showPage = function () {
            Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
                success: function (deviceType) {
                    widget = Ext.widget('loadProfileTypeSetup', {
                        config: {
                            deviceTypeId: deviceTypeId,
                            gridStore: self.store
                        }
                    });
                    preloader.show();
                    self.store.load({callback: function () {
                        preloader.destroy();
                    }});

                    self.getApplication().fireEvent('loadDeviceType', deviceType);
                    self.deviceTypeName = deviceType.get('name');
                    self.getApplication().fireEvent('changecontentevent', widget);
                    Ext.Array.each(Ext.ComponentQuery.query('[action=editloadprofiletype]'), function (item) {
                        item.hide();
                    });
                    Ext.Array.each(Ext.ComponentQuery.query('[action=deleteloadprofiletype]'), function (item) {
                        item.clearListeners();
                        item.on('click', function () {
                            self.showConfirmationPanel();
                        });
                    });
                }
            });
        };

        self.deviceTypeId = deviceTypeId;
        self.store.getProxy().extraParams = ({deviceType: deviceTypeId});

        if (loadProfileTypesStore.getCount()) {
            showPage();
        } else {
            loadProfileTypesStore.load(showPage);
        }

    },

    arrayComparator: function (array1, array2) {
        if (!array1 || !array2)
            return false;

        if (array1.length != array2.length)
            return false;

        for (var i = 0, l = array1.length; i < l; i++) {
            if (array1[i].id != array2[i].id) {
                return false;
            }
        }
        return true;
    },

    showDeviceTypeLoadProfileTypesAddView: function (deviceTypeId) {
        var me = this;
        //  availableLoadProfilesStore = me.getStore('Mdc.store.LoadProfileTypesOnDeviceTypeAvailable');

        me.deviceTypeId = deviceTypeId;
        me.store.getProxy().setExtraParam('deviceType', deviceTypeId);
        this.getLoadProfileTypesOnDeviceTypeAvailableStore().getProxy().setExtraParam('deviceType', deviceTypeId);
        this.getLoadProfileTypesOnDeviceTypeAvailableStore().load(
            {
                callback: function () {
                    me.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
                        success: function (deviceType) {
                            me.getApplication().fireEvent('loadDeviceType', deviceType);
                            me.deviceTypeName = deviceType.get('name');
                            me.intervalStore.load({
                                    callback: function () {
                                        var widget = Ext.widget('loadProfileTypesAddToDeviceTypeSetup', {
                                            intervalStore: me.intervalStore,
                                            deviceTypeId: deviceTypeId
                                        });
                                        me.getApplication().fireEvent('loadDeviceType', deviceType);
                                        me.getApplication().fireEvent('changecontentevent', widget);
                                        me.getLoadProfileTypesOnDeviceTypeAvailableStore().fireEvent('load', me.getLoadProfileTypesOnDeviceTypeAvailableStore());
                                    }
                                }
                            )
                        }
                    });
                }
            });
    },
    showLoadProfileTypesErrorPanel: function () {
        var me = this,
            formErrorsPanel = me.getAddLoadProfileTypePanel().down('#add-loadprofile-type-errors'),
            errorPanel = me.getAddLoadProfileTypePanel().down('#add-loadprofile-type-selection-error');

        formErrorsPanel.show();
        errorPanel.show();
    },

    hideLoadProfileTypesErrorPanel: function () {
        var me = this,
            formErrorsPanel = me.getAddLoadProfileTypePanel().down('#add-loadprofile-type-errors'),
            errorPanel = me.getAddLoadProfileTypePanel().down('#add-loadprofile-type-selection-error');

        formErrorsPanel.hide();
        errorPanel.hide();

    }
});

