/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.controller.CreationManualRule', {
    extend: 'Ext.app.Controller',

    stores: [
        'Isu.store.IssueDevices',
        'Isu.store.IssueReasons',
        'Isu.store.DueinTypes',
        'Isu.store.IssueWorkgroupAssignees',
        'Isu.store.UserList'
    ],

    views: [
        'Isu.view.issues.AddManuallyRuleItem',
        'Isu.view.issues.ManuallyRuleItem'
    ],

    refs: [{
           ref: 'page',
              selector: 'issue-manually-creation-rules-item-add'
           }, {
              ref: 'form',
              selector: 'issue-manually-creation-rules-item-add issue-manually-creation-rules-item'
           }],

    init: function () {
        this.control({
            'issue-manually-creation-rules-item-add issue-manually-creation-rules-item button[action=saveIssueAction]': {
                click: this.saveAction
            }
        });
    },

    createNewManuallyIssue: function () {
        var me = this,
            page = me.getPage(),
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('issue-manually-creation-rules-item-add',{
                returnLink: router.getRoute('workspace/toucampaigns').buildUrl()
            });
        var manualIssue = Ext.create('Isu.model.ManuallyRuleItem'),
        dependencies = ['Isu.store.IssueDevices', 'Isu.store.IssueReasons','Isu.store.IssueWorkgroupAssignees','Isu.store.UserList'],
        dependenciesCounter = dependencies.length,
        onDependenciesLoaded = function () {
            dependenciesCounter--;
            if (!dependenciesCounter) {
                widget.down('issue-manually-creation-rules-item').loadRecord(manualIssue);
                widget.setLoading(false);
            }
        };
        this.getApplication().fireEvent('changecontentevent', widget);
        widget.setLoading();
        Ext.Array.each(dependencies, function (store) {
            me.getStore(store).load(onDependenciesLoaded);
        });

    },

    saveAction: function (){
       var me = this,
           page = me.getPage(),
           form = me.getForm(),
           errorMessage = form.down('uni-form-error-message'),
           baseForm = form.getForm();

       form.updateRecord();
       var record = form.getRecord();
       record.beginEdit();
       var urgency = record.get('priority.urgency');
       var impact = record.get('priority.urgency');
       if ( urgency !== undefined && impact !== undefined ) record.set('priority' , urgency + ':' + impact);
       if (form.down('#dueDateTrigger')) {
            var dueDateNumber = form.down('[name=dueIn.number]').getValue();
            var dueDateType = form.down('[name=dueIn.type]').getValue();

            if (dueDateNumber && dueDateType){
                switch(dueDateType){
                    default:
                      dueDateNumber *= 3600 * 24;
                }
            }else{
                dueDateNumber = null;
            }

            record.set('dueDate', dueDateNumber);
        } else {
            record.set('dueIn', null);
       }
       record.endEdit();
       record.save({
            backUrl: page.returnLink,
            success: function (record, operation) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('tou.campaigns.addSuccess', 'TOU', 'ToU calendar campaign added'));
                if (page.rendered) {
                    window.location.href = page.returnLink;
                }
            },
            failure: function (record, operation) {
                var responseText = Ext.decode(operation.response.responseText, true);

                if (page.rendered && responseText && responseText.errors) {
                    Ext.suspendLayouts();
                    baseForm.markInvalid(responseText.errors);
                    errorMessage.show();
                    Ext.resumeLayouts(true);
                }
            },
            callback: function () {
                page.setLoading(false);
            }
        });
    }
});