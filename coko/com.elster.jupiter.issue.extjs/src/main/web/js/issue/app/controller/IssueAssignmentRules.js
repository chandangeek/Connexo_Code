Ext.define('Mtr.controller.IssueAssignmentRules', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mtr.store.AssignmentRules'
    ],
    views: [
        'workspace.datacollection.issueassignmentrules.Overview'
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
                itemclick: this.loadRule
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
    }
});
