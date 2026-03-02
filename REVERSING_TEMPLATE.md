# Solution

## Description of the problem

[Mô tả ngắn gọn về challenge: app làm gì, yêu cầu gì]

## APK Analysis

### 1. APK Structure Analysis

**Screenshot**: [jadx với cấu trúc APK được mở]

- **Package name**: `com.mobisec.xxx`
- **Main Activity**: `com.mobisec.xxx.MainActivity`
- **Key classes**: 
  - `MainActivity` - Entry point
  - `FlagChecker` - Validation logic
  - [Các class khác nếu có]

### 2. AndroidManifest.xml Analysis

**Screenshot**: [AndroidManifest.xml trong jadx]

**Key findings**:
- **Permissions**: 
  - `android.permission.INTERNET` (nếu có)
  - [Các permission khác]
- **Main Activity**: `com.mobisec.xxx.MainActivity`
- **Intent filters**: [Nếu có]
- **Exported components**: [Nếu có]

### 3. Static Analysis - MainActivity

**Screenshot**: [MainActivity.java trong jadx]

**Code analysis**:
```java
[Code từ jadx]
```

**Key observations**:
- [Phân tích code: app làm gì, flow như thế nào]
- [Input được xử lý như thế nào]
- [Gọi hàm nào để validate]

### 4. Static Analysis - FlagChecker / Validation Logic

**Screenshot**: [FlagChecker.java trong jadx]

**Code analysis**:
```java
[Code từ jadx]
```

**Key observations**:
- [Phân tích logic validation]
- [Các điều kiện check]
- [Các hàm helper được sử dụng]
- [Các constant/hardcoded values]

### 5. Resource Analysis

**Screenshot**: [res/values/strings.xml hoặc các resource khác trong jadx]

**Extracted values**:
- `R.string.xxx` = `"value"`
- [Các giá trị khác được extract]

### 6. Helper Methods Analysis

**Screenshot**: [Các hàm helper trong jadx]

**Analysis**:
- `method1()`: [Chức năng, input, output]
- `method2()`: [Chức năng, input, output]
- [Các hàm khác]

## Reverse Engineering Process

### Step 1: [Tên bước]

**Goal**: [Mục tiêu của bước này]

**Analysis**:
- [Phân tích chi tiết]
- [Rút ra kết luận]

**Key findings**:
- [Finding 1]
- [Finding 2]

### Step 2: [Tên bước]

[Lặp lại format trên]

### Step 3: [Tên bước]

[Lặp lại format trên]

## Solution

### Approach

[Giải thích cách tiếp cận: brute force, reverse decrypt, etc.]

### Implementation

**Python script** (hoặc tool khác):
```python
[Code script]
```

**Explanation**:
- [Giải thích từng phần của script]
- [Tại sao approach này hoạt động]

### Result

**Screenshot**: [Output khi chạy script/tool]

**Flag**: `MOBISEC{...}`

## Summary

- **Key vulnerabilities/weaknesses found**: 
  - [Vulnerability 1]
  - [Vulnerability 2]
- **Techniques used**: 
  - Static analysis với jadx
  - [Kỹ thuật khác]
- **Lessons learned**: 
  - [Bài học rút ra]
