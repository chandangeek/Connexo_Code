package com.energyict.mdc.protocol.inbound.mbus.factory;

import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.Telegram;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body.TelegramVariableDataRecord;

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

import static java.lang.Math.min;

public enum FrameType {        // DIF	 DIF-FunctionType                	DIF-Encoding                   	VIF	 VIF-Type           	VIF-Unit	SCB	Spacing

    WEEKLY_FRAME(new int[]  {
                                0x2f,   // SPECIAL_FUNCTION_FILL_BYTE     	ENCODING_NULL                    -
                                0x2f,   // SPECIAL_FUNCTION_FILL_BYTE    	ENCODING_NULL                    -
                                0x84,   // INSTANTANEOUS_VALUE           	ENCODING_INTEGER                 6d	DATE_TIME           	DATE_TIME 					-> telegram date
                                0x04,   // INSTANTANEOUS_VALUE           	ENCODING_INTEGER                 13	VOLUME              	M3        					-> index at logbase
                                0x8d    // INSTANTANEOUS_VALUE           	ENCODING_VARIABLE_LENGTH         93	VOLUME              	M3        	f3	01			-> daily profile, 1 day
    }),

    DAILY_FRAME(new int[]   {
                                0x2f,   // SPECIAL_FUNCTION_FILL_BYTE    	ENCODING_NULL                    -
                                0x2f,   // SPECIAL_FUNCTION_FILL_BYTE    	ENCODING_NULL                    -
                                0x86,   // INSTANTANEOUS_VALUE           	ENCODING_INTEGER                 6d	EXTENTED_DATE_TIME  	EPOCH_TIME					-> telegram date&time
                                0x04,   // INSTANTANEOUS_VALUE           	ENCODING_INTEGER                 13	VOLUME              	M3        					-> index at log base
                                0x8d,   // INSTANTANEOUS_VALUE           	ENCODING_VARIABLE_LENGTH         93	VOLUME              	M3        	e2	01			-> hourly profile, 1h
                                0x12, 	// MAXIMUM_VALUE                 	ENCODING_INTEGER                 3b	VOLUME_FLOW         	M3_H      					-> max volume
                                0x22, 	// MINIMUM_VALUE                 	ENCODING_INTEGER                 3b	VOLUME_FLOW         	M3_H      					-> min volume
                                0x04, 	// INSTANTANEOUS_VALUE           	ENCODING_INTEGER                 93	VOLUME              	M3        					-> back flow
                                0x03, 	// INSTANTANEOUS_VALUE           	ENCODING_INTEGER                 fd	-  													-> ERROR flags
                                0x04, 	// INSTANTANEOUS_VALUE           	ENCODING_INTEGER                 13	VOLUME              	M3        					-> night line
                                0x8d, 	// INSTANTANEOUS_VALUE           	ENCODING_VARIABLE_LENGTH         93	VOLUME              	M3        	d2	0f			-> night profile 15 min
                                0x02, 	// INSTANTANEOUS_VALUE           	ENCODING_INTEGER                 fd	-  													-> batery lifetime
                                0x0f, 	// USER_DEFINED_CELL_ID          	ENCODING_USER_DEFINED_CELL_ID      	-  													-> cell id
                                0x89, 	// INSTANTANEOUS_VALUE           	ENCODING_BCD                     00	ENERGY_WH           	WH        					-> padding
    }),

    NRT_FRAME(new int[]     {
                                0x2f, 	// SPECIAL_FUNCTION_FILL_BYTE    	ENCODING_NULL                    -
                                0x2f, 	// SPECIAL_FUNCTION_FILL_BYTE    	ENCODING_NULL                    -
                                0x84, 	// INSTANTANEOUS_VALUE           	ENCODING_INTEGER                 6d	DATE_TIME           	DATE_TIME
                                0x12, 	// MAXIMUM_VALUE                 	ENCODING_INTEGER                 3b	VOLUME_FLOW         	M3_H
                                0x22, 	// MINIMUM_VALUE                 	ENCODING_INTEGER                 3b	VOLUME_FLOW         	M3_H
                                0x04, 	// INSTANTANEOUS_VALUE           	ENCODING_INTEGER                 93	VOLUME              	M3
                                0x03, 	// INSTANTANEOUS_VALUE           	ENCODING_INTEGER                 fd	-
    }),

    UNKNOWN(new int[] {});



    private final int[] expectedDIFs;

    FrameType(int[] expectedDIFs) {
        this.expectedDIFs = expectedDIFs.clone();
    }


    public static FrameType of(Telegram telegram) {
        try {
            int nr = telegram.getBody().getBodyPayload().getRecords().size();

            Optional<FrameType> found = Arrays.stream(values())
                    .filter(v -> v.expectedDIFs.length >= nr)
                    .filter(v -> {
                        boolean match = true;
                        for (int i = 0; i < min(nr, v.expectedDIFs.length); i++) {
                            TelegramVariableDataRecord r = telegram.getBody().getBodyPayload().getRecords().get(i);
                            int dif = Integer.parseInt(r.getDif().getFieldParts().get(0), 16);
                            if (dif != v.expectedDIFs[i]) {
                                match = false;
                                break;
                            }
                        }
                        return match;
                    })
                    .findFirst();

            return found.orElse(UNKNOWN);
        } catch (Exception ex) {
            // swallow, upper layers should log unknown cases
        }

        return UNKNOWN;
    }


}
