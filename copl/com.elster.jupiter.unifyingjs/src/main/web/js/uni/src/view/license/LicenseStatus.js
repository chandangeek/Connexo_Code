Ext.define('Uni.view.license.LicenseStatus', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.LicenseStatus',

    initComponent: function () {
        this.content = [
            {
                xtype: 'panel',
                ui: 'large',
                layout: 'vbox',
                title: 'License',

                items: [
                    {
                        xtype: 'label',
                        name: 'authenticationName',
                        text: Uni.I18n.translate('license.licenseStatus', 'UNI', 'LICENSE STATUS')
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});