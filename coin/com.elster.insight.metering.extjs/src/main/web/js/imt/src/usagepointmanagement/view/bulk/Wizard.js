Ext.define('Imt.usagepointmanagement.view.bulk.Wizard', {
    extend: 'Ext.form.Panel',
    requires: [
        'Ext.layout.container.Card',
        'Imt.usagepointmanagement.view.bulk.Step1',
        'Imt.usagepointmanagement.view.bulk.Step2',
        'Imt.usagepointmanagement.view.bulk.Step3',
        'Imt.usagepointmanagement.view.bulk.Step4',
        'Imt.usagepointmanagement.view.bulk.Step5',
        //'Mdc.view.setup.searchitems.bulk.Step5ViewDevices',
        //'Uni.view.notifications.NotificationPanel'
    ],
    alias: 'widget.usagepoints-wizard',
    autoHeight: true,
    border: false,
    layout: 'card',
    activeItemId: 0,
    buttonAlign: 'left',

    initComponent: function () {
        this.items = [
            {
                xtype: 'usagepoints-bulk-step1',
                itemId: 'usagepoints-bulk-step1',
                deviceStore: this.deviceStore,
                navigationIndex: 0
            },
            {
                xtype: 'usagepoints-bulk-step2',
                itemId: 'usagepoints-bulk-step2',
                navigationIndex: 1
            },
            {
                xtype: 'usagepoints-bulk-step3',
                itemId: 'usagepoints-bulk-step3',
                navigationIndex: 2
            },
            {
                xtype: 'usagepoints-bulk-step4',
                itemId: 'usagepoints-bulk-step4',
                navigationIndex: 3
            },
            {
                xtype: 'usagepoints-bulk-step5',
                itemId: 'usagepoints-bulk-step5',
                navigationIndex: 4
            },
        //    {
        //        xtype: 'searchitems-bulk-step5-viewdevices',
        //        itemId: 'searchitems-bulk-step5-viewdevices',
        //        navigationIndex: 5
        //    }
        ];

        this.callParent(arguments);
    },

    bbar: {
        defaults: {
            xtype: 'button'
        },
        items: [
            {
                text: Uni.I18n.translate('general.back', 'IMT', 'Back'),
                action: 'back',
                itemId: 'backButton',
                disabled: true
            },
            {
                text: Uni.I18n.translate('general.next', 'IMT', 'Next'),
                ui: 'action',
                action: 'next',
                itemId: 'nextButton'
            },
            {
                text: Uni.I18n.translate('general.confirm', 'IMT', 'Confirm'),
                ui: 'action',
                action: 'confirm',
                itemId: 'confirmButton',
                hidden: true
            },
            {
                text: Uni.I18n.translate('general.finish', 'IMT', 'Finish'),
                ui: 'action',
                action: 'finish',
                itemId: 'finishButton',
                hidden: true
            },
            {
                text: Uni.I18n.translate('general.finish', 'IMT', 'Finish'),
                ui: 'remove',
                action: 'finish',
                itemId: 'failureFinishButton',
                hidden: true
            },
            {
                text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                ui: 'link',
                action: 'cancel',
                itemId: 'wizardCancelButton',
                href: ''
            }
        ]
    }
});
