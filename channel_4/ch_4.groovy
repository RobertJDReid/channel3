// Groovy script for ImageJ/Fiji
// does not run after file select

import ij.IJ
import loci.plugins.BF
import loci.plugins.in.ImporterOptions
import ij.WindowManager as WM
import ij.text.TextWindow

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.io.StringWriter
import java.io.PrintWriter

// Replace this path with a valid path to your file
//String pathToFile = "/path/to/myFile.lif"  // example

#@ File (label="choose input file", style="file") pathToFile


if (pathToFile == null) {            // expand this to include other param vars
    IJ.log("No file selected.")
} else {
    IJ.log("File selected: " + pathToFile)
    // You can now use filePath to open the file or process it further.
}

// 1) Prepare the Bio-Formats ImporterOptions
ImporterOptions options = new ImporterOptions()
options.setId(pathToFile)
// We do NOT automatically open images just yet
options.setOpenAllSeries(false)
// But we do want to print metadata
options.setShowMetadata(true)

// 2) Setup our “capture” objects
StringWriter sw = new StringWriter()
PrintWriter pw = new PrintWriter(sw)

// 3) Redirect System.out to our own stream
PrintStream oldOut = System.out
ByteArrayOutputStream baos = new ByteArrayOutputStream()
PrintStream ps = new PrintStream(baos)
System.setOut(ps)

// 4) Run the Bio-Formats importer with showMetadata = true
try {
    // The BF.openImagePlus call will normally print
    // metadata and other info to the console.
    // Because we have showMetadata=true, it will attempt
    // to dump metadata lines. We capture them via System.setOut().
    BF.openImagePlus(options)
} finally {
    // 5) Restore the old System.out
    System.setOut(oldOut)
}

// 6) The Bio-Formats output is now stored in baos
pw.print(baos.toString("UTF-8"))
pw.flush()

// 7) Here is our captured metadata string
String metadataString = sw.toString()

// (Optional) Print it out in the ImageJ Log, or parse further:
IJ.log("=== Captured Metadata ===")
IJ.log(metadataString)

// Now you can parse 'metadataString' (similar to the snippet from earlier),
// create your set of unique image names, and display a checkbox dialog.
