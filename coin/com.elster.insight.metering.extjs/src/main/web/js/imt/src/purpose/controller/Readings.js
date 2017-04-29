/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.controller.Readings', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router',
        'Imt.purpose.view.Outputs',
        'Imt.purpose.store.Outputs',
        'Imt.purpose.view.OutputChannelMain',
        'Imt.purpose.view.ReadingsList',
        'Uni.store.DataIntervalAndZoomLevels',
        'Imt.purpose.view.SingleReadingActionMenu',
        'Imt.purpose.view.MultipleReadingsActionMenu',
        'Imt.purpose.view.ReadingEstimationWindow',
        'Imt.purpose.view.ReadingEstimationWithRuleWindow',
        'Cfg.view.common.CopyFromReferenceWindow',
        'Cfg.view.common.EditEstimationComment',
        'Uni.view.readings.CorrectValuesWindow'

    ],

    stores: [
        'Imt.purpose.store.Outputs',
        'Imt.purpose.store.Readings',
        'Uni.store.DataIntervalAndZoomLevels',
        'Imt.purpose.store.RegisterReadings',
        'Imt.usagepointmanagement.store.UsagePointTypes',
        'Imt.purpose.store.Estimators',
        'Imt.purpose.store.EstimationRules'
    ],

    models: [
        'Imt.purpose.model.Output',
        'Imt.purpose.model.Reading',
        'Imt.usagepointmanagement.model.ValidationInfo',
        'Imt.usagepointmanagement.model.SuspectReason',
        'Imt.usagepointmanagement.model.Purpose',
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.purpose.model.ChannelDataEstimate',
        'Imt.purpose.model.CopyFromReference'
    ],

    views: [
        'Imt.purpose.view.Outputs',
        'Imt.purpose.view.OutputChannelMain',
        'Imt.purpose.view.ValidationStatusForm'
    ],

    refs: [
        {
            ref: 'outputChannelMainPage',
            selector: 'output-channel-main'
        },
        {
            ref: 'readingsList',
            selector: '#output-readings #readings-list'
        },
        {
            ref: 'outputReadings',
            selector: '#output-readings'
        },
        {
            ref: 'readingsGraph',
            selector: '#output-readings #readings-graph'
        },
        {
            ref: 'readingPreviewPanel',
            selector: 'output-channel-main reading-preview'
        },
        {
            ref: 'editEstimationComment',
            selector: 'reading-edit-estimation-comment-window'
        },
        {
            ref: 'copyFromReferenceWindow',
            selector: 'reading-copy-from-reference-window'
        },
        {
            ref: 'readingEstimationWindow',
            selector: 'reading-estimation-window'
        },
        {
            ref: 'readingEstimationWithRuleWindow',
            selector: 'reading-estimation-with-rule-window'
        },
        {
            ref: 'correctReadingWindow',
            selector: 'correct-values-window'
        }
    ],

    init: function () {
        this.control({
            '#readings-list': {
                edit: this.resumeEditorFieldValidation,
                canceledit: this.resumeEditorFieldValidation,
                selectionchange: this.onDataGridSelectionChange,
                select: this.showPreview,
                beforeedit: this.beforeEditRecord
            },
            'purpose-bulk-action-menu': {
                click: this.chooseBulkAction
            },
            'purpose-readings-data-action-menu': {
                beforeshow: this.checkSuspect,
                click: this.chooseAction
            },
            '#readings-list #undo-button': {
                click: this.undoChannelDataChanges
            },
            '#readings-list #save-changes-button': {
                click: this.saveChannelDataChanges
            },
            'reading-copy-from-reference-window #copy-reading-button': {
                click: this.copyFromReferenceUpdateGrid
            },
            'reading-edit-estimation-comment-window #edit-comment-button': {
                click: this.saveEstimationComment
            },
            'reading-estimation-window #estimate-reading-button': {
                click: this.estimateReadingWithEstimator
            },
            'reading-estimation-with-rule-window #estimate-reading-button': {
                click: this.estimateReadingWithRule
            },
            'correct-values-window #correct-reading-button': {
                click: this.correctReadings
            }
        });
    },
    valueBeforeEdit: 0,

    chooseBulkAction: function (menu, item) {
        var me = this,
            records = me.getReadingsList().getSelectionModel().getSelection();

        switch (item.action) {
            case 'editEstimationComment':
                me.editEstimationComment(records);
                break;
            case 'estimateValue':
                me.estimateValue(records);
                break;
            case 'estimateWithRule':
                me.estimateWithRule(records);
                break;
            case 'copyFromReference':
                me.copyFromReference(records);
                break;
            case 'confirmValue':
                me.confirmValue(records, true);
                break;
            case 'resetValue':
                me.resetReadings(records, true);
                break;
            case 'clearProjectedFlag':
                me.clearProjected(records);
                break;
            case 'markProjected':
                me.markProjected(records);
                break;
            case 'correctValue':
                me.openCorrectWindow(records);
                break;
        }
    },

    chooseAction: function (menu, item) {
        var me = this;

        switch (item.action) {
            case 'editValue':
                me.getReadingsList().getPlugin('cellplugin').startEdit(menu.record, 1);
                break;
            case 'resetValue':
                me.resetReadings(menu.record);
                break;
            case 'copyFromReference':
                me.copyFromReference(menu.record);
                break;
            case 'editEstimationComment':
                me.editEstimationComment(menu.record);
                break;
            case 'estimateValue':
                me.estimateValue(menu.record);
                break;
            case 'estimateWithRule':
                me.estimateWithRule(menu.record);
                break;
            case 'confirmValue':
                me.confirmValue(menu.record, false);
                break;
            case 'clearProjectedFlag':
                me.clearProjected(menu.record);
                break;
            case 'markProjected':
                me.markProjected(menu.record);
                break;
            case 'correctValue':
                me.openCorrectWindow(menu.record);
                break;
        }
    },

    beforeEditRecord: function (editor, context) {
        var intervalFlags = context.record.get('intervalFlags');
        context.column.getEditor().allowBlank = !(intervalFlags && intervalFlags.length);
        this.valueBeforeEdit = context.record.get('value');
        this.showPreview(context.grid.getSelectionModel(), context.record);
    },

    showPreview: function (selectionModel, record) {
        var me = this;
        if (selectionModel.getSelection().length === 1) {
            me.getReadingPreviewPanel().updateForm(record);
        }
    },

    confirmValue: function (record, isBulk) {
        var me = this,
            grid = me.getReadingsList(),
            isModified,
            chart = me.getReadingsGraph().chart,
            func = function (rec) {
                isModified = rec.isModified('value');
                if (!rec.get('confirmed') && !isModified) {
                    var validationResult = rec.get('validationResult') == 'validationStatus.suspect';

                    if (validationResult) {
                        rec.set('confirmedNotSaved', true);
                        chart.get(rec.get('interval').start).update({color: 'rgba(112,187,81,0.3)'});
                        grid.getView().refreshNode(grid.getStore().indexOf(rec));
                        chart.get(rec.get('interval').start).select(false);
                        me.getOutputReadings().down('#output-readings-preview-container').fireEvent('rowselect', record);
                        rec.set('confirmed', true);
                        rec.set('isConfirmed', true);
                    }
                }
            };

        Ext.suspendLayouts(true);
        if (isBulk) {
            Ext.Array.each(record, function (reading) {
                func(reading);
            });
        } else {
            func(record);
        }

        Ext.resumeLayouts();
        me.getReadingsList().down('#save-changes-button').isDisabled() && me.showButtons();
    },

    showButtons: function () {
        var me = this,
            store = me.getStore('Imt.purpose.store.Readings'),
            disabled;

        disabled = store.getUpdatedRecords().length <= 0;

        me.getReadingsList().down('#save-changes-button').setDisabled(disabled);
        me.getReadingsList().down('#undo-button').setDisabled(disabled);
    },

    checkSuspect: function (menu) {
        var me = this,
            validationResult = menu.record.get('validationResult') === 'validationStatus.suspect' ||
                menu.record.get('estimatedNotSaved') === true,
            estimationRulesCount = me.getOutputChannelMainPage().controller.hasEstimationRule,
            canClearProjected = menu.record.get('isProjected') === true,
            canMarkProjected = menu.record.get('isProjected') === false && (menu.record.isModified('value') || menu.record.get('ruleId') !== 0 || !Ext.isEmpty(menu.record.get('modificationState'))),
            canEditingComment = false,
            flagForComment = function (value) {
                if (value === 'EDITED' ||
                    value === 'ESTIMATED' ||
                    value === 'REMOVED') {
                    return true;
                } else {
                    return false;
                }
            };

        Ext.suspendLayouts();
        menu.down('#estimate-value').setVisible(validationResult);
        menu.down('#estimate-value-with-rule').setVisible(validationResult && estimationRulesCount);

        if (menu.record.get('modificationState') && menu.record.get('modificationState').flag) {
            canEditingComment = flagForComment(menu.record.get('modificationState').flag);
        }
        if (menu.record.get('confirmed') || menu.record.isModified('value')) {
            menu.down('#confirm-value').hide();
        } else {
            menu.down('#confirm-value').setVisible(validationResult);
        }
        if (menu.down('#edit-estimation-comment')) {
            menu.down('#edit-estimation-comment').setVisible(canEditingComment);
        }
        if (menu.down('#reset-value')) {
            menu.down('#reset-value').setVisible(menu.record.get('estimatedByRule') || menu.record.get('modificationFlag') == "EDITED" || menu.record.get('modificationFlag') == "ADDED");
        }
        if (menu.down('#clear-projected')) {
            menu.down('#clear-projected').setVisible(canClearProjected);
        }
        if (menu.down('#mark-projected')) {
            menu.down('#mark-projected').setVisible(canMarkProjected);
        }
        if (menu.down('#correct-value')) {
            menu.down('#correct-value').setVisible(!Ext.isEmpty(menu.record.get('value')))
        }
        Ext.resumeLayouts();
    },


    resumeEditorFieldValidation: function (editor, event) {
        var me = this;

        if (me.getReadingsGraph() && me.getReadingsGraph().chart) {
            var chart = me.getReadingsGraph().chart,
                point = chart.get(event.record.get('interval').start),
                grid = me.getReadingsList(),
                value = event.record.get('value'),
                condition = (isNaN(point.y) && isNaN(value)) ? false : (point.y != value),
                updatedObj;
            if (event.column) {
                event.column.getEditor().allowBlank = true;
            }

            if (event.record.isModified('value') && this.valueBeforeEdit !== event.record.get('value')) {
                grid.down('#save-changes-button').isDisabled() && me.showButtons();

                Ext.suspendLayouts(true);
                if (!value) {
                    point.update({y: null});
                    event.record.set('value', '0');
                } else {
                    if (event.record.get('plotBand')) {
                        chart.xAxis[0].removePlotBand(event.record.get('interval').start);
                        event.record.set('plotBand', false);
                    }
                    updatedObj = {
                        y: parseFloat(value),
                        color: 'rgba(112,187,81,0.3)',
                        value: value
                    };
                    point.update(updatedObj);
                    point.select(false);
                    me.getOutputReadings().down('#output-readings-preview-container').fireEvent('rowselect', event.record);
                }

                if (!event.record.get('estimatedNotSaved')) {
                    event.record.set('modificationState', Uni.util.ReadingEditor.modificationState('EDITED'));
                }
                if (event.column) {
                    event.record.set('validationResult', 'validationStatus.ok');
                    event.record.set('isProjected', false);
                    event.record.set('ruleId', 0);

                    grid.getView().refreshNode(grid.getStore().indexOf(event.record));
                    event.record.get('confirmed') && event.record.set('confirmed', false);
                }
                Ext.resumeLayouts();
            } else if (condition) {
                me.resetChanges(event.record, point);
            }
        }
        me.valueBeforeEdit = null;
    },

    resetChanges: function (record, point) {
        var me = this,
            properties = record.get('readingProperties'),
            grid = me.getReadingsList(),
            store = grid.getStore(),
            color = '#70BB51';

        if (!Ext.isEmpty(record.get('estimatedByRule'))) {
            color = '#568343';
        } else if (properties.notValidated) {
            color = '#71adc7';
        } else if (properties.suspect) {
            color = 'rgba(235, 86, 66, 1)';
        } else if (properties.informative) {
            color = '#dedc49';
        }
        record.get('confirmed') && record.set('confirmed', false);

        Ext.suspendLayouts(true);
        grid.getView().refreshNode(store.indexOf(record));
        point.update({
            y: parseFloat(record.get('value')),
            color: color,
            value: record.get('value')
        });
        record.reject();
        if (!store.getUpdatedRecords().length) {
            grid.down('#save-changes-button').disable();
            grid.down('#undo-button').disable();
        }
        Ext.resumeLayouts();
    },

    undoChannelDataChanges: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute().forward(router.arguments, Uni.util.QueryString.getQueryStringValues());
    },

    saveChannelDataChanges: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            changedData = me.getChangedData(me.getStore('Imt.purpose.store.Readings')),
            viewport = Ext.ComponentQuery.query('viewport > #contentPanel')[0];

        if (!Ext.isEmpty(changedData)) {
            viewport.setLoading();
            Ext.Ajax.request({
                url: Ext.String.format('/api/udr/usagepoints/{0}/purposes/{1}/outputs/{2}/channelData', router.arguments.usagePointId, router.arguments.purposeId, router.arguments.outputId),
                method: 'PUT',
                jsonData: Ext.encode(changedData),
                timeout: 300000,
                success: function () {
                    router.getRoute().forward(router.arguments, Uni.util.QueryString.getQueryStringValues());
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('devicechannels.successSavingMessage', 'IMT', 'Channel data have been saved'));
                },
                failure: function (response) {
                    viewport.setLoading(false);
                }
            });
        }
    },

    getChangedData: function (store) {
        var me = this,
            changedData = [],
            changedRecord = {},
            confirmedObj = {};

        Ext.Array.each(store.getUpdatedRecords(), function (record) {
            confirmedObj = {};
            changedRecord = {
                interval: record.get('interval')
            };

            if (record.isModified('value')) {
                Ext.merge(changedRecord, _.pick(record.getData(), 'value'));
            }
            if (record.isModified('ruleId')) {
                Ext.merge(changedRecord, _.pick(record.getData(), 'value', 'ruleId'));
            }
            if (record.isModified('isProjected')) {
                Ext.merge(changedRecord, _.pick(record.getData(), 'value', 'isProjected'));
            }
            if (record.isModified('collectedValue')) {
                Ext.merge(changedRecord, _.pick(record.getData(), 'collectedValue', 'isProjected'));
            }

            if (record.get('removedNotSaved')) {
                confirmedObj = {
                    interval: record.get('interval'),
                    value: null
                };
            } else if (record.get('confirmed')) {
                confirmedObj = {
                    interval: record.get('interval'),
                    isConfirmed: record.get('confirmedNotSaved') || false
                };
            }

            changedRecord = Ext.merge(confirmedObj, changedRecord);
            changedRecord.value = record.get('collectedValue') ? record.get('collectedValue') : record.get('value');
            changedRecord.commentId = record.get('commentId') ? record.get('commentId') : undefined;
            changedData.push(changedRecord);
        });

        return changedData;
    },

    onDataGridSelectionChange: function (selectionModel, selectedRecords) {
        var me = this,
            canEstimate = false,
            canConfirm = false,
            canReset = false,
            canCorrect = false,
            canEstimateWithRule = false,
            canClearProjected = false,
            canMarkProjected = false,
            canEditingComment = false,
            canCopyFromReference = false,
            button = me.getReadingsList().down('#readings-bulk-action-button'),
            menu = button.down('menu'),
            estimationRulesCount = me.getOutputChannelMainPage().controller.hasEstimationRule,
            flagForComment = function (value) {
                if (value === 'EDITED' ||
                    value === 'ESTIMATED' ||
                    value === 'REMOVED') {
                    return true;
                } else {
                    return false;
                }
            };

        Ext.Array.each(selectedRecords, function (record) {
            if (canEstimate && canConfirm && canReset) {
                return false;
            }
            if (!canEditingComment) {
                if (record.get('modificationState') && record.get('modificationState').flag) {
                    canEditingComment = flagForComment(record.get('modificationState').flag);
                }
            }

            if (!canEstimate && record.get('validationResult') == 'validationStatus.suspect') {
                canEstimate = true;
                if (!canConfirm && !record.get('isConfirmed') && !record.isModified('value')) {
                    canConfirm = true;
                }
                if (estimationRulesCount) {
                    canEstimateWithRule = true;
                }
            }
            if (!canReset && (record.get('estimatedByRule') || record.get('modificationFlag') == "EDITED" || record.get('modificationFlag') == "ADDED")) {
                canReset = true;
            }
            if (record.get('isProjected')) {
                canClearProjected = true;
            }
            if (record.get('isProjected') === false && (record.isModified('value') || record.get('ruleId') !== 0 || !Ext.isEmpty(record.get('modificationState')))) {
                canMarkProjected = true;
            }
            if (!canCorrect && !Ext.isEmpty(record.get('value'))) {
                canCorrect = true;
            }
            canCopyFromReference = true;
        });

        Ext.suspendLayouts();
        menu.down('#estimate-value').setVisible(canEstimate);
        menu.down('#estimate-value-with-rule').setVisible(canEstimateWithRule);
        menu.down('#edit-estimation-comment').setVisible(canEditingComment);
        menu.down('#copy-form-value').setVisible(canCopyFromReference);
        menu.down('#confirm-value').setVisible(canConfirm);
        menu.down('#reset-value').setVisible(canReset);
        menu.down('#correct-value').setVisible(canCorrect);
        menu.down('#clear-projected').setVisible(canClearProjected);
        menu.down('#mark-projected').setVisible(canMarkProjected);
        button.setDisabled(!menu.query('menuitem[hidden=false]').length);
        Ext.resumeLayouts();
    },

    resetReadings: function (records) {
        var me = this,
            point,
            calculatedValue,
            grid = me.getReadingsList(),
            store = grid.getStore(),
            gridView = grid.getView(),
            chart = me.getReadingsGraph().chart;

        Ext.suspendLayouts();
        Ext.Array.each(records, function (record) {
            calculatedValue = record.get('calculatedValue');
            record.beginEdit();
            record.set('modificationState', Uni.util.ReadingEditor.modificationState('RESET'));

            record.set('removedNotSaved', true);
            record.set('value', calculatedValue);
            if (record.get('confirmed')) {
                record.set('confirmed', false);
            }
            if (record.get('isProjected')) {
                record.set('isProjected', false);
            }
            record.set('ruleId', 0);
            record.set('validationResult', 'validationStatus.ok');
            record.endEdit(true);
            gridView.refreshNode(store.indexOf(record));
            point = chart.get(record.get('interval').start);
            point.update({y: parseFloat(calculatedValue), color: 'rgba(112,187,81,0.3)', value: calculatedValue});

        });
        chart.redraw();
        Ext.resumeLayouts(true);
        me.showButtons();
    },

    editEstimationComment: function (records) {
        var readings = [];

        if (!Array.isArray(records)) {
            readings.push(records);
        } else {
            readings = records;
        }

        Ext.widget('reading-edit-estimation-comment-window',
            {
                itemId: 'channel-edit-estimation-comment-window',
                records: readings,
                usagePoint: true
            }).show();
    },

    saveEstimationComment: function (button) {
        var me = this,
            window = me.getEditEstimationComment(),
            commentCombo = window.down('#estimation-comment-box'),
            commentValue = commentCombo.getRawValue(),
            commentId = commentCombo.getValue(),
            readings = button.readings,
            record = {
                modificationFlag: 'EDITED',
                modificationState: Uni.util.ReadingEditor.modificationState('EDITED'),
                commentId: commentId,
                commentValue: commentValue
            };
        if (commentId !== -1) {
            _.each(readings, function (reading) {
                reading.set(record);
            });
        }

        me.showButtons();
        window.close();
    },

    copyFromReference: function (records) {
        var window = Ext.widget('reading-copy-from-reference-window',
            {
                itemId: 'channel-copy-from-reference-window',
                records: records,
                usagePoint: true
            }).show();

        window.down('#usage-point-field').getStore().getProxy().extraParams = {
            page: 1,
            start: 0,
            limit: 50,
            nameOnly: true
        };

        window.down('#purpose-field').getStore().getProxy().extraParams = {
            page: 1,
            start: 0,
            limit: 50
        };

        window.down('#readingType-field').getStore().getProxy().extraParams = {
            page: 1,
            start: 0,
            limit: 50,
            property: 'fullAliasName'
        };
    },

    copyFromReferenceUpdateGrid: function () {
        var me = this,
            intervals = [],
            window = me.getCopyFromReferenceWindow(),
            form = window.down('#reading-copy-window-form'),
            model = Ext.create('Imt.purpose.model.CopyFromReference'),
            router = me.getController('Uni.controller.history.Router'),
            commentCombo = window.down('#estimation-comment-box'),
            commentId = commentCombo.getValue(),
            commentValue = commentCombo.getRawValue(),
            readings = [];

        form.updateRecord(model);
        model.getProxy().extraParams = {
            usagePointId: router.arguments.usagePointId,
            purposeId: router.arguments.purposeId,
            outputId: router.arguments.outputId
        };

        if (!Array.isArray(window.records)) {
            readings.push(window.records);
        } else {
            readings = window.records;
        }
        _.each(readings, function (reading) {
            intervals.push(reading.get('interval'));
        });

        model.set('intervals', intervals);
        model.save({
            failure: function (record, operation) {
                var response = JSON.parse(operation.response.responseText);

                _.each(response.errors, function (error) {
                    error.msg = '<span style="white-space: normal">' + error.msg + '</span>';
                });
                form.getForm().markInvalid(response.errors);
            },
            success: function (record, operation) {
                var item = null,
                    response = JSON.parse(operation.response.responseText);
                if (response[0]) {
                    Ext.suspendLayouts();
                        _.each(readings, function (reading) {
                            item = reading.get('interval').end;
                            item = _.find(response, function (rec) {
                                return rec.interval.end === item;
                            });
                            if (item && reading.get('value') !== item.value) {
                                reading.set('value', item.value);
                                reading.set('isProjected', model.get('projectedValue'));
                                reading.set('bulkValidationInfo', item.bulkValidationInfo);
                                reading.set('mainValidationInfo', item.mainValidationInfo);
                                reading.set('modificationState', Uni.util.ReadingEditor.modificationState('EDITED'));
                                if (commentId !== -1) {
                                    reading.set('commentId', commentId);
                                    reading.set('commentValue', commentValue);
                                }
                            }
                        });
                    me.showButtons();
                    Ext.resumeLayouts(true);
                }
                window.close();
            },
            callback: function () {
                window.setLoading(false);
            }
        });
    },

    clearProjected: function (records) {
        var me = this,
            grid = me.getReadingsList();
        Ext.suspendLayouts();
        Ext.Array.each(records, function (record) {
            if (record.get('isProjected') === true) {
                record.beginEdit();
                record.set('isProjected', false);
                record.endEdit(true);
                grid.getView().refreshNode(grid.getStore().indexOf(record));
            }
        });
        Ext.resumeLayouts(true);
        me.onDataGridSelectionChange(null, records);
        me.showButtons();
    },

    markProjected: function (records) {
        var me = this,
            grid = me.getReadingsList();
        Ext.suspendLayouts();
        Ext.Array.each(records, function (record) {
            if (record.get('isProjected') === false && (record.isModified('value') || record.get('ruleId') !== 0 || !Ext.isEmpty(record.get('modificationState')))) {
                record.beginEdit();
                record.set('isProjected', true);
                record.endEdit(true);
                grid.getView().refreshNode(grid.getStore().indexOf(record));
            }
        });
        me.onDataGridSelectionChange(null, records);
        Ext.resumeLayouts(true);
        me.showButtons();
    },

    estimateValue: function (record) {
        var me = this;

        me.getStore('Imt.purpose.store.Estimators').load(function () {
            Ext.widget('reading-estimation-window', {
                itemId: 'channel-reading-estimation-window',
                record: record
            }).show();
        });
    },

    estimateWithRule: function (record) {
        this.getStore('Imt.purpose.store.EstimationRules').load(function (records) {
            Ext.widget('reading-estimation-with-rule-window', {
                itemId: 'channel-reading-estimation-with-rule-window',
                record: record,
                hasRules: Boolean(records.length)
            }).show();
        });
    },

    openCorrectWindow: function (record) {
        Ext.widget('correct-values-window', {
            itemId: 'channel-reading-correct-values-window',
            record: record
        }).show();

    },

    estimateReadingWithEstimator: function () {
        var me = this,
            window = me.getReadingEstimationWindow(),
            estimator = window.down('#estimator-field').getValue(),
            propertyForm = window.down('#property-form'),
            model = Ext.create('Imt.purpose.model.ChannelDataEstimate'),
            commentCombo = window.down('#estimation-comment-box'),
            commentId = commentCombo.getValue(),
            commentValue = commentCombo.getRawValue(),
            record = window.record,
            markAsProjected,
            commentId,
            intervalsArray = [];


        !window.down('#form-errors').isHidden() && window.down('#form-errors').hide();
        !window.down('#error-label').isHidden() && window.down('#error-label').hide();

        commentId = window.down('#estimation-comment-box').getValue();
        markAsProjected = window.down('#markProjected').getValue();
        propertyForm.clearInvalid();

        if (propertyForm.getRecord()) {
            propertyForm.updateRecord();
            model.set('estimatorImpl', estimator);
            model.propertiesStore = propertyForm.getRecord().properties();
        }
        if (!Ext.isArray(record)) {
            intervalsArray.push({
                start: record.get('interval').start,
                end: record.get('interval').end
            });
            record.set('commentId', commentId);
            record.set('commentValue', commentValue);
        } else {
            Ext.Array.each(record, function (item) {
                intervalsArray.push({
                    start: item.get('interval').start,
                    end: item.get('interval').end
                });
                item.set('commentId', commentId);
                item.set('commentValue', commentValue);
            });
        }
        model.set('intervals', intervalsArray);
        model.set('markAsProjected', markAsProjected);
        me.saveChannelDataEstimateModel(model, record, window, null, 'editWithEstimator');
    },

    estimateReadingWithRule: function () {
        var me = this,
            window = me.getReadingEstimationWithRuleWindow(),
            estimationRuleId = window.down('#estimation-rule-field').getValue(),
            propertyForm = window.down('#property-form'),
            model = Ext.create('Imt.purpose.model.ChannelDataEstimate'),
            commentCombo = window.down('#estimation-comment-box'),
            commentId = commentCombo.getValue(),
            commentValue = commentCombo.getRawValue(),
            record = window.record,
            markAsProjected,
            intervalsArray = [];


        !window.down('#form-errors').isHidden() && window.down('#form-errors').hide();
        !window.down('#error-label').isHidden() && window.down('#error-label').hide();
        markAsProjected = window.down('#reading-type-mark-projected').getRecord().get('markProjected');
        if (propertyForm.getRecord()) {
            model.set('estimatorImpl', window.getEstimator());
            model.propertiesStore = propertyForm.getRecord().properties();
        }
        if (!Ext.isArray(record)) {
            intervalsArray.push({
                start: record.get('interval').start,
                end: record.get('interval').end
            });
            if (commentId !== -1) {
                record.set('commentId', commentId);
                record.set('commentValue', commentValue);
            }
        } else {
            Ext.Array.each(record, function (item) {
                intervalsArray.push({
                    start: item.get('interval').start,
                    end: item.get('interval').end
                });
                if (commentId !== -1) {
                    item.set('commentId', commentId);
                    item.set('commentValue', commentValue);
                }
            });
        }
        model.set('intervals', intervalsArray);
        model.set('markAsProjected', markAsProjected);
        me.saveChannelDataEstimateModel(model, record, window, estimationRuleId, 'estimate');
    },

    saveChannelDataEstimateModel: function (record, readings, window, ruleId, action) {
        var me = this,
            grid = me.getReadingsList(),
            router = me.getController('Uni.controller.history.Router'),
            adjustedPropertyFormErrors;

        record.getProxy().setParams(decodeURIComponent(router.arguments.usagePointId), router.arguments.purposeId, router.arguments.outputId);
        window.setLoading();
        Ext.Ajax.suspendEvent('requestexception');
        record.save({
            callback: function (rec, operation, success) {
                Ext.Ajax.resumeEvent('requestexception');
                var responseText = Ext.decode(operation.response.responseText, true),
                    chart = me.getReadingsGraph().chart;

                Ext.suspendLayouts();
                if (success && responseText[0]) {
                    if (!Ext.isArray(readings)) {
                        if (commentId !== -1) {
                            readings.set('commentId', commentId);
                            readings.set('commentValue', commentValue);
                        }
                        me.updateEstimatedValues(record, readings, responseText[0], ruleId, action);
                    } else {
                        Ext.Array.each(responseText, function (estimatedReading) {
                            Ext.Array.findBy(readings, function (reading) {
                                if (estimatedReading.interval.start == reading.get('interval').start) {
                                    if (commentId !== -1) {
                                        readings.set('commentId', commentId);
                                        readings.set('commentValue', commentValue);
                                    }
                                    me.updateEstimatedValues(record, reading, estimatedReading, ruleId, action);
                                    return true;
                                }
                            });
                        });
                    }
                    window.destroy();
                    grid.down('#save-changes-button').isDisabled() && me.showButtons();
                } else {
                    window.setLoading(false);
                    if (responseText) {
                        if (responseText.message) {
                            window.down('#error-label').show();
                            window.down('#error-label').setText('<div style="color: #EB5642">' + responseText.message + '</div>', false);
                        } else if (responseText.readings) {
                            window.down('#error-label').show();
                            var listOfFailedReadings = [];
                            Ext.Array.each(responseText.readings, function (readingTimestamp) {
                                listOfFailedReadings.push(Uni.I18n.translate('general.dateAtTime', 'IMT', '{0} at {1}', [Uni.DateTime.formatDateShort(new Date(readingTimestamp)), Uni.DateTime.formatTimeShort(new Date(readingTimestamp))], false));
                            });
                            var errorMessage = window.down('#estimator-field') ? Uni.I18n.translate('output.estimationErrorMessageWithIntervals', 'IMT', 'Could not estimate {0} with {1}',
                                [listOfFailedReadings.join(', '), window.down('#estimator-field').getRawValue()]) : Uni.I18n.translate('output.estimationErrorMessage', 'IMT', 'Could not estimate {0}',
                                listOfFailedReadings.join(', '));
                            window.down('#error-label').setText('<div style="color: #EB5642">' + errorMessage + '</div>', false);
                        } else if (responseText.errors) {
                            window.down('#form-errors').show();
                            if (Ext.isArray(responseText.errors)) {
                                adjustedPropertyFormErrors = responseText.errors.map(function (error) {
                                    if (error.id.startsWith('properties.')) {
                                        error.id = error.id.slice(11);
                                    }
                                    return error;
                                });
                            }
                            window.down('#property-form').markInvalid(responseText.errors);
                        } else {
                            window.destroy();
                        }
                    }
                }
                Ext.resumeLayouts(true);
            }
        });
    },

    updateEstimatedValues: function (record, reading, estimatedReading, ruleId, action) {
        var me = this,
            grid = me.getReadingsList();

        reading.set('value', estimatedReading.value);
        ruleId && reading.set('ruleId', ruleId);
        reading.set('validationResult', 'validationStatus.ok');
        if (action === 'estimate') {
            reading.set('estimatedNotSaved', true);
            reading.set('estimatedByRule', true);
        }
        reading.set('isProjected', estimatedReading.isProjected);
        grid.getView().refreshNode(grid.getStore().indexOf(reading));

        me.resumeEditorFieldValidation(grid.editingPlugin, {
            record: reading
        });
        reading.get('confirmed') && reading.set('confirmed', false);
    },

    updateCorrectedValues: function (reading, correctedInterval, model) {
        var me = this,
            grid = me.getReadingsList();

        reading.beginEdit();
        if (correctedInterval.value != reading.get('value')) {
            reading.set('modificationState', Uni.util.ReadingEditor.modificationState('EDITED'));
            reading.set('validationResult', 'validationStatus.ok');
        }
        reading.set('value', correctedInterval.value);
        reading.set('isProjected', model.get('projected'));

        reading.endEdit(true);

        grid.getView().refreshNode(grid.getStore().indexOf(reading));

        me.resumeEditorFieldValidation(grid.editingPlugin, {
            record: reading
        });
    },

    correctReadings: function () {
        var me = this,
            model = Ext.create('Uni.model.readings.ReadingCorrection'),
            window = me.getCorrectReadingWindow(),
            records = window.record,
            router = me.getController('Uni.controller.history.Router'),
            commentCombo = window.down('#estimation-comment-box'),
            commentValue = commentCombo.getRawValue(),
            commentId = commentCombo.getValue(),
            grid = me.getReadingsList(),
            intervalsArray = [];

        window.updateRecord(model);

        if (!Ext.isArray(records)) {
            records = [records];
        }

        Ext.Array.each(records, function (item) {
            if (model.get('onlySuspectOrEstimated')) {
                if (Uni.util.ReadingEditor.checkReadingInfoStatus(item.get('mainValidationInfo')).isSuspectOrEstimated()) {
                    intervalsArray.push({
                        start: item.get('interval').start,
                        end: item.get('interval').end
                    });
                }
            } else {
                intervalsArray.push({
                    start: item.get('interval').start,
                    end: item.get('interval').end
                });
            }
        });

        model.set('intervals', intervalsArray);

        model.getProxy().setMdmUrl(router.arguments.usagePointId, router.arguments.purposeId, router.arguments.outputId);
        window.setLoading();
        Ext.Ajax.suspendEvent('requestexception');
        model.phantom = false;
        model.save({
            callback: function (rec, operation, success) {
                Ext.Ajax.resumeEvent('requestexception');
                var responseText = Ext.decode(operation.response.responseText, true),
                    chart = me.getReadingsGraph().chart;

                Ext.suspendLayouts();
                if (success && responseText[0]) {
                    Ext.Array.each(responseText, function (correctedInterval) {
                        Ext.Array.findBy(records, function (reading) {
                            if (correctedInterval.interval.start == reading.get('interval').start) {
                                if (commentId !== -1) {
                                    reading.set('commentId', commentId);
                                    reading.set('commentValue', commentValue);
                                }
                                me.updateCorrectedValues(reading, correctedInterval, model);
                                return true;
                            }
                        });
                    });
                    window.destroy();
                    grid.down('#save-changes-button').isDisabled() && me.showButtons();
                } else {
                    window.setLoading(false);
                    if (responseText) {
                        if (responseText.message) {
                            window.down('#error-label').show();
                            window.down('#error-label').setText('<div style="color: #EB5642">' + responseText.message + '</div>', false);
                        } else if (responseText.readings) {
                            window.down('#error-label').show();
                            var listOfFailedReadings = [];
                            Ext.Array.each(responseText.readings, function (readingTimestamp) {
                                listOfFailedReadings.push(Uni.I18n.translate('general.dateAtTime', 'IMT', '{0} at {1}', [Uni.DateTime.formatDateShort(new Date(readingTimestamp)), Uni.DateTime.formatTimeShort(new Date(readingTimestamp))], false));
                            });
                            window.down('#error-label').setText('<div style="color: #EB5642">' +
                                Uni.I18n.translate('output.correctionErrorMessage', 'IMT', 'Could not correct {0}',
                                    listOfFailedReadings.join(', ')) + '</div>', false);
                        } else if (responseText.errors) {
                            window.down('#form-errors').show();
                            window.down('#property-form').markInvalid(responseText.errors);
                        } else {
                            window.destroy();
                        }
                    }

                }
                Ext.resumeLayouts(true);
            }
        });

    }
});