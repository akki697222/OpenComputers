package li.cil.oc

import net.minecraft.locale.Language
import net.minecraft.network.chat.*

object Localization {
    private fun resolveKey(key: String): String? {
        val nsKey = Settings.namespace + key
        return when {
            canLocalize(nsKey) -> nsKey
            canLocalize(key) -> key
            else -> null
        }
    }

    @JvmStatic
    fun canLocalize(key: String): Boolean = Language.getInstance().has(key)

    @JvmStatic
    fun localizeLater(key: String): MutableComponent = Component.translatable(resolveKey(key) ?: key)

    @JvmStatic
    fun localizeLater(key: String, vararg values: Any?): MutableComponent = Component.translatable(resolveKey(key) ?: key, *values)

    @JvmStatic
    fun localizeImmediately(key: String, vararg values: Any?): String {
        return resolveKey(key)?.let { k ->
            String.format(Language.getInstance().getOrDefault(k), *values)
                .lineSequence()
                .map { it.trim() }
                .joinToString("\n")
        } ?: key
    }

    @JvmStatic
    fun localizeImmediately(key: String): String {
        return resolveKey(key)?.let { k ->
            Language.getInstance().getOrDefault(k)
                .lineSequence()
                .map { it.trim() }
                .joinToString("\n")
        } ?: key
    }

    object Analyzer {
        @JvmStatic
        fun Address(value: String): MutableComponent {
            val result = localizeLater("gui.Analyzer.Address", value)
            return result.setStyle(result.style
                .withClickEvent(ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, value))
                .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, localizeLater("gui.Analyzer.CopyToClipboard"))))
        }

        @JvmStatic
        fun AddressCopied(): Component = localizeLater("gui.Analyzer.AddressCopied")

        @JvmStatic
        fun ChargerSpeed(value: Double): Component = localizeLater("gui.Analyzer.ChargerSpeed", (value * 100).toInt().toString() + "%")

        @JvmStatic
        fun ComponentName(value: String): Component = localizeLater("gui.Analyzer.ComponentName", value)

        @JvmStatic
        fun Components(count: Int, maxCount: Int): Component = localizeLater("gui.Analyzer.Components", "$count/$maxCount")

        @JvmStatic
        fun LastError(value: String): Component = localizeLater("gui.Analyzer.LastError", localizeLater(value))

        @JvmStatic
        fun RobotOwner(owner: String): Component = localizeLater("gui.Analyzer.RobotOwner", owner)

        @JvmStatic
        fun RobotName(name: String): Component = localizeLater("gui.Analyzer.RobotName", name)

        @JvmStatic
        fun RobotXp(experience: Double, level: Int): Component = localizeLater("gui.Analyzer.RobotXp", "%.2f".format(experience), level.toString())

        @JvmStatic
        fun StoredEnergy(value: String): Component = localizeLater("gui.Analyzer.StoredEnergy", value)

        @JvmStatic
        fun TotalEnergy(value: String): Component = localizeLater("gui.Analyzer.TotalEnergy", value)

        @JvmStatic
        fun Users(list: Iterable<String>): Component = localizeLater("gui.Analyzer.Users", list.joinToString(", "))

        @JvmStatic
        fun WirelessStrength(value: Double): Component = localizeLater("gui.Analyzer.WirelessStrength", value.toInt().toString())
    }

    object Assembler {
        @JvmStatic
        fun InsertTemplate(): String = localizeImmediately("gui.Assembler.InsertCase")

        @JvmStatic
        fun CollectResult(): String = localizeImmediately("gui.Assembler.Collect")

        @JvmStatic
        fun InsertCPU(): Component = localizeLater("gui.Assembler.InsertCPU")

        @JvmStatic
        fun InsertRAM(): Component = localizeLater("gui.Assembler.InsertRAM")

        @JvmStatic
        fun Complexity(complexity: Int, maxComplexity: Int): Component {
            val message = localizeLater("gui.Assembler.Complexity", complexity.toString(), maxComplexity.toString())
            return if (complexity > maxComplexity) Component.literal("§4").append(message)
            else message
        }

        @JvmStatic
        fun Run(): String = localizeImmediately("gui.Assembler.Run")

        @JvmStatic
        fun Progress(progress: Double, timeRemaining: String): String = localizeImmediately("gui.Assembler.Progress", progress.toInt().toString(), timeRemaining)

        @JvmStatic
        fun Warning(name: String): Component = Component.literal("§7- ").append(localizeLater("gui.Assembler.Warning.$name"))

        @JvmStatic
        fun Warnings(): Component = localizeLater("gui.Assembler.Warnings")
    }

    object Chat {
        @JvmStatic
        fun WarningLuaFallback(): Component = Component.literal("§aOpenComputers§f: ").append(localizeLater("gui.Chat.WarningLuaFallback"))

        @JvmStatic
        fun WarningProjectRed(): Component = Component.literal("§aOpenComputers§f: ").append(localizeLater("gui.Chat.WarningProjectRed"))

        @JvmStatic
        fun WarningRecipes(): Component = Component.literal("§aOpenComputers§f: ").append(localizeLater("gui.Chat.WarningRecipes"))

        @JvmStatic
        fun WarningClassTransformer(): Component = Component.literal("§aOpenComputers§f: ").append(localizeLater("gui.Chat.WarningClassTransformer"))

        @JvmStatic
        fun WarningLink(url: String): Component = Component.literal("§aOpenComputers§f: ").append(localizeLater("gui.Chat.WarningLink", url))

        @JvmStatic
        fun InfoNewVersion(version: String): Component = Component.literal("§aOpenComputers§f: ").append(localizeLater("gui.Chat.NewVersion", version))

        @JvmStatic
        fun TextureName(name: String): Component = Component.literal("§aOpenComputers§f: ").append(localizeLater("gui.Chat.TextureName", name))
    }

    object Computer {
        @JvmStatic
        fun TurnOff(): String = localizeImmediately("gui.Robot.TurnOff")

        @JvmStatic
        fun TurnOn(): String = localizeImmediately("gui.Robot.TurnOn")

        @JvmStatic
        fun Power(): String = localizeImmediately("gui.Robot.Power")
    }

    object Drive {
        @JvmStatic
        fun Managed(): String = localizeImmediately("gui.Drive.Managed")

        @JvmStatic
        fun Unmanaged(): String = localizeImmediately("gui.Drive.Unmanaged")

        @JvmStatic
        fun Warning(): String = localizeImmediately("gui.Drive.Warning")

        @JvmStatic
        fun ReadOnlyLock(): String = localizeImmediately("gui.Drive.ReadOnlyLock")

        @JvmStatic
        fun LockWarning(): String = localizeImmediately("gui.Drive.ReadOnlyLockWarning")
    }

    object Raid {
        @JvmStatic
        fun Warning(): String = localizeImmediately("gui.Raid.Warning")
    }

    object Rack {
        @JvmStatic
        fun Top(): String = localizeImmediately("gui.Rack.Top")

        @JvmStatic
        fun Bottom(): String = localizeImmediately("gui.Rack.Bottom")

        @JvmStatic
        fun Left(): String = localizeImmediately("gui.Rack.Left")

        @JvmStatic
        fun Right(): String = localizeImmediately("gui.Rack.Right")

        @JvmStatic
        fun Back(): String = localizeImmediately("gui.Rack.Back")

        @JvmStatic
        fun None(): String = localizeImmediately("gui.Rack.None")

        @JvmStatic
        fun RelayEnabled(): String = localizeImmediately("gui.Rack.Enabled")

        @JvmStatic
        fun RelayDisabled(): String = localizeImmediately("gui.Rack.Disabled")

        @JvmStatic
        fun RelayModeTooltip(): String = localizeImmediately("gui.Rack.RelayModeTooltip")

        @JvmStatic
        fun OrientationTooltip(): String = localizeImmediately("gui.Rack.OrientationTooltip")
    }

    object Switch {
        @JvmStatic
        fun TransferRate(): String = localizeImmediately("gui.Switch.TransferRate")

        @JvmStatic
        fun PacketsPerCycle(): String = localizeImmediately("gui.Switch.PacketsPerCycle")

        @JvmStatic
        fun QueueSize(): String = localizeImmediately("gui.Switch.QueueSize")
    }

    object Terminal {
        @JvmStatic
        fun InvalidKey(): Component = localizeLater("gui.Terminal.InvalidKey")

        @JvmStatic
        fun OutOfRange(): Component = localizeLater("gui.Terminal.OutOfRange")
    }

    object Tooltip {
        @JvmStatic
        fun DiskUsage(used: Long, capacity: Long): String = localizeImmediately("tooltip.diskusage", used.toString(), capacity.toString())

        @JvmStatic
        fun DiskMode(isUnmanaged: Boolean): String = localizeImmediately(if (isUnmanaged) "tooltip.diskmodeunmanaged" else "tooltip.diskmodemanaged")

        @JvmStatic
        fun Materials(): String = localizeImmediately("tooltip.materials")

        @JvmStatic
        fun DiskLock(lockInfo: String): String = if (lockInfo.isEmpty()) "" else localizeImmediately("tooltip.disklocked", lockInfo)

        @JvmStatic
        fun Tier(tier: Int): String = localizeImmediately("tooltip.tier", tier.toString())

        @JvmStatic
        fun PrintBeaconBase(): String = localizeImmediately("tooltip.print.BeaconBase")

        @JvmStatic
        fun PrintLightValue(level: Int): String = localizeImmediately("tooltip.print.LightValue", level.toString())

        @JvmStatic
        fun PrintRedstoneLevel(level: Int): String = localizeImmediately("tooltip.print.RedstoneLevel", level.toString())

        @JvmStatic
        fun MFULinked(isLinked: Boolean): String = localizeImmediately(if (isLinked) "tooltip.upgrademf.Linked" else "tooltip.upgrademf.Unlinked")

        @JvmStatic
        fun ExperienceLevel(level: Double): String = localizeImmediately("tooltip.robot_level", level.toString())
    }
}
