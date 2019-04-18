/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.dataquality.controller.DataQuality', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.history.Router'
    ],
    views: [
        'Imt.dataquality.view.Setup',
        'Imt.dataquality.view.Filter'
    ],
    stores:[
        'Imt.dataquality.store.DataQuality'
    ],
    models: [
        'Imt.dataquality.model.DataQuality'
    ],

    refs: [
        {ref: 'dataQualityPage', selector: 'imt-quality-setup'}
    ],

    init: function () {
        this.control({
            'imt-quality-setup #imt-quality-grid': {
                select: this.showPreview
            }
        });
    },

    showDataQuality: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        if (!me.getDataQualityPage()) {
            me.getApplication().fireEvent('changecontentevent', Ext.widget('imt-quality-setup', {
                router: router,
                filterDefault: {
                    from: moment().subtract(1, 'months').startOf('day').toDate(),
                    to: moment().add('days', 1).startOf('day').toDate()
                }
            }));
            me.getStore('Imt.dataquality.store.DataQuality').load();
        }
    },

    showPreview: function (selectionModel, record) {
        this.getDataQualityPage().down('#imt-quality-preview').loadRecord(record);
    }
});
