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

    showEditOverviewWithHistory: function(groupId) {
        location.href = '#roles/' + groupId + '/edit';
    },

    showEditOverview: function (groupId) {
        var groupStore = Ext.StoreManager.get('Groups');
        var widget = Ext.widget('groupEdit');

        widget.setLoading(true);
        var me = this;
        Ext.ModelManager.getModel('Usr.model.Group').load(groupId, {
            success: function (group) {
                groupStore.load({
                    callback: function (store) {
                        title = Uni.I18n.translate('group.edit', 'USM', 'Edit role');
                        widget.down('form').loadRecord(group);
                        widget.down('#els_usm_groupEditHeader').update('<h1>' + title + ' "' + group.get('name') + '"' + '</h1>');
                        widget.down('[name=name]').disable();
                        //widget.down('[name=description]').disable();
                        me.displayBreadcrumb(title + ' "' + group.get("name") + '"');

                        me.getPrivilegesStore().load(function () {
                            me.resetPrivilegesForRecord(group);
                        });

                        widget.setLoading(false);
                    }
                })
            }
        });

        widget.down('#cancelLink').autoEl.href = this.getApplication().getHistoryGroupController().tokenizePreviousTokens();

        this.getApplication().fireEvent('changecontentevent', widget);
    },

    showCreateOverviewWithHistory: function(groupId) {
        location.href = '#roles/create';
    },

    showCreateOverview: function () {
        var widget = Ext.widget('groupEdit');

        widget.setLoading(true);
        title = Uni.I18n.translate('group.create', 'USM', 'Create role');
        widget.down('#els_usm_groupEditHeader').update('<h1>' + title + '</h1>');
        this.displayBreadcrumb(title);

        var record = Ext.create(Usr.model.Group);
        widget.down('form').loadRecord(record);

        var me = this;
        this.getPrivilegesStore().load(function () {
            me.resetPrivilegesForRecord(record);
        });

        widget.setLoading(false);

        widget.down('#cancelLink').autoEl.href = this.getApplication().getHistoryGroupController().tokenizePreviousTokens();

        this.getApplication().fireEvent('changecontentevent', widget);
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

        record.set(values);
        record.privileges().removeAll();
        record.privileges().add(selected);

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