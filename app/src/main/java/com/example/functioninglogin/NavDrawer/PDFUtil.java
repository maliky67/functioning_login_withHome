package com.example.functioninglogin.NavDrawer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;

import androidx.core.content.FileProvider;

import com.example.functioninglogin.HomePage.GiftManagment.GiftItem;
import com.example.functioninglogin.HomePage.GiftManagment.GiftMember;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PDFUtil {

    public static Uri createDetailedListPdf(Context context, String title, String budget, List<GiftMember> members) {
        PdfDocument document = new PdfDocument();
        Paint paint = new Paint();
        paint.setTextSize(12);
        paint.setAntiAlias(true);

        int pageWidth = 350;
        int pageHeight = 700;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        int x = 10;
        int y = 30;

        // 🔹 Title and Budget
        paint.setFakeBoldText(true);
        paint.setTextSize(16);
        canvas.drawText("🎁 " + title, x, y, paint);
        y += 25;
        paint.setFakeBoldText(false);

        paint.setTextSize(12);
        String formattedBudget = (budget == null || budget.isEmpty()) ? "0.00" : budget;
        canvas.drawText("💰 Total Budget: $" + formattedBudget, x, y, paint);
        y += 30;

        // 🔹 Member + Gift listing
        for (GiftMember member : members) {
            paint.setFakeBoldText(true);
            canvas.drawText("👤 " + member.name + " (" + member.role + ")", x, y, paint);
            paint.setFakeBoldText(false);
            y += 20;

            for (GiftItem gift : member.gifts) {
                // 🧠 Determine status emoji
                String emoji = "💡";
                String status = gift.getStatus() != null ? gift.getStatus().toLowerCase() : "idea";
                switch (status) {
                    case "bought": emoji = "💸"; break;
                    case "arrived": emoji = "📦"; break;
                    case "wrapped": emoji = "🎁"; break;
                }

                String price = gift.getPrice() != null ? gift.getPrice() : "0.00";
                String line = emoji + " " + gift.getName() + " - " + status + " 💵 $" + price;

                // 🔠 Wrap long lines
                for (String wrapped : wrapLine(line, paint, pageWidth - 20)) {
                    canvas.drawText(wrapped, x + 10, y, paint);
                    y += 16;
                }

                // 🔗 Website
                if (gift.getWebsite() != null && !gift.getWebsite().isEmpty()) {
                    for (String link : wrapLine("🔗 " + gift.getWebsite(), paint, pageWidth - 20)) {
                        canvas.drawText(link, x + 15, y, paint);
                        y += 14;
                    }
                }

                // 📝 Notes
                if (gift.getNotes() != null && !gift.getNotes().isEmpty()) {
                    for (String note : wrapLine("📝 " + gift.getNotes(), paint, pageWidth - 20)) {
                        canvas.drawText(note, x + 15, y, paint);
                        y += 14;
                    }
                }

                y += 6;
            }

            y += 10;
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

    // 🔁 Line wrapping utility
    private static List<String> wrapLine(String text, Paint paint, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (paint.measureText(currentLine + word + " ") > maxWidth) {
                lines.add(currentLine.toString().trim());
                currentLine = new StringBuilder();
            }
            currentLine.append(word).append(" ");
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString().trim());
        }

        return lines;
    }
}
