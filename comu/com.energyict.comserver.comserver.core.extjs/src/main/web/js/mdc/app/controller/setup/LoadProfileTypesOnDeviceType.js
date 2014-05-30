Ext.define('Mdc.controller.setup.LoadProfileTypesOnDeviceType', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.loadprofiletype.LoadProfileTypeOnDeviceTypeSetup',
        'setup.loadprofiletype.LoadProfileTypeSideFilter',
        'setup.loadprofiletype.LoadProfileTypeSorting',
        'setup.loadprofiletype.LoadProfileTypeFiltering',
        'setup.loadprofiletype.LoadProfileTypeGrid',
        'setup.loadprofiletype.LoadProfileTypePreview',
        'setup.loadprofiletype.LoadProfileTypeDockedItems',
        'setup.loadprofiletype.LoadProfileTypeEmptyList',
        'setup.loadprofiletype.LoadProfileTypeFloatingPanel',
        'setup.loadprofiletype.LoadProfileTypesAddToDeviceTypeSetup',
        'setup.loadprofiletype.LoadProfileTypesAddToDeviceTypeDockedItems',
        'setup.loadprofiletype.LoadProfileTypesAddToDeviceTypeGrid'
    ],

    refs: [
        {ref: 'loadTypeGrid', selector: 'loadProfileTypeOnDeviceTypeSetup #loadProfileTypeGrid'},
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
        this.listen({
            store: {
                '#LoadProfileTypesOnDeviceType': {
                    load: this.checkLoadProfileTypesOnDeviceTypesCount
                }
            }
        });

        this.intervalStore = this.getStore('Intervals');
        this.store = this.getStore('LoadProfileTypesOnDeviceType');
    },

    unCheckAllLoadProfileTypes: function () {
        var grid = this.getAddLoadProfileTypesGrid();
        grid.getView().getSelectionModel().deselectAll();
    },


    retrySubmit: function (btn) {
        btn.up('messagebox').hide();
        this.addLoadProfileTypesToDeviceType();
    },

    addLoadProfileTypesToDeviceType: function() {
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
            failure: function (response) {
                me.handleFailureRequest(response, 'Error during adding load profile types to device type');
            },
            callback: function () {
                preloader.destroy();
            }
        });
    },



    handleSuccessRequest: function (headerText) {
        window.location.href = '#/administration/devicetypes/' +  this.deviceTypeId + '/loadprofiles';
        Ext.create('widget.uxNotification', {
            html: headerText,
            ui: 'notification-success'
        }).show();
    },

    handleFailureRequest: function (response, headerText) {
        var result = Ext.JSON.decode(response.responseText),
            errormsgs = '';
        Ext.each(result.errors, function(error) {
            errormsgs += error.msg + '<br>'
        });
        if (errormsgs == '') {
            errormsgs = result.message;
        }
        Ext.widget('messagebox', {
            buttons: [
                {
                    text: 'Retry',
                    action: 'loadprofiletypeondevicetypenotificationerrorretry',
                    ui: 'delete'
                },
                {
                    text: 'Cancel',
                    action: 'cancel',
                    ui: 'link',
                    handler:function(button,event){
                        this.up('messagebox').hide();
                        Ext.History.back();
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

    checkLoadProfileTypesOnDeviceTypesCount: function () {
        var grid = this.getLoadTypeGrid();
        if (!Ext.isEmpty(grid)) {
            var numberOfLoadTypesContainer = this.getLoadTypeCountContainer(),
                emptyMessage = this.getLoadTypeEmptyListContainer(),
                gridView = grid.getView(),
                selectionModel = gridView.getSelectionModel(),
                loadTypeCount = this.store.getCount(),
                loadTypesWord;

            if (loadTypeCount == 1) {
                loadTypesWord = ' load profile type'
            } else {
                loadTypesWord = ' load profile types'
            }
            var widget = Ext.widget('container', {
                html: loadTypeCount + loadTypesWord
            });

            numberOfLoadTypesContainer.removeAll(true);
            numberOfLoadTypesContainer.add(widget);

            if (loadTypeCount < 1) {
                grid.hide();
                emptyMessage.add(
                    {
                        xtype: 'loadProfileTypeEmptyList',
                        actionHref: '#/administration/devicetypes/' + this.deviceTypeId + '/loadprofiles/add'
                    }
                );
                this.getLoadTypePreview().hide();
            } else {
                selectionModel.select(0);
                grid.fireEvent('itemclick', gridView, selectionModel.getLastSelected());
            }
        }
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


    showDeviceTypeLoadProfileTypesView: function (deviceTypeId) {
        var me = this,
            widget = Ext.widget('loadProfileTypeOnDeviceTypeSetup', {deviceTypeId: deviceTypeId, intervalStore: this.intervalStore });
        widget.down('#loadProfileTypesTitle').html = '<h1>' + Uni.I18n.translate('loadprofiletype.loadprofiletypes', 'MDC', 'Load profile types') + '</h1>';
        me.deviceTypeId = deviceTypeId;
        me.store.getProxy().extraParams = ({deviceType: deviceTypeId});
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.deviceTypeName = deviceType.get('name');
                me.getApplication().fireEvent('changecontentevent', widget);
                me.store.load({ params: {sort: 'name' }});
            }
        });
    },

    showDeviceTypeLoadProfileTypesAddView: function (deviceTypeId) {
        var me = this,
            widget = Ext.widget('loadProfileTypesAddToDeviceTypeSetup', { intervalStore: this.intervalStore }),
            loadProfileTypesAlreadyAdded = [];
        me.deviceTypeId = deviceTypeId;
        me.store.getProxy().extraParams = ({deviceType: deviceTypeId});
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.deviceTypeName = deviceType.get('name');
                me.getApplication().fireEvent('changecontentevent', widget);
                me.store.load({ params: { available: true }});
            }
        });
    }
});

