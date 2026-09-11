# لعبة Subway Game — تعليمات التشغيل

اللعبة جاهزة بالكامل (الكود + الصور + إعدادات البناء). فيه طريقتين لتشغيلها:

---

## الطريقة 1: بناء APK بدون تثبيت أي برنامج (GitHub Actions)

1. سوّي حساب على github.com (لو ما عندك)
2. اضغط **+** فوق يمين الصفحة → **New repository**
   - الاسم: `SubwayGame`
   - خلّيه **Public**
   - ما تحط صح على "Add README"
   - اضغط **Create repository**
3. بالصفحة اللي بتفتح، دوس على رابط **"uploading an existing file"**
4. فكّ الضغط عن هاد الملف (SubwayGame.zip) بجهازك، واسحب **كل المحتويات جوا مجلد SubwayGame** (مو المجلد نفسه، محتوياته: app, .github, build.gradle, settings.gradle...) لصفحة الرفع
5. اضغط **Commit changes** تحت الصفحة
6. روح لتبويب **Actions** فوق بالمستودع — رح تلاقي عملية بناء شغالة تلقائيًا (اسمها Build APK)، استنى لحد ما تخلص (دائرة خضراء ✓)
7. دوس عليها، وتحت بقسم **Artifacts** رح تلاقي ملف اسمه `subway-game-apk` — حمّله، وجواه ملف الـ APK جاهز

انسخ الـ APK لجوالك (عبر واتساب لنفسك، أو تليجرام، أو كابل) وثبّته (لازم تفعّل "السماح بالتثبيت من مصادر غير معروفة" أول مرة).

---

## الطريقة 2: عن طريق Android Studio على الكمبيوتر

## 1. نزّل Android Studio
من https://developer.android.com/studio (لو ما عندك أصلًا).

## 2. أنشئ مشروع جديد
- File → New → New Project
- اختر **Empty Views Activity**
- الاسم: `SubwayGame`
- اللغة: **Kotlin**
- Minimum SDK: **API 24** أو أعلى
- اضغط Finish وخلي Android Studio يخلص المزامنة (Gradle Sync) أول مرة

## 3. انسخ الملفات
من هاد المجلد اللي حمّلته، انسخ:

- `app/src/main/java/com/example/subwaygame/GameView.kt`
- `app/src/main/java/com/example/subwaygame/MainActivity.kt`

→ حطهم مكان نفس الملفات (أو نفس المسار) بمشروعك الجديد
`app/src/main/java/com/example/subwaygame/` (لو الباكج اسمها مختلف عندك، افتح كل ملف وعدّل أول سطر `package ...` ليطابق اسم الباكج عندك)

- كل الصور من `app/src/main/res/drawable/` (فيها 6 صور: player_run1, player_run2, player_jump, obstacle, road_tile, grass_tile)

→ انسخهم لمجلد `app/src/main/res/drawable/` بمشروعك

## 4. AndroidManifest.xml
افتح `app/src/main/AndroidManifest.xml` بمشروعك، وتأكد إن جوا `<application>` موجود:

```xml
android:supportsRtl="true"
```

وإن الـ `MainActivity` هو الشاشة الرئيسية (عادة موجود تلقائيًا من القالب).

## 5. شغّل اللعبة
اضغط زر ▶ Run بالأعلى، اختر جهازك أو محاكي (Emulator)، وجرب:

- **اسحب يمين/يسار** للتنقل بين المسارات
- **اسحب لفوق** للقفز
- **دبّس بأي مكان** لإعادة اللعب بعد ما تخسر

## ملاحظات
- الصور اللي بالمشروع رسمتها بسيطة (Placeholder) عشان تشتغل اللعبة مباشرة بدون ما تحتاج إنترنت أو تسجيل بأي موقع.
- لو حبيت رسومات أحلى، تقدر تحمّل صور من kenney.nl (مجانية) وتستبدل نفس أسماء الملفات بالضبط (player_run1.png, player_run2.png, player_jump.png, obstacle.png, road_tile.png, grass_tile.png) بنفس المقاسات تقريبًا.
- لتصعيب اللعبة أو تسهيلها: بملف GameView.kt دوّر على المتغيرات `speed` و `spawnInterval`.
