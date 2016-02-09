Ext.define('Sct.view.LogLevelWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.log-level-window',
    modal: true,
    title: Uni.I18n.translate('general.selectLogLevel', 'SCT', 'Select log level'),

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'form',
            itemId: 'change-log-level-form',
            padding: 0,
            defaults: {
                width: 503,
                labelWidth: 200
            },
            items: [
                {
                    xtype: 'uni-form-error-message',
                    itemId: 'form-errors',
                    hidden: true
                },
                {
                    xtype: 'label',
                    itemId: 'error-label',
                    hidden: true,
                    margin: '10 0 10 20'
                },
                {
                    xtype: 'combobox',
                    itemId: 'log-level-field',
                    name: 'logLevel',
                    fieldLabel: Uni.I18n.translate('general.logLevel', 'SCT', 'Log level'),
                    required: true,
                    editable: 'false',
                   /* store: 'Imt.store.Estimators',
                    valueField: 'implementation',
                    displayField: 'displayName',*/
                    queryMode: 'local',
                    forceSelection: true,
                    emptyText: Uni.I18n.translate('general.selectALogLevel', 'SCT', 'Select a log level...'),
                },
                {
                    xtype: 'property-form',
                    itemId: 'property-form',
                    defaults: {
                        labelWidth: 200
                    }
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: '&nbsp;',
                    margin: '20 0 0 0',
                    items: [
                        {
                            xtype: 'button',
                            itemId: 'save-log-level-button',
                            text: Uni.I18n.translate('general.save', 'SCT', 'Save'),
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            itemId: 'cancel-button',
                            text: Uni.I18n.translate('general.cancel', 'SCT', 'Cancel'),
                            ui: 'link',
                            handler: function () {
                                me.close();
                            }
                        }
                    ]
                }
            ]
        };

        me.callParent(arguments);
    }
});