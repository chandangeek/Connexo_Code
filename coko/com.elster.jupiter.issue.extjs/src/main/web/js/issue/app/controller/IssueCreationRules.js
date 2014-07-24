Ext.define('Isu.controller.IssueCreationRules', {
    extend: 'Ext.app.Controller',

    requires: [
    ],

    stores: [
        'Isu.store.CreationRule'
    ],
    views: [
        'Isu.view.administration.datacollection.issuecreationrules.Overview',
        'Isu.view.ext.button.GridAction',
        'Isu.view.administration.datacollection.issuecreationrules.ActionMenu',
        'Isu.view.workspace.issues.MessagePanel'
    ],

    mixins: {
        isuGrid: 'Isu.util.IsuGrid'
    },

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
                select: this.loadGridItemDetail
            },
            'issue-creation-rules-overview issues-creation-rules-list gridview': {
                refresh: this.setAssigneeTypeIconTooltip
            },
            'issues-creation-rules-list uni-actioncolumn': {
                menuclick: this.chooseAction
            },
            'creation-rule-action-menu': {
                click: this.chooseAction
            },
            'issue-creation-rules-overview button[action=create]': {
                click: this.createRule
            }
        });

        this.gridItemModel = this.getModel('Isu.model.CreationRule');
    },

    showOverview: function () {
        var widget = Ext.widget('issue-creation-rules-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    chooseAction: function (menu, item) {
        var action = item.action;
        var id = menu.record.getId();
        var router = this.getController('Uni.controller.history.Router');

        switch (action) {
            case 'delete':
                this.showDeleteConfirmation(menu.record);
                break;
            case 'edit':
                router.getRoute('administration/creationrules/edit').forward({id: id});
                break;
        }
    },

    createRule: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute('administration/creationrules/add').forward();
    },

    showDeleteConfirmation: function (rule) {
        var self = this;

        Ext.create('Uni.view.window.Confirmation').show({
            msg: Uni.I18n.translate('administration.issueCreationRules.deleteConfirmation.msg', 'ISE', 'This issue creation rule will disappear from the list.<br>Issues will not be created automatically by this rule.'),
            title: Ext.String.format(Uni.I18n.translate('administration.issueCreationRules.deleteConfirmation.title', 'ISE', 'Delete rule "{0}"?'), rule.get('name')),
            config: {
                me: self,
                rule: rule
            },
            fn: function (state) {
                switch (state) {
                    case 'confirm':
                        this.close();
                        self.deleteRule(rule);
                        break;
                    case 'cancel':
                        this.close();
                        break;
                }
            }
        });
    },

    deleteRule: function (rule) {
        var self = this,
            store = this.getStore('Isu.store.CreationRule'),
            page = this.getPage();

        page.setLoading('Removing...');
        rule.destroy({
            params: {
                version: rule.get('version')
            },
            callback: function (model, operation) {
                page.setLoading(false);
                if (operation.response.status == 204) {
                    store.loadPage(1);
                    self.getApplication().fireEvent('acknowledge', Uni.I18n.translate('administration.issueCreationRules.deleteSuccess.msg', 'ISE', 'Issue creation rule deleted'));
                }
            }
        });
    }
});
