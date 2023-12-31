/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
        var me = this;
        this.getApplication().getController('Usr.controller.GroupEdit').showEditOverviewWithHistory(record.get('id'));
        if(record.get('canEdit')) {
            this.getApplication().fireEvent('editRole', record);
        }
    },

    removeGroupMenu: function (button) {
        var record = button.up('#groupBrowse').down('#groupDetailsForm').getRecord();
        this.removeGroup(record);
    },

    removeGroup: function (record) {
        var me = this,
            groupGrid = me.getGroupList();

        confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
            confirmText: Uni.I18n.translate('general.remove', 'USR', 'Remove')
        }).show({
            title: Ext.String.format(Uni.I18n.translate('role.remove.title', 'USR', 'Remove \'{0}\'?'), Ext.htmlEncode(record.get('name'))),
            msg: Uni.I18n.translate('role.remove.message', 'USR', 'Any user with this role will lose the associated privileges unless they are associated with other roles for the concerned user.'),
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
                            groupGrid.setLoading(false);
                        }
                    });
                }
            }
        });

    },

    selectGroup: function (selectionModel, record) {
        var me = this,
            page = me.getGroupBrowse(),
            form = page.down('#groupDetailsForm'),
            detailsPanel = page.down('groupDetails'),
            detailsPanelActionButton = detailsPanel.down('uni-button-action'),
            groupActionMenu = detailsPanel.down('group-action-menu'),
            canEditRecord = record.get('canEdit');

        detailsPanel.setTitle(Ext.String.htmlEncode(record.get('name')));
        if (groupActionMenu) {
            groupActionMenu.record = record;
        }
        form.loadRecord(record);

        if (!Ext.isEmpty(detailsPanelActionButton)) {
            detailsPanelActionButton.setDisabled(!canEditRecord);
            if (!canEditRecord) {
                // I agree, the next 4 lines are rather dirty code, but I didn't manage to get the same result via (S)CSS
                detailsPanelActionButton.btnIconEl.dom.style.color = '#d4d4d4';
                detailsPanelActionButton.btnInnerEl.dom.style.color = '#d4d4d4';
                detailsPanelActionButton.btnWrap.dom.classList.remove('x-btn-arrow-right');
                detailsPanelActionButton.btnWrap.dom.classList.remove('x-btn-arrow');
                detailsPanelActionButton.setTooltip(Uni.I18n.translate('role.uneditable', 'USR', 'No actions available because this role is managed internally by Connexo.'));
            } else {
                // I agree, the next 4 lines are rather dirty code, but I didn't manage to get the same result via (S)CSS
                detailsPanelActionButton.btnIconEl.dom.style.color = '#ffffff';
                detailsPanelActionButton.btnInnerEl.dom.style.color = '#ffffff';
                detailsPanelActionButton.btnWrap.dom.classList.add('x-btn-arrow-right');
                detailsPanelActionButton.btnWrap.dom.classList.add('x-btn-arrow');
                detailsPanelActionButton.setTooltip(null);
            }
        }
        detailsPanel.doLayout();
    }
});