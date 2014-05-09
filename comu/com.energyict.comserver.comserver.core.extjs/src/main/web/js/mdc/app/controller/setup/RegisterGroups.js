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
        'setup.registergroup.RegisterGroupEdit'
    ],

    stores: [
        'RegisterGroups',
        'RegisterTypes',
        'AvailableRegisterTypesForRegisterGroup'
    ],

    refs: [
        {ref: 'registerTypeGrid', selector: '#registertypegrid'},
        {ref: 'registerGroupGrid', selector: '#registerGroupGrid'},
        {ref: 'registerGroupPreviewForm', selector: '#registerGroupPreviewForm'},
        {ref: 'registerGroupPreview', selector: '#registerGroupPreview'},
        {ref: 'registerGroupPreviewTitle', selector: '#registerGroupPreviewTitle'},
        {ref: 'registerGroupEditView', selector: '#registerGroupEdit'},
        {ref: 'registerGroupEditForm', selector: '#registerGroupEditForm'},
        {ref: 'registerEditEmptyGrid', selector: '#registerEditEmptyGrid'},
        {ref: 'breadCrumbs', selector: 'breadcrumbTrail'},
        {ref: 'registerGroupPreviewDetails', selector: '#registerGroupPreviewDetails'},
        {ref: 'registerTypePreviewForm', selector: '#registerTypePreviewForm'},
        {ref: 'registerTypePreview', selector: '#registerTypePreview'},
        {ref: 'registerTypePreviewTitle', selector: '#registerTypePreviewTitle'},
        {ref: 'readingTypeDetailsForm', selector: '#readingTypeDetailsForm'},
        {ref: 'registerGroupGridContainer', selector: '#registerGroupGridContainer'},
        {ref: 'registerGroupEmptyGrid', selector: '#registerGroupEmptyGrid'},
        {ref: 'registerTypeEmptyGrid', selector: '#registerTypeEmptyGrid'}
    ],

    init: function () {
        this.getRegisterGroupsStore().on('load', this.onStoreLoad, this);

        this.control({
            '#editRegisterGroupGridField': {
                selectionchange: this.checkboxChanged
            },
            '#editRegisterGroupGridField actioncolumn': {
                showReadingTypeInfo: this.showReadingType
            },
            '#registertypegrid': {
                selectionchange: this.previewRegisterType
            },
            '#registerGroupGrid': {
                selectionchange: this.previewRegisterGroup
            },
            '#registerGroupSetup breadcrumbTrail': {
                afterrender: this.overviewBreadCrumb
            },
            '#registerGroupGrid actioncolumn': {
                editItem: this.editRegisterGroupHistory
            },
            '#registerGroupSetup button[action = createRegisterGroup]': {
                click: this.createRegisterGroupHistory
            },
            '#registerGroupPreview menuitem[action=editRegisterGroup]': {
                click: this.editRegisterGroupHistoryFromPreview
            },
            '#registerGroupEdit button[action = createRegisterType]': {
                click: this.createRegisterTypeHistory
            },
            '#createEditButton[action=createRegisterGroup]': {
                click: this.saveRegisterGroup
            },
            '#createEditButton[action=saveRegisterGroup]': {
                click: this.saveRegisterGroup
            },
            '#editEmptyPreviewButton[action=editRegisterGroup]': {
                click: this.editRegisterGroupHistoryFromPreview
            }

        });
    },

    showRegisterGroups: function (grid, record) {
        var widget = Ext.widget('registerGroupSetup');
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
    },

    onStoreLoad: function () {
        if(this.getRegisterGroupsStore().data.items.length == 0){
            this.getRegisterGroupPreview().hide();
        }
    },

    previewRegisterGroup: function (grid, record) {
        var registerGroups = this.getRegisterGroupGrid().getSelectionModel().getSelection();
        if (registerGroups.length == 1) {
            var me=this;
            var registerTypesOfRegisterGroup = Ext.data.StoreManager.lookup('AvailableRegisterTypesForRegisterGroup');

            registerTypesOfRegisterGroup.getProxy().setExtraParam('registerGroup', registerGroups[0].get('id'));
            registerTypesOfRegisterGroup.load({
                callback: function (store) {
                    me.getRegisterTypeGrid().store.loadData(this.data.items);
                    me.getRegisterGroupPreview().getLayout().setActiveItem(1);
                    if(this.data.items.length > 0){
                        me.getRegisterTypeEmptyGrid().getLayout().setActiveItem('gridContainer');
                        me.getRegisterGroupPreviewTitle().update('<b>' + Uni.I18n.translate('registerGroup.previewGroup', 'MDC', 'Register types of') + ' ' + registerGroups[0].get('name') + '</b><br>' +
                            Ext.String.format(Uni.I18n.translate('registerGroup.previewCount', 'MDC', '{0} register types'), this.data.items.length));
                        me.getRegisterTypePreview().getLayout().setActiveItem(0);
                    }
                    else{
                        me.getRegisterTypeEmptyGrid().getLayout().setActiveItem('emptyContainer');
                        me.getRegisterGroupPreviewTitle().update('<b>' + Uni.I18n.translate('registerGroup.previewGroup', 'MDC', 'Register types of') + ' ' + registerGroups[0].get('name') + '</b>');
                        me.getRegisterTypePreview().hide();
                    }
                    me.getRegisterTypeEmptyGrid().setVisible(true);
                }
            });
        } else {
            this.getRegisterGroupPreview().getLayout().setActiveItem(0);
        }
    },

    previewRegisterType: function (grid, record) {
        var registerTypes = this.getRegisterTypeGrid().getSelectionModel().getSelection();
        if (registerTypes.length == 1) {
            this.getRegisterTypePreviewForm().loadRecord(registerTypes[0]);
            this.getRegisterTypePreview().getLayout().setActiveItem(1);
            this.getRegisterTypePreviewTitle().update('<b>' + registerTypes[0].get('name') + '</b>');
        } else {
            this.getRegisterTypePreview().getLayout().setActiveItem(0);
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

    createRegisterTypeHistory: function () {
        location.href = '#setup/registertypes/create';
    },

    showRegisterGroupEditView: function (registerGroupId) {
        var widget = Ext.widget('registerGroupEdit', {
            edit: true
        });
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
        widget.setLoading(true);
        var me = this;
        Ext.ModelManager.getModel('Mdc.model.RegisterGroup').load(registerGroupId, {
            success: function (registerGroup) {
                me.getStore('Mdc.store.RegisterTypes').load({
                    callback: function (registerTypes) {
                        me.editBreadCrumb(registerGroup.get('name'));
                        widget.down('form').loadRecord(registerGroup);
                        widget.down('#registerGroupEditCreateTitle').update('<H1>' + registerGroup.get('name') + ' > ' + Uni.I18n.translate('general.edit', 'MDC', 'Edit') + ' ' + Uni.I18n.translate('registerGroup.registerGroup', 'MDC', 'Register group') + '</H1>');
                        if(this.data.items.length > 0){
                            me.getRegisterEditEmptyGrid().getLayout().setActiveItem('gridContainer');
                            widget.down('#editRegisterGroupGridField').getSelectionModel().doSelect(registerGroup.registerTypes().data.items);
                            widget.down('#editRegisterGroupGridField').store.add(registerTypes);
                        }
                        else{
                            widget.down('#editRegisterGroupSelectedField').hide();
                            widget.down('#editRegisterGroupNameField').hide();
                            widget.down('#btnsContainer').hide();
                            widget.down('#separator').hide();
                            me.getRegisterEditEmptyGrid().getLayout().setActiveItem('emptyContainer');
                        }
                        me.getRegisterEditEmptyGrid().setVisible(true);
                        widget.setLoading(false);
                    }
                });
            }
        });
    },

    showRegisterGroupCreateView: function () {
        var widget = Ext.widget('registerGroupEdit', {
            edit: false
        });
        this.getApplication().getController('Mdc.controller.Main').showContent(widget);
        widget.setLoading(true);
        widget.hide();
        var me = this;
        me.getStore('Mdc.store.RegisterTypes').load({
            params: { start: 0, limit: 2},
            callback: function (registerTypes) {
                me.createBreadCrumb();
                var registerGroup = Ext.create(Ext.ModelManager.getModel('Mdc.model.RegisterGroup'));
                widget.down('form').loadRecord(registerGroup);
                widget.down('#registerGroupEditCreateTitle').update('<H1>' + Uni.I18n.translate('registerGroup.create', 'MDC', 'Create register group') + '</H1>');
                if(this.data.items.length > 0){
                    me.getRegisterEditEmptyGrid().getLayout().setActiveItem('gridContainer');
                    widget.down('#editRegisterGroupSelectedField').setValue(Ext.String.format(Uni.I18n.translate('registerGroup.selectedRegisterTypes', 'MDC', '{0} register types selected'), 0));
                    widget.down('#editRegisterGroupGridField').store.add(registerTypes);
                }
                else{
                    widget.down('#editRegisterGroupSelectedField').hide();
                    widget.down('#editRegisterGroupNameField').hide();
                    widget.down('#btnsContainer').hide();
                    widget.down('#separator').hide();
                    me.getRegisterEditEmptyGrid().getLayout().setActiveItem('emptyContainer');
                    me.getRegisterEditEmptyGrid().setMargin('0 0 0 10');
                }
                me.getRegisterEditEmptyGrid().setVisible(true);
                widget.show();
                widget.setLoading(false);
            }
        });
    },

    checkboxChanged: function (grid, selected) {
        grid.view.up('#registerGroupEdit').down('#editRegisterGroupSelectedField').setValue(Ext.String.format(Uni.I18n.translate('registerGroup.selectedRegisterTypes', 'MDC', '{0} register types selected'), selected.length));
    },

    saveRegisterGroup: function () {
        var form = this.getRegisterGroupEditForm(),
            record = form.getRecord(),
            values = form.getValues(),
            selected = form.down('#editRegisterGroupGridField').getSelectionModel().getSelection();

        record.set(values);
        record.registerTypes().removeAll();
        record.registerTypes().add(selected);

        var me=this;
        record.save({
            success: function (record) {
                location.href = form.down('#cancelLink').autoEl.href
            },
            failure: function(record,operation){
                var json = Ext.decode(operation.response.responseText);
                if (json && json.errors) {
                    me.getRegisterGroupEditForm().getForm().markInvalid(json.errors);
                }
            }
        });
    },

    showReadingType: function (record) {
        var widget = Ext.widget('readingTypeDetails');
        this.getReadingTypeDetailsForm().loadRecord(record.getReadingType());
        widget.show();
    },

    overviewBreadCrumb: function (breadcrumbs) {
        var breadcrumbChild = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registerGroup.registerGroups', 'MDC', 'Register groups'),
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
            text: Uni.I18n.translate('registerGroup.registerGroups', 'MDC', 'Register groups'),
            href: 'registergroups'
        });
        var breadcrumb3 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registerGroup.create', 'MDC', 'Create register group'),
            href: 'create'
        });
        breadcrumb1.setChild(breadcrumb2).setChild(breadcrumb3);
        this.getBreadCrumbs().setBreadcrumbItem(breadcrumb1);
    },

    editBreadCrumb: function (registerGroupName) {
        var breadcrumb1 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('general.administration', 'MDC', 'Administration'),
            href: '#setup'
        });
        var breadcrumb2 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registerGroup.registerGroups', 'MDC', 'Register groups'),
            href: 'registergroups'
        });
        var breadcrumb4 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('registerGroup.edit', 'MDC', 'Edit register group'),
            href: 'edit'
        });
        breadcrumb1.setChild(breadcrumb2).setChild(breadcrumb4);
        this.getBreadCrumbs().setBreadcrumbItem(breadcrumb1);
    }
});
