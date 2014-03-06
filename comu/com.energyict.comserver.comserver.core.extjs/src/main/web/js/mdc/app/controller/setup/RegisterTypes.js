Ext.define('Mdc.controller.setup.RegisterTypes', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem',
        'Ext.ux.window.Notification'
    ],

    views: [
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
        {ref: 'registerTypeEditForm', selector: '#registerTypeEditForm'}
    ],

    init: function () {
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
                click: this.editRegisterTypeHistory
            },
            '#registerTypePreview menuitem[action=deleteRegisterType]': {
                click: this.deleteRegisterType
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
                change: this.getReadingType
            }

        });
    },

    showEditView: function (id) {

    },

    previewRegisterType: function (grid, record) {
        var registerTypes = this.getRegisterTypeGrid().getSelectionModel().getSelection();
        if (registerTypes.length == 1) {
            var registerTypeId = registerTypes[0].get('id');

            this.getRegisterTypePreviewForm().loadRecord(registerTypes[0]);
            this.getRegisterTypePreview().getLayout().setActiveItem(1);
            this.getRegisterTypeDetailsLink().update('<a href="#/setup/registertypes/' + registerTypeId + '">' + Uni.I18n.translate('general.viewDetails', 'MDC', 'View details') + '</a>');
            this.getRegisterTypePreviewTitle().update('<h4>' + registerTypes[0].get('name') + '</h4>');
        } else {
            this.getRegisterTypePreview().getLayout().setActiveItem(0);
        }
    },

    showRegisterTypeDetailsView: function (registerType) {
        var me = this;
        var widget = Ext.widget('registerTypeDetail');
        Ext.ModelManager.getModel('Mdc.model.RegisterType').load(registerType, {
            success: function (registerType) {
                var registerTypeId = registerType.get('id');
                me.detailBreadCrumb(registerType.get('name'), registerTypeId);

                widget.down('form').loadRecord(registerType);

                me.getRegisterTypePreviewTitle().update('<h4>' + registerType.get('name') + ' ' + Uni.I18n.translate('general.overview', 'MDC', 'Overview') + '</h4>');
            }
        });
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
    },

    createRegisterTypeHistory: function () {
        location.href = '#setup/registertypes/create';
    },

    editRegisterTypeHistory: function () {
        location.href = '#setup/registertypes/' + this.getRegisterTypeGrid().getSelectionModel().getSelection()[0].get('id') + '/edit';
    },

    deleteRegisterType: function () {
        var registerTypeToDelete = this.getRegisterTypeGrid().getSelectionModel().getSelection()[0];
        registerTypeToDelete.destroy({
            callback: function () {
                location.href = '#setup/registertypes/';
            }
        });
    },

    deleteRegisterTypeFromDetails: function () {
        var registerTypeToDelete = this.getRegisterTypeDetailForm().getRecord();
        registerTypeToDelete.destroy({
            callback: function () {
                location.href = '#setup/registertypes/';
            }
        });
    },

    showRegisterTypeEditView: function (registerTypeId) {
        console.log(registerTypeId);
        var timeOfUseStore = Ext.create('Mdc.store.TimeOfUses');
        var unitOfMeasureStore = Ext.create('Mdc.store.UnitOfMeasures');
        var widget = Ext.widget('registerTypeEdit', {
            edit: true,
            returnLink: this.getApplication().getHistorySetupController().tokenizePreviousTokens(),
            unitOfMeasure: unitOfMeasureStore,
            timeOfUse: timeOfUseStore
        });
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
        widget.setLoading(true);
        var me = this;
        Ext.ModelManager.getModel('Mdc.model.RegisterType').load(registerTypeId, {
            success: function (registerType) {
                me.editBreadCrumb(registerType.get('name'), registerTypeId)
                timeOfUseStore.load({
                    callback: function (store) {
                        unitOfMeasureStore.load({
                            callback: function (store) {
                                widget.down('form').loadRecord(registerType);
                                widget.down('#registerTypeEditCreateTitle').update('<H2>' + Uni.I18n.translate('general.edit', 'MDC', 'Edit') + ' ' + registerType.get('name') + '</H2>');
                                console.log(registerType.get('isInUse'));
                                if (registerType.get('isInUse') === true) {
                                    widget.down('#editObisCodeField').setDisabled(true);
                                    widget.down('#editObisCodeField').setReadOnly(true);
                                    widget.down('#measurementUnitComboBox').setDisabled(true);
                                    widget.down('#measurementUnitComboBox').setReadOnly(true);
                                    widget.down('#timeOfUseComboBox').setDisabled(true);
                                    widget.down('#timeOfUseComboBox').setReadOnly(true);
                                    widget.down('#editMrIdField').setDisabled(true);
                                    widget.down('#editMrIdField').setReadOnly(true);
                                    widget.down('#editRegisterTypeNameField').setDisabled(true);
                                    widget.down('#editRegisterTypeNameField').setReadOnly(true);

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
                    //console.log('store loaded');
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
        if (record) {
            record.set(values);
            record.save({
                success: function (record) {
                    location.href = '#setup/registertypes/' + record.get('id');
                }
            });

        }
    },

    editRegisterType: function () {
        var record = this.getRegisterTypeEditForm().getRecord(),
            values = this.getRegisterTypeEditForm().getValues(),
            me=this;
        if (record) {
            record.set(values);
            record.save({
                callback: function (record) {
                    location.href = me.getApplication().getHistorySetupController().tokenizePreviousTokens();
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
        var breadcrumb3 = Ext.create('Uni.model.BreadcrumbItem', {
            text: registerTypeName,
            href: registerTypeId
        });
        var breadcrumb4 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registertype.edit', 'MDC', 'Edit register type'),
            href: 'edit'
        });
        breadcrumb1.setChild(breadcrumb2).setChild(breadcrumb3).setChild(breadcrumb4);
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
        this.getReadingTypeDetailsForm().loadRecord(record);
        widget.show();
    },

    getReadingType: function (field, newValue, oldValue) {
        var widget = this.getRegisterTypeEditForm();
        var obisCode = widget.down('#editObisCodeField').getValue();
        var measurementUnit = widget.down('#measurementUnitComboBox').getValue();

        if (obisCode != '' && measurementUnit != null) {

            var readingTypeStore = Ext.data.StoreManager.lookup('ReadingTypes');
            /* readingTypeStore.filter([
             {property: 'obisCode', value: obisCode},
             {property: 'unit', value: measurementUnit}
             ]);*/
            readingTypeStore.load({
                scope: this,
                callback: function () {
                    console.log(readingTypeStore);
                    widget.down('#editMrIdField').setDisabled(false);
                    widget.down('#editMrIdField').setReadOnly(false);
                    widget.down('#editMrIdField').setValue(readingTypeStore.first().get('mrid'));
                    console.log('updated');

                }
            });
        }

    }


});
