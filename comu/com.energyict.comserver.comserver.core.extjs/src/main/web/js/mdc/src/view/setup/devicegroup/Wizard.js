Ext.define('Mdc.view.setup.devicegroup.Wizard', {
    extend: 'Ext.form.Panel',
    alias: 'widget.adddevicegroup-wizard',

    requires: [
        'Mdc.view.setup.devicegroup.Step1',
        'Mdc.view.setup.devicegroup.Step2',
        'Mdc.view.setup.devicegroup.Step3',
        'Mdc.view.setup.devicegroup.Step4'
    ],

    layout: 'card',

    router: null,
    returnLink: null,
    isEdit: false,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'device-group-wizard-step1',
                itemId: 'devicegroup-wizard-step1',
                title: me.isEdit
                    ? Uni.I18n.translate('devicegroup.wizard.step1title.edit', 'MDC', 'Step 1 of 2: Set group name')
                    : Uni.I18n.translate('devicegroup.wizard.step1title.add', 'MDC', 'Step 1 of 2: General attributes'),
                navigationIndex: 1,
                isEdit: me.isEdit
            },
            {
                xtype: 'device-group-wizard-step2',
                itemId: 'devicegroup-wizard-step2',
                title: Uni.I18n.translate('devicegroup.wizard.step2title', 'MDC', 'Step 2 of 3: Select devices'),
                navigationIndex: 2
            },
            {
                xtype: 'device-group-wizard-step3',
                itemId: 'devicegroup-wizard-step3',
                title: Uni.I18n.translate('devicegroup.wizard.step3title', 'MDC', 'Step 3 of 4: Confirmation'),
                navigationIndex: 3
            },
            {
                xtype: 'device-group-wizard-step4',
                itemId: 'devicegroup-wizard-step4',
                title: Uni.I18n.translate('devicegroup.wizard.step4title', 'MDC', 'Step 4 of 4: Status'),
                navigationIndex: 4
            }
        ];

        me.bbar = {
            itemId: 'device-group-wizard-buttons',
            items: [
                {
                    itemId: 'backButton',
                    text: Uni.I18n.translate('general.back', 'MDC', 'Back'),
                    action: 'step-back',
                    navigationBtn: true,
                    disabled: true
                },
                {
                    itemId: 'nextButton',
                    text: Uni.I18n.translate('general.next', 'MDC', 'Next'),
                    ui: 'action',
                    action: 'step-next',
                    navigationBtn: true
                },
                {

                    itemId: 'confirmButton',
                    text: Uni.I18n.translate('general.confirm', 'MDC', 'Confirm'),
                    ui: 'action',
                    action: 'confirm-action',
                    navigationBtn: true,
                    hidden: true
                },
                {
                    itemId: 'finishButton',
                    text: Uni.I18n.translate('general.finish', 'MDC', 'Finish'),
                    ui: 'action',
                    action: 'finish',
                    hidden: true,
                    href: me.returnLink
                },
                {
                    itemId: 'wizardCancelButton',
                    text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                    ui: 'link',
                    action: 'cancel',
                    href: me.returnLink
                }
            ]
        };

        me.callParent(arguments);
    }
});
