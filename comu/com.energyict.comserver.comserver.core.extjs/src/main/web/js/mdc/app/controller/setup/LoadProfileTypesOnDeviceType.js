Ext.define('Mdc.controller.setup.LoadProfileTypesOnDeviceType', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.loadprofiletype.LoadProfileTypeOnDeviceTypeSetup',
        'setup.loadprofiletype.LoadProfileTypeSideFilter',
        'setup.loadprofiletype.LoadProfileTypeSorting',
        'setup.loadprofiletype.LoadProfileTypeFiltering',
        'setup.loadprofiletype.LoadProfileTypeGrid',
        'setup.loadprofiletype.LoadProfileTypePreview',
        'setup.loadprofiletype.LoadProfileTypesAddToDeviceTypeSetup',
        'setup.loadprofiletype.LoadProfileTypesAddToDeviceTypeDockedItems',
        'setup.loadprofiletype.LoadProfileTypesAddToDeviceTypeGrid'
    ],

    refs: [
        {ref: 'loadTypeGrid', selector: 'loadProfileTypeSetup loadProfileTypeGrid'},
        {ref: 'loadTypePreview', selector: 'loadProfileTypeOnDeviceTypeSetup #loadProfileTypePreview'},
        {ref: 'loadTypeCountContainer', selector: 'loadProfileTypeOnDeviceTypeSetup #loadProfileTypesCountContainer'},
        {ref: 'loadTypeEmptyListContainer', selector: 'loadProfileTypeOnDeviceTypeSetup #loadProfileTypeEmptyListContainer'},
        {ref: 'addLoadProfileTypesGrid', selector: '#loadProfileTypesAddToDeviceTypeGrid'},
        {ref: 'uncheckLoadProfileButton', selector: '#uncheckAllLoadProfileTypes'},
        {ref: 'loadTypesToDeviceTypeCountContainer', selector: 'loadProfileTypesAddToDeviceTypeDockedItems #addLoadProfileTypesToDeviceTypeCountContainer'},
        {ref: 'addLoadProfileTypesSetup', selector: '#loadProfileTypesAddToDeviceTypeSetup' }
    ],

    stores: [
        'Mdc.store.LoadProfileTypesOnDeviceType',
        'Mdc.store.Intervals',
        'Mdc.store.LoadProfileTypes'
    ],

    init: function () {
        this.control({
            'loadProfileTypeOnDeviceTypeSetup loadProfileTypeGrid': {
                itemclick: this.loadGridItemDetail
            },
            'loadProfileTypesAddToDeviceTypeSetup radiogroup[name=allOrSelectedLoadProfileTypes]': {
                change: this.selectRadioButton
            },
            '#loadProfileTypesAddToDeviceTypeGrid': {
                selectionchange: this.getCountOfCheckedLoadProfileTypes
            },
            'loadProfileTypesAddToDeviceTypeSetup loadProfileTypesAddToDeviceTypeDockedItems button[action=uncheckallloadprofiletypes]': {
                click: this.unCheckAllLoadProfileTypes
            },
            'loadProfileTypesAddToDeviceTypeSetup button[name=addloadprofiletypestodevicetype]': {
                click: this.addLoadProfileTypesToDeviceType
            },
            'button[action=loadprofiletypeondevicetypenotificationerrorretry]': {
                click: this.retrySubmit
            }
        });

        this.intervalStore = this.getStore('Intervals');
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
                    me.handleSuccessRequest(Uni.I18n.translate('loadProfileTypes.removeSuccessMsg', 'MDC', 'Load profile type was removed successfully'));
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

    unCheckAllLoadProfileTypes: function () {
        var grid = this.getAddLoadProfileTypesGrid();
        grid.getView().getSelectionModel().deselectAll();
    },


    retrySubmit: function (btn) {
        btn.up('messagebox').hide();
        this.addLoadProfileTypesToDeviceType();
    },

    addLoadProfileTypesToDeviceType: function () {
        var me = this,
            grid = this.getAddLoadProfileTypesGrid(),
            selectedArray = grid.getView().getSelectionModel().getSelection(),
            idsArray = [],
            jsonData;
        preloader = Ext.create('Ext.LoadMask', {
            msg: "Adding load profile types to device type ",
            target: grid.up('#loadProfileTypesAddToDeviceTypeSetup')
        });
        Ext.each(selectedArray, function (loadprofileType) {
            idsArray.push(loadprofileType.getData().id);
        });
        jsonData = Ext.JSON.encode(idsArray);
        Ext.Ajax.request({
            url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/loadprofiletypes',
            method: 'POST',
            jsonData: jsonData,
            success: function () {
                me.handleSuccessRequest('Load profile types were successfully added to device type');
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
            errormsgs += error.msg + '<br>'
        });
        if (errormsgs == '') {
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
            })
    },


    getCountOfCheckedLoadProfileTypes: function () {
        var grid = this.getAddLoadProfileTypesGrid(),
            loadProfileTypesCountSelected = grid.getView().getSelectionModel().getSelection().length,
            loadProfileTypesCountContainer = this.getLoadTypesToDeviceTypeCountContainer(),
            loadProfileTypesWord;
        if (loadProfileTypesCountSelected == 1) {
            loadProfileTypesWord = ' load profile type selected'
        } else {
            loadProfileTypesWord = ' load profile types selected'
        }
        var widget = Ext.widget('container', {
            html: loadProfileTypesCountSelected + loadProfileTypesWord
        });

        loadProfileTypesCountContainer.removeAll(true);
        loadProfileTypesCountContainer.add(widget);
    },

    checkAndMarkLoadProfileTypes: function () {
        var grid = this.getAddLoadProfileTypesGrid(),
            view = this.getAddLoadProfileTypesSetup(),
            uncheckMeasurement = this.getUncheckLoadProfileButton();
        if (!Ext.isEmpty(grid) && !Ext.isEmpty(view)) {
            var selectionModel = grid.getView().getSelectionModel();
            selectionModel.selectAll();
            grid.disable();
            uncheckMeasurement.disable();
        }
    },

    selectRadioButton: function (radiogroup) {
        var radioValue = radiogroup.getValue().loadProfileTypeRange;
        switch (radioValue) {
            case 'ALL':
                this.checkAllLoadProfileTypes();
                break;
            case 'SELECTED':
                this.checkSelectedLoadProfileTypes();
                break;
        }
    },

    checkAllLoadProfileTypes: function () {
        var grid = this.getAddLoadProfileTypesGrid(),
            uncheckMeasurement = this.getUncheckLoadProfileButton(),
            selectionModel = grid.getView().getSelectionModel();
        selectionModel.selectAll();
        grid.disable();
        uncheckMeasurement.disable();
    },


    checkSelectedLoadProfileTypes: function () {
        var grid = this.getAddLoadProfileTypesGrid(),
            setup = this.getAddLoadProfileTypesSetup(),
            uncheckMeasurement = this.getUncheckLoadProfileButton();
        if (!Ext.isEmpty(grid) && !Ext.isEmpty(setup)) {
            grid.enable();
            uncheckMeasurement.enable();
        }
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
                msg: "Loading...",
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
        var me = this,
            widget = Ext.widget('loadProfileTypesAddToDeviceTypeSetup', { intervalStore: this.intervalStore });
        me.deviceTypeId = deviceTypeId;
        me.store.getProxy().extraParams = ({deviceType: deviceTypeId});
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                me.deviceTypeName = deviceType.get('name');
                me.getApplication().fireEvent('changecontentevent', widget);
                me.store.load({ params: { available: true }, callback: function () {
                    var radiogroup = Ext.ComponentQuery.query('loadProfileTypesAddToDeviceTypeSetup radiogroup[name=allOrSelectedLoadProfileTypes]')[0],
                        grid = me.getAddLoadProfileTypesGrid(),
                        // Not a good solution ( need to be replaced with opening web socket connection between server and web application )
                        autoRefresherTask = {
                            run: function () {
                                var addGrid = Ext.ComponentQuery.query('#loadProfileTypesAddToDeviceTypeGrid')[0];
                                Ext.Ajax.request({
                                    url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/loadprofiletypes/',
                                    method: 'GET',
                                    params: { available: true },
                                    success: function (response) {
                                        if (!me.arrayComparator(Ext.Array.pluck(me.store.data.items, 'data'), Ext.decode(response.responseText, true).data)) {
                                            if (addGrid) {
                                                var radioValue = radiogroup.getValue().loadProfileTypeRange;
                                                me.store.load({ params: { available: true }, callback: function() {
                                                    switch (radioValue) {
                                                        case 'ALL':
                                                            grid.getView().getSelectionModel().selectAll();
                                                            break;
                                                    }
                                                }});
                                            } else {
                                                Ext.TaskManager.stop(autoRefresherTask);
                                            }
                                        };
                                    }
                                });
                            },
                            interval: 5000
                        }
                    Ext.TaskManager.start(autoRefresherTask);
                    // end
                    radiogroup.fireEvent('change', radiogroup);
                    grid.fireEvent('selectionchange', grid);
                }});
            }
        });
    }
});

