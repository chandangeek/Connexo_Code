Ext.define('Imt.purpose.controller.Readings', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router',
        'Imt.purpose.view.Outputs',
        'Imt.purpose.store.Outputs',
        'Imt.purpose.view.OutputChannelMain',
        'Imt.purpose.view.ReadingsList',
        'Uni.store.DataIntervalAndZoomLevels',
        'Imt.purpose.view.DataBulkActionMenu'
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
                // selectionchange: this.onDataGridSelectionChange
            },
            // '#readings-list #purpose-readings-data-bulk-action-menu': {
            //     click: this.chooseBulkAction
            // },
            'purpose-readings-data-action-menu': {
                beforeshow: this.checkSuspect,
                click: this.chooseAction
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
            case 'resetReading':
                // me.removeReadings(menu.record);
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
});