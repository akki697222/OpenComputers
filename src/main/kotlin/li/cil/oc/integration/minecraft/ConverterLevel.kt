package li.cil.oc.integration.minecraft

import java.nio.charset.StandardCharsets
import java.util.UUID
import com.google.common.hash.Hashing
import li.cil.oc.api
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level

object ConverterLevel : api.driver.Converter {
    override fun convert(value: Any, output: MutableMap<Any, Any>) {
        if (value is ServerLevel) {
            output["id"] = UUID.nameUUIDFromBytes(
                Hashing.md5().newHasher()
                    .putLong(value.seed)
                    .putString(value.dimension().location().toString(), StandardCharsets.UTF_8)
                    .hash()
                    .asBytes()
            ).toString()
        }

        if (value is Level) {
            output["name"] = value.dimension().location().toString()
        }
    }
}
