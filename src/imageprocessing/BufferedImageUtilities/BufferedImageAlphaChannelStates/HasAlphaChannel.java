/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imageprocessing.BufferedImageUtilities.BufferedImageAlphaChannelStates;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * @summar
 *  The BufferedImageAdapter state that is used
 *  when the image does have an alpha channel.
 *  It reads ARGB values from the BufferedImage.
 * @author im5no
 */
public class HasAlphaChannel extends AlphaChannelState
{
    /**
     * @summary
     *  Reads ARGB values from the image's pixels
     *  into a 2D integer array.
     * @param image
     *  The image whose pixels are being parsed.
     * @return 
     *  Returns a 2D array of integers encoding
     *  the image's pixels.
     */
    @Override
    public int[][] get2DPixelColorArray(BufferedImage image)
    {
        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();

        int[][] result = new int[width][height];
        final int pixelLength = 4;
        for (
            int pixel = 0, row = 0, col = 0;
            pixel < pixels.length;
            pixel += pixelLength)
        {
            int argb = 0;
            argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
            argb += ((int) pixels[pixel + 1] & 0xff); // blue
            argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
            argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
            result[col][row] = argb;
            col++;
            if (col == width)
            {
                col = 0;
                row++;
            }
        }
        
        return result;
    }
}