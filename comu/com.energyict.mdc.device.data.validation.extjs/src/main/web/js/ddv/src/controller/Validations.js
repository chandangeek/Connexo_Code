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
            router = me.getController('Uni.controller.history.Router'),
            queryString = Uni.util.QueryString.getQueryStringValues(false);

        if (queryString.between == undefined) {
            var startDate = Ext.Date.add(new Date(), Ext.Date.DAY, 1);
            var endDate = Ext.Date.add(new Date(), Ext.Date.MONTH, -1);
            queryString.between = (new Date(endDate)).setHours(0, 0, 0, 0) + '-' + (new Date(startDate)).setHours(0, 0, 0, 0);
            window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
            return;
        }

        view = Ext.widget('ddv-validations-setup', {router: router});
        me.getApplication().fireEvent('changecontentevent', view);
        me.updateApplyButtonState(view, queryString);
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            previewContainer = me.getValidationsPreviewContainer(),
            previewForm = me.getValidationsPreviewForm(),
            typeOfSuspects = '';

        Ext.suspendLayouts();
        previewContainer.setTitle(Uni.I18n.translate('validations.preview.title', 'DDV', '{0} validations', Ext.String.htmlEncode(record.get('mrid'))));
        previewForm.loadRecord(record);

        record.typeOfSuspects().each(function (rec) {
            if (typeOfSuspects.length > 0) {
                typeOfSuspects += '<br>';
            }
            typeOfSuspects += rec.get('name');
        });
        //previewContainer.down('#type-of-suspects-validations-preview').setValue((typeOfSuspects.length > 0) ? typeOfSuspects : '-');
        Ext.resumeLayouts(true);
    },

    updateApplyButtonState: function (view, queryString) {
        var startDate = Ext.Date.add(new Date(), Ext.Date.DAY, 1),
            endDate = Ext.Date.add(new Date(), Ext.Date.MONTH, -1),
            between = (new Date(endDate)).setHours(0, 0, 0, 0) + '-' + (new Date(startDate)).setHours(0, 0, 0, 0),
            topfilterBetween = view.down('#validations-topfilter-between'),
            clearButton = (topfilterBetween == undefined) ? undefined : topfilterBetween.down('button[action=clear]')

        if (clearButton) {
            clearButton.setDisabled(queryString.between == between);
        }

        if (((Object.keys(queryString).length) == 1) &&
            (queryString.between == between)) {
            view.down('button[action=clearAll]').setDisabled(true);
        }
        else {
            view.down('button[action=clearAll]').setDisabled(false);
        }
    }
});
