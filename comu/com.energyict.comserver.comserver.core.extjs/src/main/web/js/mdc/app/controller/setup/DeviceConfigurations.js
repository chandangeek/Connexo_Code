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
        //{ref: 'deviceConfigurationDetailsLink', selector: '#deviceConfigurationDetailsLink'},
        {ref: 'deviceConfigurationDetailForm', selector: '#deviceConfigurationDetailForm'},
        {ref: 'activateDeactivateDeviceConfiguration', selector: '#activateDeactivateDeviceConfiguration'},
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
        {ref: 'activateDeviceconfigurationMenuItem', selector: '#activateDeviceconfigurationMenuItem'},
        {ref: 'gatewayCheckbox', selector: '#gatewayCheckbox'},
        {ref: 'addressableCheckbox', selector: '#addressableCheckbox'},
        {ref: 'gatewayMessage', selector: '#gatewayMessage'},
        {ref: 'addressableMessage', selector: '#addressableMessage'},
        {ref: 'editLogbookConfiguration', selector: 'edit-logbook-configuration'} ,

        {ref: 'actionStatusMenuItem', selector: '#actionStatusMenuItem'}
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
                editItem: this.editDeviceConfigurationHistory,
                deleteItem: this.deleteDeviceConfiguration,
                activateItem: this.activateDeviceConfiguration
            },
            '#deviceConfigurationPreview menuitem[action=editDeviceConfiguration]': {
                click: this.editDeviceConfigurationHistoryFromPreview
            },
            '#deviceConfigurationPreview menuitem[action=deleteDeviceConfiguration]': {
                click: this.deleteDeviceConfigurationFromPreview
            },
            '#deviceConfigurationPreview menuitem[action=activateDeactivateDeviceConfiguration]': {
                click: this.activateDeviceConfiguration
            },
            '#deviceConfigurationDetail menuitem[action=deleteDeviceConfiguration]': {
                click: this.deleteDeviceConfigurationFromDetails
            },
            '#deviceConfigurationDetail menuitem[action=editDeviceConfiguration]': {
                click: this.editDeviceConfigurationFromDetailsHistory
            },
            '#deviceConfigurationDetail menuitem[action=activateDeactivateDeviceConfiguration]': {
                click: this.activateDeviceConfigurationFromDetails
            },
            '#createEditButton[action=createDeviceConfiguration]': {
                click: this.createDeviceConfiguration
            },
            '#createEditButton[action=editDeviceConfiguration]': {
                click: this.editDeviceConfiguration
            }
        });
    },

    showEditView: function (id) {

    },

    showDeviceConfigurations: function (id) {
        var me = this;
        this.deviceTypeId = id;
        this.getDeviceConfigurationsStore().getProxy().setExtraParam('deviceType', id);
        var widget = Ext.widget('deviceConfigurationsSetup', {deviceTypeId: id});
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(id, {
            success: function (deviceType) {
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getDeviceConfigurationsGrid().getSelectionModel().doSelect(0);
            }
        });

    },

    previewDeviceConfiguration: function (grid, record) {
        var deviceConfigurations = this.getDeviceConfigurationsGrid().getSelectionModel().getSelection();
        if (deviceConfigurations.length == 1) {
            var deviceConfigurationId = deviceConfigurations[0].get('id');
            var activateDeactivateText =
                deviceConfigurations[0].get('active') ?
                    Uni.I18n.translate('general.deActivate', 'MDC', 'Deactivate') :
                    Uni.I18n.translate('general.activate', 'MDC', 'Activate');
            this.getActionStatusMenuItem().setText(activateDeactivateText);
            this.getActivateDeviceconfigurationMenuItem().setText(deviceConfigurations[0].get('active') === true ? Uni.I18n.translate('general.deActivate', 'MDC', 'Deactivate') : Uni.I18n.translate('general.activate', 'MDC', 'Activate'));
            this.getDeviceConfigurationRegisterLink().getEl().set({href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + deviceConfigurationId + '/registerconfigurations'});
            this.getDeviceConfigurationRegisterLink().getEl().setHTML(deviceConfigurations[0].get('registerCount') + ' ' + Uni.I18n.translatePlural('deviceconfig.registerconfigs', deviceConfigurations[0].get('registerCount'), 'MDC', 'register configurations'));
            this.getDeviceConfigurationLogBookLink().getEl().set({href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + deviceConfigurationId + '/logbookconfigurations'});
            this.getDeviceConfigurationLogBookLink().getEl().setHTML(deviceConfigurations[0].get('logBookCount') + ' ' + Uni.I18n.translatePlural('deviceconfiguration.logbooks', deviceConfigurations[0].get('logBookCount'), 'MDC', 'logbooks'));
            this.getDeviceConfigurationLoadProfilesLink().getEl().set({href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + deviceConfigurationId + '/loadprofiles'});
            this.getDeviceConfigurationLoadProfilesLink().getEl().setHTML(deviceConfigurations[0].get('loadProfileCount') + ' ' + Uni.I18n.translatePlural('deviceconfiguration.loadprofiles', deviceConfigurations[0].get('loadProfileCount'), 'MDC', 'load profiles'));
            this.getDeviceConfigurationPreviewForm().loadRecord(deviceConfigurations[0]);
            this.getDeviceConfigurationPreview().getLayout().setActiveItem(1);
            //this.getDeviceConfigurationDetailsLink().update('<a href="#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + deviceConfigurationId + '">' + Uni.I18n.translate('general.viewDetails', 'MDC', 'View details') + '</a>');
            this.getDeviceConfigurationPreview().setTitle(deviceConfigurations[0].get('name'));
            //this.getDeviceConfigurationPreviewTitle().update('<h1>' + deviceConfigurations[0].get('name') + '</h1>');
        } else {
            this.getDeviceConfigurationPreview().getLayout().setActiveItem(0);
        }
    },

    showDeviceConfigurationDetailsView: function (devicetype, deviceconfiguration) {
        var me = this;
        var widget = Ext.widget('deviceConfigurationDetail', {deviceTypeId: devicetype, deviceConfigurationId: deviceconfiguration});
        var deviceConfigModel = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
        this.deviceTypeId = devicetype;
        deviceConfigModel.getProxy().setExtraParam('deviceType', devicetype);
        deviceConfigModel.load(deviceconfiguration, {
            success: function (deviceConfiguration) {
                me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfiguration);
                Ext.ModelManager.getModel('Mdc.model.DeviceType').load(devicetype,{
                    success: function(deviceType){
                        me.getApplication().fireEvent('loadDeviceType', deviceType);
                        var deviceConfigurationId = deviceConfiguration.get('id');
                        me.getDeviceConfigurationDetailDeviceTypeLink().getEl().set({href: '#/administration/devicetypes/' + me.deviceTypeId});
                        me.getDeviceConfigurationDetailDeviceTypeLink().getEl().setHTML(deviceType.get('name'));
                        me.getDeviceConfigurationDetailRegisterLink().getEl().set({href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + deviceConfigurationId + '/registerconfigurations'});
                        me.getDeviceConfigurationDetailRegisterLink().getEl().setHTML(deviceConfiguration.get('registerCount') + ' ' + Uni.I18n.translatePlural('deviceconfig.registerconfigs', deviceConfiguration.get('registerCount'), 'MDC', 'register configurations'));
                        me.getDeviceConfigurationDetailLogBookLink().getEl().set({href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + deviceConfigurationId + '/logbookconfigurations'});
                        me.getDeviceConfigurationDetailLogBookLink().getEl().setHTML(deviceConfiguration.get('logBookCount') + ' ' + Uni.I18n.translatePlural('deviceconfiguration.logbooks', deviceConfiguration.get('logBookCount'), 'MDC', 'logbooks'));
                        me.getDeviceConfigurationDetailLoadProfilesLink().getEl().set({href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + deviceConfigurationId + '/loadprofiles'});
                        me.getDeviceConfigurationDetailLoadProfilesLink().getEl().setHTML(deviceConfiguration.get('loadProfileCount') + ' ' + Uni.I18n.translatePlural('deviceconfiguration.loadprofiles', deviceConfiguration.get('loadProfileCount'), 'MDC', 'load profiles'));
                        me.getDeviceConfigurationPreviewTitle().update('<h1>' + deviceConfiguration.get('name') + ' - ' + Uni.I18n.translate('general.overview', 'MDC', 'Overview') + '</h1>');
                        me.getActivateDeactivateDeviceConfiguration().setText(deviceConfiguration.get('active')===true?Uni.I18n.translate('general.deActivate', 'MDC', 'Deactivate'):Uni.I18n.translate('general.activate', 'MDC', 'Activate'));

                        widget.down('form').loadRecord(deviceConfiguration);
                    }
                });
            }
        });
        me.getApplication().fireEvent('changecontentevent', widget);
    },

    createDeviceConfigurationHistory: function () {
        location.href = '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/create';
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

    activateDeviceConfiguration: function (deviceConfigurationToActivateDeactivate) {
        var me = this;
        if (deviceConfigurationToActivateDeactivate.hasOwnProperty('action')) {
            deviceConfigurationToActivateDeactivate = this.getDeviceConfigurationsGrid().getSelectionModel().getSelection()[0];
        }
        if (deviceConfigurationToActivateDeactivate.get('active') === true) {
            deviceConfigurationToActivateDeactivate.set('active', false);
        } else {
            deviceConfigurationToActivateDeactivate.set('active', true);
        }
        deviceConfigurationToActivateDeactivate.getProxy().setExtraParam('deviceType', this.deviceTypeId);
        deviceConfigurationToActivateDeactivate.save({
            callback: function () {
                me.previewDeviceConfiguration();
            }
        });
    },

    activateDeviceConfigurationFromDetails: function () {
        var me = this;
        var deviceConfigurationToActivateDeactivate = this.getDeviceConfigurationDetailForm().getRecord();
        if (deviceConfigurationToActivateDeactivate.get('active') === true) {
            deviceConfigurationToActivateDeactivate.set('active', false);
        } else {
            deviceConfigurationToActivateDeactivate.set('active', true);
        }
        deviceConfigurationToActivateDeactivate.getProxy().setExtraParam('deviceType', this.deviceTypeId);
        deviceConfigurationToActivateDeactivate.save({
            callback: function () {
                me.getDeviceConfigurationDetailForm().loadRecord(deviceConfigurationToActivateDeactivate);
                me.getActivateDeactivateDeviceConfiguration().setText(deviceConfigurationToActivateDeactivate.get('active') === true ? Uni.I18n.translate('general.deActivate', 'MDC', 'Deactivate') : Uni.I18n.translate('general.activate', 'MDC', 'Activate'));
            }
        });
    },

    deleteDeviceConfiguration: function (deviceConfigurationToDelete) {
        var me = this;

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('deviceconfiguration.deleteDeviceConfiguration', 'MDC', 'The item will no longer be available.'),
            title: Uni.I18n.translate('general.delete', 'MDC', 'delete') + ' ' + deviceConfigurationToDelete.get('name') + '?',
            config: {
                registerConfigurationToDelete: deviceConfigurationToDelete,
                me: me
            },
            fn: function (btn) {
                if (btn === 'confirm') {
                    deviceConfigurationToDelete.getProxy().setExtraParam('deviceType', me.deviceTypeId);
                    deviceConfigurationToDelete.destroy({
                        callback: function () {
                            location.href = '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations';
                        }
                    });
                }
            }
        });
    },

    deleteDeviceConfigurationFromPreview: function () {
        this.deleteDeviceConfiguration(this.getDeviceConfigurationsGrid().getSelectionModel().getSelection()[0])
    },

    deleteDeviceConfigurationFromDetails: function () {
        var me = this;
        var deviceConfigurationToDelete = this.getDeviceConfigurationDetailForm().getRecord();
        deviceConfigurationToDelete.getProxy().setExtraParam('deviceType', this.deviceTypeId);
        deviceConfigurationToDelete.destroy({
            callback: function () {
                location.href = '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations';
            }
        });
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
                me.getApplication().fireEvent('changecontentevent', widget);
                widget.down('#deviceConfigurationEditCreateTitle').update('<h1>' + Uni.I18n.translate('general.add', 'MDC', 'Add') + ' ' + 'device configuration' + '</h1>');
                me.setCheckBoxes(deviceType);
            }
        });
    },

    setCheckBoxes: function (deviceType) {
        if (!deviceType.get('canBeGateway')) {
            this.getGatewayCheckbox().setDisabled(!deviceType.get('canBeGateway'));
            this.getGatewayMessage().show();
        }
        if (!deviceType.get('canBeDirectlyAddressed')) {
            this.getAddressableCheckbox().setDisabled(!deviceType.get('canBeDirectlyAddressed'));
            this.getAddressableMessageMessage().show();
        }
    },

    showDeviceConfigurationEditView: function (deviceTypeId, deviceConfigurationId) {
        this.deviceTypeId = deviceTypeId;
        var me = this;
        var returnlink;
        if (me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens().indexOf('null') > -1) {
            returnlink = '#/administration/devicetypes/';
        } else {
            returnlink = me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens();
        }
        var widget = Ext.widget('deviceConfigurationEdit', {
            edit: true,
            returnLink: returnlink
        });
        this.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        var me = this;
        var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
        model.getProxy().setExtraParam('deviceType', this.deviceTypeId);
        model.load(deviceConfigurationId, {
            success: function (deviceConfiguration) {
                Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
                    success: function (deviceType) {
                        widget.down('form').loadRecord(deviceConfiguration);
                        widget.down('#deviceConfigurationEditCreateTitle').update('<h1>' + Uni.I18n.translate('general.edit', 'MDC', 'Edit') + ' "' + deviceConfiguration.get('name') + '"</h1>');
                        me.setCheckBoxes(deviceType);
                        widget.setLoading(false);
                    }
                });

            }
        });
    },

    createDeviceConfiguration: function () {
        var me = this;
        var record = Ext.create(Mdc.model.DeviceConfiguration),
            values = this.getDeviceConfigurationEditForm().getValues();
        if (record) {
            record.set(values);
            record.getProxy().setExtraParam('deviceType', this.deviceTypeId);
            record.save({
                success: function (record) {
                    location.href = '#/administration/devicetypes/' + me.deviceTypeId + /deviceconfigurations/ + record.get('id');
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        me.getDeviceConfigurationEditForm().getForm().markInvalid(json.errors);
                    }
                }
            });

        }
    },

    editDeviceConfiguration: function () {
        var record = this.getDeviceConfigurationEditForm().getRecord(),
            values = this.getDeviceConfigurationEditForm().getValues();
        var me = this;
        if (record) {
            record.set(values);
            record.getProxy().setExtraParam('deviceType', this.deviceTypeId);
            record.save({
                callback: function (record) {
                    location.href = me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens()
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
                            deviceConfigModel.load(deviceConfigurationId, {
                                success: function (deviceConfiguration) {
                                    widget.setLoading(false);
                                }
                            });
                        }
                    });
                    var numberOfLogbooksContainer = Ext.ComponentQuery.query('device-configuration-logbooks toolbar container[name=LogBookCount]')[0],
                        grid = Ext.ComponentQuery.query('device-configuration-logbooks grid')[0],
                        gridView = grid.getView(),
                        selectionModel = gridView.getSelectionModel(),
                        count = Ext.widget('container', {
                            html: self.getCount() + ' logbook configuration(s)'
                        });
                    numberOfLogbooksContainer.removeAll(true);
                    numberOfLogbooksContainer.add(count);
                    if (self.getCount() < 1) {
                        grid.hide();
                        grid.next().show();
                    } else {
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
            store = Ext.data.StoreManager.lookup('LogbookConfigurations');
        store.getProxy().setExtraParam('deviceType', deviceTypeId);
        store.getProxy().setExtraParam('available', true);
        store.getProxy().setExtraParam('deviceConfiguration', deviceConfigurationId);
        deviceConfigModel.getProxy().setExtraParam('deviceType', deviceTypeId);
        store.load(
            {
                callback: function () {
                    var self = this,
                        widget = Ext.widget('add-logbook-configurations', {
                            deviceTypeId: deviceTypeId,
                            deviceConfigurationId: deviceConfigurationId
                        });
                    me.getApplication().fireEvent('changecontentevent', widget);
                    widget.setLoading(true);
                    model.load(deviceTypeId, {
                        success: function (deviceType) {
                            deviceConfigModel.load(deviceConfigurationId, {
                                success: function (deviceConfiguration) {
                                    widget.setLoading(false);
                                }
                            });
                        }
                    });
                    var numberOfLogbooksLabel = Ext.ComponentQuery.query('add-logbook-configurations toolbar label[name=LogBookCount]')[0],
                        grid = Ext.ComponentQuery.query('add-logbook-configurations grid')[0];
                    numberOfLogbooksLabel.setText('No logbooks selected');
                    if (self.getCount() < 1) {
                        grid.hide();
                        grid.next().show();
                    }
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
                            deviceConfigModel.load(deviceConfigurationId, {
                                success: function (deviceConfiguration) {
                                    widget.setLoading(false);
                                }
                            });
                        }
                    });
                }
            }
        );
    },
});

