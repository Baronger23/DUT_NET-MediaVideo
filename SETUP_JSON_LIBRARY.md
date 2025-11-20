# 📦 HƯỚNG DẪN THÊM ORG.JSON LIBRARY

## ⚠️ VẤN ĐỀ

Code TCP Socket đang bị lỗi compile vì thiếu thư viện `org.json`:
```
The import org.json cannot be resolved
JSONObject cannot be resolved to a type
```

## ✅ GIẢI PHÁP - CÓ 3 CÁCH

---

## **CÁCH 1: Tải JAR file thủ công** (KHUYẾN NGHỊ - DỄ NHẤT)

### Bước 1: Download JSON library
1. Truy cập: https://mvnrepository.com/artifact/org.json/json/20240303
2. Click vào **"Files" → "jar"**
3. Download file: `json-20240303.jar`

### Bước 2: Thêm vào project
1. Copy file `json-20240303.jar` vào folder:
   ```
   E:\K1N3\LTM\DUT_NET-MediaVideo\src\main\webapp\WEB-INF\lib\
   ```

2. Trong Eclipse:
   - Right-click project **DUT_NET-MediaVideo**
   - Chọn **Build Path → Configure Build Path**
   - Tab **Libraries** → Click **Add JARs...**
   - Navigate to: `src/main/webapp/WEB-INF/lib/json-20240303.jar`
   - Click **OK**

3. Clean project:
   ```
   Project → Clean... → Clean all projects → OK
   ```

### Bước 3: Verify
Code sẽ compile thành công và không còn lỗi!

---

## **CÁCH 2: Dùng Maven** (NẾU DỰ ÁN DÙNG MAVEN)

### Thêm dependency vào `pom.xml`:
```xml
<dependencies>
    <!-- JSON Library -->
    <dependency>
        <groupId>org.json</groupId>
        <artifactId>json</artifactId>
        <version>20240303</version>
    </dependency>
</dependencies>
```

### Sau đó:
```
Right-click project → Maven → Update Project
```

---

## **CÁCH 3: Download trực tiếp JAR**

### Link download JAR:
```
https://repo1.maven.org/maven2/org/json/json/20240303/json-20240303.jar
```

### Sau khi download:
- Copy vào `WEB-INF/lib/`
- Add to Build Path như Cách 1

---

## 🎯 KIỂM TRA SAU KHI CÀI ĐẶT

### Test import:
```java
import org.json.JSONObject;

public class TestJSON {
    public static void main(String[] args) {
        JSONObject obj = new JSONObject();
        obj.put("message", "Hello TCP Socket!");
        System.out.println(obj.toString());
    }
}
```

### Output mong đợi:
```
{"message":"Hello TCP Socket!"}
```

---

## 📋 CHECKLIST

- [ ] Download `json-20240303.jar`
- [ ] Copy vào `src/main/webapp/WEB-INF/lib/`
- [ ] Add to Build Path trong Eclipse
- [ ] Clean project
- [ ] Verify không còn lỗi compile
- [ ] Test code với JSONObject

---

## 🚀 SAU KHI XONG

Code TCP Socket sẽ compile thành công và bạn có thể:

1. **Start Worker TCP Server:**
   ```bash
   cd E:\K1N3\LTM\DUT_NET-MediaVideo
   start-worker-server.bat
   ```

2. **Start Tomcat Web Server**

3. **Test upload qua TCP:**
   ```
   http://localhost:8080/DUT_NET-MediaVideo/upload-tcp
   ```

---

## ❓ NẾU VẪN BỊ LỖI

### Kiểm tra:
1. File JAR có đúng trong `WEB-INF/lib/` không?
2. Eclipse đã refresh project chưa? (F5)
3. Đã Clean project chưa?
4. Tomcat đã được clean chưa? (Right-click → Clean...)

### Solution:
```
1. Right-click project → Refresh
2. Project → Clean → Clean all projects
3. Right-click Tomcat server → Clean...
4. Restart Eclipse (nếu cần)
```

---

**LƯU Ý:** Thư viện `org.json` là thư viện open-source, free, và được sử dụng rộng rãi trong các dự án Java. File JAR chỉ ~70KB, rất nhẹ!
