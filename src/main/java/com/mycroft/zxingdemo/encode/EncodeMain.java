package com.mycroft.zxingdemo.encode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mycroft on 2016/12/16.
 */
public class EncodeMain {

    // 生成的二维码默认宽高
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 300;
    // 生成的二维码默认图片格式
    private static final String DEFAULT_FORMAT = "png";

    // 生成二维码的参数
    private static final Map<EncodeHintType, Object> codeHints = new HashMap<>();

    static {
        codeHints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        codeHints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
//        codeHints.put(EncodeHintType.MARGIN, 10);
//        codeHints.put(EncodeHintType.DATA_MATRIX_SHAPE, SymbolShapeHint.FORCE_RECTANGLE);
//        codeHints.put(EncodeHintType.PDF417_COMPACT, Boolean.TRUE);
//        codeHints.put(EncodeHintType.PDF417_COMPACTION, Compaction.TEXT);
//        codeHints.put(EncodeHintType.PDF417_DIMENSIONS, new Dimensions(50, 100, 50, 100));
//        codeHints.put(EncodeHintType.QR_VERSION, 5);
    }

    public static void main(String[] args) throws WriterException, IOException {
        // 生成二维码的内容
        final String text = "Hello world!";

        // 使用 MultiFormatWriter 构造 BitMatrix 对象,
//        final BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, DEFAULT_WIDTH, DEFAULT_HEIGHT, codeHints);
        final BitMatrix bitMatrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, DEFAULT_WIDTH, DEFAULT_HEIGHT, codeHints);

//        MatrixToImageWriter.writeToStream(bitMatrix, DEFAULT_FORMAT, getOutputStream());

        MatrixToImageWriter.writeToStream(bitMatrix,
                "png",
                new FileOutputStream("C:\\Users\\Mycroft\\Pictures\\hello.png"),
                new MatrixToImageConfig(Color.RED.getRGB(), Color.GREEN.getRGB()));

        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix, new MatrixToImageConfig(Color.BLACK.getRGB(), Color.WHITE.getRGB()));
    }

    private static OutputStream getOutputStream() throws FileNotFoundException {
        return new BufferedOutputStream(new FileOutputStream("C:\\Users\\Mycroft\\Pictures\\hello.png"));
    }

    /**
     * 生成二维码
     *
     * @param contents 二维码内容
     * @return 二维码的描述对象 BitMatrix
     * @throws WriterException 编码时出错
     */
    private BitMatrix encode(String contents) throws WriterException {
        final Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        return new QRCodeWriter().encode(contents, BarcodeFormat.QR_CODE, 320, 320, hints);
    }

}
