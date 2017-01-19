Ext.define('Usr.controller.GroupEdit', {
    extend: 'Ext.app.Controller',

    requires: [
        'Usr.view.group.privilege.FeatureActionMenu'
    ],

    stores: [
        'Usr.store.Resources',
        'Usr.store.Groups',
        'Usr.store.Applications'
    ],

    models: [
        'Usr.model.Resource',
        'Usr.model.Group',
        'Usr.model.Application'
    ],

    views: [
        'Usr.view.group.Edit'
    ],

    refs: [
        {
            ref: 'selectApplicationsGrid',
            selector: 'groupEdit #applicationList'
        },
        {
            ref: 'editPage',
            selector: 'groupEdit'
        },
        {
            ref: 'selectFeaturesGrid',
            selector: 'groupEdit #featureList'
        }
    ],

    init: function () {
        this.control({
            'groupEdit button[action=save]': {
                click: this.saveGroup
            },
            'groupEdit button[action=cancel]': {
                click: this.back
            },
            'groupEdit #applicationList': {
                selectionchange: this.refreshFeatureList,
                afterrender: this.selectFeatureList
            },
            'groupEdit #applicationList actioncolumn': {
                privilegeNoAccess: this.applicationNoAccess,
                privilegeFullControl: this.applicationFullControl
            },
            'groupEdit #applicationList button[action = privilegesNoAccess]': {
                click: this.systemNoAccess
            },
            'groupEdit #applicationList button[action = privilegesFullControl]': {
                click: this.systemFullControl
            },
            'groupEdit #featureList uni-actioncolumn': {
                beforeshow: this.displayPermissionsMenu
            }
        });
    },

    backUrl: '#/administration/roles',

    back: function () {
        location.href = this.backUrl;
    },

    showEditOverviewWithHistory: function (groupId) {
        location.href = '#/administration/roles/' + groupId + '/edit';
    },

    showEditOverview: function (groupId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');
        me.mode = 'edit';
        Ext.ModelManager.getModel('Usr.model.Group').load(groupId, {
            success: function (group) {
                if(group.get('canEdit')) {
                    me.showOverview(group, Uni.I18n.translate('general.editx', 'USR', "Edit '{0}'",[group.get('name')], false));
                } else {
                    window.location.replace(router.getRoute('notfound').buildUrl());
                }
            }
        });
    },

    showCreateOverview: function () {
        this.mode = 'create';
        this.showOverview(Ext.create('Usr.model.Group'), Uni.I18n.translate('role.create', 'USR', 'Add role'));
    },

    showOverview: function (record, title) {
        var me = this;
        me.getStore('Usr.store.Resources').clearFilter();
        me.getStore('Usr.store.Resources').load({
            callback: function () {
                //me.onPrivilegesStoreLoad();
                var widget = Ext.widget('groupEdit', {edit: (me.mode == 'edit')}),
                    panel = widget.getCenterContainer().items.getAt(0),
                    name = '',
                    previousPermissions = '';
                me.widget = widget;
                widget.setLoading(true);
                panel.setTitle(title);
                if (record.get('id') == 1) {
                    panel.down('[itemId=alertmessagerole]').hidden = false;
                    panel.down('[name=name]').disable();
                    panel.down('[itemId=privilegesNoAccess]').hidden = true;
                    panel.down('[itemId=privilegesFullControl]').hidden = true;
                    panel.down('[itemId = applicationList]').columns[2].hidden = true;
                    panel.down('[itemId = featureList]').columns[3].hidden = true;
                }
                if(me.mode == 'edit'){
                    panel.down('[name=name]').disable();
                }

                widget.down('form').loadRecord(record);
                var allPrivileges, currentPrivileges = record.privileges(), index = 0;

                for (var i = 0; i < this.data.items.length; i++) {
                    allPrivileges = this.data.items[i].privileges();
                    for (var j = 0; j < allPrivileges.data.items.length; j++) {
                        for(var k = 0; k < currentPrivileges.data.items.length; k++){
                            if(currentPrivileges.data.items[k].data.name ==allPrivileges.data.items[j].data.name ){
                                if(this.data.items[i].get('componentName') == currentPrivileges.data.items[k].get('applicationName')){
                                    allPrivileges.data.items[j].set('selected', true);
                                    previousPermissions = this.data.items[i].data.permissions;
                                    if (previousPermissions) {
                                        previousPermissions += ', ';
                                    }
                                    name = currentPrivileges.data.items[k].data.translatedName;
                                    this.data.items[i].set('permissions', previousPermissions + name);
                                    this.data.items[i].set('selected', this.data.items[i].data.selected + 1);
                                }
                            }
                        }
                    }
                }
                me.onPrivilegesStoreLoad();
                this.commitChanges();

                widget.setLoading(false);
                me.getApplication().getController('Usr.controller.Main').showContent(widget);
            }});
    },

    onPrivilegesStoreLoad: function () {
        var privileges = Ext.data.StoreManager.lookup('Usr.store.Resources'),
            applications = Ext.data.StoreManager.lookup('Usr.store.Applications');

        privileges.clearFilter(true);
        var groups = privileges.getGroups();
        if (applications.count()) {
            applications.removeAll();
        }

        for (var i = 0; i < groups.length; i++) {
            var record = Ext.create(Ext.ModelManager.getModel('Usr.model.Application'));
            record.beginEdit();
            record.set('componentName', groups[i].name);
            record.set('translatedName', groups[i].children[0].data.translatedName);
            var value = this.checkRights(record, groups[i].children);
            record.set('selected', value);
            record.set('sortingfield', groups[i].children[0].data.translatedName);
            record.endEdit();
            applications.add(record);
        }
        applications.commitChanges();
    },

    checkRights: function (record, features) {
        var fullAccess = true,
            noAccess = true;

        for (var i = 0; i < features.length && (noAccess || fullAccess); i++) {
            if (features[i].data.selected > 0) {
                noAccess = false;
            }
            if (features[i].data.selected < features[i].privileges().data.items.length) {
                fullAccess = false;
            }
        }

        if (noAccess) {
            return 0;
        }

        if (fullAccess) {
            return 2;
        }
        return 1;
    },

    selectFeatureList: function () {
        if (this.getSelectApplicationsGrid().getStore().count() > 0) {
            this.getSelectApplicationsGrid().getSelectionModel().doSelect(0);
        }
    },

    refreshFeatureList: function (grid, record) {
        var features = this.getSelectFeaturesGrid().getStore();
        features.clearFilter(true);
        if (record.length > 0) {
            features.filter('componentName', record[0].get('componentName'));
        }
    },

    saveGroup: function (button) {
        var me = this,
            editPageForm = me.getEditPage(),
            form = editPageForm.down('#editForm'),
            formErrorsPanel = editPageForm.down('#form-errors');

        form.updateRecord();
        var record = form.getRecord();

        if (!form.isValid()) {
            formErrorsPanel.show();
        } else {
            if (!formErrorsPanel.isHidden()) {
                formErrorsPanel.hide();
            }

            record.privilegesStore.removeAll();
            var features = this.getSelectFeaturesGrid().getStore();
            features.clearFilter(true);

            for (var i = 0; i < features.count(); i++) {
                var privileges = features.data.items[i].privileges();
                for (var j = 0; j < privileges.data.items.length; j++) {
                    if (privileges.data.items[j].get('selected')) {
                        privileges.data.items[j].set('applicationName', features.data.items[i].get('componentName'));
                        record.privilegesStore.add(privileges.data.items[j]);
                    }
                }
            }

            record.save({
                backUrl: me.backUrl,
                success: function (record) {
                    var message = (me.mode === 'edit')
                        ? Uni.I18n.translate('role.saved', 'USR', 'Role saved.')
                        : Uni.I18n.translate('role.added', 'USR', 'Role added.');
                    me.getApplication().fireEvent('acknowledge', message);
                    me.back();
                },
                failure: function (record, operation) {
                    if (operation.response.status === 400) {
                        var json = Ext.decode(operation.response.responseText);
                        if (json && json.errors) {
                            form.getForm().markInvalid(json.errors);
                            var privilegesError = Ext.Array.findBy(json.errors, function (item) { return item.id == 'privileges';});
                            if(privilegesError) {
                                form.down('#privilegesError').setText(privilegesError.msg);
                                form.down('#privilegesError').show();
                                formErrorsPanel.show();
                            }
                        }
                        formErrorsPanel.show();
                    }
                }
            });
        }
    },

    updateApplicationRow: function () {
        var record = this.getSelectApplicationsGrid().getSelectionModel().getSelection()[0],
            value = this.checkRights(record, this.getSelectFeaturesGrid().getStore().data.items);
        record.set('selected', value);
    },

    applicationNoAccess: function (record) {
        this.updateApplicationFeatures(record, false);
    },

    applicationFullControl: function (record) {
        this.updateApplicationFeatures(record, true);
    },

    updateApplicationFeatures: function (record, value) {
        var store = this.getSelectFeaturesGrid().getStore();
        this.updateAllApplicationFeatures(store, value);
        record.set('selected', value ? 2 : 0);
    },

    updateAllApplicationFeatures: function (store, value) {
        var permissions = '',
            name = '';

        for (var i = 0; i < store.count(); i++) {
            var privileges = store.data.items[i].privileges(), numberSelected = 0;
            permissions = '';

            for (var j = 0; j < privileges.data.items.length; j++) {
                if(privileges.data.items[j].data.canGrant) {
                    privileges.data.items[j].data.selected = value;
                    numberSelected++;
                    if (value) {
                        if (permissions != '') {
                            permissions += ', ';
                        }
                        name = privileges.data.items[j].data.translatedName;
                        permissions += name;
                    }
                }
            }
            store.data.items[i].set('permissions', permissions);
            store.data.items[i].set('selected', value ? numberSelected : 0);
        }
    },

    systemNoAccess: function () {
        this.updateAllFeatures(false);
    },

    systemFullControl: function () {
        this.updateAllFeatures(true);
    },

    updateAllFeatures: function (value) {
        var features = this.getSelectFeaturesGrid().getStore();
        features.clearFilter(true);

        this.updateAllApplicationFeatures(features, value);

        var application = this.getSelectApplicationsGrid().getSelectionModel().getSelection()[0];
        if (application) {
            features.filter('componentName', application.get('componentName'));
        }

        var applications = this.getSelectApplicationsGrid().getStore();
        for (var i = 0; i < applications.data.length; i++) {
            var record = applications.data.get(i);
            record.set('selected', value ? 2 : 0);
        }
        this.getSelectApplicationsGrid().getView().refresh();
    },

    addPermissionMenuNoAccess: function (menu, selected) {
        menu.add({
            xtype: 'menucheckitem',
            text: Uni.I18n.translate('privilege.noAccess', 'USR', 'No access'),
            icon: '../sky/build/resources/images/grid/drop-no.png',
            iconCls: 'x-menu-item-checkbox',
            checked: selected,
            listeners: {
                checkchange: function (item, checked) {
                    if (checked) {
                        var menu = item.up('menu');
                        for (var i = 1; i < menu.items.length; i++) {
                            menu.items.items[i].setChecked(false);
                        }
                    }
                }
            }
        });
    },

    addPermissionMenuFullControl: function (menu, selected) {
        menu.add({
            xtype: 'menucheckitem',
            text: Uni.I18n.translate('privilege.fullControl', 'USR', 'Full control'),
            icon: '../sky/build/resources/images/grid/drop-yes.png',
            iconCls: 'x-menu-item-checkbox',
            checked: selected,
            listeners: {
                checkchange: function (item, checked) {
                    if (checked) {
                        var menu = item.up('menu');
                        menu.items.items[0].setChecked(false);
                        for (var i = 1; i < menu.items.length - 1; i++) {
                            if(menu.items.items[i].canGrant) {
                                menu.items.items[i].setChecked(true);
                            }
                        }
                    }
                }
            }
        });
    },

    addPermissionMenuItem: function (menu, name, code, selected, disabled) {
        menu.add(
            {
                xtype: 'menucheckitem',
                text: name,
                code: code,
                checked: selected,
                disabled: disabled,
                listeners: {
                    checkchange: function (item, checked) {
                        var panel = item.up('menu');
                        var allChecked = true, allUnchecked = true;
                        for (var i = 1; i < menu.items.length - 1; i++) {
                            allChecked &= menu.items.items[i].checked;
                            allUnchecked &= !menu.items.items[i].checked;
                        }
                        if (checked) {
                            panel.items.items[0].setChecked(false);
                            if (allChecked) {
                                panel.items.items[panel.items.length - 1].setChecked(true);
                            }
                        }
                        else {
                            panel.items.items[panel.items.length - 1].setChecked(false);
                            if (allUnchecked) {
                                panel.items.items[0].setChecked(true);
                            }
                        }
                    }
                }
            }
        );
    },

    displayPermissionsMenu: function (grid, cell, colIndex, rowIndex, record) {
        var lastColumn = grid.panel.columns.length - 1,
            privileges = record.privileges().data.items;


        if (colIndex == lastColumn) {
            var menu = grid.panel.columns[lastColumn].menu;
            Ext.suspendLayouts();
            menu.removeAll();

            this.addPermissionMenuNoAccess(menu, (record.get('permissions') == ''));
            for (var i = 0; i < privileges.length; i++) {
                this.addPermissionMenuItem(menu, privileges[i].data.translatedName, privileges[i].data.id, privileges[i].data.selected, !privileges[i].data.canGrant);
            }
            this.addPermissionMenuFullControl(menu, (record.get('selected') == privileges.length));
            Ext.resumeLayouts();
            var me = this;
            menu.on('beforehide', function (panel) {
                var text = '',
                    total = 0,
                    selRecord = me.getSelectFeaturesGrid().getSelectionModel().getSelection()[0];

                for (var i = 1; i < panel.items.length - 1; i++) {
                    if (panel.items.items[i].checked) {
                        if (text != '') {
                            text += ', ';
                        }
                        text += panel.items.items[i].text;
                        selRecord.privileges().data.items[i - 1].data.selected = true;
                        total += 1;
                    }
                    else {
                        selRecord.privileges().data.items[i - 1].data.selected = false;
                    }
                }
                if (selRecord.data.permissions != text) {
                    selRecord.set('permissions', text);
                    selRecord.set('selected', total);
                }
                me.updateApplicationRow();
            });
        }
        return true;
    }
});