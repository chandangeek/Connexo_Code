Ext.define('Isu.controller.ApplyIssueAction', {
    extend: 'Ext.app.Controller',

    views: [
        'Isu.view.issues.ActionView'
    ],

    showOverview: function (issueModelClass, issueId, actionId, widgetItemId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            actionModel = Ext.create(issueModelClass).actions().model,
            issueModel = me.getModel(issueModelClass),
            fromOverview = router.queryParams.fromOverview === 'true';

        actionModel.getProxy().url = issueModel.getProxy().url + '/' + issueId + '/actions';
        actionModel.load(actionId, {
            success: function (record) {
                if (record.properties().getCount() === 0) {
                    me.applyAction(null, null, record);
                } else {
                    var widget = Ext.widget('issue-action-view', {router: router, itemId: widgetItemId}),
                        form = widget.down('#issue-action-view-form'),
                        cancelLink = form.down('#issue-action-cancel');

                    cancelLink.href = router.getRoute(router.currentRoute.replace(fromOverview ? '/action' : '/view/action', '')).buildUrl();
                    me.getApplication().fireEvent('changecontentevent', widget);
                    form.loadRecord(record);
                    widget.setLoading(true);

                    issueModel.load(issueId, {
                        success: function (record) {
                            me.getApplication().fireEvent('issueLoad', record);
                        }
                    });
                    me.getApplication().fireEvent('issueActionLoad', record);
                    //todo: this definitely should be refactored. BE should send action button translation instead of this splitting
                    if (form.title === 'Close issue' || form.title === 'Notify user' || form.title === 'Assign issue') {
                        form.down('#issue-action-apply').setText(form.title.split(' ')[0]);
                    }
                    widget.setLoading(false);

                }
            }
        });
    },

    applyAction: function (button, action, actionRecord) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            viewport = Ext.ComponentQuery.query('viewport')[0],
            fromOverview = router.queryParams.fromOverview === 'true',
            recordCallback;

        recordCallback = {
            callback: function (model, operation, success) {
                var responseText = Ext.decode(operation.response.responseText, true);
                if (responseText) {
                    if (success) {
                        if (responseText.actions[0].success) {
                            me.getApplication().fireEvent('acknowledge', responseText.actions[0].message);
                            router.getRoute(router.currentRoute.replace(fromOverview ? '/action' : '/view/action', '')).forward();
                        } else {
                            me.getApplication().getController('Uni.controller.Error').showError(form.getRecord().get('name'), responseText.actions[0].message);
                        }
                    } else if (operation.response.status === 400) {
                        if (responseText.errors && !actionRecord) {
                            errorPanel.show();
                            basicForm.markInvalid(responseText.errors);
                        }
                    }
                }
                viewport.setLoading(false);
            }
        };

        viewport.setLoading(true);

        if (!!actionRecord) {
            actionRecord.save(recordCallback);
        } else {
            var form = me.getForm(),
                basicForm = form.getForm(),
                errorPanel = form.down('#issue-action-view-form-errors');

            errorPanel.hide();
            basicForm.clearInvalid();
            form.updateRecord();
            form.getRecord().save(recordCallback);
        }
    }
});