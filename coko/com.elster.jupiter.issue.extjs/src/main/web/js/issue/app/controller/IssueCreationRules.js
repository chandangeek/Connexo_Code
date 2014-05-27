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
            'issue-creation-rules-overview issues-creation-rules-list gridview': {
                itemclick: this.loadGridItemDetail,
                refresh: this.onGridRefresh
            },
            'issues-creation-rules-list uni-actioncolumn': {
                menuclick: this.chooseAction
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

    onGridRefresh: function (grid) {
        this.setAssigneeTypeIconTooltip(grid);
        this.setDescriptionTooltip(grid);
        this.selectFirstGridRow(grid);
    },

    chooseAction: function (menu, item) {
        var action = item.action;
        var id = menu.record.getId();

        switch (action) {
            case 'delete':
                this.deleteRule(menu.record);
                break;
            case 'edit':
                window.location.href = '#/administration/issuecreationrules/' + id + '/edit';
                break;
        }
    },

    createRule: function () {
        window.location.href = '#/administration/issuecreationrules/create';
    },

    deleteRule: function (rule) {
        var self = this,
            store = self.getStore('Isu.store.CreationRule'),
            confirmMessage = Ext.widget('messagebox', {
                buttons: [
                    {
                        text: 'Delete',
                        ui: 'delete',
                        handler: function () {
                            rule.destroy({
                                params: {
                                    version: rule.data.version
                                },
                                callback: function (model, operation) {
                                    confirmMessage.close();
                                    if (operation.response.status == 204) {
                                        self.getRulesGridPagingToolbarTop().totalCount = 0;
                                        store.loadPage(1);
                                        self.getApplication().fireEvent('isushowmsg', {
                                            type: 'notify',
                                            msgBody: [
                                                {
                                                    style: 'msgHeaderStyle',
                                                    text: 'Issue creation rule deleted'
                                                }
                                            ],
                                            y: 10,
                                            showTime: 5000
                                        });
                                    }
                                }
                            });
                        }
                    },
                    {
                        text: 'Cancel',
                        ui: 'link',
                        handler: function () {
                            confirmMessage.close();
                        }
                    }
                ]
            });

        confirmMessage.show({
            title: 'Delete issue creation rule',
            msg: '<p><b>Delete rule "' + rule.data.name + '"?</b></p><p>This issue creation rule will disappear from the list.<br>Issues will not be created automatically by this rule.</p>',
            icon: Ext.MessageBox.WARNING,
            cls: 'isu-delete-message'
        });
    }
});
