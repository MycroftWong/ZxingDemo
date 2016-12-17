package com.mycroft.zxingdemo.decode;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mycroft on 2016/12/16.
 */
public class DecodeMain {

    public static void main(String[] args) throws IOException {
        BufferedImage image = ImageIO.read(new File("C:\\Users\\Mycroft\\Pictures\\hello.png"));
        LuminanceSource luminanceSource = new BufferedImageLuminanceSource(image);
        Binarizer binarizer = new HybridBinarizer(luminanceSource);

        final String text = decode(new BinaryBitmap(binarizer));

        System.out.println(text);
    }

    /**
     * 解析图片文件上的二维码
     *
     * @param imageFile 图片文件
     * @return 解析的结果，null表示解析失败
     */
    private String decode(File imageFile) {
        try {
            BufferedImage image = ImageIO.read(imageFile);
            LuminanceSource luminanceSource = new BufferedImageLuminanceSource(image);

            Binarizer binarizer = new HybridBinarizer(luminanceSource);

            BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);

            Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, BarcodeFormat.QR_CODE);

            Result result = new QRCodeReader().decode(binaryBitmap, hints);

            return result.getText();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解析二维码
     *
     * @param binaryBitmap 被解析的图形对象
     * @return 解析的结果
     */
    private static String decode(BinaryBitmap binaryBitmap) {
        final Result result;
        try {
            Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, BarcodeFormat.QR_CODE);

            result = new QRCodeReader().decode(binaryBitmap, hints);
            return result.getText();
        } catch (NotFoundException | ChecksumException | FormatException e) {
            e.printStackTrace();
            return null;
        }
    }
}
