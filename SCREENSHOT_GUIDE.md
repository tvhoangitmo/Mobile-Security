# Hướng dẫn thêm Screenshot vào các file Markdown

## Tổng quan

Screenshots giúp minh họa rõ ràng hơn cho các báo cáo. Dưới đây là các vị trí được đề xuất để thêm screenshot cho từng loại challenge.

---

## 📱 Android Development Challenges (AppDev)

### 1. **Sau phần "Description of the problem"**
- **Screenshot**: Giao diện app khi chạy (nếu có UI) hoặc Android Studio project structure
- **Mục đích**: Cho thấy tổng quan về app

### 2. **Sau phần code của AndroidManifest.xml**
- **Screenshot**: AndroidManifest.xml trong Android Studio với các permission/intent-filter được highlight
- **Mục đích**: Minh họa các thay đổi trong manifest

### 3. **Sau phần code của các hàm quan trọng**
- **Screenshot**: Code trong Android Studio với syntax highlighting, đặc biệt là các hàm chính như:
  - `handleIntent()`, `onReceive()`, `onStartCommand()`, etc.
- **Mục đích**: Cho thấy code được viết trong môi trường thực tế

### 4. **Trước phần Flag (hoặc sau phần Development Process)**
- **Screenshot**: Logcat output trong Android Studio với:
  - Các log entries quan trọng
  - Flag được log ra
  - Các bước thực thi (nếu có)
- **Mục đích**: Chứng minh app hoạt động và flag được nhận

### Ví dụ cụ thể cho từng challenge:

#### **filehasher**
- Screenshot AndroidManifest.xml với intent-filter
- Screenshot MainActivity.java với hàm `handleIntent()` và `computeSha256OfUri()`
- Screenshot Logcat với hash được tính và flag nhận được

#### **justlisten**
- Screenshot FlagReceiver.java với hàm `onReceive()`
- Screenshot MainActivity.java với `onStart()` và `onStop()`
- Screenshot Logcat với broadcast được nhận và flag được log

#### **reachingout**
- Screenshot MainActivity.java với HTTP GET/POST code
- Screenshot Logcat với:
  - Server response (HTML form)
  - Computed answer
  - Flag response
- Có thể thêm screenshot của từng bước trong Development Process

#### **justask**
- Screenshot MainActivity.java với hàm `onActivityResult()` và `findStringDeep()`
- Screenshot Logcat với các flag parts được extract

#### **whereareyou**
- Screenshot AndroidManifest.xml với location permissions và service declaration
- Screenshot LocationService.java với `onStartCommand()` và `broadcastLocation()`
- Screenshot Logcat với location được broadcast và flag nhận được

#### **jokeprovider**
- Screenshot MainActivity.java với ContentResolver query code
- Screenshot Logcat với jokes được extract và flag được tạo

#### **unbindable**
- Screenshot MainActivity.java với ServiceConnection và Messenger code
- Screenshot Logcat với message exchange và flag nhận được

#### **serialintent**
- Screenshot MainActivity.java với reflection code (`getDeclaredMethod()`, `setAccessible()`, `invoke()`)
- Screenshot Logcat với flag được extract từ private method

---

## 🔍 Reversing Challenges

### 1. **Sau phần "Description of the problem"**
- **Screenshot**: APK Analyzer hoặc jadx với cấu trúc APK được decompile
- **Mục đích**: Cho thấy cấu trúc của target APK

### 2. **Sau phần phân tích static (nếu có)**
- **Screenshot**: 
  - jadx/APK Analyzer với code đã reverse được highlight
  - Các hàm quan trọng được decompile
  - Hardcoded strings/credentials được tìm thấy
- **Mục đích**: Minh họa quá trình reverse engineering

### 3. **Sau phần exploit code (nếu có)**
- **Screenshot**: Exploit script/code trong editor
- **Mục đích**: Cho thấy cách exploit được implement

### 4. **Trước phần Flag**
- **Screenshot**: 
  - Output khi chạy exploit
  - Logcat với flag (nếu là Android exploit)
  - Terminal output với flag (nếu là Python script)
- **Mục đích**: Chứng minh exploit thành công

### Ví dụ cụ thể:

#### **blockchain**
- Screenshot jadx với code encryption/decryption được reverse
- Screenshot Python script để brute force
- Screenshot terminal output với flag

---

## 💥 Exploitation Challenges

### 1. **Sau phần "Description of the problem"**
- **Screenshot**: APK Analyzer với target APK structure
- **Mục đích**: Cho thấy target app

### 2. **Sau phần Static Analysis**
- **Screenshot**: 
  - jadx/APK Analyzer với hardcoded credentials/endpoints được highlight
  - AndroidManifest.xml với permissions
- **Mục đích**: Minh họa các lỗ hổng được tìm thấy

### 3. **Sau phần Exploit Code**
- **Screenshot**: Exploit app code trong Android Studio
- **Mục đích**: Cho thấy exploit được implement như thế nào

### 4. **Trước phần Flag**
- **Screenshot**: Logcat output với:
  - Request được gửi
  - Server response
  - Flag được log
- **Mục đích**: Chứng minh exploit thành công

### Ví dụ cụ thể:

#### **frontdoor**
- Screenshot jadx với hardcoded credentials (`username=testuser`, `password=passtestuser123`)
- Screenshot AndroidManifest.xml với INTERNET permission
- Screenshot exploit app code trong Android Studio
- Screenshot Logcat với HTTP request và flag response

---

## 📝 Format trong Markdown

Sử dụng format sau để chèn screenshot:

```markdown
![Mô tả ngắn gọn](path/to/screenshot.png)
```

Hoặc với caption:

```markdown
**Screenshot**: [Mô tả]

![Mô tả ngắn gọn](path/to/screenshot.png)
```

### Đề xuất đặt tên file:
- `screenshot_manifest.png` - AndroidManifest.xml
- `screenshot_mainactivity.png` - MainActivity code
- `screenshot_logcat.png` - Logcat output
- `screenshot_jadx.png` - jadx decompiler
- `screenshot_exploit.png` - Exploit code
- `screenshot_output.png` - Output/result

### Đề xuất thư mục:
- Tạo thư mục `screenshots/` trong mỗi challenge folder
- Ví dụ: `appdev/filehasher/screenshots/`

---

## ✅ Checklist cho mỗi file markdown

- [ ] Screenshot tổng quan (project structure hoặc app UI)
- [ ] Screenshot code quan trọng trong Android Studio/jadx
- [ ] Screenshot AndroidManifest.xml (nếu có thay đổi)
- [ ] Screenshot Logcat/output với flag
- [ ] Tất cả screenshots có mô tả rõ ràng
- [ ] Screenshots được đặt ở vị trí logic trong document

---

## 💡 Tips

1. **Chất lượng**: Đảm bảo screenshot rõ ràng, không bị mờ
2. **Highlight**: Có thể dùng annotation để highlight các phần quan trọng
3. **Kích thước**: Không quá lớn, nhưng đủ để đọc được code
4. **Consistency**: Giữ cùng style cho tất cả screenshots
5. **Privacy**: Đảm bảo không có thông tin nhạy cảm trong screenshot
