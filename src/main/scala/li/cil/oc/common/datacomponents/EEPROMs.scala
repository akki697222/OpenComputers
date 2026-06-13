package li.cil.oc.common.datacomponents

import li.cil.oc.Settings
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.{ByteArrayTag, CompoundTag, StringTag}

import java.nio.ByteBuffer

object EEPROMCodeMigrator extends Migrator.DataNamespace[ByteBuffer] {
  override def fromData(tag: CompoundTag, provider: HolderLookup.Provider): Option[ByteBuffer] =
    (tag.get(Settings.namespace + "eeprom") match {
      case string: StringTag => Some(ByteBuffer.wrap(string.getAsString.getBytes))
      case bytes: ByteArrayTag => Some(ByteBuffer.wrap(bytes.getAsByteArray))
      case _ => None
    }).collect {
      case value => {
        tag.remove(Settings.namespace + "eeprom")
        value
      }
    }
}

object EEPROMUserDataMigrator extends Migrator.DataNamespace[ByteBuffer] {
  override def fromData(tag: CompoundTag, provider: HolderLookup.Provider): Option[ByteBuffer] = 
    (tag.get(Settings.namespace + "userdata") match {
      case string: StringTag => Some(ByteBuffer.wrap(string.getAsString.getBytes))
      case bytes: ByteArrayTag => Some(ByteBuffer.wrap(bytes.getAsByteArray))
      case _ => return None
    }).collect {
      case value => {
        tag.remove(Settings.namespace + "userdata")
        value
      }
    }
}

object EEPROMReadonlyMigrator extends Migrator.DataNamespace[Boolean] {
  override def fromData(tag: CompoundTag, provider: HolderLookup.Provider): Option[Boolean] =
    Option.when(tag.contains(Settings.namespace + "readonly")) {
      tag.getBoolean(Settings.namespace + "readonly")
    }
}

object LabelMigrator extends Migrator.DataNamespace[String] {
  override def fromData(tag: CompoundTag, provider: HolderLookup.Provider): Option[String] =
    Option.when(tag.contains(Settings.namespace + "label")) {
      tag.getString(Settings.namespace + "label")
    }
}
