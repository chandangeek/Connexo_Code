Ext.define('Scs.store.ServiceCalls', {
    extend: 'Ext.data.Store',
    //model: 'Sct.model.ServiceCallType',
    autoLoad: false,
    /*proxy: {
        type: 'rest',
        url: '/api/scs/servicecalltypes',
        timeout: 120000,
        reader: {
            type: 'json',
            root: 'scs'
        }
    }*/

    fields: [
        {name: 'internalId'},
        {name: 'hasChildren', type: 'boolean'},
        {name: 'externalReference'},
        {name: 'type'},
        {name: 'status'},
        {name: 'modificationDate'},
        {name: 'receivedDate'},
        {name: 'version', type: 'int'},
        {name: 'id', type: 'int'}
    ],
    data: [
        {internalId: '111', hasChildren: true, type: 'SAP Mdus', externalReference: '15', status: 'Ongoing', modificationDate: '13/01/2016', receivedDate: '12/01/2016', version: '204', id: '2'},
        {internalId: '114', hasChildren: true, type: 'SAP Mdus', externalReference: '17', status: 'Failed', modificationDate: '13/01/2016', receivedDate: '12/01/2016', version: '204', id: '3'},
        {internalId: '116', hasChildren: false, type: 'SAP Mdus', externalReference: '18', status: 'Success', modificationDate: '13/01/2016', receivedDate: '12/01/2016', version: '204', id: '4'},
        {internalId: '118', hasChildren: false, type: 'SAP Mdus', externalReference: '19', status: 'Success', modificationDate: '13/01/2016', receivedDate: '12/01/2016', version: '204', id: '5'},
        {internalId: '121', hasChildren: true, type: 'SAP Mdus', externalReference: '20', status: 'Success', modificationDate: '13/01/2016', receivedDate: '12/01/2016', version: '204', id: '6'},
        {internalId: '224', hasChildren: false, type: 'SAP Mdus', externalReference: '21', status: 'Paused', modificationDate: '13/01/2016', receivedDate: '12/01/2016', version: '204', id: '7'},
        {internalId: '226', hasChildren: false, type: 'SAP Mdus', externalReference: '22', status: 'Paused', modificationDate: '13/01/2016', receivedDate: '12/01/2016', version: '204', id: '8'},
        {internalId: '227', hasChildren: false, type: 'SAP Mdus', externalReference: '23', status: 'Ongoing', modificationDate: '13/01/2016', receivedDate: '12/01/2016', version: '204', id: '9'},
        {internalId: '335', hasChildren: true, type: 'SAP Mdus', externalReference: '24', status: 'Ongoing', modificationDate: '13/01/2016', receivedDate: '12/01/2016', version: '204', id: '10'},
        {internalId: '315', hasChildren: false, type: 'SAP Mdus', externalReference: '25', status: 'Waiting', modificationDate: '13/01/2016', receivedDate: '12/01/2016', version: '204', id: '11'},
        {internalId: '147', hasChildren: false, type: 'SAP Mdus', externalReference: '26', status: 'Partial success', modificationDate: '13/01/2016', receivedDate: '12/01/2016', version: '204', id: '12'},
        {internalId: '148', hasChildren: true, type: 'SAP Mdus', externalReference: '27', status: 'Ongoing', modificationDate: '13/01/2016', receivedDate: '12/01/2016', version: '204', id: '13'},
        {internalId: '987', hasChildren: false, type: 'SAP Mdus', externalReference: '28', status: 'Paused', modificationDate: '13/01/2016', receivedDate: '12/01/2016', version: '204', id: '14'},
        {internalId: '874', hasChildren: false, type: 'SAP Mdus', externalReference: '29', status: 'Failed', modificationDate: '13/01/2016', receivedDate: '12/01/2016', version: '204', id: '15'},
        {internalId: '584', hasChildren: true, type: 'SAP Mdus', externalReference: '30', status: 'Failed', modificationDate: '13/01/2016', receivedDate: '12/01/2016', version: '204', id: '16'},
        {internalId: '324', hasChildren: true, type: 'SAP Mdus', externalReference: '31', status: 'Failed', modificationDate: '13/01/2016', receivedDate: '12/01/2016', version: '204', id: '17'}

    ]
});
