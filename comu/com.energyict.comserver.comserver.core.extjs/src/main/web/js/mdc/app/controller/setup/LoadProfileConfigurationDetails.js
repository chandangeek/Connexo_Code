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
            },
            'menu menuitem[action=editloadprofileconfigurationdetailchannel]': {
                click: this.editRecord
            },
            'menu menuitem[action=deleteloadprofileconfigurationdetailchannel]': {
                click: this.showConfirmationPanel
            },
            'button[action=removeloadprofileconfigurationdetailchannelconfirm]': {
                click: this.deleteRecord
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

    editRecord: function () {
        var grid = this.getChannelsGrid(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();
        window.location.href = '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/loadprofiles/' + this.loadProfileConfigurationId + '/channels/' + lastSelected.getData().id + '/edit';
    },

    showConfirmationPanel: function () {
        var grid = this.getChannelsGrid(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected();
        Ext.widget('messagebox', {
            buttons: [
                {
                    xtype: 'button',
                    text: 'Remove',
                    action: 'removeloadprofileconfigurationdetailchannelconfirm',
                    name: 'delete',
                    ui: 'remove'
                },
                {
                    xtype: 'button',
                    text: 'Cancel',
                    handler: function (button, event) {
                        this.up('messagebox').hide();
                    },
                    ui: 'link'
                }
            ]
        }).show({
                title: 'Remove ' + lastSelected.getData().name + '?',
                msg: 'This channel will be removed from load profile configuration',
                icon: Ext.MessageBox.WARNING
            })
    },

    deleteRecord: function (btn) {
        var grid = this.getChannelsGrid(),
            lastSelected = grid.getView().getSelectionModel().getLastSelected(),
            me = this;
        btn.up('messagebox').hide();
        Ext.Ajax.request({
            url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/loadprofileconfigurations/' + me.loadProfileConfigurationId + '/channels/' + lastSelected.getData().id,
            method: 'DELETE',
            waitMsg: 'Removing...',
            success: function () {
                me.handleSuccessRequest('Channel was removed successfully');
                me.store.load();
            },
            failure: function (result, request) {
                me.handleFailureRequest(result, "Error during removing of channel", 'removeloadprofileconfigurationdetailchannelconfirm');
            }
        });
    },


    changeDisplayedObisCodeAndCIM: function (combobox) {
        var record = this.availableMeasurementTypesStore.findRecord('id', combobox.getValue());
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

    onSubmit: function (btn) {
        var me = this,
            formPanel = me.getChannelForm(),
            form = formPanel.getForm(),
            formErrorsPanel = formPanel.down('panel[name=errors]'),
            formValue = form.getValues(),
            preloader,
            jsonValues;
        formValue.measurementType = {id: formValue.measurementType };
        formValue.unitOfMeasure = {id: formValue.unitOfMeasure };
        if (form.isValid()) {
            jsonValues = Ext.JSON.encode(formValue);
            formErrorsPanel.hide();
            switch (btn.action) {
                case 'Add':
                    preloader = Ext.create('Ext.LoadMask', {
                        msg: "Adding channel",
                        target: formPanel
                    });
                    preloader.show();
                    Ext.Ajax.request({
                        url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/loadprofileconfigurations/' + me.loadProfileConfigurationId + '/channels',
                        method: 'POST',
                        jsonData: jsonValues,
                        success: function () {
                            me.handleSuccessRequest('Successfully created');
                        },
                        failure: function (response) {
                            me.handleFailureRequest(response, 'Error during create', 'channelnotificationerrorretry');
                        },
                        callback: function () {
                            preloader.destroy();
                        }
                    });
                    break;
                case 'Save':
                    preloader = Ext.create('Ext.LoadMask', {
                        msg: "Editing channel",
                        target: formPanel
                    });
                    preloader.show();
                    Ext.Ajax.request({
                        url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/loadprofileconfigurations/' + me.loadProfileConfigurationId + '/channels/' + me.channelId,
                        method: 'PUT',
                        jsonData: jsonValues,
                        success: function () {
                            me.handleSuccessRequest('Successfully updated');
                        },
                        failure: function (response) {
                            me.handleFailureRequest(response, 'Error during update', 'channelnotificationerrorretry');
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
                    href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/loadprofiles/' + me.loadProfileConfigurationId + '/channels',
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
                            url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/loadprofileconfigurations/' + me.loadProfileConfigurationId,
                            params: {},
                            method: 'GET',
                            success: function (response) {
                                var loadProfileConfiguration = Ext.JSON.decode(response.responseText).data[0],
                                    widget = Ext.widget('loadProfileConfigurationDetailSetup', {intervalStore: me.intervalStore, deviceTypeId: deviceTypeId, deviceConfigId: deviceConfigurationId, loadProfileConfigurationId: loadProfileConfigurationId });
                                widget.down('#loadProfileConfigurationDetailTitle').html = '<h1>' + loadProfileConfiguration.name + '</h1>';
                                widget.down('#loadProfileConfigurationDetailChannelConfigurationTitle').html = '<h3>' + Uni.I18n.translate('loadprofileconfiguration.loadprofilechannelconfiguation', 'MDC', 'Channel configurations') + '</h3>';
                                me.deviceTypeName = deviceType.get('name');
                                me.deviceConfigName = deviceConfig.get('name');
                                me.getApplication().fireEvent('changecontentevent', widget);
                                var detailedForm = me.getLoadConfigurationDetailForm();
                                detailedForm.getForm().setValues(loadProfileConfiguration);
                                detailedForm.down('[name=deviceConfigurationName]').setValue(Ext.String.format('<a href="#/administration/devicetypes/{0}/deviceconfigurations/{1}">{2}</a>', deviceTypeId, deviceConfigurationId, me.deviceConfigName));
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
                            url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/loadprofileconfigurations/' + me.loadProfileConfigurationId,
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
                                me.getApplication().fireEvent('changecontentevent', widget);
                            }
                        });
                    }
                });
            }
        });
    },

    showDeviceConfigurationLoadProfilesConfigurationChannelsEditView: function (deviceTypeId, deviceConfigurationId, loadProfileConfigurationId, channelId) {
        var me = this;
        me.deviceTypeId = deviceTypeId;
        me.deviceConfigurationId = deviceConfigurationId;
        me.loadProfileConfigurationId = loadProfileConfigurationId;
        me.channelId = channelId;
        me.availableMeasurementTypesStore.getProxy().extraParams = ({deviceType: deviceTypeId, deviceConfig: deviceConfigurationId, loadProfileConfiguration: loadProfileConfigurationId });
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                var model = Ext.ModelManager.getModel('Mdc.model.DeviceConfiguration');
                model.getProxy().setExtraParam('deviceType', deviceTypeId);
                model.load(deviceConfigurationId, {
                    success: function (deviceConfig) {
                        Ext.Ajax.request({
                            url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/loadprofileconfigurations/' + me.loadProfileConfigurationId,
                            params: {},
                            method: 'GET',
                            success: function (response) {
                                var loadProfileConfiguration = Ext.JSON.decode(response.responseText).data[0];
                                Ext.Ajax.request({
                                    url: '/api/dtc/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/loadprofileconfigurations/' + me.loadProfileConfigurationId + '/channels/' + me.channelId ,
                                    params: {},
                                    method: 'GET',
                                    success: function (response) {
                                        var channel = Ext.JSON.decode(response.responseText).data[0],
                                            widget = Ext.widget('loadProfileConfigurationDetailForm', {loadProfileConfigurationChannelHeader: Uni.I18n.translate('loadprofiles.loadprofileEditChannelConfiguration', 'MDC', 'Edit channel Configuration'), loadProfileConfigurationChannelAction: 'Save'}),
                                            measurementTypeCombobox = widget.down('combobox[name=measurementType]'),
                                            cimdisplayfield = widget.down('displayfield[name=cimreadingtype]'),
                                            obiscodedisplayfield = widget.down('displayfield[name=obiscode]'),
                                            unitOfMeasureCombobox = widget.down('combobox[name=unitOfMeasure]'),
                                            overruledObisField =  widget.down('textfield[name=overruledObisCode]'),
                                            overflowValueField =  widget.down('textfield[name=overflowValue]'),
                                            multiplierField =  widget.down('textfield[name=multiplier]');

                                        me.availableMeasurementTypesStore.load({callback: function () {
                                            measurementTypeCombobox.store = me.availableMeasurementTypesStore;
                                            me.availableMeasurementTypesStore.add(channel.measurementType);
                                            measurementTypeCombobox.setValue(channel.measurementType.id);
                                            cimdisplayfield.setValue(channel.measurementType.readingType.mrid);
                                            obiscodedisplayfield.setValue(channel.measurementType.obisCode);
                                        }});
                                        me.phenomenasStore.load({callback: function () {
                                            unitOfMeasureCombobox.store = me.phenomenasStore;
                                            unitOfMeasureCombobox.setValue(channel.unitOfMeasure.id);
                                        }});
                                        if (channel.isLinkedByActiveDeviceConfiguration) {
                                            measurementTypeCombobox.disable();
                                        }
                                        overruledObisField.setValue(channel.overruledObisCode);
                                        overflowValueField.setValue(channel.overflowValue);
                                        multiplierField.setValue(channel.multiplier);

                                        me.deviceTypeName = deviceType.get('name');
                                        me.deviceConfigName = deviceConfig.get('name');
                                        me.getApplication().fireEvent('changecontentevent', widget);
                                    }
                                });
                            }

                        });
                    }
                });
            }
        });
    },
});