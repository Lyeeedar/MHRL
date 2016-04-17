package com.lyeeedar.Systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.math.Vector2
import com.lyeeedar.Components.*
import com.lyeeedar.Enums
import com.lyeeedar.GlobalData
import com.lyeeedar.Sprite.SpriteAnimation.MoveAnimation
import com.lyeeedar.Util.Colour

/**
 * Created by Philip on 21-Mar-16.
 */

class LightingSystem(): EntitySystem(systemList.indexOf(LightingSystem::class))
{
	lateinit var posLightEntities: ImmutableArray<Entity>
	val temp: Colour = Colour()

	override fun addedToEngine(engine: Engine?)
	{
		val posLight = Family.all(PositionComponent::class.java, LightComponent::class.java).get()

		posLightEntities = engine?.getEntitiesFor(posLight) ?: throw RuntimeException("Engine is null!")
	}

	override fun update(deltaTime: Float)
	{
		val level = posLightEntities[0].tile()?.level ?: return
		for (x in 0.. level.width-1)
		{
			for (y in 0..level.height-1)
			{
				level.getTile(x, y)?.light?.set(0f, 0f, 0f, 0f)
			}
		}

		for (entity in posLightEntities)
		{
			val pos = Mappers.position.get(entity)
			val light = Mappers.light.get(entity)
			val offset = entity.renderOffset()

			var x = pos.position.x.toFloat()
			var y = pos.position.y.toFloat()

			if (offset != null)
			{
				x += offset[0] / GlobalData.Global.tileSize
				y += offset[1] / GlobalData.Global.tileSize
			}

			val rawGrid = light.cache.rawOutput
			for (ix in 0..rawGrid.size-1)
			{
				for (iy in 0..rawGrid[0].size-1)
				{
					val lightVal = rawGrid[ix][iy]
					val gx = ix + light.cache.lastx - light.cache.lastrange
					val gy = iy + light.cache.lasty - light.cache.lastrange

					val tile = level.getTile(gx, gy) ?: continue
					val dst2 = tile.euclideanDist2(gx.toFloat(), gy.toFloat())

					var alpha = 1f - dst2 / ( light.dist * light.dist )
					if (alpha < 0) alpha = 0f

					temp.set(light.col)
					temp *= alpha
					temp *= temp.a
					temp *= lightVal.toFloat()
					temp.a = 1f

					tile.light += temp
					tile.light.a = 1f
				}
			}
		}
	}
}