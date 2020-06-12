package org.apache.isis.client.kroviz.ui.kv

import kotlinx.serialization.UnstableDefault
import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.core.event.ResourceSpecification
import org.apache.isis.client.kroviz.core.model.Exposer
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.ui.BrowserWindow
import org.apache.isis.client.kroviz.utils.IconManager
import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.VPanel

@OptIn(UnstableDefault::class)
object RoToolPanel : SimplePanel() {

    const val format = "text/plain"
    val panel = VPanel()
    private val buttons = mutableListOf<Button>()

    init {
        panel.marginTop = CssSize(40, UNIT.px)
        panel.width = CssSize(40, UNIT.px)
        panel.height = CssSize(100, UNIT.perc)
        panel.background = Background(color = Color.name(Col.GHOSTWHITE))
        panel.setDropTargetData(format) { url ->
            val reSpec = ResourceSpecification(url!!)
            val logEntry = EventStore.find(reSpec)!!
            val obj = logEntry.obj!!
            if (obj is TObject) {
                val exp = Exposer(obj)
                addButton(exp)
            }
        }

        initButtons()
        panel.addAll(buttons)
    }

    private fun initButtons() {
        val drop: Button = buildButton("Toolbox", "Sample drop target")
        drop.setDropTarget(format) {
            //IMPROVE use string for wikipedia search
            BrowserWindow("http://isis.apache.org").open()
        }
        buttons.add(drop)
        //
        val drag = buildButton("Object", "Sample drag object")
        drag.setDragDropData(format, "element")
        buttons.add(drag)
    }

    fun toggle() {
        if (panel.width?.first == 0) show() else hide()
    }

    override fun hide(): Widget {
        panel.width = CssSize(0, UNIT.px)
        panel.removeAll()
        return super.hide()
    }

    override fun show(): Widget {
        panel.width = CssSize(40, UNIT.px)
        buttons.forEach { panel.add(it) }
        return super.show()
    }

    private fun addButton(exp: Exposer) {
        var iconName = ""
        val ed = exp.dynamise()
        if (ed.hasOwnProperty("iconName") as Boolean) {
            iconName = ed["iconName"] as String
        }
        val b = buildButton(iconName, "dynamic sample")
        val tObject = exp.delegate
        val m = MenuFactory.buildFor(
                tObject,
                false,
                ButtonStyle.LINK)
        console.log("[RoToolPanel.addButton]")
        console.log(exp)
        console.log(m)
        b.apply {
            onEvent {
                dblclick = {
                    console.log("dblclick")
                    m.toggle()
                    m.show()
                }
            }
        }
        buttons.add(b)
        panel.add(b)
    }

    private fun buildButton(iconName: String, toolTip: String): Button {
        val icon =
                if (iconName.startsWith("fa")) iconName else {
                    IconManager.find(iconName)
                }
        val b = Button(
                text = "",
                icon = icon,
                style = ButtonStyle.LINK).apply {
            padding = CssSize(-16, UNIT.px)
            margin = CssSize(0, UNIT.px)
            title = toolTip
        }
        return b
    }

}
