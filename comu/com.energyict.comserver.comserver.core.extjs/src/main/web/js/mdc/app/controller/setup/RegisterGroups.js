Ext.define('Mdc.controller.setup.RegisterGroups', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.model.BreadcrumbItem',
        'Ext.ux.window.Notification'
    ],

    views: [
        'setup.registergroup.RegisterGroupSetup',
        'setup.registergroup.RegisterGroupGrid',
        'setup.registergroup.RegisterGroupPreview',
        //'setup.registergroup.RegisterGroupDetail',
        'setup.registergroup.RegisterGroupEdit'
    ],

    stores: [
        'RegisterGroups'
    ],

    refs: [
        {ref: 'registerGroupGrid', selector: '#registergroupgrid'},
        {ref: 'registerGroupPreviewForm', selector: '#registerGroupPreviewForm'},
        {ref: 'registerGroupPreview', selector: '#registerGroupPreview'},
        {ref: 'registerGroupPreviewTitle', selector: '#registerGroupPreviewTitle'},
        {ref: 'registerGroupEditView', selector: '#registerGroupEdit'},
        {ref: 'registerGroupEditForm', selector: '#registerGroupEditForm'},
        {ref: 'breadCrumbs', selector: 'breadcrumbTrail'},
        {ref: 'registerGroupEditForm', selector: '#registerGroupEditForm'},
        {ref: 'registerGroupPreviewGrid', selector: '#registerGroupPreviewGrid'},
        {ref: 'registerGroupPreviewDetails', selector: '#registerGroupPreviewDetails'}
    ],

    init: function () {
        this.getRegisterGroupsStore().on('load', this.onStoreLoad, this);

        this.control({
            '#registergroupgrid': {
                selectionchange: this.previewRegisterGroup
            },
            '#registerGroupSetup breadcrumbTrail': {
                afterrender: this.overviewBreadCrumb
            },
            '#registergroupgrid actioncolumn': {
                editItem: this.editRegisterGroupHistory
            },
            '#registerGroupSetup button[action = createRegisterGroup]': {
                click: this.createRegisterGroupHistory
            },
            '#registerGroupPreview menuitem[action=editRegisterGroup]': {
                click: this.editRegisterGroupHistoryFromPreview
            },
            '#createEditButton[action=createRegisterGroup]': {
                click: this.createRegisterGroup
            },
            '#createEditButton[action=editRegisterGroup]': {
                click: this.editRegisterGroup
            }
        });
    },

    onStoreLoad: function () {
        /*var widget = this.getRegisterTypeEditForm();
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
        }*/
    },

    showRegisterGroups: function (grid, record) {
        var me = this;
        this.getRegisterGroupsStore().load(
            {
                callback: function () {
                    var widget = Ext.widget('registerGroupSetup');
                    me.getApplication().getController('Mdc.controller.Main').showContent(widget);
                    //me.overviewBreadCrumb(me.getBreadCrumbs);
                }
            });
    },

    previewRegisterGroup: function (grid, record) {
        var registerGroups = this.getRegisterGroupGrid().getSelectionModel().getSelection();
        if (registerGroups.length == 1) {
            this.getRegisterGroupPreviewForm().loadRecord(registerGroups[0]);
            this.getRegisterGroupPreview().getLayout().setActiveItem(1);
            this.getRegisterGroupPreviewTitle().update('<h4>' + registerGroups[0].get('name') + '</h4>');
        } else {
            this.getRegisterGroupPreview().getLayout().setActiveItem(0);
        }
    },

    createRegisterGroupHistory: function () {
        location.href = '#setup/registergroups/create';
    },

    editRegisterGroupHistory: function (item) {
        location.href = '#setup/registergroups/' + item.get('id') + '/edit';
    },

    editRegisterGroupHistoryFromPreview: function () {
        location.href = '#setup/registergroups/' + this.getRegisterGroupGrid().getSelectionModel().getSelection()[0].get('id') + '/edit';
    },

    showRegisterGroupEditView: function (registerMapping) {
        /*var timeOfUseStore = Ext.create('Mdc.store.TimeOfUses');
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
        });*/
    },

    showRegisterGroupCreateView: function () {
        /*var timeOfUseStore = Ext.create('Mdc.store.TimeOfUses');
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
        });*/
    },

    createRegisterGroup: function () {
        /*var record = Ext.create(Mdc.model.RegisterGroup),
            values = this.getRegisterGroupEditForm().getValues();
        var widget = this.getRegisterGroupEditForm();
        var mrId = widget.down('#editMrIdField').getValue();
        var me = this;

        //delete values.mrid;
        var readingType = Ext.create(Mdc.model.RegisterGroup);

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

        }*/
    },

    editRegisterGroup: function () {
        /*var record = this.getRegisterTypeEditForm().getRecord(),
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
        }*/
    },

    overviewBreadCrumb: function (breadcrumbs) {
        var breadcrumbChild = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registergroup.registerGroups', 'MDC', 'Register groups'),
            href: 'registergroups'
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
            text: Uni.I18n.translate('registergroup.registerGroups', 'MDC', 'Register groups'),
            href: 'registertypes'
        });
        var breadcrumb3 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registergroup.create', 'MDC', 'Create register group'),
            href: 'create'
        });
        breadcrumb1.setChild(breadcrumb2).setChild(breadcrumb3);
        this.getBreadCrumbs().setBreadcrumbItem(breadcrumb1);
    },

    editBreadCrumb: function (registerGroupName, registerGroupId) {
        var breadcrumb1 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });
        var breadcrumb2 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registergroup.registerGroups', 'MDC', 'Register groups'),
            href: 'registertypes'
        });
        var breadcrumb4 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registergroup.edit', 'MDC', 'Edit register group'),
            href: 'edit'
        });
        breadcrumb1.setChild(breadcrumb2).setChild(breadcrumb4);
        this.getBreadCrumbs().setBreadcrumbItem(breadcrumb1);
    }

    /*detailBreadCrumb: function (registerGroupName, registerGroupId) {
        var breadcrumb1 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });
        var breadcrumb2 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registergroup.registerGroups', 'MDC', 'Register groups'),
            href: 'registertypes'
        });
        var breadcrumb3 = Ext.create('Uni.model.BreadcrumbItem', {
            text: registerGroupName,
            href: registerGroupId
        });
        var breadcrumb4 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.overview', 'MDC', 'Overview')
        });
        breadcrumb1.setChild(breadcrumb2).setChild(breadcrumb3).setChild(breadcrumb4);
        this.getBreadCrumbs().setBreadcrumbItem(breadcrumb1);
    },

    editRegisterGroupFromDetails: function () {
        var record = this.getRegisterGroupDetailForm().getRecord();
        location.href = '#setup/registergroups/' + record.get('id') + '/edit';
    }*/
});
