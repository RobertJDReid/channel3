import ij.gui.GenericDialog
import java.awt.Checkbox
import java.awt.Panel
import java.awt.ScrollPane
import java.awt.GridLayout
import java.awt.BorderLayout

// Create a new GenericDialog
GenericDialog gd = new GenericDialog("Scrollable Options")

// Create a container panel with BorderLayout
Panel container = new Panel(new BorderLayout())

// Create a panel for checkboxes with a GridLayout (one column)
Panel checkBoxPanel = new Panel(new GridLayout(0, 1))

// Store references to each Checkbox in a list
def checkboxes = []

(1..50).each { i ->
    def cb = new Checkbox("Option ${i}", false)
    checkboxes << cb
    checkBoxPanel.add(cb)
}

// Create a ScrollPane and add the checkBoxPanel to it
ScrollPane scrollPane = new ScrollPane()
scrollPane.add(checkBoxPanel)
// Set the size of the scroll pane so that scrolling is activated when needed
scrollPane.setSize(200, 300)

// Add the scroll pane to the container panel
container.add(scrollPane, BorderLayout.CENTER)

// Add the container panel to the GenericDialog
gd.addPanel(container)

// Show the dialog
gd.showDialog()
if (gd.wasCanceled()) {
    return
}

// Recover and process user selections
checkboxes.eachWithIndex { cb, i ->
    println "Option ${i+1}: " + (cb.state ? "Selected" : "Not Selected")
}
