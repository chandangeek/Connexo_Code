Ext.define('Mdc.controller.setup.LoadProfileConfigurations', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.loadprofileconfiguration.LoadProfileConfigurationSetup',
        'setup.loadprofileconfiguration.LoadProfileConfigurationSorting',
        'setup.loadprofileconfiguration.LoadProfileConfigurationFiltering',
        'setup.loadprofileconfiguration.LoadProfileConfigurationDockedItems',
        'setup.loadprofileconfiguration.LoadProfileConfigurationGrid',
        'setup.loadprofileconfiguration.LoadProfileConfigurationPreview',
        'setup.loadprofileconfiguration.LoadProfileConfigurationEmptyList',
        'setup.loadprofileconfiguration.LoadProfileConfigurationForm'
    ],

    stores: [
        'Mdc.store.Intervals',
        'Mdc.store.LoadProfileTypes',
        'Mdc.store.LoadProfileConfigurationsOnDeviceConfiguration',
        'Mdc.store.LoadProfileConfigurationsOnDeviceConfigurationAvailable'
    ],

    refs: [
        {ref: 'loadProfileConfigurationDetailForm', selector: '#loadProfileConfigurationDetails'},
        {ref: 'loadConfigurationGrid', selector: '#loadProfileConfigurationGrid'},
        {ref: 'loadConfigurationCountContainer', selector: '#loadProfileConfigurationCountContainer'},
        {ref: 'loadConfigurationEmptyListContainer', selector: '#loadProfileConfigurationEmptyListContainer'},
        {ref: 'loadConfigurationPreview', selector: '#loadProfileConfigurationPreview'},
        {ref: 'loadConfigurationForm', selector: '#LoadProfileConfigurationFormId'}



    ],

    deviceTypeId: null,
    deviceConfigurationId: null,

    init: function () {
        this.control({
            'loadProfileConfigurationSetup': {
                afterrender: this.loadStore
            },
            'loadProfileConfigurationSetup loadProfileConfigurationGrid': {
                itemclick: this.loadGridItemDetail
            },
            'loadProfileConfigurationForm combobox[name=id]': {
                select: this.changeDisplayedObisCode
            },
            'loadProfileConfigurationForm button[name=loadprofileconfigurationaction]': {
                click: this.onSubmit
            },
            'button[action=loadprofileconfigurationnotificationerrorretry]': {
                click: this.retrySubmit
            }
        });


        this.listen({
            store: {
                '#LoadProfileConfigurationsOnDeviceConfiguration': {
                    load: this.checkLoadProfileConfigurationsCount
                }
            }
        });

        this.intervalStore = this.getStore('Intervals');
        this.store = this.getStore('LoadProfileConfigurationsOnDeviceConfiguration');
        this.availableLoadProfileTypesStore = this.getStore('LoadProfileConfigurationsOnDeviceConfigurationAvailable');
    },

    changeDisplayedObisCode: function(combobox) {
        var record = this.availableLoadProfileTypesStore.findRecord('id', combobox.getValue() );
        combobox.next().setValue(record.getData().obisCode);
    },


    loadStore: function () {
        this.store.load({
            params: {
                sort: 'name'
            }
        });
    },

    retrySubmit: function (btn) {
        var formPanel = this.getLoadConfigurationForm();
        btn.up('messagebox').hide();
        if (!Ext.isEmpty(formPanel)) {
            var submitBtn = formPanel.down('button[name=loadprofileconfigurationaction]');
            if (!Ext.isEmpty(submitBtn)) {
                this.onSubmit(submitBtn);
            }
        }
    },

    onSubmit: function(btn) {
        var me = this,
            formPanel = me.getLoadConfigurationForm(),
            form = formPanel.getForm(),
            formErrorsPanel = formPanel.down('panel[name=errors]'),
            formValue = form.getValues(),
            preloader,
            jsonValues;
        if (form.isValid()) {
            jsonValues = Ext.JSON.encode(formValue);
            formErrorsPanel.hide();
            switch (btn.text) {
                case 'Add':
                    preloader = Ext.create('Ext.LoadMask', {
                        msg: "Adding load profile configuration",
                        target: formPanel
                    });
                    preloader.show();
                    Ext.Ajax.request({
                        url: '/api/dtc/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/loadprofileconfigurations',
                        method: 'POST',
                        jsonData: jsonValues,
                        success: function () {
                            me.handleSuccessRequest('Successfully created');
                        },
                        failure: function (response) {
                            me.handleFailureRequest(response, 'Error during create');
                        },
                        callback: function () {
                            preloader.destroy();
                        }
                    });
                    break;
            }
        } else {
            formErrorsPanel.hide();
            formErrorsPanel.removeAll();
            formErrorsPanel.add({
                html: 'There are errors on this page that require your attention.',
                style: {
                    color: 'red'
                }
            });
            formErrorsPanel.show();
        }
    },

    handleSuccessRequest: function (headerText) {
        window.location.href = '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/loadprofiles';
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
                    action: 'loadprofileconfigurationnotificationerrorretry',
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

    checkLoadProfileConfigurationsCount: function () {
        var grid = this.getLoadConfigurationGrid();
        if (!Ext.isEmpty(grid)) {
            var numberOfLoadConfigurationsContainer = this.getLoadConfigurationCountContainer(),
                emptyMessage = this.getLoadConfigurationEmptyListContainer(),
                gridView = grid.getView(),
                selectionModel = gridView.getSelectionModel(),
                loadConfigurationCount = this.store.getCount(),
                loadConfigurationWord;

            if (loadConfigurationCount == 1) {
                loadConfigurationWord = ' load profile configuration'
            } else {
                loadConfigurationWord = ' load profile configurations'
            }
            var widget = Ext.widget('container', {
                html: loadConfigurationCount + loadConfigurationWord
            });

            numberOfLoadConfigurationsContainer.removeAll(true);
            numberOfLoadConfigurationsContainer.add(widget);

            if (loadConfigurationCount < 1) {
                grid.hide();
                emptyMessage.add(
                    {
                        xtype: 'loadProfileConfigurationEmptyList',
                        actionHref: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/loadprofiles/add'
                    }
                );
                this.getLoadConfigurationPreview().hide();
            } else {
                selectionModel.select(0);
                grid.fireEvent('itemclick', gridView, selectionModel.getLastSelected());
            }
        }
    },

    loadGridItemDetail: function (grid, record) {
        var form = this.getLoadProfileConfigurationDetailForm(),
            preview = this.getLoadConfigurationPreview(),
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
        preview.setTitle(recordData.name);
        form.loadRecord(record);
        preloader.destroy();
    },

    showDeviceConfigurationLoadProfilesView: function (deviceTypeId, deviceConfigurationId) {
        var me = this,
            widget = Ext.widget('loadProfileConfigurationSetup', {intervalStore: this.intervalStore, deviceTypeId: deviceTypeId, deviceConfigId: deviceConfigurationId});
        widget.down('#loadProfileConfigurationTitle').html = '<h1>' + Uni.I18n.translate('loadprofileconfigurations.loadprofileconfigurations', 'MDC', 'Load profile configurations') + '</h1>';
        me.deviceTypeId = deviceTypeId;
        me.deviceConfigurationId = deviceConfigurationId;
        me.store.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigurationId});
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigurationId, {
                    success: function (deviceConfig) {
                        me.deviceTypeName = deviceType.get('name');
                        me.deviceConfigName = deviceConfig.get('name');
                        me.getApplication().fireEvent('changecontentevent', widget);
                    }
                });
            }
        });
    },

    showDeviceConfigurationLoadProfilesAddView: function (deviceTypeId, deviceConfigurationId) {
        var me = this,
            widget = Ext.widget('loadProfileConfigurationForm', {deviceTypeId: deviceTypeId, deviceConfigurationId: deviceConfigurationId, loadProfileConfigurationAction: 'Add'});
        widget.down('#LoadProfileConfigurationHeader').html = '<h1>' + Uni.I18n.translate('loadprofileconfigurations.addloadprofileconfigurations', 'MDC', 'Add Load profile configuration') + '</h1>';
        me.deviceTypeId = deviceTypeId;
        me.deviceConfigurationId = deviceConfigurationId;
        me.store.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigurationId});
        me.availableLoadProfileTypesStore.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigurationId});
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigurationId, {
                    success: function (deviceConfig) {
                        me.deviceTypeName = deviceType.get('name');
                        me.deviceConfigName = deviceConfig.get('name');
                        me.getApplication().fireEvent('changecontentevent', widget);
                        me.availableLoadProfileTypesStore.load();
                        widget.down('combobox[name=id]').store = me.availableLoadProfileTypesStore
                    }
                });
            }
        });
    }
});