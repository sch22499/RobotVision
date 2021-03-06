/*
 * Hunter Lloyd
 * Copyrite.......I wrote, ask permission if you want to use it outside of
 * class. 
 */
package robotvisionimageprocessing;

import robotvisionimageprocessing.GlobalUtilities.ThreadSafeContainer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.awt.image.PixelGrabber;
import java.awt.image.MemoryImageSource;
import java.util.Collection;
import java.util.prefs.Preferences;
import robotvisionimageprocessing.ColorTracking.GUI.ColorTrackingFrame;
import robotvisionimageprocessing.ColorTracking.IColorTrackingFunction;
import robotvisionimageprocessing.Equalization.EqualizingFunctionGenerator;
import robotvisionimageprocessing.Equalization.IEqualizingFunction;
import robotvisionimageprocessing.Histogram.ImageHistogramData;
import robotvisionimageprocessing.Histogram.GUI.HistogramFrame;
import robotvisionimageprocessing.Histogram.IntensityFrequencyTuple;

class IMP implements MouseListener
{
    // Instance Fields you will be using below
    JFrame frame;
    JPanel panel;
    JButton startButton;
    JScrollPane scrollPane;
    JMenuItem openMenuItem;
    JMenuItem exitMenuItem;
    JMenuItem resetMenuItem;
    Toolkit toolkit;
    File imageFile;
    ImageIcon imageIcon;
    int xPixelIndex;
    int yPixelIndex;
    int[] pixelArray1D;
    int[] originalLoadedPixelArray;
   
    // This will be your height and width of your 2d array
    int imageHeight = 0;
    int imageWidth = 0;
   
    // your 2D array of pixels
    ThreadSafeContainer<int[][]> pixelArray2DContainer;

    /* 
     * In the Constructor I set up the GUI, the frame the menus. The open
     * pulldown menu is how you will open an image to manipulate. 
     */
    IMP()
    {
        this.pixelArray2DContainer = new ThreadSafeContainer<>();
        
        toolkit = Toolkit.getDefaultToolkit();
        frame = new JFrame("Image Processing Software by Hunter");
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenu functions = getFunctions();
        
        frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent ev){quit();}
        });
        
        openMenuItem = new JMenuItem("Open");
        openMenuItem.addActionListener(
            (ActionEvent evt) ->
            {
                handleOpen();
            });
        
        resetMenuItem = new JMenuItem("Reset");
        resetMenuItem.addActionListener(
            (ActionEvent evt) ->
            {
                reset();
            });     
        
        exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(
            (ActionEvent event) ->
            {
                quit();
            });
        
        file.add(openMenuItem);
        file.add(resetMenuItem);
        file.add(exitMenuItem);
        bar.add(file);
        bar.add(functions);
        frame.setSize(600, 600);
        panel = new JPanel();
        panel.setBackground(new Color(0, 0, 0));
        scrollPane = new JScrollPane(panel);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        JPanel butPanel = new JPanel();
        butPanel.setBackground(Color.black);
        
        startButton = new JButton("start");
        startButton.setEnabled(false);
        startButton.addActionListener((ActionEvent evt) ->
        {
            removeRed();
        });
        
        butPanel.add(startButton);
        frame.getContentPane().add(butPanel, BorderLayout.SOUTH);
        frame.setJMenuBar(bar);
        frame.setVisible(true);      
    }
   
    /* 
     * This method creates the pulldown menu and sets up listeners to selection of the menu choices. If the listeners are activated they call the methods 
     * for handling the choice, fun1, fun2, fun3, fun4, etc. etc. 
     */
    private JMenu getFunctions()
    {
        JMenu functionMenu = new JMenu("Functions");

        JMenuItem firstItem = new JMenuItem("Remove Red");
        firstItem.addActionListener((ActionEvent event) ->
        {
            removeRed();
        });
        functionMenu.add(firstItem);

        String degreesUnicode = "\u00b0";
        JMenuItem secondItem = new JMenuItem("Rotate 90" + degreesUnicode);
        secondItem.addActionListener((ActionEvent event) ->
        {
            rotate90Degrees();
        });
        functionMenu.add(secondItem);

        JMenuItem thirdItem = new JMenuItem("Convert to Greyscale");
        thirdItem.addActionListener((ActionEvent event) ->
        {
            convertToGreyscale();
        });
        functionMenu.add(thirdItem);

        JMenuItem fourthItem = new JMenuItem("Blur");
        fourthItem.addActionListener((ActionEvent event) ->
        {
            blur();
        });
        functionMenu.add(fourthItem);

        JMenuItem fifthItem = new JMenuItem("Detect Edges (5x5 Mask)");
        fifthItem.addActionListener((ActionEvent event) ->
        {
            int edgeDetectionMaskDimension = 5;
            detectEdges(edgeDetectionMaskDimension);
        });
        functionMenu.add(fifthItem);

        JMenuItem sixthItem = new JMenuItem("Open Color Histogram");
        sixthItem.addActionListener((ActionEvent event) ->
        {
            openColorHistogram();
        });
        functionMenu.add(sixthItem);

        JMenuItem seventhItem = new JMenuItem("Equalize");
        seventhItem.addActionListener((ActionEvent event) ->
        {
            equalize();
        });
        functionMenu.add(seventhItem);

        JMenuItem eighthItem = new JMenuItem("Track Colored Object");
        eighthItem.addActionListener((ActionEvent event) ->
        {
            launchColorTrackingGUI();
        });
        functionMenu.add(eighthItem);

        return functionMenu;
    }
  
    /*
     * This method handles opening an image file, breaking down the picture to a one-dimensional array and then drawing the image on the frame. 
     * You don't need to worry about this method. 
     */
    private void handleOpen()
    {  
        imageIcon = new ImageIcon();
        JFileChooser chooser = new JFileChooser();
            Preferences pref = Preferences.userNodeForPackage(IMP.class);
            String path = pref.get("DEFAULT_PATH", "");

        chooser.setCurrentDirectory(new File(path));
        int option = chooser.showOpenDialog(frame);
     
        if (option == JFileChooser.APPROVE_OPTION)
        {
            imageFile = chooser.getSelectedFile();
            pref.put("DEFAULT_PATH", imageFile.getAbsolutePath());
            imageIcon = new ImageIcon(imageFile.getPath());
        }
        imageWidth = imageIcon.getIconWidth();
        imageHeight = imageIcon.getIconHeight(); 
     
        JLabel label = new JLabel(imageIcon);
        label.addMouseListener(this);
        pixelArray1D = new int[imageWidth*imageHeight];
     
        originalLoadedPixelArray = new int[imageWidth*imageHeight];
  
        Image image = imageIcon.getImage();
        
        PixelGrabber pixelGrabber = new PixelGrabber(
            image,
            0,
            0,
            imageWidth,
            imageHeight,
            pixelArray1D,
            0,
            imageWidth );
        try
        {
            pixelGrabber.grabPixels();
        }
        catch(InterruptedException e)
        {
            System.err.println("Interrupted waiting for pixels");
            return;
        }
        
        System.arraycopy(
            pixelArray1D,
            0,
            originalLoadedPixelArray,
            0,
            imageWidth * imageHeight);
        
        turnTwoDimensional();
        panel.removeAll();
        panel.add(label);
        panel.revalidate();
        
        int frameHeightBuffer = 160;
        int frameWidthBuffer = 45;
        frame.setPreferredSize(
            new Dimension(
                imageWidth + frameWidthBuffer,
                imageHeight + frameHeightBuffer));
        frame.pack();
    }
  
    /*
     * The libraries in Java give a one dimensional array of RGB values for an image, I thought a 2-Dimensional array would be more usefull to you
     * So this method changes the one dimensional array to a two-dimensional. 
     */
    private void turnTwoDimensional()
    {
        pixelArray2DContainer.executeCodeAsync(() ->
        {
            pixelArray2DContainer.setValue(new int[imageHeight][imageWidth]);
            int[][] pixelArray2D = pixelArray2DContainer.getValue();
            for(int i=0; i<imageHeight; i++)
                for(int j=0; j<imageWidth; j++)
                    pixelArray2D[i][j] = pixelArray1D[i*imageWidth+j];
        });
    }
    
    /*
     *  This method takes the picture back to the original picture
     */
    private void reset()
    {
        System.arraycopy(
            originalLoadedPixelArray,
            0,
            pixelArray1D,
            0,
            imageWidth * imageHeight); 
        
        MemoryImageSource memoryImageSource = new MemoryImageSource(
            imageWidth,
            imageHeight,
            pixelArray1D,
            0,
            imageWidth);
        Image originalImage = toolkit.createImage(memoryImageSource);

        JLabel newLabel = new JLabel(new ImageIcon(originalImage));   
        turnTwoDimensional();
        panel.removeAll();
        panel.add(newLabel);
        panel.revalidate(); 
    }
    
    /*
     * This method is called to redraw the screen with the new image. 
     */
    private void resetPicture()
    {
        pixelArray2DContainer.executeCodeAsync(() ->
        {
            int[][] pixelArray2D = pixelArray2DContainer.getValue();

            for(int i = 0; i < imageHeight; i++)
                System.arraycopy(pixelArray2D[i],
                    0,
                    pixelArray1D,
                    i * imageWidth,
                    imageWidth);
        });
        
        MemoryImageSource memoryImageSource = new MemoryImageSource(
            imageWidth,
            imageHeight,
            pixelArray1D,
            0,
            imageWidth);
        
        Image image2 = toolkit.createImage(memoryImageSource);

        JLabel label2 = new JLabel(new ImageIcon(image2));
        panel.removeAll();
        panel.add(label2);
        panel.revalidate();
    }
  
    /*
     * This method takes a single integer value and breaks it down doing bit manipulation to 4 individual int values for A, R, G, and B values
     */
    private int[] getPixelArray(int pixel)
    {
        int temp[] = new int[4];
        temp[0] = (pixel >> 24) & 0xff;
        temp[1] = (pixel >> 16) & 0xff;
        temp[2] = (pixel >>  8) & 0xff;
        temp[3] = pixel & 0xff;
        return temp;
    }
    
    /*
     * This method takes an array of size 4 and combines the first 8 bits of each to create one integer. 
     */
    private int getPixels(int rgb[])
    {
        int rgba = (rgb[0] << 24) | (rgb[1] <<16) | (rgb[2] << 8) | rgb[3];
        return rgba;
    }
  
    public void getValue()
    {
        pixelArray2DContainer.executeCodeAsync(() ->
        {
            int[][] pixelArray2D = pixelArray2DContainer.getValue();

            int pix = pixelArray2D[yPixelIndex][xPixelIndex];
            int temp[] = getPixelArray(pix);
            System.out.println("Color value " + temp[0] + " " + temp[1] + " "+ temp[2] + " " + temp[3]);
        });
    }
  
    /**************************************************************************************************
     * This is where you will put your methods. Every method below is called when the corresponding pulldown menu is 
     * used. As long as you have a picture open first the when your fun1, fun2, fun....etc method is called you will 
     * have a 2D array called picture that is holding each pixel from your picture. 
     *************************************************************************************************/
    /*
     * Example function that just removes all red values from the picture. 
     * Each pixel value in picture[i][j] holds an integer value. You need to send that pixel to getPixelArray the method which will return a 4 element array 
     * that holds A,R,G,B values. Ignore [0], that's the Alpha channel which is transparency, we won't be using that, but you can on your own.
     * getPixelArray will breaks down your single int to 4 ints so you can manipulate the values for each level of R, G, B. 
     * After you make changes and do your calculations to your pixel values the getPixels method will put the 4 values in your ARGB array back into a single
     * integer value so you can give it back to the program and display the new picture. 
     */
    private void removeRed()
    {
        pixelArray2DContainer.executeCodeAsync(() ->
        {
            int[][] pixelArray2D = pixelArray2DContainer.getValue();

            for (int i=0; i<imageHeight; i++)
                for (int j=0; j<imageWidth; j++)
                {
                    //get three ints for R, G and B
                    int[] rgbArray = getPixelArray(pixelArray2D[i][j]);

                    rgbArray[1] = 0;
                    //take three ints for R, G, B and put them back into a single int
                    pixelArray2D[i][j] = getPixels(rgbArray);
                }

            resetPicture();
        });
    }
    
    private void rotate90Degrees()
    {
        pixelArray2DContainer.executeCodeAsync(() ->
        {
            int[][] pixelArray2D = pixelArray2DContainer.getValue();

            // Your code

            resetPicture();
        });
    }
    
    private void convertToGreyscale()
    {
    }
    
    private void blur()
    {
    }
    
    private void detectEdges(int maskDimension)
    {
    }
    
    private void openColorHistogram()
    {
        HistogramFrame histogramFrame = HistogramFrame.getInstance();
        histogramFrame.setHistogramDataGenerationFunction(
            this::generateHistogramData);
    }
    
    private void equalize()
    {
        ImageHistogramData histogramData = generateHistogramData();
        
        Collection<IntensityFrequencyTuple> redIntensityFrequencies =
            histogramData.getRedIntensityFrequencies();
        Collection<IntensityFrequencyTuple> greenIntensityFrequencies =
            histogramData.getGreenIntensityFrequencies();
        Collection<IntensityFrequencyTuple> blueIntensityFrequencies =
            histogramData.getBlueIntensityFrequencies();
        
        EqualizingFunctionGenerator equalizingFunctionGenerator =
            new EqualizingFunctionGenerator();
        
        IEqualizingFunction redEqualizer =
            equalizingFunctionGenerator
                .generateFunction(redIntensityFrequencies);
        IEqualizingFunction greenEqualizer =
            equalizingFunctionGenerator
                .generateFunction(greenIntensityFrequencies);
        IEqualizingFunction blueEqualizer =
            equalizingFunctionGenerator
                .generateFunction(blueIntensityFrequencies);
        
        pixelArray2DContainer.executeCodeAsync(() ->
        {
            int[][] pixelArray2D = pixelArray2DContainer.getValue();

            for (int i = 0; i < imageHeight; i++)
                for (int j = 0; j < imageWidth; j++)
                {
                    int[] rgbArray = getPixelArray(pixelArray2D[i][j]);

                    rgbArray[1] = redEqualizer.equalize(rgbArray[1]);
                    rgbArray[2] = greenEqualizer.equalize(rgbArray[2]);
                    rgbArray[3] = blueEqualizer.equalize(rgbArray[3]);
                    pixelArray2D[i][j] = getPixels(rgbArray);
                }
        });
        
        resetPicture();
    }
    
    private ImageHistogramData generateHistogramData()
    {
        ImageHistogramData histogramData = new ImageHistogramData();
        
        pixelArray2DContainer.executeCodeAsync(() ->
        {
            int[][] pixelArray2D = pixelArray2DContainer.getValue();

            for (int i = 0; i < imageHeight; i++)
                for (int j = 0; j < imageWidth; j++)
                {
                    int[] rgbArray = getPixelArray(pixelArray2D[i][j]);
                    histogramData.incrementRedHistogramTuple(rgbArray[1]);
                    histogramData.incrementGreenHistogramTuple(rgbArray[2]);
                    histogramData.incrementBlueHistogramTuple(rgbArray[3]);
                }
        });
        
        return histogramData;
    }
    
    private void launchColorTrackingGUI()
    {
        IColorTrackingFunction trackingFuncton = this::trackColor;
        ColorTrackingFrame gui = new ColorTrackingFrame(trackingFuncton);
    }
    
    private void trackColor(
        Color trackedColor,
        int colorMatchingRange)
    {
        System.out.println("Tracking the color:");
        System.out.println(
            "(" + trackedColor.getRed() + ", " +
            trackedColor.getGreen() + ", " +
            trackedColor.getBlue() + ")");
        
        int trackedRed = trackedColor.getRed();
        int trackedGreen = trackedColor.getGreen();
        int trackedBlue = trackedColor.getBlue();
        
        pixelArray2DContainer.executeCodeAsync(() ->
        {
            int[][] array2D = pixelArray2DContainer.getValue();

            for (int i = 0; i < imageHeight; i++)
                for (int j = 0; j < imageWidth; j++)
                {
                    int[] rgbArray = getPixelArray(array2D[i][j]);
                    int red = rgbArray[1];
                    int green = rgbArray[2];
                    int blue = rgbArray[3];
                    boolean pixelMatchesTrackedColor =
                        red <= trackedRed + colorMatchingRange &&
                        red >= trackedRed - colorMatchingRange &&
                        green <= trackedGreen + colorMatchingRange &&
                        green >= trackedGreen - colorMatchingRange &&
                        blue <= trackedBlue + colorMatchingRange &&
                        blue >= trackedBlue - colorMatchingRange;

                    int newColor = pixelMatchesTrackedColor
                        ? 255
                        : 0;

                    rgbArray[1] = newColor;
                    rgbArray[2] = newColor;
                    rgbArray[3] = newColor;

                    array2D[i][j] = getPixels(rgbArray);
                }
        });
        
        resetPicture();
    }
  

  
  
    private void quit()
    {  
        System.exit(0);
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent)
    {
    }
    
    @Override
    public void mouseExited(MouseEvent mouseEvent)
    {
    }
    
    @Override
    public void mouseClicked(MouseEvent mouseEvent)
    {
        xPixelIndex = mouseEvent.getX();
        yPixelIndex = mouseEvent.getY();
        System.out.println(xPixelIndex + "  " + yPixelIndex);
        getValue();
        startButton.setEnabled(true);
    }
   
   @Override
   public void mousePressed(MouseEvent mouseEvent)
   {
   }
   
   @Override
   public void mouseReleased(MouseEvent mouseEvent)
   {
   }
}