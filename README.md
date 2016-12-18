# ZXing应用详解

现在的项目中需要加上二维码扫描，虽然使用了第三方库，也还好用，但是对这部分只是还是比较感兴趣，所以研究一下。

## 分类

二维码处理分为两部分：编码与解码

编码：使用字符串生成图片。

解码：解析图片中的字符串。

首先明确一个概念：二维码图片存在的形式非常多，文件、纸张、手机、电脑屏幕。在不同的平台上存在的形式是不一样的。

## ZXing介绍

摘自百度百科
>二维条码/二维码（2-dimensional bar code）是用某种特定的几何图形按一定规律在平面（二维方向上）分布的黑白相间的图形记录数据符号信息的；在代码编制上巧妙地利用构成计算机内部逻辑基础的“0”、“1”比特流的概念，使用若干个与二进制相对应的几何形体来表示文字数值信息，通过图象输入设备或光电扫描设备自动识读以实现信息自动处理：它具有条码技术的一些共性：每种码制有其特定的字符集；每个字符占有一定的宽度；具有一定的校验功能等。同时还具有对不同行的信息自动识别功能、及处理图形旋转变化点。

目前的认知告诉我们，二维码是以正方形的形式存在，以类似于二进制的方式存储数据。

在Zxing中，使用```BitMatrix```来描述一个二维码，在其内部存储一个看似```boolean```值的矩阵数组。这个类很好的抽象了二维码。

### 转换成图片

只使用zxing-core包，那么我们最多可以得到一个```BitMatrix```, 我们想要看见二维码，则还需要将其转换成一个图片，而图片在不同的平台则是以不同的形式存在的。如png文件, jpg文件、Android的Bitmap, Java SE的 BufferedImage. 

具体转换成图片的方式，不同平台有不同的方法，后面会详细总结，这里只是尽快明确一下概念。

### 生成二维码介绍

zxing将生成图形编码的方式抽象成了一个类```com.google.zxing.Writer```, 在实现类中不仅仅生成二维码，还可以生成条形码等其他图形编码

| Writer |
|---|
|BitMatrix encode(String contents, BarcodeFormat format, int width, int height) throws WriterException|
|BitMatrix encode(String contents, BarcodeFormat format, int width, int height, Map<EncodeHintType,?> hints) throws WriterException;|

如上所示，```Writer```共有两个方法，都是用于生成二维码。方法参数说明如下

| 参数 | 说明 |
|---|:---|
| String contents | 编码的内容 |
| BarcodeFormat format | 编码的方式（二维码、条形码...） |
| int width | 首选的宽度 |
| int height | 首选的高度 |
| Map<EncodeHintType,?> hints | 编码时的额外参数 |

从上面可以看出，除了我们常规认为的编码需要内容之外，还有其他不少的信息，如编码的方式（这里只探讨二维码），二维码的首选宽高（首选的意思是：生成的图片的参考尺寸，如二维码是正方形，但给一个矩形，则会留白，条形码为矩形，设置一个正方形，则也会留白）。

下面详细讨论一下额外的参数，虽然不一定所有都用到，但是尽量讨论一些可能会用到的参数。编码额外的参数是以一个```Map<EncodeHintType, ?>```存在的，key为```EncodeHintType```枚举，那么可以看到所有的参数类型。

| 参数 | 说明 |
|---|:---|
| ERROR_CORRECTION | 容错率，指定容错等级，例如二维码中使用的```ErrorCorrectionLevel```, Aztec使用```Integer``` |
| CHARACTER_SET | 编码集 |
| DATA_MATRIX_SHAPE | 指定生成的数据矩阵的形状，类型为```SymbolShapeHint``` |
| MARGIN | 生成条码的时候使用，指定边距，单位像素，受格式的影响。类型Integer, 或String代表的数字类型 |
| PDF417_COMPACT | 指定是否使用PDF417紧凑模式（具体含义不懂）类型```Boolean``` |
| PDF417_COMPACTION | 指定PDF417的紧凑类型 |
| PDF417_DIMENSIONS | 指定PDF417的最大最小行列数 |
| AZTEC_LAYERS | aztec编码相关，不理解 |
| QR_VERSION | 指定二维码版本，版本越高越复杂，反而不容易解析 |

从上面的参数表格可以看出，适用于二维码的有：```ERROR_CORRECTION```, ```CHARACTER_SET```, ```MARGIN```, ```QR_VERSION```. 

| 参数 | 使用说明 |
|---|:---|
| ERROR_CORRECTION | 分为四个等级：L/M/Q/H, 等级越高，容错率越高，识别速度降低。例如一个角被损坏，容错率高的也许能够识别出来。通常为H |
| CHARACTER_SET | 编码集，通常有中文，设置为 utf-8 |
| MARGIN | 默认为4, 实际效果并不是填写的值，一般默认值就行 |
| QR_VERSION | 通常不变，设置越高，反而不好用 |

下面是最简化的生成二维码的代码

```Java
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
```
没错，就是这么简单。但是我们得到的是一个```BitMatrix```, 如果需要显示出来则要根据不同平台来处理。

### BitMatrix 转换成图片

首先明确Java SE平台和Android平台的区别：Android平台移除关于swing部分的代码，所以如果SE平台使用到这部分代码，Android平台则不能用，不幸的是，官方的代码恰恰用到了这部分。

明确另外一个概念：图片在一个平台的存在形式有两种，内存和文件。虽然文件在不同平台通用，但是转换成文件的过程却不是通用的。如Android中将```Bitmap```转换成图片文件，SE中将```BufferedImage```转换成图片文件。所以实际上，最重要的是将```BitMatrix```转换成在内存中图片的存在形式。

#### Java SE平台

将```BitMatrix```转换成```BufferedImage```.

在官方提供的zxing-javase包中已经有了相应的方法。下面是示例代码：
```Java
BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix, new MatrixToImageConfig(Color.BLACK.getRGB(), Color.WHITE.getRGB()));
```

方法原型为：
```Java
public static BufferedImage toBufferedImage(BitMatrix matrix, MatrixToImageConfig config)
```

关于其中的参数，如下表格所示：

| 参数 | 说明 |
|---|:---|
| BitMatrix | 二维码的描述对象 |
| MatrixToImageConfig | 二维码转换成```BufferedImage```的配置参数 |

```MatrixToImageConfig```对象中只有两个域```onColor```和```offColor```, 文章开头提到二维码类似于二进制，这样的配置表示生成的```BufferedImage```用两种颜色来表示二维码上的开关。

将```BitMatrix```转换成图片文件

在官方提供的zxing-javase包中实际上有将```BitMatrix```转换成图片文件的方法，不过实际上是先将```BitMatrix```转换成```BufferedImage```, 然后将其转换成图片文件。

转换方法(```javax.imageio.ImageIO```)
```Java
public static boolean write(RenderedImage im, String formatName, File output) throws IOException
```

参数说明：
| 参数 | 说明 |
|---|:---|
| RenderedImage im | ```BufferedImage```实现了```RenderedImage```接口 |
| String formatName | 图片文件格式，通常使用 png |
| File output | 图片文件 |

上面两步结合起来就直接将```BitMatrix```转换成文件了，下面是```MatrixToImageWriter```的方法(类型```Path```表示文件路径，可以使用```File.toPath()```方法得到)
```Java
public static void writeToPath(BitMatrix matrix, String format, Path file, MatrixToImageConfig config) throws IOException
```

#### Android 平台

类似的，在Android中也是先将```BitMatrix```转换成```Bitmap```, 然后再写入到文件中。

```Bitmap```写入到文件中则非常熟悉了，如下所示：

```Java
Bitmap.compress(CompressFormat format, int quality, OutputStream stream)
```

其中的参数就不再解释了，主要需要讨论的是将```BitMatrix```转换成```Bitmap```。

在讨论```BitMatrix```与```Bitmap```的转换之前，先研究一下两者的内部结构。

##### BitMatrix

翻译：```BitMatrix```表示位数组的二维矩阵。而它内部则是使用一维```int```数组来实现的，一个```int```数组有32位。不过比较特别的是，每一行都是由一个新的```int```值开始，如果列数不是32的倍数，一行最后一个```int```值中有没有用到的位。另外位是从```int```值的最小位开始排的，这是为了和```com.google.zxing.common.BitArray```更好的转换。

不关心其内部实现，在其抽象的数据结构中，x表示列数，y表示行数，可以通过```BitMatrix.get(int x, int y)```获取该位置是否为1(开). 

```BitMatrix```中几个可能应该熟悉方法如下

| 方法 | 说明 |
|---|:---|
| public boolean get(int x, int y) | 获取(x, y)的位值，true表示黑色 |
| public void set(int x, int y) | 设置(x, y)的位值为true |
| public void unset(int x, int y) | 设置(x, y)的位值为false |
| public void flip(int x, int y) | 对(x, y)的位值做非运算 |
| public BitMatrix(int width, int height) | 构造函数，指定宽高 |

另外说明一下```com.google.zxing.common.BitArray```这个类，这个类数据结构和```BitMatrix```的一行是一样的，使用```int```数组来表示一维位数组，同样的，最后一位```int```值可能有部分位没有用到。也同样的，位是从```int```值的最小位开始排列。

##### Bitmap

```Bitmap```内部是使用C实现的，所以不能直观看到，不过可以猜测到，其内部应该使用的是一维```int```数组来实现的，一个```int```值就表示一个点的颜色。

下面列举一些可能用到的一些方法

| 方法 | 说明 |
|---|:---|
| public static Bitmap createBitmap(int width, int height, Config config) | 构造方法，创建一个透明的Bitmap |
| public void setPixels(@ColorInt int[] pixels, int offset, int stride, int x, int y, int width, int height) | 使用数组中的颜色替换Bitmap的像素点的颜色 |
| public void setPixel(int x, int y, @ColorInt int color) | 设置Bitmap中指定像素点的颜色值 |

只列举上面几个方法是因为跟我们的理解和使用比较密切。

#### BitMatrix转换成Bitmap

从前面的理解，我们可以看出，实际上```BitMatrix```转换成```Bitmap```就是将其所代表的点的开关用颜色来表示。默认情况下，我们习惯使用黑色代表开，白色代表关。我们需要创建一个和```BitMatrix```长宽“相等”的```Bitmap```, 在转换过程中，我们发现```BitMatrix```某一个位置是开，我们则设置```Bitmap```相应位置的颜色为开的颜色，反之同理。（当然我们也可以根据特殊需求修改其中的颜色）

代码示例

```Java
private Bitmap bitMatrixToBitmap(BitMatrix bitMatrix) {
    final int width = bitMatrix.getWidth();
    final int height = bitMatrix.getHeight();

    final int[] pixels = new int[width * height];
    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            pixels[y * width + x] = bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
        }
    }
    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

    return bitmap;
}
```

上面分为三步：

1. 创建一个一维```int```数组存放转换后的颜色值
2. 根据```BitMatrix```中的位值设置相应像素点的颜色值
3. 创建一个“相同”大小的```Bitmap```, 使用代表颜色的数组为其赋值

注意：颜色值中前两位默认为00, 这样表示透明，所以一般都是要设置为FF, 不然在调试过程中就比较坑。

关于```Bitmap.setPixels(@ColorInt int[] pixels, int offset, int stride, int x, int y, int width, int height)```这个方法其中的参数比较多，详细说明一下

| 参数 | 说明 |
|---|:---|
| int[] pixels | 像素点颜色数组 |
| int offset | 从偏移颜色数组第一个像素多少开始读起 |
| int stride | 每隔多少个点跳行，通常和宽度相同，不过也可以更大，设置为负值 |
| int x | Bitmap接收值的x轴起点 |
| int y | Bitmap接收值的y轴起点 |
| int width | 每一行复制多少颜色点 |
| int height | 一个复制多少行 |

因为考虑到像素点颜色数组和```Bitmap```大小本身存在不同所以才有这些参数，实际上，像素点颜色数组的大小和```Bitmap```的大小是相同的。那么其中的参数分别是：像素点颜色数组、0表示不偏移，直接从第一位复制、```Bitmap```宽度，复制完刚好一行则开始从下一个点开始进行复制下一行、0表示从左上角开始复制、0表示从左上角开始复制、```Bitmap```的宽度表示刚好复制到整个```Bitmap```, ```Bitmap```的宽度表示刚好复制到整个```Bitmap```

### 解析二维码介绍

zxing将解析图形编码的方式抽象成了一个接口```com.google.zxing.Reader```, 实现类中可以解析多种图形编码。

| Reader |
|---|
| Result decode(BinaryBitmap image) throws NotFoundException, ChecksumException, FormatException |
| Result decode(BinaryBitmap image, Map<DecodeHintType,?> hints) throws NotFoundException, ChecksumException, FormatException |
| void reset() |

```Reader```共有三个方法，```decode```方法用于解析图形编码，返回一个解析结果；```reset```将重置解析器的状态，便于复用。

关于```encode```的参数和返回值：

| 参数 | 说明 |
|---|---|
| BinaryBitmap image | 被解析的图片 |
| Map<DecodeHintType, ?> hints | 帮助解析的一些额外的参数 |
| Result | 解析的结果 |

关于解码时额外的参数

| 参数 | 说明 |
|---|:---|
| OTHER | 未指定作用，应用自定义，Object类型 |
| PURE_BARCODE | Boolean类型，指定图片是一个纯粹的二维码 |
| POSSIBLE_FORMATS | 可能的编码格式，List类型 |
| TRY_HARDER | 花更多的时间用于寻找图上的编码，优化准确性，但不优化速度，Boolean类型 |
| CHARACTER_SET | 编码字符集，通常指定UTF-8 |
| ALLOWED_LENGTHS | 允许的编码数据长度 - 拒绝多余的数据。不懂这是什么，int[]类型 |
| ASSUME_CODE_39_CHECK_DIGIT | CODE 39 使用，不关心 |
| ASSUME_GS1 | 假设使用GS1编码来解析，不关心 |
| RETURN_CODABAR_START_END | CODABAR编码使用，不关心 |
| NEED_RESULT_POINT_CALLBACK | 当解析到可能的结束点时进行回调 |
| ALLOWED_EAN_EXTENSIONS | 允许EAN或UPC编码有额外的长度，不关心 |

从上面的参数表格可以看出，适用于二维码的有：PURE_BARCODE, POSSIBLE_FORMATS, TRY_HARDER, CHARACTER_SET. 不过一般不会使用PURE_BARCODE, POSSIBLE_FORMATS设置为```BarcodeFormat.QR_CODE```, CHARACTER_SET设置为utf-8.

下面是最简单的解析二维码的代码

```Java
/**
 * 解析二维码
 *
 * @param binaryBitmap 被解析的图形对象
 * @return 解析的结果
 */
private String decode(BinaryBitmap binaryBitmap) {
    try {
        Map<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, BarcodeFormat.QR_CODE);

        Result result = new QRCodeReader().decode(binaryBitmap, hints);

        return result.getText();
    } catch (NotFoundException | ChecksumException | FormatException e) {
        e.printStackTrace();
        return null;
    }
}
```

解析时，我们需要一个```BinaryBitmap```, 其只有一个构造器，接受一个```com.google.zxing.Binarizer```对象，所以无论是在哪个平台，无论图片是以什么样的形式存在的（文件、内存、Bitmap、BufferedImage），都需要提供一个```Binarizer```对象，将图片转换成一个```BinaryBitmap```.

##### BinaryBitmap

翻译：这是在ZXing中用于代表一个位数据的核心位图类。```Reader```对象接受一个```BinaryBitmap```并试图对它进行解码。

这个类使用了```final```修饰，只有一个接受```Binarizer```对象的构造器，并在其内部实质上也只有一个```Binarizer```对象，其所有方法都是代理到```Binarizer```或是```Binarizer```构造的一个```BitMatrix```对象。

##### Binarizer

这个类使用了```abstract```修饰。

翻译：这个类在层次上提供了一组方法用于将亮度数据(luminance data)转换成一个位数据。它允许算法多种多样，例如允许服务器使用非常耗资源的阈值计算，允许手机使用比较快的算法。它也允许实现类多样化，例如安卓上使用JNI，其他平台使用备选的版本。

摘自百度知道
> PS解释：“阈值”命令将灰度或彩色图像转换为高对比度的黑白图像。您可以指定某个色阶作为阈值。所有比阈值亮的像素转换为白色；而所有比阈值暗的像素转换为黑色。“阈值”命令对确定图像的最亮和最暗区域很有用。
>
> 我的解释，就是拿黑白2色去阐述你的图片，是可调节的。

单词Binarizer的翻译：二值化。通常在图像处理上使用比较多，可以和阈值计算处理看做类似的概念，因为对于目前的图形编码来说，一张图片只认为有两种颜色，表示开关。所以需要将一张彩色的图片转换成一张黑白色的图。这个过程就成为二值化(Binarizer). 

这个类只有一个使用```protected```修饰的构造器，这个构造器只接受一个```LuminanceSource```对象。其所有的方法都是抽象的。

```Binarizer```有两个子类，```com.google.zxing.common.GlobalHistogramBinarizer```和```com.google.zxing.common.HybridBinarizer```.

翻译：```GlobalHistogramBinarizer```, 全局直方图二值化。这个```Binarizer```的实现类使用了早期的ZXing全局直方图方法。它适合没有足够CPU和内存的低端手机来使用本地阈值算法。但它选择了全部的黑点来计算，因此不能处理阴影和渐变两种情况。快速的手机设备和所有的桌面应用应该使用```HybridBinarizer```.

翻译：```HybridBinarizer```, 混合型二值化。这个```Binarizer```的实现类使用了本地阈值算法，比```GlobalHistogramBinarizer```要慢，相对而言也比较精准。它专门为以白色为背景的连续黑色块二维码图像解析而设计，也更适合用来解析具有严重阴影和渐变的二维码图像。（部分参考文章[zxing扫描二维码和识别图片二维码及其优化策略](http://iluhcm.com/2016/01/08/scan-qr-code-and-recognize-it-from-picture-fastly-using-zxing/?utm_source=tuicool&utm_medium=referral)）

两者的大概意思是```GlobalHistogramBinarizer```适合CPU和内存比较差的低端手机，解析效果没有```HybridBinarizer```好，但是```HybridBinarizer```耗费的资源更多，解析速度也稍慢，不过对于目前市面上的手机CPU和内存都不会太差，所以大可以直接使用```HybridBinarizer```. 另外，```HybridBinarizer```继承自```GlobalHistogramBinarizer```, 两者都只有一个接受一个```LuminanceSource```的构造器。

##### LuminanceSource

翻译：这个类层次的目的是抽象在不同平台上的位图，实现成一个标准的接口用于请求灰度的亮度值。这个接口值提供不可改变的方法；因此剪切或者旋转时将创造一个副本（不复用）。这样是为了保证一个```Reader```不能修改原亮度数据，而且让他对于在处理链的其他```Reader```保持一个未知的状态。

对于这个类的作用还不太清楚，不过我们可以知道的是，我们需要将在不同平台的图片对象转换成```LuminanceSource```对象，这样就可以交给Zxing来进行解析了。

在zxing-core包中，有两个```LuminanceSource```的实现类，```com.google.zxing.RGBLuminanceSource```和```com.google.zxing.PlanarYUVLuminanceSource```. 在zxing-javase包中，有一个实现类```com.google.zxing.client.j2se.BufferedImageLuminanceSource```.

```RGBLuminanceSource```, 这个类用于帮助解析图片文件，这个图片文件是从一个ARGB的像素数组转换成一个RGB数据的。但是不支持旋转。

```PlanarYUVLuminanceSource```, 这个对象继承自```LuminanceSource```, 多从相机设备中返回的YUV数据数组转换得到，可以选择性的将YUV的完整数据剪切其中一部分用于解析（具体参数可以查看其构造函数）。这样可以用于取出边界外多余的像素用于加快解析速度。

#### Java SE平台

既然官方在zxing-core包中提供了```BufferedImageLuminanceSource```, 我们直接使用即可。它接受一个```BufferedImage```作为构造器参数，也有一个重载构造器，用于取得```BufferedImage```的部分来进行解析。

下面代码展示解析一个图片文件上的二维码

```Java
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
```

#### Android 平台

官方包中并没有一个所谓的```BitmapLuminanceSource```, 而网上也有定义这样一个类，但是实现效果并不好，多是使用```Bitmap```构造一个```RGBLuminanceSource```. 下面是演示代码

```Java
/**
 * 解析Bitmap中的二维码
 *
 * @param bitmap
 * @return 解析结果，null表示解析失败
 */
private String decode(Bitmap bitmap) {
    int width = bitmap.getWidth();
    int height = bitmap.getHeight();
    final int[] pixels = new int[width * height];
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
    RGBLuminanceSource luminanceSource = new RGBLuminanceSource(width, height, pixels);
    BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(luminanceSource));

    try {
        final Map<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        hints.put(DecodeHintType.POSSIBLE_FORMATS, BarcodeFormat.QR_CODE);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        Result result = new QRCodeReader().decode(binaryBitmap, hints);

        return result.toString();
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}
```

不过使用相机扫描解析二维码却不同，在Android API 21以下使用```android.hardware.Camera```来进行扫描时，通常在预览状态下得到的是一个```byte```数组，这时，就比较容易用来构造一个```com.google.zxing.PlanarYUVLuminanceSource```, 具体如何使用，在讨论到相机时会再说明。

## 标注

Demo地址[ZxingDemo](https://github.com/MycroftWong/ZxingDemo)

使用到的jar包：[core-3.3.0.jar](http://repo1.maven.org/maven2/com/google/zxing/core/3.3.0/core-3.3.0.jar
), [javase-3.3.0.jar](http://repo1.maven.org/maven2/com/google/zxing/javase/3.3.0/javase-3.3.0.jar
)

## 时间线

1. 2016年12月16日17:57:05 总结到Java SE平台

2. 2016年12月17日00:49:12 总结到Android平台

3. 2016年12月18日00:09:50 总结如何解析图片中的二维码

4. 2016年12月18日13:56:20 修改文章一些细节

## 参考

[zxing扫描二维码和识别图片二维码及其优化策略](http://iluhcm.com/2016/01/08/scan-qr-code-and-recognize-it-from-picture-fastly-using-zxing/?utm_source=tuicool&utm_medium=referral)