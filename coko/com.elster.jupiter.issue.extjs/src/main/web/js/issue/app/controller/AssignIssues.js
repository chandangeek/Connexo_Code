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
                'issues-assign breadcrumbTrail': {
                    afterrender: this.setBreadcrumb
                }
            });
            this.getApplication().on('assignissue', this.onSubmitForm)
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


        onSubmitForm: function () {
            var self = this.getController('Mtr.controller.AssignIssues'),
                assignPanel = Ext.ComponentQuery.query('issues-assign')[0],
                formPanel = assignPanel.down('issues-assign-form'),
                activeCombo = formPanel.down('combobox[disabled=false]'),
                form = formPanel.getForm(),
                formValues = form.getValues(),
                url = '/api/isu/issue/assign',
                sendingData = {},
                preloader;

            if (form.isValid()) {
                sendingData.issues = [
                    {
                        id: assignPanel.record.data.id,
                        version: assignPanel.record.data.version
                    }
                ];
                sendingData.assignee = {
                    id: activeCombo.findRecordByValue(activeCombo.getValue()).data.id,
                    type: activeCombo.name,
                    title: activeCombo.rawValue
                };
                sendingData.comment = formValues.comment;
                preloader = Ext.create('Ext.LoadMask', {
                    msg: "Assigning issue",
                    name: 'assign-issu-form-submit',
                    target: formPanel
                });
                preloader.show();
                Ext.Ajax.request({
                    url: url,
                    method: 'PUT',
                    jsonData: sendingData,
                    success: self.handleServerResponse,
                    controller: this
                });
            }
        },

        handleServerResponse: function (resp, param) {
            var response = Ext.JSON.decode(resp.responseText),
                successArr = response.success,
                failureArr = response.failure,
                assignIssueView = Ext.ComponentQuery.query('issues-assign')[0],
                preloader = Ext.ComponentQuery.query('loadmask[name=assign-issu-form-submit]')[0],
                activeCombo = assignIssueView.down('issues-assign-form combobox[disabled=false]'),
                id = assignIssueView.record.data.id,
                success,
                msges = [];

            preloader.destroy();
            success = Ext.Array.findBy(successArr, function (item) {
                return item == id;
            });
            if (success) {
                console.log(success)
            }
            if (failureArr.length > 0) {
                Ext.Array.each(failureArr, function (item) {
                    Ext.Array.each(item.issues, function (issue) {
                        var header = {},
                            bodyItem = {};
                        header.text = 'Failed to assign issue ' + issue.title + ' to ' + activeCombo.rawValue;
                        header.style = 'msgHeaderStyle';
                        msges.push(header);
                        bodyItem.text = item.reason;
                        bodyItem.style = 'msgItemStyle';
                        msges.push(bodyItem);
                    })
                });

                if (msges.length > 0) {
                    param.controller.getApplication().fireEvent('isushowmsg', {
                        type: 'error',
                        closeBtn: true,
                        msgBody: msges,
                        y: 10,
                        btns: [
                            {
                                text: 'Retry',
                                hnd: function () {
                                    param.controller.getApplication().fireEvent('assignissue')
                                }
                            },
                            {
                                text: 'Cancel',
                                cls: 'isu-btn-link',
                                // this function is necessary and MUST be empty
                                hnd: function () {

                                }
                            }
                        ]
                    });
                }
            }

            msges = [];

            if (successArr.length > 0) {
                Ext.Array.each(successArr, function () {
                    var header = {};
                    header.style = 'msgHeaderStyle';
                    header.text = 'Issue assigned to ' + activeCombo.rawValue;
                    msges.push(header);
                });

                if (msges.length > 0) {
                    param.controller.getApplication().fireEvent('isushowmsg', {
                        type: 'notify',
                        msgBody: msges,
                        y: 10,
                        showTime: 5000
                    });
                }
                Ext.History.back();
            }
        }
    }
);