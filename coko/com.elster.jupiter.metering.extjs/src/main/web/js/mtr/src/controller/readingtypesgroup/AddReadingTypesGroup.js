/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.controller.readingtypesgroup.AddReadingTypesGroup', {
    extend: 'Ext.app.Controller',

    views: [
        'Mtr.view.readingtypesgroup.AddReadingTypesGroup'
    ],

    requires: [
        'Uni.view.window.Confirmation'
    ],

    stores: [
        'Mtr.store.readingtypes.attributes.Interval',
        'Mtr.store.readingtypes.attributes.TimeOfUse',
        'Mtr.store.readingtypes.attributes.DataQualifier',
        'Mtr.store.readingtypes.attributes.Accumulation',
        'Mtr.store.readingtypes.attributes.DirectionOfFlow',
        'Mtr.store.readingtypes.attributes.Commodity',
        'Mtr.store.readingtypes.attributes.Kind',
        'Mtr.store.readingtypes.attributes.InterharmonicNumerator',
        'Mtr.store.readingtypes.attributes.InterharmonicDenominator',
        'Mtr.store.readingtypes.attributes.ArgumentNumerator',
        'Mtr.store.readingtypes.attributes.ArgumentDenominator',
        'Mtr.store.readingtypes.attributes.CriticalPeakPeriod',
        'Mtr.store.readingtypes.attributes.ConsumptionTier',
        'Mtr.store.readingtypes.attributes.Phase',
        'Mtr.store.readingtypes.attributes.Multiplier',
        'Mtr.store.readingtypes.attributes.UnitOfMeasures',
        'Mtr.store.readingtypes.attributes.Currency',
        'Mtr.store.readingtypes.attributes.MeasuringPeriod',


        'Mtr.store.readingtypesgroup.attributes.Commodity',
        'Mtr.store.readingtypesgroup.attributes.Kind',
        'Mtr.store.readingtypesgroup.attributes.DirectionOfFlow',
        'Mtr.store.readingtypesgroup.attributes.UnitOfMeasures',
        'Mtr.store.readingtypesgroup.attributes.MacroPeriod',
        'Mtr.store.readingtypesgroup.attributes.Accumulation',
        'Mtr.store.readingtypesgroup.attributes.MeasuringPeriod',
        'Mtr.store.readingtypesgroup.attributes.Aggregate',

        'Mtr.store.readingtypesgroup.attributes.Multiplier',
        'Mtr.store.readingtypesgroup.attributes.Phase',
        'Mtr.store.readingtypesgroup.attributes.TimeOfUse',
        'Mtr.store.readingtypesgroup.attributes.CriticalPeakPeriod',
        'Mtr.store.readingtypesgroup.attributes.ConsumptionTier'
    ],


    refs: [
        {
            ref: 'addReadingTypes',
            selector: '#add-reading-types-group'
        },
        {
            ref: 'addReadingTypeForm',
            selector: '#add-reading-types-group add-reading-types-group-form'
        },
        {
            ref: 'addReadingTypeFormErrorMessage',
            selector: '#add-reading-types-group add-reading-types-group-form #form-errors'
        },
        {
            ref: 'basicCommodity',
            selector: '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=commodity]'
        }
        ,
        {
            ref: 'basicMeasurementKind',
            selector: '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=measurementKind]'
        },
        {
            ref: 'basicUnit',
            selector: '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=unit]'
        },
        {
            ref: 'basicFlowDirection',
            selector: '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=flowDirection]'
        },
        {
            ref: 'basicMacroPeriod',
            selector: '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=macroPeriod]'
        },
        {
            ref: 'basicAccumulation',
            selector: '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=accumulation]'
        },
        {
            ref: 'basicMeasuringPeriod',
            selector: '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=measuringPeriod]'
        },
        {
            ref: 'basicAggregate',
            selector: '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=aggregate]'
        },
        {
            ref: 'basicMetricMultiplier',
            selector: '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=metricMultiplier]'
        },
        {
            ref: 'basicPhases',
            selector: '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=phases]'
        },
        {
            ref: 'basicTimeOfUse',
            selector: '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=tou]'
        },
        {
            ref: 'basicCriticalPeakPeriod',
            selector: '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=cpp]'
        },
        {
            ref: 'basicConsumptionTier',
            selector: '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=consumptionTier]'
        }
    ],

    qString: null,

    init: function () {
        var me = this;
        this.control({
            '#add-reading-types-group #add-reading-types-group-add-button': {
                click: this.addButtonClick
            },
            '#add-reading-types-group #add-reading-type-group-cancel-button': {
                click: this.goBack
            },
            // ,
            // '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox': {
            //     change: this.cimComboChange
            // },
            '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=commodity]': {
                change: this.basicCommodityChange
            },
            '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=measurementKind]': {
                change: this.basicMeasurementKindChange
            },
            '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=macroPeriod]': {
                change: this.basicMacroPeriodChange //,
                //select:this.basicMacroPeriodSelect
            }
        });
    },


    showOverview: function () {
        var me = this,
            widget,
            record = Ext.create('Mtr.model.readingtypes.AddReadingType'); //lori
        //deocamdata il folosesc pe cel de la reading types
        widget = Ext.widget('add-reading-types-group'); // lori
        widget.loadRecord(record);
        me.getApplication().fireEvent('changecontentevent', widget);
    },

    basicCommodityChange: function (combo, newValue) {
        var me = this;
        var commodity = me.getBasicCommodity().getValue();
        me.getBasicMeasurementKind().setDisabled(commodity == 0);
        me.getBasicMeasurementKind().getStore().getProxy().setExtraParam('filter', newValue);
        me.getBasicMeasurementKind().getStore().load();
        me.getBasicMeasurementKind().select(0);

        me.getBasicFlowDirection().setDisabled(commodity == 0);
        me.getBasicFlowDirection().getStore().getProxy().setExtraParam('filter', newValue);
        me.getBasicFlowDirection().getStore().load();
        me.getBasicFlowDirection().select(0);

        me.getBasicMacroPeriod().setDisabled(commodity == 0);
        me.getBasicMacroPeriod().getStore().getProxy().setExtraParam('filter', newValue);
        me.getBasicMacroPeriod().getStore().load();
        me.getBasicMacroPeriod().select(0);
        // me.getBasicMacroPeriod().getStore().load(function() {
        //         if (commodity == 1 || commodity == 2) {
        //             me.getBasicMacroPeriod().getStore().insert(1,
        //                 {code: 0x00010000, displayName: "Interval < 1 day"});
        //         }
        //         me.getBasicMacroPeriod().select(0);
        //     }
        // );

        this.prepareAccumulation();
        this.prepareMeasuringPeriod();

        me.getBasicPhases().setVisible(true);
        me.getBasicAggregate().setDisabled(commodity == 0);

        me.getBasicMetricMultiplier().setVisible(true);
        me.getBasicMetricMultiplier().setDisabled(commodity == 0);

        var showAdditionalParameters = (commodity == 1 || commodity == 2);

        me.getBasicPhases().setVisible(showAdditionalParameters);
        me.getBasicPhases().setDisabled(commodity == 0);

        me.getBasicTimeOfUse().setVisible(showAdditionalParameters);
        me.getBasicTimeOfUse().setDisabled(commodity == 0);

        me.getBasicCriticalPeakPeriod().setVisible(showAdditionalParameters);
        me.getBasicCriticalPeakPeriod().setDisabled(commodity == 0);

        me.getBasicConsumptionTier().setVisible(showAdditionalParameters);
        me.getBasicConsumptionTier().setDisabled(commodity == 0);

        // me.getBasicAggregate().getStore().getProxy().setExtraParam('filter', newValue);
        // me.getBasicAggregate().getStore().reload();
        // me.getBasicAggregate().select(0);
        // codul acesta se foloseste cand doresc valoarea campului, depoarece de aceasta
        // valoare mai depinde si alte combo box-uri

        //aici adaug si celelelate
    },

    basicMeasurementKindChange: function (combo, newValue) {
        var me = this;
        me.getBasicUnit().setDisabled(newValue == 0);
        me.getBasicUnit().select(0);
        me.getBasicUnit().getStore().getProxy().setExtraParam('filter', newValue);
        me.getBasicUnit().getStore().load();
    },

    basicMacroPeriodSelect: function (combo, records) {
        if (records.length > 0) {

        }
    },
    basicMacroPeriodChange: function (combo, newValue) {
        this.prepareAccumulation();
        this.prepareMeasuringPeriod();
    },

    prepareAccumulation: function () {
        var me = this;
        var commodity = me.getBasicCommodity().getValue();
        var macroPeriodValue = me.getBasicMacroPeriod().getValue() || 0;
        var show = (commodity == 1 || commodity == 2);
        show = show && (macroPeriodValue == 0);
        me.getBasicAccumulation().setVisible(show);
        if (show) {
            me.getBasicAccumulation().getStore().load();
            ///me.getBasicAccumulation().select(0);  // 'Select an accumulation...' text is displayed
        }
    },

    prepareMeasuringPeriod: function () {
        var me = this;
        var commodity = me.getBasicCommodity().getValue();
        var macroPeriodValue = me.getBasicMacroPeriod().getValue() || 0;
        var show = (commodity == 1 || commodity == 2);
        show = show && (macroPeriodValue == 0x10000);

        me.getBasicMeasuringPeriod().setVisible(show);
        me.getBasicMeasuringPeriod().getStore().load();
        // me.getBasicMeasuringPeriod().select(0);  // 'Select a time period...' text is displayed
    },

    addButtonClick: function () {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            form = me.getAddReadingTypeForm(),
            errorMsg = me.getAddReadingTypeFormErrorMessage(),
            record = form.updateRecord().getRecord(),
            specifyBy = record.get('specifyBy'),
            addCount = specifyBy == 'form' ? me.getAddReadingTypes().addCount : 1;
        if (form.isValid()) {
            if (addCount > 0) {
                errorMsg.hide();
                if (specifyBy == 'form') {
                    record.set('mRID', null);
                    record.getProxy().setUrl('/count');
                } else if (specifyBy == 'cim') {
                    record.getProxy().setUrl('');
                }
                record.phantom = true;
                record.save({
                    callback: function (record, operation, success) {
                        if (success) {
                            var resp = Ext.JSON.decode(operation.response.responseText),
                                count = resp.countReadingTypesToCreate;
                            if (specifyBy == 'form' && count > 0) {
                                Ext.widget('confirmation-window', {
                                    confirmBtnUi: 'action',
                                    confirmText: Uni.I18n.translate('general.add', 'MTR', 'Add')
                                }).show({
                                    closable: false,
                                    fn: function (btnId) {
                                        if (btnId == 'confirm') {
                                            me.addReadingTypesRequest(record);
                                        }
                                    },
                                    msg: Uni.I18n.translate('readingtypesmanagment.addReadingType.addMsg', 'MTR', "This could produce reading types that won't be used"),
                                    title: Uni.I18n.translatePlural('readingtypesmanagment.addReadingType.addConfirmationXXX', count, 'MTR', 'Add {0} reading types?', 'Add {0} reading type?', 'Add {0} reading types?')
                                });
                            } else if (count == 0) {
                                errorMsg.setText(Uni.I18n.translate('readingtypesmanagment.addReadingType.readingTypesExists', 'MTR', 'Reading types already exists'));
                                errorMsg.show()
                            } else if (specifyBy == 'cim') {
                                router.getRoute('administration/readingtypes').forward();
                                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('readingtypesmanagment.addReadingType.acknowledge', 'MTR', '{0} reading types added', [count]));
                            }
                        }
                    },
                    failure: function (response) {
                        var json = Ext.decode(response.responseText),
                            baseForm = form.getForm();
                        if (json && json.errors && baseForm) {
                            baseForm.markInvalid(json.errors);
                        }
                    }
                });
            } else {
                errorMsg.setText(Uni.I18n.translate('readingtypesmanagment.addReadingType.noAttrSpecified', 'MTR', 'No attributes specified'));
                errorMsg.show();
            }
        } else {
            errorMsg.setText(errorMsg.defaultText);
            errorMsg.show();
        }
    },

    addReadingTypesRequest: function (record) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            form = me.getAddReadingTypeForm().getForm(),
            errorMsg = me.getAddReadingTypeFormErrorMessage();

        record.getProxy().setUrl('');
        record.phantom = true;
        Ext.suspendLayouts();
        form.clearInvalid();
        errorMsg.hide();
        Ext.resumeLayouts(true);
        record.save({
            success: function (record, operation) {
                var response = Ext.JSON.decode(operation.response.responseText),
                    addedCount = response.countCreatedReadingTypes;
                router.getRoute('administration/readingtypes').forward();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translatePlural('readingtypesmanagment.addReadingType.readingTypesAddedAcknowledge', addedCount, 'MTR', '{0} reading types added', '{0} reading type added', '{0} reading types added'));
            },
            failure: function (record, operation) {
                var json = Ext.decode(operation.response.responseText, true);

                if (json && !Ext.isEmpty(json.errors)) {
                    Ext.suspendLayouts();
                    errorMsg.show();
                    form.markInvalid(json.errors);
                    Ext.resumeLayouts(true);
                }
            }
        });
    },

    goBack: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        router.getRoute('administration/readingtypes').forward(null,
            me.qString
        );
    }
});

