package com.energyict.protocolimpl.modbus.schneider.powerlogic.profile;

import com.energyict.cbo.Unit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 6/09/2016 - 16:21
 */
public class ChannelConfigMapping {

    private static List<ChannelConfiguration> channelConfigurations;

    static {
        channelConfigurations = new ArrayList<>();
        channelConfigurations.add(new ChannelConfiguration(3204, Unit.get("Wh"), DataType.INT64)); // Active Energy Delivered (Into Load)
        channelConfigurations.add(new ChannelConfiguration(3208, Unit.get("Wh"), DataType.INT64)); // Active Energy Received (Out of Load)
        channelConfigurations.add(new ChannelConfiguration(3212, Unit.get("Wh"), DataType.INT64)); // Active Energy Delivered + Received
        channelConfigurations.add(new ChannelConfiguration(3216, Unit.get("Wh"), DataType.INT64)); // Active Energy Delivered- Received
        channelConfigurations.add(new ChannelConfiguration(4196, Unit.get("Wh"), DataType.INT64)); // Active Energy Delivered Rate 1
        channelConfigurations.add(new ChannelConfiguration(4200, Unit.get("Wh"), DataType.INT64)); // Active Energy Delivered Rate 2
        channelConfigurations.add(new ChannelConfiguration(4204, Unit.get("Wh"), DataType.INT64)); // Active Energy Delivered Rate 3
        channelConfigurations.add(new ChannelConfiguration(4208, Unit.get("Wh"), DataType.INT64)); // Active Energy Delivered Rate 4
        channelConfigurations.add(new ChannelConfiguration(4212, Unit.get("Wh"), DataType.INT64)); // Active Energy Delivered Rate 5
        channelConfigurations.add(new ChannelConfiguration(4216, Unit.get("Wh"), DataType.INT64)); // Active Energy Delivered Rate 6
        channelConfigurations.add(new ChannelConfiguration(4220, Unit.get("Wh"), DataType.INT64)); // Active Energy Delivered Rate 7
        channelConfigurations.add(new ChannelConfiguration(4224, Unit.get("Wh"), DataType.INT64)); // Active Energy Delivered Rate 8
        channelConfigurations.add(new ChannelConfiguration(4228, Unit.get("Wh"), DataType.INT64)); // Active Energy Received Rate 1
        channelConfigurations.add(new ChannelConfiguration(4232, Unit.get("Wh"), DataType.INT64)); // Active Energy Received Rate 2
        channelConfigurations.add(new ChannelConfiguration(4236, Unit.get("Wh"), DataType.INT64)); // Active Energy Received Rate 3
        channelConfigurations.add(new ChannelConfiguration(4240, Unit.get("Wh"), DataType.INT64)); // Active Energy Received Rate 4
        channelConfigurations.add(new ChannelConfiguration(4244, Unit.get("Wh"), DataType.INT64)); // Active Energy Received Rate 5
        channelConfigurations.add(new ChannelConfiguration(4248, Unit.get("Wh"), DataType.INT64)); // Active Energy Received Rate 6
        channelConfigurations.add(new ChannelConfiguration(4252, Unit.get("Wh"), DataType.INT64)); // Active Energy Received Rate 7
        channelConfigurations.add(new ChannelConfiguration(4256, Unit.get("Wh"), DataType.INT64)); // Active Energy Received Rate 8
        channelConfigurations.add(new ChannelConfiguration(20286, Unit.get("Wh"), DataType.INT64)); // Active Energy Delivered (Into Load)
        channelConfigurations.add(new ChannelConfiguration(20290, Unit.get("Wh"), DataType.INT64)); // Active Energy Received (Out of Load)

        channelConfigurations.add(new ChannelConfiguration(3220, Unit.get("varh"), DataType.INT64)); // Reactive Energy Delivered
        channelConfigurations.add(new ChannelConfiguration(3224, Unit.get("varh"), DataType.INT64)); // Reactive Energy Received
        channelConfigurations.add(new ChannelConfiguration(3228, Unit.get("varh"), DataType.INT64)); // Reactive Energy Delivered + Received
        channelConfigurations.add(new ChannelConfiguration(3232, Unit.get("varh"), DataType.INT64)); // Reactive Energy Delivered - Received
        channelConfigurations.add(new ChannelConfiguration(4260, Unit.get("varh"), DataType.INT64)); // Reactive Energy Delivered Rate 1
        channelConfigurations.add(new ChannelConfiguration(4264, Unit.get("varh"), DataType.INT64)); // Reactive Energy Delivered Rate 2
        channelConfigurations.add(new ChannelConfiguration(4268, Unit.get("varh"), DataType.INT64)); // Reactive Energy Delivered Rate 3
        channelConfigurations.add(new ChannelConfiguration(4272, Unit.get("varh"), DataType.INT64)); // Reactive Energy Delivered Rate 4
        channelConfigurations.add(new ChannelConfiguration(4276, Unit.get("varh"), DataType.INT64)); // Reactive Energy Delivered Rate 5
        channelConfigurations.add(new ChannelConfiguration(4280, Unit.get("varh"), DataType.INT64)); // Reactive Energy Delivered Rate 6
        channelConfigurations.add(new ChannelConfiguration(4284, Unit.get("varh"), DataType.INT64)); // Reactive Energy Delivered Rate 7
        channelConfigurations.add(new ChannelConfiguration(4288, Unit.get("varh"), DataType.INT64)); // Reactive Energy Delivered Rate 8
        channelConfigurations.add(new ChannelConfiguration(4292, Unit.get("varh"), DataType.INT64)); // Reactive Energy Received Rate 1
        channelConfigurations.add(new ChannelConfiguration(4296, Unit.get("varh"), DataType.INT64)); // Reactive Energy Received Rate 2
        channelConfigurations.add(new ChannelConfiguration(4300, Unit.get("varh"), DataType.INT64)); // Reactive Energy Received Rate 3
        channelConfigurations.add(new ChannelConfiguration(4304, Unit.get("varh"), DataType.INT64)); // Reactive Energy Received Rate 4
        channelConfigurations.add(new ChannelConfiguration(4308, Unit.get("varh"), DataType.INT64)); // Reactive Energy Received Rate 5
        channelConfigurations.add(new ChannelConfiguration(4312, Unit.get("varh"), DataType.INT64)); // Reactive Energy Received Rate 6
        channelConfigurations.add(new ChannelConfiguration(4316, Unit.get("varh"), DataType.INT64)); // Reactive Energy Received Rate 7
        channelConfigurations.add(new ChannelConfiguration(4320, Unit.get("varh"), DataType.INT64)); // Reactive Energy Received Rate 8
        channelConfigurations.add(new ChannelConfiguration(20302, Unit.get("varh"), DataType.INT64)); // Reactive Energy Delivered
        channelConfigurations.add(new ChannelConfiguration(20306, Unit.get("varh"), DataType.INT64)); // Reactive Energy Received

        channelConfigurations.add(new ChannelConfiguration(3236, Unit.get("VAh"), DataType.INT64)); // Apparent Energy Delivered
        channelConfigurations.add(new ChannelConfiguration(3240, Unit.get("VAh"), DataType.INT64)); // Apparent Energy Received
        channelConfigurations.add(new ChannelConfiguration(3244, Unit.get("VAh"), DataType.INT64)); // Apparent Energy Delivered + Received
        channelConfigurations.add(new ChannelConfiguration(3248, Unit.get("VAh"), DataType.INT64)); // Apparent Energy Delivered - Received
        channelConfigurations.add(new ChannelConfiguration(4324, Unit.get("VAh"), DataType.INT64)); // Apparent Energy Delivered Rate 1
        channelConfigurations.add(new ChannelConfiguration(4328, Unit.get("VAh"), DataType.INT64)); // Apparent Energy Delivered Rate 2
        channelConfigurations.add(new ChannelConfiguration(4332, Unit.get("VAh"), DataType.INT64)); // Apparent Energy Delivered Rate 3
        channelConfigurations.add(new ChannelConfiguration(4336, Unit.get("VAh"), DataType.INT64)); // Apparent Energy Delivered Rate 4
        channelConfigurations.add(new ChannelConfiguration(4340, Unit.get("VAh"), DataType.INT64)); // Apparent Energy Delivered Rate 5
        channelConfigurations.add(new ChannelConfiguration(4344, Unit.get("VAh"), DataType.INT64)); // Apparent Energy Delivered Rate 6
        channelConfigurations.add(new ChannelConfiguration(4348, Unit.get("VAh"), DataType.INT64)); // Apparent Energy Delivered Rate 7
        channelConfigurations.add(new ChannelConfiguration(4352, Unit.get("VAh"), DataType.INT64)); // Apparent Energy Delivered Rate 8
        channelConfigurations.add(new ChannelConfiguration(4356, Unit.get("VAh"), DataType.INT64)); // Apparent Energy Received Rate 1
        channelConfigurations.add(new ChannelConfiguration(4360, Unit.get("VAh"), DataType.INT64)); // Apparent Energy Received Rate 2
        channelConfigurations.add(new ChannelConfiguration(4364, Unit.get("VAh"), DataType.INT64)); // Apparent Energy Received Rate 3
        channelConfigurations.add(new ChannelConfiguration(4368, Unit.get("VAh"), DataType.INT64)); // Apparent Energy Received Rate 4
        channelConfigurations.add(new ChannelConfiguration(4372, Unit.get("VAh"), DataType.INT64)); // Apparent Energy Received Rate 5
        channelConfigurations.add(new ChannelConfiguration(4376, Unit.get("VAh"), DataType.INT64)); // Apparent Energy Received Rate 6
        channelConfigurations.add(new ChannelConfiguration(4380, Unit.get("VAh"), DataType.INT64)); // Apparent Energy Received Rate 7
        channelConfigurations.add(new ChannelConfiguration(4384, Unit.get("VAh"), DataType.INT64)); // Apparent Energy Received Rate 8
        channelConfigurations.add(new ChannelConfiguration(20318, Unit.get("VAh"), DataType.INT64)); // Apparent Energy Delivered
        channelConfigurations.add(new ChannelConfiguration(20322, Unit.get("VAh"), DataType.INT64)); // Apparent Energy Received

        channelConfigurations.add(new ChannelConfiguration(3054, Unit.get("kW"), DataType.FLOAT32)); // Active Power A
        channelConfigurations.add(new ChannelConfiguration(3056, Unit.get("kW"), DataType.FLOAT32)); // Active Power B
        channelConfigurations.add(new ChannelConfiguration(3058, Unit.get("kW"), DataType.FLOAT32)); // Active Power C
        channelConfigurations.add(new ChannelConfiguration(3060, Unit.get("kW"), DataType.FLOAT32)); // Active Power Total
        channelConfigurations.add(new ChannelConfiguration(3764, Unit.get("kW"), DataType.FLOAT32)); // Last Demand
        channelConfigurations.add(new ChannelConfiguration(3766, Unit.get("kW"), DataType.FLOAT32)); // Present Demand
        channelConfigurations.add(new ChannelConfiguration(3768, Unit.get("kW"), DataType.FLOAT32)); // Predicted Demand
        channelConfigurations.add(new ChannelConfiguration(3770, Unit.get("kW"), DataType.FLOAT32)); // Peak Demand
        channelConfigurations.add(new ChannelConfiguration(3972, Unit.get("kW"), DataType.FLOAT32)); // Last Demand
        channelConfigurations.add(new ChannelConfiguration(3974, Unit.get("kW"), DataType.FLOAT32)); // Present Demand
        channelConfigurations.add(new ChannelConfiguration(3976, Unit.get("kW"), DataType.FLOAT32)); // Predicted Demand
        channelConfigurations.add(new ChannelConfiguration(3978, Unit.get("kW"), DataType.FLOAT32)); // Peak Demand
        channelConfigurations.add(new ChannelConfiguration(4020, Unit.get("kW"), DataType.FLOAT32)); // Last Demand
        channelConfigurations.add(new ChannelConfiguration(4022, Unit.get("kW"), DataType.FLOAT32)); // Present Demand
        channelConfigurations.add(new ChannelConfiguration(4024, Unit.get("kW"), DataType.FLOAT32)); // Predicted Demand
        channelConfigurations.add(new ChannelConfiguration(4026, Unit.get("kW"), DataType.FLOAT32)); // Peak Demand
        channelConfigurations.add(new ChannelConfiguration(4068, Unit.get("kW"), DataType.FLOAT32)); // Last Demand
        channelConfigurations.add(new ChannelConfiguration(4070, Unit.get("kW"), DataType.FLOAT32)); // Present Demand
        channelConfigurations.add(new ChannelConfiguration(4072, Unit.get("kW"), DataType.FLOAT32)); // Predicted Demand
        channelConfigurations.add(new ChannelConfiguration(4074, Unit.get("kW"), DataType.FLOAT32)); // Peak Demand
        channelConfigurations.add(new ChannelConfiguration(14165, Unit.get("kW"), DataType.FLOAT32)); // Pickup Setpoint
        channelConfigurations.add(new ChannelConfiguration(14169, Unit.get("kW"), DataType.FLOAT32)); // Dropout Setpoint
        channelConfigurations.add(new ChannelConfiguration(14305, Unit.get("kW"), DataType.FLOAT32)); // Pickup Setpoint
        channelConfigurations.add(new ChannelConfiguration(14309, Unit.get("kW"), DataType.FLOAT32)); // Dropout Setpoint
        channelConfigurations.add(new ChannelConfiguration(14325, Unit.get("kW"), DataType.FLOAT32)); // Pickup Setpoint
        channelConfigurations.add(new ChannelConfiguration(14329, Unit.get("kW"), DataType.FLOAT32)); // Dropout Setpoint
        channelConfigurations.add(new ChannelConfiguration(14345, Unit.get("kW"), DataType.FLOAT32)); // Pickup Setpoint
        channelConfigurations.add(new ChannelConfiguration(14349, Unit.get("kW"), DataType.FLOAT32)); // Dropout Setpoint
        channelConfigurations.add(new ChannelConfiguration(27272, Unit.get("kW"), DataType.FLOAT32)); // Min Active Power A
        channelConfigurations.add(new ChannelConfiguration(27274, Unit.get("kW"), DataType.FLOAT32)); // Min Active Power B
        channelConfigurations.add(new ChannelConfiguration(27276, Unit.get("kW"), DataType.FLOAT32)); // Min Active Power C
        channelConfigurations.add(new ChannelConfiguration(27278, Unit.get("kW"), DataType.FLOAT32)); // Min Active Power Total
        channelConfigurations.add(new ChannelConfiguration(27748, Unit.get("kW"), DataType.FLOAT32)); // Max Active Power A
        channelConfigurations.add(new ChannelConfiguration(27750, Unit.get("kW"), DataType.FLOAT32)); // Max Active Power B
        channelConfigurations.add(new ChannelConfiguration(27752, Unit.get("kW"), DataType.FLOAT32)); // Max Active Power C
        channelConfigurations.add(new ChannelConfiguration(27754, Unit.get("kW"), DataType.FLOAT32)); // Max Active Power Total

        channelConfigurations.add(new ChannelConfiguration(3062, Unit.get("kvar"), DataType.FLOAT32)); // Reactive Power A
        channelConfigurations.add(new ChannelConfiguration(3064, Unit.get("kvar"), DataType.FLOAT32)); // Reactive Power B
        channelConfigurations.add(new ChannelConfiguration(3066, Unit.get("kvar"), DataType.FLOAT32)); // Reactive Power C
        channelConfigurations.add(new ChannelConfiguration(3068, Unit.get("kvar"), DataType.FLOAT32)); // Reactive Power Total
        channelConfigurations.add(new ChannelConfiguration(3780, Unit.get("kvar"), DataType.FLOAT32)); // Last Demand
        channelConfigurations.add(new ChannelConfiguration(3782, Unit.get("kvar"), DataType.FLOAT32)); // Present Demand
        channelConfigurations.add(new ChannelConfiguration(3784, Unit.get("kvar"), DataType.FLOAT32)); // Predicted Demand
        channelConfigurations.add(new ChannelConfiguration(3786, Unit.get("kvar"), DataType.FLOAT32)); // Peak Demand
        channelConfigurations.add(new ChannelConfiguration(3988, Unit.get("kvar"), DataType.FLOAT32)); // Last Demand
        channelConfigurations.add(new ChannelConfiguration(3990, Unit.get("kvar"), DataType.FLOAT32)); // Present Demand
        channelConfigurations.add(new ChannelConfiguration(3992, Unit.get("kvar"), DataType.FLOAT32)); // Predicted Demand
        channelConfigurations.add(new ChannelConfiguration(3994, Unit.get("kvar"), DataType.FLOAT32)); // Peak Demand
        channelConfigurations.add(new ChannelConfiguration(4036, Unit.get("kvar"), DataType.FLOAT32)); // Last Demand
        channelConfigurations.add(new ChannelConfiguration(4038, Unit.get("kvar"), DataType.FLOAT32)); // Present Demand
        channelConfigurations.add(new ChannelConfiguration(4040, Unit.get("kvar"), DataType.FLOAT32)); // Predicted Demand
        channelConfigurations.add(new ChannelConfiguration(4042, Unit.get("kvar"), DataType.FLOAT32)); // Peak Demand
        channelConfigurations.add(new ChannelConfiguration(4084, Unit.get("kvar"), DataType.FLOAT32)); // Last Demand
        channelConfigurations.add(new ChannelConfiguration(4086, Unit.get("kvar"), DataType.FLOAT32)); // Present Demand
        channelConfigurations.add(new ChannelConfiguration(4088, Unit.get("kvar"), DataType.FLOAT32)); // Predicted Demand
        channelConfigurations.add(new ChannelConfiguration(4090, Unit.get("kvar"), DataType.FLOAT32)); // Peak Demand
        channelConfigurations.add(new ChannelConfiguration(14185, Unit.get("kvar"), DataType.FLOAT32)); // Pickup Setpoint
        channelConfigurations.add(new ChannelConfiguration(14189, Unit.get("kvar"), DataType.FLOAT32)); // Dropout Setpoint
        channelConfigurations.add(new ChannelConfiguration(14365, Unit.get("kvar"), DataType.FLOAT32)); // Pickup Setpoint
        channelConfigurations.add(new ChannelConfiguration(14369, Unit.get("kvar"), DataType.FLOAT32)); // Dropout Setpoint
        channelConfigurations.add(new ChannelConfiguration(14385, Unit.get("kvar"), DataType.FLOAT32)); // Pickup Setpoint
        channelConfigurations.add(new ChannelConfiguration(14389, Unit.get("kvar"), DataType.FLOAT32)); // Dropout Setpoint
        channelConfigurations.add(new ChannelConfiguration(14405, Unit.get("kvar"), DataType.FLOAT32)); // Pickup Setpoint
        channelConfigurations.add(new ChannelConfiguration(14409, Unit.get("kvar"), DataType.FLOAT32)); // Dropout Setpoint
        channelConfigurations.add(new ChannelConfiguration(27280, Unit.get("kvar"), DataType.FLOAT32)); // Min Reactive Power A
        channelConfigurations.add(new ChannelConfiguration(27282, Unit.get("kvar"), DataType.FLOAT32)); // Min Reactive Power B
        channelConfigurations.add(new ChannelConfiguration(27284, Unit.get("kvar"), DataType.FLOAT32)); // Min Reactive Power C
        channelConfigurations.add(new ChannelConfiguration(27286, Unit.get("kvar"), DataType.FLOAT32)); // Min Reactive Power Total
        channelConfigurations.add(new ChannelConfiguration(27756, Unit.get("kvar"), DataType.FLOAT32)); // Max Reactive Power A
        channelConfigurations.add(new ChannelConfiguration(27758, Unit.get("kvar"), DataType.FLOAT32)); // Max Reactive Power B
        channelConfigurations.add(new ChannelConfiguration(27760, Unit.get("kvar"), DataType.FLOAT32)); // Max Reactive Power C
        channelConfigurations.add(new ChannelConfiguration(27762, Unit.get("kvar"), DataType.FLOAT32)); // Max Reactive Power Total

        channelConfigurations.add(new ChannelConfiguration(3070, Unit.get("kVA"), DataType.FLOAT32)); // Apparent Power A
        channelConfigurations.add(new ChannelConfiguration(3072, Unit.get("kVA"), DataType.FLOAT32)); // Apparent Power B
        channelConfigurations.add(new ChannelConfiguration(3074, Unit.get("kVA"), DataType.FLOAT32)); // Apparent Power C
        channelConfigurations.add(new ChannelConfiguration(3076, Unit.get("kVA"), DataType.FLOAT32)); // Apparent Power Total
        channelConfigurations.add(new ChannelConfiguration(3796, Unit.get("kVA"), DataType.FLOAT32)); // Last Demand
        channelConfigurations.add(new ChannelConfiguration(3798, Unit.get("kVA"), DataType.FLOAT32)); // Present Demand
        channelConfigurations.add(new ChannelConfiguration(3800, Unit.get("kVA"), DataType.FLOAT32)); // Predicted Demand
        channelConfigurations.add(new ChannelConfiguration(3802, Unit.get("kVA"), DataType.FLOAT32)); // Peak Demand
        channelConfigurations.add(new ChannelConfiguration(4004, Unit.get("kVA"), DataType.FLOAT32)); // Last Demand
        channelConfigurations.add(new ChannelConfiguration(4006, Unit.get("kVA"), DataType.FLOAT32)); // Present Demand
        channelConfigurations.add(new ChannelConfiguration(4008, Unit.get("kVA"), DataType.FLOAT32)); // Predicted Demand
        channelConfigurations.add(new ChannelConfiguration(4010, Unit.get("kVA"), DataType.FLOAT32)); // Peak Demand
        channelConfigurations.add(new ChannelConfiguration(4052, Unit.get("kVA"), DataType.FLOAT32)); // Last Demand
        channelConfigurations.add(new ChannelConfiguration(4054, Unit.get("kVA"), DataType.FLOAT32)); // Present Demand
        channelConfigurations.add(new ChannelConfiguration(4056, Unit.get("kVA"), DataType.FLOAT32)); // Predicted Demand
        channelConfigurations.add(new ChannelConfiguration(4058, Unit.get("kVA"), DataType.FLOAT32)); // Peak Demand
        channelConfigurations.add(new ChannelConfiguration(4100, Unit.get("kVA"), DataType.FLOAT32)); // Last Demand
        channelConfigurations.add(new ChannelConfiguration(4102, Unit.get("kVA"), DataType.FLOAT32)); // Present Demand
        channelConfigurations.add(new ChannelConfiguration(4104, Unit.get("kVA"), DataType.FLOAT32)); // Predicted Demand
        channelConfigurations.add(new ChannelConfiguration(4106, Unit.get("kVA"), DataType.FLOAT32)); // Peak Demand
        channelConfigurations.add(new ChannelConfiguration(14205, Unit.get("kVA"), DataType.FLOAT32)); // Pickup Setpoint
        channelConfigurations.add(new ChannelConfiguration(14209, Unit.get("kVA"), DataType.FLOAT32)); // Dropout Setpoint
        channelConfigurations.add(new ChannelConfiguration(14425, Unit.get("kVA"), DataType.FLOAT32)); // Pickup Setpoint
        channelConfigurations.add(new ChannelConfiguration(14429, Unit.get("kVA"), DataType.FLOAT32)); // Dropout Setpoint
        channelConfigurations.add(new ChannelConfiguration(14445, Unit.get("kVA"), DataType.FLOAT32)); // Pickup Setpoint
        channelConfigurations.add(new ChannelConfiguration(14449, Unit.get("kVA"), DataType.FLOAT32)); // Dropout Setpoint
        channelConfigurations.add(new ChannelConfiguration(14465, Unit.get("kVA"), DataType.FLOAT32)); // Pickup Setpoint
        channelConfigurations.add(new ChannelConfiguration(14469, Unit.get("kVA"), DataType.FLOAT32)); // Dropout Setpoint
        channelConfigurations.add(new ChannelConfiguration(27288, Unit.get("kVA"), DataType.FLOAT32)); // Min Apparent Power A
        channelConfigurations.add(new ChannelConfiguration(27290, Unit.get("kVA"), DataType.FLOAT32)); // Min Apparent Power B
        channelConfigurations.add(new ChannelConfiguration(27292, Unit.get("kVA"), DataType.FLOAT32)); // Min Apparent Power C
        channelConfigurations.add(new ChannelConfiguration(27294, Unit.get("kVA"), DataType.FLOAT32)); // Min Apparent Power Total
        channelConfigurations.add(new ChannelConfiguration(27764, Unit.get("kVA"), DataType.FLOAT32)); // Max Apparent Power A
        channelConfigurations.add(new ChannelConfiguration(27766, Unit.get("kVA"), DataType.FLOAT32)); // Max Apparent Power B
        channelConfigurations.add(new ChannelConfiguration(27768, Unit.get("kVA"), DataType.FLOAT32)); // Max Apparent Power C
        channelConfigurations.add(new ChannelConfiguration(27770, Unit.get("kVA"), DataType.FLOAT32)); // Max Apparent Power Total
    }

    public ChannelConfigMapping() {
    }

    public static List<ChannelConfiguration> getChannelConfigurations() {
        return channelConfigurations;
    }

    public static ChannelConfiguration findChannelConfigurationFor(int registerId) throws IOException {
        for (ChannelConfiguration channelConfiguration : getChannelConfigurations()) {
            if (channelConfiguration.getRegisterId() == registerId) {
                return channelConfiguration;
            }
        }
        throw new IOException("Could not read out the profile data: channel configuration for register " + registerId + " is not defined in the protocol.");
    }

    public static class ChannelConfiguration {

        private final int registerId;
        private final Unit unit;
        private final DataType dataType;

        private ChannelConfiguration(int registerId, Unit unit, DataType dataType) {
            this.registerId = registerId;
            this.unit = unit;
            this.dataType = dataType;
        }

        public int getRegisterId() {
            return registerId;
        }

        public Unit getUnit() {
            return unit;
        }

        public DataType getDataType() {
            return dataType;
        }
    }

    public enum DataType {
        FLOAT32,
        INT64
    }
}