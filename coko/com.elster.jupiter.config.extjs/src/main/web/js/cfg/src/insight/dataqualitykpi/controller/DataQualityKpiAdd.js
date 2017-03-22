/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.insight.dataqualitykpi.controller.DataQualityKpiAdd', {
    extend: 'Ext.app.Controller',

    stores: [
        'Cfg.insight.dataqualitykpi.store.UsagePointGroups',
        'Cfg.store.DataValidationKpiFrequency'
    ],

    models: [
        'Cfg.insight.dataqualitykpi.model.DataQualityKpi'
    ],

    views: [
        'Cfg.insight.dataqualitykpi.view.Add'
    ],

    refs: [
        {ref: 'dataQualityKpiForm', selector: '#ins-data-quality-kpi-add #ins-data-quality-kpi-add-form'}
    ],

    init: function () {
        var me = this;

        me.control({
            '#ins-data-quality-kpi-add #cmb-usage-point-group': {
                change: me.onUsagePointGroupChange
            },
            '#ins-data-quality-kpi-add #add-button': {
                click: me.addDataQualityKpi
            }
        });
    },

    showAddDataQualityKpi: function () {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            usagePointGroupsStore = me.getStore('Cfg.insight.dataqualitykpi.store.UsagePointGroups');

        mainView.setLoading();
        usagePointGroupsStore.load(showPage);

        function showPage(usagePointGroups) {
            var widget = Ext.widget('ins-data-quality-kpi-add', {
                itemId: 'ins-data-quality-kpi-add',
                usagePointGroupsIsDefined: !Ext.isEmpty(usagePointGroups),
                router: router,
                returnLink: router.getRoute('administration/dataqualitykpis').buildUrl()
            });

            app.fireEvent('changecontentevent', widget);
            widget.down('#ins-data-quality-kpi-add-form').loadRecord(new Cfg.insight.dataqualitykpi.model.DataQualityKpi);
            mainView.setLoading(false);
        }
    },

    onUsagePointGroupChange: function (usagePointGroupCombo, newValue) {
        var me = this,
            purposesField,
            usagePointGroup = usagePointGroupCombo.findRecordByValue(newValue);

        if (usagePointGroup) {
            purposesField = me.getDataQualityKpiForm().down('#fld-purposes');
            Ext.suspendLayouts();
            purposesField.down('#selection-grid').getSelectionModel().deselectAll();
            purposesField.down('#selection-grid').reconfigure(usagePointGroup.purposes());
            purposesField.show();
            Ext.resumeLayouts(true);
        }
    },

    addDataQualityKpi: function () {
        var me = this,
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            form = me.getDataQualityKpiForm(),
            frequency = form.down('#cmb-frequency').getValue(),
            usagePointGroupId = form.down('#cmb-usage-point-group').getValue(),
            baseForm = form.getForm(),
            errorMsg = form.down('#form-errors'),
            record = form.getRecord(),
            backUrl = form.returnLink;

        Ext.suspendLayouts();
        baseForm.clearInvalid();
        errorMsg.hide();
        Ext.resumeLayouts(true);
        mainView.setLoading();
        form.updateRecord();

        record.beginEdit();
        if (usagePointGroupId) {
            record.set('usagePointGroup', {
                id: usagePointGroupId
            });
        }
        if (frequency) {
            record.set('frequency', me.getStore('Cfg.store.DataValidationKpiFrequency').getById(frequency).get('value'));
        }
        record.endEdit();

        record.save({
            backUrl: backUrl,
            success: onSuccessSave,
            failure: onFailureSave,
            callback: saveCallback
        });

        function onSuccessSave() {
            window.location.href = backUrl;
            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.dataqualitykpis.added', 'CFG', 'Data quality KPIs added'));
        }

        function onFailureSave(record, operation) {
            var response = Ext.decode(operation.response.responseText, true);

            if (response && !Ext.isEmpty(response.errors)) {
                Ext.suspendLayouts();
                baseForm.markInvalid(response.errors);
                errorMsg.show();
                Ext.resumeLayouts(true);
            }
        }

        function saveCallback() {
            mainView.setLoading(false);
        }
    }
});