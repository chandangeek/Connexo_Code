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
        'Imt.purpose.view.ReadingEstimationWithRuleWindow'
    ],

    stores: [
        'Imt.purpose.store.Outputs',
        'Imt.purpose.store.Readings',
        'Uni.store.DataIntervalAndZoomLevels',
        'Imt.purpose.store.RegisterReadings',
        'Imt.usagepointmanagement.store.UsagePointTypes',
        'Imt.purpose.store.Estimators'
    ],

    models: [
        'Imt.purpose.model.Output',
        'Imt.purpose.model.Reading',
        'Imt.usagepointmanagement.model.ValidationInfo',
        'Imt.usagepointmanagement.model.SuspectReason',
        'Imt.usagepointmanagement.model.Purpose',
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.purpose.model.ChannelDataEstimate'
    ],

    views: [
        'Imt.purpose.view.Outputs',
        'Imt.purpose.view.OutputChannelMain',
        'Imt.purpose.view.ValidationStatusForm'
    ],

    refs: [
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
            ref: 'readingEstimationWindow',
            selector: 'reading-estimation-window'
        },
        {
            ref: 'readingEstimationWithRuleWindow',
            selector: 'reading-estimation-with-rule-window'
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
            'reading-estimation-window #estimate-reading-button': {
                click: this.estimateReadingWithEstimator
            },
            'reading-estimation-with-rule-window #estimate-reading-button': {
                click: this.estimateReadingWithRule
            }
        });
    },

    chooseBulkAction: function (menu, item) {
        var me = this,
            records = me.getReadingsList().getSelectionModel().getSelection();

        switch (item.action) {
            case 'estimateValue':
                me.estimateValue(records);
                break;
            case 'estimateWithRule':
                me.estimateWithRule(records);
                break;
            case 'confirmValue':
                me.confirmValue(records, true);
                break;
            case 'resetValue':
                me.resetReadings(records, true);
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
            case 'estimateValue':
                me.estimateValue(menu.record);
                break;
            case 'estimateWithRule':
                me.estimateWithRule(menu.record);
                break;
            case 'confirmValue':
                me.confirmValue(menu.record, false);
                break;
        }
    },

    beforeEditRecord: function (editor, context) {
        var intervalFlags = context.record.get('intervalFlags');
        context.column.getEditor().allowBlank = !(intervalFlags && intervalFlags.length);
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
        var me = this;

        Ext.suspendLayouts();
        me.getReadingsList().down('#save-changes-button').enable();
        me.getReadingsList().down('#undo-button').enable();
        Ext.resumeLayouts();
    },

    checkSuspect: function (menu) {
        var me = this,
            validationResult = menu.record.get('validationResult') == 'validationStatus.suspect',
            estimationRulesCount = me.getStore('Imt.purpose.store.EstimationRules').getCount();
        Ext.suspendLayouts();
        menu.down('#estimate-value').setVisible(validationResult);
        menu.down('#estimate-value-with-rule').setVisible(estimationRulesCount);
        if (menu.record.get('confirmed') || menu.record.isModified('value')) {
            menu.down('#confirm-value').hide();
        } else {
            menu.down('#confirm-value').setVisible(validationResult);
        }

        if (menu.down('#reset-value')) {
            menu.down('#reset-value').setVisible(menu.record.get('estimatedByRule') || menu.record.get('modificationFlag') == "EDITED" || menu.record.get('modificationFlag') == "ADDED");
        }
        Ext.resumeLayouts();
    },


    resumeEditorFieldValidation: function (editor, event) {
        var me = this;

        if(me.getReadingsGraph() && me.getReadingsGraph().chart){
            var chart = me.getReadingsGraph().chart,
                point = chart.get(event.record.get('interval').start),
                grid = me.getReadingsList(),
                value = event.record.get('value'),
                condition = (isNaN(point.y) && isNaN(value)) ? false : (point.y != value),
                updatedObj;
            if (event.column) {
                event.column.getEditor().allowBlank = true;
            }

            if (event.record.isModified('value')) {
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

                if (event.column) {
                    event.record.set('validationResult', 'validationStatus.ok');
                    grid.getView().refreshNode(grid.getStore().indexOf(event.record));
                    event.record.get('confirmed') && event.record.set('confirmed', false);
                }
                Ext.resumeLayouts();
            } else if (condition) {
                me.resetChanges(event.record, point);
            }
        }
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
        window.location.replace(router.getRoute().buildUrl());

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
        var changedData = [],
            confirmedObj;

        Ext.Array.each(store.getUpdatedRecords(), function (record) {
            if (record.get('removedNotSaved')) {
                confirmedObj = {
                    interval: record.get('interval'),
                    value: null
                };
                changedData.push(confirmedObj);
            } else if (record.get('confirmed')) {
                confirmedObj = {
                    interval: record.get('interval'),
                    isConfirmed: record.get('confirmedNotSaved') || false
                };
                changedData.push(confirmedObj);
            } else if (record.isModified('value')) {
                changedData.push(_.pick(record.getData(), 'interval', 'value'));
            } else if (record.isModified('collectedValue')) {
                changedData.push(_.pick(record.getData(), 'interval', 'collectedValue'));
            }
        });

        return changedData;
    },

    onDataGridSelectionChange: function (selectionModel, selectedRecords) {
        var me = this,
            canEstimate = false,
            canConfirm = false,
            canReset = false,
            button = me.getReadingsList().down('#readings-bulk-action-button'),
            menu = button.down('menu');

        selectedRecords.forEach(function (record) {
            if (canEstimate && canConfirm && canReset) {
                return false;
            }
            if (!canEstimate && record.get('validationResult') == 'validationStatus.suspect') {
                canEstimate = true;
                if (!canConfirm && !record.get('isConfirmed') && !record.isModified('value')) {
                    canConfirm = true;
                }
            }
            if (!canReset && (record.get('estimatedByRule') || record.get('modificationFlag') == "EDITED" || record.get('modificationFlag') == "ADDED")) {
                canReset = true;
            }
        });

        Ext.suspendLayouts();
        menu.down('#estimate-value').setVisible(canEstimate);
        menu.down('#confirm-value').setVisible(canConfirm);
        menu.down('#reset-value').setVisible(canReset);
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
            record.set('removedNotSaved', true);
            record.set('value', calculatedValue);
            if (record.get('confirmed')) {
                record.set('confirmed', false);
            }
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
        Ext.widget('reading-estimation-with-rule-window', {
            itemId: 'channel-reading-estimation-with-rule-window',
            record: record
        }).show();
    },

    estimateReadingWithEstimator: function () {
        var me = this,
            window = me.getReadingEstimationWindow(),
            estimator = window.down('#estimator-field').getValue(),
            propertyForm = window.down('#property-form'),
            model = Ext.create('Imt.purpose.model.ChannelDataEstimate'),
            record = window.record,
            intervalsArray = [];


        !window.down('#form-errors').isHidden() && window.down('#form-errors').hide();
        !window.down('#error-label').isHidden() && window.down('#error-label').hide();

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
        me.saveChannelDataEstimateModel(model, record, window);
    },

    estimateReadingWithRule: function () {
        var me = this,
            window = me.getReadingEstimationWithRuleWindow(),
            estimationRuleId = window.down('#estimation-rule-field').getValue(),
            propertyForm = window.down('#property-form'),
            model = Ext.create('Imt.purpose.model.ChannelDataEstimate'),
            record = window.record,
            intervalsArray = [];


        !window.down('#form-errors').isHidden() && window.down('#form-errors').hide();
        !window.down('#error-label').isHidden() && window.down('#error-label').hide();

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
        me.saveChannelDataEstimateModel(model, record, window, estimationRuleId);
    },

    // estimateReading: function (window, estimator) {
    //     var me = this,
    //         window = me.getReadingEstimationWithRuleWindow(),
    //         estimator = window.down('#estimator-field').getValue(),
    //         propertyForm = window.down('#property-form'),
    //         model = Ext.create('Imt.purpose.model.ChannelDataEstimate'),
    //         record = window.record,
    //         intervalsArray = [];
    //
    //
    //     !window.down('#form-errors').isHidden() && window.down('#form-errors').hide();
    //     !window.down('#error-label').isHidden() && window.down('#error-label').hide();
    //
    //     if (propertyForm.getRecord()) {
    //         model.set('estimatorImpl', estimator);
    //         model.propertiesStore = propertyForm.getRecord().properties();
    //     }
    //     if (!Ext.isArray(record)) {
    //         intervalsArray.push({
    //             start: record.get('interval').start,
    //             end: record.get('interval').end
    //         });
    //     } else {
    //         Ext.Array.each(record, function (item) {
    //             intervalsArray.push({
    //                 start: item.get('interval').start,
    //                 end: item.get('interval').end
    //             });
    //         });
    //     }
    //     model.set('intervals', intervalsArray);
    //     me.saveChannelDataEstimateModel(model, record, window);
    // },

    //TODO
    saveChannelDataEstimateModel: function (record, readings, window, ruleId) {
        var me = this,
            grid = me.getReadingsList(),
            router = me.getController('Uni.controller.history.Router');

        record.getProxy().setParams(encodeURIComponent(router.arguments.usagePointId), router.arguments.purposeId, router.arguments.outputId);
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
                        me.updateEstimatedValues(record, readings, responseText[0], ruleId);
                    } else {
                        Ext.Array.each(responseText, function (estimatedReading) {
                            Ext.Array.findBy(readings, function (reading) {
                                if (estimatedReading.interval.start == reading.get('interval').start) {
                                    me.updateEstimatedValues(record, reading, estimatedReading, ruleId);
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
                            window.down('#error-label').setText('<div style="color: #EB5642">' +
                                Uni.I18n.translate('output.estimationErrorMessage', 'IMT', 'Could not estimate {0} with {1}',
                                    [listOfFailedReadings.join(', '), window.down('#estimator-field').getRawValue().toLowerCase()]) + '</div>', false);
                        } else if (responseText.errors) {
                            window.down('#form-errors').show();
                            window.down('#property-form').markInvalid(responseText.errors);
                        }
                    }

                }
                Ext.resumeLayouts(true);
            }
        });
    },

    updateEstimatedValues: function (record, reading, estimatedReading, ruleId) {
        var me = this,
            grid = me.getReadingsList();

        reading.set('value', estimatedReading.value);
        ruleId && reading.set('ruleId', ruleId);
        reading.set('validationResult', 'validationStatus.ok');

        grid.getView().refreshNode(grid.getStore().indexOf(reading));

        me.resumeEditorFieldValidation(grid.editingPlugin, {
            record: reading
        });
        reading.get('confirmed') && reading.set('confirmed', false);
    }
});