/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.controller.CreationManualRule', {
    extend: 'Ext.app.Controller',

    stores: [
        'Isu.store.IssueDevices',
        'Isu.store.ManualIssueReasons',
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
            router = me.getController('Uni.controller.history.Router'),
            returnLink = router.getRoute('workspace/issues').buildUrl(),
            deviceId = null;

        if (router.arguments && router.arguments.deviceId){
            deviceId = router.arguments.deviceId;
            returnLink = router.getRoute('devices').buildUrl() + '/' + deviceId;
        }
        if (Isu.privileges.Issue.canCreateManualIssue()){
            var widget = Ext.widget('issue-manually-creation-rules-item-add',{
                    returnLink: returnLink,
                    router: router,
                    deviceId: deviceId
                });
            this.getApplication().fireEvent('changecontentevent', widget);
        }

    },

    saveAction: function (){
       var me = this,
           page = me.getPage(),
           form = me.getForm(),
           errorMessage = form.down('uni-form-error-message'),
           baseForm = form.getForm(),
           comboReason = form.down('#issueReason'),
           reasonEditedValue = comboReason.getRawValue(),
           reason = comboReason.store.find('name', reasonEditedValue);

       errorMessage.hide();

       if (!form.isValid() || reasonEditedValue.trim() == '') {
            errorMessage.show();
            if (reasonEditedValue.trim() == '') comboReason.markInvalid(Uni.I18n.translate('issues.required.field', 'ISU', 'This field is required'));
            return;
       }


       form.updateRecord();

       var record = form.getRecord();

       if(reason === -1 && reasonEditedValue.trim() != ''){
            var value = reasonEditedValue.trim();
            var id = value.toLowerCase().replace (/ /g, '.');
            var rec = {
                id: value,
                name: value
            };
            comboReason.store.add(rec);
            comboReason.setValue(comboReason.store.getAt(comboReason.store.count()-1).get('id'));
            record.set('reasonId', value)
       }

       var urgency = record.get('priority.urgency');
       var impact = record.get('priority.impact');
       if ( urgency !== undefined && impact !== undefined ) record.set('priority' , urgency + ':' + impact);
       if (form.down('#dueDateTrigger') && form.down('[name=dueIn.number]') &&  form.down('[name=dueIn.number]').getValue()) {
            record.set('dueDate', {
                number: form.down('[name=dueIn.number]').getValue(),
                type: form.down('[name=dueIn.type]').getValue()
            });
            form.down('#dueDateTrigger').setValue({dueDate: true});
       } else {
            record.set('dueDate', null);
            form.down('#dueDateTrigger').setValue({dueDate: false});
       }

       Ext.Ajax.request({
            url: record.getProxy().url,
            method: 'POST',
            jsonData: {"issues": new Array( record.data )},
            success: function (response) {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('isu.manually.addSuccess', 'ISU', 'New manual issue added'));
                if (page.rendered) {
                    window.location.href = page.returnLink;
                }
            },
            failure: function (response) {
                var responseText = Ext.decode(response.responseText, true);
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