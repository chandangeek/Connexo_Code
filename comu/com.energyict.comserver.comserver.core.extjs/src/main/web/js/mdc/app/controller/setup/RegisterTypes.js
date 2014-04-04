Ext.define('Mdc.controller.setup.RegisterTypes', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem',
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
        {ref: 'registerTypeGrid', selector: '#registertypegrid'},
        {ref: 'registerTypePreviewForm', selector: '#registerTypePreviewForm'},
        {ref: 'registerTypePreview', selector: '#registerTypePreview'},
        {ref: 'registerTypeDetailsLink', selector: '#registerTypeDetailsLink'},
        {ref: 'registerTypePreviewTitle', selector: '#registerTypePreviewTitle'},
        {ref: 'registerTypeEditView', selector: '#registerTypeEdit'},
        {ref: 'registerTypeEditForm', selector: '#registerTypeEditForm'},
        {ref: 'registerTypeDetailForm', selector: '#registerTypeDetailForm'},
        {ref: 'readingTypeDetailsForm', selector: '#readingTypeDetailsForm'},
        {ref: 'breadCrumbs', selector: 'breadcrumbTrail'},
        {ref: 'registerTypeEditForm', selector: '#registerTypeEditForm'},
        {ref: 'previewMrId', selector: '#preview_mrid'},
        {ref: 'detailMrId', selector: '#detail_mrid'}
    ],

    init: function () {
        this.getReadingTypesStore().on('load', this.onStoreLoad, this);

        this.control({
            '#registertypegrid': {
                selectionchange: this.previewRegisterType
            },
            '#registerTypeSetup breadcrumbTrail': {
                afterrender: this.overviewBreadCrumb
            },
            '#registertypegrid actioncolumn': {
                editItem: this.editRegisterTypeHistory,
                deleteItem: this.deleteRegisterType,
                showReadingTypeInfo: this.showReadingType
            },
            '#registerTypeSetup button[action = createRegisterType]': {
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
            '#registerTypeDetail menuitem[action=RegisterType]': {
                click: this.deleteRegisterTypeFromDetails
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
            '#registerTypeEditForm textfield[cls=obisCode]': {
                blur: this.getReadingType
            }
        });
    },

    onStoreLoad: function () {
        var widget = this.getRegisterTypeEditForm();
        if (this.getReadingTypesStore().getCount() === 1) {
            widget.down('#editMrIdField').setValue(this.getReadingTypesStore().first().get('mrid'));
        }
    },

    showEditView: function (id) {

    },

    previewRegisterType: function (grid, record) {
        var registerTypes = this.getRegisterTypeGrid().getSelectionModel().getSelection();
        if (registerTypes.length == 1) {
            this.getRegisterTypePreviewForm().loadRecord(registerTypes[0]);
            this.getRegisterTypePreview().getLayout().setActiveItem(1);
            this.getRegisterTypePreviewTitle().update('<h4>' + registerTypes[0].get('name') + '</h4>');
            this.getPreviewMrId().setValue(registerTypes[0].getReadingType().get('mrid'));
        } else {
            this.getRegisterTypePreview().getLayout().setActiveItem(0);
        }
    },

    showRegisterTypeDetailsView: function (registerType) {
        var me = this;
        var widget = Ext.widget('registerTypeDetail');
        Ext.ModelManager.getModel('Mdc.model.RegisterType').load(registerType, {
            success: function (registerType) {
                var registerMapping = registerType.get('id');
                me.detailBreadCrumb(registerType.get('name'), registerMapping);
                widget.down('form').loadRecord(registerType);
                me.getDetailMrId().setValue(registerType.getReadingType().get('mrid'));
                me.getRegisterTypePreviewTitle().update('<h4>' + registerType.get('name') + ' ' + Uni.I18n.translate('general.overview', 'MDC', 'Overview') + '</h4>');
            }
        });
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
    },

    createRegisterTypeHistory: function () {
        location.href = '#setup/registertypes/create';
    },

    editRegisterTypeHistory: function (item) {
        location.href = '#setup/registertypes/' + item.get('id') + '/edit';
    },

    editRegisterTypeHistoryFromPreview: function () {
        location.href = '#setup/registertypes/' + this.getRegisterTypeGrid().getSelectionModel().getSelection()[0].get('id') + '/edit';
    },

    deleteRegisterType: function (registerTypeToDelete) {
        var me = this;
        Ext.MessageBox.show({
            msg: Uni.I18n.translate('registerType.deleteRegisterType', 'MDC', 'The register type will no longer be available.'),
            title: Uni.I18n.translate('general.delete', 'MDC', 'Remove') + ' ' + registerTypeToDelete.get('name'),
            config: {
                registerTypeToDelete: registerTypeToDelete
            },
            buttons: Ext.MessageBox.YESNO,
            fn: me.deleteRegisterTypeInDatabase,
            icon: Ext.MessageBox.WARNING
        });

    },

    deleteRegisterTypeFromPreview: function (registerTypeToDelete) {
        this.deleteRegisterType(this.getRegisterTypeGrid().getSelectionModel().getSelection()[0]);

    },

    deleteRegisterTypeInDatabase: function (btn, text, opt) {
        if (btn === 'yes') {
            var registerTypeToDelete = opt.config.registerTypeToDelete;
            registerTypeToDelete.destroy({
                callback: function () {
                    location.href = '#setup/registertypes/';
                }
            });

        }
    },

    deleteRegisterTypeFromDetails: function () {
        var me = this;
        var registerTypeToDelete = this.getRegisterTypeDetailForm().getRecord();
        Ext.MessageBox.show({
            msg: Uni.I18n.translate('registerType.deleteRegisterType', 'MDC', 'The register type will no longer be available.'),
            title: Uni.I18n.translate('general.delete', 'MDC', 'Remove') + ' ' + registerTypeToDelete.get('name'),
            config: {
                registerTypeToDelete: registerTypeToDelete
            },
            buttons: Ext.MessageBox.YESNO,
            fn: me.deleteRegisterTypeFromDetailsInDatabase,
            icon: Ext.MessageBox.WARNING
        });
    },

    showRegisterTypeEditView: function (registerMapping) {
        var timeOfUseStore = Ext.create('Mdc.store.TimeOfUses');
        var unitOfMeasureStore = Ext.create('Mdc.store.UnitOfMeasures');
        var widget = Ext.widget('registerTypeEdit', {
            edit: true,
            unitOfMeasure: unitOfMeasureStore,
            timeOfUse: timeOfUseStore
        });
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
        widget.setLoading(true);
        var me = this;
        Ext.ModelManager.getModel('Mdc.model.RegisterType').load(registerMapping, {
            success: function (registerType) {
                me.editBreadCrumb(registerType.get('name'), registerMapping)
                timeOfUseStore.load({
                    callback: function (store) {
                        unitOfMeasureStore.load({
                            callback: function (store) {
                                widget.down('form').loadRecord(registerType);
                                widget.down('#registerTypeEditCreateTitle').update('<H2>' + registerType.get('name') + ' > ' + Uni.I18n.translate('general.edit', 'MDC', 'Edit') + ' ' + Uni.I18n.translate('registerType.registerType', 'MDC', 'Register type') + '</H2>');
                                widget.down('#editMrIdField').setValue(registerType.getReadingType().get('mrid'));
                                if (registerType.get('isLinkedByDeviceType') === true) {
                                    widget.down('#editObisCodeField').setDisabled(true);
                                    widget.down('#editObisCodeField').setReadOnly(true);
                                    widget.down('#measurementUnitComboBox').setDisabled(true);
                                    widget.down('#measurementUnitComboBox').setReadOnly(true);
                                    widget.down('#timeOfUseComboBox').setDisabled(true);
                                    widget.down('#timeOfUseComboBox').setReadOnly(true);
                                    widget.down('#editMrIdField').setDisabled(true);
                                    widget.down('#editMrIdField').setReadOnly(true);
                                    widget.down('#editRegisterTypeNameField').setDisabled(false);
                                    widget.down('#editRegisterTypeNameField').setReadOnly(false);
                                    widget.down('#registerTypeEditCreateInformation').update(Uni.I18n.translate('registertype.warningLinkedTodeviceType', 'MDC', 'The register type has been added to a device type.  Only the name is editable.'));
                                } else {
                                    widget.down('#editMrIdField').setDisabled(false);
                                    widget.down('#editMrIdField').setReadOnly(false);
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
        var me = this;
        this.getRegisterTypesStore().load(
            {
                callback: function () {
                    var widget = Ext.widget('registerTypeSetup');
                    me.getApplication().getController('Mdc.controller.Main').showContent(widget);
                    //me.overviewBreadCrumb(me.getBreadCrumbs);
                }
            });
    },

    showRegisterTypeCreateView: function () {
        var timeOfUseStore = Ext.create('Mdc.store.TimeOfUses');
        var unitOfMeasureStore = Ext.create('Mdc.store.UnitOfMeasures');
        var widget = Ext.widget('registerTypeEdit', {
            edit: false,
            returnLink: '#setup/registertypes/',
            unitOfMeasure: unitOfMeasureStore,
            timeOfUse: timeOfUseStore
        });
        var me = this;
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
        widget.setLoading(true);

        timeOfUseStore.load({
            callback: function (store) {
                unitOfMeasureStore.load({
                    callback: function (store) {
                        widget.down('#registerTypeEditCreateTitle').update('<H2>' + Uni.I18n.translate('registerType.createRegisterType', 'MDC', 'Create register type') + '</H2>');
                        me.createBreadCrumb();
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
                    location.href = '#setup/registertypes/';
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
                    location.href = '#setup/registertypes/';
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

    overviewBreadCrumb: function (breadcrumbs) {
        var breadcrumbChild = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registertype.registerTypes', 'MDC', 'Register types'),
            href: 'registertypes'
        });

        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });
        breadcrumbParent.setChild(breadcrumbChild);
        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    createBreadCrumb: function () {
        var breadcrumb1 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });
        var breadcrumb2 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registertype.registerTypes', 'MDC', 'Register types'),
            href: 'registertypes'
        });
        var breadcrumb3 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registertype.create', 'MDC', 'Create register type'),
            href: 'create'
        });
        breadcrumb1.setChild(breadcrumb2).setChild(breadcrumb3);
        this.getBreadCrumbs().setBreadcrumbItem(breadcrumb1);
    },

    editBreadCrumb: function (registerTypeName, registerTypeId) {
        var breadcrumb1 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });
        var breadcrumb2 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registertype.registerTypes', 'MDC', 'Register types'),
            href: 'registertypes'
        });
        var breadcrumb4 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registertype.edit', 'MDC', 'Edit register type'),
            href: 'edit'
        });
        breadcrumb1.setChild(breadcrumb2).setChild(breadcrumb4);
        this.getBreadCrumbs().setBreadcrumbItem(breadcrumb1);
    },

    detailBreadCrumb: function (registerTypeName, registerTypeId) {
        var breadcrumb1 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });
        var breadcrumb2 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registertype.registerTypes', 'MDC', 'Register types'),
            href: 'registertypes'
        });
        var breadcrumb3 = Ext.create('Uni.model.BreadcrumbItem', {
            text: registerTypeName,
            href: registerTypeId
        });
        var breadcrumb4 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.overview', 'MDC', 'Overview')
        });
        breadcrumb1.setChild(breadcrumb2).setChild(breadcrumb3).setChild(breadcrumb4);
        this.getBreadCrumbs().setBreadcrumbItem(breadcrumb1);
    },

    editRegisterTypeFromDetails: function () {
        var record = this.getRegisterTypeDetailForm().getRecord();
        location.href = '#setup/registertypes/' + record.get('id') + '/edit';
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
        var mrId = widget.down('#editMrIdField').getValue();

        if (obisCode !== '' && measurementUnit !== null && mrId === '') {
            this.getReadingTypesStore().clearFilter();
            this.getReadingTypesStore().filter([
                {property: 'obisCode', value: obisCode},
                {property: 'unit', value: measurementUnit}
            ]);
        }
        widget.down('#editMrIdField').setDisabled(false);
        widget.down('#editMrIdField').setReadOnly(false);

    }


})
;
