Ext.define('Mdc.controller.setup.DeviceConfigurations', {
    extend: 'Ext.app.Controller',

    requires: [],
    deviceTypeId: null,
    deviceConfigurationId: null,
    deviceConfigurationVersion: null,
    views: [
        'setup.deviceconfiguration.DeviceConfigurationsSetup',
        'setup.deviceconfiguration.DeviceConfigurationsGrid',
        'setup.deviceconfiguration.DeviceConfigurationPreview',
        'setup.deviceconfiguration.DeviceConfigurationDetail',
        'setup.deviceconfiguration.DeviceConfigurationEdit',
        'setup.deviceconfiguration.DeviceConfigurationLogbooks',
        'setup.deviceconfiguration.AddLogbookConfigurations',
        'setup.deviceconfiguration.EditLogbookConfiguration',
        'setup.deviceconfiguration.DeviceConfigurationClone'
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
        {ref: 'previewActionMenu', selector: 'deviceConfigurationPreview #device-configuration-action-menu'},
        {ref: 'deviceConfigurationCloneForm', selector: '#deviceConfigurationCloneForm'}


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
                cloneDeviceConfiguration: this.cloneDeviceConfigurationHistory,
                deleteDeviceConfiguration: this.deleteTheDeviceConfiguration
            },
            '#deviceConfigurationPreview menuitem[action=editDeviceConfiguration]': {
                click: this.editDeviceConfigurationHistoryFromPreview
            },
            '#deviceConfigurationPreview menuitem[action=cloneDeviceConfiguration]': {
                click: this.cloneDeviceConfigurationHistoryFromPreview
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
            '#deviceConfigurationDetail menuitem[action=cloneDeviceConfiguration]': {
                click: this.cloneDeviceConfigurationFromDetailsHistory
            },
            '#device-configuration-action-menu': {
                click: this.chooseAction
            },
            'deviceConfigurationEdit #createEditButton': {
                click: this.createEditDeviceConfiguration
            },
            'deviceConfigurationClone #clone-button': {
                click: this.cloneDeviceConfiguration
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
        var me = this, widget = Ext.widget('deviceConfigurationsSetup', {deviceTypeId: id});
        this.deviceTypeId = id;
        this.getDeviceConfigurationsStore().getProxy().setExtraParam('deviceType', id);
        me.getApplication().fireEvent('changecontentevent', widget);
        this.getDeviceConfigurationsStore().load({
            callback: function () {
                Ext.ModelManager.getModel('Mdc.model.DeviceType').load(id, {
                    success: function (deviceType) {
                        me.getApplication().fireEvent('loadDeviceType', deviceType);
                        widget.down('deviceTypeSideMenu #overviewLink').setText(deviceType.get('name'));
                        me.getDeviceConfigurationsGrid().getSelectionModel().doSelect(0);
                    }
                });
            }
        });
    },

    previewDeviceConfiguration: function (grid, record) {
        var deviceConfigurations = this.getDeviceConfigurationsGrid().getSelectionModel().getSelection(),
            deviceConfigurationId,
            registerLink,
            logBookLink,
            loadProfilesLink;

        if (deviceConfigurations.length == 1) {
            deviceConfigurationId = deviceConfigurations[0].get('id');
            registerLink = this.getDeviceConfigurationRegisterLink();
            logBookLink = this.getDeviceConfigurationLogBookLink();
            loadProfilesLink = this.getDeviceConfigurationLoadProfilesLink();

            Ext.suspendLayouts();

            registerLink.setHref('#/administration/devicetypes/' + encodeURIComponent(this.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(deviceConfigurationId) + '/registerconfigurations');
            registerLink.setText(
                Uni.I18n.translatePlural('general.registerConfigurations', deviceConfigurations[0].get('registerCount'), 'MDC',
                    'No register configurations', '1 register configuration', '{0} register configurations')
            );

            logBookLink.setHref('#/administration/devicetypes/' + encodeURIComponent(this.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(deviceConfigurationId) + '/logbookconfigurations');
            logBookLink.setText(
                Uni.I18n.translatePlural('general.logbookConfigurations', deviceConfigurations[0].get('logBookCount'), 'MDC',
                    'No logbook configurations', '1 logbook configuration', '{0} logbook configurations')
            );

            loadProfilesLink.setHref('#/administration/devicetypes/' + encodeURIComponent(this.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(deviceConfigurationId) + '/loadprofiles');
            loadProfilesLink.setText(
                Uni.I18n.translatePlural('general.loadProfileConfigurations', deviceConfigurations[0].get('loadProfileCount'), 'MDC',
                    'No load profile configurations', '1 load profile configuration', '{0} load profile configurations')
            );

            this.getDeviceConfigurationPreviewForm().loadRecord(deviceConfigurations[0]);

            var actionMenu = this.getDeviceConfigurationPreview().down('#device-configuration-action-menu');
            if (actionMenu)
                actionMenu.record = deviceConfigurations[0];

            if (this.getDeviceConfigurationPreview().down('#device-configuration-action-menu')) {
                this.getDeviceConfigurationPreview().down('#device-configuration-action-menu').record = deviceConfigurations[0];
            }
            this.getDeviceConfigurationPreview().getLayout().setActiveItem(1);
            this.getDeviceConfigurationPreview().setTitle(Ext.String.htmlEncode(deviceConfigurations[0].get('name')));

            Ext.resumeLayouts(true);
        } else {
            this.getDeviceConfigurationPreview().getLayout().setActiveItem(0);
        }
    },

    showDeviceConfigurationDetailsView: function (devicetype, deviceconfiguration) {
        var me = this;
        var widget = Ext.widget('deviceConfigurationDetail', {
            deviceTypeId: devicetype,
            deviceConfigurationId: deviceconfiguration
        });
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
                        var deviceConfigurationId = deviceConfiguration.get('id'),
                            registerLink = me.getDeviceConfigurationDetailRegisterLink(),
                            logBookLink = me.getDeviceConfigurationDetailLogBookLink(),
                            loadProfilesLink = me.getDeviceConfigurationDetailLoadProfilesLink();

                        me.getApplication().fireEvent('loadDeviceType', deviceType);

                        Ext.suspendLayouts();

                        registerLink.setHref('#/administration/devicetypes/' + encodeURIComponent(me.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(deviceConfigurationId) + '/registerconfigurations');
                        registerLink.setText(
                            Uni.I18n.translatePlural('general.registerConfigurations', deviceConfiguration.get('registerCount'), 'MDC',
                                'No register configurations', '1 register configuration', '{0} register configurations')
                        );

                        logBookLink.setHref('#/administration/devicetypes/' + encodeURIComponent(me.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(deviceConfigurationId) + '/logbookconfigurations');
                        logBookLink.setText(
                            Uni.I18n.translatePlural('general.logbookConfigurations', deviceConfiguration.get('logBookCount'), 'MDC',
                                'No logbook configurations', '1 logbook configuration', '{0} logbook configurations')
                        );

                        loadProfilesLink.setHref('#/administration/devicetypes/' + encodeURIComponent(me.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(deviceConfigurationId) + '/loadprofiles');
                        loadProfilesLink.setText(
                            Uni.I18n.translatePlural('general.loadProfileConfigurations', deviceConfiguration.get('loadProfileCount'), 'MDC',
                                'No load profile configurations', '1 load profile configuration', '{0} load profile configurations')
                        );

                        widget.down('form').loadRecord(deviceConfiguration);
                        var menuItem = widget.down('#device-configuration-action-menu');
                        if (menuItem)
                            menuItem.record = deviceConfiguration;

                        Ext.resumeLayouts(true);

                        widget.setLoading(false);
                    }
                });
            }
        });
    },

    createDeviceConfigurationHistory: function () {
        location.href = '#/administration/devicetypes/' + encodeURIComponent(this.deviceTypeId) + '/deviceconfigurations/add';
    },

    editDeviceConfigurationHistory: function (record) {
        location.href = '#/administration/devicetypes/' + encodeURIComponent(this.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(record.get('id')) + '/edit';
    },

    editDeviceConfigurationHistoryFromPreview: function () {
        location.href = '#/administration/devicetypes/' + encodeURIComponent(this.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(this.getDeviceConfigurationsGrid().getSelectionModel().getSelection()[0].get('id')) + '/edit';
    },

    editDeviceConfigurationFromDetailsHistory: function () {
        this.editDeviceConfigurationHistory(this.getDeviceConfigurationDetailForm().getRecord());
    },

    cloneDeviceConfigurationHistory: function (record) {
        location.href = '#/administration/devicetypes/' + encodeURIComponent(this.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(record.get('id')) + '/clone';
    },

    cloneDeviceConfigurationHistoryFromPreview: function () {
        location.href = '#/administration/devicetypes/' + encodeURIComponent(this.deviceTypeId) + '/deviceconfigurations/' + encodeURIComponent(this.getDeviceConfigurationsGrid().getSelectionModel().getSelection()[0].get('id')) + '/clone';
    },

    cloneDeviceConfigurationFromDetailsHistory: function () {
        this.cloneDeviceConfigurationHistory(this.getDeviceConfigurationDetailForm().getRecord());
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

            var recordId = record.get('id');
            var deviceTypeId = router.arguments['deviceTypeId'];
            Ext.Ajax.request({
                url: '/api/dtc/devicetypes/' + deviceTypeId + '/deviceconfigurations/' + recordId + '/status',
                method: 'PUT',
                jsonData: {active: activeChange},
                success: function () {
                    var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                    model.getProxy().setExtraParam('deviceType', deviceTypeId);

                    model.load(recordId, {
                        callback: function (model) {
                            if (grid) {
                                record.set('active', activeChange);
                                record.commit();
                                gridView.refresh();
                                me.getPreviewActionMenu().record = model;
                            } else {
                                menu.record = model;
                            }

                            form.loadRecord(model);
                            me.getApplication().fireEvent('acknowledge', activeChange ? Uni.I18n.translate('deviceconfiguration.activated', 'MDC', 'Device configuration activated') :
                                Uni.I18n.translate('deviceconfiguration.deactivated', 'MDC', 'Device configuration deactivated'));

                        }
                    });
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
            title: Uni.I18n.translate('general.remove', 'MDC', 'Remove') + " '" + Ext.String.htmlEncode(deviceConfigurationToDelete.get('name')) + "'?",
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
            returnLink: '#/administration/devicetypes/' + encodeURIComponent(this.deviceTypeId) + '/deviceconfigurations'
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
            router = me.getController('Uni.controller.history.Router'),
            previousPath = me.getController('Uni.controller.history.EventBus').getPreviousPath(),
            widget,
            returnLink;

        me.deviceTypeId = deviceTypeId;
        model.getProxy().setExtraParam('deviceType', this.deviceTypeId);


        if (!previousPath || previousPath.indexOf(deviceConfigurationId.toString())==-1) {
            returnLink = router.getRoute('administration/devicetypes/view/deviceconfigurations').buildUrl({deviceTypeId: deviceTypeId});
        } else {
            returnLink = '#' + previousPath;
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

    showDeviceConfigurationCloneView: function (deviceTypeId, deviceConfigurationId) {
        var me = this, widget,
            model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration'),
            router = me.getController('Uni.controller.history.Router'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0];

        me.deviceTypeId = deviceTypeId;
        me.deviceConfigurationId = deviceConfigurationId;
        model.getProxy().setExtraParam('deviceType', this.deviceTypeId);

        pageMainContent.setLoading(true);

        model.load(deviceConfigurationId, {
            success: function (deviceConfiguration) {
                me.getApplication().fireEvent('loadDeviceConfiguration', deviceConfiguration);
                me.deviceConfigurationVersion = deviceConfiguration.get('version');
                Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
                    success: function (deviceType) {
                        widget = Ext.widget('deviceConfigurationClone', {
                            router: router,
                            deviceConfigurationName: deviceConfiguration.get('name')
                        });
                        me.getApplication().fireEvent('loadDeviceType', deviceType);
                        me.getApplication().fireEvent('changecontentevent', widget);
                        pageMainContent.setLoading(false);
                    }
                });

            }
        });
    },

    cloneDeviceConfiguration: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            pageMainContent = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            name = me.getDeviceConfigurationCloneForm().getValues(),
            record = Ext.create(Mdc.model.DeviceConfiguration);

        me.getDeviceConfigurationCloneForm().getForm().clearInvalid();
        record.set('version', me.deviceConfigurationVersion);
        record.set(name);
        pageMainContent.setLoading(true);

        record.save({
            url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId,
            success: function () {
                router.getRoute('administration/devicetypes/view/deviceconfigurations').forward();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('deviceconfiguration.acknowledgment.cloned', 'MDC', 'Device configuration cloned'));
            },
            failure: function (record, operation) {
                var json = Ext.decode(operation.response.responseText);
                if (json && json.errors) {
                    me.getDeviceConfigurationCloneForm().getForm().markInvalid(json.errors);
                }
            },
            callback: function () {
                pageMainContent.setLoading(false);
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
                    window.location.href = me.getDeviceConfigurationEdit().returnLink;
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
                    numberOfLogbooksLabel.setText(
                        Uni.I18n.translatePlural('general.nrOfLogbookConfigurations.selected', 0, 'MDC',
                            'No logbook configurations selected', '{0} logbook configuration selected', '{0} logbook configurations selected')
                    );
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

