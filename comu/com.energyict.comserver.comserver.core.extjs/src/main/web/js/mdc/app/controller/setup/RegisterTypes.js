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
        'setup.registertype.RegisterTypeEdit',
        'setup.register.ReadingTypeDetails'
    ],

    stores: [
        'RegisterTypes',
        'TimeOfUses',
        'UnitOfMeasures',
        'ReadingTypes'
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
        {ref: 'readingTypeDetailsForm', selector: '#readingTypeDetailsForm'},
        {ref: 'registerTypeEditForm', selector: '#registerTypeEditForm'},
        {ref: 'previewMrId', selector: '#preview_mrid'},
        {ref: 'detailMrId', selector: '#detail_mrid'}
    ],

    init: function () {
        this.getReadingTypesStore().on('load', this.onReadingTypesStoreLoad, this);
        this.getRegisterTypesStore().on('load', this.onRegisterTypesStoreLoad, this);

        this.control({
            '#registerTypeSetup #registertypegrid': {
                selectionchange: this.previewRegisterType
            },
            '#registertypegrid actioncolumn': {
                editRegisterType: this.editRegisterTypeHistory,
                deleteRegisterType: this.deleteRegisterType,
                showReadingTypeInfo: this.showReadingType
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
            '#createEditButton[action=createRegisterType]': {
                click: this.createRegisterType
            },
            '#createEditButton[action=editRegisterType]': {
                click: this.editRegisterType
            },
            '#registerTypeDetail menuitem[action=editRegisterType]': {
                click: this.editRegisterTypeFromDetails
            },
            '#registerTypePreviewForm button[action = showReadingTypeInfo]': {
                showReadingTypeInfo: this.showReadingType
            },
            '#registerTypeDetailForm button[action = showReadingTypeInfo]': {
                showReadingTypeInfo: this.showReadingType
            },
            '#registerTypeEditForm obis-field': {
                blur: this.getReadingType
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

    onReadingTypesStoreLoad: function () {
        var widget = this.getRegisterTypeEditForm();
        var me = this;
        if (me.getReadingTypesStore().getCount() === 1) {
            if (widget.down('#editMrIdField').getValue() != '') {
                if (me.getReadingTypesStore().first().get('mrid') !== widget.down('#editMrIdField').getValue()) {
                    Ext.MessageBox.show({
                        msg: Uni.I18n.translate('registerType.changeReadingType', 'MDC', 'Do you want to change the readingtype with this new readingtype {0}?', [me.getReadingTypesStore().first().get('mrid')]),
                        title: Uni.I18n.translate('general.changeReadingType', 'MDC', 'Change readingtype') + ' ' + widget.down('#editMrIdField').getValue(),
                        config: {
                            widget: widget,
                            newReadingType: me.getReadingTypesStore().first().get('mrid')
                        },
                        buttons: Ext.MessageBox.YESNO,
                        fn: me.changeReadingType,
                        icon: Ext.MessageBox.QUESTION
                    });
                }
            } else {
                widget.down('#editMrIdField').setValue(me.getReadingTypesStore().first().get('mrid'));
            }
        }
        var obisCode = widget.down('#editObisCodeField').getValue();
        var measurementUnit = widget.down('#measurementUnitComboBox').getValue();
        if (obisCode !== '' && measurementUnit !== null) {
            widget.down('#editMrIdField').enable();
        }
    },

    changeReadingType: function (btn, text, opt) {
        if (btn === 'yes') {
            var widget = opt.config.widget;
            var newReadingType = opt.config.newReadingType;
            widget.down('#editMrIdField').setValue(newReadingType);
        }
    },

    previewRegisterType: function (grid, record) {
        var registerTypes = this.getRegisterTypeGrid().getSelectionModel().getSelection();
        if (registerTypes.length == 1) {
            this.getRegisterTypePreviewForm().loadRecord(registerTypes[0]);
            this.getRegisterTypePreview().setTitle(registerTypes[0].get('name'));
            this.getPreviewMrId().setValue(registerTypes[0].getReadingType().get('mrid'));
        }
    },

    showRegisterTypeDetailsView: function (registerType) {
        var me = this;
        var widget = Ext.widget('registerTypeDetail');
        Ext.ModelManager.getModel('Mdc.model.RegisterType').load(registerType, {
            success: function (registerType) {
                me.getApplication().fireEvent('loadRegisterType', registerType);
                var registerMapping = registerType.get('id');
                widget.down('form').loadRecord(registerType);
                me.getDetailMrId().setValue(registerType.getReadingType().get('mrid'));
                me.getRegisterTypePreview().setTitle(registerType.get('name') + ' ' + Uni.I18n.translate('general.overview', 'MDC', 'Overview'));
            }
        });
        this.getApplication().fireEvent('changecontentevent', widget);
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
                registerTypeToDelete: registerTypeToDelete
            },
            fn: me.deleteRegisterTypeInDatabase
        });
    },

    deleteRegisterTypeFromPreview: function (registerTypeToDelete) {
        this.deleteRegisterType(this.getRegisterTypeGrid().getSelectionModel().getSelection()[0]);
    },

    deleteRegisterTypeInDatabase: function (btn, text, opt) {
        if (btn === 'confirm') {
            var registerTypeToDelete = opt.config.registerTypeToDelete;
            registerTypeToDelete.destroy({
                callback: function () {
                    location.href = '#/administration/registertypes/';
                }
            });
        }
    },

    showRegisterTypeEditView: function (registerMapping) {
        var me = this;
        var timeOfUseStore = Ext.create('Mdc.store.TimeOfUses');
        var unitOfMeasureStore = Ext.create('Mdc.store.UnitOfMeasures');
        var widget = Ext.widget('registerTypeEdit', {
            edit: true,
            unitOfMeasure: unitOfMeasureStore,
            timeOfUse: timeOfUseStore,
            returnLink: me.getApplication().getController('Mdc.controller.history.Setup').tokenizePreviousTokens()
        });
        this.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);
        Ext.ModelManager.getModel('Mdc.model.RegisterType').load(registerMapping, {
            success: function (registerType) {
                me.getApplication().fireEvent('loadRegisterType', registerType);
                timeOfUseStore.load({
                    callback: function (store) {
                        unitOfMeasureStore.load({
                            callback: function (store) {
                                widget.down('form').loadRecord(registerType);
                                widget.down('#registerTypeEditCreateTitle').update('<h1>' + Uni.I18n.translate('general.edit', 'MDC', 'Edit') + ' ' + registerType.get('name') + '</h1>');
                                widget.down('#editMrIdField').setValue(registerType.getReadingType().get('mrid'));
                                if (registerType.get('isLinkedByDeviceType') === true) {
                                    widget.down('#editObisCodeField').disable();
                                    widget.down('#measurementUnitComboBox').disable();
                                    widget.down('#timeOfUseComboBox').disable();
                                    widget.down('#editMrIdField').disable();
                                   // widget.down('#editRegisterTypeNameField').disable();
                                    widget.down('#registerTypeEditCreateInformation').update(Uni.I18n.translate('registertype.warningLinkedTodeviceType', 'MDC', 'The register type has been added to a device type.  Only the name is editable.'));
                                    widget.down('#registerTypeEditCreateInformation').show();
                                } else {
                                    widget.down('#editMrIdField').enable();

                                }
                                widget.setLoading(false);
                            }
                        })
                    }
                })
            }
        });
    },

    showRegisterTypes: function () {
        var widget = Ext.widget('registerTypeSetup');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    showRegisterTypeCreateView: function () {
        var timeOfUseStore = Ext.create('Mdc.store.TimeOfUses');
        var unitOfMeasureStore = Ext.create('Mdc.store.UnitOfMeasures');
        var widget = Ext.widget('registerTypeEdit', {
            edit: false,
            returnLink: '#/administration/registertypes/',
            unitOfMeasure: unitOfMeasureStore,
            timeOfUse: timeOfUseStore
        });
        var me = this;
        this.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);

        timeOfUseStore.load({
            callback: function (store) {
                unitOfMeasureStore.load({
                    callback: function (store) {
                        widget.down('#registerTypeEditCreateTitle').update('<h1>' + Uni.I18n.translate('registerType.createRegisterType', 'MDC', 'Add register type') + '</h1>');
                        widget.setLoading(false);
                    }
                });

            }
        });
    },

    createRegisterType: function () {
        var record = Ext.create(Mdc.model.RegisterType),
            values = this.getRegisterTypeEditForm().getValues();
        var widget = this.getRegisterTypeEditForm();
        var mrId = widget.down('#editMrIdField').getValue();
        var me = this;

        //delete values.mrid;
        var readingType = Ext.create(Mdc.model.ReadingType);

        readingType.data.mrid = mrId;
        if (record) {
            record.set(values);
            record.setReadingType(readingType);

            record.save({
                success: function (record) {
                    location.href = '#/administration/registertypes/';
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        me.getRegisterTypeEditForm().getForm().markInvalid(json.errors);
                    }
                }

            });

        }
    },

    editRegisterType: function () {
        var record = this.getRegisterTypeEditForm().getRecord(),
            values = this.getRegisterTypeEditForm().getValues(),
            me = this;

        var widget = this.getRegisterTypeEditForm();
        var mrId = widget.down('#editMrIdField').getValue();

        var readingType = Ext.create(Mdc.model.ReadingType);

        readingType.data.mrid = mrId;

        if (record) {
            record.set(values);
            record.setReadingType(readingType);
            record.save({
                success: function (record) {
                    location.href = '#/administration/registertypes/';
                },
                failure: function (record, operation) {
                    var json = Ext.decode(operation.response.responseText);
                    if (json && json.errors) {
                        me.getRegisterTypeEditForm().getForm().markInvalid(json.errors);
                    }
                }
            });
        }
    },

    editRegisterTypeFromDetails: function () {
        var record = this.getRegisterTypeDetailForm().getRecord();
        location.href = '#/administration/registertypes/' + record.get('id') + '/edit';
    },

    showReadingType: function (record) {
        var widget = Ext.widget('readingTypeDetails');
        this.getReadingTypeDetailsForm().loadRecord(record.getReadingType());
        widget.show();
    },

    getReadingType: function (field, newValue, oldValue) {
        var widget = this.getRegisterTypeEditForm();
        var obisCode = widget.down('#editObisCodeField').getValue();
        var measurementUnit = widget.down('#measurementUnitComboBox').getValue();
        //var mrId = widget.down('#editMrIdField').getValue();

        if (obisCode !== '' && measurementUnit !== null && obisCode != null && measurementUnit != '') {
            this.getReadingTypesStore().clearFilter();
            this.getReadingTypesStore().filter([
                {property: 'obisCode', value: obisCode},
                {property: 'unit', value: measurementUnit}
            ]);
        }
    }


})
;
