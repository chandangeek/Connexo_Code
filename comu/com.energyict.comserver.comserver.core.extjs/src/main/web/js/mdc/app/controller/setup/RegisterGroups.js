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
        'RegisterGroups',
        'RegisterTypes'
    ],

    refs: [
        {ref: 'registerTypeGrid', selector: '#registertypegrid'},
        {ref: 'registerGroupGrid', selector: '#registergroupgrid'},
        {ref: 'registerGroupPreviewForm', selector: '#registerGroupPreviewForm'},
        {ref: 'registerGroupPreview', selector: '#registerGroupPreview'},
        {ref: 'registerGroupPreviewTitle', selector: '#registerGroupPreviewTitle'},
        {ref: 'registerGroupEditView', selector: '#registerGroupEdit'},
        {ref: 'registerGroupEditForm', selector: '#registerGroupEditForm'},
        {ref: 'breadCrumbs', selector: 'breadcrumbTrail'},
        {ref: 'registerGroupPreviewGrid', selector: '#registerGroupPreviewGrid'},
        {ref: 'registerGroupPreviewDetails', selector: '#registerGroupPreviewDetails'},
        {ref: 'registerTypePreviewForm', selector: '#registerTypePreviewForm'},
        {ref: 'registerTypePreview', selector: '#registerTypePreview'},
        {ref: 'registerTypePreviewTitle', selector: '#registerTypePreviewTitle'},
        {ref: 'previewMrId', selector: '#preview_mrid'}
    ],

    init: function () {
        this.control({
            'checkcolumn': {
                checkchange: this.checkboxChanged
            },
            '#registertypegrid': {
                selectionchange: this.previewRegisterType
            },
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
                click: this.saveRegisterGroup
            },
            '#createEditButton[action=saveRegisterGroup]': {
                click: this.saveRegisterGroup
            }
        });
    },

    showRegisterGroups: function (grid, record) {
        var me = this;
        this.getRegisterGroupsStore().load(
            {
                callback: function () {
                    var widget = Ext.widget('registerGroupSetup');
                    me.getApplication().getController('Mdc.controller.Main').showContent(widget);
                }
            });
    },

    previewRegisterGroup: function (grid, record) {
        var registerGroups = this.getRegisterGroupGrid().getSelectionModel().getSelection();
        if (registerGroups.length == 1) {
            this.getRegisterGroupPreviewForm().loadRecord(registerGroups[0]);
            this.getRegisterGroupPreview().getLayout().setActiveItem(1);
            this.getRegisterGroupPreviewTitle().update('<h4>' + registerGroups[0].get('name') + '</h4>');

            this.getRegisterTypePreview().getLayout().setActiveItem(0);
        } else {
            this.getRegisterGroupPreview().getLayout().setActiveItem(0);
        }
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

    createRegisterGroupHistory: function () {
        location.href = '#setup/registergroups/create';
    },

    editRegisterGroupHistory: function (item) {
        location.href = '#setup/registergroups/' + item.get('id') + '/edit';
    },

    editRegisterGroupHistoryFromPreview: function () {
        location.href = '#setup/registergroups/' + this.getRegisterGroupGrid().getSelectionModel().getSelection()[0].get('id') + '/edit';
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
                        widget.down('#registerGroupEditCreateTitle').update('<H2>' + registerGroup.get('name') + ' > ' + Uni.I18n.translate('general.edit', 'MDC', 'Edit') + ' ' + Uni.I18n.translate('registerGroup.registerGroup', 'MDC', 'Register group') + '</H2>');
                        var selectedRegisterTypes = registerGroup.registerTypes().data.items;
                        for (var i = 0; i < selectedRegisterTypes.length; i++) {
                            var result = registerTypes.getById(selectedRegisterTypes[i].id);
                            if(result){
                                result.data.selected = true;
                            }
                        }
                        widget.down('#editRegisterGroupSelectedField').setValue(Ext.String.format(Uni.I18n.translate('registerGroup.registerTypes', 'MDC', '{0} register types selected'), selectedRegisterTypes.length));
                        widget.down('#editRegisterGroupGridField').store.add(registerTypes);
                        widget.checked = selectedRegisterTypes.length;
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
        var me = this;
        me.getStore('Mdc.store.RegisterTypes').load({
            callback: function (registerTypes) {
                me.createBreadCrumb();
                var registerGroup = Ext.create(Ext.ModelManager.getModel('Mdc.model.RegisterGroup'));
                widget.down('form').loadRecord(registerGroup);
                widget.down('#registerGroupEditCreateTitle').update('<H2>' + Uni.I18n.translate('registerGroup.create', 'MDC', 'Create register group') + '</H2>');
                widget.down('#editRegisterGroupSelectedField').setValue(Ext.String.format(Uni.I18n.translate('registerGroup.registerTypes', 'MDC', '{0} register types selected'), 0));
                widget.down('#editRegisterGroupGridField').store.add(registerTypes);
                widget.setLoading(false);
            }
        });
    },

    checkboxChanged: function (grid, rowIndex, checked) {
        var groupEditForm = grid.up('#registerGroupEdit');
        if(checked){
            groupEditForm.checked++;
        }
        else{
            groupEditForm.checked--;
        }
        groupEditForm.down('#editRegisterGroupSelectedField').setValue(Ext.String.format(Uni.I18n.translate('registerGroup.registerTypes', 'MDC', '{0} register types selected'), groupEditForm.checked));
    },

    saveRegisterGroup: function () {
        var form = this.getRegisterGroupEditForm(),
            record = form.getRecord(),
            values = form.getValues(),
            //name = form.down('#editRegisterGroupNameField').getValue(),
            registerTypes = form.down('#editRegisterGroupGridField').store.data.items;

        var selected = [];
        for (var i = 0; i < registerTypes.length; i++) {
            if(registerTypes[i].data.selected){
                selected.push(registerTypes[i]);
            }
        }

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
                    form.markInvalid(json.errors);
                }
            }
        });
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
