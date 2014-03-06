Ext.define('Mtr.controller.IssueAssignmentRules', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mtr.store.AssignmentRules'
    ],
    views: [
        'workspace.datacollection.issueassignmentrules.Overview',
        'ext.button.GridAction'
    ],

    refs: [
        {
            ref: 'itemPanel',
            selector: 'issue-assignment-rules-overview issues-assignment-rules-item'
        }
    ],

    init: function () {
        this.control({
            'issue-assignment-rules-overview breadcrumbTrail': {
                afterrender: this.setBreadcrumb
            },
            'issue-assignment-rules-overview issues-assignment-rules-list gridview': {
                itemclick: this.loadRule,
                itemmouseenter: this.onUserTypeIconHover
            },
            'issue-assignment-rules-overview issues-assignment-rules-list actioncolumn': {
                click: this.showRuleActions
            },
            'menu[name=ruleactionmenu]': {
                beforehide: this.hideRuleActions,
                click: this.chooseRuleAction
            },
            'button[name=create-issues-assignment-rules]': {
                click: this.createRule
            }
        });

    },

    showOverview: function () {
        var widget = Ext.widget('issue-assignment-rules-overview');
        this.getApplication().fireEvent('changecontentevent', widget);
    },

    setBreadcrumb: function (breadcrumbs) {
        var breadcrumbParent = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Workspace',
                href: '#/workspace'
            }),
            breadcrumbChild1 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Data collection',
                href: 'datacollection'
            }),
            breadcrumbChild2 = Ext.create('Uni.model.BreadcrumbItem', {
                text: 'Issue assignment rules',
                href: 'assignmentrules'
            });
        breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2);

        breadcrumbs.setBreadcrumbItem(breadcrumbParent);
    },

    loadRule: function (grid, record) {
        var itemPanel = this.getItemPanel(),
            model = this.getModel('Mtr.model.Rules'),
            preloader = Ext.create('Ext.LoadMask', {
                msg: "Loading...",
                target: itemPanel
            });
        if ((this.lastId != undefined) && (this.lastId != record.id)) {
            grid.clearHighlight();
            preloader.show();
        }
        this.lastId = record.id;
        /*model.load(record.data.id, {
         success: function () {
         itemPanel.fireEvent('change', itemPanel, record);
         preloader.destroy();
         }
         });*/

        /*=========== TEMP ===========*/
        setTimeout(function () {
            var data = {};
            data.data = {
                "id": 1,
                "name": "When smth then assign to smbd",
                "priority": 1,
                "status": "active",
                "when": [
                    { "field": "customer", "value": [1], "op": "EQ"},
                    { "field": "reason", "value": [1, 2, 3], "op": "EQ"}
                ],
                "assignee": {
                    "id": 3,
                    "type": "ROLE",
                    "title": "Meter operator"
                },
                "version": 1
            };

            itemPanel.fireEvent('change', itemPanel, data);
            preloader.destroy();
        }, 1000);
    },

    onUserTypeIconHover: function (me, record, item) {
        var rowEl = Ext.get(item),
            iconElem = rowEl.select('span').elements[0],
            toolTip;
        if (iconElem) {
            var icon = iconElem.getAttribute('class'),
                domIconElem = Ext.get(iconElem);
            switch (icon) {
                case 'isu-icon-USER':
                    toolTip = Ext.create('Ext.tip.ToolTip', {
                        target: domIconElem,
                        html: 'User',
                        style: {
                            borderColor: 'black'
                        }
                    });
                    break;
                case 'isu-icon-GROUP':
                    toolTip = Ext.create('Ext.tip.ToolTip', {
                        target: domIconElem,
                        html: 'User group',
                        style: {
                            borderColor: 'black'
                        }
                    });
                    break;
                case 'isu-icon-ROLE':
                    toolTip = Ext.create('Ext.tip.ToolTip', {
                        target: domIconElem,
                        html: 'User role',
                        style: {
                            borderColor: 'black'
                        }
                    });
                    break;
                default:
                    break;
            }
            domIconElem.on('mouseenter', function () {
                toolTip.show();
            });
            domIconElem.on('mouseleave', function () {
                toolTip.hide();
            });
        }
    },

    showRuleActions: function (grid, cell, rowIndex, colIndex, e, record) {
        var cellEl = Ext.get(cell);

        this.hideRuleActions();

        this.gridActionIcon = cellEl.first();

        this.gridActionIcon.hide();
        this.gridActionIcon.setHeight(0);
        this.gridActionBtn = Ext.create('widget.grid-action', {
            renderTo: cell,
            menu: {
                xtype: 'rule-action-menu',
                name: 'ruleactionmenu',
                record: record
            }
        });
        this.gridActionBtn.showMenu();
    },

    hideRuleActions: function () {
        if (this.gridActionBtn) {
            this.gridActionBtn.destroy();
        }

        if (this.gridActionIcon) {
            this.gridActionIcon.show();
            this.gridActionIcon.setHeight(22);
        }
    },

    chooseRuleAction: function (menu, item) {
        switch (item.text) {
            case 'Enable':
                break;
            case  'Disable':
                break;
            case  'Edit':
                break;
            case  'Delete':
                break;
            case  'Move up':
                break;
            case  'Move down':
                break;
        }
    },

    createRule: function () {

    }
});
