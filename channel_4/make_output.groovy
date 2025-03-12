//************. Make output folders

// Params for results resultsFolder = new File(resultsFolderPath)

//#@ String label="Results Folder" style="directory" resultsFolder
#@ File (label="choose output folder", style="directory") resultsFolderPath

// Hardcoded list of names (examples)
def nameList = [
    "Sample 1",
    "Sample 2",
    "Some Folder with Spaces",
    "AnotherFolder"
]

println("Files: " + nameList);

// Hardcoded path to the results folder (change to your desired path)
//def resultsFolderPath = "/path/to/results"

// Create a File object for the results folder
//def resultsFolder = new File(resultsFolderPath)
// Make sure the folder exists (create if it does not)
//if (!resultsFolder.exists()) {
//    resultsFolder.mkdirs()
//}

// Iterate over names, create subfolders
nameList.each { name ->
    // Replace spaces with underscores
    def sanitizedName = name.replaceAll("\\s+", "_")
    // Create the subfolder
    def subFolder = new File(resultsFolder, sanitizedName)
    if (!subFolder.exists()) {
        subFolder.mkdirs()
        println("Created folder: " + subFolder.getAbsolutePath())
    } else {
        println("Folder already exists: " + subFolder.getAbsolutePath())
    }
}
