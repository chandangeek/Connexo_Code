Ext.define('Mdc.controller.setup.RegisterTypes', {
    extend: 'Ext.app.Controller',

    requires: [
        'Ext.ux.window.Notification'
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
        {ref: 'readingTypeCombo', selector: '#registerTypeEditForm #readingTypeCombo'}
    ],

    /*  loadReadingTypes: function (combo) {
     var me = this,
     editView = me.getRegisterTypeEditView(),
     readingTypeCombo = me.getReadingTypeCombo(),
     readingTypeStore = readingTypeCombo.getStore(),
     readingHiddenDisplayField = editView.down('#noReadingAvailable');

     readingTypeCombo.disable();
     readingTypeCombo.setValue(null);

     readingTypeStore.load({
     callback: function () {
     if (this.getCount()) {
     readingTypeCombo.show();
     readingHiddenDisplayField.hide();
     } else {
     readingTypeCombo.hide();
     readingHiddenDisplayField.show();
     }
     editView.setLoading(false);
     readingTypeCombo.enable();
     }
     });

     },*/


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

    previewRegisterType: function (grid, record) {
        var registerTypes = this.getRegisterTypeGrid().getSelectionModel().getSelection();
        if (registerTypes.length == 1) {
            this.getRegisterTypePreviewForm().loadRecord(registerTypes[0]);
            this.getRegisterTypePreview().setTitle(registerTypes[0].get('readingType').fullAliasName);
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
            title: Uni.I18n.translate('general.remove', 'MDC', 'Remove') + ' ' + registerTypeToDelete.get('name') + '?',
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
                message = Uni.I18n.translate('registertype.acknowlegment.removed', 'MDC', 'Register type removed');
            registerTypeToDelete.destroy({
                success: function () {
                    me.getApplication().fireEvent('acknowledge', message);
                    me.getRegisterTypesStore().load();
                },
                callback: function () {
                    location.href = '#/administration/registertypes/';

                }

            });
        }
    },

    showRegisterTypeEditView: function (registerMapping) {
        var me = this,
            widget = Ext.widget('registerTypeEdit', {
                edit: true,
                returnLink: me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens()
            }),
            readingTypeStore;
        this.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        Ext.ModelManager.getModel('Mdc.model.RegisterType').load(registerMapping, {
            success: function (registerType) {
                me.getApplication().fireEvent('loadRegisterType', registerType);
                widget.down('form').loadRecord(registerType);
                widget.down('#registerTypeEditForm').setTitle(Uni.I18n.translate('general.edit', 'MDC', 'Edit') + ' \'' + registerType.get('readingType').fullAliasName + '\'');
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
    },

    createEditRegisterType: function (btn) {
        var me = this,
            editView = me.getRegisterTypeEditView(),
            values = this.getRegisterTypeEditForm().getValues(),
            record;

        if (btn.action === 'editRegisterType') {
            me.mode = 'edit';
            record = this.getRegisterTypeEditForm().getRecord();
        } else {
            me.mode = 'create';
            record = Ext.create(Mdc.model.RegisterType);
        }

        if (record) {
            editView.setLoading();
            record.set(values);
            if (me.getReadingTypeCombo().valueModels && me.getReadingTypeCombo().valueModels[0]) {
                record.setReadingType(Ext.create(Mdc.model.ReadingType, me.getReadingTypeCombo().valueModels[0].getData()));
            }
            record.save({
                success: function () {
                    if (me.mode == 'create') {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('registertype.acknowlegment.added', 'MDC', 'Register type added'));
                    } else {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('registertype.acknowlegment.saved', 'MDC', 'Register type saved'));
                    }
                    editView.setLoading(false);
                    location.href = '#/administration/registertypes/';
                },
                failure: function (rec, operation) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        me.getRegisterTypeEditForm().getForm().markInvalid(json.errors);
                    }
                    editView.setLoading(false);
                }

            });
        }
    },

    editRegisterTypeFromDetails: function () {
        var record = this.getRegisterTypeDetailForm().getRecord();
        location.href = '#/administration/registertypes/' + record.get('id') + '/edit';
    }
});
