import ij.IJ
import ij.WindowManager as WM
import ij.text.TextWindow
import ij.gui.GenericDialog
import ij.plugin.ZProjector
import ij.process.ImageStatistics
import ij.process.ImageProcessor
import ij.process.ImageConverter
import ij.Prefs

// Use script params to take care of UI

#@ File (label="choose input file", style="file") pathToFile
#@ File (label="choose folder with PSF files", style="directory") psfPath
#@ File (label="choose output folder", style="directory") outputDir
#@ Integer (label="Enter number of deconvolution interations", style="slider", min=1, max=15, stepSize=1) deconIter

def BlChan = 3; // Blue channel number - will parameterize at some point


// -------------------------------------------------------------------------
// Step 1) Run the Bio-Formats Importer in "Metadata only" mode
// (Adjust your path as needed)
//String pathToFile = "/Volumes/Micro_Data/Rachel /1-28-25 D4 Glyc Lac C39-46/1-28-25 D4 Glyc Lac C39-46.mvd2"
//IJ.log("File selected: " + pathToFile)

IJ.run("Bio-Formats Importer",
       "open=[" + pathToFile + "] " +
       "color_mode=Default " +
       "display_metadata " +
       "rois_import=[ROI manager] " +
       "view=[Metadata only] " +
       "stack_order=Default");

// -------------------------------------------------------------------------
// Step 2) Grab the newly created text window from the WM
String metadataWindowTitle = null
String[] nonImageTitles = WM.getNonImageTitles()
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

def window = WM.getWindow(metadataWindowTitle)
if (!(window instanceof TextWindow)) {
    IJ.log("Found window titled '${metadataWindowTitle}' but it's not a TextWindow.")
    return
}

// -------------------------------------------------------------------------
// Step 3) Extract the text and (optionally) close the metadata window
String metadataText = window.getTextPanel().getText()
IJ.log("=== Captured Metadata ===\n" + metadataText)
//window.close()  // close it if you don't need that window open

// -------------------------------------------------------------------------
// Step 4) Parse only lines that start with "Series"
//
// The lines we want usually look like:
//    Series 14 Name  D4 Glyc Lac C41 5
//
// A simple approach is to use a regex that captures everything after the "Series ## Name" part:
//    ^Series\s+\d+\s+Name\s+(.*)$
// That means:
//    - Start of line
//    - "Series"
//    - some whitespace + digits + whitespace
//    - "Name" + some whitespace
//    - Then capture everything else in a group
//
def seriesNames = []
//def pattern = /^Series\s+\d+\s+Name\s+(.*)$/
//def pattern = /Series/
def pattern = /Series\s+\d+\s+Name\s+(.*)/

def lines = metadataText.readLines()
for (line in lines) {
    def matcher = line =~ pattern
    if (matcher) {
        // The text after "Series ## Name" is in group(1)
        //def name = matcher[0][1]  // or matcher.group(1) in standard Java
        //
        //  images are loaded by series #, so just capture line here and strip into name later
        //

        seriesNames << line
    }
}

// -------------------------------------------------------------------------
// Step 5) Build a checkbox dialog with these series names
if (seriesNames.isEmpty()) {
    IJ.log("No lines matched the pattern 'Series # Name ...'")
    return
}

seriesNames = seriesNames.sort();

GenericDialog gd = new GenericDialog("Select Series to Process")
seriesNames.each { n ->
    gd.addCheckbox(n, false)
}
gd.showDialog()
if (gd.wasCanceled()) {
    IJ.log("User canceled.")
    return
}

def selected = []
seriesNames.each { n ->
    if (gd.getNextBoolean()) {
        selected << n
    }
}

IJ.log("User selected series: " + selected)

// Create a File object for the results folder
//def resultsFolder = new File(resultsFolderPath)
// Make sure the folder exists (create if it does not)
//if (!resultsFolder.exists()) {
//    resultsFolder.mkdirs()
//}

// Iterate over names, create subfolders

selected.each { name ->
    // Replace spaces with underscores
    def sanitizedName = name.replaceAll("\\s+", "_")
    // Create the subfolder
    def subFolder = new File(outputDir, sanitizedName)
    if (!subFolder.exists()) {
        subFolder.mkdirs()
        println("Created folder: " + subFolder.getAbsolutePath())
    } else {
        println("Folder already exists: " + subFolder.getAbsolutePath())
    }
}

// Load selected images from Volocity file

//IJ.run("Bio-Formats Importer", "open=[/Volumes/Micro_Data/Rachel /1-28-25 D4 Glyc Lac C39-46/1-28-25 D4 Glyc Lac C39-46.mvd2] autoscale color_mode=Default rois_import=[ROI manager] split_channels view=Hyperstack stack_order=XYCZT series_1 series_2 series_3");

