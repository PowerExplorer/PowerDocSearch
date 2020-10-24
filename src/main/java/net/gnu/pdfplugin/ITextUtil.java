package net.gnu.pdfplugin;

import com.itextpdf.text. DocumentException ;
import com.itextpdf.text.pdf. PdfDictionary ;
import com.itextpdf.text.pdf. PdfName ;
import com.itextpdf.text.pdf. PdfNumber;
import com.itextpdf.text.pdf. PdfReader;
import com.itextpdf.text.pdf. PdfStamper ;
import com.itextpdf.text. Font . FontFamily;
import java.io.File ;
import java.io.FileOutputStream ;
import java.io.IOException ;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.*;
import java.io.*;
import java.util.*;
import com.itextpdf.text.exceptions.*;
import com.itextpdf.text.pdf.parser.*;
import android.util.*;

public class ITextUtil {

	public static void close(final Closeable closable) {
		if (closable != null) {
			try {
				closable.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void flushClose(final OutputStream closable) {
		if (closable != null) {
			try {
				closable.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				closable.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void flushClose(final Writer closable) {
		if (closable != null) {
			try {
				closable.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				closable.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void extractPdfImages(final String src, final String dest)
	throws IOException {
		final PdfReader reader = new PdfReader(src);
		final int xrefSize = reader.getXrefSize();
		for (int i = 0; i < xrefSize; i++) {
			final PdfObject pdfobj = reader.getPdfObject(i);
			if (pdfobj == null || !pdfobj.isStream()) {
				continue;
			}
			final PdfStream stream = (PdfStream) pdfobj;
			// PdfObject pdfsubtype = stream.get(PdfName.SUBTYPE);
			final PdfObject pdfsubtype = stream.getDirectObject(PdfName.SUBTYPE);
			if (pdfsubtype != null && pdfsubtype.toString().equals(PdfName.IMAGE.toString())) {
				final byte[] img = PdfReader.getStreamBytesRaw((PRStream) stream);
				final File file = new File(String.format(dest + "-%1$05d", i) + ".jpg");
				final FileOutputStream fos = new FileOutputStream(file);
				final BufferedOutputStream bos = new BufferedOutputStream(fos);
				bos.write(img);
				flushClose(bos);
				flushClose(fos);
				Log.d("wrote", file.getAbsolutePath());
			}
		}
//		PdfReader reader = new PdfReader(src);
//		PdfObject obj;
//		for (int i = 1; i <= reader.getXrefSize(); i++) {
//			obj = reader.getPdfObject(i);
//			if (obj != null && obj.isStream()) {
//				PRStream stream = (PRStream) obj;
//				byte[] b;
//				try {
//					b = PdfReader.getStreamBytes(stream);
//				} catch (UnsupportedPdfException e) {
//					b = PdfReader.getStreamBytesRaw(stream);
//				}
//				FileOutputStream fos = new FileOutputStream(String.format(dest,
//						i));
//				fos.write(b);
//				fos.flush();
//				fos.close();
//			}
//		}
	}

	//	public static void parsePdfToText(String pdfFile, String txtFile) //pdfbox
//	throws IOException, PDFException, PDFSecurityException, InterruptedException {
//		Log.i("Source PDF:", pdfFile);
//		Log.i("Destination txtFile: ", txtFile);
//
//        // open the url
//        Document document = new Document();
//		File fileTmp = new File(txtFile + ".tmp");
//
//		document.setFile(pdfFile);
//		// create a file to write the extracted text to
//		BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fileTmp));
//
//		// Get text from the first page of the document, assuming that there
//		// is text to extract.
//		for (int pageNumber = 0, max = document.getNumberOfPages();
//			 pageNumber < max; pageNumber++) {
//			PageText pageText = document.getPageText(pageNumber);
//			Log.d("Extracting page text: ", pageNumber + ".");
//			if (pageText != null && pageText.getPageLines() != null) {
//				fileWriter.write(pageText.toString());
//			}
//		}
//		// close the writer
//		fileWriter.flush();
//		fileWriter.close();
//        // clean up resources
//        document.dispose();
//		File file = new File(txtFile);
//		file.delete();
//		fileTmp.renameTo(file);
//	}

	public static void parsePdfToText(final String pdfFile,
			final String txtFile) throws IOException {
		Log.i("Source PDF:", pdfFile);
		Log.i("Destination txtFile: ", txtFile);
		final PdfReader reader = new PdfReader(pdfFile);
		final String tmpTxt = txtFile + ".tmp";
		final PrintWriter out = new PrintWriter(new FileOutputStream(tmpTxt));

		for (int i = 1; i <= reader.getNumberOfPages(); i++) {
			out.println(PdfTextExtractor.getTextFromPage(reader, i,
														 new LocationTextExtractionStrategy()));
//			out.println(PdfTextExtractor.getTextFromPage(
//			 		reader, i, new SimpleTextExtractionStrategy()));
		}
		flushClose(out);
		final File file = new File(txtFile);
		file.delete();
		new File(tmpTxt).renameTo(file);
	}

	public static void pdfToText(final String pdfPath, 
			final String txtPath) throws IOException {

		final File txtFile = new File(txtPath);
		if (!txtFile.getParentFile().exists()) {
			txtFile.getParentFile().mkdirs();
		}

		if (!txtFile.exists()
			|| (txtFile.lastModified() < new File(pdfPath).lastModified())) {
			// try {
			// PDFBoxToHtml.convertToText(pdfPath, txtPath, null);
			// LOGGER.info("Used PDFBoxToHtml successfully");
			// } catch (Throwable t) {

			parsePdfToText(pdfPath, txtPath);
			Log.d("ITextUtil", "pdfToText successfully");
			// String command = "./lib/pdftohtml.exe \"" + pdfPath
			// + "\" \"" + txtPath + "\"";
			// Util.LOGGER.info(command);
			// Runtime.getRuntime().exec(command);
			// File f = new File(txtPath);
			// LOGGER.info("file: " + f);
			// File file2 = new File(txtPath.substring(0, txtPath.length()
			// - ".html".length())
			// + "s.html");
			// LOGGER.info("file2: " + file2);
			// if (file2.renameTo(f)) {
			// LOGGER.info("Rename successfully: "
			// + f.getAbsolutePath());
			// htmlFile = f;
			// } else {
			// htmlFile = file2;
			// LOGGER.info("Renaming to " + f.getAbsolutePath()
			// + " not OK");
			// }
			// LOGGER.info("Used pdftohtml.exe successfully");
			// // FileUtil.printInputStream(p.getInputStream(),
			// // p.getOutputStream(), null, null);
			// }
		} else {
			Log.d(pdfPath + " has already converted before to file : ", txtPath);
		}
	}
} 

/**
 * Example written by Bruno Lowagie in answer to:
 * http://stackoverflow.com/questions/27020542/rotating-pdf-90-degrees-
 using-itextsharp-in-c-sharp
 */
class Rotate90Degrees {
	public static final String SRC = "resources/pdfs/pages.pdf" ;
	public static final String DEST = "results/stamper/pages_rotated90degrees.pdf" ;
	public static void main (String [] args ) throws IOException,
	DocumentException {
		File file = new File (DEST );
		file. getParentFile (). mkdirs () ;
		new Rotate90Degrees (). manipulatePdf (SRC, DEST );
	}
	public void manipulatePdf (String src, String dest ) throws
	IOException, DocumentException {
		PdfReader reader = new PdfReader( src) ;
		int n = reader. getNumberOfPages ();
		PdfDictionary page ;
		PdfNumber rotate ;
		for ( int p = 1; p <= n ; p ++) {
			page = reader. getPageN (p );
			rotate = page. getAsNumber( PdfName .ROTATE );
			if ( rotate == null ) {
				page. put (PdfName .ROTATE , new PdfNumber( 90)) ;
			}
			else {
				page. put (PdfName .ROTATE , new PdfNumber(( rotate. intValue (
														   ) + 90) % 360 ));
			}
		}
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream
											(dest )) ;
		stamper. close () ;
		reader. close ();
	}
}


/**
 * Example written by Bruno Lowagie in answer to:
 * http://stackoverflow.com/questions/21871027/rotating-in-itextsharp-
 while-preserving-comment-location-orientation
 *
 * Example that shows how to scale an existing PDF using the UserUnit
 and how to remove the rotation of a page.
 */
class ScaleRotate {
	public static final String SRC = "resources/pdfs/pages.pdf" ;
	public static final String DEST = "results/stamper/pages_altered.pdf" ;
	public static void main (String [] args ) throws IOException,
	DocumentException {
		File file = new File (DEST );
		file. getParentFile (). mkdirs () ;
		new ScaleRotate (). manipulatePdf (SRC, DEST );
	}
	public void manipulatePdf (String src, String dest ) throws
	IOException, DocumentException {
		PdfReader reader = new PdfReader( src) ;
		int n = reader. getNumberOfPages ();
		PdfDictionary page ;
		for ( int p = 1; p <= n ; p ++) {
			page = reader. getPageN (p );
			if ( page. getAsNumber (PdfName . USERUNIT ) == null )
				page. put (PdfName .USERUNIT , new PdfNumber(2.5f ));
			page. remove (PdfName . ROTATE ) ;
		}
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream
											(dest )) ;
		stamper. close () ;
		reader. close ();
	}
}


/**
 * Example written by Bruno Lowagie in answer to:
 * http://stackoverflow.com/questions/25356302/shrink-pdf-pages-with-
 rotation-using-rectangle-in-existing-pdf
 */

class ShrinkPdf {
	public static final String SRC = "resources/pdfs/hero.pdf" ;
	public static final String DEST = "results/stamper/hero_shrink.pdf"
	;
	public static void main (String [] args ) throws IOException,
	DocumentException {
		File file = new File (DEST );
		file. getParentFile (). mkdirs () ;
		new ShrinkPdf() .manipulatePdf ( SRC, DEST ) ;
	}
	public void manipulatePdf (String src, String dest ) throws
	IOException, DocumentException {
		PdfReader reader = new PdfReader( src) ;
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream
											(dest )) ;
		int n = reader. getNumberOfPages ();
		PdfDictionary page ;
		PdfArray crop ;
		PdfArray media ;
		for ( int p = 1; p <= n ; p ++) {
			page = reader. getPageN (p );
			media = page. getAsArray (PdfName .CROPBOX );
			if ( media == null ) {
				media = page. getAsArray(PdfName . MEDIABOX );
			}
			crop = new PdfArray () ;
			crop. add (new PdfNumber( 0)) ;
			crop. add (new PdfNumber( 0)) ;
			crop. add (new PdfNumber( media. getAsNumber(2 ).floatValue () / 2
									 ));
			crop. add (new PdfNumber( media. getAsNumber(3 ).floatValue () / 2
									 ));
			page. put (PdfName . MEDIABOX , crop );
			page. put (PdfName . CROPBOX , crop );
			stamper. getUnderContent (p) .setLiteral( "\nq 0.5 0 0 0.5 0 0 cm\n q\n ");
			stamper. getOverContent ( p). setLiteral(" \nQ\n Q\n ") ;
		}
		stamper. close () ;
		reader. close ();
	}
}

/**
 * Example written by Bruno Lowagie in answer to:
 * http://stackoverflow.com/questions/25356302/shrink-pdf-pages-with-
 rotation-using-rectangle-in-existing-pdf
 */

class ShrinkPdf2 {
	public static final String SRC = "resources/pdfs/hero.pdf" ;
	public static final String DEST = "results/stamper/hero_shrink2.pdf";
	public static void main (String [] args ) throws IOException,
	DocumentException {
		File file = new File (DEST );
		file. getParentFile (). mkdirs () ;
		new ShrinkPdf2 ().manipulatePdf (SRC, DEST );
	}
	public void manipulatePdf (String src, String dest ) throws
	IOException, DocumentException {
		PdfReader reader = new PdfReader( src) ;
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream
											(dest )) ;
		int n = reader. getNumberOfPages ();
		float percentage = 0.8f ;
		for ( int p = 1; p <= n ; p ++) {
			float offsetX = ( reader. getPageSize( p). getWidth () * ( 1 -
				percentage )) / 2;
			float offsetY = ( reader. getPageSize( p). getHeight() * (1 -
				percentage )) / 2;
			stamper. getUnderContent (p) .setLiteral(
				String .format (" \nq %s 0 0 %s %s %s cm \n q\n ",
								percentage, percentage, offsetX, offsetY )) ;
			stamper. getOverContent ( p). setLiteral(" \nQ\n Q\n ") ;
		}
		stamper. close () ;
		reader. close ();
	}
}

/**
 * Example written by Bruno Lowagie in answer to
 * http://stackoverflow.com/questions/29152313/fix-the-orientation-of-a-
 pdf-in-order-to-scale-it
 */

/**
 * This solution is suboptimal as it throws away all interactivity.
 * If you want to keep the interactive elements (annotations, form
 fields,...),
 * you need to do much more work. If you don't need to scale down, but
 instead
 * only have to scale up, you should try the example that was written
 in
 * answer to http://stackoverflow.com/questions/21871027/rotating-in-
 itextsharp-while-preserving-comment-location-orientation
 */
class ScaleDown {
	public static final String SRC = "resources/pdfs/orientations.pdf" ;
	public static final String DEST = "results/events/scaled_down.pdf" ;
	public class ScaleEvent extends PdfPageEventHelper {
		protected float scale = 1;
		protected PdfDictionary pageDict ;
		public ScaleEvent( float scale ) {
			this .scale = scale ;
		}
		public void setPageDict ( PdfDictionary pageDict ) {
			this .pageDict = pageDict ;
		}

		public void onStartPage ( PdfWriter writer, Document document ) {
			writer. addPageDictEntry (PdfName .ROTATE , pageDict. getAsNumber
									  (PdfName .ROTATE )) ;
			writer. addPageDictEntry (PdfName .MEDIABOX , scaleDown( pageDict
																	.getAsArray( PdfName .MEDIABOX ) , scale ));
			writer. addPageDictEntry (PdfName .CROPBOX , scaleDown(pageDict.
																   getAsArray(PdfName . CROPBOX ), scale )) ;
		}
	}
	public static void main (String [] args ) throws IOException,
	DocumentException {
		File file = new File (DEST );
		file. getParentFile (). mkdirs () ;
		new ScaleDown() .manipulatePdf ( SRC, DEST ) ;
	}
	public void manipulatePdf (String src, String dest ) throws
	IOException, DocumentException {
		PdfReader reader = new PdfReader( src) ;
		float scale = 0.5f ;
		ScaleEvent event = new ScaleEvent (scale ) ;
		event. setPageDict( reader. getPageN ( 1));
		int n = reader. getNumberOfPages ();
		Document document = new Document ();
		PdfWriter writer = PdfWriter.getInstance (document, new
												  FileOutputStream (dest )) ;
		writer. setPageEvent (event );
		document. open () ;
		Image page ;
		for ( int p = 1; p <= n ; p ++) {
			page = Image .getInstance (writer. getImportedPage ( reader, p )) ;
			page. setAbsolutePosition( 0, 0);
			page. scalePercent (scale * 100 );
			document. add (page );
			if ( p < n) {
				event. setPageDict(reader. getPageN (p + 1 ));
			}
			document. newPage () ;
		}
		document. close () ;
	}
	public PdfArray scaleDown (PdfArray original, float scale ) {
		if (original == null )
			return null ;
		float width = original. getAsNumber (2). floatValue()
			- original. getAsNumber (0) .floatValue() ;
		float height = original. getAsNumber (3) .floatValue()
			- original. getAsNumber (1) .floatValue() ;
		return new PdfRectangle ( width * scale, height * scale ) ;
	}
}

/*
 * Example written by Bruno Lowagie in answer to:
 * http://stackoverflow.com/questions/30911216/how-to-re-arrange-pages-
 in-pdf-using-itext
 */
class ReorderPages {
	public static final String DEST = "results/stamper/reordered.pdf" ;
	public static void main (String [] args ) throws IOException,
	DocumentException {
		File file = new File (DEST );
		file. getParentFile (). mkdirs () ;
		new ReorderPages () .createPdf(DEST );
	}
	public void createPdf( String dest ) throws IOException,
	DocumentException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream() ;
		Document document = new Document ();
		PdfWriter.getInstance (document, baos ) ;
		document. open () ;
		for ( int i = 1; i < 17 ; i ++) {
			document. add (new Paragraph(String .format ("Page %s" , i ))) ;
			document. newPage () ;
		}
		document. close () ;
		PdfReader reader = new PdfReader( baos. toByteArray ()) ;
		int startToc = 13;
		int n = reader. getNumberOfPages ();
		reader. selectPages (String .format ("1,%s-%s, 2-%s, %s" , startToc,
											 n- 1, startToc - 1, n )) ;
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream
											(dest )) ;
		stamper. close () ;
	}
}

/**
 * Example written by Bruno Lowagie in answer to:
 * http://stackoverflow.com/questions/23280083/itextsharp-change-order-
 of-optional-content-groups
 */

class ChangeOCGOrder {
	public static final String SRC = "resources/pdfs/ocg.pdf" ;
	public static final String DEST = "results/stamper/ocg_reordered.pdf" ;
	public static void main (String [] args ) throws IOException,
	DocumentException {
		File file = new File (DEST );
		file. getParentFile (). mkdirs () ;
		new ChangeOCGOrder (). manipulatePdf (SRC, DEST );
	}
	public void manipulatePdf (String src, String dest ) throws
	IOException, DocumentException {
		PdfReader reader = new PdfReader( src) ;
		PdfDictionary catalog = reader. getCatalog() ;
		PdfDictionary ocProps = catalog. getAsDict(PdfName . OCPROPERTIES );
		PdfDictionary occd = ocProps. getAsDict(PdfName . D);
		PdfArray order = occd. getAsArray(PdfName . ORDER );
		PdfObject nestedLayers = order. getPdfObject (0) ;
		PdfObject nestedLayerArray = order. getPdfObject (1 );
		PdfObject groupedLayers = order. getPdfObject (2 );
		PdfObject radiogroup = order. getPdfObject ( 3);
		order. set (0, radiogroup ) ;
		order. set (1, nestedLayers );
		order. set (2, nestedLayerArray );
		order. set (3, groupedLayers );
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream
											(dest )) ;
		stamper. close () ;
		reader. close ();
	}
}
/**
 * Example written by Bruno Lowagie in answer to the following question
 :
 * http://stackoverflow.com/questions/24678640/itext-pdfdocument-page-
 size-inaccurate
 */

class StampHeader1 {
	public static final String SRC = "resources/pdfs/Right.pdf" ;
	public static final String DEST = "results/stamper/stamped_header1.pdf" ;
	public static void main (String [] args ) throws IOException,
	DocumentException {
		File file = new File (DEST );
		file. getParentFile (). mkdirs () ;
		new StampHeader1 () .manipulatePdf ( SRC, DEST ) ;
	}
	public void manipulatePdf (String src, String dest ) throws
	IOException, DocumentException {
		PdfReader reader = new PdfReader( src) ;
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream
											(dest )) ;
		Phrase header = new Phrase ("Copy" , new Font (FontFamily .HELVETICA
													   , 14 ));
		for ( int i = 1; i <= reader. getNumberOfPages (); i ++ ) {
			float x = reader. getPageSize( i).getWidth () / 2 ;
			float y = reader. getPageSize( i).getTop ( 20);
			ColumnText.showTextAligned (
				stamper. getOverContent (i) , Element . ALIGN_CENTER ,
				header, x, y, 0) ;
		}
		stamper. close () ;
		reader. close ();
	}
}

/**
 * Example written by Bruno Lowagie in answer to the following question
 :
 * http://stackoverflow.com/questions/24678640/itext-pdfdocument-page-
 size-inaccurate
 */

class StampHeader2 {
	public static final String SRC = "resources/pdfs/Wrong.pdf" ;
	public static final String DEST = "results/stamper/stamped_header2.pdf" ;
	public static void main (String [] args ) throws IOException,
	DocumentException {
		File file = new File (DEST );
		file. getParentFile (). mkdirs () ;
		new StampHeader2 () .manipulatePdf ( SRC, DEST ) ;
	}
	public void manipulatePdf (String src, String dest ) throws
	IOException, DocumentException {
		PdfReader reader = new PdfReader( src) ;
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream
											(dest )) ;
		stamper. setRotateContents (false );
		Phrase header = new Phrase ("Copy" , new Font (FontFamily .HELVETICA
													   , 14 ));
		for ( int i = 1; i <= reader. getNumberOfPages (); i ++ ) {
			float x = reader. getPageSize( i).getWidth () / 2 ;
			float y = reader. getPageSize( i).getTop ( 20);
			ColumnText.showTextAligned (
				stamper. getOverContent (i) , Element . ALIGN_CENTER ,
				header, x, y, 0) ;
		}
		stamper. close () ;
		reader. close ();
	}
}

class StampHeader3 {
	public static final String SRC = "resources/pdfs/Wrong.pdf" ;
	public static final String DEST = "results/stamper/stamped_header3.pdf" ;
	public static void main (String [] args ) throws IOException,
	DocumentException {
		File file = new File (DEST );
		file. getParentFile (). mkdirs () ;
		new StampHeader3 () .manipulatePdf ( SRC, DEST ) ;
	}
	public void manipulatePdf (String src, String dest ) throws
	IOException, DocumentException {
		PdfReader reader = new PdfReader( src) ;
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream
											(dest )) ;
		Phrase header = new Phrase ("Copy" , new Font (FontFamily .HELVETICA
													   , 14 ));
		float x, y ;
		for ( int i = 1; i <= reader. getNumberOfPages (); i ++ ) {
			System . out .println (reader. getPageRotation (i ));
			if ( reader. getPageRotation (i) % 180 == 0) {
				x = reader. getPageSize (i) .getWidth () / 2;
				y = reader. getPageSize (i) .getTop (20 );
			}
			else {
				System .out .println ("rotated" );
				x = reader. getPageSize (i) .getHeight() / 2 ;
				y = reader. getPageSize (i) .getRight ( 20);
			}
			ColumnText.showTextAligned (
				stamper. getOverContent (i) , Element . ALIGN_CENTER ,
				header, x, y, 0) ;
		}
		stamper. close () ;
		reader. close ();
	}
}

/**
 * Example written by Bruno Lowagie in answer to
 * http://stackoverflow.com/questions/28368317/itext-or-itextsharp-move-
 text-in-an-existing-pdf
 */

class CutAndPaste {
	/** The original PDF file. */
	public static final String SRC
	= "resources/pdfs/page229.pdf" ;
	/** The resulting PDF file. */
	public static final String DEST
	= "results/merge/page229_cut-paste.pdf" ;
	/**
	 * Manipulates a PDF file src with the file dest as result
	 * @param src the original PDF
	 * @param dest the resulting PDF
	 * @throws IOException
	 * @throws DocumentException
	 */
	public void manipulatePdf (String src, String dest )
	throws IOException , DocumentException {
// Creating a reader
		PdfReader reader = new PdfReader( src) ;
// step 1
		Rectangle pageSize = reader. getPageSize( 1);
		Rectangle toMove = new Rectangle( 100, 500 , 200, 600 );
		Document document = new Document (pageSize ) ;
// step 2
		PdfWriter writer
			= PdfWriter.getInstance (document, new FileOutputStream (dest )
									 );
// step 3
		document. open () ;
// step 4
		PdfImportedPage page = writer. getImportedPage (reader, 1);
		PdfContentByte cb = writer. getDirectContent ();
		PdfTemplate template1 = cb. createTemplate (pageSize. getWidth () ,
													pageSize. getHeight()) ;
		template1. rectangle(0 , 0 , pageSize. getWidth () , pageSize.
							 getHeight ()) ;
		template1. rectangle(toMove. getLeft (), toMove. getBottom(),
							 toMove. getWidth () , toMove. getHeight());
		template1. eoClip ();
		template1. newPath () ;
		template1. addTemplate (page, 0, 0);
		PdfTemplate template2 = cb. createTemplate (pageSize. getWidth () ,
													pageSize. getHeight()) ;
		template2. rectangle(toMove. getLeft (), toMove. getBottom(),
							 toMove. getWidth () , toMove. getHeight());
		template2. clip () ;
		template2. newPath () ;
		template2. addTemplate (page, 0, 0);
		cb. addTemplate (template1, 0, 0) ;
		cb. addTemplate (template2, -20 , -2) ;
// step 4
		document. close () ;
		reader. close ();
	}
	/**
	 * Main method.
	 * @param    args    no arguments needed
	 * @throws DocumentException
	 * @throws IOException
	 */
	public static void main (String [] args )
	throws IOException , DocumentException {
		File file = new File (DEST );
		file. getParentFile (). mkdirs () ;
		new CutAndPaste (). manipulatePdf (SRC, DEST );
	}
}


/**
 * Example written by Bruno Lowagie in answer to:
 * http://stackoverflow.com/questions/26773942/itext-crop-out-a-part-of-
 pdf-file
 */

class ClipPdf {
	public static final String SRC = "resources/pdfs/hero.pdf" ;
	public static final String DEST = "results/stamper/hero_clipped.pdf";
	public static void main (String [] args ) throws IOException,
	DocumentException {
		File file = new File (DEST );
		file. getParentFile (). mkdirs () ;
		new ClipPdf () .manipulatePdf ( SRC, DEST ) ;
	}
	public void manipulatePdf (String src, String dest ) throws
	IOException, DocumentException {
		PdfReader reader = new PdfReader( src) ;
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream
											(dest )) ;
		int n = reader. getNumberOfPages ();
		PdfDictionary page ;
		PdfArray media ;
		for ( int p = 1; p <= n ; p ++) {
			page = reader. getPageN (p );
			media = page. getAsArray (PdfName .CROPBOX );
			if ( media == null ) {
				media = page. getAsArray(PdfName . MEDIABOX );
			}
			float llx = media. getAsNumber (0). floatValue() + 200;
			float lly = media. getAsNumber (1). floatValue() + 200;
			float w = media. getAsNumber (2 ).floatValue() - media.
				getAsNumber (0 ).floatValue() - 400 ;
			float h = media. getAsNumber (3 ).floatValue() - media.
				getAsNumber (1 ).floatValue() - 400 ;
			String command = String . format (Locale .ROOT ,
											  " \nq %.2f %.2f %.2f %.2f re W n \nq\n ",
											  llx, lly, w, h ) ;
			stamper. getUnderContent (p) .setLiteral( command );
			stamper. getOverContent ( p). setLiteral(" \nQ\n Q\n ") ;
		}
		stamper. close () ;
		reader. close ();
	}
}

/**
 * This example was written by Bruno Lowagie in answer to the following
 StackOverflow question:
 * http://stackoverflow.com/questions/29775893/watermark-in-a-pdf-with-
 itext
 */
class AddExtraMargin {
	public static final String SRC = "resources/pdfs/primes.pdf" ;
	public static final String DEST = "results/stamper/primes_extra_margin.pdf" ;
	public static void main (String [] args ) throws IOException,
	DocumentException {
		File file = new File (DEST );
		file. getParentFile (). mkdirs () ;
		new AddExtraMargin (). manipulatePdf (SRC, DEST );
	}
	public void manipulatePdf (String src, String dest ) throws
	IOException, DocumentException {
		PdfReader reader = new PdfReader( src) ;
		int n = reader. getNumberOfPages ();
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream
											(dest )) ;
// properties
		PdfContentByte over ;
		PdfDictionary pageDict ;
		PdfArray mediabox ;
		float llx, lly, ury ;
// loop over every page
		for ( int i = 1; i <= n ; i ++) {
			pageDict = reader. getPageN ( i);
			mediabox = pageDict. getAsArray(PdfName . MEDIABOX );
			llx = mediabox. getAsNumber (0) .floatValue() ;
			lly = mediabox. getAsNumber (1) .floatValue() ;
			ury = mediabox. getAsNumber (3) .floatValue() ;
			mediabox. set (0 , new PdfNumber(llx - 36)) ;
			over = stamper. getOverContent (i);
			over. saveState() ;
			over. setColorFill (new GrayColor( 0.5f )) ;
			over. rectangle(llx - 36 , lly, 36, ury - llx );
			over. fill () ;
			over. restoreState () ;
		}
		stamper. close () ;
		reader. close ();
	}
}


/**
 * Example written by Bruno Lowagie.
 * This example will only work with iText 5.5.6 and higher (you also
 need the xtra package).
 */
//class MergeAndCount {
//	/** The original PDF file. */
//	public static final String SRC
//	= "resources/pdfs/Wrong.pdf" ;
//	/** The resulting PDF file. */
//	public static final String DEST
//	= "results/merge/pages_counted.pdf" ;
//	/**
//	 * Manipulates a PDF file src with the file dest as result
//	 * @param src the original PDF
//	 * @param dest the resulting PDF
//	 * @throws IOException
//	 * @throws DocumentException
//	 */
//	public void manipulatePdf (String src, String dest )
//	throws IOException , DocumentException {
//		PdfReader reader = new PdfReader( src) ;
//		SmartPdfSplitter splitter = new SmartPdfSplitter (reader );
//		int part = 1 ;
//		while (splitter. hasMorePages ()) {
//			splitter. split (new FileOutputStream ( "results/merge/part_" +
//												   part + ".pdf" ), 200000 ) ;
//			part ++;
//		}
//		reader. close ();
//	}
//	/**
//	 * Main method.
//	 * @param    args    no arguments needed
//	 * @throws DocumentException
//	 * @throws IOException
//	 */
//	public static void main (String [] args )
//	throws IOException , DocumentException {
////			LoggerFactory . getInstance() .setLogger(new SysoLogger()) ;
//		File file = new File (DEST );
//		file. getParentFile (). mkdirs () ;
//		new MergeAndCount () .manipulatePdf (SRC, DEST );
//	}
//}

/*
 * Example written in answer to:
 * http://stackoverflow.com/questions/33582245/extract-pdf-page-and-
 insert-into-existing-pdf
 */

class SuperImpose {
	public static final String SRC = "resources/pdfs/primes.pdf" ;
	public static final String [] EXTRA =
	{"resources/pdfs/hello.pdf" , "resources/pdfs/base_url.pdf" , "resources/pdfs/state.pdf" };
	public static final String DEST = "results/stamper/primes_superimpose.pdf" ;
	public static void main (String [] args ) throws IOException,
	DocumentException {
		File file = new File (DEST );
		file. getParentFile (). mkdirs () ;
		new SuperImpose (). manipulatePdf (SRC, DEST );
	}
	public void manipulatePdf (String src, String dest ) throws
	IOException, DocumentException {
		PdfReader reader = new PdfReader( src) ;
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream
											(dest )) ;
		PdfContentByte canvas = stamper. getUnderContent (1) ;
		PdfReader r ;
		PdfImportedPage page ;
		for ( String path : EXTRA ) {
			r = new PdfReader(path ) ;
			page = stamper. getImportedPage (r, 1) ;
			canvas. addTemplate( page, 0 , 0 );
			stamper. getWriter() .freeReader(r );
			r. close ();
		}
		stamper. close () ;
	}
}

/**
 * Example written by Bruno Lowagie.
 */

class TilingHero {
	/** The original PDF file. */
	public static final String SRC
	= "resources/pdfs/hero.pdf" ;
	/** The resulting PDF file. */
	public static final String DEST
	= "results/merge/superman.pdf" ;
	/**
	 * Manipulates a PDF file src with the file dest as result
	 * @param src the original PDF
	 * @param dest the resulting PDF
	 * @throws IOException
	 * @throws DocumentException
	 */
	public void manipulatePdf (String src, String dest )
	throws IOException , DocumentException {
// Creating a reader
		PdfReader reader = new PdfReader( src) ;
		Rectangle pagesize = reader. getPageSizeWithRotation ( 1);
		float width = pagesize. getWidth () ;
		float height = pagesize. getHeight() ;
// step 1
		Rectangle mediabox = new Rectangle(0, 3 * height, width, 4 *
										   height ) ;
		Document document = new Document (mediabox ) ;
// step 2
		PdfWriter writer
			= PdfWriter.getInstance (document, new FileOutputStream (dest )
									 );
// step 3
		document. open () ;
// step 4
		PdfContentByte content = writer. getDirectContent () ;
		PdfImportedPage page = writer. getImportedPage (reader, 1);
// adding the same page 16 times with a different offset
		for ( int i = 0; i < 16 ; ) {
			content. addTemplate (page, 4, 0, 0 , 4 , 0 , 0) ;
			i ++;
			mediabox = new Rectangle(
				( i % 4) * width, ( 4 - (i / 4 )) * height,
				(( i % 4) + 1) * width, (3 - (i / 4)) * height );
			document. setPageSize (mediabox );
			document. newPage () ;
		}
// step 4
		document. close () ;
		reader. close ();
	}
	/**
	 * Main method.
	 * @param    args    no arguments needed
	 * @throws DocumentException
	 * @throws IOException
	 */
	public static void main (String [] args )
	throws IOException , DocumentException {
		File file = new File (DEST );
		file. getParentFile (). mkdirs () ;
		new TilingHero ().manipulatePdf (SRC, DEST );
	}
}

/**
 * Example written by Bruno Lowagie in answer to:
 * http://stackoverflow.com/questions/32769493/tiling-with-itext-and-
 adding-margins
 */

class TileClipped {
	public static final String SRC = "resources/pdfs/hero.pdf" ;
	public static final String DEST = "results/stamper/hero_tiled_clipped.pdf" ;
	public static void main (String [] args ) throws IOException,
	DocumentException {
		File file = new File (DEST );
		file. getParentFile (). mkdirs () ;
		new TileClipped (). manipulatePdf (SRC, DEST );
	}
	public void manipulatePdf (String src, String dest )
	throws IOException , DocumentException {
		float margin = 30;
// Creating a reader
		PdfReader reader = new PdfReader( src) ;
		Rectangle rect = reader. getPageSizeWithRotation (1 );
		Rectangle pagesize = new Rectangle(rect. getWidth () + margin * 2 ,
										   rect. getHeight() + margin * 2);
// step 1
		Document document = new Document (pagesize ) ;
// step 2
		PdfWriter writer
			= PdfWriter.getInstance (document, new FileOutputStream (dest )
									 );
// step 3
		document. open () ;
// step 4
		PdfContentByte content = writer. getDirectContent () ;
		PdfImportedPage page = writer. getImportedPage (reader, 1);
// adding the same page 16 times with a different offset
		float x, y ;
		for ( int i = 0; i < 16 ; i ++) {
			x = -rect. getWidth () * ( i % 4) + margin ;
			y = rect. getHeight() * (i / 4 - 3 ) + margin ;
			content. rectangle(margin, margin, rect. getWidth () , rect.
							   getHeight ()) ;
			content. clip () ;
			content. newPath () ;
			content. addTemplate (page, 4, 0, 0 , 4 , x, y ) ;
			document. newPage () ;
		}
// step 4
		document. close () ;
		reader. close ();
	}
}

/*
 * Example written by Bruno Lowagie in answer to a question on
 StackOverflow:
 * http://stackoverflow.com/questions/27011829/divide-one-page-pdf-file-
 in-two-pages-pdf-file
 */

class TileInTwo {
	/** The original PDF file. */
	public static final String SRC
	= "resources/pdfs/united_states.pdf" ;
	/** The resulting PDF file. */
	public static final String DEST
	= "results/merge/unitedstates_tiled.pdf" ;
	public static void main (String [] args ) throws DocumentException ,
	IOException {
		File file = new File (DEST );
		file. getParentFile (). mkdirs () ;
		new TileInTwo() .manipulatePdf ( SRC, DEST ) ;
	}
	public void manipulatePdf (String src, String dest )
	throws IOException , DocumentException {
// Creating a reader
		PdfReader reader = new PdfReader( src) ;
		int n = reader. getNumberOfPages ();
// step 1
		Rectangle mediabox = new Rectangle(getHalfPageSize (reader.
															getPageSizeWithRotation (1))) ;
		Document document = new Document (mediabox ) ;
// step 2
		PdfWriter writer
			= PdfWriter.getInstance (document, new FileOutputStream (dest )
									 );
// step 3
		document. open () ;
// step 4
		PdfContentByte content = writer. getDirectContent () ;
		PdfImportedPage page ;
		int i = 1 ;
		while (true ) {
			page = writer. getImportedPage (reader, i );
			content. addTemplate (page, 0, -mediabox. getHeight());
			document. newPage () ;
			content. addTemplate (page, 0, 0);
			if ( ++i > n )
				break ;
			mediabox = new Rectangle(getHalfPageSize (reader.
													  getPageSizeWithRotation (i ))) ;
			document. setPageSize (mediabox );
			document. newPage () ;
		}
// step 5
		document. close () ;
		reader. close ();
	}
	public Rectangle getHalfPageSize (Rectangle pagesize ) {
		float width = pagesize. getWidth () ;
		float height = pagesize. getHeight() ;
		return new Rectangle( width, height / 2 );
	}
}

/*
 * Example written by Bruno Lowagie in answer to a question on
 StackOverflow:
 * http://stackoverflow.com/questions/27600809/divide-pdf-exact-equal-
 half-using-itextsharp
 */

class TileInTwo2 {
	/** The original PDF file. */
	public static final String SRC
	= "resources/pdfs/united_states.pdf" ;
	/** The resulting PDF file. */
	public static final String DEST
	= "results/merge/unitedstates_tiled2.pdf" ;
	public static void main (String [] args ) throws DocumentException ,
	IOException {
		File file = new File (DEST );
		file. getParentFile (). mkdirs () ;
		new TileInTwo2 ().manipulatePdf (SRC, DEST );
	}
	public void manipulatePdf (String src, String dest )
	throws IOException , DocumentException {
// Creating a reader
		PdfReader reader = new PdfReader( src) ;
		int n = reader. getNumberOfPages ();
// step 1
		Rectangle mediabox = new Rectangle(getHalfPageSize (reader.
															getPageSizeWithRotation (1))) ;
		Document document = new Document (mediabox ) ;
// step 2
		PdfWriter writer
			= PdfWriter.getInstance (document, new FileOutputStream (dest )
									 );
// step 3
		document. open () ;
// step 4
		PdfContentByte content = writer. getDirectContent () ;
		PdfImportedPage page ;
		int i = 1 ;
		while (true ) {
			page = writer. getImportedPage (reader, i );
			content. addTemplate (page, 0, 0);
			document. newPage () ;
			content. addTemplate (page, -mediabox. getWidth () , 0 );
			if ( ++i > n )
				break ;
			mediabox = new Rectangle(getHalfPageSize (reader.
													  getPageSizeWithRotation (i ))) ;
			document. setPageSize (mediabox );
			document. newPage () ;
		}
// step 5
		document. close () ;
		reader. close ();
	}
	public Rectangle getHalfPageSize (Rectangle pagesize ) {
		float width = pagesize. getWidth () ;
		float height = pagesize. getHeight() ;
		return new Rectangle( width / 2, height );
	}
}

/**
 * Example written by Bruno Lowagie in answer to a question on
 StackOverflow
 *
 * When concatenating documents, we add a named destination every time
 * a new document is started. After we've finished merging, we add an
 extra
 * page with the table of contents and links to the named destinations.
 */
class NUpWithLink {
	public static final String SRC1 = "resources/pdfs/links1.pdf" ;
	public static final String SRC2 = "resources/pdfs/links2.pdf" ;
	public static final String DEST = "results/merge/nup_links.pdf" ;
	public static void main (String [] args ) throws IOException,
	DocumentException {
		File file = new File (DEST );
		file. getParentFile (). mkdirs () ;
		new NUpWithLink (). createPdf(DEST ) ;
	}
	public void createPdf( String filename ) throws IOException ,
	DocumentException {
		Document document = new Document () ;
		PdfWriter writer = PdfWriter.getInstance (document, new
												  FileOutputStream ( filename )) ;
		float W = PageSize .A4 .getWidth () / 2;
		float H = PageSize .A4 .getHeight() / 2;
		document. open ();
		int firstPage = 1;
		String [] files = new String []{ SRC1, SRC2 };
		PdfContentByte cb = writer. getDirectContent () ;
		for (int i = 0; i < files. length ; i++ ) {
			PdfReader currentReader = new PdfReader(files [i ]);
			currentReader. consolidateNamedDestinations() ;
			for (int page = 1 ; page <= currentReader. getNumberOfPages () ;
			page ++ ) {
				PdfImportedPage importedPage = writer. getImportedPage (
					currentReader, page );
				float a = 0.5f ;
				float e = (page % 2 == 0) ? W : 0 ;
				float f = (page % 4 == 1 || page % 4 == 2) ? H : 0 ;
				ArrayList <PdfAnnotation . PdfImportedLink > links =
					currentReader. getLinks ( page );
				cb. addTemplate( importedPage, a, 0, 0, a, e, f );
				for (int j = 0; j < links. size (); j++ ) {
					PdfAnnotation . PdfImportedLink link = (PdfAnnotation .
						PdfImportedLink ) links. get( j);
					if (link. isInternal()) {
						int dPage = link. getDestinationPage();
						int newDestPage = (dPage -1 )/4 + firstPage;
						float ee = (dPage % 2 == 0) ? W : 0;
						float ff = (dPage % 4 == 1 || dPage % 4 == 2 ) ?
							H : 0;
						link. setDestinationPage(newDestPage );
						link. transformDestination (a, 0, 0, a, ee, ff ) ;
					}
					link. transformRect (a, 0, 0, a, e, f );
					writer. addAnnotation (link. createAnnotation ( writer ));
				}
				if (page % 4 == 0)
					document. newPage ();
			}
			if ( i < files. length - 1 )
				document. newPage ();
			firstPage += (currentReader. getNumberOfPages ()+ 3)/4 ;
		}
		document. close ();
	}
}

/**
 * This example was written by Bruno Lowagie in answer to a question by
 a customer.
 */

class TransparentWatermark {
	public static final String SRC = "resources/pdfs/hero.pdf" ;
	public static final String DEST = "results/stamper/hero_watermarked.pdf" ;
	public static void main (String [] args ) throws IOException,
	DocumentException {
		File file = new File (DEST );
		file. getParentFile (). mkdirs () ;
		new TransparentWatermark (). manipulatePdf (SRC, DEST );
	}
	public void manipulatePdf (String src, String dest ) throws
	IOException, DocumentException {
		PdfReader reader = new PdfReader( src) ;
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream
											(dest )) ;
		PdfContentByte under = stamper. getUnderContent ( 1);
		Font f = new Font (FontFamily .HELVETICA, 15) ;
		Phrase p = new Phrase ( "This watermark is added UNDER the existing content" , f );
		ColumnText. showTextAligned ( under, Element .ALIGN_CENTER , p, 297 ,
									 550 , 0) ;
		PdfContentByte over = stamper. getOverContent (1) ;
		p = new Phrase ( "This watermark is added ON TOP OF the existing content" , f );
		ColumnText. showTextAligned ( over, Element .ALIGN_CENTER , p, 297 ,
									 500 , 0) ;
		p = new Phrase ( "This TRANSPARENT watermark is added ON TOP OF the existing content" , f );
		over. saveState();
		PdfGState gs1 = new PdfGState() ;
		gs1. setFillOpacity (0.5f ) ;
		over. setGState(gs1 );
		ColumnText. showTextAligned ( over, Element .ALIGN_CENTER , p, 297 ,
									 450 , 0) ;
		over. restoreState() ;
		stamper. close () ;
		reader. close ();
	}
}

/**
 * This example was written by Bruno Lowagie in answer to the following
 StackOverflow question:
 * http://stackoverflow.com/questions/29560373/watermark-pdfs-using-text
 -or-images-in-java
 */

class TransparentWatermark2 {
	public static final String SRC = "resources/pdfs/pages.pdf" ;
	public static final String DEST = "results/stamper/pages_watermarked.pdf" ;
	public static final String IMG = "resources/images/itext.png" ;
	public static void main (String [] args ) throws IOException,
	DocumentException {
		File file = new File (DEST );
		file. getParentFile (). mkdirs () ;
		new TransparentWatermark2 () .manipulatePdf ( SRC, DEST ) ;
	}
	public void manipulatePdf (String src, String dest ) throws
	IOException, DocumentException {
		PdfReader reader = new PdfReader( src) ;
		int n = reader. getNumberOfPages ();
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream
											(dest )) ;
// text watermark
		Font f = new Font (FontFamily .HELVETICA, 30) ;
		Phrase p = new Phrase ( "My watermark (text)" , f );
// image watermark
		Image img = Image .getInstance (IMG ) ;
		float w = img. getScaledWidth ();
		float h = img. getScaledHeight () ;
// transparency
		PdfGState gs1 = new PdfGState() ;
		gs1. setFillOpacity (0.5f ) ;
// properties
		PdfContentByte over ;
		Rectangle pagesize ;
		float x, y ;
// loop over every page
		for ( int i = 1; i <= n ; i ++) {
			pagesize = reader. getPageSizeWithRotation ( i);
			x = (pagesize. getLeft () + pagesize. getRight ()) / 2 ;
			y = (pagesize. getTop () + pagesize. getBottom()) / 2;
			over = stamper. getOverContent (i);
			over. saveState() ;
			over. setGState(gs1 ) ;
			if ( i % 2 == 1 )
				ColumnText.showTextAligned (over, Element . ALIGN_CENTER , p
											, x, y, 0) ;
			else
				over. addImage (img, w, 0 , 0 , h, x - ( w / 2), y - (h / 2)
								);
			over. restoreState () ;
		}
		stamper. close () ;
		reader. close ();
	}
}

/**
 * This example was written by Bruno Lowagie in answer to the following
 StackOverflow question:
 * http://stackoverflow.com/questions/29560373/watermark-pdfs-using-text
 -or-images-in-java
 */

class TransparentWatermark3 {
	public static final String SRC = "resources/pdfs/pages.pdf" ;
	public static final String DEST = "results/stamper/pages_watermarked3.pdf" ;
	public static final String IMG = "resources/images/itext.png" ;
	public static void main (String [] args ) throws IOException,
	DocumentException {
		File file = new File (DEST );
		file. getParentFile (). mkdirs () ;
		new TransparentWatermark3 () .manipulatePdf ( SRC, DEST ) ;
	}
	public void manipulatePdf (String src, String dest ) throws
	IOException, DocumentException {
		PdfReader reader = new PdfReader( src) ;
		int n = reader. getNumberOfPages ();
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream
											(dest )) ;
		stamper. setRotateContents (false );
// text watermark
		Font f = new Font (FontFamily .HELVETICA, 30) ;
		Phrase p = new Phrase ( "My watermark (text)" , f );
// image watermark
		Image img = Image .getInstance (IMG ) ;
		float w = img. getScaledWidth ();
		float h = img. getScaledHeight () ;
// transparency
		PdfGState gs1 = new PdfGState() ;
		gs1. setFillOpacity (0.5f ) ;
// properties
		PdfContentByte over ;
		Rectangle pagesize ;
		float x, y ;
// loop over every page
		for ( int i = 1; i <= n ; i ++) {
			pagesize = reader. getPageSize (i);
			x = (pagesize. getLeft () + pagesize. getRight ()) / 2 ;
			y = (pagesize. getTop () + pagesize. getBottom()) / 2;
			over = stamper. getOverContent (i);
			over. saveState() ;
			over. setGState(gs1 ) ;
			if ( i % 2 == 1 )
				ColumnText.showTextAligned (over, Element . ALIGN_CENTER , p
											, x, y, 0) ;
			else
				over. addImage (img, w, 0 , 0 , h, x - ( w / 2), y - (h / 2)
								);
			over. restoreState () ;
		}
		stamper. close () ;
		reader. close ();
	}
}

/**
 * This example was written by Bruno Lowagie in answer to
 * http://stackoverflow.com/questions/36511649
 */
class WatermarkToTheSide {
	public static final String SRC = "resources/pdfs/pages.pdf" ;
	public static final String DEST = "results/stamper/side_watermark.pdf" ;
	public static void main (String [] args ) throws IOException,
	DocumentException {
		File file = new File (DEST );
		file. getParentFile (). mkdirs () ;
		new WatermarkToTheSide() .manipulatePdf ( SRC, DEST ) ;
	}
	public void manipulatePdf (String src, String dest ) throws
	IOException, DocumentException {
		PdfReader reader = new PdfReader( src) ;
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream
											(dest )) ;
		int n = reader. getNumberOfPages ();
		PdfContentByte canvas ;
		Rectangle pageSize ;
		float x, y ;
		for ( int p = 1; p <= n ; p ++) {
			pageSize = reader. getPageSizeWithRotation ( p);
// left of the page
			x = pageSize. getLeft ();
// middle of the height
			y = (pageSize. getTop () + pageSize. getBottom()) / 2;
// getting the canvas covering the existing content
			canvas = stamper. getOverContent (p );
// adding some lines to the left
			ColumnText.showTextAligned (canvas, Element .ALIGN_CENTER ,
										new Phrase ("This is some extra text added to the left of the page" ) ,
										x + 18 , y, 90 );
			ColumnText.showTextAligned (canvas, Element .ALIGN_CENTER ,
										new Phrase ("This is some more text added to the left of the page" ),
										x + 34 , y, 90 );
		}
		stamper. close () ;
	}
}

/**
 * Example written by Bruno Lowagie in answer to the following question
 :
 * http://stackoverflow.com/questions/30286601/extracting-an-embedded-
 object-from-a-pdf
 */
class ExtractStreams {
	public static final String SRC = "resources/pdfs/image.pdf" ;
	public static final String DEST = "results/parse/stream%s" ;
	public static void main (String [] args ) throws IOException {
		File file = new File (DEST );
		file. getParentFile (). mkdirs () ;
		new ExtractStreams (). parse (SRC, DEST );
	}
	public void parse (String src, String dest ) throws IOException {
		PdfReader reader = new PdfReader( src) ;
		PdfObject obj ;
		for ( int i = 1; i <= reader. getXrefSize () ; i ++) {
			obj = reader. getPdfObject ( i);
			if ( obj != null && obj. isStream ()) {
				PRStream stream = (PRStream )obj ;
				byte [] b;
				try {
					b = PdfReader. getStreamBytes ( stream ) ;
				}
				catch ( UnsupportedPdfException e ) {
					b = PdfReader. getStreamBytesRaw (stream ) ;
				}
				FileOutputStream fos = new FileOutputStream ( String .
															 format ( dest, i ));
				fos. write ( b);
				fos. flush () ;
				fos. close () ;
			}
		}
	}
}

/*
 * Example written by Bruno Lowagie in answer to:
 * http://stackoverflow.com/questions/24506830/can-we-use-text-
 extraction-strategy-after-applying-location-extraction-strategy
 */

class ParseCustom {
	public static final String SRC = "resources/pdfs/nameddestinations.pdf" ;
	class FontRenderFilter extends RenderFilter {
		public boolean allowText (TextRenderInfo renderInfo ) {
			String font = renderInfo. getFont () .getPostscriptFontName () ;
			return font. endsWith ("Bold" ) || font. endsWith ("Oblique" );
		}
	}
	public static void main (String [] args ) throws IOException
	{
		new ParseCustom (). parse ( SRC) ;
	}
	public void parse (String filename ) throws IOException {
		PdfReader reader = new PdfReader( filename );
		Rectangle rect = new Rectangle(36 , 750 , 559 , 806 );
		RenderFilter regionFilter = new RegionTextRenderFilter ( rect );
		FontRenderFilter fontFilter = new FontRenderFilter () ;
		TextExtractionStrategy strategy = new FilteredTextRenderListener
		(
			new LocationTextExtractionStrategy (), regionFilter,
			fontFilter );
		System .out .println ( PdfTextExtractor .getTextFromPage (reader, 1,
																  strategy )) ;
		reader. close ();
	}
}

/*
 * Example written by Bruno Lowagie in answer to:
 * http://stackoverflow.com/questions/26670919/itextsharp-diacritic-
 chars
 */
class ParseCzech {
	public static final String SRC = "resources/pdfs/czech.pdf" ;
	public static final String DEST = "results/parse/czech.txt" ;
	public static void main (String [] args ) throws IOException {
		File file = new File (DEST );
		file. getParentFile (). mkdirs () ;
		new ParseCzech ().parse (SRC );
	}
	public void parse (String filename ) throws IOException {
		PdfReader reader = new PdfReader( filename );
		FileOutputStream fos = new FileOutputStream (DEST );
		for ( int page = 1; page <= 1; page ++ ) {
			fos. write ( PdfTextExtractor .getTextFromPage (reader, page ).
						getBytes ("UTF-8" )) ;
		}
		fos. flush ();
		fos. close ();
	}
}

	/*
	 * Example written by Bruno Lowagie in answer to:
	 * http://stackoverflow.com/questions/27905740/remove-text-occurrences-
	 contained-in-a-specified-area-with-itext
	 */
	
//	class RemoveContentInRectangle {
//		public static final String SRC = "resources/pdfs/page229.pdf" ;
//		public static final String DEST = "results/parse/page229_removed_content.pdf" ;
//		public static void main (String [] args ) throws IOException,
//		DocumentException {
//			File file = new File (DEST );
//			file. getParentFile (). mkdirs () ;
//			new RemoveContentInRectangle () .manipulatePdf (SRC, DEST );
//		}
//		public void manipulatePdf (String src, String dest ) throws
//		IOException, DocumentException {
//			PdfReader reader = new PdfReader( src) ;
//			PdfStamper stamper = new PdfStamper(reader, new FileOutputStream
//												(dest )) ;
//			ArrayList < PdfCleanUpLocation> cleanUpLocations = new ArrayList<
//				PdfCleanUpLocation > ();
//			cleanUpLocations. add ( new PdfCleanUpLocation(1, new Rectangle( 97f
//																			, 405f, 480f, 445f ), BaseColor.GRAY )) ;
//			PdfCleanUpProcessor cleaner = new PdfCleanUpProcessor(
//				cleanUpLocations, stamper ) ;
//			cleaner. cleanUp ();
//			stamper. close () ;
//			reader. close ();
//		}
//	}
	
	/*
	 * Example written by Bruno Lowagie in answer to:
	 * http://stackoverflow.com/questions/24037282/any-way-to-create-
	 redactions
	 */
//	class RemoveRedactedContent {
//		public static final String SRC = "resources/pdfs/page229_redacted.pdf" ;
//		public static final String DEST = "results/parse/page229_apply_redacted.pdf" ;
//		public static void main (String [] args ) throws IOException,
//		DocumentException {
//			File file = new File (DEST );
//			file. getParentFile (). mkdirs () ;
//			new RemoveRedactedContent () .manipulatePdf ( SRC, DEST ) ;
//		}
//		public void manipulatePdf (String src, String dest ) throws
//		IOException, DocumentException {
//			PdfReader reader = new PdfReader( src) ;
//			PdfStamper stamper = new PdfStamper(reader, new FileOutputStream
//												(dest )) ;
//			PdfCleanUpProcessor cleaner = new PdfCleanUpProcessor(stamper );
//			cleaner. cleanUp ();
//			stamper. close () ;
//			reader. close ();
//		}
//	}


