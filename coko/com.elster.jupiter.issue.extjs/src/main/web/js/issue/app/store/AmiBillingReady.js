Ext.define('Mtr.store.AmiBillingReady', {
    extend: 'Ext.data.Store',
    model: 'Mtr.model.AmiBillingReadyKind',
    pageSize: 50,
    data: [
        {
            value: 'ENABLED',
            display: 'Enabled'
        },
        {
            value: 'OPERABLE',
            display: 'Operable'
        },
        {
            value: 'BILLINGAPPROVED',
            display: 'Billing approved'
        },
        {
            value: 'NONAMI',
            display: 'Non-AMI'
        },
        {
            value: 'AMIDISABLED',
            display: 'AMI disabled'
        },
        {
            value: 'AMICAPABLE',
            display: 'AMI capable'
        },
        {
            value: 'NONMETERED',
            display: 'Non-metered'
        }
    ]
});