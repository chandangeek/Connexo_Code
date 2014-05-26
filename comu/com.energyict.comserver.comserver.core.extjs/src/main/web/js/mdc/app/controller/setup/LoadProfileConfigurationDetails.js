Ext.define('Mdc.controller.setup.LoadProfileConfigurationDetails', {
    extend: 'Ext.app.Controller',

    views: [
        'setup.register.ReadingTypeDetails',
        'setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailSetup',
        'setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailInfo',
        'setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailDockedItems',
        'setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailChannelGrid',
        'setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailChannelPreview',
        'setup.loadprofileconfigurationdetail.LoadProfileConfigurationDetailForm'
    ],

    stores: [
        'Mdc.store.Intervals',
        'Mdc.store.Phenomenas',
        'Mdc.store.LoadProfileConfigurationDetailChannels',
        'Mdc.store.MeasurementTypesOnLoadProfileConfiguration'
    ],

    refs: [
        {ref: 'breadCrumbs', selector: 'breadcrumbTrail'},
        {ref: 'loadConfigurationDetailForm', selector: '#loadProfileConfigurationDetailInfo'},
        {ref: 'loadProfileConfigurationChannelDetailsForm', selector: '#loadProfileConfigurationChannelDetailsForm'},
        {ref: 'loadProfileDetailChannelPreview', selector: '#loadProfileConfigurationDetailChannelPreview'},
        {ref: 'channelForm', selector: '#loadProfileConfigurationDetailChannelFormId'},
        {ref: 'readingTypeDetailsForm', selector: '#readingTypeDetailsForm'},
        {ref: 'channelsGrid', selector: '#loadProfileConfigurationDetailChannelGrid'}

    ],

    deviceTypeId: null,
    deviceConfigurationId: null,

    init: function () {
        this.control({
            'loadProfileConfigurationDetailSetup': {
                afterrender: this.loadStore
            },
            'loadProfileConfigurationDetailSetup loadProfileConfigurationDetailChannelGrid': {
                itemclick: this.loadGridItemDetail
            },
            '#channelsReadingTypeBtn': {
                showReadingTypeInfo: this.showReadingType
            },
            'loadProfileConfigurationDetailForm combobox[name=measurementType]': {
                select: this.changeDisplayedObisCodeAndCIM
            },
            'loadProfileConfigurationDetailForm button[name=loadprofilechannelaction]': {
                click: this.onSubmit
            },
            'button[action=channelnotificationerrorretry]': {
                click: this.retrySubmit
            }
        });


        this.listen({
            store: {
                '#LoadProfileConfigurationDetailChannels': {
                    load: this.selectFirstChannel
                }
            }
        });

        this.intervalStore = this.getStore('Intervals');
        this.store = this.getStore('LoadProfileConfigurationDetailChannels');
        this.phenomenasStore = this.getStore('Phenomenas');
        this.availableMeasurementTypesStore = this.getStore('MeasurementTypesOnLoadProfileConfiguration');
    },

    changeDisplayedObisCodeAndCIM: function(combobox) {
        var record = this.availableMeasurementTypesStore.findRecord('id', combobox.getValue() );
        combobox.next().setValue(record.getData().readingType.mrid);
        combobox.next().next().setValue(record.getData().obisCode);
    },


    retrySubmit: function (btn) {
        var formPanel = this.getChannelForm();
        btn.up('messagebox').hide();
        if (!Ext.isEmpty(formPanel)) {
            var submitBtn = formPanel.down('button[name=loadprofilechannelaction]');
            if (!Ext.isEmpty(submitBtn)) {
                this.onSubmit(submitBtn);
            }
        }
    },

    onSubmit: function(btn) {
        var me = this,
            formPanel = me.getChannelForm(),
            form = formPanel.getForm(),
            formErrorsPanel = formPanel.down('panel[name=errors]'),
            formValue = form.getValues(),
            preloader,
            jsonValues;
        formValue.measurementType = {id: formValue.measurementType };
        formValue.unitOfMeasure = {id: formValue.unitOfMeasure };
        console.log(formValue);
        if (form.isValid()) {
            jsonValues = Ext.JSON.encode(formValue);
            formErrorsPanel.hide();
            switch (btn.text) {
                case 'Add':
                    preloader = Ext.create('Ext.LoadMask', {
                        msg: "Adding channel",
                        target: formPanel
                    });
                    preloader.show();
                    Ext.Ajax.request({
                        url: '/api/dtc/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/loadprofileconfigurations/' + this.loadProfileConfigurationId + '/channels' ,
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
        window.location.href = '#/administration/loadprofiletypes';
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
                    action: 'channelnotificationerrorretry',
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


    selectFirstChannel: function () {
        var grid = this.getChannelsGrid();
        if (!Ext.isEmpty(grid)) {
            var gridView = grid.getView(),
                selectionModel = gridView.getSelectionModel(),
                channelsCount = this.store.getCount();

            if (channelsCount > 0) {
                selectionModel.select(0);
                grid.fireEvent('itemclick', gridView, selectionModel.getLastSelected());
            }
        }
    },

    loadStore: function () {
        this.store.load({
            params: {
                sort: 'name'
            }
        });
    },

    loadGridItemDetail: function (grid, record) {
        var form = this.getLoadProfileConfigurationChannelDetailsForm(),
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
        this.getLoadProfileDetailChannelPreview().setTitle(recordData.name);
        form.loadRecord(record);
        preloader.destroy();
    },


    showReadingType: function (record) {
        var widget = Ext.widget('readingTypeDetails');
        this.getReadingTypeDetailsForm().loadRecord(record.getReadingType());
        widget.show();
    },

    showDeviceConfigurationLoadProfilesConfigurationDetailsView: function (deviceTypeId, deviceConfigurationId, loadProfileConfigurationId) {
        var me = this;
        me.deviceTypeId = deviceTypeId;
        me.deviceConfigurationId = deviceConfigurationId;
        me.loadProfileConfigurationId = loadProfileConfigurationId;
        me.store.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigurationId, loadProfileConfiguration: loadProfileConfigurationId });
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigurationId, {
                    success: function (deviceConfig) {
                        Ext.Ajax.request({
                            url: '/api/dtc/devicetypes/' + me.deviceTypeId +'/deviceconfigurations/' + me.deviceConfigurationId  + '/loadprofileconfigurations/' + me.loadProfileConfigurationId ,
                            params: {},
                            method: 'GET',
                            success: function (response) {
                                var loadProfileConfiguration = Ext.JSON.decode(response.responseText).data[0],
                                    widget = Ext.widget('loadProfileConfigurationDetailSetup', {intervalStore: me.intervalStore, deviceTypeId: deviceTypeId, deviceConfigId: deviceConfigurationId, loadProfileConfigurationId: loadProfileConfigurationId });
                                widget.down('#loadProfileConfigurationDetailTitle').html = '<h1>' + loadProfileConfiguration.name + '</h1>';
                                widget.down('#loadProfileConfigurationDetailChannelConfigurationTitle').html = '<h3>' + Uni.I18n.translate('loadprofileconfiguration.loadprofilechannelconfiguation', 'MDC', 'Channel configurations') + '</h3>';
                                me.deviceTypeName = deviceType.get('name');
                                me.deviceConfigName = deviceConfig.get('name');
                                me.getApplication().getController('Mdc.controller.Main').showContent(widget);
                                var detailedForm = me.getLoadConfigurationDetailForm();
                                detailedForm.getForm().setValues(loadProfileConfiguration);
                                detailedForm.down('[name=deviceConfigurationName]').setValue(Ext.String.format('<a href="#/administration/devicetypes/{0}/deviceconfigurations/{1}">{2}</a>', deviceTypeId, deviceConfigurationId, me.deviceConfigName));
                                me.overviewBreadCrumbs(deviceTypeId, deviceConfigurationId, loadProfileConfigurationId,  me.deviceTypeName, me.deviceConfigName, loadProfileConfiguration.name, null);
                            }
                        });


                    }
                });
            }
        });
    },

    showDeviceConfigurationLoadProfilesConfigurationChannelsAddView: function (deviceTypeId, deviceConfigurationId, loadProfileConfigurationId) {
        var me = this;
        me.deviceTypeId = deviceTypeId;
        me.deviceConfigurationId = deviceConfigurationId;
        me.loadProfileConfigurationId = loadProfileConfigurationId;
        me.availableMeasurementTypesStore.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigurationId, loadProfileConfiguration: loadProfileConfigurationId });
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigurationId, {
                    success: function (deviceConfig) {
                        Ext.Ajax.request({
                            url: '/api/dtc/devicetypes/' + me.deviceTypeId +'/deviceconfigurations/' + me.deviceConfigurationId  + '/loadprofileconfigurations/' + me.loadProfileConfigurationId ,
                            params: {},
                            method: 'GET',
                            success: function (response) {
                                var loadProfileConfiguration = Ext.JSON.decode(response.responseText).data[0],
                                    widget = Ext.widget('loadProfileConfigurationDetailForm', {loadProfileConfigurationChannelHeader: Uni.I18n.translate('loadprofiles.loadporfileaddChannelConfiguration', 'MDC', 'Add channel Configuration'), loadProfileConfigurationChannelAction: 'Add'}),
                                    measurementTypeCombobox = widget.down('combobox[name=measurementType]'),
                                    unitOfMeasureCombobox = widget.down('combobox[name=unitOfMeasure]');
                                me.availableMeasurementTypesStore.load();
                                me.phenomenasStore.load();
                                measurementTypeCombobox.store = me.availableMeasurementTypesStore;
                                unitOfMeasureCombobox.store = me.phenomenasStore;
                                me.deviceTypeName = deviceType.get('name');
                                me.deviceConfigName = deviceConfig.get('name');
                                me.getApplication().getController('Mdc.controller.Main').showContent(widget);
//                                var detailedForm = me.getLoadConfigurationDetailForm();
//                                detailedForm.getForm().setValues(loadProfileConfiguration);
//                                detailedForm.down('[name=deviceConfigurationName]').setValue(Ext.String.format('<a href="#/administration/devicetypes/{0}/deviceconfigurations/{1}">{2}</a>', deviceTypeId, deviceConfigurationId, me.deviceConfigName));
                                me.overviewBreadCrumbs(deviceTypeId, deviceConfigurationId, loadProfileConfigurationId,  me.deviceTypeName, me.deviceConfigName, loadProfileConfiguration.name, Uni.I18n.translate('loadprofiles.loadporfileaddChannelConfiguration', 'MDC', 'Add channel Configuration'));
                            }
                        });


                    }
                });
            }
        });
    },

    overviewBreadCrumbs: function (deviceTypeId, deviceConfigId, loadProfileConfigurationId, deviceTypeName, deviceConfigName, loadProfileConfigurationName, action) {
        var me = this;

        var breadcrumbLoadProfileConfigurationConfig = Ext.create('Uni.model.BreadcrumbItem', {
            text: loadProfileConfigurationName,
            href: loadProfileConfigurationId
        });

        var breadcrumbLoadProfileConfigurations = Ext.create('Uni.model.BreadcrumbItem', {
            text: ('loadprofiles.loadporfiles', 'MDC', 'Load profiles'),
            href: 'loadprofiles'
        });

        var breadcrumbDeviceConfig = Ext.create('Uni.model.BreadcrumbItem', {
            text: deviceConfigName,
            href: deviceConfigId
        });

        var breadcrumbDeviceConfigs = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registerConfig.deviceConfigs', 'MDC', 'Device configurations'),
            href: 'deviceconfigurations'
        });

        var breadcrumbDevicetype = Ext.create('Uni.model.BreadcrumbItem', {
            text: deviceTypeName,
            href: deviceTypeId
        });

        var breadcrumbDeviceTypes = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registerConfig.deviceTypes', 'MDC', 'Device types'),
            href: 'devicetypes'
        });
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#/administration'
        });

        if (Ext.isEmpty(action)) {
            breadcrumbParent.setChild(breadcrumbDeviceTypes).setChild(breadcrumbDevicetype).setChild(breadcrumbDeviceConfigs).setChild(breadcrumbDeviceConfig).setChild(breadcrumbLoadProfileConfigurations).setChild(breadcrumbLoadProfileConfigurationConfig);
        } else {
            var breadaction = Ext.create('Uni.model.BreadcrumbItem', {
                text: action,
                href: ''
            });
            breadcrumbParent.setChild(breadcrumbDeviceTypes).setChild(breadcrumbDevicetype).setChild(breadcrumbDeviceConfigs).setChild(breadcrumbDeviceConfig).setChild(breadcrumbLoadProfileConfigurations).setChild(breadcrumbLoadProfileConfigurationConfig).setChild(breadaction);
        }

        me.getBreadCrumbs().setBreadcrumbItem(breadcrumbParent);
    }

});