# Channel3 v0.3

![TV](TV.png)

A [Groovy](https://groovy-lang.org/) script for [ImageJ/Fiji](https://imagej.net/software/fiji/) that automates channel-based deconvolution, Z-projection, local maxima detection, and binary mask generation on a multi-channel image. This script uses [Bio-Formats](https://www.openmicroscopy.org/bio-formats/) for file import and the [DeconvolutionLab2](https://github.com/Biomedical-Imaging-Group/DeconvolutionLab2) plugin for deconvolution.

The goal is identification of subcellular focus formation of fluorescently tagged proteins.
This script identifies foci positions in the XY plane, and results are fed in to a Matlab
script for Z positioning and reporting of distances.

---

## Table of Contents
1. [Features](#features)  
2. [Requirements](#requirements)  
3. [Installation](#installation)  
4. [Usage](#usage)  
5. [Script Parameters & Workflow](#script-parameters--workflow)  
6. [Outputs](#outputs)  
7. [Notes & Tips](#notes--tips)  
8. [License](#license)

---

## Features

- **Automated Channel Processing**: The script splits the input image into separate channels and processes each for deconvolution.
- **Deconvolution**: Uses the DeconvolutionLab2 plugin to run Richardson-Lucy deconvolution with user-specified PSF files.
- **Z-Projection**: Generates a maximum-intensity projection (MIP) for each channel.
- **Local Maxima Detection**: Identifies bright spots (maxima) in each channel, saving the coordinates for later analysis.
- **Binary Mask Creation**: Creates a binary mask from one specified channel (default: the blue channel), applying a Gaussian blur and thresholding.

---

## Requirements

1. **ImageJ or Fiji**:  
   - [Fiji](https://imagej.net/software/fiji/) is recommended because it comes bundled with many scientific image processing plugins.
3. **Bio-Formats** plugin (already included in Fiji) for reading various image file formats (e.g., `.czi`, `.nd2`, etc.).
4. **DeconvolutionLab2** plugin installed in ImageJ/Fiji.  
   - [Installation instructions](https://github.com/Biocomputing-UTech/DeconvolutionLab2/wiki/Install)
5. **PSF Files**: You will need point spread function (PSF) images for each channel you intend to deconvolve.
   - PSF files can be generated using the PSF Generator plugin from [__EPFL__](https://bigwww.epfl.ch/algorithms/psfgenerator/)

---

## Installation

1. **Download the script**  
   Save or copy the `channel_3.groovy` file into a convenient location on your computer.

2. **Place in Fiji Scripts folder**  
   - Open Fiji, go to **`Plugins` > `Install...`** and place the script into Fiji’s `scripts` folder, or simply store the `.groovy` file wherever you keep your scripts.
   - Alternatively put a copy of the file in the Fiji.app folder under __`Fiji` > `scripts` > `Plugins`__.
      - if there is no _Plugins_ folder make a new folder called 'Plugins'
      - note this is a different folder than __`Fiji` > `plugins`__.
   - __`Channel_3`__ will now show up at the bottom of the plugins menu in __ImageJ/Fiji__

3. **Ensure DeconvolutionLab2 is installed**  
   - Download the DeconvolutionLab2 `.jar` file and place it in __`Fiji` > `plugins`__ folder if you haven’t already.

---

## Usage

1. **Launch Fiji/ImageJ**.
2. **Launch script from the Plugins menu**:  
   - Go to **`Plugins` > `Scripts` > `channel_3`** at the bottom of the menu.
3. **Run from the script editor**:  
   - Open the Fiji Script Editor (in Fiji: **`File` > `New` > `Script`** or use
     the `[` key shortcut. and then open the `channel_3.groovy` file).
   - Click the **`Run`** button (in the Script Editor).

When you run the script, it will prompt you for:

1. **Input file** (`choose input file`): The multi-channel image to be processed.  
2. **PSF folder** (`choose folder with PSF files`): The directory containing PSF `.tif` files for each channel (e.g., `red_psf.tif`, `yellow_psf.tif`, `blue_psf.tif`).  
3. **Output folder** (`choose output folder`): Where the script will save all output files.
4. __Iterations__ for the Richarson-Lucy deconvolution algorithm.

---

## Script Parameters & Workflow

1. **Channel Definitions**  
   - By default, the script is set up to handle 4 channels:
     - Channel 0: DIC (Differential Interference Contrast)  
     - Channel 1: Red  
     - Channel 2: Yellow  
     - Channel 3: Blue  
   - The script focuses on channels `1`, `2`, and `3` for deconvolution and subsequent processing.

2. **Deconvolution**  
   - Uses DeconvolutionLab2’s Richardson-Lucy (RL) algorithm (15 iterations) for each channel (1 to 3).
   - PSF files must follow naming conventions like `red_psf.tif`, `yellow_psf.tif`, `blue_psf.tif`, stored in the selected PSF directory.

3. **Z-Projection**  
   - After deconvolution, it performs a maximum intensity projection (MIP) on each channel.

4. **Local Maxima Detection**  
   - Applies a small Gaussian blur (σ=1) to each MIP, calculates image statistics, and uses `3×(standard deviation)` as the prominence threshold for local maxima.  
   - Points are saved in a CSV file for each channel (`pointsC{channel}.csv`).

5. **Binary Mask Creation (Blue Channel)**  
   - For the final step, channel 3 (blue) is processed again.  
   - A Gaussian blur (σ=2) is applied, image is converted to 8-bit, and auto-thresholded (MaxEntropy) to produce a binary mask.  
   - The resulting mask is saved as `channel3_binaryMask.tif`.

6. **Cleanup**  
   - The script closes all images after processing is complete to keep the Fiji workspace tidy.

---

## Outputs

Inside the chosen output folder, you should find:

1. **`channelNdeconv.tif`** – Deconvolved stack for each channel (N = 1,2,3).  
2. **`channelN_maxProj.tif`** – Maximum-intensity projection for each channel (N = 1,2,3).  
3. **`pointsC{N}.csv`** – CSV files containing the (X, Y) coordinates and intensity measurements for local maxima in each channel.  
4. **`channel3_binaryMask.tif`** – Binary mask generated from the blue channel (default channel 3).

---

## Notes & Tips

- **Channel Mapping**: If your dataset does not use the default arrangement (0: DIC, 1: Red, 2: Yellow, 3: Blue), you may need to adjust the script accordingly.
- **PSF Files**: Ensure that your PSF filenames match the channel naming in the script (`red_psf.tif`, `yellow_psf.tif`, `blue_psf.tif`). Modify if needed.
- **Adjusting Parameters**: 
  - Iterations for Richardson-Lucy can be changed in the line:  
    ```groovy
    def algorithm = " -algorithm RL 15"
    ```
  - The Gaussian blur settings and threshold method can also be edited in the script if your data calls for different filters.
- **Performance**: Deconvolution is computationally intensive and requires a Fourier transform library.
   The FFTW2 libraries available from EPFL can dramatically speed up the deconvolution step

## TBD

- User specified channel mapping
- User specified control over deconvolution algorithm
- automate calling of followup script for Z positioning

---

## License

This script is provided under the [MIT License](./LICENSE). Feel free to modify and distribute as needed.

---
