package cn.cq.puzzles.bleach;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

// import org.loon.framework.game.helper.ImageHelper;

public class BlockView extends View {

	private static final int GRID_BG_COLOR = Color.argb(0xff, 0x0, 0x0, 0x0);

	private static final int HIGHLIGHT_COLOR = Color.argb(0xff, 0xff, 0x0, 0x0);

	private int delayMillis = 30;

	private Context context = null;

	private Bitmap backImage;

	private Bitmap screen;

	private int[] blocks;

	private boolean isEvent = false;

	private int last_x, last_y;

	private int last_x2, last_y2;

	private int  isSelected = 0;

	private float _width;

	private float _height;

	private static final int ROWS = 4;

	private static final int COLS = 4;

	private int blockWidth;

	private int blockHeight;

	private int imgsCounts;

	private Resources r = null;

	private Paint paint = new Paint();

	private Canvas backCanvas = null;

	private Canvas screenCanvas = null;

	private float rale;

	private boolean running = false;

	private boolean inited = false;

	private int xOffset, yOffset;

	private Bitmap overImage;

	private Rect bufSrcRect = null;

	private RectF bufTarRect = null;

	private int[] gridCols = new int[(COLS + 1) * ROWS];

	private int[] gridRows = new int[(ROWS + 1) * COLS];

	private int c_drawable = R.drawable.backimage;

	// add by cq
	private int init_w;
	private int init_h;

	public BlockView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public BlockView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (!inited) {
			this.init_w = w;
			this.init_h = h;
			r = context.getResources();
			init(w, h);
			int i = 0;
			// 初始化格子坐标
			for (i = 0; i <= COLS; i++) {
				gridCols[i * COLS + 0] = xOffset + i * blockWidth;
				gridCols[i * COLS + 1] = yOffset;
				gridCols[i * COLS + 2] = xOffset + i * blockWidth;
				gridCols[i * COLS + 3] = ROWS * blockHeight + yOffset;
			}

			for (i = 0; i <= ROWS; i++) {
				gridRows[i * ROWS + 0] = xOffset;
				gridRows[i * ROWS + 1] = i * blockHeight + yOffset;
				gridRows[i * ROWS + 2] = COLS * blockWidth + xOffset;
				gridRows[i * ROWS + 3] = i * blockHeight + yOffset;
			}

			bufSrcRect = new Rect(0, 0, (int) _width, (int) _height);

			bufTarRect = new RectF(0, 0, _width, _height);

			inited = true;
			update();
		}
	}

	/**
	 * 初始化拼图参数。
	 * 
	 * @param bImage
	 * @param overImage
	 * @param cs
	 * @param rs
	 */
	public void init(int w, int h) {
		setFocusable(true);
		// 获取背景图片的 Drawable 资源
		Drawable blockImage = r.getDrawable(c_drawable);
		// 获得实际窗体宽。+
		_width = blockImage.getMinimumWidth();
		// 获得实际窗体高。+
		_height = blockImage.getMinimumHeight();
		rale = Math.min(w / _width, h / _height);
		_width = (int) (_width * rale);
		_height = (int) (_height * rale);

		xOffset = (w - (int) _width) >> 1;
		yOffset = (h - (int) _height) >> 1;
		// 获得单块图像宽。
		blockWidth = (int) (_width / COLS);
		// 获得单块图像高。
		blockHeight = (int) (_height / ROWS);

		// 本程序直接使用backimage上一块图形区域缓冲选择项，所以实际背景图像高=图形高+额外图块高。

		getBackImage(r.getDrawable(c_drawable));
		getScreenImage();
		getOverImage(r.getDrawable(R.drawable.over));

		// 获得等同图片总数的数组，要多出一行和一列用作图像临时存放区
		imgsCounts = COLS * ROWS;

		blocks = new int[imgsCounts];
		// 初始化为非点击。
		isEvent = false;

		// 初始化图块参数。保存图块正常序列
		for (int i = 0; i < imgsCounts; i++) {
			blocks[i] = i;
		}
		// 随机生成图像面板内容。
		randomPannel();

	}

	private void getBackImage(Drawable src) {
		// 背景图片多一行
		Bitmap bitmap = Bitmap.createBitmap((int) _width, (int) _height
				+ blockHeight, Bitmap.Config.ARGB_8888);
		backCanvas = new Canvas();
		backCanvas.setBitmap(bitmap);
		src.setBounds(0, 0, (int) _width, (int) _height);
		src.draw(backCanvas);
		backImage = bitmap;
	}

	private void getOverImage(Drawable src) {

		Bitmap bitmap = Bitmap.createBitmap((int) _width, (int) _height,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas();
		canvas.setBitmap(bitmap);
		src.setBounds(0, 0, (int) _width, (int) _height);
		src.draw(canvas);
		overImage = bitmap;
	}

	private void getScreenImage() {
		// 背景图片多一行
		Bitmap bitmap = Bitmap.createBitmap((int) _width, (int) _height,
				Bitmap.Config.ARGB_8888);
		screenCanvas = new Canvas();
		screenCanvas.setBitmap(bitmap);

		screen = bitmap;
	}

	/**
	 * 随机生成面板图片。
	 * 
	 */
	private void randomPannel() {
		// 将源图片画到缓存图片上
		// 获取应用程序资源的类
		// 是不是打乱的次数多了些？神说7次洗牌，洗得最乱
		for (int i = 0; i < imgsCounts + (double) COLS * Math.random(); i++) {
			// 随机产生相互交换的小图片的各自的位置
			int firstPicColNum = (int) ((double) COLS * Math.random());
			int firstPicRowNum = (int) ((double) ROWS * Math.random());
			int anotherPicColNum = (int) ((double) COLS * Math.random());
			int anotherPicRowNum = (int) ((double) ROWS * Math.random());

			// 随机布局，就是随机将两个小图片交换位置。
			copyPic(firstPicColNum, firstPicRowNum, 0, ROWS);
			copyPic(anotherPicColNum, anotherPicRowNum, firstPicColNum,
					firstPicRowNum);
			copyPic(0, ROWS, anotherPicColNum, anotherPicRowNum);

			// 在blocks数组（记录小图片的顺序，都编了号）中记录打乱的位置。
			int indexOfFirstPic = blocks[firstPicRowNum * COLS + firstPicColNum];// 找到初始被移动的小图片
			blocks[firstPicRowNum * COLS + firstPicColNum] = blocks[anotherPicRowNum
					* COLS + anotherPicColNum];
			blocks[anotherPicRowNum * COLS + anotherPicColNum] = indexOfFirstPic;
		}

	}

	/**
	 * 记住对于位图的操作总是相对于位图的坐标，而在绘制的时候才是整个屏幕的坐标
	 * 一个Bitmap如果绑定了一个Canvas，利用这个Canvas我们可以在这个Bitmap上面画 东西，比如另外的图片资源，线条等。
	 */
	private void doBufferedImage() {
		// 描绘底层背景。
		screenCanvas.drawBitmap(backImage, bufSrcRect, bufTarRect, null);
		if (isEvent) {
			// 将背景图片绘制到Screen上
			screenCanvas.drawBitmap(overImage, null, bufTarRect, null);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int c = paint.getColor();
		// 绘制背景看看
		canvas.drawBitmap(screen, xOffset, yOffset, null);
		c = paint.getColor();
		if (!isEvent) {
			// 循环绘制小格子于背景缓存中。
			paint.setColor(GRID_BG_COLOR);
			int i = 0;
			int row_col_count = 0;
			for (i = 0; i <= COLS; i++) {
				row_col_count = i * COLS;
				canvas.drawLine(gridCols[row_col_count],
						gridCols[row_col_count + 1],
						gridCols[row_col_count + 2],
						gridCols[row_col_count + 3], paint);
			}
			row_col_count = 0;
			for (i = 0; i <= ROWS; i++) {
				row_col_count = i * ROWS;
				canvas.drawLine(gridRows[row_col_count],
						gridRows[row_col_count + 1],
						gridRows[row_col_count + 2],
						gridRows[row_col_count + 3], paint);
			}

			switch (isSelected) {
			case 1: {
			    Paint pt = new Paint(paint);

			    pt.setColor(HIGHLIGHT_COLOR);
			    pt.setStrokeWidth(3);
			    highlightBlock(canvas, pt, last_x, last_y);
			    break;
			}
			case 2: {
			    Paint pt = new Paint(paint);

			    pt.setColor(HIGHLIGHT_COLOR);
			    pt.setStrokeWidth(3);

			    highlightBlock(canvas, pt, last_x, last_y);
			    highlightBlock(canvas, pt, last_x2, last_y2);
			    break;
			}
			}
		}
		paint.setColor(c);
		super.onDraw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (isSelected == 2 && event.getAction() == MotionEvent.ACTION_UP) {
		    isSelected = 0; 

		    copyPic(last_x, last_y, 0, ROWS);
		    copyPic(last_x2, last_y2, last_x, last_y);
		    copyPic(0, ROWS, last_x2, last_y2);

		    mRedrawHandler.sleep(50);
		    return true;
		}
		if (event.getAction() != MotionEvent.ACTION_DOWN) {
		    return true;
		}

		if (isEvent)
		    return true;

		int k = (int) ((event.getX() - xOffset) / blockWidth);
		int l = (int) ((event.getY() - yOffset) / blockHeight);

		if (k >= ROWS)
			k = ROWS - 1;
		if (l >= COLS)
			l = COLS - 1;

		isSelected = (isSelected + 1) % 3;
		if (isSelected == 1) {
		    last_x = k;
		    last_y = l;
		}
		else if (isSelected == 2) {
		    last_x2 = k;
		    last_y2 = l;
		}

		/*it's not good to swap by (0, 0) as a temp
		// 注意这里的交换，还有记住hight 与 行相关，与y相关。width与x相关，与列相关
		copyPic(0, 0, 0, ROWS);
		copyPic(k, l, 0, 0);
		copyPic(0, ROWS, k, l);
		*/
		int i1 = blocks[0];
		// 换算选中图片存储区。
		blocks[0] = blocks[l * COLS + k];
		blocks[l * COLS + k] = i1;

		// 检测是否
		for (int j1 = 0; j1 < imgsCounts; j1++) {
			if (blocks[j1] != j1)
				break;
			if (j1 == imgsCounts - 1)
				isEvent = true;

		}

		invalidate();

		return true;
	}

	/**
	 * copy换算后的图像区域。
	 * 
	 * @param i
	 *            源图像格子的x位置
	 * @param j
	 *            源图像格子的y位置
	 * @param k
	 *            目标的x位置
	 * @param l
	 *            目标的y位置
	 */
	void copyPic(int srcCol, int srcRow, int distCol, int distRow) {
		// 注意这个方法，参数1，2，为源图像的位置，3，4为源图像的宽和高，5，6为复制的距离（相对于源图像的位置）
		RectF dist = new RectF(srcCol * blockWidth + (distCol - srcCol)
				* blockWidth, srcRow * blockHeight + (distRow - srcRow)
				* blockHeight, srcCol * blockWidth + (distCol - srcCol)
				* blockWidth + blockWidth, srcRow * blockHeight
				+ (distRow - srcRow) * blockHeight + blockHeight);

		try {
			// Bitmap.createBitmap 将制定的Bitmap上的一部分分出来创建为一个Bitmap。
			Bitmap subCopy = Bitmap.createBitmap(backImage,
					srcCol * blockWidth, srcRow * blockHeight, blockWidth,
					blockHeight);
			backCanvas.drawBitmap(subCopy, null, dist, null);

			subCopy.recycle(); // 提示Bitmap操作完应该显示的释放

		} catch (OutOfMemoryError ex) {
			// 建议大家如何出现了内存不足异常，最好return 原始的bitmap对象。.

		}
	}

	void highlightBlock(Canvas canvas, Paint paint, int col, int row) {
	    canvas.drawLine(xOffset + col * blockWidth, 
			    yOffset + row * blockHeight, 
			    xOffset + (col + 1) * blockWidth, 
			    yOffset + row * blockHeight, paint); 

	    canvas.drawLine(xOffset + col * blockWidth, 
			    yOffset + row * blockHeight, 
			    xOffset + col * blockWidth, 
			    yOffset + (row + 1) * blockHeight, paint); 

	    canvas.drawLine(xOffset + (col + 1) * blockWidth, 
			    yOffset + (row + 1) * blockHeight, 
			    xOffset + col * blockWidth, 
			    yOffset + (row + 1) * blockHeight, paint); 

	    canvas.drawLine(xOffset + (col + 1) * blockWidth, 
			    yOffset + (row + 1) * blockHeight, 
			    xOffset + (col + 1) * blockWidth, 
			    yOffset + row * blockHeight, paint); 
	}

	/**
	 * 事件触发状态。
	 * 
	 * @return
	 */
	public boolean isEvent() {
		return isEvent;
	}

	public void setEvent(boolean isEvent) {
		this.isEvent = isEvent;
	}

	private RefreshHandler mRedrawHandler = new RefreshHandler();

	// 这个Handler直接作用于UI主线程
	class RefreshHandler extends Handler {

		public void handleMessage(Message msg) {
			BlockView.this.invalidate(); // 导致重绘
			BlockView.this.update();
		}

		public void sleep(long delayMillis) {
			this.removeMessages(0);
			// 这个消息第一次是即时发送的，以后再延迟
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
	}

	public void update() {
		if (running) {
			doBufferedImage();
			mRedrawHandler.sleep(delayMillis);
		}
	}

	public void setUpdateRunningAfterInited(boolean running) {
		this.running = running;
		if (inited) {
			update();
		}
	}

	// 换图
	public void change(int drawable) {
		this.c_drawable = drawable;
		this.inited = false;
		this.init(init_w, init_h);
		this.update();
	}
}
