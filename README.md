# 📘 Project Blueprint: Hanzi 2-Step Quiz App

**[!] NOTE FOR AI CODING ASSISTANTS:**  
*This `README.md` is designed to be the foundational context file for this repository. When assisting with code generation, please strictly follow the business logic, database schema, and user flows outlined below.*

---

## 🎯 1. Project Overview
Đây là một ứng dụng di động hỗ trợ học từ vựng tiếng Trung (Chữ Hán) với cơ chế **"Tự nạp dữ liệu - Tự kiểm tra"**. Điểm khác biệt cốt lõi của ứng dụng là quy trình **Kiểm tra 2 bước (2-Step Validation)** cho mỗi từ vựng để đảm bảo người dùng nhớ cả cách đọc (Pinyin) lẫn ý nghĩa (Meaning).

## ⚙️ 2. Core Features & Business Logic

### A. Nhập liệu (Data Entry)
Người dùng tự nhập từ vựng sau mỗi buổi học.
- **Fields bắt buộc:** `Chữ Hán` (Hanzi), `Pinyin`, `Từ loại` (Word Type), `Nghĩa` (Meaning).
- *Lưu ý cho AI:* Ưu tiên UI tối giản, hỗ trợ focus auto-next giữa các TextFields để nhập liệu nhanh.

### B. Luồng Ôn Tập (The 2-Step Quiz Flow)
Đây là logic cốt lõi nhất của ứng dụng. Trạng thái của quiz phải được quản lý chặt chẽ.

1. **Khởi tạo Quiz:** Lấy danh sách từ vựng cần ôn (Flashcards).
2. **Hiển thị Flashcard:** Hiện `Chữ Hán` to ở chính giữa.
3. **Step 1: Xác nhận Pinyin (Pinyin Validation)**
   - Hệ thống render 4 nút đáp án Pinyin (1 đúng, 3 sai lấy random từ DB).
   - **User chọn Đúng:** Chuyển ngay sang Step 2.
   - **User chọn Sai:** Báo lỗi (đổi màu nút thành đỏ, rung màn hình), hiện đáp án đúng, đánh dấu từ này là "Failed" và đẩy xuống cuối queue để lặp lại vào cuối session.
4. **Step 2: Xác nhận Ý nghĩa (Meaning Validation)**
   - Vẫn giữ nguyên `Chữ Hán` trên màn hình. Màn hình thay đổi 4 nút đáp án thành Nghĩa tiếng Việt (1 đúng, 3 sai random).
   - **User chọn Đúng:** Đánh dấu thẻ từ là "Passed", lưu tiến độ, chuyển sang `Chữ Hán` tiếp theo.
   - **User chọn Sai:** Xử lý giống như chọn sai ở Step 1 (Failed và đẩy xuống cuối queue).

---

## 🗄️ 3. Database Schema (SQLite)

Dự án sử dụng Database Local (SQLite/Isar/Room tùy framework). Dưới đây là schema chính:

```sql
CREATE TABLE vocabulary (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    hanzi TEXT NOT NULL,         -- Chữ Hán (e.g., "学校")
    pinyin TEXT NOT NULL,        -- Phiên âm (e.g., "xuéxiào")
    word_type TEXT,              -- Từ loại (e.g., "Danh từ")
    meaning TEXT NOT NULL,       -- Nghĩa tiếng Việt (e.g., "Trường học")
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_reviewed_at TIMESTAMP,  -- Lần cuối ôn tập
    review_status INTEGER        -- 0: New, 1: Learning, 2: Mastered
);
```

*Note to AI:* Khi generate logic tạo đáp án sai (Distractors), hãy viết hàm `get_random_distractors(current_id, field_type, limit=3)` query các record khác trong bảng `vocabulary` loại trừ `current_id`.

---

## 🛠️ 4. Tech Stack & Architecture

- **Frontend:** Flutter / React Native (Tùy chọn) - Ưu tiên State Management (như Provider/Riverpod trong Flutter hoặc Redux/Zustand trong RN) để quản lý trạng thái 2-step của Quiz.
- **Local Database:** SQLite (sqflite / sqlite3).
- **Architecture Pattern:** MVVM (Model-View-ViewModel) hoặc BLoC để tách biệt giao diện Quiz và logic lấy dữ liệu/kiểm tra đáp án.

---

## 🚀 5. Roadmap & Future Features (Do Not Implement Yet)
*Lưu ý: Chỉ thiết kế code để có thể mở rộng, chưa implement ở Phase 1.*
1. **Auto-fill Pinyin:** Tự động parse Hanzi sang Pinyin thông qua API/Local Library.
2. **Spaced Repetition System (SRS):** Tích hợp thuật toán Anki (SM-2) để tính ngày ôn tập tiếp theo.
3. **Text-to-Speech (TTS):** Đọc âm thanh khi chọn đúng Pinyin.
