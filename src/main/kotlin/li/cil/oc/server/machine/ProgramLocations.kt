package li.cil.oc.server.machine

object ProgramLocations {
    @JvmField
    val architectureLocations: MutableMap<String, MutableMap<String, String>> = mutableMapOf()

    @JvmField
    val globalLocations: MutableMap<String, String> = mutableMapOf()

    @JvmStatic
    fun addMapping(program: String, label: String, vararg architectures: String) {
        if (architectures.isEmpty()) {
            globalLocations[program] = label
        } else {
            architectures.forEach { arch ->
                architectureLocations.getOrPut(arch) { mutableMapOf() }[program] = label
            }
        }
    }

    @JvmStatic
    fun getMappings(architecture: String): Map<String, String> =
        architectureLocations.getOrElse(architecture) { emptyMap<String, String>() } + globalLocations
}
