Ext.define('Usr.controller.GroupPrivileges', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.model.BreadcrumbItem'
    ],
    stores: [
        'Privileges',
        'Groups'
    ],
    models: [
        'Privilege',
        'Group'
    ],
    views: [
        'group.Edit'
    ],
    refs: [
        {
            ref: 'selectPrivilegesGrid',
            selector: 'groupEdit #selectPrivileges'
        },
        {
            ref: 'breadCrumbs',
            selector: 'breadcrumbTrail'
        }
    ],

    init: function () {
        this.control({
            'groupEdit button[action=save]': {
                click: this.saveGroup
            }
        });
    },

      showOverview: function (record, type) {
        var widget = Ext.widget('groupEdit'),
            form = widget.down('form');
        this.getApplication().fireEvent('changecontentevent', widget);

        form.loadRecord(record);

        this.operation = type;

        var editHeader = Ext.getCmp('els_usm_groupEditHeader');
        var title = '';
        if(type == 'create'){
            title = Uni.I18n.translate('group.create', 'USM', 'Create role');
            editHeader.update('<h1>' + title + '</h1>');
            this.displayBreadcrumb(title);
        }
        else{
            title = Uni.I18n.translate('group.edit', 'USM', 'Edit role');
            editHeader.update('<h1>' + title + ' "' + record.get('name') + '"' + '</h1>');
            this.displayBreadcrumb(title + ' "' + record.get("name") + '"');
            form.down('[name=name]').disable();
            form.down('[name=description]').disable();
        }

        var me = this;
        this.getPrivilegesStore().load(function () {
            me.resetPrivilegesForRecord(record);
        });
    },

    displayBreadcrumb: function (current) {
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('user.root', 'USM', 'User Management'),
            href: '#'
        });
        var breadcrumbChild1 = Ext.create('Uni.model.BreadcrumbItem', {
            text: Uni.I18n.translate('group.title', 'USM', 'Roles'),
            href: 'roles'
        });

        var editForm = Ext.getCmp('els_usm_groupEditForm'),
            record=editForm.getRecord();

        var breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem', {
            text: current
        });

        breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2);

        this.getBreadCrumbs().setBreadcrumbItem(breadcrumbParent);
    },

    saveGroup: function (button) {
        var form = button.up('form'),
            record = form.getRecord(),
            values = form.getValues(),
            privileges = this.getSelectPrivilegesGrid().store.data.items;

        var selected = [];
        for (var i = 0; i < privileges.length; i++) {
            if(privileges[i].data.selected){
                selected.push(privileges[i]);
            }
        }

        if (this.operation == 'create') {
            record = Ext.create(Usr.model.Group);
        }

        record.set(values);
        record.privileges().removeAll();
        record.privileges().add(selected);

        var me=this;
        record.save({
            success: function (record) {
                var widget = Ext.widget('groupBrowse');
                me.getApplication().fireEvent('changecontentevent', widget);
            },
            failure: function(record,operation){
                var json = Ext.decode(operation.response.responseText);
                if (json && json.errors) {
                    form.markInvalid(json.errors);
                }
            }
        });

        //record.commit();

    },
    /*saveSuccess: function () {
        alert('Saved');
    },
    saveFailed: function () {
        alert('Failed');
    },*/
    resetPrivilegesForRecord: function (record) {
        var privileges = this.getPrivilegesStore(),
            currentPrivileges = [];

        if(record){
            currentPrivileges = record.privileges().data.items;
        }

        var availablePrivileges = privileges.data.items;

        for (var i = 0; i < currentPrivileges.length; i++) {
             var privilegeName = currentPrivileges[i].data.name;
             var result = privileges.getById(privilegeName);
             if(result){
                result.data.selected = true;
             }
         }

        var selectedStore = this.getSelectPrivilegesGrid().store;
        selectedStore.removeAll();
        selectedStore.add(availablePrivileges);
    }
});