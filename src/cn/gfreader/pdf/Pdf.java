package cn.gfreader.pdf;

import org.vudroid.pdfdroid.codec.PdfContext;
import org.vudroid.pdfdroid.codec.PdfDocument;
import org.vudroid.pdfdroid.codec.PdfPage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;

public class Pdf {
	
	private PdfContext pdfContext;
	private PdfDocument pdfDocument;
	private PdfPage pdfPage;

	public Pdf(Context context, String pdfFilePath) {
		pdfContext = new PdfContext();
		pdfDocument = pdfContext.openDocument(pdfFilePath);
	}
	
	public int getPageCount() {
		return pdfDocument.getPageCount();
	}
	
	public Bitmap getPage(int pageNum) {
		pdfPage = pdfDocument.getPage(pageNum);
		RectF rectF = new RectF();
		rectF.bottom = rectF.right = (float) 1.0;
		Bitmap bitmap = pdfPage.renderBitmap(pdfPage.getWidth(),
				pdfPage.getHeight(), rectF);
		return bitmap;
	}
}
