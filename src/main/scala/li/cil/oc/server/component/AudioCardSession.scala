package li.cil.oc.server.component

import java.io.ByteArrayOutputStream

private[component] final class AudioCardSession(
                                                 val handle: Int,
                                                 val channel: Int,
                                                 val sampleRate: Int,
                                                 val channels: Int,
                                                 val format: Int
                                               ) {
  private val buffer = new ByteArrayOutputStream()

  var loop: Boolean = false
  var closed: Boolean = false

  private var playing: Boolean = false
  private var paused: Boolean = false
  private var playStartTime: Long = 0L
  private var remainingDurationMs: Long = 0L

  def size: Int = buffer.size()

  def append(data: Array[Byte]): Unit = buffer.write(data)

  def pcm: Array[Byte] = buffer.toByteArray

  def startPlayback(): Unit = {
    playing = true
    paused = false
    val bytesPerSample = 2
    val totalDurationMs = (size * 1000L) / (sampleRate * channels * bytesPerSample)
    remainingDurationMs = totalDurationMs
    playStartTime = System.currentTimeMillis()
  }

  def pausePlayback(): Unit = {
    if (playing && !paused) {
      paused = true
      val elapsed = System.currentTimeMillis() - playStartTime
      remainingDurationMs = math.max(0L, remainingDurationMs - elapsed)
    }
  }

  def resumePlayback(): Unit = {
    if (playing && paused) {
      paused = false
      playStartTime = System.currentTimeMillis()
    }
  }

  def stopPlayback(): Unit = {
    playing = false
    paused = false
    remainingDurationMs = 0L
  }

  def isPlayingNow: Boolean = {
    if (!playing) false
    else if (loop) true
    else if (paused) true
    else {
      val elapsed = System.currentTimeMillis() - playStartTime
      if (elapsed >= remainingDurationMs) {
        playing = false
        false
      } else {
        true
      }
    }
  }
}