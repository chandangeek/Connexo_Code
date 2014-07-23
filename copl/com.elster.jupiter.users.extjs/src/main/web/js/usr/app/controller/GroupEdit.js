Ext.define('Usr.controller.GroupEdit', {
    extend: 'Ext.app.Controller',
    requires: [
    ],
    stores: [
        'Usr.store.Privileges',
        'Usr.store.Groups',
        'Usr.store.Applications'
    ],
    models: [
        'Usr.model.Privilege',
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
            'groupEdit #featureList actioncolumn': {
                privilegeAllow: this.allowPrivilege,
                privilegeDeny: this.denyPrivilege
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
            }
        });
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

        widget.setLoading(true);
        panel.setTitle(title);

        me.getStore('Usr.store.Privileges').load(function () {
            widget.down('form').loadRecord(record);
            var currentPrivileges = record.privileges().data.items;
            for(var i=0; i<currentPrivileges.length; i++){
                var field = this.findRecord('name', currentPrivileges[i].data.name);
                if(field){
                    field.set('selected', true);
                }
            }
            this.commitChanges();
            me.onPrivilegesStoreLoad();
            widget.setLoading(false);

            me.getApplication().getController('Usr.controller.Main').showContent(widget);
        });
    },

    onPrivilegesStoreLoad: function () {
        var privileges = Ext.data.StoreManager.lookup('Usr.store.Privileges'),
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
            record.set('rights', value);
            applications.add(record);
        }
        applications.commitChanges();
    },

    checkRights: function (record, features) {
        var fullAccess = true,
            noAccess = true;

        for(var i=0; i<features.length && (noAccess || fullAccess); i++){
            if(features[i].data.selected){
                noAccess = false;
            }
            else{
                fullAccess = false;
            }
        }

        var value = 1;
        if(noAccess){
            value = 0;
        }
        if(fullAccess){
            value = 2;
        }
        return value;
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
            if(features.data.items[i].get('selected')){
                record.privilegesStore.add(features.data.items[i])
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

    allowPrivilege: function (record) {
        this.updatePrivilege(record, true);
    },

    denyPrivilege: function (record) {
        this.updatePrivilege(record, false);
    },

    updatePrivilege: function (record, value) {
        record.set('selected', value);
        this.updateApplication();
    },

    updateApplication: function () {
        var record = this.getSelectApplicationsGrid().getSelectionModel().getSelection()[0],
            value = this.checkRights(record, this.getSelectFeaturesGrid().getStore().data.items);
        record.set('rights', value);
    },

    applicationNoAccess: function (record) {
        this.updateApplicationPrivileges(record, false);
    },

    applicationFullControl: function (record) {
        this.updateApplicationPrivileges(record, true);
    },

    updateApplicationPrivileges: function (record, value) {
        var store = this.getSelectFeaturesGrid().getStore();
        for(var i=0; i<store.count(); i++){
            store.data.items[i].set('selected', value);
        }
        if(value){
            record.set('rights', 2);
        }
        else{
            record.set('rights', 0);
        }
    },

    systemNoAccess: function () {
        this.updateAllPrivileges(false);
    },

    systemFullControl: function () {
        this.updateAllPrivileges(true);
    },

    updateAllPrivileges: function (value) {
        var features = this.getSelectFeaturesGrid().getStore();
        features.clearFilter(true);

        for(var i=0; i<features.count(); i++){
            features.data.items[i].set('selected', value);
        }

        var application = this.getSelectApplicationsGrid().getSelectionModel().getSelection()[0];
        if(application){
            features.filter('componentName', application.get('componentName'));
        }

        var applications = this.getSelectApplicationsGrid().getStore();
        for(var i=0; i<applications.data.length; i++){
            var record = applications.data.get(i);
            if(value){
                record.set('rights', 2);
            }
            else{
                record.set('rights', 0);
            }
        }
        this.getSelectApplicationsGrid().getView().refresh();
    }
});