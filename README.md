<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓  FACULTY OF INFORMATION TECHNOLOGY (DAINAM UNIVERSITY)
    </a>
</h2>
<h2 align="center">
    HỆ THỐNG CẢNH BÁO THỜI GIAN THỰC
</h2>
<div align="center">
    <p align="center">
        <img alt="AIoTLab Logo" width="170" src="https://github.com/user-attachments/assets/711a2cd8-7eb4-4dae-9d90-12c0a0a208a2" />
        <img alt="AIoTLab Logo" width="180" src="https://github.com/user-attachments/assets/dc2ef2b8-9a70-4cfa-9b4b-f6c2f25f1660" />
        <img alt="DaiNam University Logo" width="200" src="https://github.com/user-attachments/assets/77fe0fd1-2e55-4032-be3c-b1a705a1b574" />
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)

</div>


## 📖 1. Giới thiệu hệ thống

Hệ thống cảnh báo thời gian thực sử dụng giao thức UDP cho phép server gửi các cảnh báo đến nhiều client theo thời gian thực thông qua cơ chế multicast.

**Server: Gửi các cảnh báo tùy chỉnh đến một nhóm multicast với các mức độ khác nhau (INFO, WARNING, CRITICAL).**

**Client: Nhận dữ liệu từ nhóm multicast và hiển thị cảnh báo trên giao diện người dùng (GUI). Lưu trữ dữ liệu: Các cảnh báo được lưu vào file văn bản (client.log) để theo dõi lịch sử.**  

Các chức năng chính:

**🖥 Chức năng của Server:**  

- Gửi cảnh báo tùy chỉnh: Cho phép nhập nội dung cảnh báo và chọn mức độ (INFO, WARNING, CRITICAL) để gửi đến tất cả client.  
- Quản lý kết nối: Theo dõi số lượng client đã kết nối và xử lý các thông báo JOIN/LEAVE từ client.  
- Xử lý ACK: Nhận và xử lý xác nhận từ client, tự động gửi lại nếu chưa nhận được ACK.  
- Quản lý lịch sử: Ghi lại các hoạt động vào log (GUI và file server.log).  
- Giao diện người dùng: Cung cấp GUI hiện đại với gradient background, styled buttons và log viewer chi tiết.

**💻 Chức năng của Client:**  

- Kết nối nhóm multicast: Tham gia vào nhóm multicast để nhận dữ liệu từ server.  
- Hiển thị cảnh báo: Nhận và hiển thị các cảnh báo với popup notification đẹp mắt.  
- Giao diện người dùng: Hiển thị các cảnh báo với màu sắc phù hợp theo mức độ (CRITICAL: đỏ, WARNING: cam, INFO: xanh lá).  
- Lưu trữ lịch sử: Lưu các cảnh báo vào file client.log với dấu thời gian.  
- Quản lý trạng thái: Hiển thị trạng thái kết nối và client ID duy nhất.

**🌐 Chức năng hệ thống:**  

- Giao thức UDP Multicast: Sử dụng DatagramSocket và MulticastSocket để gửi/nhận dữ liệu qua nhóm multicast (230.0.0.0:4446).  
- Dữ liệu văn bản: Dữ liệu cảnh báo được truyền dưới dạng chuỗi văn bản với format "ALERT:content".  
- Lưu trữ file: Các cảnh báo được ghi vào file server.log và client.log theo định dạng có dấu thời gian.  
- Xử lý lỗi: Hiển thị thông báo lỗi trên GUI và ghi log chi tiết.




## 🛠️ 2. Công nghệ sử dụng




Các công nghệ được sử dụng để xây dựng hệ thống cảnh báo thời gian thực:  

**Java Core và Multithreading: Sử dụng Thread và AtomicBoolean để xử lý kết nối mạng và quản lý trạng thái.**  

**Java Swing: Xây dựng giao diện người dùng hiện đại cho cả server và client với gradient background và styled components.**

**Java Sockets (UDP): Sử dụng DatagramSocket và MulticastSocket cho giao thức UDP multicast.**

**File I/O: Ghi lịch sử cảnh báo vào file văn bản (server.log, client.log).**

**Custom UI Components: Tạo các button và panel tùy chỉnh với rounded corners và hover effects.**

Hỗ trợ:  

**java.net và java.io: Xử lý kết nối mạng và đọc/ghi file.**

**java.time.LocalDateTime: Tạo dấu thời gian cho các bản ghi log.**  

**javax.swing.text.html: Hiển thị popup alert với định dạng HTML (màu sắc, font chữ).** 

**java.util.concurrent: Sử dụng ScheduledExecutorService để xử lý resend mechanism.**

Không sử dụng cơ sở dữ liệu, đảm bảo ứng dụng nhẹ và dễ triển khai.




## 🚀 3. Hình ảnh các chức năng




## 📝 4. Hướng dẫn cài đặt và sử dụng

### 🔧 Yêu cầu hệ thống

- **Java Development Kit (JDK)**: Phiên bản 8 trở lên
- **Hệ điều hành**: Windows, macOS, hoặc Linux
- **Môi trường phát triển**: IDE (IntelliJ IDEA, Eclipse, VS Code) hoặc terminal/command prompt
- **Bộ nhớ**: Tối thiểu 512MB RAM
- **Dung lượng**: Khoảng 5MB cho mã nguồn và file thực thi
- **Mạng**: Hỗ trợ UDP multicast (230.0.0.0:4446)




## 📦 Cài đặt và triển khai

#### Bước 1: Chuẩn bị môi trường
1. **Kiểm tra Java**: Mở terminal/command prompt và chạy:
   ```bash
   java -version
   javac -version
   ```

Đảm bảo cả hai lệnh đều hiển thị phiên bản Java 8 trở lên.

2. **Tải mã nguồn**: Sao chép thư mục `udpWarning` chứa các file:
- `Server.java`
- `Client.java`
- `LogViewerDialog.java`
- `pom.xml`


#### Bước 2: Biên dịch mã nguồn

1. **Mở terminal** và điều hướng đến thư mục chứa mã nguồn
2. **Biên dịch các file Java**:
   ```bash
   javac -d target/classes src/main/java/udpWarning/*.java
   ```
   Hoặc biên dịch từng file riêng lẻ:
   ```bash
   javac src/main/java/udpWarning/Server.java
   javac src/main/java/udpWarning/Client.java
   javac src/main/java/udpWarning/LogViewerDialog.java
   ```

3. **Kiểm tra kết quả**: Nếu biên dịch thành công, sẽ tạo ra các file `.class` tương ứng.



#### Bước 3: Chạy ứng dụng

**Khởi động Server:**
```bash
java -cp target/classes udpWarning.Server
```
- Giao diện server sẽ hiển thị với gradient header.
- Chọn mức độ cảnh báo từ dropdown (INFO, WARNING, CRITICAL).
- Nhập nội dung cảnh báo và nhấn "Send Alert".
- Server sẽ gửi cảnh báo đến nhóm multicast 230.0.0.0:4446.

**Khởi động Client:**
```bash
java -cp target/classes udpWarning.Client
```

- Mở terminal mới cho mỗi client.
- Client tự động tham gia nhóm multicast và hiển thị popup cảnh báo khi nhận được.

### 🚀 Sử dụng ứng dụng

1.**Server:**
- Chọn mức độ cảnh báo từ dropdown
- Nhập nội dung cảnh báo vào text field
- Nhấn "Send Alert" để gửi đến tất cả client
- Sử dụng "Clear Log" để xóa log hiện tại
- Sử dụng "Check Log" để mở Log Viewer chi tiết

2.**Client:**
- Client tự động kết nối và hiển thị trạng thái
- Khi nhận cảnh báo, popup sẽ hiển thị với màu sắc phù hợp
- Activity log sẽ ghi lại tất cả hoạt động
- Client ID duy nhất được hiển thị trong status panel

## 📚 5. Thông tin liên hệ
Họ tên: Võ Vĩnh Thái.  
Lớp: CNTT 16-01.  
Email: vovinhthai2004@gmail.com

© 2025 AIoTLab, Faculty of Information Technology, DaiNam University. All rights reserved.

---
