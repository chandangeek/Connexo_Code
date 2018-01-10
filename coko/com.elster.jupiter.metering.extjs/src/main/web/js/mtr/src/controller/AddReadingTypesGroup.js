/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.controller.AddReadingTypesGroup', {
    extend: 'Ext.app.Controller',

    views: [
        'Mtr.view.AddReadingTypesGroup'
    ],

    requires: [
        'Uni.view.window.Confirmation',
        'Mtr.model.AddReadingTypeGroup'
    ],

    stores: [
        'Mtr.store.attributes.extended.Interval',
        'Mtr.store.attributes.extended.TimeOfUse',
        'Mtr.store.attributes.extended.DataQualifier',
        'Mtr.store.attributes.extended.Accumulation',
        'Mtr.store.attributes.extended.DirectionOfFlow',
        'Mtr.store.attributes.extended.Commodity',
        'Mtr.store.attributes.extended.Kind',
        'Mtr.store.attributes.extended.InterharmonicNumerator',
        'Mtr.store.attributes.extended.InterharmonicDenominator',
        'Mtr.store.attributes.extended.ArgumentNumerator',
        'Mtr.store.attributes.extended.ArgumentDenominator',
        'Mtr.store.attributes.extended.CriticalPeakPeriod',
        'Mtr.store.attributes.extended.ConsumptionTier',
        'Mtr.store.attributes.extended.Phase',
        'Mtr.store.attributes.extended.Multiplier',
        'Mtr.store.attributes.extended.UnitOfMeasures',
        'Mtr.store.attributes.extended.Currency',
        'Mtr.store.attributes.extended.MeasuringPeriod',


        'Mtr.store.attributes.basic.Commodity',
        'Mtr.store.attributes.basic.Kind',
        'Mtr.store.attributes.basic.DirectionOfFlow',
        'Mtr.store.attributes.basic.UnitOfMeasures',
        'Mtr.store.attributes.basic.MacroPeriod',
        'Mtr.store.attributes.basic.Accumulation',
        'Mtr.store.attributes.basic.MeasuringPeriod',
        'Mtr.store.attributes.basic.Aggregate',

        'Mtr.store.attributes.basic.Multiplier',
        'Mtr.store.attributes.basic.Phase',
        'Mtr.store.attributes.basic.TimeOfUse',
        'Mtr.store.attributes.basic.CriticalPeakPeriod',
        'Mtr.store.attributes.basic.ConsumptionTier'
    ],

    refs: [
        {
            ref: 'addReadingTypeForm',
            selector: '#add-reading-types-group add-reading-types-group-form'
        },
        {
            ref: 'addReadingTypeFormErrorMessage',
            selector: '#add-reading-types-group add-reading-types-group-form #form-errors'
        },
        {
            ref: 'cimCode',
            selector: '#add-reading-types-group textfield[name=mRID]'
        },
        {
            ref: 'basicCommodity',
            selector: '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=basicCommodity]'
        },
        {
            ref: 'basicMeasurementKind',
            selector: '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=basicMeasurementKind]'
        },
        {
            ref: 'basicUnit',
            selector: '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=basicUnit]'
        },
        {
            ref: 'basicFlowDirection',
            selector: '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=basicFlowDirection]'
        },
        {
            ref: 'basicMacroPeriod',
            selector: '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=basicMacroPeriod]'
        },
        {
            ref: 'basicAccumulation',
            selector: '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=basicAccumulation]'
        },
        {
            ref: 'basicMeasuringPeriod',
            selector: '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=basicMeasuringPeriod]'
        },
        {
            ref: 'basicAggregate',
            selector: '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=basicAggregate]'
        },
        {
            ref: 'basicMetricMultiplier',
            selector: '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=basicMetricMultiplier]'
        },
        {
            ref: 'basicPhases',
            selector: '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=basicPhases]'
        },
        {
            ref: 'basicTimeOfUse',
            selector: '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=basicTou]'
        },
        {
            ref: 'basicCriticalPeakPeriod',
            selector: '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=basicCpp]'
        },
        {
            ref: 'basicConsumptionTier',
            selector: '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=basicConsumptionTier]'
        },
        {
            ref: 'noAdditionalParameters',
            selector: '#add-reading-types-group #no-additional-parameters'
        },
        {
            ref: 'tabPanel',
            selector: '#reading-types-add-group-tab-panel'
        }
    ],

    qString: null,

    // Util object that handles the MRID in the URL processing
    cimHandler: null,

    // List of objects that handle the comboboxes
    comboProcessors: [],

    // Flag that notifies the first change of commodity
    firstRun: true,

    // Combo processors factory
    factory: null,


    init: function () {
        this.control({
            '#add-reading-types-group #add-reading-types-group-general-add-button': {
                click: this.addGeneralButtonClick
            },
            '#add-reading-types-group #add-reading-types-group-general-cancel-button': {
                click: this.goBack
            },
            '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=basicCommodity]': {
                change: this.basicCommodityChange
            },
            '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=basicMeasurementKind]': {
                change: this.basicMeasurementKindChange
            },
            '#add-reading-types-group #reading-types-groups-add-basic-tab cimcombobox[name=basicMacroPeriod]': {
                change: this.basicMacroPeriodChange
            }
        });
    },

    showOverview: function () {
        var me = this,
            widget,
            record = Ext.create('Mtr.model.AddReadingTypeGroup');
        widget = Ext.widget('add-reading-types-group');
        widget.loadRecord(record);
        me.getApplication().fireEvent('changecontentevent', widget);
        me.preloadComboBoxes();

    },

    /**
     * Preload combos if we have an MRID value in the URL from
     * the Add Register Type page
     */
    preloadComboBoxes: function (){
        var me = this;

        if (!me.cimHandler) {
            me.cimHandler = Ext.create('Mtr.controller.readingtypesgroup.processors.CIMHandler', {});
        }
        if (!me.factory) {
            me.factory = Ext.create('Mtr.controller.readingtypesgroup.processors.ComboProcessorFactory', {
                controller: me
            });
        }
        // All combos are linked to the commodity one. If commodity has a preload value,
        // it will tell all the other combos to process
        me.factory.getProcessor(me.getBasicCommodity()).process();

        // Need to set the controller value for all processors every time we get on the page
        if (!me.firstRun) {
            me.comboProcessors.forEach(function (item) {
                item.setController(me);
            });
        }
    },

    /**
     * Register all processors that listen to the commodity value.
     */
    registerProcessors: function(){
        var me = this;
        if (me.firstRun) {
            me.firstRun = false;
            me.comboProcessors.push(me.factory.getProcessor(me.getBasicMeasurementKind()));
            me.comboProcessors.push(me.factory.getProcessor(me.getBasicFlowDirection()));
            me.comboProcessors.push(me.factory.getProcessor(me.getBasicMacroPeriod()));
            me.comboProcessors.push(me.factory.getProcessor(me.getBasicAggregate()));
            me.comboProcessors.push(me.factory.getProcessor(me.getBasicAccumulation()));
            me.comboProcessors.push(me.factory.getProcessor(me.getBasicMetricMultiplier()));
            me.comboProcessors.push(me.factory.getProcessor(me.getBasicPhases()));
            me.comboProcessors.push(me.factory.getProcessor(me.getBasicTimeOfUse()));
            me.comboProcessors.push(me.factory.getProcessor(me.getBasicCriticalPeakPeriod()));
            me.comboProcessors.push(me.factory.getProcessor(me.getBasicConsumptionTier()));
        }
    },

    basicCommodityChange: function (combo, commodity) {
        var me = this;
        me.getNoAdditionalParameters().setVisible(commodity === 0);
        me.registerProcessors();
        me.comboProcessors.forEach(function (item){
           item.process();
        });
    },

    basicMeasurementKindChange: function () {
        this.factory.getProcessor(this.getBasicUnit()).process();
    },

    basicMacroPeriodChange: function () {
        this.factory.getProcessor(this.getBasicAccumulation()).process();
        this.factory.getProcessor(this.getBasicMeasuringPeriod()).process();
    },


    addGeneralButtonClick: function () {
        var me = this,
            tabPanel = me.getTabPanel(),
            activeTab = tabPanel.getActiveTab(),
            router = this.getController('Uni.controller.history.Router'),
            form = me.getAddReadingTypeForm(),
            errorMsg = me.getAddReadingTypeFormErrorMessage(),
            isBasic = activeTab.itemId === 'reading-types-groups-add-basic-tab',
            record = form.getRecord(isBasic),
            specifyBy = record.get('specifyBy'),
            addCount = (specifyBy == 'form') ? me.getAddReadingTypeForm().getCount(isBasic) : 1;

        if (form.isValid()) {
            if (addCount > 0) {
                errorMsg.hide();
                var urlCount = isBasic ? '/basiccount' : '/extendedcount';
                var urlSave = isBasic ? '/basic' : '/extended';
                if (specifyBy == 'form') {
                    record.set('mRID', null);
                    record.getProxy().setUrl(urlCount);
                } else if (specifyBy == 'cim') {
                    record.getProxy().setUrl(urlSave);
                }

                record.phantom = true;
                record.save({
                    callback: function (record, operation, success) {
                        if (success) {
                            var resp = Ext.JSON.decode(operation.response.responseText),
                                count = resp.total;
                            if (specifyBy == 'form' && count > 0) {
                                Ext.widget('confirmation-window', {
                                    confirmBtnUi: 'action',
                                    confirmText: Uni.I18n.translate('general.add', 'MTR', 'Add')
                                }).show({
                                    closable: false,
                                    fn: function (btnId) {
                                        if (btnId == 'confirm') {
                                            me.addReadingTypesRequest(record, isBasic);
                                        }
                                    },
                                    msg: Uni.I18n.translate('readingtypesmanagment.addReadingType.addMsg', 'MTR', "This could produce reading types that won't be used"),
                                    title: Uni.I18n.translatePlural('readingtypesmanagment.addReadingType.addConfirmationXXX', count, 'MTR', 'Add {0} reading types?', 'Add {0} reading type?', 'Add {0} reading types?')
                                });
                            } else if (count == 0) {
                                errorMsg.setText(Uni.I18n.translate('readingtypesmanagment.addReadingType.readingTypesExists', 'MTR', 'Reading types already exists'));
                                errorMsg.show()
                            } else if (specifyBy == 'cim') {
                                if (me.goBackWithOptions(resp) === false) {
                                    router.getRoute('administration/readingtypes').forward();
                                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('readingtypesmanagment.addReadingType.acknowledge', 'MTR', '{0} reading types added', [count]));
                                }
                            }
                        }
                    },

                    failure: function (response, operation) {
                        var baseForm = form.getForm();
                        if (operation.response.status == 400) {
                            if (!Ext.isEmpty(operation.response.responseText)) {
                                var json = Ext.JSON.decode(operation.response.responseText);
                                if (json && json.errors) {
                                    Ext.each(json.errors, function (error) {
                                        me.getCimCode().markInvalid(error.msg);
                                    });
                                    Ext.suspendLayouts();
                                    errorMsg.show();
                                    baseForm.markInvalid(json.errors);
                                    Ext.resumeLayouts(true);
                                }
                            }
                        }

                    }
                });
            } // if addCount > 0
            else {
                errorMsg.setText(Uni.I18n.translate('readingtypesmanagment.addReadingType.noAttrSpecified', 'MTR', 'No attributes specified'));
                errorMsg.show();
            }  // else if addCount > 0
        } // if form.isValid
        else {
            errorMsg.setText(errorMsg.defaultText);
            errorMsg.show();
        }
    },

    addReadingTypesRequest: function (record, isBasic) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            form = me.getAddReadingTypeForm().getForm(),
            errorMsg = me.getAddReadingTypeFormErrorMessage();

        var urlSave = isBasic ? '/basic' : '/extended';

        record.getProxy().setUrl(urlSave);

        record.phantom = true;
        Ext.suspendLayouts();
        form.clearInvalid();
        errorMsg.hide();
        Ext.resumeLayouts(true);
        record.save({
            success: function (record, operation) {
                var response = Ext.JSON.decode(operation.response.responseText),
                    addedCount = response.total;
                if (me.goBackWithOptions(response) === false) {
                    router.getRoute('administration/readingtypes').forward();
                    me.getApplication().fireEvent('acknowledge',
                        Uni.I18n.translatePlural('readingtypesmanagment.addReadingType.readingTypesAddedAcknowledge',
                            addedCount, 'MTR', '{0} reading types added', '{0} reading type added', '{0} reading types added'));

                }
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

        if (me.goBackWithOptions(null) === false) {
            router.getRoute('administration/readingtypes').forward(null,
                me.qString
            );
        }
    },


    /**
     * This method takes us back to the Add Register Type page
     * @param response Response after adding reading types
     * @returns {boolean}
     */
    goBackWithOptions: function (response) {
        var me = this,
            url,
            queryValues = Uni.util.QueryString.getQueryStringValues(false);

        if (queryValues.back && queryValues.back === "addRegister") {
            url = me.getBackUrl();
            if (queryValues.obis) {
                url = Ext.String.urlAppend(url, "obis=" + queryValues.obis);
            }
            if (!Ext.isEmpty(response)) {
                // If we're adding multiple reading types, we return the first one only
                url = Ext.String.urlAppend(url, "mRID=" + response.MRIDs[0]);
            }
            location.href = url;
            return true;

        }
        return false;
    },

    getBackUrl: function () {
        var host = location.protocol + "//" + location.host,
            pathname = "/apps/multisense/index.html",
            hash = "#/administration/registertypes/add";

        return host + pathname + hash;
    }
});

