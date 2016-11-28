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
        'Imt.purpose.view.MultipleReadingsActionMenu'
    ],

    stores: [
        'Imt.purpose.store.Outputs',
        'Imt.purpose.store.Readings',
        'Uni.store.DataIntervalAndZoomLevels',
        'Imt.purpose.store.RegisterReadings',
        'Imt.usagepointmanagement.store.UsagePointTypes'
    ],

    models: [
        'Imt.purpose.model.Output',
        'Imt.purpose.model.Reading',
        'Imt.usagepointmanagement.model.ValidationInfo',
        'Imt.usagepointmanagement.model.SuspectReason',
        'Imt.usagepointmanagement.model.Purpose',
        'Imt.usagepointmanagement.model.UsagePoint'
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
            ref: 'readingsGraph',
            selector: '#output-readings #readings-graph'
        }
    ],

    init: function () {
        this.control({
            '#readings-list': {
                // select: this.showPreview,
                // beforeedit: this.beforeEditRecord,
                edit: this.resumeEditorFieldValidation,
                canceledit: this.resumeEditorFieldValidation,
                selectionchange: this.onDataGridSelectionChange
                // selectionchange: this.onDataGridSelectionChange
            },
            // '#readings-list #purpose-readings-data-bulk-action-menu': {
            //     click: this.chooseBulkAction
            // },
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
        });
    },

    // chooseBulkAction: function (menu, item) {
    //     var me = this,
    //         records = me.getReadingsList().getSelectionModel().getSelection();
    //
    //     switch (item.action) {
    //         case 'estimateValue':
    //             // me.estimateValue(records);
    //             break;
    //         case 'confirmValue':
    //             me.confirmValue(records, true);
    //             break;
    //         case 'removeReadings':
    //             // me.removeReadings(records, true);
    //             break;
    //     }
    // },

    chooseAction: function (menu, item) {
        var me = this;

        switch (item.action) {
            case 'editValue':
                me.getReadingsList().getPlugin('cellplugin').startEdit(menu.record, 1);
                break;
            case 'resetValue':
                me.removeReadings(menu.record);
                break;
            case 'estimateValue':
                // me.estimateValue(menu.record);
                break;
            case 'confirmValue':
                me.confirmValue(menu.record, false);
                break;
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
                        rec.set('confirmed', true);
                    }
                }
            };

        if (isBulk) {
            Ext.Array.each(record, function (reading) {
                func(reading);
            });
        } else {
            func(record);
        }

        me.getReadingsList().down('#save-changes-button').isDisabled() && me.showButtons();
    },

    showButtons: function () {
        var me = this;

        me.getReadingsList().down('#save-changes-button').enable();
        me.getReadingsList().down('#undo-button').enable();
    },

    checkSuspect: function (menu) {
        var validationResult = menu.record.get('validationResult') == 'validationStatus.suspect';


        menu.down('#estimate-value').setVisible(validationResult);
        if (menu.record.get('confirmed') || menu.record.isModified('value')) {
            menu.down('#confirm-value').hide();
        } else {
            menu.down('#confirm-value').setVisible(validationResult);
        }

        if (menu.down('#reset-value')) {
            menu.down('#reset-value').setVisible(menu.record.get('value'));
        }
    },


    resumeEditorFieldValidation: function (editor, event) {
        var me = this,
            chart = me.getReadingsGraph().chart,
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

            if (!event.record.get('value')) {
                point.update({y: null});
            } else {
                if (event.record.get('plotBand')) {
                    chart.xAxis[0].removePlotBand(event.record.get('interval').start);
                    event.record.set('plotBand', false);
                }
                updatedObj = {
                    y: parseFloat(value),
                    color: 'rgba(112,187,81,0.3)',
                    value: value  // Change value or not?
                };
                point.update(updatedObj);
            }

            if (event.column) {
                event.record.set('validationResult', 'validationStatus.ok');
                grid.getView().refreshNode(grid.getStore().indexOf(event.record));
                event.record.get('confirmed') && event.record.set('confirmed', false);
            }
        } else if (condition) {
            me.resetChanges(event.record, point);
        }
    },

    resetChanges: function (record, point) {
        var me = this,
            properties = record.get('readingProperties'),
            grid = me.getReadingsList(),
            store = grid.getStore(),
            color = '#70BB51';

        if (record.get('estimatedByRule')) {
            color = '#568343';
        } else if (properties.delta.notValidated) {
            color = '#71adc7';
        } else if (properties.delta.suspect) {
            color = 'rgba(235, 86, 66, 1)';
        } else if (properties.delta.informative) {
            color = '#dedc49';
        }

        record.get('confirmed') && record.set('confirmed', false);
        grid.getView().refreshNode(store.indexOf(record));
        point.update({
            y: parseFloat(record.get('value')),
            color: color,
            value: record.get('value')
        });
        record.reject();
        if (!store.getUpdatedRecords().length) {
            me.getPage().down('#save-changes-button').disable();
            me.getPage().down('#undo-button').disable();
        }
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

        viewport.setLoading();
        if (!Ext.isEmpty(changedData)) {
            Ext.Ajax.request({
                url: Ext.String.format('/api/ddr/devices/{0}/channels/{1}/data', Uni.util.Common.encodeURIComponent(router.arguments.deviceId), router.arguments.channelId),
                method: 'PUT',
                jsonData: Ext.encode(changedData),
                timeout: 300000,
                success: function () {
                    router.getRoute().forward(router.arguments, Uni.util.QueryString.getQueryStringValues());
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('devicechannels.successSavingMessage', 'IMT', 'Channel data have been saved'));
                },
                failure: function (response) {
                    viewport.setLoading(false);
                    // if (response.status == 400) {
                    //     var failureResponseText = Ext.decode(response.responseText, true);
                    //     if (failureResponseText && failureResponseText.error !== 'cannotAddChannelValueWhenLinkedToSlave') {
                    //         Ext.create('Uni.view.window.Confirmation', {
                    //             confirmText: Uni.I18n.translate('general.retry', 'MDC', 'Retry'),
                    //             closeAction: 'destroy',
                    //             confirmation: function () {
                    //                 this.close();
                    //                 me.saveChannelDataChanges();
                    //             },
                    //             cancellation: function () {
                    //                 this.close();
                    //                 router.getRoute().forward(router.arguments, router.queryParams);
                    //             }
                    //         }).show({
                    //             msg: failureResponseText.message ? failureResponseText.message :
                    //                 Uni.I18n.translate('general.emptyField', 'MDC', 'Value field can not be empty'),
                    //             title: failureResponseText.error ? failureResponseText.error :
                    //                 Uni.I18n.translate('general.during.editing', 'MDC', 'Error during editing')
                    //         });
                    //     }
                    // }
                }
            });
        }
    },

    getChangedData: function (store) {
        var changedData = [],
            confirmedObj;

        Ext.Array.each(store.getUpdatedRecords(), function (record) {
            if (record.get('confirmed')) {
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
            button = me.getReadingsList().down('#readings-bulk-action-button'),
            menu = button.down('menu');

        Ext.suspendLayouts();
        var suspects = selectedRecords.filter(function (record) {
            return record.get('validationResult') == 'validationStatus.suspect';
        });
        menu.down('#estimate-value').setVisible(suspects.length);

        var confirms = suspects.filter(function (record) {
            return !record.get('confirmed') && !record.isModified('value')
        });

        menu.down('#confirm-value').setVisible(confirms.length);
        menu.down('#reset-value').setVisible(_.find(selectedRecords, function (record) {
            return record.get('value') || record.get('collectedValue')
        }));
        button.setDisabled(!menu.query('menuitem[hidden=false]').length);
        Ext.resumeLayouts();
    },

    removeReadings: function (records) {
        var me = this,
            point,
            grid = me.getReadingsList(),
            store = grid.getStore(),
            gridView = grid.getView(),
            chart = me.getReadingsGraph().chart;

        Ext.suspendLayouts();
        Ext.Array.each(records, function (record) {
            record.beginEdit();
            record.set('value', null);
            // if (record.get('intervalFlags').length) {
            //     record.set('intervalFlags', []);
            // }
            if (record.get('confirmed')) {
                record.set('confirmed', false);
            }
            record.set('validationResult','validationStatus.ok');
            record.endEdit(true);
            gridView.refreshNode(store.indexOf(record));
            point = chart.get(record.get('interval').start);
            point.update({y: null}, false);
        });
        chart.redraw();
        me.showButtons();
        Ext.resumeLayouts(true);
    },
});