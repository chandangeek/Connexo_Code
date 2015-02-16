Ext.define('Mdc.controller.setup.DeviceConfigurations', {
    extend: 'Ext.app.Controller',

    requires: [
    ],
    deviceTypeId: null,
    views: [
        'setup.deviceconfiguration.DeviceConfigurationsSetup',
        'setup.deviceconfiguration.DeviceConfigurationsGrid',
        'setup.deviceconfiguration.DeviceConfigurationPreview',
        'setup.deviceconfiguration.DeviceConfigurationDetail',
        'setup.deviceconfiguration.DeviceConfigurationEdit',
        'setup.deviceconfiguration.DeviceConfigurationLogbooks',
        'setup.deviceconfiguration.AddLogbookConfigurations',
        'setup.deviceconfiguration.EditLogbookConfiguration'
    ],

    stores: [
        'DeviceConfigurations',
        'DeviceTypes',
        'LogbookConfigurations'
    ],

    refs: [
        {ref: 'deviceConfigurationsGrid', selector: '#deviceconfigurationsgrid'},
        {ref: 'deviceConfigurationPreviewForm', selector: '#deviceConfigurationPreviewForm'},
        {ref: 'deviceConfigurationPreview', selector: '#deviceConfigurationPreview'},
        {ref: 'deviceConfigurationDetail', selector: '#deviceConfigurationDetail'},
        {ref: 'deviceConfigurationDetailForm', selector: '#deviceConfigurationDetailForm'},
        {ref: 'deviceConfigurationPreviewTitle', selector: '#deviceConfigurationPreviewTitle'},
        {ref: 'deviceConfigurationRegisterLink', selector: '#deviceConfigurationRegistersLink'},
        {ref: 'deviceConfigurationLogBookLink', selector: '#deviceConfigurationLogBooksLink'},
        {ref: 'deviceConfigurationLoadProfilesLink', selector: '#deviceConfigurationLoadProfilesLink'},
        {ref: 'deviceConfigurationDetailRegisterLink', selector: '#deviceConfigurationDetailRegistersLink'},
        {ref: 'deviceConfigurationDetailLogBookLink', selector: '#deviceConfigurationDetailLogBooksLink'},
        {ref: 'deviceConfigurationDetailLoadProfilesLink', selector: '#deviceConfigurationDetailLoadProfilesLink'},
        {ref: 'deviceConfigurationDetailDeviceTypeLink', selector: '#deviceConfigurationDetailDeviceTypeLink'},
        {ref: 'deviceConfigurationDetailForm', selector: '#deviceConfigurationDetailForm'},
        {ref: 'deviceConfigurationEditForm', selector: '#deviceConfigurationEditForm'},
        {ref: 'deviceConfigurationEdit', selector: '#deviceConfigurationEdit'},
        {ref: 'deviceConfigurationsSetup', selector: '#deviceConfigurationsSetup'},
        {ref: 'activateDeviceconfigurationMenuItem', selector: '#activateDeviceconfigurationMenuItem'},
        {ref: 'gatewayMessage', selector: '#deviceConfigurationEditForm #gatewayMessage'},
        {ref: 'addressableMessage', selector: '#deviceConfigurationEditForm #addressableMessage'},
        {ref: 'gatewayCombo', selector: '#deviceConfigurationEditForm #deviceIsGatewayCombo'},
        {ref: 'addressableCombo', selector: '#deviceConfigurationEditForm #directlyAddressableCombo'},
        {ref: 'typeOfGatewayCombo', selector: '#deviceConfigurationEditForm #typeOfGatewayCombo'},
        {ref: 'typeOfGatewayComboContainer', selector: '#deviceConfigurationEditForm #typeOfGatewayComboContainer'},
        {ref: 'editLogbookConfiguration', selector: 'edit-logbook-configuration'},
        {ref: 'activateDeviceconfigurationMenuItem', selector: '#activateDeviceconfigurationMenuItem'}
    ],

    init: function () {
        this.control({
            '#deviceconfigurationsgrid': {
                selectionchange: this.previewDeviceConfiguration
            },
            '#deviceConfigurationsSetup button[action = createDeviceConfiguration]': {
                click: this.createDeviceConfigurationHistory
            },
            '#deviceconfigurationsgrid actioncolumn': {
                editDeviceConfiguration: this.editDeviceConfigurationHistory,
                deleteDeviceConfiguration: this.deleteTheDeviceConfiguration
            },
            '#deviceConfigurationPreview menuitem[action=editDeviceConfiguration]': {
                click: this.editDeviceConfigurationHistoryFromPreview
            },
            'add-logbook-configurations add-logbook-configurations-grid': {
                selectionchange: this.enableAddBtn
            },
            '#deviceConfigurationPreview menuitem[action=deleteDeviceConfiguration]': {
                click: this.deleteDeviceConfigurationFromPreview
            },
            '#deviceConfigurationDetail menuitem[action=deleteDeviceConfiguration]': {
                click: this.deleteDeviceConfigurationFromDetails
            },
            '#deviceConfigurationDetail menuitem[action=editDeviceConfiguration]': {
                click: this.editDeviceConfigurationFromDetailsHistory
            },
            '#device-configuration-action-menu': {
                click: this.chooseAction
            },
            'deviceConfigurationEdit #createEditButton': {
                click: this.createEditDeviceConfiguration
            },
            '#deviceConfigurationEditForm #deviceIsGatewayCombo': {
                change: this.showTypeOfGatewayCombo
            }
        });
    },

    enableAddBtn: function (selectionModel) {
        var addBtn = Ext.ComponentQuery.query('add-logbook-configurations #logbookConfAdd')[0];
        if (addBtn) {
            selectionModel.getSelection().length > 0 ? addBtn.enable() : addBtn.disable();
        }
    },

    showTypeOfGatewayCombo: function (deviceIsGatewayCombo) {
        this.getTypeOfGatewayComboContainer().setVisible(deviceIsGatewayCombo.getValue().canBeGateway);
    },

    showDeviceConfigurations: function (id) {
        var me = this;
        this.deviceTypeId = id;
        this.getDeviceConfigurationsStore().getProxy().setExtraParam('deviceType', id);
        this.getDeviceConfigurationsStore().load({
            callback: function () {
                var widget = Ext.widget('deviceConfigurationsSetup', {deviceTypeId: id});
                Ext.ModelManager.getModel('Mdc.model.DeviceType').load(id, {
                    success: function (deviceType) {
                        me.getApplication().fireEvent('loadDeviceType', deviceType);
                        me.getApplication().fireEvent('changecontentevent', widget);
                        widget.down('deviceTypeSideMenu #overviewLink').setText(deviceType.get('name'));
                        me.getDeviceConfigurationsGrid().getSelectionModel().doSelect(0);
                    }
                });
            }
        });

    },

    previewDeviceConfiguration: function (grid, record) {
        var deviceConfigurations = this.getDeviceConfigurationsGrid().getSelectionModel().getSelection();
        if (deviceConfigurations.length == 1) {
            var deviceConfigurationId = deviceConfigurations[0].get('id');
            this.getDeviceConfigurationRegisterLink().getEl().set({href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + deviceConfigurationId + '/registerconfigurations'});
            this.getDeviceConfigurationRegisterLink().getEl().setHTML(deviceConfigurations[0].get('registerCount') + ' ' + Uni.I18n.translatePlural('deviceconfig.registerconfigs', deviceConfigurations[0].get('registerCount'), 'MDC', 'register configurations'));
            this.getDeviceConfigurationLogBookLink().getEl().set({href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + deviceConfigurationId + '/logbookconfigurations'});
            this.getDeviceConfigurationLogBookLink().getEl().setHTML(deviceConfigurations[0].get('logBookCount') + ' ' + Uni.I18n.translatePlural('general.logbookConfigurations', deviceConfigurations[0].get('logBookCount'), 'MDC', 'logbook configurations'));
            this.getDeviceConfigurationLoadProfilesLink().getEl().set({href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + deviceConfigurationId + '/loadprofiles'});
            this.getDeviceConfigurationLoadProfilesLink().getEl().setHTML(deviceConfigurations[0].get('loadProfileCount') + ' ' + Uni.I18n.translatePlural('general.loadProfileConfigurations', deviceConfigurations[0].get('loadProfileCount'), 'MDC', 'load profile configurations'));
            this.getDeviceConfigurationPreviewForm().loadRecord(deviceConfigurations[0]);
            this.getDeviceConfigurationPreview().down('#device-configuration-action-menu').record = deviceConfigurations[0];
            this.getDeviceConfigurationPreview().getLayout().setActiveItem(1);
            this.getDeviceConfigurationPreview().setTitle(deviceConfigurations[0].get('name'));
        } else {
            this.getDeviceConfigurationPreview().getLayout().setActiveItem(0);
        }
    },

    showDeviceConfigurationDetailsView: function (devicetype, deviceconfiguration) {
        var me = this;
        var widget = Ext.widget('deviceConfigurationDetail', {deviceTypeId: devicetype, deviceConfigurationId: deviceconfiguration});
        var deviceConfigModel = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
        this.deviceTypeId = devicetype;
        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        deviceConfigModel.getProxy().setExtraParam('deviceType', devicetype);
        deviceConfigModel.load(deviceconfiguration, {
            success: function (deviceConfiguration) {
                me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfiguration);
                widget.down('#stepsMenu #deviceConfigurationOverviewLink').setText(deviceConfiguration.get('name'));
                Ext.ModelManager.getModel('Mdc.model.DeviceType').load(devicetype, {
                    success: function (deviceType) {
                        me.getApplication().fireEvent('loadDeviceType', deviceType);
                        var deviceConfigurationId = deviceConfiguration.get('id');
                        me.getDeviceConfigurationDetailRegisterLink().getEl().set({href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + deviceConfigurationId + '/registerconfigurations'});
                        me.getDeviceConfigurationDetailRegisterLink().getEl().setHTML(deviceConfiguration.get('registerCount') + ' ' + Uni.I18n.translatePlural('deviceconfig.registerconfigs', deviceConfiguration.get('registerCount'), 'MDC', 'register configurations'));
                        me.getDeviceConfigurationDetailLogBookLink().getEl().set({href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + deviceConfigurationId + '/logbookconfigurations'});
                        me.getDeviceConfigurationDetailLogBookLink().getEl().setHTML(deviceConfiguration.get('logBookCount') + ' ' + Uni.I18n.translatePlural('general.logbookConfigurations', deviceConfiguration.get('logBookCount'), 'MDC', 'logbook configurations'));
                        me.getDeviceConfigurationDetailLoadProfilesLink().getEl().set({href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + deviceConfigurationId + '/loadprofiles'});
                        me.getDeviceConfigurationDetailLoadProfilesLink().getEl().setHTML(deviceConfiguration.get('loadProfileCount') + ' ' + Uni.I18n.translatePlural('general.loadProfileConfigurations', deviceConfiguration.get('loadProfileCount'), 'MDC', 'load profile configurations'));
                        //me.getDeviceConfigurationPreviewTitle().update('<h1>' + Uni.I18n.translate('general.overview', 'MDC', 'Overview') + '</h1>');
                        widget.down('form').loadRecord(deviceConfiguration);
                        widget.down('#device-configuration-action-menu').record = deviceConfiguration;
                        widget.setLoading(false);
                    }
                });
            }
        });
    },

    createDeviceConfigurationHistory: function () {
        location.href = '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/add';
    },

    editDeviceConfigurationHistory: function (record) {
        location.href = '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + record.get('id') + '/edit';
    },

    editDeviceConfigurationHistoryFromPreview: function () {
        location.href = '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.getDeviceConfigurationsGrid().getSelectionModel().getSelection()[0].get('id') + '/edit';
    },

    editDeviceConfigurationFromDetailsHistory: function () {
        this.editDeviceConfigurationHistory(this.getDeviceConfigurationDetailForm().getRecord());
    },

    chooseAction: function (menu, item) {
        var me = this,
            grid = me.getDeviceConfigurationsGrid(),
            router = this.getController('Uni.controller.history.Router'),
            activeChange = 'notChanged',
            record,
            form,
            gridView,
            viewport;


        if (grid) {
            viewport = me.getDeviceConfigurationsSetup();
            gridView = grid.getView();
            record = gridView.getSelectionModel().getLastSelected();
            form = this.getDeviceConfigurationPreview().down('form');
        } else {
            viewport = me.getDeviceConfigurationDetail();
            record = menu.record;
            form = this.getDeviceConfigurationDetailForm();
        }

        switch (item.action) {
            case 'activateDeviceConfiguration':
                activeChange = true;
                break;
            case 'deactivateDeviceConfiguration':
                activeChange = false;
                break;
        }

        if (activeChange != 'notChanged') {
            viewport.setLoading(true);

            Ext.Ajax.request({
                url: '/api/dtc/devicetypes/' + router.arguments['deviceTypeId'] + '/deviceconfigurations/' + record.get('id') + '/status',
                method: 'PUT',
                jsonData: {active: activeChange},
                success: function () {
                    record.set('active', activeChange);
                    record.commit();

                    me.getApplication().fireEvent('acknowledge', activeChange ? Uni.I18n.translate('deviceconfiguration.activated', 'MDC', 'Device configuration activated') :
                        Uni.I18n.translate('deviceconfiguration.deactivated', 'MDC', 'Device configuration deactivated'));
                },
                callback: function () {
                    viewport.setLoading(false);
                }
            });
        }
    },

    deleteTheDeviceConfiguration: function (deviceConfigurationToDelete) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('deviceconfiguration.removeDeviceConfiguration', 'MDC', 'This device configuration will no longer be available.'),
            title: Uni.I18n.translate('general.remove', 'MDC', 'Remove') + " '" + deviceConfigurationToDelete.get('name') + "'?",
            config: {
                registerConfigurationToDelete: deviceConfigurationToDelete,
                me: me
            },
            fn: function (btn) {
                if (btn === 'confirm') {
                    deviceConfigurationToDelete.getProxy().setExtraParam('deviceType', me.deviceTypeId);
                    deviceConfigurationToDelete.destroy({
                        success: function () {
                            var grid = me.getDeviceConfigurationsGrid(),
                                gridPagingToolbar;
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceconfiguration.acknowledgment.removed', 'MDC', 'Device configuration removed'));
                            if (router.currentRoute === 'administration/devicetypes/view/deviceconfigurations/view') {
                                router.getRoute('administration/devicetypes/view/deviceconfigurations').forward();
                            } else if (grid) {
                                gridPagingToolbar = grid.down('pagingtoolbartop');
                                gridPagingToolbar.isFullTotalCount = false;
                                gridPagingToolbar.totalCount = -1;
                                grid.down('pagingtoolbarbottom').totalCount--;
                                grid.getStore().loadPage(1);
                            }
                        },
                        failure: function () {

                        }
                    });
                }
            }
        });
    },

    deleteDeviceConfigurationFromPreview: function () {
        this.deleteTheDeviceConfiguration(this.getDeviceConfigurationsGrid().getSelectionModel().getSelection()[0])
    },

    deleteDeviceConfigurationFromDetails: function () {
        var deviceConfigurationToDelete = this.getDeviceConfigurationDetailForm().getRecord();
        this.deleteTheDeviceConfiguration(deviceConfigurationToDelete);
    },

    showDeviceConfigurationCreateView: function (deviceTypeId) {
        var me = this;
        this.deviceTypeId = deviceTypeId;
        var widget = Ext.widget('deviceConfigurationEdit', {
            edit: false,
            returnLink: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations'
        });

        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.down('#deviceConfigurationEditCreateTitle').setTitle(Uni.I18n.translate('general.add', 'MDC', 'Add') + ' ' + 'device configuration');
                me.setRadioButtons(deviceType);
            }
        });
    },

    setRadioButtons: function (deviceType, deviceConfiguration) {
        var addressableCombo = this.getAddressableCombo(),
            gatewayCombo = this.getGatewayCombo(),
            gatewayMessage = this.getGatewayMessage(),
            addressableMessage = this.getAddressableMessage(),
            typeOfGatewayCombo = this.getTypeOfGatewayCombo();

        if (deviceConfiguration) {
            addressableCombo.setValue({isDirectlyAddressable: deviceConfiguration.get('isDirectlyAddressable')});
            gatewayCombo.setValue({canBeGateway: deviceConfiguration.get('canBeGateway')});
            typeOfGatewayCombo.setValue({gatewayType: deviceConfiguration.get('gatewayType')});
            if (deviceConfiguration.get('active')) {
                gatewayCombo.disable();
                addressableCombo.disable();
                typeOfGatewayCombo.disable();
            }
        }

        if (!deviceType.get('canBeGateway')) {
            gatewayMessage.removeAll();
            gatewayMessage.add({
                xtype: 'container',
                html: '<span style="color: grey;padding: 0 0 0 23px;">' + Uni.I18n.translate('deviceconfiguration.gatewayMessage.cannotActAsGateway', 'MDC', 'The device cannot act as a gateway') + '</span>'
            });
            gatewayCombo.setValue({canBeGateway: false});
            gatewayCombo.disable();
        }

        if (!deviceType.get('canBeDirectlyAddressed')) {
            addressableMessage.removeAll();
            addressableMessage.add({
                xtype: 'container',
                html: '<span style="color: grey;padding: 0 0 0 23px;">' + Uni.I18n.translate('deviceconfiguration.directlyAddressableMessage', 'MDC', 'The device cannot be directly addressed') + '</span>'
            });
            addressableCombo.setValue({isDirectlyAddressable: false});
            addressableCombo.disable();
        }

    },

    showDeviceConfigurationEditView: function (deviceTypeId, deviceConfigurationId) {
        var me = this,
            model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration'),
            widget,
            returnLink;

        me.deviceTypeId = deviceTypeId;
        model.getProxy().setExtraParam('deviceType', this.deviceTypeId);


        if (me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens().indexOf('null') > -1) {
            returnLink = '#/administration/devicetypes/';
        } else {
            returnLink = me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens();
        }

        widget = Ext.widget('deviceConfigurationEdit', {
            edit: true,
            returnLink: returnLink
        });

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);


        model.load(deviceConfigurationId, {
            success: function (deviceConfiguration) {
                me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfiguration);
                Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
                    success: function (deviceType) {
                        me.getApplication().fireEvent('loadDeviceType', deviceType);
                        widget.down('form').loadRecord(deviceConfiguration);
                        widget.down('#deviceConfigurationEditCreateTitle').setTitle(Uni.I18n.translate('general.edit', 'MDC', 'Edit') + " '" + deviceConfiguration.get('name') + "'");
                        me.setRadioButtons(deviceType, deviceConfiguration);
                        widget.setLoading(false);
                    }
                });

            }
        });
    },

    createEditDeviceConfiguration: function (btn) {
        var me = this,
            editForm = me.getDeviceConfigurationEdit(),
            values = me.getDeviceConfigurationEditForm().getValues(),
            router = me.getController('Uni.controller.history.Router'),
            record;


        editForm.setLoading(true);

        if (btn.action === 'createDeviceConfiguration') {
            record = Ext.create(Mdc.model.DeviceConfiguration);
        } else {
            record = this.getDeviceConfigurationEditForm().getRecord();
        }

        if (record) {
            record.set(values);
            if (!record.get('canBeGateway')) {
                record.set('gatewayType', 'NONE')
            }
            record.getProxy().setExtraParam('deviceType', this.deviceTypeId);
            record.save({
                success: function (record) {
                    router.getRoute('administration/devicetypes/view/deviceconfigurations').forward();
                    if (btn.action === 'createDeviceConfiguration') {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceconfiguration.acknowledgment.added', 'MDC', 'Device configuration added'));
                    } else {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceconfiguration.acknowledgment.saved', 'MDC', 'Device configuration saved'));
                    }
                    editForm.setLoading(false);
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        me.getDeviceConfigurationEditForm().getForm().markInvalid(json.errors);
                    }
                    editForm.setLoading(false);
                }
            });
        }
    },

    showDeviceConfigurationLogbooksView: function (deviceTypeId, deviceConfigurationId) {
        var me = this,
            model = Ext.ModelManager.getModel('Mdc.model.DeviceType'),
            deviceConfigModel = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration'),
            store = Ext.data.StoreManager.lookup('LogbookConfigurations');
        store.getProxy().setExtraParam('deviceType', deviceTypeId);
        store.getProxy().setExtraParam('available', false);
        store.getProxy().setExtraParam('deviceConfiguration', deviceConfigurationId);
        deviceConfigModel.getProxy().setExtraParam('deviceType', deviceTypeId);
        store.load(
            {
                callback: function () {
                    var self = this,
                        widget = Ext.widget('device-configuration-logbooks', {
                            deviceTypeId: deviceTypeId,
                            deviceConfigurationId: deviceConfigurationId
                        });
                    me.getApplication().fireEvent('changecontentevent', widget);
                    widget.setLoading(true);
                    model.load(deviceTypeId, {
                        success: function (deviceType) {
                            me.getApplication().fireEvent('loadDeviceType', deviceType);
                            deviceConfigModel.load(deviceConfigurationId, {
                                success: function (deviceConfiguration) {
                                    me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfiguration);
                                    widget.down('#stepsMenu #deviceConfigurationOverviewLink').setText(deviceConfiguration.get('name'));
                                    widget.setLoading(false);
                                }
                            });
                        }
                    });
                    var grid = Ext.ComponentQuery.query('device-configuration-logbooks grid')[0],
                        gridView = grid.getView(),
                        selectionModel = gridView.getSelectionModel();
                    if (self.getCount() > 0) {
                        selectionModel.select(0);
                        grid.fireEvent('itemclick', gridView, selectionModel.getLastSelected());
                    }
                }
            }
        );
    },

    showAddDeviceConfigurationLogbooksView: function (deviceTypeId, deviceConfigurationId) {
        var me = this,
            model = Ext.ModelManager.getModel('Mdc.model.DeviceType'),
            deviceConfigModel = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration'),
            store = Ext.data.StoreManager.lookup('LogbookConfigurations'),
            widget = Ext.widget('add-logbook-configurations', {
                deviceTypeId: deviceTypeId,
                deviceConfigurationId: deviceConfigurationId
            });
        store.getProxy().setExtraParam('deviceType', deviceTypeId);
        store.getProxy().setExtraParam('available', true);
        store.getProxy().setExtraParam('deviceConfiguration', deviceConfigurationId);
        deviceConfigModel.getProxy().setExtraParam('deviceType', deviceTypeId);
        store.load(
            {
                callback: function () {
                    me.getApplication().fireEvent('changecontentevent', widget);
                    if (!this.getCount()) {
                        widget.down('button[action=add]').disable();
                    }
                    widget.setLoading(true);
                    model.load(deviceTypeId, {
                        success: function (deviceType) {
                            me.getApplication().fireEvent('loadDeviceType', deviceType);
                            deviceConfigModel.load(deviceConfigurationId, {
                                success: function (deviceConfiguration) {
                                    me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfiguration);
                                    widget.setLoading(false);
                                }
                            });
                        }
                    });
                    var numberOfLogbooksLabel = Ext.ComponentQuery.query('add-logbook-configurations #logbook-count')[0],
                        grid = Ext.ComponentQuery.query('add-logbook-configurations grid')[0];
                    numberOfLogbooksLabel.setText(Uni.I18n.translate('logbookConfiguration.zeroSelected', 'MDC', '0 logbook configurations selected'));
                }
            }
        );
    },

    showEditDeviceConfigurationLogbooksView: function (deviceTypeId, deviceConfigurationId, logbookConfigurationId) {
        var me = this,
            model = Ext.ModelManager.getModel('Mdc.model.DeviceType'),
            deviceConfigModel = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration'),
            store = Ext.data.StoreManager.lookup('LogbookConfigurations');
        store.getProxy().setExtraParam('deviceType', deviceTypeId);
        store.getProxy().setExtraParam('available', false);
        store.getProxy().setExtraParam('deviceConfiguration', deviceConfigurationId);
        deviceConfigModel.getProxy().setExtraParam('deviceType', deviceTypeId);
        store.load(
            {
                callback: function (records) {
                    var widget = Ext.widget('edit-logbook-configuration', {
                        deviceTypeId: deviceTypeId,
                        deviceConfigurationId: deviceConfigurationId,
                        logbookConfigurationId: logbookConfigurationId
                    });
                    me.getApplication().fireEvent('changecontentevent', widget);
                    widget.setLoading(true);
                    var editView = me.getEditLogbookConfiguration(),
                        form = editView.down('form'),
                        overruledObisField = form.down('[name=overruledObisCode]');
                    Ext.Array.each(records, function (rec) {
                        if (rec.data.id == logbookConfigurationId) {
                            form.loadRecord(rec);
                            if (rec.data.overruledObisCode == rec.data.obisCode) {
                                overruledObisField.setValue('');
                            }
                        }
                    });
                    model.load(deviceTypeId, {
                        success: function (deviceType) {
                            me.getApplication().fireEvent('loadDeviceType', deviceType);
                            deviceConfigModel.load(deviceConfigurationId, {
                                success: function (deviceConfiguration) {
                                    me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfiguration);
                                    me.getApplication().fireEvent('loadLogbooksConfiguration', form.down('[name=name]'));
                                    widget.down('#editLogbookPanel').setTitle(Uni.I18n.translate('general.edit', 'MDC', 'Edit') + " '" + form.down('[name=name]').getValue() + "'");
                                    widget.setLoading(false);
                                }
                            });
                        }
                    });
                }
            }
        );
    }
});

