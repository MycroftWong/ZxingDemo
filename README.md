# ZxingDemo

现在的项目中需要加上二维码扫描，虽然使用了第三方库，也还好用，但是对这部分只是还是比较感兴趣，所以研究一下。

## 分类

二维码处理分为两部分：编码与解码

编码：使用字符串生成图片。

解码：解析图片中的字符串。

首先明确一个概念：二维码图片存在的形式非常多，文件、纸张、手机、电脑屏幕。在不同的平台上存在的形式是不一样的。

## Zxing介绍

摘自百度百科
>二维条码/二维码（2-dimensional bar code）是用某种特定的几何图形按一定规律在平面（二维方向上）分布的黑白相间的图形记录数据符号信息的；在代码编制上巧妙地利用构成计算机内部逻辑基础的“0”、“1”比特流的概念，使用若干个与二进制相对应的几何形体来表示文字数值信息，通过图象输入设备或光电扫描设备自动识读以实现信息自动处理：它具有条码技术的一些共性：每种码制有其特定的字符集；每个字符占有一定的宽度；具有一定的校验功能等。同时还具有对不同行的信息自动识别功能、及处理图形旋转变化点。

目前的认知告诉我们，二维码是以正方形的形式存在，以类似于二进制的方式存储数据。

在Zxing中，使用```BitMatrix```来描述一个二维码，在其内部有一个存储```boolean```值的矩形数组。这个类很好的使用代码描述了二维码。

### 转换成图片

只是用zxing-core包，那么我们最多可以得到一个```BitMatrix```, 我们想要看见二维码，则还需要将其转换成一个图片，而图片在不同的平台则是以不同的形式存在的。如png文件, jpg文件、Android的Bitmap, Java SE的 BufferedImage. 

具体转换成图片的方式，不同平台有不同的方法，后面会详细总结，这里只是尽快明确一下概念。

### 生成二维码介绍

zxing将生成二维码的方式抽象成了一个类```com.google.zxing.Writer```, 这个类不仅仅生成二维码，还可以生成条形码、

| Writer |
|---|
|BitMatrix encode(String contents, BarcodeFormat format, int width, int height) throws WriterException|
|BitMatrix encode(String contents, BarcodeFormat format, int width, int height, Map<EncodeHintType,?> hints) throws WriterException;|

如上所示，```Writer```共有两个方法，都是用于生成二维码。方法参数说明如下

| 方法 | 说明 |
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
| AZTEC_LAYERS | aztec编码，相关，不理解 |
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

转换方法
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

其中的参数就不再解释了，主要讨论将```BitMatrix```转换成```Bitmap```的问题。

在讨论```BitMatrix```与```Bitmap```的转换之前，想先讨论一下两者的内部结构。

##### BitMatrix

```BitMatrix```表示位数组的二维矩阵。而它内部则是使用一维```int```数组来实现的，一个```int```数组有32位。不过比较特别的是，每一行都是由一个新的```int```值开始，如果一行不是32的倍数，一行最后一个```int```值中没有用到的位没有任何值。另外位是从```int```值的最小位还是排的，这是为了和```com.google.zxing.common.BitArray```更好的转换。

不关心其内部实现，在其抽象的数据结构中，x表示列数，y表示行数，可以通过```BitMatrix.get(int x, int y)```获取该位置是否为1. 

```BitMatrix```中几个可能应该熟悉方法如下
| 方法 | 说明 |
|---|:---|
| public boolean get(int x, int y) | 获取(x, y)的位值，true表示黑色 |
| public void set(int x, int y) | 设置(x, y)的位值为true |
| public void unset(int x, int y) | 设置(x, y)的位值为false |
| public void flip(int x, int y) | 对(x, y)的位值做非运算 |
| public BitMatrix(int width, int height) | 构造函数，指定宽高 |

另外说明一下```com.google.zxing.common.BitArray```这个类，这个类数据结构和```BitMatrix```的一行是一样的，使用```int```数组来表示一维位数组，同样的，最后一位```int```值可能有部分位没有用到。也同样的，位事从```int```值的最小位开始排列。

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





## 时间线

1. 2016年12月16日17:57:05 总结到Java SE平台

2. 2016年12月17日00:49:12 总结到Android平台