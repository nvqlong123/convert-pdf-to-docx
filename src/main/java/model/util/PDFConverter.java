package model.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.util.Units;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class PDFConverter {

    public static void convertPDFtoDoc(String pdfPath, String docPath) throws IOException {
        PDDocument pdfDocument = null;
        XWPFDocument docxDocument = new XWPFDocument();

        try {
            pdfDocument = PDDocument.load(new File(pdfPath));
            int totalPages = pdfDocument.getNumberOfPages();

            System.out.println("Converting " + totalPages + " pages...");

            // Xử lý từng trang
            for (int pageNum = 0; pageNum < totalPages; pageNum++) {
                System.out.println("Processing page " + (pageNum + 1) + "...");

                PDPage page = pdfDocument.getPage(pageNum);

                // 1. Extract và sort text với vị trí
                List<TextBlock> textBlocks = extractTextWithPositions(pdfDocument, pageNum);

                // 2. Extract images
                List<ImageBlock> imageBlocks = extractImages(page, pageNum);

                // 3. Merge và sort tất cả elements theo vị trí Y
                List<ContentBlock> allBlocks = new ArrayList<>();
                allBlocks.addAll(textBlocks);
                allBlocks.addAll(imageBlocks);

                // Sort theo Y position (top to bottom)
                allBlocks.sort(Comparator.comparingDouble(b -> b.yPosition));

                // 4. Tạo content trong DOCX
                for (ContentBlock block : allBlocks) {
                    if (block instanceof TextBlock) {
                        TextBlock textBlock = (TextBlock) block;
                        createTextParagraph(docxDocument, textBlock);
                    } else if (block instanceof ImageBlock) {
                        ImageBlock imageBlock = (ImageBlock) block;
                        insertImage(docxDocument, imageBlock);
                    }
                }

                // Page break (trừ trang cuối)
                if (pageNum < totalPages - 1) {
                    XWPFParagraph pageBreak = docxDocument.createParagraph();
                    pageBreak.setPageBreak(true);
                }
            }

            // Lưu file
            try (FileOutputStream out = new FileOutputStream(docPath)) {
                docxDocument.write(out);
            }

            System.out.println("Conversion completed: " + docPath);

        } finally {
            if (pdfDocument != null) {
                pdfDocument.close();
            }
            docxDocument.close();
        }
    }

    // Extract text với vị trí chính xác và xử lý space
    private static List<TextBlock> extractTextWithPositions(PDDocument document, int pageNum) throws IOException {
        SmartTextStripper stripper = new SmartTextStripper();
        stripper.setStartPage(pageNum + 1);
        stripper.setEndPage(pageNum + 1);
        stripper.setSortByPosition(true);

        stripper.getText(document);
        return stripper.getTextBlocks();
    }

    // Extract images từ page
    private static List<ImageBlock> extractImages(PDPage page, int pageNum) {
        List<ImageBlock> images = new ArrayList<>();

        try {
            PDResources resources = page.getResources();
            if (resources == null) {
                return images;
            }

            int imageIndex = 0;
            for (COSName name : resources.getXObjectNames()) {
                if (resources.isImageXObject(name)) {
                    try {
                        PDImageXObject image = (PDImageXObject) resources.getXObject(name);
                        BufferedImage bufferedImage = image.getImage();

                        // Ước lượng vị trí Y (giữa trang)
                        float yPosition = 400 * imageIndex; // Spread images vertically

                        ImageBlock imgBlock = new ImageBlock(bufferedImage, yPosition);
                        images.add(imgBlock);
                        imageIndex++;

                        System.out.println("Found image on page " + (pageNum + 1));
                    } catch (Exception e) {
                        System.err.println("Error extracting image: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing images: " + e.getMessage());
        }

        return images;
    }

    // Tạo paragraph từ TextBlock
    private static void createTextParagraph(XWPFDocument document, TextBlock block) {
        if (block.text.trim().isEmpty()) {
            document.createParagraph(); // Empty line
            return;
        }

        XWPFParagraph paragraph = document.createParagraph();

        // Set indentation
        if (block.indentLevel > 0) {
            paragraph.setIndentationLeft(block.indentLevel * 200);
        }

        // Set alignment
        switch (block.alignment) {
            case CENTER:
                paragraph.setAlignment(ParagraphAlignment.CENTER);
                break;
            case RIGHT:
                paragraph.setAlignment(ParagraphAlignment.RIGHT);
                break;
            case JUSTIFY:
                paragraph.setAlignment(ParagraphAlignment.BOTH);
                break;
            default:
                paragraph.setAlignment(ParagraphAlignment.LEFT);
        }

        // Set spacing
        if (block.hasSpacingBefore) {
            paragraph.setSpacingBefore(240);
        }

        // Create run
        XWPFRun run = paragraph.createRun();
        run.setText(block.text);
        run.setFontFamily("Times New Roman");

        // Set font size
        int fontSize = block.fontSize > 0 ? (int) Math.round(block.fontSize) : 11;
        run.setFontSize(fontSize);

        // Set bold for headings
        if (block.isHeading) {
            run.setBold(true);
        }
    }

    // Insert image vào document
    private static void insertImage(XWPFDocument document, ImageBlock imageBlock) {
        try {
            XWPFParagraph paragraph = document.createParagraph();
            paragraph.setAlignment(ParagraphAlignment.CENTER);

            XWPFRun run = paragraph.createRun();

            // Convert BufferedImage to ByteArray
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(imageBlock.image, "png", baos);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

            // Tính toán kích thước (scale down nếu quá lớn)
            int width = imageBlock.image.getWidth();
            int height = imageBlock.image.getHeight();

            // Max width: 6 inches (~ 15cm), max height: 8 inches
            int maxWidth = Units.toEMU(6 * 72); // 6 inches in EMU
            int maxHeight = Units.toEMU(8 * 72);

            int finalWidth = Units.toEMU(width);
            int finalHeight = Units.toEMU(height);

            // Scale down if needed
            if (finalWidth > maxWidth || finalHeight > maxHeight) {
                double scale = Math.min(
                        (double) maxWidth / finalWidth,
                        (double) maxHeight / finalHeight
                );
                finalWidth = (int) (finalWidth * scale);
                finalHeight = (int) (finalHeight * scale);
            }

            run.addPicture(bais, XWPFDocument.PICTURE_TYPE_PNG, "image.png",
                    finalWidth, finalHeight);

            System.out.println("Inserted image: " + width + "x" + height);

        } catch (Exception e) {
            System.err.println("Error inserting image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== HELPER CLASSES ====================

    // Base class cho tất cả content blocks
    static abstract class ContentBlock {
        float yPosition;

        ContentBlock(float y) {
            this.yPosition = y;
        }
    }

    // Text block với full formatting info
    static class TextBlock extends ContentBlock {
        String text;
        float xPosition;
        float fontSize;
        int indentLevel;
        TextAlignment alignment;
        boolean hasSpacingBefore;
        boolean isHeading;

        TextBlock(String text, float x, float y, float fontSize) {
            super(y);
            this.text = text;
            this.xPosition = x;
            this.fontSize = fontSize;
            this.indentLevel = 0;
            this.alignment = TextAlignment.LEFT;
            this.hasSpacingBefore = false;
            this.isHeading = false;
        }
    }

    // Image block
    static class ImageBlock extends ContentBlock {
        BufferedImage image;

        ImageBlock(BufferedImage image, float y) {
            super(y);
            this.image = image;
        }
    }

    enum TextAlignment {
        LEFT, CENTER, RIGHT, JUSTIFY
    }

    // ==================== SMART TEXT STRIPPER ====================

    static class SmartTextStripper extends PDFTextStripper {
        private List<TextBlock> textBlocks;
        private List<CharacterInfo> characters;
        private float pageWidth;
        private float pageHeight;

        public SmartTextStripper() throws IOException {
            super();
            textBlocks = new ArrayList<>();
            characters = new ArrayList<>();
        }

        @Override
        protected void startPage(PDPage page) throws IOException {
            super.startPage(page);
            pageWidth = page.getMediaBox().getWidth();
            pageHeight = page.getMediaBox().getHeight();
            characters.clear();
        }

        @Override
        protected void writeString(String text, List<TextPosition> positions) throws IOException {
            // Lưu thông tin từng ký tự
            for (TextPosition position : positions) {
                CharacterInfo charInfo = new CharacterInfo(
                        position.getUnicode(),
                        position.getX(),
                        position.getY(),
                        position.getWidth(),
                        position.getFontSizeInPt()
                );
                characters.add(charInfo);
            }
        }

        @Override
        protected void endPage(PDPage page) throws IOException {
            super.endPage(page);

            if (characters.isEmpty()) {
                return;
            }

            // Sort characters theo Y rồi X
            characters.sort((a, b) -> {
                int yCompare = Float.compare(a.y, b.y);
                if (Math.abs(a.y - b.y) < 2) { // Same line threshold
                    return Float.compare(a.x, b.x);
                }
                return yCompare;
            });

            // Group thành lines
            List<List<CharacterInfo>> lines = groupIntoLines(characters);

            // Convert lines thành TextBlocks
            float lastY = -1;
            for (List<CharacterInfo> line : lines) {
                if (line.isEmpty()) continue;

                // Build text với space detection
                StringBuilder lineText = new StringBuilder();
                float lineX = line.get(0).x;
                float lineY = line.get(0).y;
                float fontSize = line.get(0).fontSize;

                for (int i = 0; i < line.size(); i++) {
                    CharacterInfo ch = line.get(i);

                    // Phát hiện space giữa các ký tự
                    if (i > 0) {
                        CharacterInfo prevCh = line.get(i - 1);
                        float gap = ch.x - (prevCh.x + prevCh.width);

                        // Nếu khoảng cách > 20% của font size, thêm space
                        if (gap > fontSize * 0.2) {
                            lineText.append(" ");
                        }
                    }

                    lineText.append(ch.character);
                }

                String text = lineText.toString();

                // Tạo TextBlock
                TextBlock block = new TextBlock(text, lineX, lineY, fontSize);

                // Tính indent level
                block.indentLevel = (int) (lineX / 36); // 36pt per indent

                // Phát hiện alignment
                float textWidth = estimateTextWidth(text, fontSize);
                float centerX = pageWidth / 2;
                float rightMargin = pageWidth - 72; // 1 inch margin

                if (Math.abs(lineX + textWidth / 2 - centerX) < 50) {
                    block.alignment = TextAlignment.CENTER;
                } else if (lineX > pageWidth * 0.6) {
                    block.alignment = TextAlignment.RIGHT;
                } else {
                    block.alignment = TextAlignment.LEFT;
                }

                // Phát hiện spacing
                if (lastY != -1 && (lineY - lastY) > fontSize * 1.8) {
                    block.hasSpacingBefore = true;
                }

                // Phát hiện heading
                block.isHeading = detectHeading(text, fontSize);

                textBlocks.add(block);
                lastY = lineY;
            }
        }

        // Group characters thành lines
        private List<List<CharacterInfo>> groupIntoLines(List<CharacterInfo> chars) {
            List<List<CharacterInfo>> lines = new ArrayList<>();

            if (chars.isEmpty()) {
                return lines;
            }

            List<CharacterInfo> currentLine = new ArrayList<>();
            float currentY = chars.get(0).y;

            for (CharacterInfo ch : chars) {
                if (Math.abs(ch.y - currentY) < 2) { // Same line
                    currentLine.add(ch);
                } else {
                    // New line
                    if (!currentLine.isEmpty()) {
                        lines.add(new ArrayList<>(currentLine));
                    }
                    currentLine.clear();
                    currentLine.add(ch);
                    currentY = ch.y;
                }
            }

            // Add last line
            if (!currentLine.isEmpty()) {
                lines.add(currentLine);
            }

            return lines;
        }

        private float estimateTextWidth(String text, float fontSize) {
            return text.length() * fontSize * 0.5f;
        }

        private boolean detectHeading(String text, float fontSize) {
            String trimmed = text.trim();

            if (trimmed.isEmpty() || trimmed.length() > 120) {
                return false;
            }

            boolean isAllCaps = trimmed.equals(trimmed.toUpperCase()) &&
                    trimmed.matches(".*[A-ZÀÁẢÃẠĂẰẮẲẴẶÂẦẤẨẪẬÈÉẺẼẸÊỀẾỂỄỆÌÍỈĨỊÒÓỎÕỌÔỒỐỔỖỘƠỜỚỞỠỢÙÚỦŨỤƯỪỨỬỮỰỲÝỶỸỴĐ].*");
            boolean hasLargeFont = fontSize > 12;
            boolean isTitleCase = trimmed.matches("^[A-ZÀÁẢÃẠĂẰẮẲẴẶÂẦẤẨẪẬÈÉẺẼẸÊỀẾỂỄỆÌÍỈĨỊÒÓỎÕỌÔỒỐỔỖỘƠỜỚỞỠỢÙÚỦŨỤƯỪỨỬỮỰỲÝỶỸỴĐ].*[^.!?]$");

            return isAllCaps || hasLargeFont || (isTitleCase && trimmed.length() < 80);
        }

        public List<TextBlock> getTextBlocks() {
            return textBlocks;
        }
    }

    // Lưu info từng ký tự
    static class CharacterInfo {
        String character;
        float x;
        float y;
        float width;
        float fontSize;

        CharacterInfo(String ch, float x, float y, float width, float fontSize) {
            this.character = ch;
            this.x = x;
            this.y = y;
            this.width = width;
            this.fontSize = fontSize;
        }
    }
}