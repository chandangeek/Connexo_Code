Ext.define('Imt.rulesets.controller.mixins.AddPurposesCommon', {
    showPreview: function (selectionModel, record) {
        var me = this,
            preview = me.getPreviewPanel();

        Ext.suspendLayouts();
        preview.setTitle(record.get('metrologyConfigurationInfo').name);
        preview.loadRecord(record);
        Ext.resumeLayouts(true);
    },

    addPurposesToRuleSet: function (grid, records) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0];

        mainView.setLoading();
        Ext.Ajax.request({
            method: 'PUT',
            url: Ext.String.format(me.addPurposesLink, router.arguments.ruleSetId),
            jsonData: formatData(),
            success: onSuccessAdd,
            callback: addCallback
        });

        function formatData() {
            return _.map(records, function (record) {
                return record.getId();
            });
        }

        function onSuccessAdd() {
            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('ruleSet.metrologyConfigurationPurposes.add.success.msg', 'IMT', '{0} purposes of metrology configuirations added',
                records.length));
            if (grid.rendered) {
                window.location.href = grid.cancelHref;
            }
        }

        function addCallback() {
            mainView.setLoading(false);
        }
    }
});