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
//    refs: [
//        {
//            ref: 'selectApplicationsGrid',
//            selector: 'groupEdit #applicationList'
//        },
//        {
//            ref: 'selectFeaturesGrid',
//            selector: 'groupEdit #featureList'
//        }
//    ],

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
            'groupEdit #featureList': {
                beforecellmousedown: this.displayPermissionsMenu
            }
        });
    },

    getSelectApplicationsGrid: function () {
        return this.widget.down("#applicationList");
    },

    getSelectFeaturesGrid: function () {
        return this.widget.down("#featureList");
    },

    backUrl: '#usermanagement/roles',

    back: function () {
        location.href = this.backUrl;
    },

    showEditOverviewWithHistory: function(groupId) {
        location.href = '#usermanagement/roles/' + groupId + '/edit';
    },

    showEditOverview: function (groupId) {
        var me = this;
        me.mode = 'edit';
        Ext.ModelManager.getModel('Usr.model.Group').load(groupId, {
            success: function (group) {
                me.showOverview(group, Uni.I18n.translate('group.edit', 'USM', 'Edit') + ' \'' + group.get('name') + '\'');
            }
        });
    },

    showCreateOverviewWithHistory: function (groupId) {
        location.href = '#usermanagement/roles/add';
    },

    showCreateOverview: function () {
        this.mode = 'create';
        this.showOverview(Ext.create('Usr.model.Group'), Uni.I18n.translate('group.create', 'USM', 'Add role'));
    },

    showOverview: function (record, title) {
        var me = this,
            widget = Ext.widget('groupEdit', {edit: (me.mode == 'edit')}),
            panel = widget.getCenterContainer().items.getAt(0);

        this.widget = widget;
        widget.setLoading(true);
        panel.setTitle(title);

        var name = '',
            previousPermissions = '';
        me.getStore('Usr.store.Resources').load(function () {
            widget.down('form').loadRecord(record);
            var allPrivileges, currentPrivileges = record.privileges(), index=0;
            for(var i=0; i<this.data.items.length; i++){
                allPrivileges = this.data.items[i].privileges();
                for(var j=0; j<allPrivileges.data.items.length; j++){
                    index = currentPrivileges.indexOf(allPrivileges.data.items[j]);
                    if(index >= 0){
                        allPrivileges.data.items[j].set('selected', true);

                        previousPermissions = this.data.items[i].data.permissions;
                        if(previousPermissions){
                            previousPermissions += ', ';
                        }
                        name = currentPrivileges.data.items[index].data.name;
                        this.data.items[i].set('permissions',  previousPermissions + Uni.I18n.translate(name, 'USM', name));
                        this.data.items[i].set('selected', this.data.items[i].data.selected + 1);
                    }
                }
            }

            this.commitChanges();
            me.onPrivilegesStoreLoad();
            widget.setLoading(false);

            me.getApplication().getController('Usr.controller.Main').showContent(widget);
        });
    },

    onPrivilegesStoreLoad: function () {
        var privileges = Ext.data.StoreManager.lookup('Usr.store.Resources'),
            applications = Ext.data.StoreManager.lookup('Usr.store.Applications');

        privileges.clearFilter(true);
        var groups = privileges.getGroups();
        if(applications.count()){
            applications.removeAll();
        }

        for (var i=0; i<groups.length; i++){
            var record = Ext.create(Ext.ModelManager.getModel('Usr.model.Application'));
            record.set('componentName', groups[i].name);
            var value = this.checkRights(record, groups[i].children);
            record.set('selected', value);
            applications.add(record);
        }
        applications.commitChanges();
    },

    checkRights: function (record, features) {
        var fullAccess = true,
            noAccess = true;

        for(var i=0; i<features.length && (noAccess || fullAccess); i++){
            if(features[i].data.selected > 0){
                noAccess = false;
            }
            if(features[i].data.selected < features[i].privileges().data.items.length){
                fullAccess = false;
            }
        }

        if(noAccess){
            return 0;
        }
        if(fullAccess){
            return 2;
        }
        return 1;
    },

    selectFeatureList: function () {
        if(this.getSelectApplicationsGrid().getStore().count() > 0){
            this.getSelectApplicationsGrid().getSelectionModel().doSelect(0);
        }
    },

    refreshFeatureList: function (grid, record) {
        var features = this.getSelectFeaturesGrid().getStore();
        features.clearFilter(true);
        if(record.length > 0){
            features.filter('componentName', record[0].get('componentName'));
        }
    },

    saveGroup: function (button) {
        var me = this;
        var form = button.up('form');

        form.updateRecord();
        var record = form.getRecord();

        record.privilegesStore.removeAll();
        var features = this.getSelectFeaturesGrid().getStore();
        features.clearFilter(true);

        for(var i=0; i<features.count(); i++){
            var privileges = features.data.items[i].privileges();
            for(var j=0; j<privileges.data.items.length; j++){
                if(privileges.data.items[j].get('selected')){
                    record.privilegesStore.add(privileges.data.items[j]);
                }
            }
        }

        record.save({
            success: function (record) {
                var message;
                if(me.mode == 'edit'){
                    message = Uni.I18n.translatePlural('group.saved', record.get('name'), 'USM', 'Role \'{0}\' saved.');
                }
                else{
                    message = Uni.I18n.translatePlural('group.added', record.get('name'), 'USM', 'Role \'{0}\' added.');
                }
                me.getApplication().fireEvent('acknowledge', message);
                me.back();
            },
            failure: function (record, operation) {
                var json = Ext.decode(operation.response.responseText);
                if (json && json.errors) {
                    form.markInvalid(json.errors);
                }
            }
        });
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
        record.set('selected', value?2:0);
    },

    updateAllApplicationFeatures: function (store, value) {
        var permissions = '',
            name = '';
        for(var i=0; i<store.count(); i++){
            var privileges = store.data.items[i].privileges();
            permissions = '';


            for(var j=0; j<privileges.data.items.length; j++){
                privileges.data.items[j].data.selected = value;
                if(value){
                    if(permissions != ''){
                        permissions += ', ';
                    }
                    name = privileges.data.items[j].data.name;
                    permissions += Uni.I18n.translate(name, 'USM', name);
                }
            }
            store.data.items[i].set('permissions', permissions);
            store.data.items[i].set('selected', value?privileges.data.items.length:0);
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
        if(application){
            features.filter('componentName', application.get('componentName'));
        }

        var applications = this.getSelectApplicationsGrid().getStore();
        for(var i=0; i<applications.data.length; i++){
            var record = applications.data.get(i);
            record.set('selected', value?2:0);
        }
        this.getSelectApplicationsGrid().getView().refresh();
    },

    addPermissionMenuNoAccess: function (menu, selected) {
        menu.add({
            xtype: 'menucheckitem',
            text: Uni.I18n.translate('privilege.noAccess', 'USM', 'No access'),
            checked: selected,
            listeners:{
                checkchange: function(item, checked){
                    if(checked){
                        var menu = item.up('menu');
                        for(var i=1; i<menu.items.length; i++){
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
            text: Uni.I18n.translate('privilege.fullControl', 'USM', 'Full control'),
            checked: selected,
            listeners:{
                checkchange: function(item, checked){
                    if(checked){
                        var menu = item.up('menu');
                        menu.items.items[0].setChecked(false);
                        for(var i=1; i<menu.items.length-1; i++){
                            menu.items.items[i].setChecked(true);
                        }
                    }
                }
            }
        });
    },

    addPermissionMenuItem: function (menu, name, code, selected) {
        menu.add(
            {
                xtype: 'menucheckitem',
                text: Uni.I18n.translate(name, 'USM', name),
                code: code,
                checked: selected,
                listeners:{
                    checkchange: function(item, checked){
                        var panel = item.up('menu');
                        if(checked){
                            panel.items.items[0].setChecked(false);
                        }
                        else{
                            panel.items.items[panel.items.length-1].setChecked(false);
                        }
                    }
                }
            }
        );
    },

    displayPermissionsMenu: function (grid, td, cellIndex, record, tr, rowIndex) {
        var lastColumn = grid.panel.columns.length - 1,
            privileges = record.privileges().data.items;


        if(cellIndex == lastColumn){
            var menu = grid.panel.columns[lastColumn].menu;
            menu.removeAll();

            this.addPermissionMenuNoAccess(menu, (record.get('permissions') == ''));
            for(var i=0; i<privileges.length; i++){
                this.addPermissionMenuItem(menu, privileges[i].data.name, privileges[i].data.id, privileges[i].data.selected);
            }
            this.addPermissionMenuFullControl(menu, (record.get('selected') == privileges.length));

            var me=this;
            menu.on('beforehide', function (panel) {
                var text = '',
                    total = 0,
                    selRecord = me.getSelectFeaturesGrid().getSelectionModel().getSelection()[0];

                for(var i=1; i<panel.items.length-1; i++){
                    if(panel.items.items[i].checked){
                        if(text != ''){
                            text += ', ';
                        }
                        text += panel.items.items[i].text;
                        selRecord.privileges().data.items[i-1].data.selected = true;
                        total += 1;
                    }
                    else{
                        selRecord.privileges().data.items[i-1].data.selected = false;
                    }
                }
                if(selRecord.data.permissions != text){
                    selRecord.set('permissions', text);
                    selRecord.set('selected', total);
                }
                me.updateApplicationRow();
            });
        }
        return true;
    }
});