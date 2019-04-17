/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Ddv.controller.DataQuality', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.history.Router'
    ],

    views: [
        'Ddv.view.Setup',
        'Ddv.view.Filter'
    ],
    stores:[
        'Ddv.store.DataQuality'
    ],

    models: [
        'Ddv.model.DataQuality'
    ],

    refs: [
        {ref: 'dataQualityPage', selector: 'ddv-quality-setup'}
    ],

    init: function () {
        this.control({
            'ddv-quality-setup #ddv-quality-grid': {
                select: this.showPreview
            }
        });
    },

    showDataQuality: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        if (!me.getDataQualityPage()) {
            me.getApplication().fireEvent('changecontentevent', Ext.widget('ddv-quality-setup', {
                router: router,
                filterDefault: {
                    from: moment().subtract(1, 'months').startOf('day').toDate(),
                    to: moment().add('days', 1).startOf('day').toDate()
                }
            }));
            me.getStore('Ddv.store.DataQuality').load();
        }
    },

    showPreview: function (selectionModel, record) {
        this.getDataQualityPage().down('#ddv-quality-preview').loadRecord(record);
    }
});
