# Thư Viện Hỗ Trợ Xuất File (Export Libraries)

📅 **Ngày cập nhật:** 18/11/2025

## 📋 Tổng Quan

Document này liệt kê các thư viện Java được sử dụng để hỗ trợ chức năng xuất kết quả Speech-to-Text sang các định dạng file khác nhau (TXT, Word DOCX, PDF).

---

## 📦 Danh Sách Thư Viện

### 1. Apache POI (Tạo File Word/Excel)

#### **poi-5.2.5.jar**
- **Mô tả:** Thư viện chính của Apache POI để làm việc với Microsoft Office documents
- **Chức năng:** Xử lý file Word (.doc), Excel (.xls)
- **Maven Repository:** https://repo1.maven.org/maven2/org/apache/poi/poi/5.2.5/
- **Kích thước:** ~2.9 MB
- **License:** Apache License 2.0

#### **poi-ooxml-5.2.5.jar**
- **Mô tả:** Phần mở rộng của Apache POI hỗ trợ Office Open XML
- **Chức năng:** Xử lý file Word (.docx), Excel (.xlsx), PowerPoint (.pptx)
- **Maven Repository:** https://repo1.maven.org/maven2/org/apache/poi/poi-ooxml/5.2.5/
- **Kích thước:** ~1.9 MB
- **License:** Apache License 2.0
- **Sử dụng cho:** Xuất kết quả sang file Word (.docx)

#### **poi-ooxml-lite-5.2.5.jar**
- **Mô tả:** Schema definitions cho Office Open XML format (phiên bản lite)
- **Chức năng:** Cung cấp các định nghĩa schema cho OOXML, tương thích với POI 5.x
- **Maven Repository:** https://repo1.maven.org/maven2/org/apache/poi/poi-ooxml-lite/5.2.5/
- **Kích thước:** ~5.8 MB
- **License:** Apache License 2.0
- **Phụ thuộc:** **QUAN TRỌNG** - Phải dùng phiên bản 5.2.5 để tương thích với poi-5.2.5

---

### 2. Apache Log4j (Logging Framework)

#### **log4j-api-2.22.0.jar**
- **Mô tả:** API của Apache Log4j 2 - logging framework hiện đại
- **Chức năng:** Cung cấp API interface cho logging
- **Maven Repository:** https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-api/2.22.0/
- **Kích thước:** ~324 KB
- **License:** Apache License 2.0
- **Phụ thuộc:** **QUAN TRỌNG** - Cần thiết cho Apache POI 5.x

#### **log4j-core-2.22.0.jar**
- **Mô tả:** Implementation của Apache Log4j 2
- **Chức năng:** Core implementation của logging framework
- **Maven Repository:** https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-core/2.22.0/
- **Kích thước:** ~1.8 MB
- **License:** Apache License 2.0
- **Phụ thuộc:** **QUAN TRỌNG** - Cần thiết cho Apache POI 5.x

---

### 3. Apache PDFBox (Tạo File PDF)

#### **pdfbox-3.0.1.jar**
- **Mô tả:** Thư viện Java để tạo và xử lý file PDF
- **Chức năng:** Tạo, đọc, chỉnh sửa file PDF
- **Maven Repository:** https://repo1.maven.org/maven2/org/apache/pdfbox/pdfbox/3.0.1/
- **Kích thước:** ~1.9 MB
- **License:** Apache License 2.0
- **Sử dụng cho:** Xuất kết quả sang file PDF

#### **pdfbox-io-3.0.1.jar**
- **Mô tả:** Thư viện I/O utilities cho Apache PDFBox
- **Chức năng:** Cung cấp các utility classes cho I/O operations trong PDF
- **Maven Repository:** https://repo1.maven.org/maven2/org/apache/pdfbox/pdfbox-io/3.0.1/
- **Kích thước:** ~45 KB
- **License:** Apache License 2.0
- **Phụ thuộc:** **QUAN TRỌNG** - Cần thiết cho PDFBox 3.x

#### **fontbox-3.0.1.jar**
- **Mô tả:** Thư viện xử lý font cho Apache PDFBox
- **Chức năng:** Đọc, parse và render font trong PDF
- **Maven Repository:** https://repo1.maven.org/maven2/org/apache/pdfbox/fontbox/3.0.1/
- **Kích thước:** ~1.5 MB
- **License:** Apache License 2.0
- **Phụ thuộc:** **QUAN TRỌNG** - Cần thiết cho PDFBox 3.x

---

### 4. Apache XMLBeans (Xử Lý XML)

#### **xmlbeans-5.1.1.jar**
- **Mô tả:** Thư viện để bind XML Schema sang Java objects
- **Chức năng:** Xử lý XML Schema, compile XSD
- **Maven Repository:** https://repo1.maven.org/maven2/org/apache/xmlbeans/xmlbeans/5.1.1/
- **Kích thước:** ~2.1 MB
- **License:** Apache License 2.0
- **Phụ thuộc:** Cần thiết cho Apache POI OOXML

---

### 5. Apache Commons Libraries (Các Thư Viện Tiện Ích)

#### **commons-logging-1.2.jar**
- **Mô tả:** Thư viện logging wrapper
- **Chức năng:** Cung cấp interface chung cho các logging framework
- **Maven Repository:** https://repo1.maven.org/maven2/commons-logging/commons-logging/1.2/
- **Kích thước:** ~61 KB
- **License:** Apache License 2.0
- **Phụ thuộc:** Cần cho Apache POI và PDFBox

#### **commons-compress-1.26.0.jar**
- **Mô tả:** Thư viện nén và giải nén file
- **Chức năng:** Hỗ trợ nhiều định dạng nén (zip, tar, gzip, bzip2, etc.)
- **Maven Repository:** https://repo1.maven.org/maven2/org/apache/commons/commons-compress/1.26.0/
- **Kích thước:** ~1 MB
- **License:** Apache License 2.0
- **Phụ thuộc:** **QUAN TRỌNG** - Phải dùng v1.26.0+ để tương thích với Apache POI 5.2.5

#### **commons-collections4-4.4.jar**
- **Mô tả:** Thư viện mở rộng Java Collections Framework
- **Chức năng:** Cung cấp thêm các collection types và utilities
- **Maven Repository:** https://repo1.maven.org/maven2/org/apache/commons/commons-collections4/4.4/
- **Kích thước:** ~734 KB
- **License:** Apache License 2.0
- **Phụ thuộc:** Cần cho Apache POI

#### **commons-io-2.15.1.jar**
- **Mô tả:** Thư viện tiện ích cho xử lý I/O (Input/Output)
- **Chức năng:** Cung cấp các utility classes để làm việc với streams, files, readers, writers
- **Maven Repository:** https://repo1.maven.org/maven2/commons-io/commons-io/2.15.1/
- **Kích thước:** ~489 KB
- **License:** Apache License 2.0
- **Phụ thuộc:** **QUAN TRỌNG** - Cần thiết cho Apache POI OOXML

#### **commons-codec-1.16.0.jar**
- **Mô tả:** Thư viện encoding/decoding cho nhiều format
- **Chức năng:** Cung cấp Base64, Hex, Phonetic và URL encoding
- **Maven Repository:** https://repo1.maven.org/maven2/commons-codec/commons-codec/1.16.0/
- **Kích thước:** ~352 KB
- **License:** Apache License 2.0
- **Phụ thuộc:** **QUAN TRỌNG** - Cần thiết cho Apache POI

#### **commons-math3-3.6.1.jar**
- **Mô tả:** Thư viện toán học và thống kê
- **Chức năng:** Cung cấp các hàm toán học, thống kê, xác suất
- **Maven Repository:** https://repo1.maven.org/maven2/org/apache/commons/commons-math3/3.6.1/
- **Kích thước:** ~2.1 MB
- **License:** Apache License 2.0
- **Phụ thuộc:** Cần cho Apache POI

---

### 6. SparseBitSet

#### **SparseBitSet-1.3.jar**
- **Mô tả:** Cấu trúc dữ liệu BitSet tối ưu cho sparse data
- **Chức năng:** Quản lý tập hợp bit một cách hiệu quả
- **Maven Repository:** https://repo1.maven.org/maven2/com/zaxxer/SparseBitSet/1.3/
- **Kích thước:** ~26 KB
- **License:** Apache License 2.0
- **Phụ thuộc:** **QUAN TRỌNG** - Cần thiết cho Apache POI 5.x

---

## 📂 Vị Trí Thư Viện

Tất cả thư viện được đặt trong thư mục:
```
E:\K1N3\LTM\DUT_NET-MediaVideo\src\main\webapp\WEB-INF\lib\
```

## 🔧 Cách Sử Dụng Trong Code

### Import trong Java Class:

```java
// Apache POI - Tạo Word
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

// Apache PDFBox - Tạo PDF
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
```

### Các Class Đã Sử dụng:

1. **ExportService.java** - Service xử lý xuất file
   - `exportToTxt()` - Xuất sang TXT
   - `exportToDocx()` - Xuất sang Word DOCX
   - `exportToPdf()` - Xuất sang PDF

2. **DownloadController.java** - Controller xử lý tải xuống
   - Endpoint: `/download/{taskId}/{format}`
   - Hỗ trợ format: txt, docx, pdf

---

## 📥 Lệnh Tải Thư Viện (Curl)

Để tải lại các thư viện nếu cần:

```bash
cd E:\K1N3\LTM\DUT_NET-MediaVideo\src\main\webapp\WEB-INF\lib

# Apache POI
curl -L -o poi-5.2.5.jar https://repo1.maven.org/maven2/org/apache/poi/poi/5.2.5/poi-5.2.5.jar
curl -L -o poi-ooxml-5.2.5.jar https://repo1.maven.org/maven2/org/apache/poi/poi-ooxml/5.2.5/poi-ooxml-5.2.5.jar
curl -L -o poi-ooxml-lite-5.2.5.jar https://repo1.maven.org/maven2/org/apache/poi/poi-ooxml-lite/5.2.5/poi-ooxml-lite-5.2.5.jar

# Apache Log4j
curl -L -o log4j-api-2.22.0.jar https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-api/2.22.0/log4j-api-2.22.0.jar
curl -L -o log4j-core-2.22.0.jar https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-core/2.22.0/log4j-core-2.22.0.jar

# Apache PDFBox
curl -L -o pdfbox-3.0.1.jar https://repo1.maven.org/maven2/org/apache/pdfbox/pdfbox/3.0.1/pdfbox-3.0.1.jar
curl -L -o pdfbox-io-3.0.1.jar https://repo1.maven.org/maven2/org/apache/pdfbox/pdfbox-io/3.0.1/pdfbox-io-3.0.1.jar
curl -L -o fontbox-3.0.1.jar https://repo1.maven.org/maven2/org/apache/pdfbox/fontbox/3.0.1/fontbox-3.0.1.jar

# XMLBeans
curl -L -o xmlbeans-5.1.1.jar https://repo1.maven.org/maven2/org/apache/xmlbeans/xmlbeans/5.1.1/xmlbeans-5.1.1.jar

# Apache Commons
curl -L -o commons-logging-1.2.jar https://repo1.maven.org/maven2/commons-logging/commons-logging/1.2/commons-logging-1.2.jar
curl -L -o commons-compress-1.26.0.jar https://repo1.maven.org/maven2/org/apache/commons/commons-compress/1.26.0/commons-compress-1.26.0.jar
curl -L -o commons-collections4-4.4.jar https://repo1.maven.org/maven2/org/apache/commons/commons-collections4/4.4/commons-collections4-4.4.jar
curl -L -o commons-io-2.15.1.jar https://repo1.maven.org/maven2/commons-io/commons-io/2.15.1/commons-io-2.15.1.jar
curl -L -o commons-codec-1.16.0.jar https://repo1.maven.org/maven2/commons-codec/commons-codec/1.16.0/commons-codec-1.16.0.jar
curl -L -o commons-math3-3.6.1.jar https://repo1.maven.org/maven2/org/apache/commons/commons-math3/3.6.1/commons-math3-3.6.1.jar

# SparseBitSet
curl -L -o SparseBitSet-1.3.jar https://repo1.maven.org/maven2/com/zaxxer/SparseBitSet/1.3/SparseBitSet-1.3.jar
```

---

## 🔗 Maven Dependencies (Tham khảo)

Nếu dự án sử dụng Maven, có thể thêm vào `pom.xml`:

```xml
<dependencies>
    <!-- Apache POI for Word -->
    <dependency>
        <groupId>org.apache.poi</groupId>
        <artifactId>poi</artifactId>
        <version>5.2.5</version>
    </dependency>
    <dependency>
        <groupId>org.apache.poi</groupId>
        <artifactId>poi-ooxml</artifactId>
        <version>5.2.5</version>
    </dependency>
    
    <!-- Apache Log4j -->
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-api</artifactId>
        <version>2.22.0</version>
    </dependency>
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-core</artifactId>
        <version>2.22.0</version>
    </dependency>
    
    <!-- Apache PDFBox for PDF -->
    <dependency>
        <groupId>org.apache.pdfbox</groupId>
        <artifactId>pdfbox</artifactId>
        <version>3.0.1</version>
    </dependency>
    <dependency>
        <groupId>org.apache.pdfbox</groupId>
        <artifactId>pdfbox-io</artifactId>
        <version>3.0.1</version>
    </dependency>
    <dependency>
        <groupId>org.apache.pdfbox</groupId>
        <artifactId>fontbox</artifactId>
        <version>3.0.1</version>
    </dependency>
    
    <!-- XMLBeans -->
    <dependency>
        <groupId>org.apache.xmlbeans</groupId>
        <artifactId>xmlbeans</artifactId>
        <version>5.1.1</version>
    </dependency>
    
    <!-- Apache Commons -->
    <dependency>
        <groupId>commons-logging</groupId>
        <artifactId>commons-logging</artifactId>
        <version>1.2</version>
    </dependency>
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-compress</artifactId>
        <version>1.26.0</version>
    </dependency>
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-collections4</artifactId>
        <version>4.4</version>
    </dependency>
    <dependency>
        <groupId>commons-io</groupId>
        <artifactId>commons-io</artifactId>
        <version>2.15.1</version>
    </dependency>
    <dependency>
        <groupId>commons-codec</groupId>
        <artifactId>commons-codec</artifactId>
        <version>1.16.0</version>
    </dependency>
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-math3</artifactId>
        <version>3.6.1</version>
    </dependency>
    
    <!-- SparseBitSet -->
    <dependency>
        <groupId>com.zaxxer</groupId>
        <artifactId>SparseBitSet</artifactId>
        <version>1.3</version>
    </dependency>
</dependencies>
```

---

## ⚠️ Lưu Ý Quan Trọng

### 1. Hỗ Trợ Tiếng Việt

- **TXT:** ✅ Hỗ trợ đầy đủ tiếng Việt có dấu (UTF-8)
- **Word (DOCX):** ✅ Hỗ trợ đầy đủ tiếng Việt có dấu
- **PDF:** ⚠️ **Giới hạn**: Do sử dụng font tiêu chuẩn (Standard14Fonts), tiếng Việt có dấu sẽ được chuyển thành không dấu

### 2. Cải Thiện Hỗ Trợ Tiếng Việt cho PDF

Để hỗ trợ tiếng Việt có dấu trong PDF, cần:
1. Tải font Unicode hỗ trợ tiếng Việt (VD: Arial Unicode, Roboto)
2. Nhúng font vào file PDF
3. Sử dụng `PDType0Font` thay vì `PDType1Font`

Ví dụ:
```java
// Tải font hỗ trợ Unicode
File fontFile = new File("path/to/unicode-font.ttf");
PDType0Font font = PDType0Font.load(document, fontFile);
contentStream.setFont(font, 12);
```

### 3. Kích Thước File

- **TXT:** Nhỏ nhất (~1-10 KB cho văn bản ngắn)
- **Word (DOCX):** Trung bình (~20-50 KB cho văn bản ngắn)
- **PDF:** Trung bình (~10-30 KB cho văn bản ngắn)

### 4. Performance

- **TXT:** Nhanh nhất (chỉ ghi text thuần)
- **Word:** Trung bình (cần xử lý XML và format)
- **PDF:** Trung bình (cần render layout và font)

---

## 🔄 Phiên Bản & Tương Thích

| Thư viện | Phiên bản | Java Version | Servlet API |
|----------|-----------|--------------|-------------|
| Apache POI | 5.2.5 | Java 8+ | Jakarta EE 10 |
| Apache PDFBox | 3.0.1 | Java 8+ | Jakarta EE 10 |
| XMLBeans | 5.1.1 | Java 8+ | - |
| Commons Logging | 1.2 | Java 6+ | - |
| Commons Compress | 1.24.0 | Java 8+ | - |
| Commons Collections4 | 4.4 | Java 8+ | - |

---

## 📚 Tài Liệu Tham Khảo

- **Apache POI:** https://poi.apache.org/
- **Apache PDFBox:** https://pdfbox.apache.org/
- **Apache XMLBeans:** https://xmlbeans.apache.org/
- **Apache Commons:** https://commons.apache.org/

---

## 🐛 Troubleshooting

### Lỗi "NoClassDefFoundError"
**Nguyên nhân:** Thiếu thư viện phụ thuộc

**Giải pháp:** 
1. Kiểm tra tất cả 8 file JAR đã có trong thư mục `WEB-INF/lib`
2. Refresh project trong Eclipse (F5)
3. Clean và Build lại project

### Lỗi "Cannot resolve import"
**Nguyên nhân:** Eclipse chưa nhận diện thư viện mới

**Giải pháp:**
1. Right-click vào project → **Refresh** (F5)
2. Menu: **Project** → **Clean...**
3. Nếu vẫn lỗi: Right-click project → **Properties** → **Java Build Path** → **Libraries** → **Add JARs**

### PDF hiển thị sai tiếng Việt
**Nguyên nhân:** Font tiêu chuẩn không hỗ trợ tiếng Việt có dấu

**Giải pháp:** Xem phần "Cải Thiện Hỗ Trợ Tiếng Việt cho PDF" ở trên

---

## ✅ Checklist Cài Đặt

- [x] Tải 8 file JAR về thư mục `WEB-INF/lib`
- [x] Tạo `DownloadController.java`
- [x] Tạo `ExportService.java`
- [x] Cập nhật `history.jsp` với nút tải xuống
- [ ] Refresh project trong Eclipse
- [ ] Clean và Build project
- [ ] Test tải xuống TXT
- [ ] Test tải xuống Word
- [ ] Test tải xuống PDF

---

**📝 Ghi chú:** Document này được tạo tự động khi implement chức năng xuất file cho dự án DUT_NET-MediaVideo.
