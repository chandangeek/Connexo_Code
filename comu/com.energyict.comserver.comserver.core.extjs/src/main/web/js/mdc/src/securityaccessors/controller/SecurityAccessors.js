/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.controller.SecurityAccessors', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.securityaccessors.view.Setup',
        'Mdc.securityaccessors.view.AddEditSecurityAccessor',
        'Uni.view.window.Confirmation',
        'Mdc.securityaccessors.view.SecurityAccessorsPrivilegesEditWindow',
        'Mdc.securityaccessors.view.SecurityAcessorsSetDefaultKeyValue',
        'Mdc.securityaccessors.view.AddSecurityAccessorToDeviceType',
        'Mdc.securityaccessors.view.EditSecurityAccessorKeyRenewal'
    ],

    stores: [
        'Mdc.securityaccessors.store.SecurityAccessors',
        'Mdc.securityaccessors.store.TrustStores',
        'Mdc.store.TimeUnitsYearsSeconds',
        'Mdc.securityaccessors.store.UnusedSecurityAccessors',
        'Mdc.securityaccessors.store.SecurityAccessorsOnDeviceType',
        'Mdc.securityaccessors.store.HsmJssKeyTypes',
        'Mdc.securityaccessors.store.HSMLabelEndPoint',
        'Mdc.securityaccessors.store.HsmCapabilities',
        'Mdc.securityaccessors.store.SecurityCategoryCommands',
        'Mdc.securityaccessors.store.WrappingSecurityAccessors',
        'Mdc.securityaccessors.store.KeyPurposes'
    ],

    models: [
        'Mdc.model.DeviceType',
        'Mdc.securityaccessors.model.SecurityAccessor',
        'Mdc.securityaccessors.model.SecurityPreviewProperties',
        'Mdc.securityaccessors.model.SecurityAccessorsOnDeviceType'
    ],

    refs: [
        {
            ref: 'securityAccessorsGrid',
            selector: '#mdc-security-accessors-grid'
        },
        {
            ref: 'previewForm',
            selector: '#mdc-devicetype-security-accessors-preview-form'
        },
        {
            ref: 'preview',
            selector: 'security-accessors-preview'
        },
        {
            ref: 'addEditForm',
            selector: 'security-accessor-add-form form'
        },
        {
            ref: 'activePassiveCertContainer',
            selector: 'security-accessor-add-form #mdc-active-passive-certificates'
        },
        {
            ref: 'manageCentrallyCheckbox',
            selector: 'security-accessor-add-form #mdc-security-accessor-manage-centrally-checkbox'
        },
        {
            ref: 'securityAccessorPrivilegesEditWindow',
            selector: 'security-accessors-privileges-edit-window'
        },
        {
            ref: 'securityAccessorSetDefaultKeyWindow',
            selector: 'security-accessors-set-default-key-window'
        },
        {
            ref: 'availableSecurityAccessorsGrid',
            selector: 'security-accessor-add-to-device-type-form #available-security-accessors-grd'
        },
        {
            ref: 'securityAccessorsActionMenu',
            selector: 'security-accessors-action-menu'
        },
        {
            ref: 'previewPropertiesHeader',
            selector: '#mdc-devicetype-security-accessors-preview-form form #previewPropertiesHeader'
        },
        {
            ref: 'previewPropertiesPanel',
            selector: '#mdc-devicetype-security-accessors-preview-form form container #previewPropertiesPanel'
        },
        {
            ref: 'previewPropertiesForm',
            selector: '#mdc-devicetype-security-accessors-preview-form #previewPropertiesPanel property-form'
        },
        {
            ref: 'serviceKeyPreviewPropertiesHeader',
            selector: '#mdc-devicetype-security-accessors-preview-form #serviceKey-previewPropertiesHeader'
        },
        {
            ref: 'serviceKeyPreviewPropertiesPanel',
            selector: '#mdc-devicetype-security-accessors-preview-form #serviceKeypreviewPropertiesPanel'
        },
        {
            ref: 'serviceKeyPreviewPropertiesForm',
            selector: '#mdc-devicetype-security-accessors-preview-form #serviceKeypreviewPropertiesPanel property-form'
        },
        {
            ref: 'keyRenewalPropertiesForm',
            selector: '#edit-security-accessor-key-renewal #key-renewal-property-form'
        },
        {
            ref: 'serviceKeyRenewalPropertiesForm',
            selector: '#edit-security-accessor-key-renewal #service-key-renewal-property-form'
        },
        {
            ref: 'keyRenewalPage',
            selector: '#edit-security-accessor-key-renewal'
        },
        {
            ref: 'keyRenewalForm',
            selector: '#key-renewal-with-form'
        },
        {
            ref: 'keyWrapperForm',
            selector: '#key-wrapper-with-form'
        },
        {
            ref: 'serviceKeyPreviewNoProperties',
            selector: '#mdc-devicetype-security-accessors-preview-form #serviceKey-previewNoProperties'
        },
        {
            ref: 'previewNoProperties',
            selector: '#mdc-devicetype-security-accessors-preview-form #previewNoProperties'
        },
        {
            ref: 'keyRenewalPropertyHeader',
            selector: '#edit-security-accessor-key-renewal #key-renewal-property-header'
        },
        {
            ref: 'serviceKeyRenewalPropertyHeader',
            selector: '#edit-security-accessor-key-renewal #service-key-renewal-property-header'
        }
    ],

    fromEditForm: false,
    isManageCentrallyChecked: false,
    deviceTypeId: null,
    deviceType: null,
    selectedRecord: undefined,
    recordToSetKey: null,
    defaultKeyValue: "",

    init: function () {
        var me = this;

        me.control({
            'device-type-security-accessors-setup #mdc-security-accessors-grid': {
                select: me.recordSelected
            },
            '#mdc-add-security-accessor': {
                click: me.navigateToAddSecurityAccessor
            },
            '#mdc-add-security-accessor-empty-grid': {
                click: me.navigateToAddSecurityAccessor
            },
            '#mdc-security-accessor-cancel-link[action=cancelAddEditSecurityAccessor]': {
                click: me.navigateToOverviewPage
            },
            '#btn-cancel-add-security-accessors': {
                click: me.navigateToOverviewPage
            },
            '#mdc-security-accessor-add-button': {
                click: me.addSecurityAccessor
            },
            '#btn-add-security-accessors': {
                click: me.addAvailableSecurityAccessorToDeviceType
            },
            'security-accessors-action-menu': {
                click: me.chooseAction
            },
            '#mdc-security-accessor-key-type-combobox': {
                change: me.keyTypeChanged
            },
            '#mdc-security-accessor-purpose-radio': {
                change: me.purposeChanged
            },
            '#mdc-security-accessors-privileges-edit-window-save': {
                click: me.saveSecurityAccessor
            },
            '#mdc-security-accessors-set-default-key-window-save': {
                click: me.saveDefaultKeyValue
            },
            '#mdc-security-accessor-manage-centrally-checkbox': {
                change: me.onManageCentrallyCheck
            },
            '#mdc-security-accessor-trust-store-combobox': {
                change: me.onTrustStoreChange
            },
            '#edit-security-accessor-key-renewal #key-renewal-command-combo': {
                change: me.keyRenewalCommandChanged
            },
            '#edit-security-accessor-key-renewal #service-key-renewal-command-combo': {
                change: me.serviceKeyRenewalCommandChanged
            },
            '#edit-security-accessor-key-renewal button[action=save]': {
                click: this.onSaveKeyRenewal
            }
        });
    },

    navigateToOverviewPage: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        (me.deviceTypeId
            ? router.getRoute('administration/devicetypes/view/securityaccessors', {deviceTypeId: me.deviceTypeId})
            : router.getRoute('administration/securityaccessors')).forward();
    },

    showSecurityAccessorsOverview: function (deviceTypeId) {
        var me = this,
            view,
            store = me.getStore('Mdc.securityaccessors.store.SecurityAccessorsOnDeviceType');
        me.deviceTypeId = deviceTypeId;
        store.getProxy().setUrl(deviceTypeId);
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.deviceType = deviceType;
                view = Ext.widget('device-type-security-accessors-setup', {
                    deviceTypeId: deviceTypeId
                });

                me.getApplication().fireEvent('changecontentevent', view);
                me.reconfigureMenu(deviceType, view);
            }
        });
    },

    showSecurityAccessorsOverviewAdministration: function () {
        var me = this,
            view = Ext.widget('device-type-security-accessors-setup'),
            trustStoresStore = Ext.getStore('Mdc.securityaccessors.store.TrustStores');

        trustStoresStore.load({
            callback: function () {
                me.deviceTypeId = null;
                me.deviceType = null;
                me.getApplication().fireEvent('changecontentevent', view);
            }
        });
    },

    chooseAction: function (menu, item) {
        var me = this;
        switch (item.action) {
            case 'edit':
                me.navigateToEditSecurityAccessor(menu.record);
                break;
            case 'remove':
                me.deviceType ? me.removeSecurityAccessorFromDeviceType(menu.record) : me.removeSecurityAccessor(me.selectedRecord);
                break;
            case 'changePrivileges':
                Ext.widget('security-accessors-privileges-edit-window', {
                    securityAccessorRecord: me.selectedRecord
                }).show();
                break;
            case 'clearPassiveCertificate': {
                me.clearPassive(me.selectedRecord, false);
            }
                break;
            case 'activatePassiveCertificate': {
                me.activatePassiveCertificate(me.selectedRecord);
            }
                break;
            case 'setDefaultKeyValue': {
                Ext.widget('security-accessors-set-default-key-window', {
                    securityAccessorRecord: me.selectedRecord,
                    defaultKeyValueToSet: me.defaultKeyValue
                }).show();
            }
                break;
            case 'configureKeyRenewal': {
                me.goToConfigureKeyRenewal(menu.record);
            }
                break;
        }
    },

    goToConfigureKeyRenewal: function (record) {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('administration/devicetypes/view/securityaccessors/edit').forward({
            deviceTypeId: encodeURIComponent(router.arguments.deviceTypeId),
            securityAccessorId: record.get('id')
        });
    },

    configurekeyrenewal: function (deviceTypeId, securityAccessorId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            view,
            securityCategoryCommandsStore = me.getStore('Mdc.securityaccessors.store.SecurityCategoryCommands'),
            wrappers = me.getStore('Mdc.securityaccessors.store.WrappingSecurityAccessors');

        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.deviceType = deviceType;

                wrappers.getProxy().setUrl(deviceTypeId, securityAccessorId);
                wrappers.load();

                me.getApplication().fireEvent('loadDeviceType', deviceType);
                securityCategoryCommandsStore.getProxy().setUrl(deviceTypeId);

                securityCategoryCommandsStore.load({
                    callback: function (records, operation, success) {
                        var securityAccessorModel = Ext.ModelManager.getModel('Mdc.securityaccessors.model.SecurityAccessorsOnDeviceType');
                        securityAccessorModel.getProxy().setUrl(deviceTypeId);
                        securityAccessorModel.load(securityAccessorId, {
                            success: function (securityAccessorRecord) {
                                var view = Ext.widget('editSecurityAccessorKeyRenewal', {
                                        cancelLink: router.getRoute('administration/devicetypes/view/securityaccessors').buildUrl({deviceTypeId: deviceTypeId}),
                                        deviceType: deviceType,
                                        itemId: 'edit-security-accessor-key-renewal',
                                        securityAccessorRecord: securityAccessorRecord
                                    }),
                                    keyRenewalForm = me.getKeyRenewalForm(),
                                    commandCombo = keyRenewalForm.down('#key-renewal-command-combo'),
                                    noCommand = keyRenewalForm.down('#key-renewal-no-command'),
                                    serviceKeyCommandCombo = keyRenewalForm.down('#service-key-renewal-command-combo'),
                                    serviceKeyNoCommand = keyRenewalForm.down('#service-key-renewal-no-command');

                                keyWrapperForm = me.getKeyWrapperForm(),
                                    wrapperCombo = keyWrapperForm.down('#key-wrapper-combo');

                                wrapperCombo.setVisible(true);
                                wrapperCombo.setValue(securityAccessorRecord.get('wrapperIdAndName').id);

                                records.length == 0 && commandCombo.setVisible(false) && noCommand.setVisible(true);
                                records.length != 0 && commandCombo.setVisible(true) && noCommand.setVisible(false);
                                if (serviceKeyCommandCombo && serviceKeyNoCommand){
                                    serviceKeyCommandCombo.setVisible(records.length) && serviceKeyNoCommand.setVisible(!records.length);
                                }
                                me.getApplication().fireEvent('changecontentevent', view);
                                me.getApplication().fireEvent('configurekeyrenewal', securityAccessorRecord);
                                view.down('#edit-security-accessor-key-renewal-panel').setTitle(Uni.I18n.translate('general.editKeyRenewal', 'MDC', "Edit key renewal for '{0}'", securityAccessorRecord.get('name')));

                                if (securityAccessorRecord.get('keyRenewalCommandSpecification')) {
                                    commandCombo.setValue(securityAccessorRecord.get('keyRenewalCommandSpecification').id);
                                    view.down('#key-renewal-property-form').loadRecord(securityAccessorRecord);
                                    var keyRenewalPropertyHeader = me.getKeyRenewalPropertyHeader();
                                    if (securityAccessorRecord.properties() && (securityAccessorRecord.properties().getCount() > 0)) {
                                        keyRenewalPropertyHeader.show();
                                        keyRenewalPropertyHeader.update('<h3>' + Uni.I18n.translate('securityAccessors.overview.attr', 'MDC', 'Attributes of {0}', securityAccessorRecord.get('keyRenewalCommandSpecification').name) + '</h3>');
                                    } else {
                                        keyRenewalPropertyHeader.hide();
                                    }
                               }else{
                                    var commandComboStore = commandCombo.store;
                                    if (commandComboStore && commandComboStore.getCount() > 0){
                                        commandCombo.setValue(commandComboStore.getAt(0).data.id)
                                    }
                               }
                               var serviceKeyRenewalCommandSpecification = securityAccessorRecord.get('serviceKeyRenewalCommandSpecification');
                               if (serviceKeyRenewalCommandSpecification) {
                                    serviceKeyCommandCombo.setValue(serviceKeyRenewalCommandSpecification.id);
                                    view.down('#service-key-renewal-property-form').loadRecord(securityAccessorRecord);
                                    var serviceKeyRenewalPropertyHeader = me.getServiceKeyRenewalPropertyHeader();
                                    if (securityAccessorRecord.serviceProperties() && (securityAccessorRecord.serviceProperties().getCount() > 0)) {
                                        serviceKeyRenewalPropertyHeader.show();
                                        serviceKeyRenewalPropertyHeader.update('<h3>' + Uni.I18n.translate('securityAccessors.overview.attr', 'MDC', 'Attributes of {0}', serviceKeyRenewalCommandSpecification.name) + '</h3>');
                                    } else {
                                        serviceKeyRenewalPropertyHeader.hide();
                                    }
                               } else {
                                    var serviceKeyCommandComboStore = serviceKeyCommandCombo.store;
                                    if (serviceKeyCommandComboStore && serviceKeyCommandComboStore.getCount() > 0){
                                        serviceKeyCommandCombo.setValue(serviceKeyCommandComboStore.getAt(0).data.id)
                                    }
                               }
                            }
                        });
                    }
                });
            }
        });
    },

    keyRenewalCommandChanged: function (combobox, newValue, oldValue) {
        var me = this,
            propertiesForm = me.getKeyRenewalPropertiesForm(),
            keyRenewalRecord = combobox.findRecordByValue(newValue),
            keyRenewalPropertyHeader = me.getKeyRenewalPropertyHeader();

        if (keyRenewalRecord && keyRenewalRecord.properties() && (keyRenewalRecord.properties().getCount() > 0)) {
            keyRenewalPropertyHeader.show();
            keyRenewalPropertyHeader.update('<h3>' + Uni.I18n.translate('securityAccessors.overview.attr', 'MDC', 'Attributes of {0}', keyRenewalRecord.get('name')) + '</h3>');
        } else {
            keyRenewalPropertyHeader.hide();
        }
        propertiesForm.loadRecord(combobox.findRecordByValue(newValue));
    },

    serviceKeyRenewalCommandChanged: function (combobox, newValue, oldValue) {
        var me = this,
            propertiesForm = me.getServiceKeyRenewalPropertiesForm(),
            keyRenewalRecord = combobox.findRecordByValue(newValue),
            keyRenewalPropertyHeader = me.getServiceKeyRenewalPropertyHeader();

        if (keyRenewalRecord && keyRenewalRecord.properties() && (keyRenewalRecord.properties().getCount() > 0)) {
            keyRenewalPropertyHeader.show();
            keyRenewalPropertyHeader.update('<h3>' + Uni.I18n.translate('securityAccessors.overview.attr', 'MDC', 'Attributes of {0}', keyRenewalRecord.get('name')) + '</h3>');
        } else {
            keyRenewalPropertyHeader.hide();
        }
        propertiesForm.loadRecord(keyRenewalRecord);
    },

    onSaveKeyRenewal: function (button) {
        var me = this,
            keyRenewalPage = me.getKeyRenewalPage(),
            propertiesForm = me.getKeyRenewalPropertiesForm(),
            servicePropertiesForm = me.getServiceKeyRenewalPropertiesForm(),
            keyRenewalForm = me.getKeyRenewalForm(),
            securityAccessorRecord = me.getKeyRenewalPage().securityAccessorRecord;

            if (keyRenewalForm.isValid() && (propertiesForm && propertiesForm.isValid()) && (servicePropertiesForm && servicePropertiesForm.isValid())) {
                propertiesForm.updateRecord(securityAccessorRecord);
                servicePropertiesForm.updateRecord(securityAccessorRecord);
                securityAccessorRecord.beginEdit();
                securityAccessorRecord.set('keyRenewalCommandSpecification', {
                    id: keyRenewalPage.down('#key-renewal-command-combo').getValue()
                });
                securityAccessorRecord.set('serviceKeyRenewalCommandSpecification', {
                    id: keyRenewalPage.down('#service-key-renewal-command-combo').getValue()
                });
                securityAccessorRecord.set('wrapperAccessorId', keyRenewalPage.down('#key-wrapper-combo').getValue());
                securityAccessorRecord.propertiesStore = propertiesForm.getRecord().properties();
                securityAccessorRecord.servicePropertiesStore = servicePropertiesForm.getRecord().properties()
                securityAccessorRecord.endEdit();
                securityAccessorRecord.save({
                    backUrl: keyRenewalPage.backUrl,
                    success: function (record) {
                        location.href = keyRenewalPage.cancelLink;
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('securityAccessor.acknowledgment', 'MDC', 'Security accessor saved'));
                    },
                    failure: function (record, operation) {
                        var json = Ext.decode(operation.response.responseText);
                        if (json && json.errors) {
                            keyRenewalPage.getForm().markInvalid(json.errors);
                            propertiesForm.getForm().markInvalid(json.errors);
                        }
                    }
                });
            } else {
                keyRenewalPage.down('#key-renewal-with-key-renewal-error').show();
            }
    },

    recordSelected: function (grid, recordParam) {
        var me = this,
            gridMenu = me.getSecurityAccessorsGrid().down('uni-actioncolumn').menu,
            previewForm = me.getPreviewForm(),
            processRecord = function (record, defaultKeyValue) {
                me.selectedRecord = record;
                previewForm.doLoadRecord(record, defaultKeyValue, me.deviceTypeId);
                var previewPropertiesPanel = previewForm.down('#previewPropertiesPanel'),
                    previewPropertiesForm = previewForm.down('#previewPropertiesPanel property-form'),
                    previewPropertiesHeader = previewForm.down('#previewPropertiesHeader'),
                    previewNoProperties = previewForm.down('#previewNoProperties'),
                    keyRenewalOverviewPropertiesForm = previewForm.down('#keyRenewalOverviewProperties')
                    serviceKeyPreviewPropertiesPanel = previewForm.down('#serviceKey-previewPropertiesPanel'),
                    serviceKeyPreviewPropertiesForm = previewForm.down('#serviceKey-previewPropertiesPanel property-form'),
                    serviceKeyPreviewPropertiesHeader = previewForm.down('#serviceKey-previewPropertiesHeader'),
                    serviceKeyPreviewNoProperties = previewForm.down('#serviceKey-previewNoProperties'),
                    serviceKeyRenewalOverviewPropertiesForm = previewForm.down('#serviceKeyRenewalOverviewProperties');

                var keyRenewalCommandSpecification = recordParam.get('keyRenewalCommandSpecification');
                if (keyRenewalCommandSpecification && keyRenewalCommandSpecification.name) {
                    previewForm.down('#previewPropertiesCommandName').setValue(keyRenewalCommandSpecification.name);
                } else {
                    previewForm.down('#previewPropertiesCommandName').setVisible(false);
                }
                if (!Ext.isEmpty(keyRenewalCommandSpecification) && keyRenewalCommandSpecification.id !== "NOT_SET") {
                    keyRenewalOverviewPropertiesForm.show();
                    previewPropertiesHeader.update('<h3>' + Uni.I18n.translate('securityAccessors.overview.attr', 'MDC', 'Attributes of {0}', recordParam.get('keyRenewalCommandSpecification').name) + '</h3>');
                    previewPropertiesHeader.show();
                    if (!Ext.isEmpty(recordParam.get('properties'))) {
                        previewNoProperties.hide();
                        previewPropertiesPanel.show();
                    }
                    else {
                        previewPropertiesPanel.hide();
                        previewNoProperties.show();
                    }
                } else {
                    keyRenewalOverviewPropertiesForm.hide();
                }
                var serviceKeyRenewalCommandSpecification = recordParam.get('serviceKeyRenewalCommandSpecification');
                if (serviceKeyRenewalCommandSpecification && serviceKeyRenewalCommandSpecification.name) {
                    previewForm.down('#previewPropertiesServiceKeyCommandName').setValue(serviceKeyRenewalCommandSpecification.name);
                } else {
                    previewForm.down('#previewPropertiesServiceKeyCommandName').setVisible(false);
                }
                if (!Ext.isEmpty(serviceKeyRenewalCommandSpecification) && serviceKeyRenewalCommandSpecification.id !== "NOT_SET") {
                    serviceKeyRenewalOverviewPropertiesForm.show();
                    serviceKeyPreviewPropertiesHeader.update('<h3>' + Uni.I18n.translate('securityAccessors.overview.attr', 'MDC', 'Attributes of {0}', recordParam.get('serviceKeyRenewalCommandSpecification').name) + '</h3>');
                    serviceKeyPreviewPropertiesHeader.show();
                    if (!Ext.isEmpty(recordParam.get('serviceProperties'))) {
                        serviceKeyPreviewNoProperties.hide();
                        serviceKeyPreviewPropertiesPanel.show();
                    }
                    else {
                        serviceKeyPreviewPropertiesPanel.hide();
                        serviceKeyPreviewNoProperties.show();
                    }
                } else {
                    serviceKeyRenewalOverviewPropertiesForm.hide();
                }
                if (recordParam.get('keyType').isKey && !Ext.isEmpty(recordParam.get('wrapperIdAndName'))) {
                    me.getPreviewForm().down('#previewWrapperName').setValue(recordParam.get('wrapperIdAndName').name);
                } else {
                    me.getPreviewForm().down('#previewWrapperName').setVisible(false);
                }

                previewPropertiesForm.loadRecord(recordParam);
                me.getPreview().setTitle(Ext.htmlEncode(record.get('name')));
                gridMenu.updateMenuItems(record);
                gridMenu.record = record;
                if (me.getPreview().down('security-accessors-action-menu')) {
                    me.getPreview().down('security-accessors-action-menu').updateMenuItems(record);
                    me.getPreview().down('security-accessors-action-menu').record = record;

                }
                me.getPreview().setLoading(false);
                gridMenu.setLoading(false);
            };

        me.getPreview().setLoading();
        gridMenu.setLoading();

        var model = Ext.ModelManager.getModel('Mdc.securityaccessors.model.SecurityAccessor');


        if (recordParam.get('keyType').name == 'HSM Key' && me.deviceTypeId) {
            Ext.Ajax.request({
                url: Ext.String.format('/api/dtc/devicetypes/{0}/securityaccessors/{1}', me.deviceTypeId, recordParam.get('id')),
                method: 'GET',
                success: function (response) {
                    var data = Ext.JSON.decode(response.responseText);
                    me.defaultKeyValue = data.defaultServiceKey;
                    model.load(recordParam.get('id'), {
                        success: function (keyRecord) {
                            recordToSetKey = keyRecord;
                            processRecord(keyRecord, me.defaultKeyValue);
                        }
                    });
                }
            });


        } else {
            model.load(recordParam.get('id'), {
                success: function (keyRecord) {
                    processRecord(keyRecord, '-');
                }
            });
        }


    },

    reconfigureMenu: function (deviceType, view) {
        var me = this;
        view.suspendLayouts();
        view.setLoading(true);
        me.getApplication().fireEvent('loadDeviceType', deviceType);
        if (view.down('deviceTypeSideMenu')) {
            view.down('deviceTypeSideMenu').setDeviceTypeTitle(deviceType.get('name'));
            view.down('deviceTypeSideMenu #conflictingMappingLink').setText(
                Uni.I18n.translate('deviceConflictingMappings.ConflictingMappingCount', 'MDC', 'Conflicting mappings ({0})', deviceType.get('deviceConflictsCount'))
            );
        }
        view.setLoading(false);
        view.resumeLayouts();
    },

    navigateToAddSecurityAccessor: function (button) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        (me.deviceTypeId
            ? router.getRoute('administration/devicetypes/view/securityaccessors/add', {deviceTypeId: me.deviceTypeId})
            : router.getRoute('administration/securityaccessors/add')).forward();
    },

    showAddSecurityAccessorToDeviceType: function (deviceTypeId) {
        var me = this,
            view,
            store = me.getStore('Mdc.securityaccessors.store.UnusedSecurityAccessors');

        me.deviceTypeId = deviceTypeId;
        store.getProxy().setUrl(deviceTypeId);
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                me.deviceType = deviceType;
                me.getApplication().fireEvent('loadDeviceType', deviceType);
                view = Ext.widget('security-accessor-add-to-device-type-form', {deviceTypeId: deviceTypeId});
                me.getApplication().on('changecontentevent', function () {
                    store.on('load', function (store, records, success) {
                        if (records === null || records.length === 0) {
                            var btnAddSecurityAccessors = view && view.down('#btn-add-security-accessors');
                            if (btnAddSecurityAccessors) btnAddSecurityAccessors.hide();
                        }
                    })
                    store.load();
                });
                me.getApplication().fireEvent('changecontentevent', view);
            }
        });

    },

    addAvailableSecurityAccessorToDeviceType: function () {
        var me = this,
            grid = this.getAvailableSecurityAccessorsGrid(),
            securityAccessors = _.map(grid.getSelectionModel().getSelection(), function (accessorToAdd) {
                if (accessorToAdd.get('keyType').name != 'HSM Key') {
                    delete accessorToAdd.data.importCapability;
                    delete accessorToAdd.data.renewCapability;
                    delete accessorToAdd.data.label;
                    delete accessorToAdd.data.keySize;
                    delete accessorToAdd.data.hsmJssKeySize;
                    delete accessorToAdd.data.isReversible;
                }
                delete accessorToAdd.data.keyRenewalCommandSpecification;
                delete accessorToAdd.data.wrapperIdAndName;
                delete accessorToAdd.data.properties;
                return accessorToAdd.getData();
            }),
            jsonData = {
                name: me.deviceType.get('name'),
                version: me.deviceType.get('version'),
                securityAccessors: securityAccessors
            };

        Ext.Ajax.request({
            url: Ext.String.format('/api/dtc/devicetypes/{0}/securityaccessors', me.deviceTypeId),
            method: 'POST',
            jsonData: jsonData,
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('securityaccessors.addSecurityAccessorSuccess', 'MDC', 'Security accessor added'));
                me.getController('Uni.controller.history.Router')
                    .getRoute('administration/devicetypes/view/securityaccessors', {deviceTypeId: me.deviceTypeId})
                    .forward();
            }
        });
    },

    removeSecurityAccessorFromDeviceType: function (record) {
        var me = this,
            store = me.getStore('Mdc.securityaccessors.store.SecurityAccessorsOnDeviceType'),
            confirmationWindow = Ext.create('Uni.view.window.Confirmation'),
            jsonData = {
                name: me.deviceType.get('name'),
                version: me.deviceType.get('version')
            };

        confirmationWindow.show(
            {
                msg: Uni.I18n.translate('deviceType.securityaccessors.remove.msg', 'MDC', 'This security accessor will no longer be available on this device type.'),
                title: Uni.I18n.translate('general.removeX', 'MDC', "Remove '{0}'?", Ext.htmlEncode(record.get('name')), false),
                fn: function (state) {
                    if (state === 'confirm') {
                        Ext.Ajax.suspendEvent('requestexception');
                        Ext.Ajax.request({
                            url: Ext.String.format('/api/dtc/devicetypes/{0}/securityaccessors/{1}', me.deviceTypeId, record.get('id')),
                            method: 'DELETE',
                            jsonData: jsonData,
                            success: function () {
                                me.getSecurityAccessorsGrid().setLoading();
                                me.getApplication().fireEvent('acknowledge',
                                    Uni.I18n.translate('securityaccessors.removeSecurityAccessorSuccess', 'MDC', 'Security accessor removed')
                                );
                                me.getSecurityAccessorsGrid().down('pagingtoolbartop').resetPaging();
                                me.getSecurityAccessorsGrid().down('pagingtoolbarbottom').resetPaging();
                                store.load({
                                    callback: function (records, operation, success) {
                                        me.getSecurityAccessorsGrid().setLoading(false);
                                    }
                                });
                                //  the version of device type is increased, so it is necessary to reloaded it
                                Ext.ModelManager.getModel('Mdc.model.DeviceType').load(me.deviceTypeId, {
                                    success: function (deviceType) {
                                        me.deviceType = deviceType;
                                    }
                                });
                            },
                            failure: function (response) {
                                var message = response.responseText || response.statusText,
                                    decoded = Ext.decode(message, true);
                                if (decoded && decoded.message) {
                                    var title = Uni.I18n.translate('securityaccessors.removeSecurityAccessorFailure', 'MDC', 'Couldn\'t delete security accessor');
                                    me.getApplication().getController('Uni.controller.Error').showError(title, decoded.message, decoded.errorCode);
                                }
                            },
                            callback: function () {
                                Ext.Ajax.resumeEvent('requestexception');
                            }
                        });
                    }
                }
            }
        );
    },

    showAddSecurityAccessor: function () {
        var me = this,
            view,
            unitStore = me.getStore('Mdc.store.TimeUnitsYearsSeconds'),
            keyTypesStore = me.getStore('Mdc.securityaccessors.store.KeyTypes'),
            keyPurposesStore = me.getStore('Mdc.securityaccessors.store.KeyPurposes'),
            trustStoresStore = me.getStore('Mdc.securityaccessors.store.TrustStores');

        me.isManageCentrallyChecked = false;
        keyTypesStore.load();
        keyPurposesStore.load();
        trustStoresStore.load();

        view = Ext.widget('security-accessor-add-form');
        view.down('form').loadRecord(Ext.create('Mdc.securityaccessors.model.SecurityAccessor'));
        unitStore.load({
            callback: function (records, operation, success) {
                if (success && records.length > 0) {
                    view.down('#cbo-security-accessor-validity-period-delay').select(records[0]);
                }
            }
        });

        me.getApplication().fireEvent('changecontentevent', view);

    },

    addSecurityAccessor: function () {
        var me = this,
            form = me.getAddEditForm(),
            errorMessage = form.down('#mdc-security-accessor-error-message'),
            viewport = Ext.ComponentQuery.query('viewport')[0],
            defaultValue = Ext.create('Mdc.securityaccessors.model.SecurityPreviewProperties'),
            activeAliasCombo = me.getActivePassiveCertContainer().down('#mdc-active-alias-combo'),
            passiveAliasCombo = me.getActivePassiveCertContainer().down('#mdc-passive-alias-combo'),
            record;

        me.editRecord = null;

        errorMessage.hide();
        if (!form.isValid()) {
            errorMessage.show();
            return;
        }
        // viewport.setLoading();
        form.updateRecord();
        record = form.getRecord();

        record.beginEdit();
        if (record.get('keyType') && record.get('keyType').requiresDuration) {
            record.set('duration', {
                count: form.down('#num-security-accessor-validity-period').getValue(),
                timeUnit: form.down('#cbo-security-accessor-validity-period-delay').getValue()
            });
        } else {
            record.set('duration', null);
        }
        if (!Ext.isArray(record.get('defaultEditLevels'))) {
            record.set('defaultEditLevels', null);
        }
        if (!Ext.isArray(record.get('defaultViewLevels'))) {
            record.set('defaultViewLevels', null);
        }
        if (!Ext.isArray(record.get('editLevels'))) {
            record.set('editLevels', null);
        }
        if (!Ext.isArray(record.get('viewLevels'))) {
            record.set('viewLevels', null);
        }

        if (me.isManageCentrallyChecked) {
            var defaultValueData = me.defaultPropertiesData;

            if (activeAliasCombo.getValue()) {
                var currentProperties = me.defaultPropertiesData.currentProperties;
                _.map(currentProperties, function (property) {
                    var key = property.key;
                    if (key === 'alias') {
                        property.propertyValueInfo = {
                            value: activeAliasCombo.getValue()
                        };
                    }
                });
                defaultValueData.currentProperties = currentProperties;
            } else {
                defaultValueData.currentProperties = undefined;
            }

            if (passiveAliasCombo.getValue()) {
                var tempProperties = me.defaultPropertiesData.tempProperties;
                _.forEach(tempProperties, function (property) {
                    var key = property.key;
                    if (key === 'alias') {
                        property.propertyValueInfo = {
                            value: passiveAliasCombo.getValue()
                        };
                    }
                });
                defaultValueData.tempProperties = tempProperties;
            } else {
                defaultValueData.tempProperties = undefined;
            }

            record.set('defaultValue', defaultValueData);
        } else {
            record.set('defaultValue', null);
        }
        delete record.data.keyRenewalCommandSpecification;
        delete record.data.wrapperIdAndName;
        delete record.data.properties;
        if (!record.get("keyType").isKey){
            delete record.data.keyPurpose;
        }
        record.endEdit();

        record.save({
            callback: function (record, operation, success) {
                var responseText = Ext.decode(operation.response.responseText, true);
                viewport.setLoading(false);
                if (success) {
                    me.getApplication().fireEvent('acknowledge', operation.action === 'update'
                        ? Uni.I18n.translate('securityaccessors.saveSecurityAccessorSuccess', 'MDC', 'Security accessor saved')
                        : Uni.I18n.translate('securityaccessors.addSecurityAccessorSuccess', 'MDC', 'Security accessor added'));
                    me.navigateToOverviewPage();
                } else if (responseText && responseText.errors) {
                    errorMessage.show();
                    if (Ext.isArray(responseText.errors)) {
                        Ext.Array.each(responseText.errors, function (error) {
                            if (error.id === 'duration') {
                                form.down('#num-security-accessor-validity-period').markInvalid(error.msg);
                            }
                        })
                    }
                    form.getForm().markInvalid(responseText.errors);
                }
            }
        });
    },

    getSecurityAccessorPreviewProperties: function () {
        var me = this,
            form = me.getAddEditForm(),
            errorMessage = form.down('#mdc-security-accessor-error-message'),
            viewport = Ext.ComponentQuery.query('viewport')[0],
            record = me.editRecord || Ext.create('Mdc.securityaccessors.model.SecurityPreviewProperties');

        errorMessage.hide();

        form.updateRecord(record);
        record.beginEdit();
        if (record.get('keyType') && record.get('keyType').requiresDuration) {
            record.set('duration', {
                count: form.down('#num-security-accessor-validity-period').getValue(),
                timeUnit: form.down('#cbo-security-accessor-validity-period-delay').getValue()
            });
        } else {
            record.set('duration', null);
        }
        if (!Ext.isArray(record.get('defaultEditLevels'))) {
            record.set('defaultEditLevels', null);
        }
        if (!Ext.isArray(record.get('defaultViewLevels'))) {
            record.set('defaultViewLevels', null);
        }
        if (!Ext.isArray(record.get('editLevels'))) {
            record.set('editLevels', null);
        }
        if (!Ext.isArray(record.get('viewLevels'))) {
            record.set('viewLevels', null);
        }
        record.endEdit();

        if (me.editRecord && me.editRecord.get('defaultValue')) {
            me.defaultPropertiesData = me.editRecord.get('defaultValue');
            me.loadProperties(me.editRecord.get('defaultValue'), me.editRecord);
        } else {
            viewport.setLoading();
            Ext.Ajax.request({
                url: '/api/dtc/securityaccessors/previewproperties',
                method: 'POST',
                jsonData: record.getRecordData(),
                success: function (operation) {
                    var responseText = Ext.JSON.decode(operation.responseText);
                    viewport.setLoading(false);
                    me.defaultPropertiesData = responseText;
                    me.loadProperties(responseText, record);
                }
            });
        }

    },

    navigateToEditSecurityAccessor: function (record) {
        this.getController('Uni.controller.history.Router')
            .getRoute('administration/securityaccessors/edit').forward({securityAccessorId: record.get('id')});
    },

    showEditSecurityAccessor: function (securityAccessorId) {
        var me = this,
            view,
            timeUnitsStore = me.getStore('Mdc.store.TimeUnitsYearsSeconds'),
            keyTypesStore = me.getStore('Mdc.securityaccessors.store.KeyTypes'),
            keyPurposesStore = me.getStore('Mdc.securityaccessors.store.KeyPurposes'),
            model = Ext.ModelManager.getModel('Mdc.securityaccessors.model.SecurityAccessor'),
            callBackFunction,
            storesToLoad = 3;

        model.load(securityAccessorId, {
            success: function (record) {
                me.editRecord = record;

                me.getApplication().fireEvent('securityaccessorload', record.get('name'));
                callBackFunction = function () {
                    storesToLoad--;
                    if (storesToLoad <= 0) {
                        view = Ext.widget('security-accessor-add-form', {
                            isEdit: true,
                            title: Uni.I18n.translate('general.editX', 'MDC', "Edit '{0}'", record.get('name'), false)
                        });
                        view.down('form').loadRecord(record);
                        if (record.get('keyType').isKey) {
                            view.down('#mdc-security-accessor-key').setValue(true);
                            if (record.get('purpose').id != 'FILE_OPERATIONS') {
                                view.down('#mdc-security-accessor-isWrapper-checkbox').setVisible(true);
                                view.down('#mdc-security-accessor-isWrapper-checkbox').setDisabled(false);
                                view.down('#mdc-security-accessor-isWrapper-checkbox').setValue(record.get('isWrapper'));
                            }
                        } else {
                            view.down('#mdc-security-accessor-certificate').setValue(true);
                        }
                        if (record.get('keyType').name == 'HSM Key') {
                            view.down('#mdc-security-accessor-jss-keytype-combobox').setDisabled(false);
                            view.down('#mdc-security-accessor-jss-keytype-combobox').setRawValue(record.get('hsmJssKeyType'));
                            view.down('#mdc-security-accessor-import-capability-combobox').setDisabled(false);
                            view.down('#mdc-security-accessor-import-capability-combobox').setRawValue(record.get('importCapability'));
                            view.down('#mdc-security-accessor-renew-capability-combobox').setDisabled(false);
                            view.down('#mdc-security-accessor-renew-capability-combobox').setRawValue(record.get('renewCapability'));
                            view.down('#mdc-security-accessor-label-end-point-combobox').setDisabled(false);
                            view.down('#mdc-security-accessor-label-end-point-combobox').setRawValue(record.get('label'));
                            view.down('#mdc-security-accessor-key-size').setDisabled(false);
                            view.down('#mdc-security-accessor-key-size').setValue(record.get('keySize'));
                            view.down('#mdc-security-accessor-isReversible-checkbox').setDisabled(false);
                            view.down('#mdc-security-accessor-isReversible-checkbox').setValue(record.get('isReversible'));

                        }
                        if (record.get('purpose').id === 'FILE_OPERATIONS') {
                            view.down('#mdc-security-accessor-purpose-file-operations').setValue(true);
                        } else {
                            view.down('#mdc-security-accessor-purpose-device-operations').setValue(true);
                        }
                        view.down('#mdc-security-accessor-key-type-combobox').editAccessorRecord = record;
                        view.down('#mdc-security-accessor-key-type-combobox').setValue(record.get('keyType').id);
                        if (record.get('keyPurpose')){
                            view.down('#mdc-security-accessor-key-purpose-combobox').setValue(record.get('keyPurpose').key);
                        }
                        if (record.get('duration')) {
                            view.down('#num-security-accessor-validity-period').setValue(record.get('duration').count);
                            view.down('#cbo-security-accessor-validity-period-delay').select(record.get('duration').timeUnit);
                        }
                        view.down('#mdc-security-accessor-storage-method-combobox').setDisabled(true);
                        me.getApplication().fireEvent('changecontentevent', view);
                        if (record.get('defaultValue')) {
                            me.getManageCentrallyCheckbox().setValue(true);
                        } else {
                            me.getManageCentrallyCheckbox().setValue(false);
                        }
                        me.getManageCentrallyCheckbox().disable();
                    }
                };
                timeUnitsStore.load({callback: callBackFunction});
                keyTypesStore.load({callback: callBackFunction});
                keyPurposesStore.load({callback: callBackFunction});
            }
        });
    },

    removeSecurityAccessor: function (record) {
        var me = this,
            store = me.getStore('Mdc.securityaccessors.store.SecurityAccessors'),
            confirmationWindow = Ext.create('Uni.view.window.Confirmation');

        confirmationWindow.show(
            {
                msg: Uni.I18n.translate('securityaccessors.remove.msg', 'MDC', 'This security accessor will no longer be available.'),
                title: Uni.I18n.translate('general.removeX', 'MDC', "Remove '{0}'?", Ext.htmlEncode(record.get('name')), false),
                fn: function (state) {
                    if (state === 'confirm') {
                        delete record.data.keyRenewalCommandSpecification;
                        delete record.data.wrapperIdAndName;
                        delete record.data.properties;
                        record.destroy({
                            success: function () {
                                me.getSecurityAccessorsGrid().setLoading();
                                me.getApplication().fireEvent('acknowledge',
                                    Uni.I18n.translate('securityaccessors.removeSecurityAccessorSuccess', 'MDC', 'Security accessor removed')
                                );
                                me.getSecurityAccessorsGrid().down('pagingtoolbartop').resetPaging();
                                me.getSecurityAccessorsGrid().down('pagingtoolbarbottom').resetPaging();
                                store.load({
                                    callback: function (records, operation, success) {
                                        me.getSecurityAccessorsGrid().setLoading(false);
                                    }
                                });
                            }
                        });
                    }
                }
            }
        );
    },

    onTrustStoreChange: function (combobox, newValue, oldValue) {
        var me = this,
            form = combobox.up('form'),
            keyTypeCombo = form.down('#mdc-security-accessor-key-type-combobox'),
            manageCentrallyCheckbox = form.down('#mdc-security-accessor-manage-centrally-checkbox');

        if (manageCentrallyCheckbox.getValue()
            && newValue && keyTypeCombo.getValue()) {
            me.getActivePassiveCertContainer().removeAll();
            me.getSecurityAccessorPreviewProperties();
        }
    },

    onManageCentrallyCheck: function (fld, newValue) {
        var me = this,
            form = fld.up('form'),
            keyTypeCombo = form.down('#mdc-security-accessor-key-type-combobox'),
            trustStoreCombo = form.down('#mdc-security-accessor-trust-store-combobox');

        me.isManageCentrallyChecked = newValue;

        if (trustStoreCombo.getValue()
            && newValue && keyTypeCombo.getValue()) {
            me.getActivePassiveCertContainer().removeAll();
            me.getSecurityAccessorPreviewProperties();
        } else {
            me.getActivePassiveCertContainer().removeAll();
        }

    },

    purposeChanged: function (combobox, newValue, oldValue) {
        var form = combobox.up('form'),
            isWrapper = form.down('#mdc-security-accessor-isWrapper-checkbox'),
            purposeRadio = form.down('#mdc-security-accessor-purpose-radio');
        if (purposeRadio.getValue().purpose.id != 'FILE_OPERATIONS') {
            isWrapper.setVisible(true);
        } else {
            isWrapper.setVisible(false);
        }
    },

    keyTypeChanged: function (combobox, newValue, oldValue) {
        var me = this,
            form = combobox.up('form'),
            purposeRadio = form.down('#mdc-security-accessor-purpose-radio'),
            keyRadio = form.down('#mdc-security-accessor-key-radio'),
            trustStoreCombo = form.down('#mdc-security-accessor-trust-store-combobox'),
            keyEncryptionMethodStore = me.getStore('Mdc.securityaccessors.store.KeyEncryptionMethods'),
            storageMethodCombo = form.down('#mdc-security-accessor-storage-method-combobox'),
            manageCentrallyCheckbox = form.down('#mdc-security-accessor-manage-centrally-checkbox'),
            validityPeriod = form.down('#mdc-security-accessor-validity-period'),
            hsmJssKeyTypeCombo = form.down('#mdc-security-accessor-jss-keytype-combobox'),
            importCapabiltyCombo = form.down('#mdc-security-accessor-import-capability-combobox'),
            renewCapabiltyCombo = form.down('#mdc-security-accessor-renew-capability-combobox'),
            labelEndPointCombo = form.down('#mdc-security-accessor-label-end-point-combobox'),
            keySizeInput = form.down('#mdc-security-accessor-key-size');
            isReversibleCheckBox = form.down('#mdc-security-accessor-isReversible-checkbox'),
            isWrapper = form.down('#mdc-security-accessor-isWrapper-checkbox'),

            requiresDuration = newValue && newValue.requiresDuration,
            requiresKeyEncryptionMethod = newValue && newValue.requiresKeyEncryptionMethod;

        validityPeriod.setVisible(requiresDuration);
        if (newValue && newValue.name == 'HSM Key') {
            hsmJssKeyTypeCombo.setVisible(true);
            hsmJssKeyTypeCombo.allowBlank = false;
            importCapabiltyCombo.setVisible(true);
            importCapabiltyCombo.allowBlank = false;
            renewCapabiltyCombo.setVisible(true);
            renewCapabiltyCombo.allowBlank = false;
            labelEndPointCombo.setVisible(true);
            labelEndPointCombo.allowBlank = false;
            isReversibleCheckBox.setVisible(true);
            keySizeInput.setVisible(true);
            keySizeInput.allowBlank = false;
        }
        else {
            hsmJssKeyTypeCombo.setVisible(false);
            hsmJssKeyTypeCombo.allowBlank = true;
            importCapabiltyCombo.setVisible(false);
            importCapabiltyCombo.allowBlank = true;
            renewCapabiltyCombo.setVisible(false);
            renewCapabiltyCombo.allowBlank = true;
            labelEndPointCombo.setVisible(false);
            labelEndPointCombo.allowBlank = true;
            isReversibleCheckBox.setVisible(false);
            keySizeInput.setVisible(false);
            keySizeInput.allowBlank = true;;
        }
        storageMethodCombo.setVisible(requiresKeyEncryptionMethod);
        storageMethodCombo.setDisabled(!requiresKeyEncryptionMethod);

        if (!(purposeRadio.getValue().purpose.id === 'FILE_OPERATIONS' && !keyRadio.getValue().key)) {
            manageCentrallyCheckbox.enable();
        }

        if (purposeRadio.getValue().purpose.id != 'FILE_OPERATIONS') {
            isWrapper.setVisible(true);
        } else {
            isWrapper.setVisible(false);
        }

        if (manageCentrallyCheckbox.getValue()
            && newValue && trustStoreCombo.getValue()) {
            me.getActivePassiveCertContainer().removeAll();
            me.getSecurityAccessorPreviewProperties();
        }

        if (!Ext.isEmpty(newValue)) {
            keyEncryptionMethodStore.getProxy().setUrl(newValue.id);
            keyEncryptionMethodStore.on('load', function (store, records, successful) {
                storageMethodCombo.bindStore(keyEncryptionMethodStore);
                if (successful) {
                    if (store.getCount() === 1) {
                        storageMethodCombo.setValue(store.getAt(0).get('name'));
                    }
                }
            }, me, {single: true});
            keyEncryptionMethodStore.load();
        }
    },

    saveSecurityAccessor: function () {
        var me = this,
            editWindow = me.getSecurityAccessorPrivilegesEditWindow(),
            securityAccessorRecordInEditWindow = editWindow.securityAccessorRecord,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        editWindow.close();
        viewport.setLoading();
        securityAccessorRecordInEditWindow.save({
            callback: function (record, operation, success) {
                if (success) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('securityaccessors.saveSecurityAccessorSuccess', 'MDC', 'Security accessor saved'));
                    me.recordSelected(me.getSecurityAccessorsGrid(), securityAccessorRecordInEditWindow);
                    viewport.setLoading(false);
                }
            }
        });
    },

    saveDefaultKeyValue: function () {
        var me = this,
            setDefaultKeyWindow = me.getSecurityAccessorSetDefaultKeyWindow(),
            securityAccessorRecord = setDefaultKeyWindow.securityAccessorRecord,
            viewport = Ext.ComponentQuery.query('viewport')[0];

        var keyValue = setDefaultKeyWindow.down('#defaultKeyValue').getValue();
        setDefaultKeyWindow.close();

        viewport.setLoading();

        Ext.Ajax.request({
            url: Ext.String.format('/api/dtc/devicetypes/{0}/securityaccessors/{1}/defaultkey', me.deviceTypeId, securityAccessorRecord.get('id')),
            method: 'PUT',
            jsonData: {
                "value": keyValue
            },
            success: function (response) {
                me.defaultKeyValue = keyValue;
                me.getPreviewForm().doLoadRecord(recordToSetKey, keyValue, me.deviceTypeId);

            },

            callback: function () {
                viewport.setLoading(false);
            }
        });
    },

    loadProperties: function (defaultPropertiesData, currentRecord) {

        var me = this,
            currentProperties = defaultPropertiesData.currentProperties,
            activeAliasCombo = undefined,
            passiveAliasCombo = undefined,
            aliasesStore = Ext.getStore('Mdc.securityaccessors.store.CertificateAliases') || Ext.create('Mdc.securityaccessors.store.CertificateAliases'),
            trustStoreId = currentRecord.get('trustStoreId');

        delete aliasesStore.getProxy().extraParams['trustStore'];
        if (currentProperties.length > 0) {
            _.map(currentProperties, function (property) {
                if (property.key === 'alias') {
                    aliasesStore.getProxy().setUrl(property.propertyTypeInfo.propertyValuesResource.possibleValuesURI);
                    activeAliasCombo = {
                        xtype: 'combobox',
                        fieldLabel: Uni.I18n.translate('general.activeCertificate', 'MDC', 'Active certificate'),
                        labelWidth: 185,
                        width: 485,
                        itemId: 'mdc-active-alias-combo',
                        dataIndex: 'defaultValue.currentProperties.alias',
                        emptyText: Uni.I18n.translate('general.startTypingToSelect', 'MDC', 'Start typing to select...'),
                        listConfig: {
                            emptyText: Uni.I18n.translate('general.startTypingToSelect', 'MDC', 'Start typing to select...')
                        },
                        displayField: 'alias',
                        value: property.propertyValueInfo.value,
                        valueField: 'alias',
                        store: aliasesStore,
                        queryMode: 'remote',
                        queryParam: 'alias',
                        queryDelay: 500,
                        queryCaching: false,
                        minChars: 0,
                        required: true,
                        allowBlank: false,
                        loadStore: false,
                        forceSelection: false,
                        listeners: {
                            expand: {
                                fn: me.comboLimitNotification
                            }
                        }
                    };
                } else if (property.key === 'trustStore') {
                    aliasesStore.getProxy().setExtraParam('trustStore', trustStoreId);
                    property.propertyValueInfo.value = {id: trustStoreId};
                }

            });
            me.getActivePassiveCertContainer().add(activeAliasCombo);
        }

        var tempProperties = defaultPropertiesData.tempProperties,
            aliasesStoreTemp = Ext.getStore('Mdc.securityaccessors.store.CertificateAliases') || Ext.create('Mdc.securityaccessors.store.CertificateAliases');

        delete aliasesStoreTemp.getProxy().extraParams['trustStore'];
        if (tempProperties.length > 0) {
            _.map(tempProperties, function (property) {
                if (property.key === 'alias') {
                    aliasesStoreTemp.getProxy().setUrl(property.propertyTypeInfo.propertyValuesResource.possibleValuesURI);
                    passiveAliasCombo = {
                        xtype: 'combobox',
                        fieldLabel: Uni.I18n.translate('general.passiveCertificate', 'MDC', 'Passive certificate'),
                        labelWidth: 185,
                        width: 485,
                        itemId: 'mdc-passive-alias-combo',
                        dataIndex: 'defaultValue.tempProperties.alias',
                        emptyText: Uni.I18n.translate('general.startTypingToSelect', 'MDC', 'Start typing to select...'),
                        listConfig: {
                            emptyText: Uni.I18n.translate('general.startTypingToSelect', 'MDC', 'Start typing to select...')
                        },
                        displayField: 'alias',
                        valueField: 'alias',
                        value: property.propertyValueInfo.value,
                        store: aliasesStoreTemp,
                        queryMode: 'remote',
                        queryParam: 'alias',
                        queryDelay: 500,
                        queryCaching: false,
                        minChars: 0,
                        loadStore: false,
                        forceSelection: false,
                        listeners: {
                            expand: {
                                fn: me.comboLimitNotification
                            }
                        }
                    };
                } else if (property.key === 'trustStore') {
                    aliasesStoreTemp.getProxy().setExtraParam('trustStore', trustStoreId);
                    property.propertyValueInfo.value = {id: trustStoreId};
                }
            });
            me.getActivePassiveCertContainer().add(passiveAliasCombo);
        }
    },

    activatePassiveCertificate: function (certificateRecord) {
        var me = this,
            url = '/api/dtc/securityaccessors/{certificateId}/swap';

        url = url.replace('{certificateId}', certificateRecord.get('id'));

        Ext.Ajax.request({
            url: url,
            method: 'PUT',
            jsonData: Ext.encode(certificateRecord.getData()),
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.passiveCertificate.activated', 'MDC', 'Passive certificate activated'));
                var router = me.getController('Uni.controller.history.Router');
                router.getRoute().forward(router.arguments);
            }
        });
    },

    clearPassive: function (keyOrCertificateRecord, keyMode) {
        var me = this,
            url = '/api/dtc/securityaccessors/{keyOrCertificateId}/tempvalue',
            title,
            confirmMessage,
            clearedMessage;

        title = Uni.I18n.translate('general.clearPassiveCertificate.title', 'MDC', "Clear passive certificate of '{0}'?", keyOrCertificateRecord.get('name'));
        confirmMessage = Uni.I18n.translate('general.clearPassiveCertificate.msg', 'MDC', 'The passive certificate will no longer be available.');
        clearedMessage = Uni.I18n.translate('general.passiveCertificate.cleared', 'MDC', 'Passive certificate cleared');


        Ext.create('Uni.view.window.Confirmation', {confirmText: Uni.I18n.translate('general.clear', 'MDC', 'Clear')}).show({
            title: title,
            msg: confirmMessage,
            fn: function (action) {
                if (action == 'confirm') {
                    url = url.replace('{keyOrCertificateId}', keyOrCertificateRecord.get('id'));
                    Ext.Ajax.request({
                        url: url,
                        method: 'DELETE',
                        jsonData: Ext.encode(keyOrCertificateRecord.getData()),
                        success: function () {
                            me.getApplication().fireEvent('acknowledge', clearedMessage);
                            var router = me.getController('Uni.controller.history.Router');
                            router.getRoute().forward(router.arguments);
                        }
                    });
                }
            }
        });
    },
});
