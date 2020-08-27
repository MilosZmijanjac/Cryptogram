package services;

import java.awt.*;
import java.io.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;

public final class SteganographyUtil {

        public static void main(String[] args) throws IOException {

            SteganographyUtil.encode(new File("/home/snake/Desktop/Cryptogram/src/resources/view_resources/User.png"),"ananas",new File("/home/snake/Desktop/Cryptogram/src/resources/view_resources/User-c.png"));
            System.out.println(SteganographyUtil.decode(new File("/home/snake/Desktop/Cryptogram/src/resources/view_resources/User-c.png")));
        }
        
        public static void encode(File inFile, String message, File outFile) throws IOException {
            Image image_orig	= ImageIO.read(inFile);
            BufferedImage image = user_space(image_orig);
            add_text(image, message);
            ImageIO.write(image,"png",outFile);
        }

        public static String decode(File file) throws IOException {
            return new String(decode_text(get_byte_data(user_space(ImageIO.read(file)))));
        }

      
        private static void add_text(BufferedImage image, String text) {
            byte[] img = get_byte_data(image);
            byte[] msg = text.getBytes();
            byte[] len = ByteBuffer.allocate(4).putInt(msg.length).array();

            encode_text(img, len,  0); 
            encode_text(img, msg, 32); 
        }

        private static BufferedImage user_space(Image image) {
            BufferedImage new_img  = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_3BYTE_BGR);
            new_img.getGraphics().drawImage(image,0,0,null);
            return new_img;
        }
        
        private static byte[] get_byte_data(BufferedImage image) {
            WritableRaster raster   = image.getRaster();
            DataBufferByte buffer = (DataBufferByte)raster.getDataBuffer();
            return buffer.getData();
        }

        private static void encode_text(byte[] image, byte[] addition, int offset) {
           
            if(addition.length + offset > image.length)
            {
                throw new IllegalArgumentException("File not long enough!");
            }
           
            for (int add : addition) {
              
                for (int bit = 7; bit >= 0; --bit, ++offset) {
                    int b = (add >>> bit) & 1;
                    image[offset] = (byte) ((image[offset] & 0xFE) | b);
                }
            }
        }

        
        private static byte[] decode_text(byte[] image) {
            int length = 0;
            int offset  = 32;
            for(int i=0; i<32; ++i){
                length = (length << 1) | (image[i] & 1);
            }

            byte[] result = new byte[length];

            for(int b=0; b<result.length; ++b )
            {
                for(int i=0; i<8; ++i, ++offset)
                {
                    result[b] = (byte)((result[b] << 1) | (image[offset] & 1));
                }
            }
            return result;
        }
    }
