package li.cil.oc.common.blockentity;

import li.cil.oc.OpenComputers;
import li.cil.oc.Constants;
import li.cil.oc.api.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class TileEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, OpenComputers.ID());

    public static final RegistryObject<BlockEntityType<Adapter>> ADAPTER =
            BLOCK_ENTITY_TYPES.register("adapter", () -> BlockEntityType.Builder
                    .of((pos, state) -> new Adapter(pos, state),
                            Items.get(Constants.BlockName$.MODULE$.Adapter()).block())
                    .build(null));

    public static final RegistryObject<BlockEntityType<Assembler>> ASSEMBLER =
            BLOCK_ENTITY_TYPES.register("assembler", () -> BlockEntityType.Builder
                    .of((pos, state) -> new Assembler(pos, state),
                            Items.get(Constants.BlockName$.MODULE$.Assembler()).block())
                    .build(null));

    public static final RegistryObject<BlockEntityType<Cable>> CABLE =
            BLOCK_ENTITY_TYPES.register("cable", () -> BlockEntityType.Builder
                    .of((pos, state) -> new Cable(pos, state),
                            Items.get(Constants.BlockName$.MODULE$.Cable()).block())
                    .build(null));

    public static final RegistryObject<BlockEntityType<Capacitor>> CAPACITOR =
            BLOCK_ENTITY_TYPES.register("capacitor", () -> BlockEntityType.Builder
                    .of((pos, state) -> new Capacitor(pos, state),
                            Items.get(Constants.BlockName$.MODULE$.Capacitor()).block())
                    .build(null));

    public static final RegistryObject<BlockEntityType<CarpetedCapacitor>> CARPETED_CAPACITOR =
            BLOCK_ENTITY_TYPES.register("carpeted_capacitor", () -> BlockEntityType.Builder
                    .of((pos, state) -> new CarpetedCapacitor(pos, state),
                            Items.get(Constants.BlockName$.MODULE$.CarpetedCapacitor()).block())
                    .build(null));

    public static final RegistryObject<BlockEntityType<Case>> CASE =
            BLOCK_ENTITY_TYPES.register("case", () -> BlockEntityType.Builder
                    .of((pos, state) -> new Case(pos, state),
                            Items.get(Constants.BlockName$.MODULE$.CaseCreative()).block(),
                            Items.get(Constants.BlockName$.MODULE$.CaseTier1()).block(),
                            Items.get(Constants.BlockName$.MODULE$.CaseTier2()).block(),
                            Items.get(Constants.BlockName$.MODULE$.CaseTier3()).block())
                    .build(null));

    public static final RegistryObject<BlockEntityType<Charger>> CHARGER =
            BLOCK_ENTITY_TYPES.register("charger", () -> BlockEntityType.Builder
                    .of((pos, state) -> new Charger(pos, state),
                            Items.get(Constants.BlockName$.MODULE$.Charger()).block())
                    .build(null));

    public static final RegistryObject<BlockEntityType<Disassembler>> DISASSEMBLER =
            BLOCK_ENTITY_TYPES.register("disassembler", () -> BlockEntityType.Builder
                    .of((pos, state) -> new Disassembler(pos, state),
                            Items.get(Constants.BlockName$.MODULE$.Disassembler()).block())
                    .build(null));

    public static final RegistryObject<BlockEntityType<DiskDrive>> DISK_DRIVE =
            BLOCK_ENTITY_TYPES.register("disk_drive", () -> BlockEntityType.Builder
                    .of((pos, state) -> new DiskDrive(pos, state),
                            Items.get(Constants.BlockName$.MODULE$.DiskDrive()).block())
                    .build(null));

    public static final RegistryObject<BlockEntityType<Geolyzer>> GEOLYZER =
            BLOCK_ENTITY_TYPES.register("geolyzer", () -> BlockEntityType.Builder
                    .of((pos, state) -> new Geolyzer(pos, state),
                            Items.get(Constants.BlockName$.MODULE$.Geolyzer()).block())
                    .build(null));

    public static final RegistryObject<BlockEntityType<Hologram>> HOLOGRAM =
            BLOCK_ENTITY_TYPES.register("hologram", () -> BlockEntityType.Builder
                    .of((pos, state) -> new Hologram(pos, state),
                            Items.get(Constants.BlockName$.MODULE$.HologramTier1()).block(),
                            Items.get(Constants.BlockName$.MODULE$.HologramTier2()).block())
                    .build(null));

    public static final RegistryObject<BlockEntityType<Keyboard>> KEYBOARD =
            BLOCK_ENTITY_TYPES.register("keyboard", () -> BlockEntityType.Builder
                    .of((pos, state) -> new Keyboard(pos, state),
                            Items.get(Constants.BlockName$.MODULE$.Keyboard()).block())
                    .build(null));

    public static final RegistryObject<BlockEntityType<Microcontroller>> MICROCONTROLLER =
            BLOCK_ENTITY_TYPES.register("microcontroller", () -> BlockEntityType.Builder
                    .of((pos, state) -> new Microcontroller(pos, state),
                            Items.get(Constants.BlockName$.MODULE$.Microcontroller()).block())
                    .build(null));

    public static final RegistryObject<BlockEntityType<MotionSensor>> MOTION_SENSOR =
            BLOCK_ENTITY_TYPES.register("motion_sensor", () -> BlockEntityType.Builder
                    .of((pos, state) -> new MotionSensor(pos, state),
                            Items.get(Constants.BlockName$.MODULE$.MotionSensor()).block())
                    .build(null));

    public static final RegistryObject<BlockEntityType<NetSplitter>> NET_SPLITTER =
            BLOCK_ENTITY_TYPES.register("net_splitter", () -> BlockEntityType.Builder
                    .of((pos, state) -> new NetSplitter(pos, state),
                            Items.get(Constants.BlockName$.MODULE$.NetSplitter()).block())
                    .build(null));

    public static final RegistryObject<BlockEntityType<PowerConverter>> POWER_CONVERTER =
            BLOCK_ENTITY_TYPES.register("power_converter", () -> BlockEntityType.Builder
                    .of((pos, state) -> new PowerConverter(pos, state),
                            Items.get(Constants.BlockName$.MODULE$.PowerConverter()).block())
                    .build(null));

    public static final RegistryObject<BlockEntityType<PowerDistributor>> POWER_DISTRIBUTOR =
            BLOCK_ENTITY_TYPES.register("power_distributor", () -> BlockEntityType.Builder
                    .of((pos, state) -> new PowerDistributor(pos, state),
                            Items.get(Constants.BlockName$.MODULE$.PowerDistributor()).block())
                    .build(null));

    public static final RegistryObject<BlockEntityType<Print>> PRINT =
            BLOCK_ENTITY_TYPES.register("print", () -> BlockEntityType.Builder
                    .of((pos, state) -> new Print(pos, state),
                            Items.get(Constants.BlockName$.MODULE$.Print()).block())
                    .build(null));

    public static final RegistryObject<BlockEntityType<Printer>> PRINTER =
            BLOCK_ENTITY_TYPES.register("printer", () -> BlockEntityType.Builder
                    .of((pos, state) -> new Printer(pos, state),
                            Items.get(Constants.BlockName$.MODULE$.Printer()).block())
                    .build(null));

    public static final RegistryObject<BlockEntityType<Rack>> RACK =
            BLOCK_ENTITY_TYPES.register("rack", () -> BlockEntityType.Builder
                    .of((pos, state) -> new Rack(pos, state),
                            Items.get(Constants.BlockName$.MODULE$.Rack()).block())
                    .build(null));

    public static final RegistryObject<BlockEntityType<Raid>> RAID =
            BLOCK_ENTITY_TYPES.register("raid", () -> BlockEntityType.Builder
                    .of((pos, state) -> new Raid(pos, state),
                            Items.get(Constants.BlockName$.MODULE$.Raid()).block())
                    .build(null));

    public static final RegistryObject<BlockEntityType<Redstone>> REDSTONE_IO =
            BLOCK_ENTITY_TYPES.register("redstone_io", () -> BlockEntityType.Builder
                    .of((pos, state) -> new Redstone(pos, state),
                            Items.get(Constants.BlockName$.MODULE$.Redstone()).block())
                    .build(null));

    public static final RegistryObject<BlockEntityType<Relay>> RELAY =
            BLOCK_ENTITY_TYPES.register("relay", () -> BlockEntityType.Builder
                    .of((pos, state) -> new Relay(pos, state),
                            Items.get(Constants.BlockName$.MODULE$.Relay()).block())
                    .build(null));

    public static final RegistryObject<BlockEntityType<RobotProxy>> ROBOT =
            BLOCK_ENTITY_TYPES.register("robot", () -> BlockEntityType.Builder
                    .of((pos, state) -> new RobotProxy(pos, state),
                            Items.get(Constants.BlockName$.MODULE$.Robot()).block())
                    .build(null));

    public static final RegistryObject<BlockEntityType<Screen>> SCREEN =
            BLOCK_ENTITY_TYPES.register("screen", () -> BlockEntityType.Builder
                    .of((pos, state) -> new Screen(pos, state),
                            Items.get(Constants.BlockName$.MODULE$.ScreenTier1()).block(),
                            Items.get(Constants.BlockName$.MODULE$.ScreenTier2()).block(),
                            Items.get(Constants.BlockName$.MODULE$.ScreenTier3()).block())
                    .build(null));

    public static final RegistryObject<BlockEntityType<Transposer>> TRANSPOSER =
            BLOCK_ENTITY_TYPES.register("transposer", () -> BlockEntityType.Builder
                    .of((pos, state) -> new Transposer(pos, state),
                            Items.get(Constants.BlockName$.MODULE$.Transposer()).block())
                    .build(null));

    public static final RegistryObject<BlockEntityType<Waypoint>> WAYPOINT =
            BLOCK_ENTITY_TYPES.register("waypoint", () -> BlockEntityType.Builder
                    .of((pos, state) -> new Waypoint(pos, state),
                            Items.get(Constants.BlockName$.MODULE$.Waypoint()).block())
                    .build(null));

    public static void init(IEventBus bus) {
        BLOCK_ENTITY_TYPES.register(bus);
    }

    private TileEntityTypes() {
        throw new Error();
    }
}