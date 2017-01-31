/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Ddv.controller.Validations', {
    extend: 'Ext.app.Controller',
    requires: [
    ],

    views: [
        'Ddv.view.validations.Setup',
        'Ddv.view.validations.Filter'
    ],
    stores:[
        'Ddv.store.Validations',
        'Ddv.store.DeviceGroups',
        'Ddv.store.Validators'
    ],

    models: [
        'Ddv.model.Validation'
    ],

    refs: [
        {ref: 'validations', selector: 'ddv-validations'},
        {ref: 'validationsPreviewContainer', selector: '#preview-validations'},
        {ref: 'validationsPreviewForm', selector: '#validations-details-form'}
    ],

    init: function () {
        this.control({
            'ddv-validations-setup #validations-grid': {
                select: this.showPreview
            }
        });
    },

    showValidations: function () {
        var me = this, view,
            router = me.getController('Uni.controller.history.Router');

        me.getApplication().fireEvent('changecontentevent', Ext.widget('ddv-validations-setup', {
            router: router,
            filterDefault: {
                from: moment().subtract(1, 'months').startOf('day').toDate(),
                to: moment().add(1, 'days').startOf('day').toDate()
            }
        }));
        me.getStore('Ddv.store.Validations').load();
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            previewContainer = me.getValidationsPreviewContainer(),
            previewForm = me.getValidationsPreviewForm(),
            typeOfSuspects = '';

        Ext.suspendLayouts();
        previewContainer.setTitle(Uni.I18n.translate('validations.preview.title', 'DDV', '{0} validations', Ext.String.htmlEncode(record.get('name'))));
        previewForm.loadRecord(record);

        record.typeOfSuspects().each(function (rec) {
            if (typeOfSuspects.length > 0) {
                typeOfSuspects += '<br>';
            }
            typeOfSuspects += rec.get('name');
        });
        Ext.resumeLayouts(true);
    }
});
