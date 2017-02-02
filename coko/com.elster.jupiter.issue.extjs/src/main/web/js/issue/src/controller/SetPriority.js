Ext.define('Isu.controller.SetPriority', {
    extend: 'Ext.app.Controller',
    views: [
        'Isu.view.issues.SetPriority'
    ],

    refs: [
        {
            ref: 'priorityForm',
            selector: 'issue-set-priority form'
        }
    ],

    init: function () {
        var me = this;
        me.control({
            'issue-set-priority button[action=savePriority]': {
                click: this.savePriority
            }
        });
    },

    setPriority: function (issueId) {
        var me = this,
                viewport = Ext.ComponentQuery.query('viewport')[0],
                router = me.getController('Uni.controller.history.Router'),
                fromDetails = router.queryParams.details === 'true',
                issueType = router.queryParams.issueType,
                issueModel,
                queryParamsForBackUrl = fromDetails ? router.queryParams : null;

        var widget = Ext.widget('issue-set-priority', {
            router: router,
            returnLink: router.getRoute(router.currentRoute.replace(fromDetails ?'/setpriority': '/view/setpriority', '')).buildUrl()
        });
        viewport.setLoading();

        if (issueType == 'datacollection') {
            issueModel = me.getModel('Idc.model.Issue');
        } else if (issueType == 'datavalidation') {
            issueModel = me.getModel('Idv.model.Issue');
        } else {
            issueModel = me.getModel(me.issueModel);
        }

        issueModel.load(issueId, {
            success: function (issue) {
                viewport.setLoading(false);
                widget.down('form').loadRecord(issue);
                me.getApplication().fireEvent('changecontentevent', widget);
                me.getApplication().fireEvent('issueLoad', issue);
            },
            failure: function (response) {
                viewport.setLoading(false);
            }
        });
    },

    savePriority: function () {
        var me = this,
            form = me.getPriorityForm(),
            returnUrl = form.returnLink,
            router = me.getController('Uni.controller.history.Router'),
            issueType = router.queryParams.issueType,
            forAlarm = issueType === 'alarm',
            record,
            updatedData,
            acknowledgeMessage;

        form.updateRecord();
        record = form.getRecord();

        updatedData = {
            id:record.get('id'),
            priority:
            {
                urgency:record.get('urgency'),
                impact:record.get('impact')
            }
        };

        if (forAlarm) {
            updatedData.alarm =
            {
                title: record.get('title'),
                version:record.get('version')
            }
        } else {
            updatedData.issue =
            {
                title: record.get('title'),
                version:record.get('version')
            }
        };

        acknowledgeMessage = (forAlarm ? Uni.I18n.translate('issue.dal.setpriority.success', 'ISU', 'Alarm priority changed'):
                                         Uni.I18n.translate('issue.isu.setpriority.success', 'ISU', 'Issue priority changed')
                             );

        Ext.Ajax.request({
            url: '/api/' + (forAlarm ? 'dal/':'isu/') + record.get('id') + '/priority',
            method: 'PUT',
            jsonData: Ext.encode(updatedData),
            success: function () {
                me.getApplication().fireEvent('acknowledge', acknowledgeMessage);
                window.location.assign(returnUrl);
            },
            failure: function (response) {
                var json = Ext.decode(response.responseText),
                    baseForm = form.getForm();
                if (json && json.errors && baseForm) {
                    baseForm.markInvalid(json.errors);
                }
            }
        });
    }
});