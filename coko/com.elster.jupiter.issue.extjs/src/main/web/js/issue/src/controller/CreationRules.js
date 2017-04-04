/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.controller.CreationRules', {
    extend: 'Ext.app.Controller',

    stores: [
        'Isu.store.CreationRules'
    ],

    views: [
        'Isu.view.creationrules.Overview'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'issue-creation-rules-overview'
        },
        {
            ref: 'itemPanel',
            selector: 'issue-creation-rules-overview issue-creation-rules-item'
        },
        {
            ref: 'rulesGridPagingToolbarTop',
            selector: 'issue-creation-rules-overview issues-creation-rules-list pagingtoolbartop'
        }
    ],

    init: function () {
        this.control({
            'issue-creation-rules-overview issues-creation-rules-list': {
                select: this.showPreview
            },
            'creation-rule-action-menu': {
                click: this.chooseAction
            },
            'issue-creation-rules-overview button[action=create]': {
                click: this.createRule
            }
        });
    },

    showOverview: function () {
        var widget = Ext.widget('issue-creation-rules-overview');
        this.getApplication().fireEvent('changecontentevent', widget);

    },

    showPreview: function (selectionModel, record) {
        var me = this,
            itemPanel = this.getItemPanel(),
            form = itemPanel.down('form'),
            menu = itemPanel.down('menu');

        Ext.suspendLayouts();
        me.setupMenuItems(record);
        form.loadRecord(record);
        itemPanel.setTitle(Ext.String.htmlEncode(record.get('title')));
        if (menu) {
            menu.record = record;
        }
        Ext.resumeLayouts(true);
    },

    chooseAction: function (menu, item) {
        var me = this;
        var action = item.action;
        var id = menu.record.getId();
        var router = this.getController('Uni.controller.history.Router');

        switch (action) {
            case 'remove':
                this.showDeleteConfirmation(menu.record);
                break;
            case 'edit':
                router.getRoute('administration/creationrules/edit').forward({id: id});
                break;
            case 'activate':
                router.setState(router.getRoute()); // store the current url
                me.activateRule(menu.record);
                break;
        }
    },

    setupMenuItems: function (record) {
        var suspended = record.get('active'),
            menuText = suspended
                ? Uni.I18n.translate('administration.issueCreationRules.deactivate', 'ISU', 'Deactivate')
                : Uni.I18n.translate('administration.issueCreationRules.activate', 'ISU', 'Activate'),
            menuItems = Ext.ComponentQuery.query('menu menuitem[action=activate]');
        if (!Ext.isEmpty(menuItems)) {
            Ext.Array.each(menuItems, function (item) {
                item.setText(menuText);
            });
        }
    },

    createRule: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('administration/creationrules/add').forward();
    },

    activateRule: function (record) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            suspended = record.data.active;

        var action = ((suspended == true) ? 'deactivate' : 'activate');

        Ext.Ajax.request({
            url: '/api/isu/creationrules/' + record.data.id + '/' + action,
            method: 'PUT',
            jsonData: Ext.encode(record.raw),
            success: function () {
                var messageText = suspended
                    ? Uni.I18n.translate('administration.issueCreationRules.deactivateSuccessMsg', 'ISU', 'Issue creation rule deactivated')
                    : Uni.I18n.translate('administration.issueCreationRules.activateSuccessMsg', 'ISU', 'Issue creation rule activated');
                me.getApplication().fireEvent('acknowledge', messageText);
                router.getState().forward(); // navigate to the previously stored url
            },
            failure: function (response) {
                if (response.status == 400) {
                    var errorText = Uni.I18n.translate('administration.issueCreationRules.error.unknown', 'ISU', 'Unknown error occurred');
                    if (!Ext.isEmpty(response.statusText)) {
                        errorText = response.statusText;
                    }
                    if (!Ext.isEmpty(response.responseText)) {
                        var json = Ext.decode(response.responseText, true);
                        if (json && json.error) {
                            errorText = json.error;
                        }
                    }
                    var titleText = suspended
                        ? Uni.I18n.translate('administration.issueCreationRules.deactivate.operation.failed', 'ISU', 'Deactivate operation failed')
                        : Uni.I18n.translate('administration.issueCreationRules.activate.operation.failed', 'ISU', 'Activate operation failed');

                    me.getApplication().getController('Uni.controller.Error').showError(titleText, errorText);
                }
            }
        });
    },

    showDeleteConfirmation: function (rule) {
        var me = this;

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('administration.issueCreationRules.deleteConfirmation.message', 'ISU', 'This issue creation rule will disappear from the list. Issues will not be created automatically by this rule.'),
            title: Uni.I18n.translate('administration.issueCreationRules.deleteConfirmation.title', 'ISU', "Remove rule '{0}'?", [rule.get('name')]),
            config: {
                me: me,
                rule: rule
            },
            fn: function (state) {
                switch (state) {
                    case 'confirm':
                        me.deleteRule(rule);
                        break;
                    case 'cancel':
                        break;
                }
            }
        });
    },

    deleteRule: function (rule) {
        var me = this,
            store = this.getStore('Isu.store.CreationRules'),
            page = this.getPage();

        page.setLoading('Removing...');
        rule.destroy({
            success: function () {
                page.down('#creation-rules-list pagingtoolbartop').totalCount = 0;
                page.down('#creation-rules-list pagingtoolbarbottom').resetPaging();
                store.loadPage(1);
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('administration.issueCreationRules.deleteSuccess.msg', 'ISU', 'Issue creation rule removed'));
            },
            callback: function () {
                page.setLoading(false);
            }
        });
    }
});