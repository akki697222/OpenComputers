package li.cil.oc.client.audio

import li.cil.oc.util.BlockPosition

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import org.lwjgl.BufferUtils
import org.lwjgl.openal.AL10
import li.cil.oc.util.Audio
import li.cil.oc.{OpenComputers, Settings}
import net.minecraft.client.Minecraft
import net.minecraft.world.phys.Vec3

class AudioSession(
                    val handle: Int,
                    val channel: Int,
                    val sampleRate: Int,
                    val channels: Int,
                    val format: Int,
                    val pos: BlockPosition
                  ) {
  private val bufferStream = new ByteArrayOutputStream()
  var loop: Boolean = false

  private var alSource: Int = -1
  private var alBuffer: Int = -1
  private var isPlayCalled: Boolean = false

  def append(data: Array[Byte]): Unit = {
    if (!isPlayCalled) {
      bufferStream.write(data)
    }
  }

  def play(): Unit = {
    if (isPlayCalled) {
      if (alSource != -1) {
        AL10.alSourcePlay(alSource)
      }
      return
    }
    isPlayCalled = true

    val pcmData = bufferStream.toByteArray
    if (pcmData.isEmpty) return

    val mc = Minecraft.getInstance
    if (mc.getSoundManager == null || mc.getSoundManager.soundEngine == null) return

    mc.getSoundManager.soundEngine.executor.execute(() => {
      try {
        AL10.alGetError()

        alBuffer = AL10.alGenBuffers()
        Audio.checkALError()

        val dataBuffer = BufferUtils.createByteBuffer(pcmData.length)
        dataBuffer.put(pcmData)
        dataBuffer.flip()

        val alFormat = if (channels == 2) AL10.AL_FORMAT_STEREO16 else AL10.AL_FORMAT_MONO16

        AL10.alBufferData(alBuffer, alFormat, dataBuffer, sampleRate)
        Audio.checkALError()

        alSource = AL10.alGenSources()
        Audio.checkALError()

        AL10.alSourceQueueBuffers(alSource, alBuffer)
        Audio.checkALError()

        val x = pos.x + 0.5f
        val y = pos.y + 0.5f
        val z = pos.z + 0.5f
        AL10.alSource3f(alSource, AL10.AL_POSITION, x, y, z)

        val maxDistance = Settings.get.beepRadius
        val volume = mc.options.getSoundSourceVolume(net.minecraft.sounds.SoundSource.BLOCKS)
        val distanceBasedGain = math.max(0f, 1f - mc.player.position.distanceTo(new Vec3(x, y, z)) / maxDistance).toFloat
        val gain = distanceBasedGain * volume

        AL10.alSourcef(alSource, AL10.AL_REFERENCE_DISTANCE, maxDistance)
        AL10.alSourcef(alSource, AL10.AL_MAX_DISTANCE, maxDistance)
        AL10.alSourcef(alSource, AL10.AL_GAIN, gain * 0.3f)
        AL10.alSourcei(alSource, AL10.AL_LOOPING, if (loop) AL10.AL_TRUE else AL10.AL_FALSE)
        Audio.checkALError()

        AL10.alSourcePlay(alSource)
        Audio.checkALError()
      } catch {
        case t: Throwable =>
          OpenComputers.log.error("Failed to play audio", t)
          cleanup()
      }
    })
  }

  def pause(): Unit = {
    if (alSource != -1) {
      Minecraft.getInstance.getSoundManager.soundEngine.executor.execute(() => {
        if (alSource != -1 && AL10.alGetSourcei(alSource, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING) {
          AL10.alSourcePause(alSource)
        }
      })
    }
  }

  def resume(): Unit = {
    if (alSource != -1) {
      Minecraft.getInstance.getSoundManager.soundEngine.executor.execute(() => {
        if (alSource != -1 && AL10.alGetSourcei(alSource, AL10.AL_SOURCE_STATE) == AL10.AL_PAUSED) {
          AL10.alSourcePlay(alSource)
        }
      })
    }
  }

  def stop(): Unit = {
    if (alSource != -1) {
      Minecraft.getInstance.getSoundManager.soundEngine.executor.execute(() => {
        if (alSource != -1) {
          AL10.alSourceStop(alSource)
        }
      })
    }
  }

  def setLoopMode(newLoop: Boolean): Unit = {
    loop = newLoop
    if (alSource != -1) {
      Minecraft.getInstance.getSoundManager.soundEngine.executor.execute(() => {
        if (alSource != -1) {
          AL10.alSourcei(alSource, AL10.AL_LOOPING, if (loop) AL10.AL_TRUE else AL10.AL_FALSE)
        }
      })
    }
  }

  def checkFinished: Boolean = {
    if (isPlayCalled && alSource != -1) {
      val state = AL10.alGetSourcei(alSource, AL10.AL_SOURCE_STATE)
      state != AL10.AL_PLAYING && state != AL10.AL_PAUSED
    } else {
      false
    }
  }

  def cleanup(): Unit = {
    if (alSource != -1) {
      try AL10.alDeleteSources(alSource) catch { case _: Throwable => }
      alSource = -1
    }
    if (alBuffer != -1) {
      try AL10.alDeleteBuffers(alBuffer) catch { case _: Throwable => }
      alBuffer = -1
    }
  }
}