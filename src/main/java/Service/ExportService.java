package Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

/**
 * Service xử lý xuất kết quả Speech-to-Text sang các định dạng file
 */
public class ExportService {
    
    /**
     * Xuất sang file TXT đơn giản
     */
    public byte[] exportToTxt(String content) throws Exception {
        if (content == null) {
            content = "";
        }
        return content.getBytes(StandardCharsets.UTF_8);
    }
    
    /**
     * Xuất sang file Word (DOCX) sử dụng Apache POI
     */
    public byte[] exportToDocx(String content, String sourceFileName) throws Exception {
        XWPFDocument document = new XWPFDocument();
        
        try {
            // Thêm tiêu đề
            XWPFParagraph titleParagraph = document.createParagraph();
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText("KẾT QUẢ SPEECH-TO-TEXT");
            titleRun.setBold(true);
            titleRun.setFontSize(16);
            titleRun.addBreak();
            
            // Thêm thông tin file gốc
            XWPFParagraph infoParagraph = document.createParagraph();
            XWPFRun infoRun = infoParagraph.createRun();
            infoRun.setText("File gốc: " + sourceFileName);
            infoRun.setFontSize(10);
            infoRun.addBreak();
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            infoRun.setText("Thời gian xuất: " + LocalDateTime.now().format(formatter));
            infoRun.addBreak();
            infoRun.addBreak();
            
            // Thêm nội dung chính
            XWPFParagraph contentParagraph = document.createParagraph();
            XWPFRun contentRun = contentParagraph.createRun();
            
            if (content != null && !content.isEmpty()) {
                // Xử lý xuống dòng
                String[] lines = content.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    contentRun.setText(lines[i]);
                    if (i < lines.length - 1) {
                        contentRun.addBreak();
                    }
                }
            } else {
                contentRun.setText("(Không có nội dung)");
            }
            contentRun.setFontSize(12);
            
            // Chuyển sang byte array
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);
            return out.toByteArray();
            
        } finally {
            document.close();
        }
    }
    
    /**
     * Xuất sang file PDF sử dụng Apache PDFBox
     */
    public byte[] exportToPdf(String content, String sourceFileName) throws Exception {
        PDDocument document = new PDDocument();
        
        try {
            PDPage page = new PDPage();
            document.addPage(page);
            
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            // Sử dụng font hỗ trợ Unicode (cần file font riêng cho tiếng Việt có dấu)
            // Nếu không có font tiếng Việt, dùng font tiêu chuẩn
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            
            float margin = 50;
            float yPosition = page.getMediaBox().getHeight() - margin;
            float fontSize = 12;
            float leading = 1.5f * fontSize;
            
            // Tiêu đề
            contentStream.beginText();
            contentStream.setFont(fontBold, 16);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("KET QUA SPEECH-TO-TEXT");
            contentStream.endText();
            
            yPosition -= leading * 2;
            
            // Thông tin file
            contentStream.beginText();
            contentStream.setFont(font, 10);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("File goc: " + sanitizeForPdf(sourceFileName));
            contentStream.endText();
            
            yPosition -= leading;
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            contentStream.beginText();
            contentStream.setFont(font, 10);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Thoi gian xuat: " + LocalDateTime.now().format(formatter));
            contentStream.endText();
            
            yPosition -= leading * 2;
            
            // Nội dung
            contentStream.setFont(font, fontSize);
            
            if (content != null && !content.isEmpty()) {
                String[] lines = content.split("\n");
                float pageWidth = page.getMediaBox().getWidth() - 2 * margin;
                
                for (String line : lines) {
                    // Xử lý ký tự đặc biệt cho PDF
                    line = sanitizeForPdf(line);
                    
                    // Chia dòng nếu quá dài
                    String[] wrappedLines = wrapText(line, font, fontSize, pageWidth);
                    
                    for (String wrappedLine : wrappedLines) {
                        // Kiểm tra còn chỗ trên trang không
                        if (yPosition < margin + leading) {
                            contentStream.close();
                            page = new PDPage();
                            document.addPage(page);
                            contentStream = new PDPageContentStream(document, page);
                            contentStream.setFont(font, fontSize);
                            yPosition = page.getMediaBox().getHeight() - margin;
                        }
                        
                        contentStream.beginText();
                        contentStream.newLineAtOffset(margin, yPosition);
                        contentStream.showText(wrappedLine);
                        contentStream.endText();
                        
                        yPosition -= leading;
                    }
                }
            } else {
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("(Khong co noi dung)");
                contentStream.endText();
            }
            
            contentStream.close();
            
            // Chuyển sang byte array
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
            
        } finally {
            document.close();
        }
    }
    
    /**
     * Loại bỏ ký tự đặc biệt không hỗ trợ trong PDF font tiêu chuẩn
     */
    private String sanitizeForPdf(String text) {
        if (text == null) return "";
        
        // Chuyển ký tự tiếng Việt có dấu sang không dấu cho PDF
        text = text.replaceAll("[àáảãạăắằẳẵặâấầẩẫậ]", "a");
        text = text.replaceAll("[ÀÁẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬ]", "A");
        text = text.replaceAll("[èéẻẽẹêếềểễệ]", "e");
        text = text.replaceAll("[ÈÉẺẼẸÊẾỀỂỄỆ]", "E");
        text = text.replaceAll("[ìíỉĩị]", "i");
        text = text.replaceAll("[ÌÍỈĨỊ]", "I");
        text = text.replaceAll("[òóỏõọôốồổỗộơớờởỡợ]", "o");
        text = text.replaceAll("[ÒÓỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢ]", "O");
        text = text.replaceAll("[ùúủũụưứừửữự]", "u");
        text = text.replaceAll("[ÙÚỦŨỤƯỨỪỬỮỰ]", "U");
        text = text.replaceAll("[ỳýỷỹỵ]", "y");
        text = text.replaceAll("[ỲÝỶỸỴ]", "Y");
        text = text.replaceAll("[đ]", "d");
        text = text.replaceAll("[Đ]", "D");
        
        // Loại bỏ toàn bộ ký tự không thuộc ASCII cơ bản (0-127)
        // Chỉ giữ lại chữ cái, số, dấu câu và khoảng trắng cơ bản
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            // Chỉ giữ ký tự ASCII printable (32-126)
            if (c >= 32 && c <= 126) {
                result.append(c);
            } else if (c == '\n' || c == '\r' || c == '\t') {
                // Giữ các ký tự xuống dòng và tab
                result.append(c);
            } else {
                // Thay thế ký tự không hỗ trợ bằng khoảng trắng
                result.append(' ');
            }
        }
        
        return result.toString();
    }
    
    /**
     * Chia văn bản thành nhiều dòng dựa trên độ rộng
     */
    private String[] wrapText(String text, PDType1Font font, float fontSize, float width) throws Exception {
        java.util.List<String> lines = new java.util.ArrayList<>();
        
        // Sanitize text trước khi tính toán width
        text = sanitizeForPdf(text);
        
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            
            try {
                float textWidth = font.getStringWidth(testLine) / 1000 * fontSize;
                
                if (textWidth > width && currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    currentLine = new StringBuilder(testLine);
                }
            } catch (IllegalArgumentException e) {
                // Nếu vẫn có ký tự không hỗ trợ, thay thế bằng "?"
                testLine = testLine.replaceAll("[^\\x00-\\x7F]", "?");
                float textWidth = font.getStringWidth(testLine) / 1000 * fontSize;
                
                if (textWidth > width && currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word.replaceAll("[^\\x00-\\x7F]", "?"));
                } else {
                    currentLine = new StringBuilder(testLine);
                }
            }
        }
        
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines.toArray(new String[0]);
    }
}
