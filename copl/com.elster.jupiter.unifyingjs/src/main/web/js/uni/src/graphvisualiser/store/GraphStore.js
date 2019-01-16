Ext.define('Uni.graphvisualiser.store.GraphStore', {
    extend: 'Ext.data.Store',
    requires: [
        'Uni.graphvisualiser.model.GraphModel'
    ],
    model: 'Uni.graphvisualiser.model.GraphModel',
    storeId: 'graph',

    proxy: {
        type: 'memory',
        reader: {
           // type: 'json',
            root: 'graph'
        }
    },

    data: {
        graph: {
            nodes: [
                //{id: 1,type: 'concentrator'},
                //{id: 2,type: 'device',name: 'device-2'},
                //{id: 3,type: 'device',name: 'device-3'},
                //{id: 4,type: 'device',name: 'device-4'},
                //{id: 5,type: 'device',name: 'device-5'},
                //{id: 6,type: 'device',name: 'device-6'},
                //{id: 7,type: 'device',name: 'device-7'},
                //{id: 8,type: 'device',name: 'device-8'},
                //{id: 9,type: 'device',name: 'device-9'},
                //{id: 10,type: 'device',name: 'device-10'},
                //{id: 11,type: 'device',name: 'device-11'},
                //{id: 12,type: 'device',name: 'device-12'},
                //{id: 13,type: 'device',name: 'device-13'},
                //{id: 14,type: 'device',name: 'device-14', alarms: 5},
                //{id: 15,type: 'device',name: 'device-15'},
                //{id: 16,type: 'device',name: 'device-16'},
                //{id: 17,type: 'device',name: 'device-17'},
                //{id: 18,type: 'device',name: 'device-18', alarms: 3},
                //{id: 19,type: 'device',name: 'device-19', alarms: 1},
                //{id: 20,type: 'device',name: 'device-20', alarms: 9},
                //{id: 21,type: 'device',name: 'device-21'},
                //{id: 22,type: 'device',name: 'device-22'},
                //{id: 23,type: 'device',name: 'device-23'},
                //{id: 24,type: 'device',name: 'device-24'},
                //{id: 25,type: 'device',name: 'device-25'},
                //{id: 26,type: 'device',name: 'device-26'},
                //{id: 27,type: 'device',name: 'device-27'},
                //{id: 28,type: 'device',name: 'device-28'}
                {id:'1', name:'device-1', serialNumber:'SN001',deviceType:'Actaris SL7000',deviceConfiguration:'Config 1',alarms: 0, gateway:true},
                {id:'2', name:'device-2', serialNumber:'SN002',deviceType:'Elster A1800',deviceConfiguration:'Default',alarms: 0, issues: 0},
                {id:'3', name:'de<vi>ce-3', serialNumber:'SN003',deviceType:'Elster<AS1440>',deviceConfiguration:'Default',alarms: 0, issues: 2},
                {id:'4', name:'device-4', serialNumber:'SN004',deviceType:'Elster AS1440',deviceConfiguration:'Default',alarms: 0, issues: 0},
                {id:'5', name:'device-5', serialNumber:'SN005',deviceType:'Elster AS1440',deviceConfiguration:'Default',alarms: 0},
                {id:'6', name:'device-6', serialNumber:'SN006',deviceType:'Elster AS1440',deviceConfiguration:'Default',alarms: 1},
                {id:'7', name:'device-7', serialNumber:'SN007',deviceType:'Elster AS1440',deviceConfiguration:'Default',alarms: 0},
                {id:'8', name:'device-8', serialNumber:'SN008',deviceType:'Elster AS1440',deviceConfiguration:'Default',alarms: 1, issues: 17},
                {id:'9', name:'device-9', serialNumber:'SN009',deviceType:'Elster AS1440',deviceConfiguration:'Default',alarms: 0, failedComTasks:['Communication task 1']},
                {id:'10',name:'device-10',serialNumber:'SN010',deviceType:'Iskra 382',deviceConfiguration:'Config 2',alarms: 0, failedComTasks:['Read load profiles', 'Another task']},
                //{id:'11',type:'device',name:'device-11',alarms: 0},
                //{id:'12',type:'device',name:'device-12',alarms: 0},
                //{id:'13',type:'device',name:'device-13',alarms: 0},
                //{id:'14',type:'device',name:'device-14',alarms: 0},
                //{id:'15',type:'device',name:'device-15',alarms: 0},
                //{id:'16',type:'device',name:'device-16',alarms: 0},
                //{id:'17',type:'device',name:'device-17',alarms: 4},
                //{id:'18',type:'device',name:'device-18',alarms: 0},
                //{id:'19',type:'device',name:'device-19',alarms: 0},
                //{id:'20',type:'device',name:'device-20',alarms: 0},
                //{id:'21',type:'device',name:'device-21',alarms: 0},
                //{id:'22',type:'device',name:'device-22',alarms: 0},
                //{id:'23',type:'device',name:'device-23',alarms: 3},
                //{id:'24',type:'device',name:'device-24',alarms: 0},
                //{id:'25',type:'device',name:'device-25',alarms: 0},
                //{id:'26',type:'device',name:'device-26',alarms: 0},
                //{id:'27',type:'device',name:'device-27',alarms: 0},
                //{id:'28',type:'device',name:'device-28',alarms: 0},
                //{id:'29',type:'device',name:'device-29',alarms: 0},
                //{id:'30',type:'device',name:'device-30',alarms: 0},
                //{id:'31',type:'device',name:'device-31',alarms: 0},
                //{id:'32',type:'device',name:'device-32',alarms: 0},
                //{id:'33',type:'device',name:'device-33',alarms: 0},
                //{id:'34',type:'device',name:'device-34',alarms: 0},
                //{id:'35',type:'device',name:'device-35',alarms: 0},
                //{id:'36',type:'device',name:'device-36',alarms: 0},
                //{id:'37',type:'device',name:'device-37',alarms: 0},
                //{id:'38',type:'device',name:'device-38',alarms: 0},
                //{id:'39',type:'device',name:'device-39',alarms: 0},
                //{id:'40',type:'device',name:'device-40',alarms: 0},
                //{id:'41',type:'device',name:'device-41',alarms: 0},
                //{id:'42',type:'device',name:'device-42',alarms: 0},
                //{id:'43',type:'device',name:'device-43',alarms: 0},
                //{id:'44',type:'device',name:'device-44',alarms: 3},
                //{id:'45',type:'device',name:'device-45',alarms: 0},
                //{id:'46',type:'device',name:'device-46',alarms: 0},
                //{id:'47',type:'device',name:'device-47',alarms: 0},
                //{id:'48',type:'device',name:'device-48',alarms: 0},
                //{id:'49',type:'device',name:'device-49',alarms: 0},
                //{id:'50',type:'device',name:'device-50',alarms: 0},
                //{id:'51',type:'device',name:'device-51',alarms: 3},
                //{id:'52',type:'device',name:'device-52',alarms: 0},
                //{id:'53',type:'device',name:'device-53',alarms: 0},
                //{id:'54',type:'device',name:'device-54',alarms: 0},
                //{id:'55',type:'device',name:'device-55',alarms: 0},
                //{id:'56',type:'device',name:'device-56',alarms: 0},
                //{id:'57',type:'device',name:'device-57',alarms: 0},
                //{id:'58',type:'device',name:'device-58',alarms: 2},
                //{id:'59',type:'device',name:'device-59',alarms: 0},
                //{id:'60',type:'device',name:'device-60',alarms: 0},
                //{id:'61',type:'device',name:'device-61',alarms: 0},
                //{id:'62',type:'device',name:'device-62',alarms: 0},
                //{id:'63',type:'device',name:'device-63',alarms: 0},
                //{id:'64',type:'device',name:'device-64',alarms: 0},
                //{id:'65',type:'device',name:'device-65',alarms: 0},
                //{id:'66',type:'device',name:'device-66',alarms: 0},
                //{id:'67',type:'device',name:'device-67',alarms: 2},
                //{id:'68',type:'device',name:'device-68',alarms: 0},
                //{id:'69',type:'device',name:'device-69',alarms: 0},
                //{id:'70',type:'device',name:'device-70',alarms: 0},
                //{id:'71',type:'device',name:'device-71',alarms: 0},
                //{id:'72',type:'device',name:'device-72',alarms: 0},
                //{id:'73',type:'device',name:'device-73',alarms: 0},
                //{id:'74',type:'device',name:'device-74',alarms: 0},
                //{id:'75',type:'device',name:'device-75',alarms: 0},
                //{id:'76',type:'device',name:'device-76',alarms: 0},
                //{id:'77',type:'device',name:'device-77',alarms: 0},
                //{id:'78',type:'device',name:'device-78',alarms: 0},
                //{id:'79',type:'device',name:'device-79',alarms: 0},
                //{id:'80',type:'device',name:'device-80',alarms: 0},
                //{id:'81',type:'device',name:'device-81',alarms: 0},
                //{id:'82',type:'device',name:'device-82',alarms: 0},
                //{id:'83',type:'device',name:'device-83',alarms: 0},
                //{id:'84',type:'device',name:'device-84',alarms: 0},
                //{id:'85',type:'device',name:'device-85',alarms: 0},
                //{id:'86',type:'device',name:'device-86',alarms: 0},
                //{id:'87',type:'device',name:'device-87',alarms: 0},
                //{id:'88',type:'device',name:'device-88',alarms: 0},
                //{id:'89',type:'device',name:'device-89',alarms: 0},
                //{id:'90',type:'device',name:'device-90',alarms: 0},
                //{id:'91',type:'device',name:'device-91',alarms: 0},
                //{id:'92',type:'device',name:'device-92',alarms: 0},
                //{id:'93',type:'device',name:'device-93',alarms: 0},
                //{id:'94',type:'device',name:'device-94',alarms: 3},
                //{id:'95',type:'device',name:'device-95',alarms: 1},
                //{id:'96',type:'device',name:'device-96',alarms: 0},
                //{id:'97',type:'device',name:'device-97',alarms: 0},
                //{id:'98',type:'device',name:'device-98',alarms: 0},
                //{id:'99',type:'device',name:'device-99',alarms: 0},
                //{id:'100',type:'device',name:'device-100',alarms: 0},
                //{id:'101',type:'device',name:'device-101',alarms: 4},
                //{id:'102',type:'device',name:'device-102',alarms: 0},
                //{id:'103',type:'device',name:'device-103',alarms: 0},
                //{id:'104',type:'device',name:'device-104',alarms: 1},
                //{id:'105',type:'device',name:'device-105',alarms: 0},
                //{id:'106',type:'device',name:'device-106',alarms: 0},
                //{id:'107',type:'device',name:'device-107',alarms: 0},
                //{id:'108',type:'device',name:'device-108',alarms: 0},
                //{id:'109',type:'device',name:'device-109',alarms: 0},
                //{id:'110',type:'device',name:'device-110',alarms: 0},
                //{id:'111',type:'device',name:'device-111',alarms: 3},
                //{id:'112',type:'device',name:'device-112',alarms: 0},
                //{id:'113',type:'device',name:'device-113',alarms: 0},
                //{id:'114',type:'device',name:'device-114',alarms: 0},
                //{id:'115',type:'device',name:'device-115',alarms: 0},
                //{id:'116',type:'device',name:'device-116',alarms: 0},
                //{id:'117',type:'device',name:'device-117',alarms: 0},
                //{id:'118',type:'device',name:'device-118',alarms: 0},
                //{id:'119',type:'device',name:'device-119',alarms: 2},
                //{id:'120',type:'device',name:'device-120',alarms: 0},
                //{id:'121',type:'device',name:'device-121',alarms: 0},
                //{id:'122',type:'device',name:'device-122',alarms: 0},
                //{id:'123',type:'device',name:'device-123',alarms: 0},
                //{id:'124',type:'device',name:'device-124',alarms: 0},
                //{id:'125',type:'device',name:'device-125',alarms: 0},
                //{id:'126',type:'device',name:'device-126',alarms: 0},
                //{id:'127',type:'device',name:'device-127',alarms: 0},
                //{id:'128',type:'device',name:'device-128',alarms: 2},
                //{id:'129',type:'device',name:'device-129',alarms: 0},
                //{id:'130',type:'device',name:'device-130',alarms: 0},
                //{id:'131',type:'device',name:'device-131',alarms: 0},
                //{id:'132',type:'device',name:'device-132',alarms: 0},
                //{id:'133',type:'device',name:'device-133',alarms: 0},
                //{id:'134',type:'device',name:'device-134',alarms: 2},
                //{id:'135',type:'device',name:'device-135',alarms: 0},
                //{id:'136',type:'device',name:'device-136',alarms: 1},
                //{id:'137',type:'device',name:'device-137',alarms: 0},
                //{id:'138',type:'device',name:'device-138',alarms: 0},
                //{id:'139',type:'device',name:'device-139',alarms: 0},
                //{id:'140',type:'device',name:'device-140',alarms: 0},
                //{id:'141',type:'device',name:'device-141',alarms: 0},
                //{id:'142',type:'device',name:'device-142',alarms: 0},
                //{id:'143',type:'device',name:'device-143',alarms: 0},
                //{id:'144',type:'device',name:'device-144',alarms: 0},
                //{id:'145',type:'device',name:'device-145',alarms: 0},
                //{id:'146',type:'device',name:'device-146',alarms: 0},
                //{id:'147',type:'device',name:'device-147',alarms: 0},
                //{id:'148',type:'device',name:'device-148',alarms: 0},
                //{id:'149',type:'device',name:'device-149',alarms: 0},
                //{id:'150',type:'device',name:'device-150',alarms: 0},
                //{id:'151',type:'device',name:'device-151',alarms: 0},
                //{id:'152',type:'device',name:'device-152',alarms: 0},
                //{id:'153',type:'device',name:'device-153',alarms: 0},
                //{id:'154',type:'device',name:'device-154',alarms: 0},
                //{id:'155',type:'device',name:'device-155',alarms: 0},
                //{id:'156',type:'device',name:'device-156',alarms: 0},
                //{id:'157',type:'device',name:'device-157',alarms: 0},
                //{id:'158',type:'device',name:'device-158',alarms: 0},
                //{id:'159',type:'device',name:'device-159',alarms: 0},
                //{id:'160',type:'device',name:'device-160',alarms: 0},
                //{id:'161',type:'device',name:'device-161',alarms: 0},
                //{id:'162',type:'device',name:'device-162',alarms: 5},
                //{id:'163',type:'device',name:'device-163',alarms: 0},
                //{id:'164',type:'device',name:'device-164',alarms: 0},
                //{id:'165',type:'device',name:'device-165',alarms: 0},
                //{id:'166',type:'device',name:'device-166',alarms: 0},
                //{id:'167',type:'device',name:'device-167',alarms: 0},
                //{id:'168',type:'device',name:'device-168',alarms: 0},
                //{id:'169',type:'device',name:'device-169',alarms: 0},
                //{id:'170',type:'device',name:'device-170',alarms: 0},
                //{id:'171',type:'device',name:'device-171',alarms: 2},
                //{id:'172',type:'device',name:'device-172',alarms: 0},
                //{id:'173',type:'device',name:'device-173',alarms: 0},
                //{id:'174',type:'device',name:'device-174',alarms: 0},
                //{id:'175',type:'device',name:'device-175',alarms: 0},
                //{id:'176',type:'device',name:'device-176',alarms: 0},
                //{id:'177',type:'device',name:'device-177',alarms: 0},
                //{id:'178',type:'device',name:'device-178',alarms: 0},
                //{id:'179',type:'device',name:'device-179',alarms: 0},
                //{id:'180',type:'device',name:'device-180',alarms: 0},
                //{id:'181',type:'device',name:'device-181',alarms: 0},
                //{id:'182',type:'device',name:'device-182',alarms: 0},
                //{id:'183',type:'device',name:'device-183',alarms: 0},
                //{id:'184',type:'device',name:'device-184',alarms: 0},
                //{id:'185',type:'device',name:'device-185',alarms: 2},
                //{id:'186',type:'device',name:'device-186',alarms: 0},
                //{id:'187',type:'device',name:'device-187',alarms: 0},
                //{id:'188',type:'device',name:'device-188',alarms: 0},
                //{id:'189',type:'device',name:'device-189',alarms: 0},
                //{id:'190',type:'device',name:'device-190',alarms: 0},
                //{id:'191',type:'device',name:'device-191',alarms: 0},
                //{id:'192',type:'device',name:'device-192',alarms: 0},
                //{id:'193',type:'device',name:'device-193',alarms: 0},
                //{id:'194',type:'device',name:'device-194',alarms: 0},
                //{id:'195',type:'device',name:'device-195',alarms: 0},
                //{id:'196',type:'device',name:'device-196',alarms: 0},
                //{id:'197',type:'device',name:'device-197',alarms: 0},
                //{id:'198',type:'device',name:'device-198',alarms: 0},
                //{id:'199',type:'device',name:'device-199',alarms: 1},
                //{id:'200',type:'device',name:'device-200',alarms: 0},
                //{id:'201',type:'device',name:'device-201',alarms: 0},
                //{id:'202',type:'device',name:'device-202',alarms: 0},
                //{id:'203',type:'device',name:'device-203',alarms: 0},
                //{id:'204',type:'device',name:'device-204',alarms: 0},
                //{id:'205',type:'device',name:'device-205',alarms: 0},
                //{id:'206',type:'device',name:'device-206',alarms: 0},
                //{id:'207',type:'device',name:'device-207',alarms: 0},
                //{id:'208',type:'device',name:'device-208',alarms: 0},
                //{id:'209',type:'device',name:'device-209',alarms: 0},
                //{id:'210',type:'device',name:'device-210',alarms: 0},
                //{id:'211',type:'device',name:'device-211',alarms: 0},
                //{id:'212',type:'device',name:'device-212',alarms: 0},
                //{id:'213',type:'device',name:'device-213',alarms: 0},
                //{id:'214',type:'device',name:'device-214',alarms: 0},
                //{id:'215',type:'device',name:'device-215',alarms: 0},
                //{id:'216',type:'device',name:'device-216',alarms: 0},
                //{id:'217',type:'device',name:'device-217',alarms: 0},
                //{id:'218',type:'device',name:'device-218',alarms: 0},
                //{id:'219',type:'device',name:'device-219',alarms: 0},
                //{id:'220',type:'device',name:'device-220',alarms: 0},
                //{id:'221',type:'device',name:'device-221',alarms: 0},
                //{id:'222',type:'device',name:'device-222',alarms: 0},
                //{id:'223',type:'device',name:'device-223',alarms: 0},
                //{id:'224',type:'device',name:'device-224',alarms: 0},
                //{id:'225',type:'device',name:'device-225',alarms: 3},
                //{id:'226',type:'device',name:'device-226',alarms: 0},
                //{id:'227',type:'device',name:'device-227',alarms: 0},
                //{id:'228',type:'device',name:'device-228',alarms: 0},
                //{id:'229',type:'device',name:'device-229',alarms: 0},
                //{id:'230',type:'device',name:'device-230',alarms: 0},
                //{id:'231',type:'device',name:'device-231',alarms: 0},
                //{id:'232',type:'device',name:'device-232',alarms: 0},
                //{id:'233',type:'device',name:'device-233',alarms: 0},
                //{id:'234',type:'device',name:'device-234',alarms: 0},
                //{id:'235',type:'device',name:'device-235',alarms: 0},
                //{id:'236',type:'device',name:'device-236',alarms: 0},
                //{id:'237',type:'device',name:'device-237',alarms: 0},
                //{id:'238',type:'device',name:'device-238',alarms: 0},
                //{id:'239',type:'device',name:'device-239',alarms: 0},
                //{id:'240',type:'device',name:'device-240',alarms: 0},
                //{id:'241',type:'device',name:'device-241',alarms: 0},
                //{id:'242',type:'device',name:'device-242',alarms: 0},
                //{id:'243',type:'device',name:'device-243',alarms: 0},
                //{id:'244',type:'device',name:'device-244',alarms: 0},
                //{id:'245',type:'device',name:'device-245',alarms: 0},
                //{id:'246',type:'device',name:'device-246',alarms: 0},
                //{id:'247',type:'device',name:'device-247',alarms: 0},
                //{id:'248',type:'device',name:'device-248',alarms: 0},
                //{id:'249',type:'device',name:'device-249',alarms: 0},
            ],
            links: [
                //{source: 1, target: 2,linkQuality: 5},
                //{source: 1, target: 3,linkQuality: 4},
                //{source: 1, target: 4,linkQuality: 3},
                //{source: 1, target: 5,linkQuality: 2},
                //{source: 1, target: 6,linkQuality: 1},
                //{source: 1, target: 7,linkQuality: 5},
                //{source: 1, target: 8,linkQuality: 4},
                //{source: 1, target: 9,linkQuality: 3},
                //{source: 1, target: 10,linkQuality: 2},
                //{source: 1, target: 11,linkQuality: 1},
                //{source: 1, target: 12,linkQuality: 5},
                //{source: 5, target: 13,linkQuality: 4},
                //{source: 5, target: 14,linkQuality: 3},
                //{source: 5, target: 15,linkQuality: 2},
                //{source: 5, target: 16,linkQuality: 1},
                //{source: 5, target: 17,linkQuality: 5},
                //{source: 14, target: 18,linkQuality: 4},
                //{source: 18, target: 19,linkQuality: 3},
                //{source: 18, target: 20,linkQuality: 2},
                //{source: 18, target: 21,linkQuality: 1},
                //{source: 18, target: 22,linkQuality: 5},
                //{source: 18, target: 23,linkQuality: 4},
                //{source: 18, target: 24,linkQuality: 3},
                //{source: 18, target: 25,linkQuality: 2},
                //{source: 18, target: 26,linkQuality: 1},
                //{source: 8, target: 27,linkQuality: 5},
                //{source: 8, target: 28,linkQuality: 4}
                {id: 'l1',source: 1, target: 2,linkQuality: 60},
                {id: 'l2',source: 1, target: 3,linkQuality: 70},
                {id: 'l3',source: 2, target: 4,linkQuality: 75},
                {id: 'l4',source: 4, target: 5,linkQuality: 50},
                {id: 'l5',source: 2, target: 6,linkQuality: 60},
                {id: 'l6',source: 6, target: 7,linkQuality: 52},
                {id: 'l7',source: 7, target: 8,linkQuality: 51},
                {id: 'l8',source: 6, target: 9,linkQuality: 40},
                {id: 'l9',source: 7, target: 10,linkQuality: 45},
                //{id: 'l10',source: 1, target: 11,linkQuality: 3},
                //{id: 'l11',source: 5, target: 12,linkQuality: 0},
                //{id: 'l12',source: 4, target: 13,linkQuality: 1},
                //{id: 'l13',source: 11, target: 14,linkQuality: 2},
                //{id: 'l14',source: 6, target: 15,linkQuality: 1},
                //{id: 'l15',source: 6, target: 16,linkQuality: 3},
                //{id: 'l16',source: 5, target: 17,linkQuality: 4},
                //{id: 'l17',source: 4, target: 18,linkQuality: 2},
                //{id: 'l18',source: 8, target: 19,linkQuality: 3},
                //{id: 'l19',source: 12, target: 20,linkQuality: 3},
                //{id: 'l20',source: 8, target: 21,linkQuality: 1},
                //{id: 'l21',source: 8, target: 22,linkQuality: 2},
                //{id: 'l22',source: 3, target: 23,linkQuality: 0},
                //{id: 'l23',source: 21, target: 24,linkQuality: 2},
                //{id: 'l24',source: 18, target: 25,linkQuality: 2},
                //{id: 'l25',source: 8, target: 26,linkQuality: 1},
                //{id: 'l26',source: 20, target: 27,linkQuality: 0},
                //{id: 'l27',source: 5, target: 28,linkQuality: 3},
                //{id: 'l28',source: 3, target: 29,linkQuality: 3},
                //{id: 'l29',source: 14, target: 30,linkQuality: 0},
                //{id: 'l30',source: 14, target: 31,linkQuality: 3},
                //{id: 'l31',source: 21, target: 32,linkQuality: 4},
                //{id: 'l32',source: 3, target: 33,linkQuality: 4},
                //{id: 'l33',source: 14, target: 34,linkQuality: 3},
                //{id: 'l34',source: 13, target: 35,linkQuality: 3},
                //{id: 'l35',source: 32, target: 36,linkQuality: 1},
                //{id: 'l36',source: 24, target: 37,linkQuality: 0},
                //{id: 'l37',source: 17, target: 38,linkQuality: 4},
                //{id: 'l38',source: 24, target: 39,linkQuality: 3},
                //{id: 'l39',source: 24, target: 40,linkQuality: 0},
                //{id: 'l40',source: 30, target: 41,linkQuality: 4},
                //{id: 'l41',source: 39, target: 42,linkQuality: 4},
                //{id: 'l42',source: 22, target: 43,linkQuality: 1},
                //{id: 'l43',source: 8, target: 44,linkQuality: 2},
                //{id: 'l44',source: 44, target: 45,linkQuality: 0},
                //{id: 'l45',source: 38, target: 46,linkQuality: 4},
                //{id: 'l46',source: 11, target: 47,linkQuality: 0},
                //{id: 'l47',source: 35, target: 48,linkQuality: 3},
                //{id: 'l48',source: 22, target: 49,linkQuality: 2},
                //{id: 'l49',source: 47, target: 50,linkQuality: 0},
                //{id: 'l50',source: 20, target: 51,linkQuality: 1},
                //{id: 'l51',source: 6, target: 52,linkQuality: 4},
                //{id: 'l52',source: 2, target: 53,linkQuality: 2},
                //{id: 'l53',source: 51, target: 54,linkQuality: 3},
                //{id: 'l54',source: 29, target: 55,linkQuality: 1},
                //{id: 'l55',source: 5, target: 56,linkQuality: 2},
                //{id: 'l56',source: 43, target: 57,linkQuality: 1},
                //{id: 'l57',source: 42, target: 58,linkQuality: 1},
                //{id: 'l58',source: 22, target: 59,linkQuality: 3},
                //{id: 'l59',source: 45, target: 60,linkQuality: 4},
                //{id: 'l60',source: 7, target: 61,linkQuality: 3},
                //{id: 'l61',source: 19, target: 62,linkQuality: 2},
                //{id: 'l62',source: 13, target: 63,linkQuality: 1},
                //{id: 'l63',source: 53, target: 64,linkQuality: 4},
                //{id: 'l64',source: 23, target: 65,linkQuality: 1},
                //{id: 'l65',source: 9, target: 66,linkQuality: 2},
                //{id: 'l66',source: 37, target: 67,linkQuality: 1},
                //{id: 'l67',source: 18, target: 68,linkQuality: 4},
                //{id: 'l68',source: 18, target: 69,linkQuality: 1},
                //{id: 'l69',source: 48, target: 70,linkQuality: 2},
                //{id: 'l70',source: 4, target: 71,linkQuality: 1},
                //{id: 'l71',source: 56, target: 72,linkQuality: 3},
                //{id: 'l72',source: 49, target: 73,linkQuality: 2},
                //{id: 'l73',source: 10, target: 74,linkQuality: 2},
                //{id: 'l74',source: 47, target: 75,linkQuality: 4},
                //{id: 'l75',source: 49, target: 76,linkQuality: 1},
                //{id: 'l76',source: 17, target: 77,linkQuality: 2},
                //{id: 'l77',source: 29, target: 78,linkQuality: 0},
                //{id: 'l78',source: 72, target: 79,linkQuality: 1},
                //{id: 'l79',source: 57, target: 80,linkQuality: 3},
                //{id: 'l80',source: 18, target: 81,linkQuality: 4},
                //{id: 'l81',source: 26, target: 82,linkQuality: 1},
                //{id: 'l82',source: 16, target: 83,linkQuality: 3},
                //{id: 'l83',source: 65, target: 84,linkQuality: 1},
                //{id: 'l84',source: 58, target: 85,linkQuality: 4},
                //{id: 'l85',source: 74, target: 86,linkQuality: 3},
                //{id: 'l86',source: 21, target: 87,linkQuality: 4},
                //{id: 'l87',source: 7, target: 88,linkQuality: 4},
                //{id: 'l88',source: 21, target: 89,linkQuality: 4},
                //{id: 'l89',source: 1, target: 90,linkQuality: 2},
                //{id: 'l90',source: 25, target: 91,linkQuality: 1},
                //{id: 'l91',source: 90, target: 92,linkQuality: 2},
                //{id: 'l92',source: 54, target: 93,linkQuality: 1},
                //{id: 'l93',source: 75, target: 94,linkQuality: 3},
                //{id: 'l94',source: 2, target: 95,linkQuality: 1},
                //{id: 'l95',source: 61, target: 96,linkQuality: 3},
                //{id: 'l96',source: 71, target: 97,linkQuality: 0},
                //{id: 'l97',source: 97, target: 98,linkQuality: 0},
                //{id: 'l98',source: 72, target: 99,linkQuality: 3},
                //{id: 'l99',source: 23, target: 100,linkQuality: 3},
                //{id: 'l100',source: 90, target: 101,linkQuality: 4},
                //{id: 'l101',source: 9, target: 102,linkQuality: 2},
                //{id: 'l102',source: 99, target: 103,linkQuality: 4},
                //{id: 'l103',source: 79, target: 104,linkQuality: 3},
                //{id: 'l104',source: 42, target: 105,linkQuality: 4},
                //{id: 'l105',source: 11, target: 106,linkQuality: 0},
                //{id: 'l106',source: 23, target: 107,linkQuality: 3},
                //{id: 'l107',source: 31, target: 108,linkQuality: 1},
                //{id: 'l108',source: 106, target: 109,linkQuality: 3},
                //{id: 'l109',source: 100, target: 110,linkQuality: 4},
                //{id: 'l110',source: 94, target: 111,linkQuality: 4},
                //{id: 'l111',source: 37, target: 112,linkQuality: 2},
                //{id: 'l112',source: 66, target: 113,linkQuality: 0},
                //{id: 'l113',source: 90, target: 114,linkQuality: 2},
                //{id: 'l114',source: 50, target: 115,linkQuality: 3},
                //{id: 'l115',source: 71, target: 116,linkQuality: 0},
                //{id: 'l116',source: 89, target: 117,linkQuality: 2},
                //{id: 'l117',source: 116, target: 118,linkQuality: 1},
                //{id: 'l118',source: 116, target: 119,linkQuality: 0},
                //{id: 'l119',source: 54, target: 120,linkQuality: 1},
                //{id: 'l120',source: 87, target: 121,linkQuality: 1},
                //{id: 'l121',source: 31, target: 122,linkQuality: 3},
                //{id: 'l122',source: 15, target: 123,linkQuality: 3},
                //{id: 'l123',source: 51, target: 124,linkQuality: 0},
                //{id: 'l124',source: 19, target: 125,linkQuality: 1},
                //{id: 'l125',source: 72, target: 126,linkQuality: 4},
                //{id: 'l126',source: 123, target: 127,linkQuality: 3},
                //{id: 'l127',source: 77, target: 128,linkQuality: 2},
                //{id: 'l128',source: 42, target: 129,linkQuality: 4},
                //{id: 'l129',source: 114, target: 130,linkQuality: 3},
                //{id: 'l130',source: 18, target: 131,linkQuality: 0},
                //{id: 'l131',source: 24, target: 132,linkQuality: 1},
                //{id: 'l132',source: 10, target: 133,linkQuality: 3},
                //{id: 'l133',source: 46, target: 134,linkQuality: 2},
                //{id: 'l134',source: 80, target: 135,linkQuality: 3},
                //{id: 'l135',source: 111, target: 136,linkQuality: 0},
                //{id: 'l136',source: 68, target: 137,linkQuality: 4},
                //{id: 'l137',source: 67, target: 138,linkQuality: 3},
                //{id: 'l138',source: 18, target: 139,linkQuality: 0},
                //{id: 'l139',source: 68, target: 140,linkQuality: 2},
                //{id: 'l140',source: 108, target: 141,linkQuality: 3},
                //{id: 'l141',source: 135, target: 142,linkQuality: 0},
                //{id: 'l142',source: 56, target: 143,linkQuality: 3},
                //{id: 'l143',source: 84, target: 144,linkQuality: 0},
                //{id: 'l144',source: 103, target: 145,linkQuality: 3},
                //{id: 'l145',source: 95, target: 146,linkQuality: 2},
                //{id: 'l146',source: 61, target: 147,linkQuality: 2},
                //{id: 'l147',source: 8, target: 148,linkQuality: 0},
                //{id: 'l148',source: 146, target: 149,linkQuality: 0},
                //{id: 'l149',source: 28, target: 150,linkQuality: 3},
                //{id: 'l150',source: 79, target: 151,linkQuality: 1},
                //{id: 'l151',source: 55, target: 152,linkQuality: 4},
                //{id: 'l152',source: 31, target: 153,linkQuality: 3},
                //{id: 'l153',source: 116, target: 154,linkQuality: 0},
                //{id: 'l154',source: 26, target: 155,linkQuality: 2},
                //{id: 'l155',source: 77, target: 156,linkQuality: 4},
                //{id: 'l156',source: 21, target: 157,linkQuality: 4},
                //{id: 'l157',source: 87, target: 158,linkQuality: 0},
                //{id: 'l158',source: 110, target: 159,linkQuality: 4},
                //{id: 'l159',source: 3, target: 160,linkQuality: 1},
                //{id: 'l160',source: 85, target: 161,linkQuality: 1},
                //{id: 'l161',source: 60, target: 162,linkQuality: 2},
                //{id: 'l162',source: 55, target: 163,linkQuality: 0},
                //{id: 'l163',source: 55, target: 164,linkQuality: 0},
                //{id: 'l164',source: 131, target: 165,linkQuality: 0},
                //{id: 'l165',source: 59, target: 166,linkQuality: 2},
                //{id: 'l166',source: 84, target: 167,linkQuality: 3},
                //{id: 'l167',source: 158, target: 168,linkQuality: 2},
                //{id: 'l168',source: 159, target: 169,linkQuality: 0},
                //{id: 'l169',source: 73, target: 170,linkQuality: 0},
                //{id: 'l170',source: 131, target: 171,linkQuality: 0},
                //{id: 'l171',source: 74, target: 172,linkQuality: 3},
                //{id: 'l172',source: 152, target: 173,linkQuality: 3},
                //{id: 'l173',source: 49, target: 174,linkQuality: 4},
                //{id: 'l174',source: 125, target: 175,linkQuality: 0},
                //{id: 'l175',source: 151, target: 176,linkQuality: 4},
                //{id: 'l176',source: 135, target: 177,linkQuality: 2},
                //{id: 'l177',source: 104, target: 178,linkQuality: 1},
                //{id: 'l178',source: 122, target: 179,linkQuality: 0},
                //{id: 'l179',source: 50, target: 180,linkQuality: 3},
                //{id: 'l180',source: 151, target: 181,linkQuality: 0},
                //{id: 'l181',source: 82, target: 182,linkQuality: 1},
                //{id: 'l182',source: 117, target: 183,linkQuality: 1},
                //{id: 'l183',source: 70, target: 184,linkQuality: 4},
                //{id: 'l184',source: 139, target: 185,linkQuality: 2},
                //{id: 'l185',source: 117, target: 186,linkQuality: 2},
                //{id: 'l186',source: 2, target: 187,linkQuality: 2},
                //{id: 'l187',source: 154, target: 188,linkQuality: 2},
                //{id: 'l188',source: 93, target: 189,linkQuality: 4},
                //{id: 'l189',source: 90, target: 190,linkQuality: 3},
                //{id: 'l190',source: 50, target: 191,linkQuality: 2},
                //{id: 'l191',source: 1, target: 192,linkQuality: 1},
                //{id: 'l192',source: 30, target: 193,linkQuality: 4},
                //{id: 'l193',source: 79, target: 194,linkQuality: 0},
                //{id: 'l194',source: 69, target: 195,linkQuality: 3},
                //{id: 'l195',source: 157, target: 196,linkQuality: 2},
                //{id: 'l196',source: 190, target: 197,linkQuality: 0},
                //{id: 'l197',source: 174, target: 198,linkQuality: 2},
                //{id: 'l198',source: 79, target: 199,linkQuality: 4},
                //{id: 'l199',source: 128, target: 200,linkQuality: 4},
                //{id: 'l200',source: 10, target: 201,linkQuality: 0},
                //{id: 'l201',source: 146, target: 202,linkQuality: 4},
                //{id: 'l202',source: 180, target: 203,linkQuality: 1},
                //{id: 'l203',source: 110, target: 204,linkQuality: 4},
                //{id: 'l204',source: 128, target: 205,linkQuality: 3},
                //{id: 'l205',source: 29, target: 206,linkQuality: 4},
                //{id: 'l206',source: 160, target: 207,linkQuality: 1},
                //{id: 'l207',source: 126, target: 208,linkQuality: 3},
                //{id: 'l208',source: 33, target: 209,linkQuality: 3},
                //{id: 'l209',source: 48, target: 210,linkQuality: 3},
                //{id: 'l210',source: 188, target: 211,linkQuality: 4},
                //{id: 'l211',source: 49, target: 212,linkQuality: 2},
                //{id: 'l212',source: 59, target: 213,linkQuality: 4},
                //{id: 'l213',source: 14, target: 214,linkQuality: 1},
                //{id: 'l214',source: 163, target: 215,linkQuality: 4},
                //{id: 'l215',source: 194, target: 216,linkQuality: 0},
                //{id: 'l216',source: 100, target: 217,linkQuality: 0},
                //{id: 'l217',source: 45, target: 218,linkQuality: 0},
                //{id: 'l218',source: 100, target: 219,linkQuality: 2},
                //{id: 'l219',source: 84, target: 220,linkQuality: 0},
                //{id: 'l220',source: 87, target: 221,linkQuality: 2},
                //{id: 'l221',source: 114, target: 222,linkQuality: 3},
                //{id: 'l222',source: 143, target: 223,linkQuality: 0},
                //{id: 'l223',source: 40, target: 224,linkQuality: 1},
                //{id: 'l224',source: 55, target: 225,linkQuality: 3},
                //{id: 'l225',source: 199, target: 226,linkQuality: 0},
                //{id: 'l226',source: 162, target: 227,linkQuality: 4},
                //{id: 'l227',source: 143, target: 228,linkQuality: 2},
                //{id: 'l228',source: 74, target: 229,linkQuality: 2},
                //{id: 'l229',source: 197, target: 230,linkQuality: 0},
                //{id: 'l230',source: 166, target: 231,linkQuality: 1},
                //{id: 'l231',source: 228, target: 232,linkQuality: 2},
                //{id: 'l232',source: 158, target: 233,linkQuality: 1},
                //{id: 'l233',source: 210, target: 234,linkQuality: 2},
                //{id: 'l234',source: 192, target: 235,linkQuality: 4},
                //{id: 'l235',source: 234, target: 236,linkQuality: 4},
                //{id: 'l236',source: 137, target: 237,linkQuality: 1},
                //{id: 'l237',source: 116, target: 238,linkQuality: 2},
                //{id: 'l238',source: 130, target: 239,linkQuality: 0},
                //{id: 'l239',source: 79, target: 240,linkQuality: 3},
                //{id: 'l240',source: 53, target: 241,linkQuality: 1},
                //{id: 'l241',source: 240, target: 242,linkQuality: 2},
                //{id: 'l242',source: 51, target: 243,linkQuality: 4},
                //{id: 'l243',source: 98, target: 244,linkQuality: 3},
                //{id: 'l244',source: 218, target: 245,linkQuality: 2},
                //{id: 'l245',source: 131, target: 246,linkQuality: 2},
                //{id: 'l246',source: 150, target: 247,linkQuality: 0},
                //{id: 'l247',source: 44, target: 248,linkQuality: 2},
                //{id: 'l248',source: 141, target: 249,linkQuality: 4},
            ]
        }
    }


});