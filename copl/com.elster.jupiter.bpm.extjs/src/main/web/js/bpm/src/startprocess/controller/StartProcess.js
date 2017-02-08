/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.startprocess.controller.StartProcess', {
    extend: 'Ext.app.Controller',
    requires: [
        'Bpm.startprocess.view.StartProcess'
    ],
    stores: [

    ],
    models: [
        'Bpm.startprocess.model.ProcessContent'
    ],
    views: [
        'Bpm.startprocess.view.StartProcess'
    ],
    refs: [
        {ref: 'startProcessPanel', selector: 'bpm-start-processes-panel'},
        {ref: 'startProcessForm', selector: 'bpm-start-processes-panel #process-start-form'},
        {ref: 'processStartContent',selector: 'bpm-start-processes-panel #process-start-content'}
    ],

    init: function () {
        var me = this;
        me.control({
            '#process-start-content button[action=cancelStartProcess]': {
                click: this.cancelStartProcess
            },
            '#process-start-content button[action=startProcess]': {
                click: this.startProcess
            },
            '#start-process-form #processes-definition-combo': {
                select: this.processComboChange
            }
        });
    },

    loadJbpmForm: function (processRecord) {
        var me = this,
            startProcessPanel = me.getStartProcessPanel(),
            processStartContent = me.getProcessStartContent(),
            processContent = me.getModel('Bpm.startprocess.model.ProcessContent'),
            propertyForm;

        if (processStartContent == undefined){
            return;
        }
        propertyForm = processStartContent.down('property-form');
        startProcessPanel.setLoading();

        me.processRecord = processRecord.lastSelection[0].data;
        processContent.getProxy().setUrl(me.processRecord.deploymentId);
        processContent.load(me.processRecord.processId, {
            success: function (startProcessRecord) {

                processStartContent.startProcessRecord = startProcessRecord;
                if (startProcessRecord && startProcessRecord.properties() && startProcessRecord.properties().count()) {
                    propertyForm.loadRecord(startProcessRecord);
                    propertyForm.show();
                } else {
                    propertyForm.hide();
                }
                startProcessPanel.setLoading(false);
                propertyForm.up('#process-start-content').doLayout();
            },
            failure: function (record, operation) {
                startProcessPanel.setLoading(false);
                propertyForm.hide();
                propertyForm.up('#process-start-content').doLayout();
            }
        });

    },

    processComboChange: function (processCombo) {
        var me = this,
            widget = me.getStartProcessPanel(),
            form = widget.down('#start-process-form'),
            formErrorsPanel = form.down('#form-errors');

        if (!formErrorsPanel.isHidden()) {
            formErrorsPanel.hide();
        }
        me.loadJbpmForm(processCombo);
    },

    cancelStartProcess: function (btn) {
        var me = this,
            startProcessPanel = me.getStartProcessPanel(),
            url = startProcessPanel.properties.cancelLink;
        window.location.assign(url);
    },

    startProcess: function (button) {
        var me=this,
            startProcessPanel = me.getStartProcessPanel(),
            url = startProcessPanel.properties.successLink,
            extraParams = startProcessPanel.properties.startProcessParams,
            startProcessForm = me.getStartProcessForm(),
            processStartContent = me.getProcessStartContent(),
            startProcessRecord = processStartContent.startProcessRecord,
            propertyForm = processStartContent.down('property-form'),
            form = startProcessPanel.down('#start-process-form'),
            formErrorsPanel = form.down('#form-errors'),
            businessObject = {};

        if (form.isValid()) {
            if (!formErrorsPanel.isHidden()) {
                formErrorsPanel.hide();
            }

            propertyForm.updateRecord();

            startProcessRecord.beginEdit();

            Ext.Array.each(extraParams, function(param) {
                businessObject[param.name] = param.value;
            });

            startProcessRecord.set('businessObject', businessObject);
            startProcessRecord.set('deploymentId', me.processRecord.deploymentId);
            startProcessRecord.set('id', me.processRecord.processId);
            startProcessRecord.set('versionDB', me.processRecord.versionDB);
            startProcessRecord.set('processName', me.processRecord.name);
            startProcessRecord.set('processVersion', me.processRecord.version);

            startProcessRecord.save({
                success: function () {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('bpm.startprocess.started', 'BPM', 'Process started.'));
                    window.location.assign(url);
                },
                failure: function (record, operation) {
                    if (operation.response.status == 400) {
                        formErrorsPanel.show();
                        var json = Ext.decode(operation.response.responseText, true);
                        if (json && json.errors) {
                            startProcessForm.getForm().markInvalid(json.errors);
                        }
                    }
                }
            })
        }
        else {
            formErrorsPanel.show();
        }
    }
});