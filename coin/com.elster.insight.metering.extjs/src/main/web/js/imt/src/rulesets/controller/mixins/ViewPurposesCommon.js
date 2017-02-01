Ext.define('Imt.rulesets.controller.mixins.ViewPurposesCommon', {
    showPreview: function (selectionModel, record) {
        var me = this,
            preview = me.getPreviewPanel(),
            menu = preview.down('#metrology-configuration-purpose-action-menu');

        Ext.suspendLayouts();
        preview.setTitle(record.get('metrologyConfigurationInfo').name);
        preview.loadRecord(record);
        Ext.resumeLayouts(true);

        if (menu) {
            menu.record = record;
        }
    },

    chooseAction: function (menu, menuItem) {
        var me = this;

        switch (menuItem.action) {
            case 'remove':
                me.removePurpose(menu.record);
                break;
        }
    },

    removePurpose: function (record) {
        var me = this,
            mainView = Ext.ComponentQuery.query('#contentPanel')[0];

        Ext.create('Uni.view.window.Confirmation').show({
            title: Uni.I18n.translate('general.removex', 'IMT', "Remove '{0}'?",
                record.get('name')),
            msg: me.confirmRemoveMsg,
            fn: remove
        });

        function remove(state) {
            if (state === 'confirm') {
                mainView.setLoading();
                record.destroy({
                    isNotEdit: true,
                    success: onSuccessRemove,
                    callback: removeCallback
                });
            }
        }

        function onSuccessRemove() {
            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('ruleSet.metrologyConfigurationPurposes.remove.success.msg', 'IMT', 'Metrology configuration purpose removed'));
            me.getController('Uni.controller.history.Router').getRoute().forward();
        }

        function removeCallback() {
            mainView.setLoading(false);
        }
    }
});