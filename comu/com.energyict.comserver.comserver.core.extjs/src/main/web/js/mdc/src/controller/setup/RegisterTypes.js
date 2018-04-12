/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.controller.setup.RegisterTypes', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.ux.window.Notification',
        'Mdc.model.StringResponse'
    ],

    views: [
        'setup.registertype.RegisterTypeFilter',
        'setup.registertype.RegisterTypeSetup',
        'setup.registertype.RegisterTypeGrid',
        'setup.registertype.RegisterTypePreview',
        'setup.registertype.RegisterTypeDetail',
        'setup.registertype.RegisterTypeEdit'
    ],

    stores: [
        'RegisterTypes',
        'ReadingTypes',
        'AvailableReadingTypesForRegisterType'
    ],

    refs: [
        {ref: 'registerTypeSetup', selector: '#registerTypeSetup'},
        {ref: 'registerTypeGrid', selector: '#registertypegrid'},
        {ref: 'registerTypePreviewForm', selector: '#registerTypePreviewForm'},
        {ref: 'registerTypePreview', selector: '#registerTypeSetup #registerTypePreview'},
        {ref: 'registerTypeDetailsLink', selector: '#registerTypeDetailsLink'},
        {ref: 'registerTypeEditView', selector: '#registerTypeEdit'},
        {ref: 'registerTypeEditForm', selector: '#registerTypeEditForm'},
        {ref: 'registerTypeDetailForm', selector: '#registerTypeDetailForm'},
        {ref: 'registerTypeEditForm', selector: '#registerTypeEditForm'},
        {ref: 'readingTypeCombo', selector: '#registerTypeEditForm #readingTypeCombo'},
        {ref: 'readingTypeToObisMappingMessage', selector: '#readingTypeToObisMappingMessage'},
        {ref: 'obisCodeToReadingTypeMessage', selector: '#obisCodeToReadingTypeMessage'},
        {ref: 'editObisCodeField', selector: '#registerTypeEditForm #editObisCodeField'},
        {ref: 'addReadingTypeButton', selector: '#registerTypeEditForm #addReadingTypeButton'}
    ],

    init: function () {
        this.getRegisterTypesStore().on('load', this.onRegisterTypesStoreLoad, this);

        this.control({
            '#registerTypeSetup #registertypegrid': {
                selectionchange: this.previewRegisterType
            },
            '#registertypegrid actioncolumn': {
                editRegisterType: this.editRegisterTypeHistory,
                deleteRegisterType: this.deleteRegisterType
            },
            '#registerTypeSetup button[action = createRegisterType]': {
                click: this.createRegisterTypeHistory
            },
            '#registerGroupEdit button[action = createRegisterType]': {
                click: this.createRegisterTypeHistory
            },
            '#registerTypePreview menuitem[action=editRegisterType]': {
                click: this.editRegisterTypeHistoryFromPreview
            },
            '#registerTypePreview menuitem[action=deleteRegisterType]': {
                click: this.deleteRegisterTypeFromPreview
            },
            '#registerTypeEditForm #createEditButton': {
                click: this.createEditRegisterType
            },
            '#registerTypeDetail menuitem[action=editRegisterType]': {
                click: this.editRegisterTypeFromDetails
            },
            '#registerTypeEdit #readingTypeCombo': {
                select: this.selectReadingType,
                expand: this.expandReadingTypes
            },
            '#registerTypeEdit #addReadingTypeButton': {
                click: this.addReadingType
            }

        });
    },
    onRegisterTypesStoreLoad: function () {
        if (this.getRegisterTypesStore().data.items.length > 0) {
            var setupWidget = this.getRegisterTypeSetup(),
                gridWidget = this.getRegisterTypeGrid();
            if (!Ext.isEmpty(setupWidget) && !Ext.isEmpty(gridWidget)) {
                setupWidget.show();
                gridWidget.getSelectionModel().doSelect(0);
            }

        }
    },

    /**
     * Check if any obis code to reading type mapping error
     * @param store
     */
    onAvailableReadingTypesForRegisterTypeStoreLoad: function (store) {
        if (this.getReadingTypeCombo().isDisabled())
            return;

        var me = this,
            data = store.getProxy().getReader().rawData,
            warning = me.getObisCodeToReadingTypeMessage();

        // Obis code doesn't map to a reading type and the obis field is not empty
        if (data && data.mappingError && me.getEditObisCodeField().getValue()){
            warning.update(data.mappingError);
            warning.show();
        }
        else {
            warning.hide();
        }
    },

    previewRegisterType: function (grid, record) {
        var registerTypes = this.getRegisterTypeGrid().getSelectionModel().getSelection();
        if (registerTypes.length == 1) {
            this.getRegisterTypePreviewForm().loadRecord(registerTypes[0]);
            this.getRegisterTypePreview().setTitle(Ext.String.htmlEncode(registerTypes[0].get('readingType').fullAliasName));
        }
    },

    createRegisterTypeHistory: function () {
        location.href = '#/administration/registertypes/add';
    },

    editRegisterTypeHistory: function (item) {
        location.href = '#/administration/registertypes/' + item.get('id') + '/edit';
    },

    editRegisterTypeHistoryFromPreview: function () {
        location.href = '#/administration/registertypes/' + this.getRegisterTypeGrid().getSelectionModel().getSelection()[0].get('id') + '/edit';
    },

    deleteRegisterType: function (registerTypeToDelete) {
        var me = this;

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('registerType.deleteRegisterType', 'MDC', 'The register type will no longer be available.'),
            title: Uni.I18n.translate('general.removex', 'MDC', "Remove '{0}'?",[registerTypeToDelete.get('name')]),
            config: {
                me: me,
                registerTypeToDelete: registerTypeToDelete

            },

            fn: me.deleteRegisterTypeInDatabase
        });
    },

    deleteRegisterTypeFromPreview: function () {
        this.deleteRegisterType(this.getRegisterTypeGrid().getSelectionModel().getSelection()[0]);
    },

    deleteRegisterTypeInDatabase: function (btn, text, opt) {
        if (btn === 'confirm') {
            var me = opt.config.me,
                registerTypeToDelete = opt.config.registerTypeToDelete,
                message = Uni.I18n.translate('registertype.acknowledgment.removed', 'MDC', 'Register type removed');
            registerTypeToDelete.destroy({
                success: function () {
                    me.getApplication().fireEvent('acknowledge', message);
                    me.getRegisterTypesStore().load();
                }
            });
        }
    },

    showRegisterTypeEditView: function (registerMapping) {
        var me = this,
            widget = Ext.widget('registerTypeEdit', {
                edit: true,
                returnLink: me.getController('Uni.controller.history.Router').getRoute('administration/registertypes').buildUrl()
            }),
            readingTypeStore;

        me.getAddReadingTypeButton().hide();
        this.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        Ext.ModelManager.getModel('Mdc.model.RegisterType').load(registerMapping, {
            success: function (registerType) {
                me.getApplication().fireEvent('loadRegisterType', registerType);
                widget.down('form').loadRecord(registerType);
                widget.down('#registerTypeEditForm').setTitle(Uni.I18n.translate('general.editx', 'MDC', "Edit '{0}'",[registerType.get('readingType').fullAliasName]));
                readingTypeStore = widget.down('#readingTypeCombo').getStore();
                readingTypeStore.load({
                    callback: function () {
                        widget.down('#readingTypeCombo').setValue(registerType.getReadingType());
                        widget.setLoading(false);
                    }
                });
                widget.down('#readingTypeCombo').disable();
                if (registerType.get('isLinkedByDeviceType') === true) {
                    widget.down('obis-field').disable();
                    widget.down('#createEditButton').disable();
                    widget.down('#registerTypeEditCreateInformation').update(Uni.I18n.translate('registertype.warningLinkedTodeviceType', 'MDC', 'The register type has been added to a device type'));
                    widget.down('#registerTypeEditCreateInformation').show();
                } else {
                    widget.down('obis-field').enable();
                }
            }
        });

    },

    showRegisterTypes: function () {
        var widget = Ext.widget('registerTypeSetup');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    showRegisterTypeCreateView: function () {
        var availableReadingTypesStore = Ext.create('Mdc.store.AvailableReadingTypesForRegisterType');
        var widget = Ext.widget('registerTypeEdit', {
            edit: false,
            returnLink: '#/administration/registertypes/',
            availableReadingTypes: availableReadingTypesStore
        });
        var me = this;
        me.getApplication().fireEvent('changecontentevent', widget);
        widget.down('#registerTypeEditForm').setTitle(Uni.I18n.translate('registerType.createRegisterType', 'MDC', 'Add register type'));
        availableReadingTypesStore.on('load', this.onAvailableReadingTypesForRegisterTypeStoreLoad, me);
        me.loadDefaultValues(availableReadingTypesStore);
    },

    /**
     * If we get back from the AddReadingType page, we might have an OBIS/CIM code in the URL.
     * We'll display the url query values in the corresponding containers.
     */
    loadDefaultValues: function(store) {
        var me = this,
            queryValues = Uni.util.QueryString.getQueryStringValues(false);

        if (queryValues.obis){
            me.getEditObisCodeField().setValue(queryValues.obis);
        }

        if (queryValues.mRID) {
            store.getProxy().extraParams = ({mRID: queryValues.mRID});
            store.load({
                callback: function (records, operation, success) {
                    if (success) {
                        me.getReadingTypeCombo().setValue(queryValues.mRID);
                    }
                }
            });
            store.getProxy().extraParams = {};
        }
    },

    createEditRegisterType: function (btn) {
        var me = this,
            editView = me.getRegisterTypeEditView(),
            values = me.getRegisterTypeEditForm().getValues(),
            router = me.getController('Uni.controller.history.Router'),
            backUrl = router.getRoute('administration/registertypes').buildUrl(),
            record;

        if (Ext.isEmpty(values.readingType)) values.readingType = null;

        if (btn.action === 'editRegisterType') {
            me.mode = 'edit';
            record = this.getRegisterTypeEditForm().getRecord();
        } else {
            me.mode = 'create';
            record = Ext.create(Mdc.model.RegisterType);
        }

        me.getAvailableReadingTypesForRegisterTypeStore().getProxy().extraParams = {};
        this.getRegisterTypeEditForm().getForm().clearInvalid();
        me.hideErrorPanel();
        if (record) {
            me.getRegisterTypeEditForm().getForm().clearInvalid();
            if (!me.getRegisterTypeEditForm().getForm().isValid()){
                me.showErrorPanel();
                return;
            }
            editView.setLoading();
            record.set(values);
            if (me.getReadingTypeCombo().valueModels && me.getReadingTypeCombo().valueModels[0]) {
                record.setReadingType(Ext.create(Mdc.model.ReadingType, me.getReadingTypeCombo().valueModels[0].getData()));
            }
            record.save({
                backUrl: backUrl,
                success: function () {
                    if (me.mode == 'create') {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('registertype.acknowledgment.added', 'MDC', 'Register type added'));
                    } else {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('registertype.acknowledgment.saved', 'MDC', 'Register type saved'));
                    }
                    location.href = backUrl;
                },
                failure: function (rec, operation) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        Ext.each(json.errors, function(error){
                           if(error.id === 'obisCodeCached.obisCode') {
                               error.id = 'obisCode';
                           }
                        });
                        me.getRegisterTypeEditForm().getForm().markInvalid(json.errors);
                        me.showErrorPanel();
                    }
                },
                callback: function () {
                    editView.setLoading(false);
                }
            });
        }
    },

    editRegisterTypeFromDetails: function () {
        var record = this.getRegisterTypeDetailForm().getRecord();
        location.href = '#/administration/registertypes/' + record.get('id') + '/edit';
    },

    /**
     * Map the selected reading type in the combobox to an Obis code.
     * @param combo Reading Type combobox
     */
    selectReadingType: function(combo) {
        var me = this;

        if (me.getEditObisCodeField().getValue()){
            return;
        }
        me.getReadingTypeToObisMappingMessage().hide();

        var model = Ext.ModelManager.getModel('Mdc.model.StringResponse');
        model.getProxy().setUrl("mappedObisCode");
        model.load(combo.getValue(), {
            success: function(record) {
                var obisCode = record.getData().response;
                if (obisCode) {
                    me.getEditObisCodeField().setValue(obisCode);
                } else {
                    me.getReadingTypeToObisMappingMessage().show();
                }
            }
        });
    },

    /**
     * Sets the obis code param value, which will be used to map to one or more
     * reading type values
     * @param combo Reading Type combobox
     */
    expandReadingTypes: function(combo) {
        var me = this,
            obis = me.getEditObisCodeField().getValue();
        if (obis){
            combo.getStore().getProxy().extraParams = ({obisCode: obis});
        } else {
            combo.getStore().getProxy().extraParams = {};
        }
    },

    addReadingType: function() {
        var me = this,
            mRID = me.getReadingTypeCombo().getValue(),
            obis =  me.getEditObisCodeField().getValue(),
            url = me.getReadingTypeUrl();

        // This flag tells kore how to return to this page.
        url = Ext.String.urlAppend(url, "back=addRegister");
        // Obis will still be loaded when we return to this page
        if (obis){
            url = Ext.String.urlAppend(url, "obis=" + obis);
        }

        if (mRID){
            location.href = Ext.String.urlAppend(url, "mRID=" + mRID);
        } else if (obis) {
            var model = Ext.ModelManager.getModel('Mdc.model.StringResponse');
            model.getProxy().setUrl("mappedReadingType");
            model.load(obis, {
                success: function (record) {
                    var value = record.getData().response;
                    if (value) {
                        url = Ext.String.urlAppend(url, "mRID=" + value);
                    }
                    location.href = url;
                }
            });
        } else {
            location.href = url;
        }
    },


    getReadingTypeUrl: function() {
        var host = location.protocol + "//" + location.host,
            pathname = "/apps/admin/index.html",
            hash = "#/administration/readingtypes/add";
        return host + pathname + hash;
    },


    showErrorPanel: function () {
        this.getRegisterTypeEditForm().down('#registerTypeEditFormErrors').show();
    },

    hideErrorPanel: function () {
        this.getRegisterTypeEditForm().down('#registerTypeEditFormErrors').hide();
    }

});
