# TURKUAZ AI — Android Beta İstemcisi (Faz 3, Faz 2 atlanarak)

Kotlin (native) + Jetpack Compose. Harici JSON/network kütüphanesi
(Retrofit/OkHttp/Moshi/Gson) **yok** — `HttpURLConnection` + `org.json`
(Android SDK'sının parçası) kullanıldı; bağımlılık yüzeyi küçük tutuldu
(Windows istemcisindeki "NuGet paketi yok" yaklaşımıyla aynı ruh).
AndroidX/Compose/coroutines bağımlılıkları normal Android geliştirmede
kaçınılmaz olduğu için dahil edildi.

## Açma / Çalıştırma

En kolayı: **Android Studio ile açın** ("Open" → `TurkuazBeta` klasörü).
Android Studio, eksik olan Gradle wrapper JAR dosyasını otomatik
tamamlayacaktır (`gradle/wrapper/gradle-wrapper.properties` zaten hazır,
Gradle 8.9 işaret ediyor).

Komut satırından denemek isterseniz, önce kendi Gradle kurulumunuzla
wrapper'ı oluşturun:
```bash
cd TurkuazBeta
gradle wrapper --gradle-version 8.9
./gradlew assembleDebug
```

> **Not:** Bu sandbox'ta ne Android SDK ne de internet var, dolayısıyla
> gerçek bir Gradle sync/derleme yapamadım. Kodu elle gözden geçirdim,
> parantez/blok dengesini otomatik kontrol ettim, kullanılan Compose
> API'lerinin (TopAppBar, AssistChip, NavigationBar) hangi Material3
> sürümünde stabil olduğunu bilerek `@OptIn(ExperimentalMaterial3Api::class)`
> ekledim. Yine de ilk gerçek derlemeyi siz yapacaksınız — küçük bir API
> uyuşmazlığı çıkarsa hata mesajını paylaşın, birlikte düzeltelim.

## Sunucu adresi

`data/Config.kt` içinde `BASE_URL`, varsayılan `http://10.0.2.2:8000`
(Android emulator'den host makinenin localhost'una erişim takma adı).
**Fiziksel cihazda test için** bilgisayarınızın LAN IP'sini yazın (ör.
`http://192.168.1.20:8000`) — telefon ve sunucu aynı ağda olmalı.
Manifest'te `usesCleartextTraffic="true"` bilerek açık — MVP'de düz HTTP
ile yerel sunucuya bağlanabilmek için; production'da sunucu HTTPS'e
taşınınca bu satır kaldırılmalı.

## İlk kullanıcı

Sunucudaki `/api/v1/auth/register` ucu henüz uygulamadan çağrılmıyor
(giriş ekranı sadece login yapıyor). Test için önce `turkuaz-core`'un
`/docs` (Swagger) arayüzünden ya da `curl` ile bir kullanıcı kaydedin:
```bash
curl -X POST http://localhost:8000/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test1234","device_fingerprint":"manual-test"}'
```

## Ekranlar (Bölüm 3.1 ile eşleşme)

| Özellik | Karşılığı |
|---|---|
| Standart sohbet arayüzü | `ChatScreen.kt` → `/api/v1/chat` |
| Kişisel kalıcı hafıza (görüntüle/sil/dışa aktar) | `MemoryScreen.kt` → `/api/v1/memory/me/*` |
| Kontrollü öğrenme adayı gönderimi ("şunu öğren") | `LearnScreen.kt` → `/api/v1/memory/learn-candidate` (**doğrudan Master Memory'ye yazmaz**, yönetici kuyruğuna gider) |
| Uygulama içi geri bildirim / hata raporlama | `FeedbackScreen.kt` → `/api/v1/feedback` (Sistem Günlüğü'ne düşer, Windows Admin'de görünür) |
| Erken/deneysel özellik bayrakları | `MainActivity.kt` → `/api/v1/feature-flags` (örnek: `beta_experimental_banner`) |
| Beta sürüm etiketi kullanıcıya görünür | Giriş ekranı + üst çubukta "BETA" rozeti |

## Sunucu tarafında bu istemci için eklenen uçlar

Bu istemciyi desteklemek için `turkuaz-core`'a şunlar eklendi (zip
güncellendi, tekrar indirin):
- `GET /api/v1/feature-flags` — herhangi bir giriş yapmış kullanıcı
- `GET/PUT /api/v1/admin/feature-flags[/{key}]` — sadece admin (bayrak aç/kapat)
- `POST /api/v1/feedback` — geri bildirim/hata bildirimi, SystemLog'a yazılır

**Not:** Windows Yönetici uygulamasında henüz bayrakları açıp kapatan bir
ekran yok — API hazır ama UI eklenmedi (isterseniz sıradaki adım olarak
ekleyebiliriz).

## Bilerek Faz 2'yi (Tam Sürüm) atlamanın sonucu

Doküman Bölüm 3.1'de Beta'nın "Tam Sürüm üzerine katman katman" eklendiği
belirtiliyor. Faz 2 atlandığı için bu istemci sohbet + kişisel hafıza
temel akışını **kendisi** implemente ediyor; offline-first yaklaşım
(Bölüm 2.1 — internetsiz geçmiş sohbet görüntüleme) bilerek **eklenmedi**
çünkü o Tam Sürüm'ün özelliği. İsterseniz sonradan basit bir yerel cache
(Room ya da düz SharedPreferences) ile eklenebilir.

## Bilinen sınırlamalar

- Refresh token akışı yok (401 alınca kullanıcı tekrar login olmalı).
- Cihaz parmak izi `ANDROID_ID` tabanlı — fabrika sıfırlamada değişir,
  production için daha güçlü bir kimlik değerlendirilmeli (Windows
  istemcisindeki notla aynı).
- Launcher icon olarak platformun varsayılan simgesi kullanılıyor
  (`@android:style/Theme.Material.Light.NoActionBar` teması) — gerçek bir
  uygulama ikonu eklemek isterseniz Android Studio'nun Image Asset
  aracını kullanabilirsiniz.
