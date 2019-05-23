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
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('issue-manually-creation-rules-item-add',{
                returnLink: router.getRoute('workspace/issues').buildUrl(),
                router: router
            });
        /*var manualIssue = Ext.create('Isu.model.ManuallyRuleItem'),
        dependencies = ['Isu.store.IssueDevices', 'Isu.store.IssueReasons'],
        dependenciesCounter = dependencies.length,
        onDependenciesLoaded = function () {
            dependenciesCounter--;
            if (!dependenciesCounter) {
                widget.down('issue-manually-creation-rules-item').loadRecord(manualIssue);
                widget.setLoading(false);
            }
        };*/
        this.getApplication().fireEvent('changecontentevent', widget);
        /*widget.setLoading();
        Ext.Array.each(dependencies, function (store) {
            me.getStore(store).load(onDependenciesLoaded);
        });*/

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
            if (reasonEditedValue.trim() == '') comboReason.markInvalid('This field is required');
            return;
       }

       form.updateRecord();

       var record = form.getRecord();

       if(reason === -1 && reasonEditedValue.trim() != ''){
            var value = reasonEditedValue.trim();
            var id = value.toLowerCase().replace (/ /g, '.');
            var rec = {
                id: id,
                name: value
            };
            comboReason.store.add(rec);
            comboReason.setValue(comboReason.store.getAt(comboReason.store.count()-1).get('id'));
            record.set('reasonId', id)
       }
       var urgency = record.get('priority.urgency');
       var impact = record.get('priority.impact');
       if ( urgency !== undefined && impact !== undefined ) record.set('priority' , urgency + ':' + impact);
       if (form.down('#dueDateTrigger')) {
            if (form.down('#dueDateTrigger')) {
                record.set('dueIn', {
                    number: form.down('[name=dueIn.number]').getValue(),
                    type: form.down('[name=dueIn.type]').getValue()
                });
            } else {
                record.set('dueIn', null);
            }
       }

        Ext.Ajax.request({
            url: record.getProxy().url,
            method: 'POST',
            jsonData: {"issues": new Array( record.getRecordData() )},
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