package com.example.functioninglogin.NavDrawerUIClasses;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;

import androidx.core.content.FileProvider;

import com.example.functioninglogin.HomePageUIClasses.GiftManagment.GiftItem;
import com.example.functioninglogin.HomePageUIClasses.GiftManagment.GiftMember;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PDFUtil {

    public static Uri createDetailedListPdf(Context context, String title, String description, List<GiftMember> members) {
        PdfDocument document = new PdfDocument();
        Paint paint = new Paint();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(350, 700, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        int y = 30;
        paint.setTextSize(16);
        canvas.drawText("🎁 " + title, 10, y, paint);
        y += 25;

        paint.setTextSize(12);
        canvas.drawText(description, 10, y, paint);
        y += 30;

        for (GiftMember member : members) {
            paint.setFakeBoldText(true);
            canvas.drawText("👤 " + member.name + " (" + member.role + ")", 10, y, paint);
            paint.setFakeBoldText(false);
            y += 20;

            for (GiftItem gift : member.gifts) {
                canvas.drawText("- " + gift.getName() + " ($" + gift.getPrice() + ")", 20, y, paint);
                y += 18;

                if (gift.getNotes() != null && !gift.getNotes().isEmpty()) {
                    canvas.drawText("  📝 " + gift.getNotes(), 25, y, paint);
                    y += 16;
                }
                if (gift.getWebsite() != null && !gift.getWebsite().isEmpty()) {
                    canvas.drawText("  🔗 " + gift.getWebsite(), 25, y, paint);
                    y += 16;
                }
            }
            y += 15;
        }

        document.finishPage(page);

        File pdfDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "pdfs");
        if (!pdfDir.exists()) pdfDir.mkdirs();

        File pdfFile = new File(pdfDir, title.replace(" ", "_") + ".pdf");

        try {
            FileOutputStream fos = new FileOutputStream(pdfFile);
            document.writeTo(fos);
            document.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", pdfFile);
    }
}
