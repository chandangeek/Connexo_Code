Ext.define('Isu.controller.ApplyIssueAction', {
    extend: 'Ext.app.Controller',

    views: [
        'Isu.view.issues.ActionView'
    ],

    mixins: [
        'Isu.util.CreatingControl'
    ],

    showOverview: function (issueModelClass, issueId, actionId, widgetItemId) {
        var me = this,
            widget = Ext.widget('issue-action-view', {
                router: me.getController('Uni.controller.history.Router'),
                itemId: widgetItemId
            }),
            form = widget.down('#issue-action-view-form'),
            issueModel = me.getModel(issueModelClass),
            actionModel = Ext.create(issueModelClass).actions().model;

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading(true);

        issueModel.load(issueId, {
            success: function (record) {
                me.getApplication().fireEvent('issueLoad', record);
            }
        });

        actionModel.getProxy().url = issueModel.getProxy().url + '/' + issueId + '/actions';
        actionModel.load(actionId, {
            success: function (record) {
                me.getApplication().fireEvent('issueActionLoad', record);
                form.setTitle(record.get('name'));
                Ext.Object.each(record.get('parameters'), function(key, value) {
                    var formItem = me.createControl(value);
                    formItem && form.add(formItem);
                });
                form.loadRecord(record);
                widget.setLoading(false);
            }
        });
    },

    applyAction: function () {
        var me = this,
            page = me.getPage(),
            form = me.getForm(),
            basicForm = form.getForm(),
            errorPanel = form.down('#issue-action-view-form-errors'),
            router = me.getController('Uni.controller.history.Router');

        errorPanel.hide();
        basicForm.clearInvalid();
        form.updateRecord();
        page.setLoading(true);
        form.getRecord().save({
            callback: function (model, operation, success) {
                var responseText = Ext.decode(operation.response.responseText, true);

                page.setLoading(false);
                if (responseText) {
                    if (success) {
                        if (responseText.data.actions[0].success) {
                            me.getApplication().fireEvent('acknowledge', responseText.data.actions[0].message);
                            router.getRoute(router.currentRoute.replace('/action', '')).forward();
                        } else {
                            me.getApplication().getController('Uni.controller.Error').showError(form.getRecord().get('name'), responseText.data.actions[0].message);
                        }
                    } else if (operation.response.status === 400) {
                        if (responseText.errors) {
                            errorPanel.show();
                            basicForm.markInvalid(responseText.errors);
                        }
                    }
                }
            }
        });
    }
});