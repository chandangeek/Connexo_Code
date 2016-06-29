Ext.define('Isu.controller.ApplyIssueAction', {
    extend: 'Ext.app.Controller',

    views: [
        'Isu.view.issues.ActionView'
    ],

    stores: [
        'Isu.store.Issues'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'issue-action-view'
        },
        {
            ref: 'form',
            selector: 'issue-action-view #issue-action-view-form'
        }
    ],

    init: function () {
        this.control({
            'issue-action-view issue-action-form #issue-action-apply': {
                click: this.applyAction
            }
        });
    },

    showOverview: function (issueId, actionId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            issueType = router.queryParams.issueType,
            actionModel = Ext.create('Isu.model.Issue').actions().model,
            issueModel,
            fromOverview = router.queryParams.fromOverview === 'true',
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            dependenciesCounter = 2,
            onAllDependenciesLoad = function () {
                var clipboard,
                    app;

                dependenciesCounter--;
                if (!dependenciesCounter) {
                    app = me.getApplication();
                    clipboard = Ext.getStore('Isu.store.Clipboard');
                    app.fireEvent('issueActionLoad', actionRecord);
                    app.fireEvent('issueLoad', issueRecord);
                    mainView.setLoading(false);
                    if (actionRecord.properties().getCount() === 0) {
                        me.applyAction(null, null, actionRecord, clipboard.get('issue') || issueRecord);
                        clipboard.clear('issue');
                    } else {
                        var widget = Ext.widget('issue-action-view', {router: router}),
                            form = widget.down('#issue-action-view-form'),
                            cancelLink = form.down('#issue-action-cancel'),
                            queryParamsForCancel = fromOverview ? router.queryParams : null;

                        cancelLink.href = router.getRoute(router.currentRoute.replace(fromOverview ? '/action' : '/view/action', '')).buildUrl(null, queryParamsForCancel);
                        app.fireEvent('changecontentevent', widget);
                        form.loadRecord(actionRecord);
                        form.issue = issueRecord;

                        //todo: this definitely should be refactored. BE should send action button translation instead of this splitting
                        if (form.title === 'Close issue' || form.title === 'Notify user' || form.title === 'Assign issue') {
                            form.down('#issue-action-apply').setText(form.title.split(' ')[0]);
                        }
                    }
                }
            },
            actionRecord,
            issueRecord;

        mainView.setLoading();
        actionModel.getProxy().url = '/api/isu/issues/' + issueId + '/actions';
        actionModel.load(actionId, {
            success: function (record) {
                actionRecord = record;
                onAllDependenciesLoad();
            }
        });

        if (issueType == 'datacollection') {
            issueModel = me.getModel('Idc.model.Issue');
        } else if (issueType == 'datavalidation') {
            issueModel = me.getModel('Idv.model.Issue');
        }
        issueModel.load(issueId, {
            success: function (record) {
                issueRecord = record;
                onAllDependenciesLoad();
            }
        });
    },

    applyAction: function (button, action, actionRecord, issueRecord) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            fromOverview = router.queryParams.fromOverview === 'true',
            queryParamsForBackUrl = fromOverview ? router.queryParams : null,
            backUrl = router.getRoute(router.currentRoute.replace(fromOverview ? '/action' : '/view/action', '')).buildUrl(null, queryParamsForBackUrl),
            record,
            requestOptions;

        requestOptions = {
            backUrl: backUrl,
            success: function (record, operation) {
                var responseText = Ext.decode(operation.response.responseText, true);

                if (responseText.actions[0].success) {
                    me.getApplication().fireEvent('acknowledge', responseText.actions[0].message);
                    window.location.href = backUrl;
                } else {
                    me.getApplication().getController('Uni.controller.Error').showError(actionRecord.get('issue').title, responseText.actions[0].message);
                }
            },
            failure: function (record, operation) {
                var responseText = Ext.decode(operation.response.responseText, true);

                if (operation.response.status === 400 && responseText.errors && !actionRecord) {
                    errorPanel.show();
                    basicForm.markInvalid(responseText.errors);
                }
                if (operation.response.status === 200 && responseText.actions) {
                    window.location.href = backUrl;
                    me.getApplication().getController('Uni.controller.Error').showError(actionRecord.get('issue').title, responseText.actions[0].message);
                }
            },
            callback: function () {
                mainView.setLoading(false);
            }
        };

        mainView.setLoading();

        if (!!actionRecord) {
            actionRecord.set('issue', _.pick(issueRecord.getData(), 'title', 'version'));
            actionRecord.save(requestOptions);
        } else {
            var form = me.getForm(),
                basicForm = form.getForm(),
                errorPanel = form.down('#issue-action-view-form-errors');

            errorPanel.hide();
            basicForm.clearInvalid();
            form.updateRecord();
            record = form.getRecord();
            record.set('issue', _.pick(form.issue.getData(), 'title', 'version'));
            record.save(requestOptions);
        }
    }
});