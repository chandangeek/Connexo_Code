Ext.define('Cfg.view.validationtask.DataSourcesPreviewContainer', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.cfg-data-sources-preview-container',
    fieldLabel: Uni.I18n.translate('validationTasks.general.dataSources', 'CFG', 'Data sources'),
    labelAlign: 'top',
    layout: 'vbox',
    defaults: {
        xtype: 'displayfield',
        labelWidth: 250
    },
    initComponent: function () {
        var me = this,
            fieldRenderer = function (value) {
                return value && value.displayValue ? Ext.htmlEncode(value.displayValue) : '-';
            };

        switch (Uni.util.Application.getAppName()) {

            case 'MultiSense':
            {
                me.items = [
                    {
                        fieldLabel: Uni.I18n.translate('validationTasks.general.deviceGroup', 'CFG', 'Device group'),
                        name: 'deviceGroup',
                        renderer: fieldRenderer
                    }
                ];
            }
                break;
            case 'MdmApp':
            {
                me.items = [
                    {
                        fieldLabel: Uni.I18n.translate('validationTasks.general.usagePointGroup', 'CFG', 'Usage point group'),
                        name: 'usagePointGroup',
                        renderer: fieldRenderer
                    },
                    {
                        fieldLabel: Uni.I18n.translate('validationTasks.general.purpose', 'CFG', 'Purpose'),
                        name: 'metrologyPurpose',
                        renderer: fieldRenderer
                    }
                ];
            }
                break;
        }
        me.callParent(arguments);
    }
});

