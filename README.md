# Manifest Hava (Android / Kotlin / Jetpack Compose)

Tamamen Turkce, uc sayfalik (asagi kaydirmali) tatli hava durumu uygulamasi.

1. Sayfa: sehir, sicaklik, saatlik tahmin. Arka plan senin gorselin, hafifce hareket eder
   (yagmur, kar, simsek, sis, pariltilar).
2. Sayfa: havaya uygun Manifest sarkisi (YouTube'da acilir) + havaya gore giyinmis karakter.
3. Sayfa: yakindaki yerler (kafe, restoran, park, muze...). Dokununca harita acilir.

Dosyalar
- Arka plan gorselleri: app/src/main/res/drawable-nodpi/bg_*.jpg
- Hava kodu -> gorsel: ui/Theme.kt (toScene), uye secimi: memberFor, kiyafet: outfitFor
- Sarkilar: ui/Songs.kt
- Karakter cizimi: ui/Character.kt
- Veri: Open-Meteo (hava), OpenStreetMap/Overpass (yerler). API anahtari gerekmez.

Acma: Android Studio ile klasoru Open > Gradle Sync (JDK 17) > Run.
Emulatorde konum: ... (Extended controls) > Location.
