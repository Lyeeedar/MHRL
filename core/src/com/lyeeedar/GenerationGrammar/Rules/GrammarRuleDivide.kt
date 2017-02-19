package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate
import com.lyeeedar.GenerationGrammar.Area
import com.lyeeedar.GenerationGrammar.GrammarSymbol
import com.lyeeedar.Util.children
import com.lyeeedar.Util.round
import java.util.*

class GrammarRuleDivide : AbstractGrammarRule()
{
	val divisions = Array<Division>()
	var onX = true

	override fun execute(area: Area, ruleTable: ObjectMap<String, AbstractGrammarRule>, defines: ObjectMap<String, String>, variables: ObjectFloatMap<String>, symbolTable: ObjectMap<Char, GrammarSymbol>, ran: Random, deferredRules: Array<DeferredRule>)
	{
		area.xMode = onX

		var current = 0
		for (i in 0..divisions.size-1)
		{
			val division = divisions[i]

			area.writeVariables(variables)
			val size = if (division.size == "remainder") area.size - current else Math.min(area.size - current, division.size.evaluate(variables, ran).round())
			val newArea = area.copy()
			newArea.pos = area.pos + current
			newArea.size = size

			if (!division.rule.isNullOrBlank() && newArea.hasContents)
			{
				newArea.points.clear()
				newArea.addPointsWithin(area)

				val rule = ruleTable[division.rule]
				rule.execute(newArea, ruleTable, defines, variables, symbolTable, ran, deferredRules)
			}

			current += size
			if (current == area.size) break
		}
	}

	override fun parse(xml: XmlReader.Element)
	{
		onX = xml.getAttribute("Axis", "X") == "X"

		for (el in xml.children())
		{
			val size = el.get("Size").toLowerCase().replace("%", "#size")
			val rule = el.get("Rule", "")

			divisions.add(Division(size, rule))
		}
	}
}

data class Division(val size: String, val rule: String?)