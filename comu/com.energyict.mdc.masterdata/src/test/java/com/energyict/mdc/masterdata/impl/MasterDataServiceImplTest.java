package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterType;
import org.fest.assertions.core.Condition;
import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Copyrights EnergyICT
 * Date: 15.12.15
 * Time: 15:00
 */
@RunWith(MockitoJUnitRunner.class)
public class MasterDataServiceImplTest {

    private static InMemoryPersistence inMemoryPersistence = new InMemoryPersistence();

    private static final List<Pair<String, String>> readingTypes = Arrays.asList(
            Pair.of("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0"),
            Pair.of("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0"),
            Pair.of("0.0.0.1.1.2.12.0.0.0.0.0.0.0.0.3.72.0", "0.0.2.1.1.2.12.0.0.0.0.0.0.0.0.3.72.0"),
            Pair.of("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.111.0", "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.111.0"),
            Pair.of("0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0", "0.0.2.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0"),
            Pair.of("0.0.0.1.19.2.12.0.0.0.0.0.0.0.0.3.72.0", "0.0.2.1.19.2.12.0.0.0.0.0.0.0.0.3.72.0")
            );

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());

    public static TransactionService getTransactionService() {
        return inMemoryPersistence.getTransactionService();
    }

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = new InMemoryPersistence();
        inMemoryPersistence.initializeDatabase("com.energyict.mdc.masterdata.impl.MasterDataServiceImplTest", false, false,
                readingTypes.stream().flatMap(pair -> Stream.of(pair.getFirst(), pair.getLast())).toArray(String[]::new));
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }


    @Before
    public void setup() {
        readingTypes.forEach(pair -> {
            ReadingType readingType = inMemoryPersistence.getMeteringService().getReadingType(pair.getFirst()).get();
            if(!readingType.isRegular()){
                RegisterType registerType = inMemoryPersistence.getMasterDataService().newRegisterType(readingType, ObisCode.fromString("1.2.3.4.5.6"));
                registerType.save();
                ChannelType channelType = inMemoryPersistence.getMasterDataService()
                        .newChannelType(registerType, TimeDuration.minutes(15), inMemoryPersistence.getMeteringService()
                                .getReadingType(pair.getLast()).get());
                channelType.save();
            }
        });
    }

    @Test
    @Transactional
    public void getPossibleMultiplyReadingTypesForChannelsSecondaryElectricityTest() {
        MeasurementType measurementType = inMemoryPersistence.getMasterDataService()
                .findMeasurementTypeByReadingType(
                        inMemoryPersistence.getMeteringService().getReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get()).get();

        List<ReadingType> possibleMultiplyRegisterTypesFor = inMemoryPersistence.getMasterDataService().getOrCreatePossibleMultiplyReadingTypesFor(measurementType.getReadingType());
        assertThat(possibleMultiplyRegisterTypesFor).hasSize(1);
        assertThat(possibleMultiplyRegisterTypesFor.get(0).getMRID()).isEqualTo("0.0.2.1.1.2.12.0.0.0.0.0.0.0.0.3.72.0");
    }

    @Test
    @Transactional
    public void getPossibleMultiplyReadingTypesForRegistersSecondaryElectricityTest() {
        MeasurementType measurementType = inMemoryPersistence.getMasterDataService()
                .findMeasurementTypeByReadingType(
                        inMemoryPersistence.getMeteringService().getReadingType("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get()).get();

        List<ReadingType> possibleMultiplyRegisterTypesFor = inMemoryPersistence.getMasterDataService().getOrCreatePossibleMultiplyReadingTypesFor(measurementType.getReadingType());
        assertThat(possibleMultiplyRegisterTypesFor).hasSize(1);
        assertThat(possibleMultiplyRegisterTypesFor.get(0).getMRID()).isEqualTo("0.0.0.1.1.2.12.0.0.0.0.0.0.0.0.3.72.0");
    }

    @Test
    @Transactional
    public void getPossibleMultiplyReadingTypesForRegistersPrimaryElectricityTest() {
        MeasurementType measurementType = inMemoryPersistence.getMasterDataService()
                .findMeasurementTypeByReadingType(
                        inMemoryPersistence.getMeteringService().getReadingType("0.0.0.1.1.2.12.0.0.0.0.0.0.0.0.3.72.0").get()).get();

        List<ReadingType> possibleMultiplyRegisterTypesFor = inMemoryPersistence.getMasterDataService().getOrCreatePossibleMultiplyReadingTypesFor(measurementType.getReadingType());
        assertThat(possibleMultiplyRegisterTypesFor).isEmpty();
    }

    @Test
    @Transactional
    public void getPossibleMultiplyReadingTypesForRegistersCountersTest() {
        MeasurementType measurementType = inMemoryPersistence.getMasterDataService()
                .findMeasurementTypeByReadingType(
                        inMemoryPersistence.getMeteringService().getReadingType("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.111.0").get()).get();

        List<ReadingType> possibleMultiplyRegisterTypesFor = inMemoryPersistence.getMasterDataService().getOrCreatePossibleMultiplyReadingTypesFor(measurementType.getReadingType());
        assertThat(possibleMultiplyRegisterTypesFor).hasSize(1);
        assertThat(possibleMultiplyRegisterTypesFor).haveExactly(1, new Condition<ReadingType>() {
            @Override
            public boolean matches(ReadingType measurementType) {
                return measurementType.getMRID().equals("0.0.0.1.1.2.12.0.0.0.0.0.0.0.0.3.72.0");
            }
        });
    }
}