Ext.define('Mtr.controller.AssignIssues', {
        extend: 'Ext.app.Controller',

        stores: [
            'Mtr.store.UserList',
            'Mtr.store.UserRoleList',
            'Mtr.store.UserGroupList'
        ],

        views: [
            'workspace.issues.Assign'
        ],

        refs: [
            {
                ref: 'itemPanel',
                selector: 'issues-item'
            }
        ],

        init: function () {
            this.control({
                'issues-assign button[name=assign]': {
                    click: this.onSubmitForm
                },
                'issues-assign button[name=cancel]': {
                    click: this.onCancel
                },
                'issues-assign breadcrumbTrail': {
                    afterrender: this.setBreadcrumb
                }
            });
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
                    text: 'Issues',
                    href: 'issues'
                }),
                breadcrumbChild3 = Ext.create('Uni.model.BreadcrumbItem', {
                    text: 'Assign Issue',
                    href: 'assignissue'
                });
            breadcrumbParent.setChild(breadcrumbChild1).setChild(breadcrumbChild2).setChild(breadcrumbChild3);

            breadcrumbs.setBreadcrumbItem(breadcrumbParent);
        },

        onSubmitForm: function (button) {
            var self = this,
                formPanel = button.up('form'),
                form = formPanel.getForm(),
                formValues = form.getValues(),
                url = '/api/isu/issue/assign',
                preloader;

            formPanel.sendingData.comment = formValues.comment;

            if (form.isValid()) {
                preloader = Ext.create('Ext.LoadMask', {
                    msg: "Assigning issue",
                    name: 'assign-issu-form-submit',
                    target: formPanel
                });
                preloader.show();
                Ext.Ajax.request({
                    url: url,
                    method: 'PUT',
                    jsonData: formPanel.sendingData,
                    success: self.handleServerResponse,
                    app: this.getApplication()
                });
            }
        },

        onCancel: function () {
            Ext.History.back();
        },

        handleServerResponse: function (resp, param) {
            var response = Ext.JSON.decode(resp.responseText),
                successArr = response.success,
                failureArr = response.failure,
                assignIssueView = Ext.ComponentQuery.query('issues-assign')[0],
                preloader = Ext.ComponentQuery.query('loadmask[name=assign-issu-form-submit]')[0],
                form = Ext.ComponentQuery.query('issues-assign form')[0],
                id = assignIssueView.record.data.id,
                success,
                msges = [];

            preloader.destroy();

            success = Ext.Array.findBy(successArr, function (item) {
                return item == id;
            });

            if (success) {

            }

            Ext.Array.each(failureArr, function (item) {
                var header = {};
                header.text = item.reason;
                header.style = 'msgHeaderStyle';
                msges.push(header);
                Ext.Array.each(item.issues, function (issue) {
                    var bodyItem = {};
                    bodyItem.text = issue.title;
                    bodyItem.style = 'msgItemStyle';
                    msges.push(bodyItem);
                })
            });

            if (msges.length > 0) {
                param.app.getApplication().fireEvent('isushowmsg', {
                    type: 'error',
                    closeBtn: true,
                    msgBody: msges,
                    y: 10
                });
            }

            msges = [];
            Ext.Array.each(successArr, function (item) {
                var header = {};
                header.style = 'msgHeaderStyle';
                header.text = 'Issue assigned to ' + form.sendingData.assignee.title;
                msges.push(header);
            });

            if (msges.length > 0) {
                param.app.getApplication().fireEvent('isushowmsg', {
                    type: 'notify',
                    msgBody: msges,
                    y: 10,
                    showTime: 5000
                });
            }

            Ext.History.back();
        }

    }
);