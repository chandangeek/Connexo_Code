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
        'Imt.purpose.store.EstimationRules',
        'Imt.purpose.store.HistoricalChannelReadings',
        'Imt.purpose.store.HistoricalRegisterReadings'
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
        'Imt.purpose.view.ValidationStatusForm',
        'Imt.purpose.view.history.History'
    ],

    mixins: {
        viewHistoryActionListener: 'Imt.purpose.util.ViewHistoryActionListener'
    },

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
            selector: 'output-channel-main interval-reading-preview'
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
        },
        {
            ref: 'outputReadingsFilterPanel',
            selector: 'output-readings #output-readings-topfilter'
        },
        {
            ref: 'historyRegisterDataPreviewPanel',
            selector: 'output-readings-history history-register-preview'
        },
        {
            ref: 'historyIntervalDataPreviewPanel',
            selector: 'output-readings-history history-interval-preview'
        }
    ],

    init: function () {
        this.control({
            '#readings-list': {
                edit: this.resumeEditorFieldValidation,
                canceledit: this.resumeEditorFieldValidation,
                selectionchange: this.onDataGridSelectionChange,
                select: this.showPreview,
                beforeedit: this.beforeEditRecord,
                paste: this.onPaste
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
            '#readings-list #pre-validate-button': {
                click: this.preValidateReadings
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
            },
            'output-readings-history-grid': {
                select: this.showHistoryPreview
            }
        });
    },
    valueBeforeEdit: 0,
    numberOfPotentialSuspects: 0,

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
            case 'viewHistory':
                me.moveToHistoryPage.call(me, menu.record, true);
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
            case 'viewHistory':
                me.moveToHistoryPage.call(me, menu.record);
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
        me.getReadingsList().down('#pre-validate-button').setDisabled(disabled);
        me.numberOfPotentialSuspects = 0;
    },

    checkSuspect: function (menu) {
        var me = this,
            validationResult = menu.record.get('validationResult') === 'validationStatus.suspect' ||
                menu.record.get('estimatedNotSaved') === true,
            canClearProjected = menu.record.get('isProjected') === true,
            canMarkProjected = menu.record.get('isProjected') === false && (menu.record.isModified('value') || menu.record.get('ruleId') !== 0 || !Ext.isEmpty(menu.record.get('modificationState'))),
            canEditingComment = menu.record.get('estimatedByRule'),
            flagForComment = function (value) {
                if (value === 'EDITED' || value === 'ESTIMATED' || value === 'REMOVED') {
                    return true;
                } else {
                    return false;
                }
            };

        Ext.suspendLayouts();
        if (!canEditingComment && menu.record.get('modificationState') && menu.record.get('modificationState').flag) {
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
        menu.reorderItems();
        Ext.resumeLayouts();
    },


    resumeEditorFieldValidation: function (editor, event, doNotSelect) {
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

            if ((event.record.isModified('value') && this.valueBeforeEdit !== event.record.get('value')) || (event.record.get('potentialSuspect') && doNotSelect)) {
                grid.down('#save-changes-button').isDisabled() && me.showButtons();

                Ext.suspendLayouts();
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
                        color: event.record.get('potentialSuspect') ? 'rgba(255, 0, 0, 0.3)' : 'rgba(112,187,81,0.3)',
                        value: value
                    };
                    point.update(updatedObj, !doNotSelect);
                    point.select(false);
                    me.getOutputReadings().down('#output-readings-preview-container').fireEvent('rowselect', event.record, null, doNotSelect);
                }

                if (event.column) {
                    event.record.set('validationResult', 'validationStatus.ok');
                    event.record.set('isProjected', false);
                    event.record.set('ruleId', 0);
                    if (!event.record.get('estimatedNotSaved')) {
                        event.record.set('modificationState', Uni.util.ReadingEditor.modificationState('EDITED'));
                    }
                    grid.getView().refreshNode(grid.getStore().indexOf(event.record));
                    event.record.get('confirmed') && event.record.set('confirmed', false);
                }
                Ext.resumeLayouts(true);
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

        if (record.get('potentialSuspect')) {
            record.beginEdit();
            record.set('potentialSuspect', false);
            record.set('validationRules', []);
            record.endEdit(true);
        }

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
            grid.down('#pre-validate-button').disable();
        }
        Ext.resumeLayouts();
    },

    undoChannelDataChanges: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.getRoute().forward(router.arguments, Uni.util.QueryString.getQueryStringValues());
    },

    saveChannelDataChanges: function (getConfirmationWindow) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            changedData = me.getChangedData(me.getStore('Imt.purpose.store.Readings')),
            viewport = Ext.ComponentQuery.query('viewport > #contentPanel')[0];

        if (Ext.isFunction(getConfirmationWindow)) {
            getConfirmationWindow().close();
        }
        if (!Ext.isEmpty(changedData)) {
            viewport.setLoading();
            Ext.Ajax.request({
                url: Ext.String.format('/api/udr/usagepoints/{0}/purposes/{1}/outputs/{2}/channelData', router.arguments.usagePointId, router.arguments.purposeId, router.arguments.outputId),
                method: 'PUT',
                jsonData: Ext.encode(changedData),
                timeout: 300000,
                success: function () {
                    router.getRoute().forward(router.arguments, Uni.util.QueryString.getQueryStringValues());
                    if (me.numberOfPotentialSuspects > 0) {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translatePlural('channelData.successMsg.potentialSuspects.', me.numberOfPotentialSuspects, 'IMT', null,
                            'Channel data have been saved with {0} potential suspect', 'Channel data have been saved with {0} potential suspects'));
                    } else {
                        me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('devicechannels.successSavingMessage', 'IMT', 'Channel data have been saved'));
                    }
                },
                failure: function (response) {
                    viewport.setLoading(false);
                }
            });
        }
    },

    getChangedData: function (store) {
        var changedData = [],
            changedRecord,
            confirmedObj;

        Ext.Array.each(store.getUpdatedRecords(), function (record) {
            confirmedObj = null;
            changedRecord = {
                interval: record.get('interval')
            };

            if (record.get('removedNotSaved')) {
                confirmedObj = {
                    interval: record.get('interval'),
                    value: null,
                    commentId: record.get('commentId') ? record.get('commentId') : undefined
                };
            } else if (record.get('confirmed')) {
                confirmedObj = {
                    interval: record.get('interval'),
                    isConfirmed: record.get('confirmedNotSaved') || false
                };
            } else if (record.get('ruleId')) {
                Ext.merge(changedRecord, _.pick(record.getData(), 'value', 'ruleId', 'isProjected'));
            } else if (record.isModified('value') || record.isModified('isProjected')) {
                Ext.merge(changedRecord, _.pick(record.getData(), 'value', 'isProjected'));
            } else if (record.isModified('collectedValue')) {
                Ext.merge(changedRecord, _.pick(record.getData(), 'collectedValue', 'isProjected'));
            }

            if (confirmedObj) {
                changedData.push(confirmedObj);
            } else {
                changedRecord.value = record.get('value');
                changedRecord.commentId = record.get('commentId') ? record.get('commentId') : undefined;
                changedData.push(changedRecord);
            }
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
                if (value === 'EDITED' || value === 'ESTIMATED' || value === 'REMOVED') {
                    return true;
                } else {
                    return false;
                }
            };

        Ext.Array.each(selectedRecords, function (record) {
            if (canEstimate && canConfirm && canReset) {
                return false;
            }
            canEditingComment = canEditingComment ? record.get('estimatedByRule') : false;
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
        menu.down('#edit-estimation-comment') && menu.down('#edit-estimation-comment').setVisible(canEditingComment);
        menu.down('#copy-form-value') && menu.down('#copy-form-value').setVisible(canCopyFromReference);
        menu.down('#confirm-value') && menu.down('#confirm-value').setVisible(canConfirm);
        menu.down('#reset-value') && menu.down('#reset-value').setVisible(canReset);
        menu.down('#correct-value') && menu.down('#correct-value').setVisible(canCorrect);
        menu.down('#clear-projected') && menu.down('#clear-projected').setVisible(canClearProjected);
        menu.down('#mark-projected') && menu.down('#mark-projected').setVisible(canMarkProjected);
        menu.reorderItems();
        button.setDisabled(!selectedRecords.length || !menu.query('menuitem[hidden=false]').length);
        Ext.resumeLayouts();
    },

    onPaste: function (grid, event) {
        event && event.record && event.record.set('modificationState', Uni.util.ReadingEditor.modificationState('EDITED'));
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
                estimatedCommentNotSaved: true,
                commentId: commentId ? commentId : 0,
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
            limit: 50
        };
    },

    copyFromReferenceUpdateGrid: function () {
        var me = this,
            intervals = [],
            window = me.getCopyFromReferenceWindow(),
            form = window.down('#reading-copy-window-form'),
            changedData = me.getChangedData(me.getStore('Imt.purpose.store.Readings')),
            model = Ext.create('Imt.purpose.model.CopyFromReference'),
            router = me.getController('Uni.controller.history.Router'),
            commentCombo = window.down('#estimation-comment-box'),
            commentId = commentCombo.getValue(),
            commentValue = commentCombo.getRawValue(),
            readings = window.records;

        form.updateRecord(model);
        model.getProxy().extraParams = {
            usagePointId: router.arguments.usagePointId,
            purposeId: router.arguments.purposeId,
            outputId: router.arguments.outputId
        };

        if (!Array.isArray(readings)) {
            readings = [readings];
        }
        _.each(readings, function (reading) {
            intervals.push(reading.get('interval'));
        });

        model.set('intervals', intervals);
        model.set('editedReadings', changedData);
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
                        if (item) {
                            reading.set('value', item.value);
                            reading.set('isProjected', model.get('projectedValue'));
                            reading.set('estimatedByRule', undefined);
                            reading.set('modificationState', Uni.util.ReadingEditor.modificationState('EDITED'));
                            reading.set('validationResult', 'validationStatus.ok');
                            if (commentId !== -1) {
                                reading.modified['value'] = reading.get('value');
                                reading.set('estimatedCommentNotSaved', true);
                                reading.set('commentId', commentId ? commentId : 0);
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
            intervalsArray = [],
            comment = null;

        if (commentId !== -1) {
            comment = {
                commentId: commentId ? commentId : 0,
                commentValue: commentValue
            }
        }

        !window.down('#form-errors').isHidden() && window.down('#form-errors').hide();
        !window.down('#error-label').isHidden() && window.down('#error-label').hide();

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
        } else {
            Ext.Array.each(record, function (item) {
                intervalsArray.push({
                    start: item.get('interval').start,
                    end: item.get('interval').end
                });
            });
        }
        model.set('intervals', intervalsArray);
        model.set('markAsProjected', markAsProjected);
        me.saveChannelDataEstimateModel(model, record, window, null, 'editWithEstimator', comment);
    },

    estimateReadingWithRule: function () {
        var me = this,
            window = me.getReadingEstimationWithRuleWindow(),
            estimationRuleId = window.down('#estimation-rule-field').getValue(),
            propertyForm = window.down('#property-form'),
            model = Ext.create('Imt.purpose.model.ChannelDataEstimate'),
            commentCombo = window.down('#estimation-comment'),
            commentId = commentCombo.commentId,
            commentValue = commentCombo.getValue(),
            record = window.record,
            markAsProjected,
            intervalsArray = [],
            comment = null;

        if (commentId !== -1) {
            comment = {
                commentId: commentId ? commentId : 0,
                commentValue: commentValue
            }
        }


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
        } else {
            Ext.Array.each(record, function (item) {
                intervalsArray.push({
                    start: item.get('interval').start,
                    end: item.get('interval').end
                });
            });
        }
        model.set('intervals', intervalsArray);
        model.set('markAsProjected', markAsProjected);
        me.saveChannelDataEstimateModel(model, record, window, estimationRuleId, 'estimate', comment);
    },

    saveChannelDataEstimateModel: function (record, readings, window, ruleId, action, comment) {
        var me = this,
            grid = me.getReadingsList(),
            changedData = me.getChangedData(me.getStore('Imt.purpose.store.Readings')),
            router = me.getController('Uni.controller.history.Router'),
            adjustedPropertyFormErrors;

        record.set('editedReadings', changedData);
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
                        if (comment) {
                            readings.set('commentId', comment.commentId);
                            readings.set('commentValue', comment.commentValue);
                            readings.set('estimatedCommentNotSaved', true);
                        }
                        me.updateEstimatedValues(record, readings, responseText[0], ruleId, action);
                    } else {
                        Ext.Array.each(responseText, function (estimatedReading) {
                            Ext.Array.findBy(readings, function (reading) {
                                if (estimatedReading.interval.start == reading.get('interval').start) {
                                    if (comment) {
                                        reading.set('commentId', comment.commentId);
                                        reading.set('commentValue', comment.commentValue);
                                        reading.set('estimatedCommentNotSaved', true);
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
                                listOfFailedReadings.push(Uni.DateTime.formatDateTimeShort(new Date(readingTimestamp)));
                            });
                            var errorMessage = window.down('#estimator-field') ? Uni.I18n.translate('output.estimationErrorMessageWithIntervals', 'IMT', 'Could not estimate {0} with {1}',
                                [listOfFailedReadings.join(', '), window.down('#estimator-field').getRawValue()]) : Uni.I18n.translate('output.estimationErrorMessage', 'IMT', 'Could not estimate {0}',
                                listOfFailedReadings.join(', '));
                            window.down('#error-label').setText('<div style="color: #EB5642">' + errorMessage + '</div>', false);
                        } else if (responseText.errors) {
                            window.down('#form-errors').show();
                            if (Ext.isArray(responseText.errors)) {
                                adjustedPropertyFormErrors = responseText.errors.map(function (error) {
                                    if (Ext.String.startsWith(error.id, 'properties.')) {
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
            reading.set('modificationState', Uni.util.ReadingEditor.modificationState(null));
        }
        if (action === 'editWithEstimator') {
            reading.set('estimatedNotSaved', false);
            reading.set('modificationState', Uni.util.ReadingEditor.modificationState('EDITED'));
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
        reading.set('value', correctedInterval.value);
        reading.set('isProjected', model.get('projected'));
        if (reading.isModified('value')) {
            reading.set('ruleId', null);
            reading.set('modificationState', Uni.util.ReadingEditor.modificationState('EDITED'));
            reading.set('validationResult', 'validationStatus.ok');
        }


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
            changedData = me.getChangedData(me.getStore('Imt.purpose.store.Readings')),
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
                if(item.get('value') != ""){
                     intervalsArray.push({
                        start: item.get('interval').start,
                        end: item.get('interval').end
                    });}
            }
        });

        model.set('intervals', intervalsArray);
        model.set('editedReadings', changedData);
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
                                    reading.modified['value'] = reading.get('value');
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

    },

    preValidateReadings: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            store = me.getStore('Imt.purpose.store.Readings'),
            viewport = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            changedData = me.getChangedData(store),
            jsonData = {
                validateUntil: store.getAt(0).get('interval').end,
                editedReadings: me.getChangedData(store)
            },
            firstModifiedReadingTimestamp = changedData[changedData.length - 1].interval.end;

        viewport.setLoading();
        Ext.Ajax.request({
            url: Ext.String.format('/api/udr/usagepoints/{0}/purposes/{1}/outputs/{2}/channelData/prevalidate', router.arguments.usagePointId, router.arguments.purposeId, router.arguments.outputId),
            method: 'PUT',
            jsonData: Ext.encode(jsonData),
            success: function (response) {
                var responseText = Ext.decode(response.responseText, true);
                if (responseText.potentialSuspects) {
                    responseText.potentialSuspects.length ? me.showPotentialSuspectsWindow(firstModifiedReadingTimestamp, responseText) : me.showEmptyPotentialSuspectsWindow(firstModifiedReadingTimestamp);
                }
            },
            callback: function () {
                viewport.setLoading(false);
            }
        });
    },

    showEmptyPotentialSuspectsWindow: function (firstModifiedReadingTimestamp) {
        var me = this,
            confirmationWindow = Ext.widget('confirmation-window', {
                itemId: 'empty-potential-suspects-window',
                closeAction: 'destroy',
                green: true,
                confirmText: Uni.I18n.translate('general.saveChanges', 'IMT', 'Save changes'),
                cancelText: Uni.I18n.translate('general.close', 'IMT', 'Close'),
                confirmation: me.saveChannelDataChanges.bind(me, getConfirmationWindow),
                listeners: {
                    close: function () {
                        Ext.ComponentQuery.query('#contentPanel')[0].setLoading();
                        Ext.defer(me.resetPotentialSuspects, 100, me, [true]);
                    }
                }
            });

        confirmationWindow.show({
            title: Uni.I18n.translate('preValidate.noPotentialSuspectsFound', 'IMT', 'No potential suspect readings found.'),
            msg: Uni.I18n.translate('preValidate.noPotentialSuspectsFoundMsg', 'IMT', 'No potential suspect readings found in visible part of data starting from {0}.', Uni.DateTime.formatDateTimeShort(firstModifiedReadingTimestamp), false)
        });

        function getConfirmationWindow() {
            return confirmationWindow
        }
    },

    showPotentialSuspectsWindow: function (firstModifiedReadingTimestamp, response) {
        var me = this,
            mainPage = Ext.ComponentQuery.query('#contentPanel')[0],
            confirmationWindow = Ext.widget('confirmation-window', {
                itemId: 'potential-suspects-window',
                closeAction: 'destroy',
                cancelText: Uni.I18n.translate('general.close', 'IMT', 'Close'),
                noConfirmBtn: true,
                listeners: {
                    close: function () {
                        mainPage.setLoading();
                        Ext.defer(function() {
                            var me = this,
                                grid = me.getReadingsList(),
                                store = grid.getStore(),
                                record,
                                index;

                            me.resetPotentialSuspects();
                            Ext.suspendLayouts();
                            response.potentialSuspects.forEach(function(potentialSuspect) {
                                index = store.findBy(function(item) {
                                    return item.get('interval').end === potentialSuspect.readingTime;
                                });
                                record = store.getAt(index);
                                record.beginEdit();
                                record.set('potentialSuspect', true);
                                record.set('validationRules', potentialSuspect.validationRules);
                                record.endEdit(true);
                                grid.getView().refreshNode(index);
                                me.resumeEditorFieldValidation(grid.editingPlugin, {
                                    record: record
                                }, true);
                            });
                            me.getReadingsGraph().chart.redraw();
                            Ext.resumeLayouts(true);
                            me.numberOfPotentialSuspects = response.potentialSuspects.length;
                            mainPage.setLoading(false);
                        }, 100, me);
                    }
                }
            });

        confirmationWindow.show({
            title: Uni.I18n.translatePlural('preValidate.potentialSuspectsFound', response.total, 'IMT', null, '{0} potential suspect reading found.', '{0} potential suspect readings found.'),
            msg: Uni.I18n.translate('preValidate.potentialSuspectsFoundMsg', 'IMT', 'There are potential suspects in visible part of data starting from {0}.', [Uni.DateTime.formatDateTimeShort(firstModifiedReadingTimestamp)], false)
        });
    },

    resetPotentialSuspects: function (redrawChart) {
        var me = this,
            grid = me.getReadingsList(),
            store = grid.getStore();

        if (redrawChart) {
            me.numberOfPotentialSuspects = 0;
            Ext.suspendLayouts();
        }
        store.getRange().forEach(function(reading) {
            if (reading.get('potentialSuspect')) {
                reading.beginEdit();
                reading.set('potentialSuspect', false);
                reading.set('validationRules', []);
                reading.endEdit(true);
                grid.getView().refreshNode(store.indexOf(reading));
                me.resumeEditorFieldValidation(grid.editingPlugin, {
                    record: reading
                }, true);
            }
        });
        if (redrawChart) {
            me.getReadingsGraph().chart.redraw();
            Ext.resumeLayouts(true);
            Ext.ComponentQuery.query('#contentPanel')[0].setLoading(false);
        }
    },

    viewHistory: function (usagePointId, purposeId, outputId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            dependenciesCounter = 2,
            app = me.getApplication(),
            usagePointsController = me.getController('Imt.usagepointmanagement.controller.View'),
            outputModel = me.getModel('Imt.purpose.model.Output'),
            intervalStore = me.getStore('Uni.store.DataIntervalAndZoomLevels'),
            isBulk = router.queryParams.changedDataOnly === 'yes',
            historyStore,
            widget,
            usagePoint,
            purposes,
            output,
            interval,
            durations,
            filterDefault,
            displayPage = function () {
                dependenciesCounter--;
                if (!dependenciesCounter) {
                    app.fireEvent('output-loaded', output);
                    if (isBulk) {
                        durations = Ext.create('Uni.store.Durations');
                        filterDefault = {
                            durationStore: durations,
                            defaultFromDate: new Date(Number(router.queryParams.interval.split('-')[0])),
                            duration: router.queryParams.interval.split('-')[1]
                        };
                        if (output.get('outputType') === 'channel') {
                            interval = intervalStore.getIntervalRecord(output.get('interval'));
                            durations.loadData(interval.get('duration'));
                        }
                    } else {
                        var fromDateMilis = router.queryParams.endInterval.split('-')[0];
                        var toDateMilis = router.queryParams.endInterval.split('-')[1];
                        if(isNaN(fromDateMilis)){
                            fromDateMilis = toDateMilis;
                        }
                        filterDefault = {
                            defaultFromDate: new Date(Number(fromDateMilis)),
                            defaultToDate: new Date(Number(toDateMilis))
                        };
                    }
                    widget = Ext.widget('output-readings-history', {
                        itemId: 'output-readings-history',
                        router: router,
                        usagePoint: usagePoint,
                        purposes: purposes,
                        output: output,
                        filterDefault: filterDefault,
                        isBulk: isBulk,
                        store: historyStore
                    });
                    app.fireEvent('changecontentevent', widget);
                    historyStore.load(function () {
                        mainView.setLoading(false);
                    });
                }
            };

        mainView.setLoading();
        usagePointsController.loadUsagePoint(usagePointId, {
            success: function (types, up, records) {
                usagePoint = up;
                purposes = records;
                displayPage();
            }
        });

        outputModel.getProxy().extraParams = {usagePointId: usagePointId, purposeId: purposeId};
        outputModel.load(outputId, {
            success: function (record) {
                output = record;
                if (output.get('outputType') === 'channel') {
                    historyStore = me.getStore('Imt.purpose.store.HistoricalChannelReadings');
                } else {
                    historyStore = me.getStore('Imt.purpose.store.HistoricalRegisterReadings');
                }
                historyStore.getProxy().setUrl(usagePointId, purposeId, outputId);
                displayPage();
            }
        });
    },

    showHistoryPreview: function (selectionModel, record) {
        if (selectionModel.getSelection().length === 1) {
            if (record.get('type') === 'OutputRegisterHistoryDataInfo'){
                this.getHistoryRegisterDataPreviewPanel().updateForm(record);
            } else {
                this.getHistoryIntervalDataPreviewPanel().updateForm(record);
            }

        }
    }
});
