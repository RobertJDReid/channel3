// Does not have sorting of metadata

import ij.IJ
import ij.WindowManager
import ij.gui.GenericDialog
import ij.text.TextWindow

// -------------------------------------------------------------------------
// Step 1) Run the Bio-Formats Importer in "Metadata Only" mode
// Adjust your path and options as needed:
String pathToFile = "/Volumes/Micro_Data/Rachel /1-28-25 D4 Glyc Lac C39-46/1-28-25 D4 Glyc Lac C39-46.mvd2"
IJ.log("File selected: " + pathToFile)
IJ.run("Bio-Formats Importer",
       "open=[" + pathToFile + "] " +
       "color_mode=Default " +
       "display_metadata " +
       "rois_import=[ROI manager] " +
       "view=[Metadata only] " +
       "stack_order=Default");

// -------------------------------------------------------------------------
// Step 2) Grab the newly created text window from the WindowManager
// We do not know the exact window title, but usually it starts with "Bio-Formats"
String metadataWindowTitle = null
String[] nonImageTitles = WindowManager.getNonImageTitles()
for (String t in nonImageTitles) {
    if (t.startsWith("Original Metadata")) {
        metadataWindowTitle = t
        break
    }
}

if (metadataWindowTitle == null) {
    IJ.log("Could not find the Bio-Formats metadata window!")
    return
}

def window = WindowManager.getWindow(metadataWindowTitle)
if (!(window instanceof TextWindow)) {
    IJ.log("Found window titled '${metadataWindowTitle}' but it's not a TextWindow.")
    return
}

// -------------------------------------------------------------------------
// Step 3) Extract the text
String metadataText = window.getTextPanel().getText()
IJ.log("=== Captured Metadata ===\n" + metadataText)

// (Optional) If you no longer want this text window visible, you can close it:
window.close()

// -------------------------------------------------------------------------
// Step 4) Parse the lines to extract unique image names.
//
// For example, if your lines look like:
//   D4 Glyc Lac C39 10 BitsPerPixel 16
//   D4 Glyc Lac C39 10 DimensionOrder XYCZT
//   D4 Glyc Lac C39 11 BitsPerPixel 16
// and so forth, you can parse them to collect the series names.
//
// The logic below is just a sample that assumes the first 5 tokens
// represent the image name. Adjust as needed!
def imageNames = new LinkedHashSet()
def lines = metadataText.readLines()
for (line in lines) {
    def tokens = line.split("\\s+")
    // Make sure there's at least enough tokens to form a name + key + value
    // Adjust the range [0..4] to whatever suits your naming scheme.
    if (tokens.size() >= 6) {
        def imageName = tokens[0..4].join(" ")
        imageNames << imageName
    }
}

// -------------------------------------------------------------------------
// Step 5) Build a checkbox dialog with all the discovered image names
if (imageNames.isEmpty()) {
    IJ.log("No image names detected in metadata!")
    return
}

GenericDialog gd = new GenericDialog("Select images to process")
imageNames.each { name ->
    gd.addCheckbox(name, false)
}
gd.showDialog()
if (gd.wasCanceled()) {
    IJ.log("User canceled")
    return
}

// Retrieve user’s selections
def selected = []
imageNames.each { name ->
    if (gd.getNextBoolean()) {
        selected << name
    }
}

IJ.log("User selected these images: " + selected)
