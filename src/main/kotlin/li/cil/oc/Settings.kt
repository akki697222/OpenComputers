package li.cil.oc

import com.mojang.authlib.GameProfile
import com.typesafe.config.*
import li.cil.oc.api.internal.TextBuffer.ColorDepth
import li.cil.oc.common.Tier
import li.cil.oc.server.component.DebugCard.AccessContext
import li.cil.oc.util.InternetFilteringRule
import net.minecraftforge.fml.loading.FMLPaths
import org.apache.commons.codec.binary.Hex
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.apache.maven.artifact.versioning.VersionRange
import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.security.SecureRandom
import java.util.UUID
import kotlin.math.max

class Settings(val config: Config) {
    // ----------------------------------------------------------------------- //
    // client
    val screenTextFadeStartDistance = config.getDouble("client.screenTextFadeStartDistance")
    val maxScreenTextRenderDistance = config.getDouble("client.maxScreenTextRenderDistance")
    val textLinearFiltering = config.getBoolean("client.textLinearFiltering")
    val textAntiAlias = config.getBoolean("client.textAntiAlias")
    val robotLabels = config.getBoolean("client.robotLabels")
    val soundVolume = config.getDouble("client.soundVolume").toFloat().coerceIn(0f, 2f)
    val fontCharScale = config.getDouble("client.fontCharScale").coerceIn(0.5, 2.0)
    val hologramFadeStartDistance = max(config.getDouble("client.hologramFadeStartDistance"), 0.0)
    val hologramRenderDistance = max(config.getDouble("client.hologramRenderDistance"), 0.0)
    val hologramFlickerFrequency = max(config.getDouble("client.hologramFlickerFrequency"), 0.0)
    val monochromeColor = Integer.decode(config.getString("client.monochromeColor"))
    val fontRenderer = config.getString("client.fontRenderer")
    val beepSampleRate = config.getInt("client.beepSampleRate")
    val beepAmplitude = config.getInt("client.beepVolume").coerceIn(0, Byte.MAX_VALUE.toInt())
    val beepRadius = config.getDouble("client.beepRadius").toFloat().coerceIn(1f, 32f)
    val nanomachineHudPos: Pair<Double, Double> = config.getDoubleList("client.nanomachineHudPos").let {
        if (it.size == 2) it[0].toDouble() to it[1].toDouble()
        else {
            OpenComputers.log.warn("Bad number of HUD coordiantes, ignoring.")
            -1.0 to -1.0
        }
    }
    val enableNanomachinePfx = config.getBoolean("client.enableNanomachinePfx")
    val transposerFluidTransferRate = config.getInt("misc.transposerFluidTransferRate")

    // ----------------------------------------------------------------------- //
    // computer
    val threads = max(config.getInt("computer.threads"), 1)
    val timeout = max(config.getDouble("computer.timeout"), 0.0)
    val startupDelay = max(config.getDouble("computer.startupDelay"), 0.05)
    val eepromSize = max(config.getInt("computer.eepromSize"), 0)
    val eepromDataSize = max(config.getInt("computer.eepromDataSize"), 0)
    val cpuComponentSupport = config.getIntList("computer.cpuComponentCount").let {
        if (it.size == 4) intArrayOf(it[0].toInt(), it[1].toInt(), it[2].toInt(), it[3].toInt())
        else {
            OpenComputers.log.warn("Bad number of CPU component counts, ignoring.")
            intArrayOf(8, 12, 16, 1024)
        }
    }
    val callBudgets = config.getDoubleList("computer.callBudgets").let {
        if (it.size == 3) doubleArrayOf(it[0].toDouble(), it[1].toDouble(), it[2].toDouble())
        else {
            OpenComputers.log.warn("Bad number of call budgets, ignoring.")
            doubleArrayOf(0.5, 1.0, 1.5)
        }
    }
    val canComputersBeOwned = config.getBoolean("computer.canComputersBeOwned")
    val maxUsers = max(config.getInt("computer.maxUsers"), 0)
    val maxUsernameLength = max(config.getInt("computer.maxUsernameLength"), 0)
    val eraseTmpOnReboot = config.getBoolean("computer.eraseTmpOnReboot")
    val executionDelay = max(config.getInt("computer.executionDelay"), 0)

    // computer.lua
    val allowBytecode = config.getBoolean("computer.lua.allowBytecode")
    val allowGC = config.getBoolean("computer.lua.allowGC")
    val enableLua53 = config.getBoolean("computer.lua.enableLua53")
    val defaultLua53 = config.getBoolean("computer.lua.defaultLua53")
    val enableLua54 = config.getBoolean("computer.lua.enableLua54")
    val ramSizes = config.getIntList("computer.lua.ramSizes").let {
        if (it.size == 6) intArrayOf(it[0].toInt(), it[1].toInt(), it[2].toInt(), it[3].toInt(), it[4].toInt(), it[5].toInt())
        else {
            OpenComputers.log.warn("Bad number of RAM sizes, ignoring.")
            intArrayOf(192, 256, 384, 512, 768, 1024)
        }
    }
    val ramScaleFor64Bit = max(config.getDouble("computer.lua.ramScaleFor64Bit"), 1.0)
    val maxTotalRam = max(config.getInt("computer.lua.maxTotalRam"), 0)

    // ----------------------------------------------------------------------- //
    // robot
    val allowActivateBlocks = config.getBoolean("robot.allowActivateBlocks")
    val allowUseItemsWithDuration = config.getBoolean("robot.allowUseItemsWithDuration")
    val canAttackPlayers = config.getBoolean("robot.canAttackPlayers")
    val limitFlightHeight = max(config.getInt("robot.limitFlightHeight"), -1)
    val screwCobwebs = config.getBoolean("robot.notAfraidOfSpiders")
    val swingRange = config.getDouble("robot.swingRange")
    val useAndPlaceRange = config.getDouble("robot.useAndPlaceRange")
    val itemDamageRate = config.getDouble("robot.itemDamageRate").coerceIn(0.0, 1.0)
    val nameFormat = config.getString("robot.nameFormat")
    val uuidFormat = config.getString("robot.uuidFormat")
    val upgradeFlightHeight = config.getIntList("robot.upgradeFlightHeight").let {
        if (it.size == 2) intArrayOf(it[0].toInt(), it[1].toInt())
        else {
            OpenComputers.log.warn("Bad number of hover flight height counts, ignoring.")
            intArrayOf(64, 256)
        }
    }

    // robot.xp
    val baseXpToLevel = max(config.getDouble("robot.xp.baseValue"), 0.0)
    val constantXpGrowth = max(config.getDouble("robot.xp.constantGrowth"), 1.0)
    val exponentialXpGrowth = max(config.getDouble("robot.xp.exponentialGrowth"), 1.0)
    val robotActionXp = max(config.getDouble("robot.xp.actionXp"), 0.0)
    val robotExhaustionXpRate = max(config.getDouble("robot.xp.exhaustionXpRate"), 0.0)
    val robotOreXpRate = max(config.getDouble("robot.xp.oreXpRate"), 0.0)
    val bufferPerLevel = max(config.getDouble("robot.xp.bufferPerLevel"), 0.0)
    val toolEfficiencyPerLevel = max(config.getDouble("robot.xp.toolEfficiencyPerLevel"), 0.0)
    val harvestSpeedBoostPerLevel = max(config.getDouble("robot.xp.harvestSpeedBoostPerLevel"), 0.0)

    // ----------------------------------------------------------------------- //
    // robot.delays

    // Note: all delays are reduced by one tick to account for the tick they are
    // performed in (since all actions are delegated to the server thread).
    val turnDelay = max(config.getDouble("robot.delays.turn") - 0.06, 0.05)
    val moveDelay = max(config.getDouble("robot.delays.move") - 0.06, 0.05)
    val swingDelay = max(config.getDouble("robot.delays.swing") - 0.06, 0.0)
    val useDelay = max(config.getDouble("robot.delays.use") - 0.06, 0.0)
    val placeDelay = max(config.getDouble("robot.delays.place") - 0.06, 0.0)
    val dropDelay = max(config.getDouble("robot.delays.drop") - 0.06, 0.0)
    val suckDelay = max(config.getDouble("robot.delays.suck") - 0.06, 0.0)
    val harvestRatio = max(config.getDouble("robot.delays.harvestRatio"), 0.0)

    // ----------------------------------------------------------------------- //
    // power
    val ignorePower = config.getBoolean("power.ignorePower")
    val tickFrequency = max(config.getDouble("power.tickFrequency"), 1.0)
    val chargeRateExternal = config.getDouble("power.chargerChargeRate")
    val chargeRateTablet = config.getDouble("power.chargerChargeRateTablet")
    val generatorEfficiency = config.getDouble("power.generatorEfficiency")
    val solarGeneratorEfficiency = config.getDouble("power.solarGeneratorEfficiency")
    val assemblerTickAmount = max(config.getDouble("power.assemblerTickAmount"), 1.0)
    val disassemblerTickAmount = max(config.getDouble("power.disassemblerTickAmount"), 1.0)
    val printerTickAmount = max(config.getDouble("power.printerTickAmount"), 1.0)
    val powerModBlacklist: List<String> = config.getStringList("power.modBlacklist")

    // power.carpetedCapacitors
    val sheepPower = max(config.getDouble("power.carpetedCapacitors.sheepPower"), 0.0)
    val ocelotPower = max(config.getDouble("power.carpetedCapacitors.ocelotPower"), 0.0)
    val carpetDamageChance = config.getDouble("power.carpetedCapacitors.damageChance").coerceIn(0.0, 1.0)

    // power.buffer
    val bufferCapacitor = max(config.getDouble("power.buffer.capacitor"), 0.0)
    val bufferCapacitorAdjacencyBonus = max(config.getDouble("power.buffer.capacitorAdjacencyBonus"), 0.0)
    val bufferComputer = max(config.getDouble("power.buffer.computer"), 0.0)
    val bufferRobot = max(config.getDouble("power.buffer.robot"), 0.0)
    val bufferConverter = max(config.getDouble("power.buffer.converter"), 0.0)
    val bufferDistributor = max(config.getDouble("power.buffer.distributor"), 0.0)
    val bufferCapacitorUpgrades = config.getDoubleList("power.buffer.batteryUpgrades").let {
        if (it.size == 3) doubleArrayOf(it[0].toDouble(), it[1].toDouble(), it[2].toDouble())
        else {
            OpenComputers.log.warn("Bad number of battery upgrade buffer sizes, ignoring.")
            doubleArrayOf(10000.0, 15000.0, 20000.0)
        }
    }
    val bufferTablet = max(config.getDouble("power.buffer.tablet"), 0.0)
    val bufferAccessPoint = max(config.getDouble("power.buffer.accessPoint"), 0.0)
    val bufferDrone = max(config.getDouble("power.buffer.drone"), 0.0)
    val bufferMicrocontroller = max(config.getDouble("power.buffer.mcu"), 0.0)
    val bufferHoverBoots = max(config.getDouble("power.buffer.hoverBoots"), 1.0)
    val bufferNanomachines = max(config.getDouble("power.buffer.nanomachines"), 0.0)

    // power.cost
    val computerCost = max(config.getDouble("power.cost.computer"), 0.0)
    val microcontrollerCost = max(config.getDouble("power.cost.microcontroller"), 0.0)
    val robotCost = max(config.getDouble("power.cost.robot"), 0.0)
    val droneCost = max(config.getDouble("power.cost.drone"), 0.0)
    val sleepCostFactor = max(config.getDouble("power.cost.sleepFactor"), 0.0)
    val screenCost = max(config.getDouble("power.cost.screen"), 0.0)
    val hologramCost = max(config.getDouble("power.cost.hologram"), 0.0)
    val hddReadCost = max(config.getDouble("power.cost.hddRead"), 0.0) / 1024.0
    val hddWriteCost = max(config.getDouble("power.cost.hddWrite"), 0.0) / 1024.0
    val gpuSetCost = max(config.getDouble("power.cost.gpuSet"), 0.0) / Settings.basicScreenPixels
    val gpuFillCost = max(config.getDouble("power.cost.gpuFill"), 0.0) / Settings.basicScreenPixels
    val gpuClearCost = max(config.getDouble("power.cost.gpuClear"), 0.0) / Settings.basicScreenPixels
    val gpuCopyCost = max(config.getDouble("power.cost.gpuCopy"), 0.0) / Settings.basicScreenPixels
    val robotTurnCost = max(config.getDouble("power.cost.robotTurn"), 0.0)
    val robotMoveCost = max(config.getDouble("power.cost.robotMove"), 0.0)
    val robotExhaustionCost = max(config.getDouble("power.cost.robotExhaustion"), 0.0)
    val wirelessCostPerRange = config.getDoubleList("power.cost.wirelessCostPerRange").let {
        if (it.size == 2) doubleArrayOf(max(it[0].toDouble(), 0.0), max(it[1].toDouble(), 0.0))
        else {
            OpenComputers.log.warn("Bad number of wireless card energy costs, ignoring.")
            doubleArrayOf(0.05, 0.05)
        }
    }
    val abstractBusPacketCost = max(config.getDouble("power.cost.abstractBusPacket"), 0.0)
    val geolyzerScanCost = max(config.getDouble("power.cost.geolyzerScan"), 0.0)
    val robotBaseCost = max(config.getDouble("power.cost.robotAssemblyBase"), 0.0)
    val robotComplexityCost = max(config.getDouble("power.cost.robotAssemblyComplexity"), 0.0)
    val microcontrollerBaseCost = max(config.getDouble("power.cost.microcontrollerAssemblyBase"), 0.0)
    val microcontrollerComplexityCost = max(config.getDouble("power.cost.microcontrollerAssemblyComplexity"), 0.0)
    val tabletBaseCost = max(config.getDouble("power.cost.tabletAssemblyBase"), 0.0)
    val tabletComplexityCost = max(config.getDouble("power.cost.tabletAssemblyComplexity"), 0.0)
    val droneBaseCost = max(config.getDouble("power.cost.droneAssemblyBase"), 0.0)
    val droneComplexityCost = max(config.getDouble("power.cost.droneAssemblyComplexity"), 0.0)
    val disassemblerItemCost = max(config.getDouble("power.cost.disassemblerPerItem"), 0.0)
    val chunkloaderCost = max(config.getDouble("power.cost.chunkloaderCost"), 0.0)
    val pistonCost = max(config.getDouble("power.cost.pistonPush"), 0.0)
    val eepromWriteCost = max(config.getDouble("power.cost.eepromWrite"), 0.0)
    val printCost = max(config.getDouble("power.cost.printerModel"), 0.0)
    val hoverBootJump = max(config.getDouble("power.cost.hoverBootJump"), 0.0)
    val hoverBootAbsorb = max(config.getDouble("power.cost.hoverBootAbsorb"), 0.0)
    val hoverBootMove = max(config.getDouble("power.cost.hoverBootMove"), 0.0)
    val dataCardTrivial = max(config.getDouble("power.cost.dataCardTrivial"), 0.0)
    val dataCardTrivialByte = max(config.getDouble("power.cost.dataCardTrivialByte"), 0.0)
    val dataCardSimple = max(config.getDouble("power.cost.dataCardSimple"), 0.0)
    val dataCardSimpleByte = max(config.getDouble("power.cost.dataCardSimpleByte"), 0.0)
    val dataCardComplex = max(config.getDouble("power.cost.dataCardComplex"), 0.0)
    val dataCardComplexByte = max(config.getDouble("power.cost.dataCardComplexByte"), 0.0)
    val dataCardAsymmetric = max(config.getDouble("power.cost.dataCardAsymmetric"), 0.0)
    val transposerCost = max(config.getDouble("power.cost.transposer"), 0.0)
    val nanomachineCost = max(config.getDouble("power.cost.nanomachineInput"), 0.0)
    val nanomachineReconfigureCost = max(config.getDouble("power.cost.nanomachinesReconfigure"), 0.0)
    val mfuCost = max(config.getDouble("power.cost.mfuRelay"), 0.0)

    // power.rate
    val accessPointRate = max(config.getDouble("power.rate.accessPoint"), 0.0)
    val assemblerRate = max(config.getDouble("power.rate.assembler"), 0.0)
    val caseRate = (config.getDoubleList("power.rate.case").let {
        if (it.size == 3) doubleArrayOf(it[0].toDouble(), it[1].toDouble(), it[2].toDouble())
        else {
            OpenComputers.log.warn("Bad number of computer case conversion rates, ignoring.")
            doubleArrayOf(5.0, 10.0, 20.0)
        }
    }) + doubleArrayOf(9001.0)
    // Creative case.
    val chargerRate = max(config.getDouble("power.rate.charger"), 0.0)
    val disassemblerRate = max(config.getDouble("power.rate.disassembler"), 0.0)
    val powerConverterRate = max(config.getDouble("power.rate.powerConverter"), 0.0)
    val serverRackRate = max(config.getDouble("power.rate.serverRack"), 0.0)

    // power.value
    private val valueAppliedEnergistics2 = config.getDouble("power.value.AppliedEnergistics2")
    private val valueFactorization = config.getDouble("power.value.Factorization")
    private val valueGalacticraft = config.getDouble("power.value.Galacticraft")
    private val valueIndustrialCraft2 = config.getDouble("power.value.IndustrialCraft2")
    private val valueMekanism = config.getDouble("power.value.Mekanism")
    private val valuePowerAdvantage = config.getDouble("power.value.PowerAdvantage")
    private val valueRedstoneFlux = config.getDouble("power.value.RedstoneFlux")
    private val valueRotaryCraft = config.getDouble("power.value.RotaryCraft") / 11256.0
    private val valueForgeEnergy = if (config.hasPath("power.value.ForgeEnergy")) config.getDouble("power.value.ForgeEnergy") else valueRedstoneFlux

    private val valueInternal = 1000.0

    val ratioAppliedEnergistics2 = valueAppliedEnergistics2 / valueInternal
    val ratioFactorization = valueFactorization / valueInternal
    val ratioGalacticraft = valueGalacticraft / valueInternal
    val ratioIndustrialCraft2 = valueIndustrialCraft2 / valueInternal
    val ratioMekanism = valueMekanism / valueInternal
    val ratioPowerAdvantage = valuePowerAdvantage / valueInternal
    val ratioRedstoneFlux = valueRedstoneFlux / valueInternal
    val ratioRotaryCraft = valueRotaryCraft / valueInternal
    val ratioForgeEnergy = valueForgeEnergy / valueInternal

    // ----------------------------------------------------------------------- //
    // filesystem
    val fileCost = max(config.getInt("filesystem.fileCost"), 0)
    val bufferChanges = config.getBoolean("filesystem.bufferChanges")
    val hddSizes = config.getIntList("filesystem.hddSizes").let {
        if (it.size == 3) intArrayOf(it[0].toInt(), it[1].toInt(), it[2].toInt())
        else {
            OpenComputers.log.warn("Bad number of HDD sizes, ignoring.")
            intArrayOf(1024, 2048, 4096)
        }
    }
    val hddPlatterCounts = config.getIntList("filesystem.hddPlatterCounts").let {
        if (it.size == 3) intArrayOf(it[0].toInt(), it[1].toInt(), it[2].toInt())
        else {
            OpenComputers.log.warn("Bad number of HDD platter counts, ignoring.")
            intArrayOf(2, 4, 6)
        }
    }
    val floppySize = max(config.getInt("filesystem.floppySize"), 0)
    val tmpSize = max(config.getInt("filesystem.tmpSize"), 0)
    val maxHandles = max(config.getInt("filesystem.maxHandles"), 0)
    val maxReadBuffer = max(config.getInt("filesystem.maxReadBuffer"), 0)
    val sectorSeekThreshold = config.getInt("filesystem.sectorSeekThreshold")
    val sectorSeekTime = config.getDouble("filesystem.sectorSeekTime")

    // ----------------------------------------------------------------------- //
    // internet
    val httpEnabled = config.getBoolean("internet.enableHttp")
    val httpHeadersEnabled = config.getBoolean("internet.enableHttpHeaders")
    val tcpEnabled = config.getBoolean("internet.enableTcp")
    val internetFilteringRules: Array<InternetFilteringRule> = config.getStringList("internet.filteringRules")
            .filter { it != "removeme" }
            .map { InternetFilteringRule(it) }
            .toTypedArray()
    val internetFilteringRulesObserved = !config.getStringList("internet.filteringRules").contains("removeme")
    val httpTimeout = max(config.getInt("internet.requestTimeout"), 0) * 1000
    val maxConnections = max(config.getInt("internet.maxTcpConnections"), 0)
    val internetThreads = max(config.getInt("internet.threads"), 1)

    // ----------------------------------------------------------------------- //
    // switch
    val switchDefaultMaxQueueSize = max(config.getInt("switch.defaultMaxQueueSize"), 1)
    val switchQueueSizeUpgrade = max(config.getInt("switch.queueSizeUpgrade"), 0)
    val switchDefaultRelayDelay = max(config.getInt("switch.defaultRelayDelay"), 1)
    val switchRelayDelayUpgrade = max(config.getDouble("switch.relayDelayUpgrade"), 0.0)
    val switchDefaultRelayAmount = max(config.getInt("switch.defaultRelayAmount"), 1)
    val switchRelayAmountUpgrade = max(config.getInt("switch.relayAmountUpgrade"), 0)

    // ----------------------------------------------------------------------- //
    // hologram
    val hologramMaxScaleByTier = config.getDoubleList("hologram.maxScale").let {
        if (it.size == 2) doubleArrayOf(max(it[0].toDouble(), 1.0), max(it[1].toDouble(), 1.0))
        else {
            OpenComputers.log.warn("Bad number of hologram max scales, ignoring.")
            doubleArrayOf(3.0, 4.0)
        }
    }
    val hologramMaxTranslationByTier = config.getDoubleList("hologram.maxTranslation").let {
        if (it.size == 2) doubleArrayOf(max(it[0].toDouble(), 0.0), max(it[1].toDouble(), 0.0))
        else {
            OpenComputers.log.warn("Bad number of hologram max translations, ignoring.")
            doubleArrayOf(0.25, 0.5)
        }
    }
    val hologramSetRawDelay = max(config.getDouble("hologram.setRawDelay"), 0.0)
    val hologramLight = config.getBoolean("hologram.emitLight")

    // ----------------------------------------------------------------------- //
    // misc
    val maxScreenWidth = max(config.getInt("misc.maxScreenWidth"), 1)
    val maxScreenHeight = max(config.getInt("misc.maxScreenHeight"), 1)
    val inputUsername = config.getBoolean("misc.inputUsername")
    val initialNetworkPacketTTL = max(config.getInt("misc.initialNetworkPacketTTL"), 5)
    val maxNetworkPacketSize = max(config.getInt("misc.maxNetworkPacketSize"), 0)
    // Need at least 4 for nanomachine protocol. Because I can!
    val maxNetworkPacketParts = max(config.getInt("misc.maxNetworkPacketParts"), 4)
    val maxOpenPorts = config.getIntList("misc.maxOpenPorts").let {
        if (it.size == 3) intArrayOf(max(it[0].toInt(), 0), max(it[1].toInt(), 0), max(it[2].toInt(), 0))
        else {
            OpenComputers.log.warn("Bad number of max open ports, ignoring.")
            intArrayOf(16, 1, 16)
        }
    }
    val maxWirelessRange = config.getDoubleList("misc.maxWirelessRange").let {
        if (it.size == 2) doubleArrayOf(max(it[0].toDouble(), 0.0), max(it[1].toDouble(), 0.0))
        else {
            OpenComputers.log.warn("Bad number of wireless card max ranges, ignoring.")
            doubleArrayOf(16.0, 400.0)
        }
    }
    val rTreeMaxEntries = 10
    val terminalsPerServer = 4
    val updateCheck = config.getBoolean("misc.updateCheck")
    val lootProbability = config.getInt("misc.lootProbability")
    val lootRecrafting = config.getBoolean("misc.lootRecrafting")
    val geolyzerRange = config.getInt("misc.geolyzerRange")
    val geolyzerNoise = max(config.getDouble("misc.geolyzerNoise").toFloat(), 0f)
    val disassembleAllTheThings = config.getBoolean("misc.disassembleAllTheThings")
    val disassemblerBreakChance = config.getDouble("misc.disassemblerBreakChance").coerceIn(0.0, 1.0)
    val disassemblerInputBlacklist: List<String> = config.getStringList("misc.disassemblerInputBlacklist")
    val hideOwnPet = config.getBoolean("misc.hideOwnSpecial")
    val allowItemStackInspection = config.getBoolean("misc.allowItemStackInspection")
    val databaseEntriesPerTier = intArrayOf(9, 25, 81)
    // Not configurable because of GUI design.
    val presentChance = config.getDouble("misc.presentChance").coerceIn(0.0, 1.0)
    val assemblerBlacklist: List<String> = config.getStringList("misc.assemblerBlacklist")
    val threadPriority = config.getInt("misc.threadPriority")
    val giveManualToNewPlayers = config.getBoolean("misc.giveManualToNewPlayers")
    val dataCardSoftLimit = max(config.getInt("misc.dataCardSoftLimit"), 0)
    val dataCardHardLimit = max(config.getInt("misc.dataCardHardLimit"), 0)
    val dataCardTimeout = max(config.getDouble("misc.dataCardTimeout"), 0.0)
    val serverRackSwitchTier = (config.getInt("misc.serverRackSwitchTier") - 1).coerceIn(Tier.None, Tier.Three)
    val redstoneDelay = max(config.getDouble("misc.redstoneDelay"), 0.0)
    val tradingRange = max(config.getDouble("misc.tradingRange"), 0.0)
    val mfuRange = config.getInt("misc.mfuRange").coerceIn(0, 128)

    // ----------------------------------------------------------------------- //
    // nanomachines
    val nanomachineTriggerQuota = max(config.getDouble("nanomachines.triggerQuota"), 0.0)
    val nanomachineConnectorQuota = max(config.getDouble("nanomachines.connectorQuota"), 0.0)
    val nanomachineMaxInputs = max(config.getInt("nanomachines.maxInputs"), 1)
    val nanomachineMaxOutputs = max(config.getInt("nanomachines.maxOutputs"), 1)
    val nanomachinesSafeInputsActive = max(config.getInt("nanomachines.safeInputsActive"), 0)
    val nanomachinesMaxInputsActive = max(config.getInt("nanomachines.maxInputsActive"), 0)
    val nanomachinesCommandDelay = max(config.getDouble("nanomachines.commandDelay"), 0.0)
    val nanomachinesCommandRange = max(config.getDouble("nanomachines.commandRange"), 0.0)
    val nanomachineMagnetRange = max(config.getDouble("nanomachines.magnetRange"), 0.0)
    val nanomachineDisintegrationRange = max(config.getInt("nanomachines.disintegrationRange"), 0)
    val nanomachinePotionWhitelist: List<Any> = config.getAnyRefList("nanomachines.potionWhitelist")
    val nanomachinesHungryDamage = max(config.getDouble("nanomachines.hungryDamage").toFloat(), 0f)
    val nanomachinesHungryEnergyRestored = max(config.getDouble("nanomachines.hungryEnergyRestored"), 0.0)

    // ----------------------------------------------------------------------- //
    // printer
    val maxPrintComplexity = config.getInt("printer.maxShapes")
    val printRecycleRate = config.getDouble("printer.recycleRate")
    val chameliumEdible = config.getBoolean("printer.chameliumEdible")
    val maxPrintLightLevel = config.getInt("printer.maxBaseLightLevel").coerceIn(0, 15)
    val printCustomRedstone = max(config.getInt("printer.customRedstoneCost"), 0)
    val printMaterialValue = max(config.getInt("printer.materialValue"), 0)
    val printInkValue = max(config.getInt("printer.inkValue"), 0)
    val printsHaveOpacity = config.getBoolean("printer.printsHaveOpacity")
    val noclipMultiplier = max(config.getDouble("printer.noclipMultiplier"), 0.0)

    // chunkloader
    val chunkloadDimensionBlacklist = Settings.getIntList(config, "chunkloader.dimBlacklist")
    val chunkloadDimensionWhitelist = Settings.getIntList(config, "chunkloader.dimWhitelist")

    // ----------------------------------------------------------------------- //
    // integration
    val modBlacklist: List<String> = config.getStringList("integration.modBlacklist")
    val peripheralBlacklist: List<String> = config.getStringList("integration.peripheralBlacklist")
    val fakePlayerUuid = config.getString("integration.fakePlayerUuid")
    val fakePlayerName = config.getString("integration.fakePlayerName")
    val fakePlayerProfile = GameProfile(UUID.fromString(fakePlayerUuid), fakePlayerName)

    // integration.vanilla
    val enableInventoryDriver = config.getBoolean("integration.vanilla.enableInventoryDriver")
    val enableTankDriver = config.getBoolean("integration.vanilla.enableTankDriver")
    val enableCommandBlockDriver = config.getBoolean("integration.vanilla.enableCommandBlockDriver")
    val allowItemStackNBTTags = config.getBoolean("integration.vanilla.allowItemStackNBTTags")

    // integration.buildcraft
    val costProgrammingTable = max(config.getDouble("integration.buildcraft.programmingTableCost"), 0.0)

    // ----------------------------------------------------------------------- //
    // debug
    val logLuaCallbackErrors = config.getBoolean("debug.logCallbackErrors")
    val forceLuaJ = config.getBoolean("debug.forceLuaJ")
    val allowUserdata = !config.getBoolean("debug.disableUserdata")
    val allowPersistence = !config.getBoolean("debug.disablePersistence")
    val limitMemory = !config.getBoolean("debug.disableMemoryLimit")
    val forceCaseInsensitive = config.getBoolean("debug.forceCaseInsensitiveFS")
    val logFullLibLoadErrors = config.getBoolean("debug.logFullNativeLibLoadErrors")
    val forceNativeLibPlatform = config.getString("debug.forceNativeLibPlatform")
    val forceNativeLibPathFirst = config.getString("debug.forceNativeLibPathFirst")
    val logOpenGLErrors = config.getBoolean("debug.logOpenGLErrors")
    val logHexFontErrors = config.getBoolean("debug.logHexFontErrors")
    val alwaysTryNative = config.getBoolean("debug.alwaysTryNative")
    val debugPersistence = config.getBoolean("debug.verbosePersistenceErrors")
    val nativeInTmpDir = config.getBoolean("debug.nativeInTmpDir")
    val periodicallyForceLightUpdate = config.getBoolean("debug.periodicallyForceLightUpdate")
    val insertIdsInConverters = config.getBoolean("debug.insertIdsInConverters")

    val debugCardAccess: Settings.DebugCardAccess = config.getValue("debug.debugCardAccess").unwrapped().let {
        when (it) {
            "true", "allow", true -> Settings.DebugCardAccess.Allowed
            "false", "deny", false -> Settings.DebugCardAccess.Forbidden
            "whitelist" -> {
                val wlFile = FMLPaths.CONFIGDIR.get().resolve(Paths.get("opencomputers", "debug_card_whitelist.txt")).toFile()
                Settings.DebugCardAccess.Whitelist(wlFile)
            }
            else -> {
                OpenComputers.log.warn("Unknown debug card access type, falling back to `deny`. Allowed values: `allow`, `deny`, `whitelist`.")
                Settings.DebugCardAccess.Forbidden
            }
        }
    }

    val registerLuaJArchitecture = config.getBoolean("debug.registerLuaJArchitecture")
    val disableLocaleChanging = config.getBoolean("debug.disableLocaleChanging")

    // >= 1.7.4
    val maxSignalQueueSize: Int = max(if (config.hasPath("computer.maxSignalQueueSize")) config.getInt("computer.maxSignalQueueSize") else 256, 256)

    // >= 1.7.6
    val vramSizes: DoubleArray = config.getDoubleList("gpu.vramSizes").let {
        if (it.size == 3) doubleArrayOf(it[0].toDouble(), it[1].toDouble(), it[2].toDouble())
        else {
            OpenComputers.log.warn("Bad number of VRAM sizes (expected 3), ignoring.")
            doubleArrayOf(1.0, 2.0, 3.0)
        }
    }

    val bitbltCost: Double = if (config.hasPath("gpu.bitbltCost")) config.getDouble("gpu.bitbltCost") else 0.5

    // >= 1.8.2
    val diskActivitySoundDelay: Int = max(config.getInt("misc.diskActivitySoundDelay"), -1)
    val maxNetworkClientPacketDistance: Double = max(config.getDouble("misc.maxNetworkClientPacketDistance"), 0.0)
    val maxNetworkClientEffectPacketDistance: Double = max(config.getDouble("misc.maxNetworkClientEffectPacketDistance"), 0.0)
    val maxNetworkClientSoundPacketDistance: Double = max(config.getDouble("misc.maxNetworkClientSoundPacketDistance"), 0.0)

    fun internetFilteringRulesInvalid(): Boolean = internetFilteringRules.any { it.invalid() }

    fun internetAccessConfigured(): Boolean = httpEnabled || tcpEnabled

    fun internetAccessAllowed(): Boolean = internetAccessConfigured() && !internetFilteringRulesInvalid()

    // >= 1.8.8
    val httpUserAgent = config.getString("internet.httpUserAgent")

    // >= 1.9.0
    val audioCardChunkSize: Int = max(config.getInt("audio.chunkSize"), 0)
    val audioCardBufferLimit: Int = max(config.getInt("audio.bufferLimit"), 0)
    val audioCardSampleRate: Int = config.getInt("audio.sampleRate").coerceIn(0, 48000)
    val audioCardFormat: Int = config.getInt("audio.format")
}

object Settings {
    val resourceDomain = OpenComputers.ID
    const val namespace = "oc:"
    const val savePath = "opencomputers/"
    val scriptPath: String = "/assets/$resourceDomain/lua/"
    val screenResolutionsByTier: Array<Pair<Int, Int>> = arrayOf(50 to 16, 80 to 25, 160 to 50)
    val screenDepthsByTier: Array<ColorDepth> = arrayOf(ColorDepth.OneBit, ColorDepth.FourBit, ColorDepth.EightBit)
    val deviceComplexityByTier: IntArray = intArrayOf(12, 24, 32, 9001)
    var rTreeDebugRenderer = false
    var blockRenderId: Int = -1
    private val forbiddenConfigLists: List<String> = listOf(
            /* 1.8.3+ filtering rules migration */
            "internet.blacklist", "internet.whitelist"
    )
    private const val prefix = "opencomputers."

    @JvmStatic
    val basicScreenPixels: Int get() = screenResolutionsByTier[0].first * screenResolutionsByTier[0].second

    private var _settings: Settings? = null

    @JvmStatic
    fun get(): Settings = _settings ?: throw IllegalStateException("Settings not loaded")

    @JvmStatic
    fun load(file: File) {
        val defaults = {
            val stream = Settings::class.java.getResourceAsStream("/application.conf") ?: throw IOException("Could not find application.conf")
            val configStr = stream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
            ConfigFactory.parseString(configStr)
        }()

        val config = try {
            val plain = file.readText(StandardCharsets.UTF_8)
            val patched = patchConfig(ConfigFactory.parseString(plain), defaults).withFallback(defaults)
            _settings = Settings(patched.getConfig("opencomputers"))
            patched
        } catch (e: Exception) {
            if (file.exists()) {
                throw RuntimeException("Error parsing configuration file. To restore defaults, delete '${file.name}' and restart the game.", e)
            }
            _settings = Settings(defaults.getConfig("opencomputers"))
            defaults
        }

        for (key in forbiddenConfigLists) {
            if (config.hasPath(prefix + key)) {
                if (config.getStringList(prefix + key).isNotEmpty()) {
                    throw RuntimeException("Error parsing configuration file: removed configuration option '$key' is not empty. This option should no longer be used.")
                }
            }
        }

        try {
            val renderSettings = ConfigRenderOptions.defaults().setJson(false).setOriginComments(false)
            file.parentFile.mkdirs()
            val out = PrintWriter(file, "UTF-8")
            val rendered = config.root().render(renderSettings)
            val processed = rendered.lineSequence()
                    .filter { it.isNotBlank() }
                    .map { line ->
                        line.replace(Regex("""^(\s*)""")) { m ->
                            m.groupValues[1].replace("  ", " ")
                        }
                    }
                    .joinToString(System.lineSeparator())
                    // Newline after values.
                    .replace(Regex("""((?:\s*#.*(?:\r\n|\r|\n))(?:\s*[^#\s].*(?:\r\n|\r|\n))+)"""), "$1" + System.lineSeparator())
            out.write(processed)
            out.close()
        } catch (e: Throwable) {
            OpenComputers.log.warn("Failed saving config.", e)
        }
    }

    private val configPatches = arrayOf(
            VersionRange.createFromVersionSpec("[0.0, 1.5.20)") to arrayOf(
                    "switch.relayDelayUpgrade"
            ),
            VersionRange.createFromVersionSpec("[0.0, 1.6.2)") to arrayOf(
                    "nanomachines.potionWhitelist"
            ),
            VersionRange.createFromVersionSpec("[0.0, 1.7.2)") to arrayOf(
                    "power.cost.wirelessCostPerRange",
                    "misc.maxWirelessRange",
                    "misc.maxOpenPorts",
                    "computer.cpuComponentCount"
            ),
            VersionRange.createFromVersionSpec("[0.0, 1.8.0)") to arrayOf(
                    "computer.robot.limitFlightHeight"
            )
    )
    private val filteringRulesPatchVersion = VersionRange.createFromVersionSpec("[0.0, 1.8.3)")

    private fun patchConfig(config: Config, defaults: Config): Config {
        val modVersion = DefaultArtifactVersion(OpenComputers.Version)
        val configVersion = DefaultArtifactVersion(if (config.hasPath(prefix + "version")) config.getString(prefix + "version") else "0.0.0")
        var patched = config
        if (configVersion != modVersion) {
            OpenComputers.log.info("Updating config from version '$configVersion' to '${defaults.getString(prefix + "version")}'.")
            patched = patched.withValue(prefix + "version", defaults.getValue(prefix + "version"))
            for ((version, paths) in configPatches) {
                if (version.containsVersion(configVersion)) {
                    for (path in paths) {
                        val fullPath = prefix + path
                        OpenComputers.log.info("=> Updating setting '$fullPath'. ")
                        patched = if (defaults.hasPath(fullPath)) {
                            patched.withValue(fullPath, defaults.getValue(fullPath))
                        } else {
                            patched.withoutPath(fullPath)
                        }
                    }
                }
            }

            // Migrate filtering rules to 1.8.3+
            if (filteringRulesPatchVersion.containsVersion(configVersion) && patched.hasPath(prefix + "internet.whitelist") && patched.hasPath(prefix + "internet.blacklist")) {
                OpenComputers.log.info("=> Migrating Internet Card filtering rules. ")
                val cidrPattern = Regex("""(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})(?:/(\d{1,2}))""")
                val httpHostWhitelist = patched.getStringList(prefix + "internet.whitelist")
                val httpHostBlacklist = patched.getStringList(prefix + "internet.blacklist")
                val internetFilteringRules = mutableListOf<String>()
                for (blockedAddress in httpHostBlacklist) {
                    if (cidrPattern.find(blockedAddress) != null) {
                        internetFilteringRules += "deny ip:$blockedAddress"
                    } else {
                        internetFilteringRules += "deny domain:$blockedAddress"
                    }
                }
                for (allowedAddress in httpHostWhitelist) {
                    if (cidrPattern.find(allowedAddress) != null) {
                        internetFilteringRules += "allow ip:$allowedAddress"
                    } else {
                        internetFilteringRules += "allow domain:$allowedAddress"
                    }
                }
                if (httpHostWhitelist.isNotEmpty()) {
                    internetFilteringRules += "deny all"
                }
                for (defaultRule in defaults.getStringList(prefix + "internet.filteringRules")) {
                    internetFilteringRules += defaultRule
                }
                val patchedRules = ConfigValueFactory.fromIterable(internetFilteringRules)
                try {
                    for (key in listOf("internet.whitelist", "internet.blacklist")) {
                        if (patched.hasPath(prefix + key)) {
                            val deprecatedValue = ConfigValueFactory.fromIterable(emptyList<String>(), patched.getValue(prefix + key).origin().description())
                            patched = patched.withValue(prefix + key, deprecatedValue)
                        }
                    }
                } catch (e: Throwable) { /* pass */ }
                patched = patched.withValue(prefix + "internet.filteringRules", patchedRules)
            }
        }
        return patched
    }

    sealed interface DebugCardAccess {
        fun checkAccess(ctx: AccessContext?): String?

        object Forbidden : DebugCardAccess {
            override fun checkAccess(ctx: AccessContext?): String? = "debug card is disabled"
        }

        object Allowed : DebugCardAccess {
            override fun checkAccess(ctx: AccessContext?): String? = null
        }

        class Whitelist(val noncesFile: File) : DebugCardAccess {
            private val values = mutableMapOf<String, String>()
            private val rng = SecureRandom.getInstance("SHA1PRNG")

            init {
                load()
            }

            fun save() {
                val noncesDir = noncesFile.parentFile
                if (!noncesDir.exists() && !noncesDir.mkdirs())
                    throw IOException("Cannot create nonces directory: ${noncesDir.canonicalPath}")

                PrintWriter(OutputStreamWriter(FileOutputStream(noncesFile), StandardCharsets.UTF_8), false).use { writer ->
                    for ((p, n) in values)
                        writer.println("$p $n")
                }
            }

            fun load() {
                values.clear()
                if (!noncesFile.exists()) return

                noncesFile.bufferedReader(StandardCharsets.UTF_8).useLines { lines ->
                    lines.map { it.split(" ", limit = 2) }
                            .forEach { parts ->
                                if (parts.size == 2) {
                                    values[parts[0]] = parts[1]
                                }
                            }
                }
            }

            private fun generateNonce(): String {
                val buf = ByteArray(16)
                rng.nextBytes(buf)
                return Hex.encodeHexString(buf, true)
            }

            fun nonce(player: String) = values[player.lowercase()]

            fun isWhitelisted(player: String) = values.containsKey(player.lowercase())

            val whitelist: Set<String> get() = values.keys

            fun add(player: String) {
                if (!values.containsKey(player.lowercase())) {
                    values[player.lowercase()] = generateNonce()
                    save()
                }
            }

            fun remove(player: String) {
                if (values.remove(player.lowercase()) != null)
                    save()
            }

            fun invalidate(player: String) {
                if (values.containsKey(player.lowercase())) {
                    values[player.lowercase()] = generateNonce()
                    save()
                }
            }

            override fun checkAccess(ctx: AccessContext?): String? = when {
                ctx != null -> values[ctx.player.lowercase()].let {
                    if (it == ctx.nonce) null
                    else "debug card is invalidated, please re-bind it to yourself"
                } ?: "you are not whitelisted to use debug card"
                else -> "debug card is whitelisted, Shift+Click with it to bind card to yourself"
            }
        }
    }

    @JvmStatic
    fun getIntList(config: Config, path: String, default: List<Int>? = null): List<Int> {
        return if (config.hasPath(path))
            config.getIntList(path).map { it.toInt() }
        else
            default ?: emptyList()
    }
}
