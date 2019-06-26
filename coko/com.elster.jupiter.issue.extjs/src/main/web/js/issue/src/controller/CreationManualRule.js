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
            returnLink = router.getRoute('devices/device').buildUrl();
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

    validateIssueReason: function(record){
        var me = this,
            form = me.getForm(),
            comboReason = form.down('#issueReason'),
            reasonEditedValue = comboReason.getRawValue(),
            reason = comboReason.store.find('name', reasonEditedValue),
            reasonEditedValueWithoutSpaces = reasonEditedValue.trim();

            if (reason === -1){
                if (reasonEditedValueWithoutSpaces !== ''){
                    var reasonData = {
                        id: reasonEditedValue,
                        name: reasonEditedValue
                    };
                    comboReason.store.add(reasonData);
                    record.set('reasonId', reasonEditedValue)
                }else{
                    comboReason.markInvalid(Uni.I18n.translate('issues.required.field', 'ISU', 'This field is required'));
                    return false;
                }
            }
            return true;
    },

    setDueDate: function(record){
        var me = this,
            form = me.getForm(),
            dueDateTrigger = form.down('#dueDateTrigger'),
            dueInNumber = form.down('[name=dueIn.number]'),
            dueInType = form.down('[name=dueIn.type]'),
            urgency = record.get('priority.urgency'),
            impact = record.get('priority.impact');

           if ( urgency !== undefined && impact !== undefined ){
                record.set('priority' , urgency + ':' + impact);
           }
           if (dueDateTrigger && dueInNumber &&  dueInNumber.getValue()) {
                record.set('dueDate', {
                    number: dueInNumber.getValue(),
                    type: dueInType.getValue()
                });
                dueDateTrigger.setValue({dueDate: true});
           } else {
                record.set('dueDate', null);
                dueDateTrigger.setValue({dueDate: false});
           }
    },

    saveAction: function (){
       var me = this,
           page = me.getPage(),
           form = me.getForm(),
           errorMessage = form.down('uni-form-error-message'),
           baseForm = form.getForm();

       errorMessage.hide();

       form.updateRecord();
       var record = form.getRecord();

       if (!form.isValid() || !me.validateIssueReason(record)) {
            errorMessage.show();
            return;
       }

       me.setDueDate(record);

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