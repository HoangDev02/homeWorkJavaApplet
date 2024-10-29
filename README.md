**Các file cần cài đặt:**  :  [Link Text](https://www.oracle.com/java/technologies/javacard-downloads.html#license-lightbox) 
-    Java version 17 
-    Java Card Development Kit Tools 24.1
-    Java Card Development Kit Simulator 24.0 for Windows
-    Java Card Development Kit Eclipse Plug-in 24.0 
-    Cài Win32 OpenSSL v3.4.0 [Link Text](https://slproweb.com/products/Win32OpenSSL.html) 

**Chạy dự án bằng terminal:**  [Link Hướng dẫn cài đặt](https://drive.google.com/file/d/1DMtVeh24Gk9Xrtu_jqztCyt5Dc6QaNJD/view?usp=sharing)  
1. Tải file .zip và giải nén  
2. Chạy server (Vào file Java Card Development Kit Simulator 24.0 for Windows /runtime/bin/jcsw.exe)  
3. Mở terminal và nhập `build.bat`  
4. Sau khi `build.bat` thành công, nhập `run.bat`  
5. Nhập Input applet: `DATA-ABCD`  
6. Nhập Input client: `DATA-ABCD`  

**Chạy dự án bằng Eclipse:**  
[Link Hướng dẫn cài đặt](https://drive.google.com/file/d/1Kn_0sWOy0Fmoek7bo8KOih0ktMtD6TXK/view?usp=sharing)  
1. File -> import -> General -> Project from Folder or Archive -> Directory... -> chọn Folder dự án đã tải về -> Select Folder -> Finish  
2. Click chuột phải vào `homeWordApplet` -> run as -> run configurations -> Java Card Project Run -> `homeWordApplet` -> run  
3. Click chuột phải vào `homeWorkClient` -> run configuration -> Java application -> `AMSMyApplet` -> Arguments -> Program arguments -> thay thế bằng đường dẫn của file -> run  
4. Nhập Input applet: `data-abcd` -> enter  
5. Nhập Input client: `data-abcd` -> enter  