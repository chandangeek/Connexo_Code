Ext.define('Usr.controller.Group', {
    extend: 'Ext.app.Controller',
    requires: [
        'Usr.controller.GroupEdit'
    ],
    stores: [
        'Usr.store.Resources',
        'Usr.store.Groups'
    ],
    views: [
        'Usr.view.group.Browse'
    ],

    refs: [
        {ref: 'groupBrowse', selector: 'groupBrowse'},
        {ref: 'groupList', selector: 'groupBrowse groupList'},
        {ref: 'groupDetails', selector: 'groupBrowse groupDetails'}
    ],

    init: function () {
        this.control({
            'groupBrowse groupList': {
                select: this.selectGroup
            },
            'groupBrowse groupDetails menuitem[action=edit]': {
                click: this.editGroupMenu
            },
            'groupBrowse groupDetails menuitem[action=remove]': {
                click: this.removeGroupMenu
            },
            'groupBrowse groupList uni-actioncolumn': {
                edit: this.editGroup,
                remove: this.removeGroup
            },
            'group-action-menu': {
                show: this.onMenuShow
            }
        });
    },

    showOverview: function () {
        var widget = Ext.widget('groupBrowse');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    editGroupMenu: function (button) {
        var record = button.up('#groupBrowse').down('#groupDetailsForm').getRecord();
        this.editGroup(record);
    },

    editGroup: function (record) {
        this.getApplication().getController('Usr.controller.GroupEdit').showEditOverviewWithHistory(record.get('id'));
        this.getApplication().fireEvent('editRole', record);
    },

    removeGroupMenu: function (button) {
        var me = this,
            record = button.up('#groupBrowse').down('#groupDetailsForm').getRecord();

        me.removeGroup(record);
    },

    removeGroup: function (record) {
        var me = this,
            groupGrid = me.getGroupList();

        confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
            confirmText: Uni.I18n.translate('general.remove', 'USR', 'Remove')
        }).show({
            title: Ext.String.format(Uni.I18n.translate('role.remove.title', 'USR', 'Remove \'{0}\'?'), record.get('name')),
            msg: Uni.I18n.translate('role.remove.message', 'USR', 'This user role will no longer be available.'),
            fn: function (state) {
                if (state === 'confirm') {
                    groupGrid.setLoading();
                    record.destroy({
                        success: function () {
                            groupGrid.down('pagingtoolbartop').totalCount = 0;
                            groupGrid.down('pagingtoolbarbottom').resetPaging();
                            groupGrid.getStore().load();

                            groupGrid.setLoading(false);
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('role.remove.confirmation', 'USR', 'Role removed'));
                        },
                        failure: function (object, operation) {
                            view.setLoading(false);
                        }
                    });
                }
            }
        });

    },

    onMenuShow: function (menu) {
        menu.record.get('name') == 'Administrators' ? menu.down('#removeGroup').hide() : menu.down('#removeGroup').show();
    },

    selectGroup: function (selectionModel, record) {
        var me = this,
            page = me.getGroupBrowse(),
            form = page.down('#groupDetailsForm');

        page.down('groupDetails').setTitle(Ext.String.htmlEncode(record.get('name')));
        page.down('groupDetails').down('group-action-menu').record = record;
        form.loadRecord(record);

    }
});