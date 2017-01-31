/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.controller.AddReadingTypes', {
    extend: 'Ext.app.Controller',

    views: [
        'Mtr.readingtypes.view.AddReadingTypes'
    ],

    requires: [
        'Uni.view.window.Confirmation'
    ],

    stores: [
        'Mtr.readingtypes.attributes.store.Interval',
        'Mtr.readingtypes.attributes.store.TimeOfUse',
        'Mtr.readingtypes.attributes.store.DataQualifier',
        'Mtr.readingtypes.attributes.store.Accumulation',
        'Mtr.readingtypes.attributes.store.DirectionOfFlow',
        'Mtr.readingtypes.attributes.store.Commodity',
        'Mtr.readingtypes.attributes.store.Kind',
        'Mtr.readingtypes.attributes.store.InterharmonicNumerator',
        'Mtr.readingtypes.attributes.store.InterharmonicDenominator',
        'Mtr.readingtypes.attributes.store.ArgumentNumerator',
        'Mtr.readingtypes.attributes.store.ArgumentDenominator',
        'Mtr.readingtypes.attributes.store.CriticalPeakPeriod',
        'Mtr.readingtypes.attributes.store.ConsumptionTier',
        'Mtr.readingtypes.attributes.store.Phase',
        'Mtr.readingtypes.attributes.store.Multiplier',
        'Mtr.readingtypes.attributes.store.UnitOfMeasures',
        'Mtr.readingtypes.attributes.store.Currency',
        'Mtr.readingtypes.attributes.store.MeasuringPeriod'
    ],


    refs: [
        {
            ref: 'addReadingTypes',
            selector: '#add-reading-types'
        },
        {
            ref: 'addReadingTypeForm',
            selector: '#add-reading-types add-reading-types-form'
        },
        {
            ref: 'addReadingTypeFormErrorMessage',
            selector: '#add-reading-types add-reading-types-form #form-errors'
        }
    ],

    qString : null,

    init: function () {
        this.control({
            '#add-reading-types #add-reading-types-add-button': {
                click: this.addButtonClick
            },
            '#add-reading-types #add-reading-type-cancel-button': {
                click: this.goBack
            }
        });
    },

    showOverview: function () {
        var me = this,
            widget,
            record = Ext.create('Mtr.readingtypes.model.AddReadingType');
        widget = Ext.widget('add-reading-types');
        widget.loadRecord(record);
        me.getApplication().fireEvent('changecontentevent', widget);
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

    addReadingTypesRequest: function(record){
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

