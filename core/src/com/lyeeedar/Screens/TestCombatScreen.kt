package com.lyeeedar.Screens

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.lyeeedar.Components.*
import com.lyeeedar.GenerationGrammar.GenerationGrammar
import com.lyeeedar.Global
import com.lyeeedar.Level.Level
import com.lyeeedar.Level.Tile
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Systems.level
import com.lyeeedar.Systems.systemList
import com.lyeeedar.UI.DebugConsole
import com.lyeeedar.Util.Array2D
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.Colour
import com.lyeeedar.Util.Point

class TestCombatScreen : AbstractScreen()
{
	lateinit var font: BitmapFont
	lateinit var batch: SpriteBatch

	var timeMultiplier = 1f
	var showFPS = true

	data class SystemData(val name: String, var time: Float, var entities: String)
	val systemTimes: Array<SystemData> by lazy { Array<SystemData>(systemList.size) { i -> SystemData(systemList[i].java.simpleName.replace("System", ""), 0f, "--") } }
	var totalSystemTime: Float = 0f
	var drawSystemTime = false

	override fun create()
	{
		val grammar = GenerationGrammar.load("Test")

		val level = grammar.generate(10, Global.engine)
		Global.engine.level = level
		level.ambient.set(Colour.WHITE)


		font = Global.skin.getFont("default")
		batch = SpriteBatch()
	}

	override fun doRender(delta: Float)
	{
		Global.engine.update(delta * timeMultiplier)

		systemUpdateAccumulator += delta
		if (systemUpdateAccumulator > 0.5f)
		{
			systemUpdateAccumulator = 0f
			var totalTime = 0f

			for (i in 0..systemTimes.size-1)
			{
				val system = Global.engine.getSystem(systemList[i].java)
				val time = system.processDuration
				val perc = (time / frameDuration) * 100f
				systemTimes[i].time = perc
				systemTimes[i].entities = if (system.family != null) system.entities.size().toString() else "-"

				totalTime += perc
			}

			totalSystemTime = totalTime
		}

		batch.begin()

		font.draw(batch, "$mousex,$mousey", 20f, Global.resolution.y - 20f)

		if (showFPS)
		{
			font.draw(batch, "FPS: $fps", Global.resolution.x - 100f, Global.resolution.y - 20f)
		}

		if (drawSystemTime)
		{
			val x = 20f
			var y = Global.resolution.y - 40f

			font.draw(batch, "System Debug: Total time usage - $totalSystemTime%", x, y)
			y -= 20f

			for (pair in systemTimes.sortedByDescending { it.time })
			{
				font.draw(batch, pair.name + " (" + pair.entities + ")", x, y)
				font.draw(batch, " - ${pair.time}%", x + 15 * 10, y)
				y -= 20f
			}
		}

		batch.end()
	}

	override fun show()
	{
		DebugConsole.register("TimeMultiplier", "'TimeMultiplier speed' to enable, 'TimeMultiplier false' to disable", fun (args, console): Boolean {
			if (args[0] == "false")
			{
				timeMultiplier = 1f
				return true
			}
			else
			{
				try
				{
					val speed = args[0].toFloat()
					timeMultiplier = speed

					return true
				}
				catch (ex: Exception)
				{
					console.error(ex.message!!)
					return false
				}
			}
		})

		DebugConsole.register("ShowFPS", "'ShowFPS true' to enable, 'ShowFPS false' to disable", fun (args, console): Boolean {
			if (args[0] == "false")
			{
				showFPS = false
				return true
			}
			else if (args[0] == "true")
			{
				showFPS = true
				return true
			}

			return false
		})

		DebugConsole.register("SystemDebug", "'SystemDebug true' to enable, 'SystemDebug false' to disable", fun (args, console): Boolean {
			if (args[0] == "false")
			{
				drawSystemTime = false
				return true
			}
			else if (args[0] == "true")
			{
				drawSystemTime = true
				return true
			}

			return false
		})

		super.show()
	}

	override fun hide()
	{
		DebugConsole.unregister("TimeMultiplier")
		DebugConsole.unregister("ShowFPS")
		DebugConsole.unregister("SystemDebug")

		super.hide()
	}

	// ----------------------------------------------------------------------
	override fun mouseMoved( screenX: Int, screenY: Int ): Boolean
	{
		updateMousePos(screenX, screenY)

		return true
	}

	override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean
	{
		updateMousePos(screenX, screenY)

		return super.touchDown(screenX, screenY, pointer, button)
	}

	fun updateMousePos(screenX: Int, screenY: Int)
	{
		val level = Global.engine.level!!
		val player = level.player
		val playerPos = player.pos()!!
		val playerSprite = player.renderable() ?: return

		var offsetx = Global.resolution.x / 2 - playerPos.position.x * Global.tilesize - Global.tilesize / 2
		var offsety = Global.resolution.y / 2 - playerPos.position.y * Global.tilesize - Global.tilesize / 2

		val offset = playerSprite.renderable.animation?.renderOffset()
		if (offset != null)
		{
			offsetx -= offset[0] * Global.tilesize
			offsety -= offset[1] * Global.tilesize
		}

		mousex = ((screenX - offsetx) / 32f).toInt()
		mousey = (((Global.resolution[1] - screenY) - offsety) / Global.tilesize).toInt()
	}

	var mousex: Int = 0
	var mousey: Int = 0

	var systemUpdateAccumulator = 0f
}