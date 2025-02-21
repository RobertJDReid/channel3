import ij.IJ
import ij.WindowManager as WM
import ij.plugin.ZProjector
import ij.process.ImageStatistics
import ij.process.ImageProcessor
import ij.process.ImageConverter
import ij.Prefs
import ij.text.TextWindow

// Use script params to take care of UI

#@ File (label="choose input file", style="file") filePath
#@ File (label="choose folder with PSF files", style="directory") psfPath
#@ File (label="choose output folder", style="directory") outputDir

def BlChan = 3; // Blue channel number - will parameterize at some point


if (filePath == null) {            // expand this to include other param vars
    IJ.log("No file selected.")
} else {
    IJ.log("File selected: " + filePath)
    // You can now use filePath to open the file or process it further.
}

// Use Bio-Formats to open the file

IJ.run("Bio-Formats Importer", "open=[" + filePath + "] autoscale color_mode=Default display_metadata rois_import=[ROI manager] split_channels view=Hyperstack stack_order=XYCZT")

// Need to biuld in some chooser for channel order in future
// for now just go with DIC = 0, Red = 1, Yellow = 2, Blue = 3
//


// **************************************************
//
// Iterate over channels for deconvolution
//

def psf_colors = ["DIC","red","yellow","blue"] as String[];
def imageList = WM.getImageTitles()

//def psfPath = IJ.getDirectory("Choose directory containing PSF files")

//def outputDir = IJ.getDirectory("Choose directory for output")
for (i in 1..3) {

    println("Running channel " + i + " - " + psf_colors[i])
    
    def match = imageList.find { it.contains("C=" + i) }
    
    def image = " -image platform " + match
    def psf = " -psf file " + psfPath + "/" + psf_colors[i] + "_psf.tif"
    def algorithm = " -algorithm RL 15"
    def outputPath = " -path " + outputDir
    def outputFile = "channel" + i + "deconv"
    def outputFileString = " -out stack " + outputFile
    
    //println("DeconvolutionLab2 Run"+ image + psf + algorithm)
    //println("Save location "+ outputFile)
    //println("output dir "+outputDir)
    IJ.run("DeconvolutionLab2 Run", image + psf + algorithm + outputFileString + outputPath);
}

// **************************************************
//
// Iterate over channels for Z projection
//

for (i in 1..3) {

    def match = imageList.find { it.contains("C=" + i) }
    def imp = WM.getImage(match)
    def impZ = ZProjector.run(imp, "max")
    def newFile = new File(outputDir, "channel${i}_maxProj.tif")
    IJ.saveAs(impZ, "Tiff", newFile.getAbsolutePath())
    impZ.show()
}


// **************************************************
//
// Iterate over channels for local maxima
//
      
imageList = WM.getImageTitles()

//println imageList

for (i in 1..3) {
    def match = imageList.find { it.contains("channel" + i + "_maxProj") }
    def imp = WM.getImage(match)
    println imp.getTitle()
    IJ.run(imp, "Gaussian Blur...", "sigma=1");
    stats = imp.getProcessor().getStatistics();
    println("The min is: " + stats.min);
    println("The mean is: " + stats.mean);
    println("The max is: " + stats.max);
    println("The sd is: " + stats.stdDev);
    
    def prominence = 3 * stats.stdDev
    IJ.run(imp, "Find Maxima...", "prominence=" + prominence + " strict exclude output=[Point Selection]");
    
    IJ.run(imp, "Measure", "");
    def newFile2 = new File(outputDir, "pointsC${i}.csv")
    
    //def newFileName2 = outputDir + "pointsC${i}.csv"
    
    IJ.saveAs("Results", newFile2.getAbsolutePath());
    IJ.run("Clear Results");
}

// **************************************************
//
// process blue channel for local binary mask
//

// close all image windows and reload blue from disk  



match = imageList.find { it.contains("C=" + BlChan) }
imp = WM.getImage(match)

def resave = outputDir.getAbsolutePath() + "/channel${BlChan}_binaryMask.tif"

//println "reload from " + reload
//println "resave to " + resave

//imp = IJ.openImage(reload);
IJ.run(imp, "Gaussian Blur...", "sigma=2 stack");
ImageConverter.setDoScaling(true);
IJ.run(imp, "8-bit", "");
IJ.setAutoThreshold(imp, "MaxEntropy dark no-reset stack");
IJ.run(imp, "Convert to Mask", "method=MaxEntropy  background=Dark black");
imp.show();

//IJ.run("Close");
IJ.saveAs(imp, "Tiff", resave);

// cleanup

IJ.run("Close All");